package cn.qssq666.dbutilx;

import cn.qssq666.db.anotation.Column;
import cn.qssq666.db.anotation.ColumnType;
import cn.qssq666.db.anotation.ID;
import cn.qssq666.db.anotation.Table;

/**
 * Created by qssq on 2018/4/18 qssq666@foxmail.com
 */
@Table("testtable")
public class GroupWhiteNameBean extends ParentTable {


    public String getTest1() {
        return test1;
    }

    public void setTest1(String test1) {
        this.test1 = test1;
    }

    public int getTest2() {
        return test2;
    }

    public void setTest2(int test2) {
        this.test2 = test2;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getMydb() {
        return mydb;
    }

    public void setMydb(int mydb) {
        this.mydb = mydb;
    }

    public int getMydb1() {
        return mydb1;
    }

    public void setMydb1(int mydb1) {
        this.mydb1 = mydb1;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    /**
     * 你可以添加任意字段，但是不需要升级数据库，因为dbutil自动升级了
     */
    String test1;

    int test2;

    @ID
    int idx;//如果没有这个默认是_id
    int mydb;
    @Column("helo")//列名不以java字段命名
            int mydb1;


    @ColumnType("REAL")
    private String money = "";//虽然这里是字符串，但是存储的时候是小数点哈!  为什么要这么高？这里可以直接实现依然查询大小，但是又不影响java字段.

    @Override
    public String toString() {
        return super.toString()+"{" +
                "test1='" + test1 + '\'' +
                ", test2=" + test2 +
                ", idx=" + idx +
                ", mydb=" + mydb +
                ", mydb1=" + mydb1 +
                ", money='" + money + '\'' +
                '}';
    }
}
