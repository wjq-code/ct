# ct
电信客服(HBase)

电信客服架构：

项目需求：统计每天、每月以及每年的每个人的通话次数及时长。

1.生产数据：call1、call1name、call2、call2name、通话建立时间、通话时长
	用一个List集合做一组随机电话号码的容器
	用一个Map集合做这组随机号码与一组随机人名映射的容器
	用Math.random*list.length获取一个[0，list.length）的随机数字；
	用这个随机数字作为索引从list集合中取出这个随机号码作为主叫号码；
	再从Map中取出对应的姓名
	同理被叫号码也用这个方法取出，但将取出被叫号码的步骤放到一个死循环中，
	在取出被叫号码后与主叫号码比较，如果不相同，跳出循环，防止随机取出的两个号码相同
	同理，通过Math.random的方法根据传入的起始时间和结束时间获得随机的通话建立时间和通话时长(startTime + ((endTime - startTime) * Math.random))
	最后将产生的六个字段按照一定的顺序用输出流写入到文件中，文件路径为传入的配置参数
	
2.flume监听产生的数据，导入到kafka
	编辑flume脚本，将sink的类型配置为kafka。
	a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
	
3.将kafka中的数据导入到hbase
	新建一个项目，用kafkaAPI和HBaseAPI编写Java代码将kafka中的数据导入到hbase中
	构建配置文件，在配置文件中写入kafka和hbase的一些配置信息，包括kafka的brokerlist与消费的主题，hbase的命名空间,表名，分区数等
	构建propertiesutil类用来读取配置文件中的内容，并对外提供获取配置文件内容的接口getProperty()方法
	kafka部分：
		在主类中通过kafkaconsumer的实例对象调用subscribe方法和propertiesutil类提供的getProperty()方法获取kafka的配置信息后消费kafka中的数据
	hbase部分：
		构建HBaseDAO类，在构造方法中用propertiesUtil提供的方法读取hbase相关的配置
		编写connectionUtil类通过传入的conf对象用createConnection()方法获得connection对象
		编写HBaseUtil类，实现命名空间的初始化、表的创建、rowkey的生成、分区号的生成
			命名空间的初始化：
				通过ConnectionFactory.createConnection()获取到connection对象
				通过connection.getAdmin()获取admin对象
				通过NamespaceDescriptor类获取NamespaceDescriptor对象
				通过admin对象调用createNamespace()方法并将NamespaceDescriptor对象传入创建命名空间
			表的创建：
				通过ConnectionFactory.createConnection()获取到connection对象
				通过connection.getAdmin()获取admin对象
				通过传入的表名获取HTableDescriptor对象，并将传入的列族用HTableDescriptor对象添加到HColumnDescriptor对象中
				用admin对象调用createTable方法并利用HTableDescriptor对象与传入的分区数创建表
			分区号的生成：
				通过传入的参数取出手机号的后四位、年月日做离散操作后%分区数生成分区号
			rowkey的生成：
				用传入的分区号、主叫号码、通话建立时间、被叫号码、标记1、通话时间的拼接作为rowkey
		在HBaseDAO类中编写put方法调用HBaseUtil中封装的方法用put对象调用addColumn()方法将HBase中的表创建好并插入数据
	协处理器：
		因为在HBase插入数据时，只插入主叫数据，不考虑被叫数据，但是有可能被叫数据也会有一部分是主叫数据，所以新增加协处理器，
		在协处理器中，一条主叫日志成功插入后，将该日志切换为被叫视角再次插入一次，放入到与主叫日志不同的列族中。
		协处理器继承BaseRegionObserver类，重写postPut()方法
		在postPut()中，用传入的上下文对象e调用getEnvironment().getRegionInfo().getTable().getNameAsString()方法获取之前成功put的表，
		和从配置文件中读取出来的表名进行对比，如果相同，就用用put.getRow()方法将rowkey取出
		然后从rowkey中分割出各个字段，将主叫号码与被叫号码的位置调换，然后将标记改为0，再次插入。
	
4.读取hbase中的数据用mapreduce做处理
	map：
		map类继承TableMapper类，实现从HBase中读取数据到map
		因为处理的数据特殊，所以自定义map的输出类型，以主叫号码和时间两个维度的结合的对象做key的输出类型
		自定义key的输出类型：
			封装一个以主叫手机号和号主姓名为属性的对象；
			封装一个以时间年月日为属性的对象；
			封装一个以以上两个对象为属性的对象作为key的输出类型
			在这几个封装的对象compare方法里实现排序
		在map中，传进来的key为rowkey，所以从将rowkey分割，从中提取出主叫号码、被叫号码、通话建立时间的年月日、通话时长分别封装到自定义的输出类型对象中
		将通话时长作为value，分别以年月日作为纬度封装好的自定义对象作为key输出
		同时聚合主叫数据和被叫数据
	reduce：
		reduce类继承reducer类
		和map相同，reduce的输出类型以主叫号码和时间两个维度的结合的对象做key的输出类型
		自定义value的输出类型：
			封装一个以通话时长和通话次数为属性的对象
		将map传过来的value聚合，统计出通话时长
		同时，定义一个计数器，每聚合一次计数器累加1，统计出通话次数
		将通话时长和通话次数封装到自定义的对象做value，将map传过来的key作为key输出
	driver：
		driver类继承Tools类
		因为读取的数据为HBase中的数据，所以需要实例化admin对象，并通过TableMapReduceUtil.initTableMapperJob()方法初始化mapper(需要初始化scan对象做传入参数)
		
5.将处理后的数据插入Mysql数据库
	mysql表的设计：
		采用星型模型，主表根据主叫联系人和时间两个维度建立字段
		id		contact(联系人，主叫)	date_time(时间)	count（通话次数）	duration（通话时长）
		因为要求每一个id不相同，所以采用主叫联系人和时间拼接的字符串作为id的数据
		将主叫联系人和时间分别作为纬度建立附表
	Util类的编写：
		JDBCUtil类：
			定义JDBC连接器实例化所需要的固定参数
			实例化JDBC连接器对象：DriverManager.getConnection()
			释放连接器资源
		JDBCInstanse类：
			调用JDBCUtil类的实例化连接器返回connection对象
		LRUCache类：
			因为在数据分析结果涉及到数据库多表联合查询，会根据传入的纬度去表中查找对应的主键
			为了增加程序的效率，不应该每次都去数据库中查询主键再返回，而应该将返回的数据缓存到内存中，并且查询最多的主键应该更快速的被查找到
			LRUCache类继承LinkedHashMap类，缓存算法，最近最多使用，底层是一个LinkedHashMap
			在自定义的缓存中，时间纬度对应的缓存数据以拼接的date_dimension_year_month_day字符串做key，MySQL时间附表中对应的主键id作为value
			主叫联系人纬度对应的缓存数据以拼接的contact_dimension_telephone字符串作为key，MySQL主叫联系人附表中对应的主键id作为value
	因为是将数据输出到Mysql，所以需要自定义outputformat。新建Mysqloutputformat类，该类继承自outputformat类
	重写outputformat的三个方法RecordWriter()、checkOutputSpecs()、getOutputCommitter()
	RecordWriter():
		调用JDBCInstance类提供的方法初始化JDBC连接器，并将自动提交改为false
		返回一个自定义mysqlrecordwriter对象，在自定义的MysqlRecordWriter类中实现真正的数据插入MySQL
		RecordWriter:（采用静态内部类方式）
			该类继承自RecordWriter，重写write()和close()方法
			write():
				该方法的传入参数是reducer对应输出的数据，所以从value中将每一条数据对应的通话建立时间和通话时长取出作为数据库的一个字段准备插入主表
				在数据库的主表中，因为时间字段和主叫号码字段的数据是附表中对应时间、对应主叫号码的主键id，所以自定义一个DimensionConverterImpl类来读取数据库的附表并返回对应的主键id
				DimensionConverterImpl：
					编写几个方法处理数据、读取数据库中的数据并做对应的缓存：
					genCacheKey()：
						根据传入参数对应的纬度信息，拼接两个字符串，分别做两个纬度在缓存中的key
					getDimensionID()：
						初始化缓存对象；
						调用genCacheKey()方法，获取到对应纬度在缓存中的key然后通过缓存对象获取缓存中对应的value值id；
						如果缓存中有对应的id，则将id返回到write()方法，如果没有，则用传入的纬度参数去数据库对应的附表中查询主键id，然后加载到缓存中(.put()方法)，然后将id返回给write()方法
						在根据传入的纬度参数去数据库对应的附表中查询主键id时，如果数据库中也没有这条数据，则插入这条数据，再返回（有责返回，无则插入再返回）。
					getConnection():
						得到当前线程维护的Connection对象，供getDimensionID()中查询数据库使用
					......（其他数据库查询插入操作方法）
	checkOutputSpecs()：
		输出校验
	getOutputCommitter()：
		返回提交对象，以确保正确的提交。
		用上下文对象.getConfiguration().get(FileOutputFormat.OUTDIR)获取输出路径
		再通过输出路径和上下文对象实例化FileOutputCommitter的对象获取提交对象
	

6.数据展示
	echarts


