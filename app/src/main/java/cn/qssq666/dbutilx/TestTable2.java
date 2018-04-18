package cn.qssq666.dbutilx;

/**
 * Created by qssq on 2018/4/18 qssq666@foxmail.com
 */
public class TestTable2 {

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }

    public int getXxx() {
        return xxx;
    }

    public void setXxx(int xxx) {
        this.xxx = xxx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    String hello;
    int xxx;
    String name;
    int age;

    @Override
    public String toString() {
        return "TestTable2{" +
                "hello='" + hello + '\'' +
                ", xxx=" + xxx +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
