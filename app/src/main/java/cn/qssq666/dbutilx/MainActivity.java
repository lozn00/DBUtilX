package cn.qssq666.dbutilx;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.qssq666.db.DBUtils;

/**
 * Created by qssq on 2018/4/18 qssq666@foxmail.com
 */
public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);


        //如果数据比较多你可以插入线程
        //通过GroupWhiteNameBean+别名1 查询某个表

        List<GroupWhiteNameBean> list = DBHelper.getQQGroupWhiteNameDBUtil(AppContext.getInstance().getDbUtils()).queryAll(GroupWhiteNameBean.class);

        Log.w(TAG, "查询表" + AppContext.getInstance().getDbUtils().geInnerTableName(GroupWhiteNameBean.class) + "结果如下");

        for (GroupWhiteNameBean groupWhiteNameBean : list) {
            Log.w(TAG, "query:" + groupWhiteNameBean);
        }


        list = DBHelper.getTableclassAlias2(AppContext.getInstance().getDbUtils()).queryAll(GroupWhiteNameBean.class);

        Log.w(TAG, "查询表" + AppContext.getInstance().getDbUtils().geInnerTableName(GroupWhiteNameBean.class) + "结果如下");
        for (GroupWhiteNameBean groupWhiteNameBean : list) {
            Log.w(TAG, "query:" + groupWhiteNameBean);
        }
        //查询另外一个class ,

        List<TestTable2> listTestTable = DBHelper.getNotAliasTable(AppContext.getInstance().getDbUtils()).queryAll(TestTable2.class);

        String tableName =AppContext.getInstance().getDbUtils().geInnerTableName(TestTable2.class);
        Log.w(TAG, "查询表" + tableName + "结果如下");
        for (TestTable2 table2 : listTestTable) {

            Log.w(TAG, "result:" + table2);
        }


        DBUtils.HashMapDBInfo hashMapDBInfo = AppContext.getInstance().getDbUtils().queryAllSaveCollections("select * from " + tableName);


        List<HashMap<String, Object>> listMap = hashMapDBInfo.getList();
        for (int i = 0; i < listMap.size(); i++) {

            HashMap<String, Object> stringObjectHashMap = listMap.get(i);

            int columnIndex = 0;
            for (Map.Entry<String, Object> entry : stringObjectHashMap.entrySet()) {

                Log.w(TAG, "第" + i + "行 " + entry.getKey() + "第" + columnIndex + "列 " + entry.getKey() + "=" + entry.getValue());
                columnIndex++;

            }

        }

    }

}
