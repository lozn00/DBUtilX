package cn.qssq666.dbutilx;

import cn.qssq666.db.DBUtils;

/**
 * Created by qssq on 2018/4/18 qssq666@foxmail.com
 */
public class DBHelper {


    //升级字段的方式多种多样，可以重命名表，也可以直接使用insertNewCloumnFromClasss
//            DBUtils.ToolHelper.reNameTable(dbUtils.getDb(), groupTableOld, groupTableNew);
//            DBUtils.insertNewCloumnFromClasss(DBHelper.getQQGroupWhiteNameDBUtil(dbUtils), GroupWhiteNameBean.class, dbUtils.getDb());
    //改表还需要添加字段,太麻烦了.
//            result = DBHelper.getQQGroupWhiteNameDBUtil(dbUtils).createTable(GroupWhiteNameBean.class);//作废了
    //LogUtil.writeLog("发现老表" + groupTableOld + "存在，尝试导入老表数据到new new table " + groupTableNew + ",result:");

    //同一个class生成多个表

    public static void init(DBUtils dbUtils) {

        boolean exist = DBUtils.ToolHelper.tableExist(dbUtils.getDb(), getQQGroupWhiteNameDBUtil(dbUtils).geInnerTableName(GroupWhiteNameBean.class));

        if (exist) {//标存在,修改为指定new table//如果表已经存在了.
            //多次执行不会出现任何问题
            DBUtils.insertNewCloumnFromClasss(DBHelper.getQQGroupWhiteNameDBUtil(dbUtils), GroupWhiteNameBean.class, dbUtils.getDb());


        } else {

            boolean result = DBHelper.getQQGroupWhiteNameDBUtil(dbUtils).createTable(GroupWhiteNameBean.class);//

            if (result) {
//初始化一些数据
                int count = 10;
                for (int i = 0; i < count; i++) {
                    GroupWhiteNameBean table2 = new GroupWhiteNameBean();
                    table2.setMoney(String.valueOf(1005.5 * i));
                    dbUtils.insert(table2);//已经设置别名了，可以直接这样写。
                    DBHelper.getQQGroupWhiteNameDBUtil(dbUtils).insert(table2);

                }

            }


        }


        exist = DBUtils.ToolHelper.tableExist(dbUtils.getDb(), getTableclassAlias2(dbUtils).geInnerTableName(GroupWhiteNameBean.class));

        if (exist) {//标存在,修改为指定new table//如果表已经存在了.
            //多次执行不会出现任何问题  如果这个table可能 会升级字段的，那么创建表都需要这么写，
            DBUtils.insertNewCloumnFromClasss(DBHelper.getQQGroupWhiteNameDBUtil(dbUtils), GroupWhiteNameBean.class, dbUtils.getDb());
            //LogUtil.writeLog("发现老表" + groupTableOld + "存在，尝试导入老表数据到new new table " + groupTableNew + ",result:");
        } else {//第一次创建，


            boolean result = DBHelper.getQQGroupWhiteNameDBUtil(dbUtils).createTable(GroupWhiteNameBean.class);//
            if (result) {
                int count = 5;
                for (int i = 0; i < count; i++) {
                    GroupWhiteNameBean nameBean = new GroupWhiteNameBean();
                    nameBean.setMoney(String.valueOf(105.5 * i));
                    getTableclassAlias2(dbUtils).insert(nameBean);//已经设置别名了，可以直接这样写。
                    DBHelper.getTableclassAlias2(dbUtils).insert(nameBean);

                }

            }


        }

        dbUtils.clearAlias();//如果不清除那么tablename= 上次getAlias()+这次操作的表名 我这里不需要设置别名了，
        //也可以直接创建
        boolean result = dbUtils.createTable(TestTable2.class);//多次调用不会翻车

        if (result) {

            int count = 10;
            for (int i = 0; i < count; i++) {
                TestTable2 table2 = new TestTable2();
                table2.setAge(i);
                table2.setHello("我是");
                table2.setName("我是编号:" + i);
                dbUtils.insert(table2);
            }

        }


    }

    public static DBUtils getQQGroupWhiteNameDBUtil(DBUtils dbUtils) {
        dbUtils.setAlias("group");//可以不这样写,如果一个class，
        return dbUtils;
    }


    public static DBUtils getTableclassAlias2(DBUtils dbUtils) {
        dbUtils.setAlias("group1");//可以不这样写,如果一个class，
        return dbUtils;
    }

    public static DBUtils getNotAliasTable(DBUtils dbUtils) {
//        dbUtils.setAlias("");
        dbUtils.clearAlias();//写法一样
        return dbUtils;

    }
}