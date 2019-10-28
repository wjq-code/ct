package kv.key;

import kv.base.BaseDimension;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ComDimension extends BaseDimension {
    private ContactDimension contactDimension = new ContactDimension();
    private DateDimension dateDimension = new DateDimension();

    public ComDimension() {
    }

    public ContactDimension getContactDimension() {
        return contactDimension;
    }

    public void setContactDimension(ContactDimension contactDimension) {
        this.contactDimension = contactDimension;
    }

    public DateDimension getDateDimension() {
        return dateDimension;
    }

    public void setDateDimension(DateDimension dateDimension) {
        this.dateDimension = dateDimension;
    }

    @Override
    public int compareTo(BaseDimension o) {
        ComDimension anotherComDimension = (ComDimension) o;

        int result = this.dateDimension.compareTo(anotherComDimension.dateDimension);
        if (result != 0) {
            return result;
        }

        result = this.contactDimension.compareTo(anotherComDimension.contactDimension);
        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        this.dateDimension.write(out);
        this.contactDimension.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.dateDimension.readFields(in);
        this.contactDimension.readFields(in);
    }

    @Override
    public String toString() {
        return "ComDimension{" +
                "contactDimension=" + contactDimension +
                ", dateDimension=" + dateDimension +
                '}';
    }
}
