/**
 * 类名称: Data
 * 类描述:
 *
 * @author 武建谦
 * @Time 2019/4/29 17:44
 */
public class DataDao {
    private String city;
    private String careerAndSalary;
    private String dataTime;

    public DataDao() {
    }

    public DataDao(String city, String careerAndSalary, String dataTime) {
        this.city = city;
        this.careerAndSalary = careerAndSalary;
        this.dataTime = dataTime;
    }

    @Override
    public String toString() {
        return "{" +
                "\"city\":\"" + city + '\"' +
                ",\"careerAndSalary\":\"" + careerAndSalary + '\"' +
                ",\"dataTime\":\"" + dataTime + '\"' +
                '}';
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCareerAndSalary() {
        return careerAndSalary;
    }

    public void setCareerAndSalary(String careerAndSalary) {
        this.careerAndSalary = careerAndSalary;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }
}
