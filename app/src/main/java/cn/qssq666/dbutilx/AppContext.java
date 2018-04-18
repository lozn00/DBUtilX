package cn.qssq666.dbutilx;

import android.app.Application;

import cn.qssq666.db.DBUtils;

/**
 * Created by qssq on 2018/4/18 qssq666@foxmail.com
 */
public class AppContext extends Application {

    private static AppContext instance;

    public DBUtils getDbUtils() {
        return dbUtils;
    }

    private DBUtils dbUtils;

    @Override
    public void onCreate() {
        super.onCreate();

        instance=this;

        dbUtils = new DBUtils(this);

        /**
         * 注意:
         * dbutil可以在任何地方初始化，前提是确保不会调用查询表的方法，否则会引发崩溃，
         * 可以在一个activity onCreate 初始化然后调用  dbUtils.close();关闭数据库, dbutil传递的上下文不要传递activity.
         *
         *
         * dbutils支持自动升级字段
         *
         * dbutils 支持一个class 模型生成不同的表
         * 支持通过注解加别名，生成组合, 别名和 table注解就可以实现一个class无法生成不表的问题啦!
         */

        DBHelper.init(dbUtils);
    }

    public static AppContext getInstance(){
        return instance;
    }
}
