package cn.qssq666.dbutilx;

/**
 * Created by qssq on 2018/4/18 qssq666@foxmail.com
 */
public class ParentTable {

    public String getParentField() {
        return parentField;
    }

    public void setParentField(String parentField) {
        this.parentField = parentField;
    }

    String parentField="woshi parent";

    @Override
    public String toString() {
        return "{" +
                "parentField='" + parentField + '\'' +
                '}';
    }
}
