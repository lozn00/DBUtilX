package cn.qssq666.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import java.lang.reflect.Type;
//import java.util.Iterator;

/**
 * http://supershll.blog.163.com/blog/static/3707043620123153547193/
 *
 * @author luozheng
 *         <p/>
 *         创建表，创建库，插入数据，更新数据 删除数据
 *         <p>
 *         2016-10-27 10:24:23  增加 字段是否存在判断
 *         <p>2017年2月16日 12:49:31 别名 判断bug修复
 *         dbutils是单例的所以操作的时候切换选项卡容易销毁，因此不需要关闭只需要在activity做关闭就行了
 *         2017年7月23日 17:20:03 增加唯一字段
 */
public class DBUtils {
    private static final String TAG = "DBUtils";
    private Context context;

    public void setAlias(String aliasName) {
        this.aliasName = aliasName;
    }

    String aliasName = "";
    private String dbName = "qssq.db";

    public SQLiteDatabase getDb() {
        return mSQLiteDatabase;
    }

    public void setDb(SQLiteDatabase mDb) {
        this.mSQLiteDatabase = mDb;
    }

    private SQLiteDatabase mSQLiteDatabase;

    public SQLiteDatabaseObj getSQLiteDatabaseObj() {
        return sQLiteDatabaseObj;
    }

    public void setsQLiteDatabaseObj(SQLiteDatabaseObj sQLiteDatabaseObj) {
        this.sQLiteDatabaseObj = sQLiteDatabaseObj;
    }

    private SQLiteDatabaseObj sQLiteDatabaseObj;

    public DBUtils(Context context, String dbName) {
        if (context instanceof Activity) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
        this.dbName = dbName;
        init();
    }

    public DBUtils(Context context) {
        this.context = context;
        init();
    }


    private void init() {
        sQLiteDatabaseObj = new SQLiteDatabaseObj();
        mSQLiteDatabase = sQLiteDatabaseObj.getSQLiteDatabase();
    }

    /**
     * 会自动加上别名
     *
     * @param table
     * @return
     */
    public boolean tableExist(String table) {
        return tableExistFromDb(sQLiteDatabaseObj, aliasName + table);
    }

    public static boolean tableExistFromDb(SQLiteDatabaseObj databaseObj, String table) {
        return databaseObj.tableExist(table);
    }

    /**
     * 根据类的字节码自动创建表，如果存在不会创建, 会自动加上别名 在本方法。 如果是int,或者integer类型的将创建的是integer类型，如果注解是id那么自动创建id字段，此类必须有注解，否则将无主见。其他类型将默认按字符串来创建表
     * http://blog.csdn.net/naturebe/article/details/6981843
     *
     * @param
     * @return
     */


    public boolean modifyTableName(String groupTableOld, String groupTableNew) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ALTER   TABLE  %s RENAME TO %s", groupTableOld, groupTableNew));

        try {
            sQLiteDatabaseObj.execSQL(sb.toString());
            return true;

        } catch (Exception e) {
            return false;
        }
    }


    public boolean createTable(Class<?> klass) {
        String className = geInnerTableName(klass);
        if (sQLiteDatabaseObj.tableExist(className)) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("create table " + className + "(");
        Field[] fields = getDbFieldBy(mGetDeclared, klass);
//        Field[] fields = getDbFieldBy(klass);
        Log.i(TAG, "field总数" + fields.length);
        if (fields == null || fields.length == 0) {
            throw new RuntimeException("抱歉,无法创建table," + className + "没有可创建的字段");
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (ReflectUtils.isIgnoreFiled(field)) {
                continue;
            }
            if (field.isSynthetic()) {
                continue;
            }

            String temp = DBUtils.getDbFieldDeclare(field, true);
            if (temp != null) {
                sb.append(temp);
            } else {
                continue;
            }
           /* if (ReflectUtils.isConstant(field)) {
                continue;
            } else if (ReflectUtils.isIDField(field)) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " integer primary key autoincrement");
            } else if (ReflectUtils.getColumnType(field) != null) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " " + ReflectUtils.getColumnType(field) + ReflectUtils.getUniqueCrateFieldFlag(field));//浮点型
            } else if (ReflectUtils.isIntType(field)) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " integer" + ReflectUtils.getUniqueCrateFieldFlag(field));
            } else if (ReflectUtils.isDoubleType(field)) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " REAL" + ReflectUtils.getUniqueCrateFieldFlag(field));//浮点型
            } else if (ReflectUtils.isLongType(field)) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " number" + ReflectUtils.getUniqueCrateFieldFlag(field));
            } else if (ReflectUtils.isDoubleType(field)) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " REAL" + ReflectUtils.getUniqueCrateFieldFlag(field));
            } else if (ReflectUtils.isBooleanType(field)) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " integer" + ReflectUtils.getUniqueCrateFieldFlag(field));
            } else if (ReflectUtils.isFloatType(field) || ReflectUtils.isDoubleType(field)) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " REAL" + ReflectUtils.getUniqueCrateFieldFlag(field));//浮点型
            } else if (ReflectUtils.isStringType(field)) {
                sb.append(ReflectUtils.getColumnNameByField(field) + " varchar" + ReflectUtils.getUniqueCrateFieldFlag(field));//字符型
            } else {
                continue;
            }*/


            sb.append(",");
        }
        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);//删除,
        }
        sb.append(")");
        Log.w(TAG, "create table,sql:" + sb.toString());
        // sb.append("("++")");
        // String sql="create table"+tableName;
        // mSQLiteDatabase.execSQL(sql);
        sQLiteDatabaseObj.execSQL(sb.toString());
        return true;
    }

    public static String getDbFieldDeclare(Field field, boolean needField) {
        String result = null;
        if (ReflectUtils.isConstant(field)) {
            return null;
        } else if (ReflectUtils.isIDField(field)) {
            result = "integer primary key autoincrement";
        } else if (ReflectUtils.getColumnType(field) != null) {
            result = ReflectUtils.getColumnType(field) + ReflectUtils.getUniqueCrateFieldFlag(field);//浮点型
        } else if (ReflectUtils.isIntType(field)) {
            result = "integer" + ReflectUtils.getUniqueCrateFieldFlag(field);
        } else if (ReflectUtils.isDoubleType(field)) {
            result = "REAL" + ReflectUtils.getUniqueCrateFieldFlag(field);//浮点型
        } else if (ReflectUtils.isLongType(field)) {
            result = "number" + ReflectUtils.getUniqueCrateFieldFlag(field);
        } else if (ReflectUtils.isDoubleType(field)) {
            result = "REAL" + ReflectUtils.getUniqueCrateFieldFlag(field);
        } else if (ReflectUtils.isBooleanType(field)) {
            result = "integer" + ReflectUtils.getUniqueCrateFieldFlag(field);
        } else if (ReflectUtils.isFloatType(field) || ReflectUtils.isDoubleType(field)) {
            result = "REAL" + ReflectUtils.getUniqueCrateFieldFlag(field);//浮点型
        } else if (ReflectUtils.isStringType(field)) {
            result = "varchar" + ReflectUtils.getUniqueCrateFieldFlag(field);//字符型
        } else {
        }
        if (result == null) {
            return null;
        } else {
            if (needField) {
                return ReflectUtils.getColumnNameByField(field) + " " + result;
            } else {
                return result;
            }
        }
    }


    /**
     * 删除表
     *
     * @param klass
     */
    public void deleteTable(Class<?> klass) {
        deleteTable(geInnerTableName(klass));
    }

    public void deleteTable(String tableName) {

        DBUtils.ToolHelper.deleteTable(sQLiteDatabaseObj, tableName);
    }

    /**
     * 给我对象我会自动根据里面的id字段来修改 数据库中存在的
     *
     * @param object 我犯了一个低级错误吧字节码传递进去了。
     * @return
     */
    public int update(Object object) {

        Class<? extends Object> klass = object.getClass();
        Log.i(TAG, ":" + klass.getName());
        ContentValues values = new ContentValues();
        boolean succ = fillContentValues(object, klass, values);
        if (!succ) {
            return -2;
        }

        int valueId = ReflectUtils.getIntValue(object, getIdFieldFromJavaBean(klass));//获取id字段的 值
        return mSQLiteDatabase.update(aliasName + ReflectUtils.getTableNameByClass(klass), values, ReflectUtils.getColumnNameByField(getIdFieldFromJavaBean(klass)) + "=?", new String[]{"" + valueId});

    }


    public int updateAllByField(Object object, String column, String value) {

        Class<? extends Object> klass = object.getClass();
        Log.i(TAG, ":" + klass.getName());
        ContentValues values = new ContentValues();
        boolean succ = fillContentValues(object, klass, values);
        if (!succ) {
            return -2;
        }

        return mSQLiteDatabase.update(aliasName + ReflectUtils.getTableNameByClass(klass), values, column + "=?", new String[]{"" + value});

    }

    /**
     * 更新指定字段 失败返回值小于0
     *
     * @param object   更新的对象
     * @param fieldstr 需要更新的字段
     * @return
     */
    public int update(Object object, String fieldstr) {
        Class<? extends Object> klass = object.getClass();
        ContentValues values = new ContentValues();
        boolean b = fillContentValue(object, klass, values, fieldstr);
        if (b == false) {
            return -2;
        }
        int valueId = getIntValue(object, klass);//获取id字段的 值
        return mSQLiteDatabase.update(aliasName + ReflectUtils.getTableNameByClass(klass), values, ReflectUtils.getColumnNameByField(getIdFieldFromJavaBean(klass)) + "=?", new String[]{"" + valueId});
    }

    public int getIntValue(Object object, Class<?> klass) {
        return ReflectUtils.getIntValue(object, mGetDeclared ? ReflectUtils.getDeclaredIDField(klass) : getIdFieldFromJavaBean(klass));
    }

    public long insert(Object object) {
        Class<? extends Object> klass = object.getClass();
        ContentValues values = new ContentValues();
        boolean b = fillContentValues(object, klass, values);
        if (!b) {
            return -2;
        }
        return mSQLiteDatabase.insert(aliasName + ReflectUtils.getTableNameByClass(klass), null, values);
    }

    /**
     * 删除通过id
     *
     * @param klass
     * @return
     */
    public <T> int deleteById(Class<T> klass, int id) {
        return delete(klass, ReflectUtils.getColumnNameByField(getIdFieldFromJavaBean(klass)) + "=?", new String[]{"" + id});
    }

    /**
     * @param klass
     * @param fieldstr 字段
     * @param value    要查找的值
     * @param <T>
     * @return
     */
    public <T> int deleteByColumn(Class<T> klass, String fieldstr, String value) {
        return delete(klass, fieldstr + "=?", new String[]{"" + value});
    }

    public <T> int deleteByColumnLike(Class<T> klass, String fieldstr, String value) {
        return delete(klass, fieldstr + " like '%" + value + "%'", null);
    }

    public <T> int deleteByColumn(Class<T> klass, String[] fieldArr, String[] valueArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < fieldArr.length; i++) {
            String s = fieldArr[i];
            stringBuffer.append(s + "=?" + (i == fieldArr.length - 1 ? "" : " and "));
        }
        return delete(klass, stringBuffer.toString(), valueArr);
//        return delete(klass, fieldstr + "=? and " + filedStr1 + "=?", new String[]{"" + value, value1});
    }

    public <T> int deleteByColumnOr(Class<T> klass, String[] fieldArr, String[] valueArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < fieldArr.length; i++) {
            String s = fieldArr[i];
            stringBuffer.append(s + "=?" + (i == fieldArr.length - 1 ? "" : " or "));
        }
        return delete(klass, stringBuffer.toString(), valueArr);
//        return delete(klass, fieldstr + "=? and " + filedStr1 + "=?", new String[]{"" + value, value1});
    }

    /**
     * 删除所有
     *
     * @param klass
     * @return
     */
    public <T> int deleteAll(Class<T> klass) {
        return delete(klass, null, null);
    }

    public <T> int delete(Class<T> klass, String whereClause, String[] whereArgs) {

        return mSQLiteDatabase.delete(aliasName + ReflectUtils.getTableNameByClass(klass), whereClause, whereArgs);
    }

    /**
     * 查询id=某某
     * android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed. # Open Cursors=2 (# cursors opened by this proc=2)
     *
     * @param id
     * @return
     */
    public <T> T queryByID(Class<T> klass, int id) {
        String selection = ReflectUtils.getColumnNameByField(getIdFieldFromJavaBean(klass)) + "=?";
        String table = aliasName + ReflectUtils.getTableNameByClass(klass);
        Cursor cursor = mSQLiteDatabase.query(table, null, selection, new String[]{"" + id}, null, null, null, null);

        while (cursor.moveToNext()) {
            T object = getObjectByCurosr(klass, cursor);
            cursor.close();
            return object;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    /**
     * 根据游标反射对象数组
     *
     * @param klass
     * @param getDeclared
     * @param cursor
     * @param <T>
     * @return
     */
    public static <T> List<T> queryBeanListByCurosr(Class<T> klass, boolean getDeclared, Cursor cursor) {
        ArrayList<T> arrayList = null;
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
                T object = getObjectByCurosrStatic(getDeclared, klass, cursor);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;


    }

    public boolean queryIDExist(Class klass, int id) {
        return queryColumnExist(klass, ReflectUtils.getColumnNameByField(getIdFieldFromJavaBean(klass)), id + "");
    }

    public boolean queryColumnExist(Class klass, String column, String value) {
        String selection = column + "=?";
        boolean flag = false;
        String table = aliasName + ReflectUtils.getTableNameByClass(klass);
        Cursor cursor = mSQLiteDatabase.query(table, null, selection, new String[]{"" + value}, null, null, null, null);
        while (cursor.moveToNext()) {
            flag = true;
        }

        cursor.close();
//		return query(t, null, selection, new String []{""+id});
        return flag;
    }

    public <T> T queryFinal(Class<T> klass) {
        String table = aliasName + ReflectUtils.getTableNameByClass(klass);
        //select * from (select t.*,from table t order by pxColumn desc) where rownum =1
        //mGetDeclared ? ReflectUtils.getDeclaredIDField(klass).getName() : ReflectUtils.getIDField(klass).getName()
        Cursor cursor = mSQLiteDatabase.query(table, null, null, null, null, null, (getIdFieldFromJavaBean(klass).getName()) + " desc", "0,1");
        while (cursor.moveToNext()) {
            T object = getObjectByCurosr(klass, cursor);
            cursor.close();
            return object;
        }
        cursor.close();
//		return query(t, null, selection, new String []{""+id});
        return null;
    }


    /**
     * @param klass
     * @param column 要查询的列
     * @param value  要查询列所等于的值
     * @param <T>
     * @return
     */
    public <T> T queryByColumn(Class<T> klass, String column, String value) {
        String selection = column + "=?";
        String table = aliasName + ReflectUtils.getTableNameByClass(klass);
        Cursor cursor = mSQLiteDatabase.query(table, null, selection, new String[]{"" + value}, null, null, null, null);
        while (cursor.moveToNext()) {
            T object = getObjectByCurosr(klass, cursor);
            cursor.close();
            return object;
        }
        cursor.close();
//		return query(t, null, selection, new String []{""+id});
        return null;
    }

    /**
     * 查询类名通过 指定的字段
     *
     * @param t
     * @param field
     * @param obj
     * @return
     */
    public <T> List<T> queryAllByField(Class<T> t, Field field, Object obj) {
        String columnName = ReflectUtils.getColumnNameByField(field);
        return queryAllByField(t, columnName, obj);
    }

    public <T> List<T> queryAllByField(Class<T> klass, String filedName, Object value) {
        String selection = filedName + "=?";
        return query(klass, null, selection, new String[]{"" + value.toString()});
    }

    public <T> List<T> queryAllByField(Class<T> klass, String filedName, Object value, String fieldName1, Object value1) {
        String selection = filedName + "=? and " + fieldName1 + "=?";
        return query(klass, null, selection, new String[]{"" + value.toString(), "" + value1.toString()});
    }

    public <T> List<T> queryAllByFieldLike(Class<T> klass, String filedName, Object value) {
        String selection = filedName + " like '%" + value + "%'";
        return query(klass, null, selection, null);
    }

    /**
     * 查询所有
     *
     * @param klass
     * @return
     */
    public <T> List<T> queryAll(Class<T> klass) {
        return query(klass, null, null, null);
    }


    /**
     * @param klass
     * @param desc  是否从大到小 降序查询
     * @param <T>
     * @return
     */
    public <T> List<T> queryAllIsDesc(Class<T> klass, boolean desc) {
        return queryAllIsDesc(klass, desc, null);
    }

    public <T> List<T> queryAllIsDesc(Class<T> klass, boolean desc, String fieldStr) {
        /**
         * SELECT * FROM SearchBean ORDER BY id desc
         */
        return query(klass, null, null, null, null, null, (fieldStr == null ? ReflectUtils.getColumnNameByField(getIdFieldFromJavaBean(klass)) : fieldStr) + " " + (desc ? "desc" : "asc"), null);
    }

    /**
     * 查询id=某某 所有
     *
     * @param id
     * @return
     */
    public <T> List<T> queryAllByID(Class<T> klass, int id) {
        String selection = ReflectUtils.getColumnNameByField(getIdFieldFromJavaBean(klass)) + "=?";
        return query(klass, null, selection, new String[]{"" + id});
    }

    /**
     * @param klass
     * @param columns
     * @param selection
     * @param selectionArgs
     * @return
     */
    public <T> List<T> query(Class<T> klass, String[] columns, String selection, String[] selectionArgs) {
        return query(klass, columns, selection, selectionArgs, null, null, null, null);
    }

    /**
     * @param klass
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @param <T>
     * @return
     */

    public <T> List<T> query(Class<T> klass, String[] columns, String selection, String[] selectionArgs, String orderBy) {
        return query(klass, columns, selection, selectionArgs, null, null, orderBy, null);
    }

    /**
     * @param klass
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param <T>
     * @return
     */
    public <T> List<T> query(Class<T> klass, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        /**
         * 查询所有还是弄一个对象来吧 查询的填充到一个对象是
         *
         */
        String table = aliasName + ReflectUtils.getTableNameByClass(klass);
        ArrayList<T> arrayList = null;
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){
        Cursor cursor = mSQLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
                T object = getObjectByCurosr(klass, cursor);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;
    }

    public void clearAlias() {
        setAlias("");
    }


    public static class HashMapDBInfo {
        List<HashMap<String, Object>> list;
        HashMap<String, Object> maxInfo;


/**
 * 表示每一行的的key,键名键z值
 *
 * @return
 */

        public List<HashMap<String, Object>> getList() {
            return list;
        }

        public void setList(List<HashMap<String, Object>> list) {
            this.list = list;
        }



        public HashMap<String, Object> getMaxInfo() {
            return maxInfo;
        }

        public void setMaxInfo(HashMap<String, Object> maxInfo) {
            if (this.maxInfo == null) {
                this.maxInfo = maxInfo;

            } else if (this.maxInfo.size() < maxInfo.size()) {
                this.maxInfo = maxInfo;
            }
            //忽略
        }
    }



    public static HashMap<String, Object> queryCurosrToHashMap(Cursor cursor) {

        HashMap<String, Object> map = new HashMap<>();

        int columnCount = cursor.getColumnCount();

        for (int i = 0; i < columnCount; i++) {


            String key = cursor.getColumnName(i);

            int columnIndex = cursor.getColumnIndex(key);
            int type = cursor.getType(columnIndex);
            Object value = null;
            if (type == Cursor.FIELD_TYPE_INTEGER) {
                value = cursor.getInt(i);

            } else if (type == Cursor.FIELD_TYPE_FLOAT) {

                value = cursor.getFloat(i);

            } else if (type == Cursor.FIELD_TYPE_BLOB) {


                value = cursor.getBlob(i);
            } else if (type == Cursor.FIELD_TYPE_NULL) {


            } else if (type == Cursor.FIELD_TYPE_STRING) {

                value = cursor.getString(i);
            }

            map.put(key, value);

        }
        return map;
    }


    public HashMapDBInfo queryAllSaveCollectionsByClass(Class klass, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        HashMapDBInfo info = new HashMapDBInfo();
        List<HashMap<String, Object>> list = new ArrayList<>();
        String table = geInnerTableName(klass);
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){
        Cursor cursor = mSQLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        //List<HashMap<String, String>> find = null;

        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                HashMap<String, Object> object = queryCurosrToHashMap(cursor);
                info.setMaxInfo(object);
                list.add(object);
            }
        }
        cursor.close();
        info.setList(list);
        return info;


    }


    public HashMapDBInfo queryAllSaveCollections(String sql) {
        HashMapDBInfo info = new HashMapDBInfo();
        List<HashMap<String, Object>> list = new ArrayList<>();
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){
        Cursor cursor = mSQLiteDatabase.rawQuery(sql, null);

        //List<HashMap<String, String>> find = null;


        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                HashMap<String, Object> object = queryCurosrToHashMap(cursor);
                info.setMaxInfo(object);
                list.add(object);
            }
        }
        cursor.close();
        info.setList(list);
        return info;


    }






    /**
     * SELECT DISTINCT name FROM COMPANY;
     *
     * @param klassTable    反悔的字节码对象
     * @param klass         字节码对象
     * @param queryFiledStr 查询的字段
     * @param selectionArgs 填写的参数
     * @param <T>
     * @return
     */
    public <T> List<T> queryDistinct(Class klassTable, Class<T> klass, String queryFiledStr, String[] selectionArgs) {
        /**
         * 查询所有还是弄一个对象来吧 查询的填充到一个对象是
         *
         */
        String table = aliasName + ReflectUtils.getTableNameByClass(klassTable);
        ArrayList<T> arrayList = null;
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){//SELECT DISTINCT name FROM COMPANY;
        Cursor cursor = mSQLiteDatabase.rawQuery("select distinct " + queryFiledStr + " from  " + table, selectionArgs);
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
//                mDataBind object = getObjectByOnlyCurosr(klass, cursor, stringBuffer.toString());
                T object = getObjectByOnlyCurosr(klass, cursor, queryFiledStr);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;
    }

    /**
     * SELECT DISTINCT name FROM COMPANY;
     *
     * @param klassTable    反悔的字节码对象
     * @param klass         字节码对象
     * @param queryFiledStr 查询的字段
     * @param selectionArgs 填写的参数
     * @param <T>
     * @return
     */
    public <T> List<T> queryDistinct(Class klassTable, Class<T> klass, String[] queryFiledStr, String[] selectionArgs) {
        /**
         * 查询所有还是弄一个对象来吧 查询的填充到一个对象是
         *
         */
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < queryFiledStr.length; i++) {
            stringBuffer.append(queryFiledStr[i]);
            if (i != queryFiledStr.length - 1) {
                stringBuffer.append(",");
            }
        }
        String table = aliasName + ReflectUtils.getTableNameByClass(klassTable);
        ArrayList<T> arrayList = null;
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){//SELECT DISTINCT name FROM COMPANY;
        Cursor cursor = mSQLiteDatabase.rawQuery("select DISTINCT " + stringBuffer.toString() + " from  " + table, selectionArgs);
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
//                mDataBind object = getObjectByOnlyCurosr(klass, cursor, stringBuffer.toString());
                T object = getObjectByCurosr(klass, cursor);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;
    }

    /**
     * 通过游标给制定字段赋值
     *
     * @param klass
     * @param cursor
     * @param str
     * @param <T>
     * @return
     */
    private <T> T getObjectByOnlyCurosr(Class<T> klass, Cursor cursor, String str) {
        T object = ReflectUtils.getInstance(klass);//创建一个对象
        Field field = getJavaBeanField(klass, str);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
     /*       if(ReflectUtils.isIgnoreFiled(field)){
                return null;
            }
            if (ReflectUtils.isConstant(field)) {
               return;
            }*/
        String columnName = ReflectUtils.getColumnNameByField(field);
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex != -1) {
            if (ReflectUtils.isIntType(field))//INT
            {
                //直接通过名字找值 以前是通过getInt(下标)找方法为
                int valueInt = cursor.getInt(columnIndex);//管它是-1还是啥都赋值
                ReflectUtils.setValue(object, field, valueInt);
            } else if (ReflectUtils.isStringType(field)) {//String
                String valueString = cursor.getString(columnIndex);
                ReflectUtils.setValue(object, field, valueString);
            } else if (ReflectUtils.isBooleanType(field)) {//Boolean
                boolean valueBoolean = cursor.getInt(columnIndex) == 1 ? true : false;
                ReflectUtils.setValue(object, field, valueBoolean);
            } else if (ReflectUtils.isBytesType(field)) {//byte[]
                byte[] valueBytes = cursor.getBlob(columnIndex);
                ReflectUtils.setValue(object, field, valueBytes);
            } else if (ReflectUtils.isShortType(field)) {//Short
                short valueShort = cursor.getShort(columnIndex);
                ReflectUtils.setValue(object, field, valueShort);
            } else if (ReflectUtils.isLongType(field)) {//Long
                long valueLong = cursor.getLong(columnIndex);
                ReflectUtils.setValue(object, field, valueLong);
            } else if (ReflectUtils.isFloatType(field)) {//Float
                float valueFloat = cursor.getFloat(columnIndex);
                ReflectUtils.setValue(object, field, valueFloat);
            } else if (ReflectUtils.isDoubleType(field)) {//Double
                double valueDouble = cursor.getDouble(columnIndex);
                ReflectUtils.setValue(object, field, valueDouble);
            } else {
                //这里无法解决boolean类型和一些对象类型，所以还是不推荐这么做
                String valueStr = cursor.getString(columnIndex);//管它是-1还是啥都赋值
                ReflectUtils.setValue(object, field, valueStr);//这样赋值是字符串
            }
        } else {
            Log.w(TAG, "抱歉 " + field + "对于的列" + columnName + "找不到index");
        }
        return object;

    }


    /**
     * 逆向过程通过游标 给字节码的所有 赋值  从数据库查询出来赋值给对象 这么做会导致一个问题那就是没法赋值给枚举了.
     *
     * @param klass
     * @param cursor
     * @param <T>
     * @return
     */
    public <T> T getObjectByCurosr(Class<T> klass, Cursor cursor) {
        return getObjectByCurosrStatic(mGetDeclared, klass, cursor);
    }

    public static <T> T getObjectByCurosrStatic(boolean getDeclared, Class<T> klass, Cursor cursor) {
        T object = ReflectUtils.getInstance(klass);//创建一个对象
        Field[] fields = getDbFieldBy(getDeclared, klass);
        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (ReflectUtils.isIgnoreFiled(field)) {
                continue;
            }
            if (ReflectUtils.isConstant(field)) {
                continue;
            }
            String columnName = ReflectUtils.getColumnNameByField(field);
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {

                if (ReflectUtils.isIntType(field))//INT
                {
                    //直接通过名字找值 以前是通过getInt(下标)找方法为
                    int valueInt = cursor.getInt(columnIndex);//管它是-1还是啥都赋值
                    ReflectUtils.setValue(object, field, valueInt);
                } else if (ReflectUtils.isStringType(field)) {//String
                    String valueString = cursor.getString(columnIndex);
                    ReflectUtils.setValue(object, field, valueString);
                } else if (ReflectUtils.isBooleanType(field)) {//Boolean
                    boolean valueBoolean = cursor.getInt(columnIndex) == 1 ? true : false;
                    ReflectUtils.setValue(object, field, valueBoolean);
                } else if (ReflectUtils.isBytesType(field)) {//byte[]
                    byte[] valueBytes = cursor.getBlob(columnIndex);
                    ReflectUtils.setValue(object, field, valueBytes);
                } else if (ReflectUtils.isShortType(field)) {//Short
                    short valueShort = cursor.getShort(columnIndex);
                    ReflectUtils.setValue(object, field, valueShort);
                } else if (ReflectUtils.isLongType(field)) {//Long
                    long valueLong = cursor.getLong(columnIndex);
                    ReflectUtils.setValue(object, field, valueLong);
                } else if (ReflectUtils.isFloatType(field)) {//Float
                    float valueFloat = cursor.getFloat(columnIndex);
                    ReflectUtils.setValue(object, field, valueFloat);
                } else if (ReflectUtils.isDoubleType(field)) {//Double
                    double valueDouble = cursor.getDouble(columnIndex);
                    ReflectUtils.setValue(object, field, valueDouble);
                } else {
                    //这里无法解决boolean类型和一些对象类型，所以还是不推荐这么做
                    String valueStr = cursor.getString(columnIndex);//管它是-1还是啥都赋值
                    ReflectUtils.setValue(object, field, valueStr);//这样赋值是字符串
                }
            } else {
                Log.w(TAG, "抱歉 " + field + "对于的列" + columnName + "找不到index");
            }

        }
        return object;
    }



    public static <T> T insertNewCloumnFromClasss(DBUtils dbUtils, Class<T> klass) {
        return insertNewCloumnFromClasss(dbUtils, klass, dbUtils.getDb());
    }

    public static <T> T insertNewCloumnFromClasss(DBUtils dbUtils, Class<T> klass, SQLiteDatabase database) {
        String tableName = dbUtils.geInnerTableName(klass);
        T object = ReflectUtils.getInstance(klass);//创建一个对象

        Field[] fields = ReflectUtils.getJavaBeanAllFields(klass);
        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (field.isSynthetic()) {
                continue;
            }
            if (ReflectUtils.isIgnoreFiled(field)) {
                continue;
            }
            if (ReflectUtils.isConstant(field)) {
                continue;
            }

            String columnName = ReflectUtils.getColumnNameByField(field);

            if (ToolHelper.columnExist(database, tableName, columnName)) {

                Log.w(TAG, "ignore column ,because is exist " + columnName);
                continue;
            }


            String dbFieldDeclare = getDbFieldDeclare(field, false);
            if (dbFieldDeclare != null) {
                ToolHelper.addColumn(database, tableName, columnName, dbFieldDeclare);
            } else {

                ToolHelper.addColumn(database, tableName, columnName);
            }
            Log.w(TAG, "insert column type " + dbFieldDeclare);
        }
        return object;
    }

    private boolean fillContentValues(Object object, Class<? extends Object> klass, ContentValues values) {
        boolean flag = false;
        Field[] fields = getDbFieldBy(mGetDeclared, klass);

        for (Field field : fields) {
            if (!mGetDeclared) {
                field.setAccessible(true);
            }
            if (ReflectUtils.isIgnoreFiled(field)) {
                continue;
            }
            if (ReflectUtils.isConstant(field)) {
                continue;
            }
            if (field.isSynthetic()) {
                continue;
            }

        /*    if (ReflectUtils.isIDField(field)) {
                continue;
            }*/

            boolean currentflag = fillContentValue(object, klass, values, field);
            Log.i(TAG, "fillContentValue->" + field.getName() + ",RESULT:" + currentflag);
            if (currentflag && flag == false) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * @param object
     * @param klass
     * @param values
     * @param fieldStr 字符串字段 将根据字节码反射出字段对象
     */
    private boolean fillContentValue(Object object, Class<? extends Object> klass, ContentValues values, String fieldStr) {

        Field field = ReflectUtils.getJavaBeanFieldFromFieldStr(klass, fieldStr);
//        Field field = mGetDeclared ? ReflectUtils.getDeclaredField(klass, fieldStr) : ReflectUtils.getField(klass, fieldStr);
        if (field == null) {
            Log.e(TAG, "cannot fill value field:" + fieldStr);
            return false;
        }
        return fillContentValue(object, klass, values, field);

    }


    /**
     * 正向过程 把对象中的值赋值写入数据库
     *
     * @param object 对象实例
     * @param klass  字节码
     * @param values 已经初始化的contentValues()
     * @param field  字段对象
     */
    private boolean fillContentValue(Object object, Class<? extends Object> klass, ContentValues values, Field field) {
        boolean flag = false;
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        if (field.isSynthetic()) {
            Log.w(TAG, "isSynthetic 忽略");
            return true;
        }
        if (field == null || TextUtils.isEmpty(field.getName())) {
            Log.w(TAG, object.getClass().getSimpleName() + ".class中包含字段" + field);
//			new RuntimeException(object.getClass().getSimpleName()+".class中包含字段"+field);
            return false;
        }

        if (ReflectUtils.isConstant(field)) {
            Log.w(TAG, "常量忽略:" + field.getName());
            return false;
        }

        try {
            if (ReflectUtils.isIDField(field)) {
                int valueInt = ReflectUtils.getIntValue(object, field);
                Log.w(TAG, "id子炖:" + field.getName() + ",value:" + valueInt);
                if (valueInt > 0)// 大于0说明指定了值
                {
                    values.put(ReflectUtils.getColumnNameByField(field), valueInt);
                } else {
                    Log.w(TAG, "id没有设置,因此由系统自动产生");
                }
            } else if (ReflectUtils.isShortType(field)) {//short
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getShortValue(object, field));
            } else if (ReflectUtils.isBooleanType(field)) {//boolean
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getBooleanValue(object, field));
            } else if (ReflectUtils.isIntType(field)) {//int
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getIntValue(object, field));
            } else if (ReflectUtils.isLongType(field)) {//int
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getLongValue(object, field));
            } else if (ReflectUtils.isStringType(field)) {//String
                String stringValue = ReflectUtils.getStringValue(object, field);

                values.put(ReflectUtils.getColumnNameByField(field), stringValue);
            } else if (ReflectUtils.isBytesType(field)) {//Bolb  字节数组 等待验证//TODO 等待验证
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getBytesValue(object));
            } else if (ReflectUtils.isFloatType(field)) {//Float
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getFloatValue(object, field));
            } else if (ReflectUtils.isDoubleType(field)) {//Double
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getDoubleValue(object, field));
            } else {
                Log.w(TAG, "无法识别字段类型 " + field + ",type:" + field.getType());
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "E:" + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    public void execSQL(String sql) {
        mSQLiteDatabase.execSQL(sql);
    }

    public <T> List<T> rawQuery(Class<T> classs, String sql) {


        String table = aliasName + ReflectUtils.getTableNameByClass(classs);
        Cursor cursor = mSQLiteDatabase.rawQuery(sql, null);
        ArrayList<T> arrayList = null;
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
                T object = getObjectByCurosrStatic(mGetDeclared, classs, cursor);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;
    }


    /**
     * 包含别名
     *
     * @param classs
     * @return
     */
    public String geInnerTableName(Class classs) {
        String table = DBUtils.getTableNameStatic(aliasName, classs);
        return table;
    }

    public static String getTableNameStatic(String aliasName, Class classs) {
        String table = aliasName + ReflectUtils.getTableNameByClass(classs);
        return table;
    }


    public class SQLiteDatabaseObj {


        private SQLiteDatabase sqLiteDatabase;

        public SQLiteDatabase getSQLiteDatabase() {
            if (sqLiteDatabase == null) {
                synchronized (SQLiteDatabaseObj.class) {
                    if (sqLiteDatabase == null) {
                        sqLiteDatabase = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
                    }
                }

            } else {
                if (!sqLiteDatabase.isOpen()) {

                    sqLiteDatabase = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
                }
            }

            return sqLiteDatabase;
        }

        /**
         * 需要别名
         *
         * @param tableName
         * @return
         */
        public boolean tableExist(String tableName) {
            /**
             * upper表示转大写，所以所有转大写要么全都不转大写
             */
            // name,type字段 sql表示为建表语句
            //select count(*)  from sqlite_master where type='table' and name = 'yourtablename';
            String sql = "select count(*) from sqlite_master where type = 'table' and upper(name) =upper( ? )";
            Cursor cursor = mSQLiteDatabase.rawQuery(sql, new String[]{tableName});

            boolean result = cursor.moveToNext() && cursor.getInt(0) > 0;
            Log.w(TAG, "表是否存在:" + tableName + ",result:" + result);
            return result;
        }

        /**
         * 不包含别名
         *
         * @param table
         * @param filed
         */

        public void deleteColumn(String table, String filed) {
            String sql = " alter table " + aliasName + table + " drop column " + filed;
            mSQLiteDatabase.execSQL(sql);

        }

        /**
         * 不包含别名 但是调用的 addColumn(table,colun,type)会加上别名。
         *
         * @param table  ReflectUtils.getTableNameByClass(LocalMusicInfo.class)
         * @param column
         */
        public void addColumn(String table, String column) {
            addColumn(table, column, "varchar");

        }

        /**
         * 不包含别名
         *
         * @param table
         * @param column
         * @param type
         */
        public void addColumn(String table, String column, String type) {
            String sql = "alter table " + aliasName + table + " add column " + column + " " + type;//ALTER TABLE Teachers ADD COLUMN Sex text;

            mSQLiteDatabase.execSQL(sql);
        }

        /**
         * 不包含别名
         *
         * @param table
         * @param newTable
         */
        public void reNameTable(String table, String newTable) {
            mSQLiteDatabase.execSQL("alter table " + aliasName + table + " rename to " + newTable);
        }


        /**
         * 我会自动加上别名，因此传递的表明不能加上别名
         *
         * @param tableName
         * @param columnName
         * @return
         */
        public boolean columnExist1(String tableName
                , String columnName) {
            boolean result = false;
            Cursor cursor = null;
            try {
                //查询一行
                cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " + aliasName + tableName + " LIMIT 0"
                        , null);
                result = cursor != null && cursor.getColumnIndex(columnName) != -1;
            } catch (Exception e) {
                Log.e(TAG, "checkColumnExists1..." + e.getMessage());
            } finally {
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            return result;
        }
        /**
         *    2、通过查询sqlite的系统表 sqlite_master 来查找相应表里是否存在该字段，稍微换下语句也可以查找表是否存在
         */

        /**
         * 方法2：检查表中某列是否存在 这种方法查询确实是真的，但是安卓为毛对于aler 增加进去的查询不到
         * 安卓的问题还是数据库问题我就不知道了。列加进去了
         * <p>
         * 情随事迁 2016/10/27 11:10:18
         * 但是执行的时候找不到列 ，还有一种方法也查询不到列 原始的方法查询到了也么用
         *
         * @param tableName  表名
         * @param columnName 列名
         * @return
         */
        public boolean columnExist2(String tableName
                , String columnName) {
            boolean result = false;
            Cursor cursor = null;

            try {
                cursor = mSQLiteDatabase.rawQuery("select * from sqlite_master where name = ? and sql like ?"
                        , new String[]{aliasName + tableName, "%" + columnName + "%"});
                int a = cursor.getCount();
                result = null != cursor && cursor.getCount() > 0;
//                result = null != cursor && cursor.moveToFirst() ;
            } catch (Exception e) {
                Log.e(TAG, "columnExists2..." + e.getMessage());
            } finally {
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            return result;
        }

        /**
         * 表是否存在 不存在则创建表，但是必须保证此表能反射 否则将创建失败
         *
         * @param classTable 字节码对象
         * @return
         */
        public boolean tableExistOrCreate(Class classTable) {
            if (!tableExist(aliasName + ReflectUtils.getTableNameByClass(classTable))) {
                createTable(classTable);
                return false;
            }
            return true;
        }


        public void execSQL(String sql) {
            mSQLiteDatabase.execSQL(sql);
        }

        public void close() {
            mSQLiteDatabase.close();
            mSQLiteDatabase = null;

        }
        // public boolean
    }

    public void close() {
        if (sQLiteDatabaseObj != null) {
            sQLiteDatabaseObj.close();
        }
        sQLiteDatabaseObj = null;
        context = null;
    }

    public boolean isClose() {
        return sQLiteDatabaseObj == null;
    }

    @Deprecated
    public void setGetDeclared(boolean mGetDeclared) {
//        this.mGetDeclared = mGetDeclared;
    }


    /**
     * 只有符合javaBean 包含set get的才会被返回。 2017年12月3日 17:45:37
     *
     * @param getDeclared
     * @param klass
     * @return
     */
    public static Field[] getDbFieldBy(boolean getDeclared, Class klass) {
        return ReflectUtils.getJavaBeanAllFields(klass);
//      return   mGetDeclared ? ReflectUtils.getDeclaredFields(klass) : ReflectUtils.getFields(klass);
    }


    private <T> Field getJavaBeanField(Class<T> klass, String str) {
        return ReflectUtils.getMethodFromAllField(klass, str);

//        return mGetDeclared ? declaredField : ReflectUtils.getField(klass, str);
    }

    public Field getIdFieldFromJavaBean(Class<?> klass) {
        return ReflectUtils.getJavaBeanIDFieldFromFields(klass);

    }

    /*

     true,当前公共、保护、默认（包）访问和私有方法/ 成员，但不包括父类的方法 getFileds父类子类所有public        mGetDeclared 访问当前类所有的 包含私有的，但是只能访问当前类的 由于设计师根据。字段来设置值所以 ，整个思想都是错误的。
     */
    private boolean mGetDeclared = false;


    public static class ToolHelper {

        /**
         * 需要别名
         *
         * @param tableName
         * @return
         */
        public static boolean tableExist(SQLiteDatabase liteDatabase, String tableName) {
            /**
             * upper表示转大写，所以所有转大写要么全都不转大写
             */
            // name,type字段 sql表示为建表语句
            //select count(*)  from sqlite_master where type='table' and name = 'yourtablename';
            String sql = "select count(*) from sqlite_master where type = 'table' and upper(name) =upper( ? )";
            Cursor cursor = liteDatabase.rawQuery(sql, new String[]{tableName});
            boolean result = cursor.moveToNext() && cursor.getInt(0) > 0;
            Log.w(TAG, "tabble exist:" + tableName + ",result:" + result);
            return result;
        }

        /**
         * 不包含别名
         *
         * @param table
         * @param filed
         */

        public static void deleteColumn(SQLiteDatabase liteDatabase, String table, String filed) {
            String sql = " alter table " + table + " drop column " + filed;
            liteDatabase.execSQL(sql);

        }

        public static boolean columnExist(SQLiteDatabase liteDatabase, String tableName
                , String columnName) {
            boolean result = false;
            Cursor cursor = null;
            try {
                //查询一行
                cursor = liteDatabase.rawQuery("SELECT * FROM " + tableName + " LIMIT 0"
                        , null);
                result = cursor != null && cursor.getColumnIndex(columnName) != -1;
            } catch (Exception e) {
                Log.e(TAG, "checkColumnExists1..." + e.getMessage());
            } finally {
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            return result;
        }

        /**
         * 不包含别名 但是调用的 addColumn(table,colun,type)会加上别名。
         *
         * @param table  ReflectUtils.getTableNameByClass(LocalMusicInfo.class)
         * @param column
         */
        public static void addColumn(SQLiteDatabase liteDatabase, String table, String column) {
            addColumn(liteDatabase, table, column, "varchar");

        }

        /**
         * 不包含别名
         *
         * @param table
         * @param column
         * @param type
         */
        public static void addColumn(SQLiteDatabase liteDatabase, String table, String column, String type) {
            String sql = "alter table " + table + " add column " + column + " " + type;//ALTER TABLE Teachers ADD COLUMN Sex text;

            liteDatabase.execSQL(sql);
        }

        /**
         * 不包含别名
         *
         * @param table
         * @param newTable
         */
        public static void reNameTable(SQLiteDatabase liteDatabase, String table, String newTable) {
            liteDatabase.execSQL("alter table " + table + " rename to " + newTable);
        }


        public static void deleteTable(SQLiteDatabaseObj sqLiteDatabaseObj, String tableName) {
            sqLiteDatabaseObj.execSQL("DROP TABLE " + tableName);
        }
    }


    public <T> List<T> queryByColumnArr(Class klassTable, Class<T> klass, String[] queryFiledStr, String[] selectionArgs) {
        /**
         * 查询所有还是弄一个对象来吧 查询的填充到一个对象是
         *
         */

        StringBuffer stringBufferWhere = new StringBuffer();
        for (int i = 0; i < queryFiledStr.length; i++) {
            stringBufferWhere.append(queryFiledStr[i]);
            stringBufferWhere.append("=?");

            if (i != queryFiledStr.length - 1) {

                stringBufferWhere.append(" and ");
            }
        }


        String table = aliasName + ReflectUtils.getTableNameByClass(klassTable);
        ArrayList<T> arrayList = null;
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){//SELECT DISTINCT name FROM COMPANY;
        String sql = "select * from  " + table + " where " + stringBufferWhere.toString();
        Cursor cursor = mSQLiteDatabase.rawQuery(sql, selectionArgs);
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
//                mDataBind object = getObjectByOnlyCurosr(klass, cursor, stringBuffer.toString());
                T object = getObjectByCurosr(klass, cursor);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;
    }


}














/*
package cn.qssq666.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import java.lang.reflect.Type;
//import java.util.Iterator;

*/
/**
 *
 * @author luozheng
 * <p/>
 * 创建表，创建库，插入数据，更新数据 删除数据
 * <p>
 * 2016-10-27 10:24:23  增加 字段是否存在判断
 * <p>2017年2月16日 12:49:31 别名 判断bug修复
 * dbutils是单例的所以操作的时候切换选项卡容易销毁，因此不需要关闭只需要在activity做关闭就行了
 * <p>
 * 2017年3月30日 18:49:57
 * 增加总数查询 查询最后几条。
 * 2017年4月27日 21:39:39
 * 增加根据filedTypebean类型创建数据库 而并非以前的方式。
 *//*
























public class DBUtils {
    private static final String TAG = "DBUtils";
    private Context context;

    public void setAlias(String aliasName) {
        this.aliasName = aliasName;
    }

    String aliasName = "";
    private String dbName = "qssq666.db";

    public SQLiteDatabase getDb() {
        return mSQLiteDatabase;
    }

    public String getAliasName() {
        return aliasName;
    }

    public static String getTAG() {
        return TAG;
    }

    public void setDb(SQLiteDatabase mDb) {
        this.mSQLiteDatabase = mDb;
    }

    private SQLiteDatabase mSQLiteDatabase;

    public DBHepler getDbHelper() {
        return dbHepler;
    }

    public void setDbHepler(DBHepler dbHepler) {
        this.dbHepler = dbHepler;
    }

    private DBHepler dbHepler;

    public DBUtils(Context context, String dbName) {
        if (context instanceof Activity) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
        this.dbName = dbName;
        init();
    }

    public DBUtils(Context context) {
        this.context = context;
        init();
    }


    private void init() {
        dbHepler = new DBHepler();
        mSQLiteDatabase = dbHepler.getSQLiteDatabase();
    }

    */
/**
     * 会自动加上别名
     *
     * @param table
     * @return
     *//*

    public boolean tableExist(String table) {
        return dbHepler.tableExist(aliasName + table);
    }

    */
/**
     * 根据类的字节码自动创建表，如果存在不会创建, 会自动加上别名 在本方法。 如果是int,或者integer类型的将创建的是integer类型，如果注解是id那么自动创建id字段，此类必须有注解，否则将无主见。其他类型将默认按字符串来创建表
     * http://blog.csdn.net/naturebe/article/details/6981843
     *
     * @param klass
     * @return
     *//*

    public boolean createTable(Class<?> klass) {
        String className = geInnerTableName(klass);
        if (dbHepler.tableExist(className)) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("create table " + className);
        sb.append("(");
        Field[] fields = getDeclared ? ReflectUtils.getDeclaredFields(klass) : ReflectUtils.getFields(klass);
        Log.i(TAG, "field总数" + fields.length);
        if (fields == null || fields.length == 0) {
            throw new RuntimeException("抱歉,无法创建table," + className + "没有可创建的字段");
        }
        for (int i = 0; i < fields.length; i++) {
            if (ReflectUtils.isIgnoreFiled(fields[i])) {
                continue;
            }
            if (ReflectUtils.isConstant(fields[i])) {
                continue;
            } else if (ReflectUtils.isIDField(fields[i])) {
                sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " integer primary key autoincrement");
            } else if (ReflectUtils.isIntType(fields[i])) {
                sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " integer");
            } else if (ReflectUtils.isDoubleType(fields[i])) {
                sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " REAL");//浮点型
            } else if (ReflectUtils.isLongType(fields[i])) {
                sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " number");
            } else if (ReflectUtils.isDoubleType(fields[i])) {
                sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " REAL");
            } else if (ReflectUtils.isBooleanType(fields[i])) {
                sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " integer");
            } else if (ReflectUtils.isFloatType(fields[i]) || ReflectUtils.isDoubleType(fields[i])) {
                sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " REAL");//浮点型
            } else if (ReflectUtils.isStringType(fields[i])) {
                sb.append(ReflectUtils.getColumnNameByField(fields[i]) + " varchar");//字符型
            } else {
                continue;
            }
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);//删除,
        sb.append(")");
        Log.i(TAG, "create table,sql:" + sb.toString());
        // sb.append("("++")");
        // String sql="create table"+tableName;
        // mSQLiteDatabase.execSQL(sql);
        dbHepler.execSQL(sb.toString());
        return true;
    }

    @NonNull
    public String geInnerTableName(Class<?> klass) {
        return aliasName + ReflectUtils.getTableNameByClass(klass);
    }


    */
/**
     * 删除表
     *
     * @param klass
     *//*

    public void deleteTable(Class<?> klass) {
        deleteTable(ReflectUtils.getTableNameByClass(klass));
    }

    public void deleteTable(String table) {
        dbHepler.execSQL("DROP TABLE " + aliasName + table);
    }

    */
/**
     * 给我对象我会自动根据里面的id字段来修改 数据库中存在的
     *
     * @param object 我犯了一个低级错误吧字节码传递进去了。
     * @return
     *//*

    public int update(Object object) {

        Class<? extends Object> klass = object.getClass();
        Log.i(TAG, ":" + klass.getName());
        ContentValues values = new ContentValues();
        boolean succ = fillContentValues(object, klass, values);
        if (!succ) {
            return -2;
        }

        int valueId = ReflectUtils.getIntValue(object, getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass));//获取id字段的 值
        return mSQLiteDatabase.update(geInnerTableName(klass), values, ReflectUtils.getColumnNameByField(getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass)) + "=?", new String[]{"" + valueId});

    }


    public int updateByColumn(String tableName, String whereColumn, String whereColumnValue, String[] keys, String[] values) {


        ContentValues valuesContet = new ContentValues();
        for (int i = 0; i < keys.length; i++) {

            String key = keys[i];
            valuesContet.put(key, values[i]);

        }
        return mSQLiteDatabase.update(aliasName + tableName, valuesContet, whereColumn + "=?", new String[]{"" + whereColumnValue});

    }

    public int updateAllByField(Object object, String column, String value) {

        Class<? extends Object> klass = object.getClass();
        Log.i(TAG, ":" + klass.getName());
        ContentValues values = new ContentValues();
        boolean succ = fillContentValues(object, klass, values);
        if (!succ) {
            return -2;
        }

        return mSQLiteDatabase.update(geInnerTableName(klass), values, column + "=?", new String[]{"" + value});

    }

    */
/**
     * 更新指定字段 失败返回值小于0
     *
     * @param object   更新的对象
     * @param fieldstr 需要更新的字段
     * @return
     *//*

    public int update(Object object, String fieldstr) {
        Class<? extends Object> klass = object.getClass();
        ContentValues values = new ContentValues();
        boolean b = fillContentValue(object, klass, values, fieldstr);
        if (b == false) {
            return -2;
        }
        int valueId = ReflectUtils.getIntValue(object, getDeclared ? ReflectUtils.getDeclaredIDField(klass) : getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass));//获取id字段的 值
        return mSQLiteDatabase.update(geInnerTableName(klass), values, ReflectUtils.getColumnNameByField(getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass)) + "=?", new String[]{"" + valueId});
    }

    public long insert(Object object) {
        Class<? extends Object> klass = object.getClass();
        ContentValues values = new ContentValues();
        boolean b = fillContentValues(object, klass, values);
        if (!b) {
            return -2;
        }
        return mSQLiteDatabase.insert(geInnerTableName(klass), null, values);
    }


    */
/**
     * 删除通过id
     *
     * @param klass
     * @return
     *//*


    public <T> int deleteById(Class<T> klass, int id) {
        return delete(klass, ReflectUtils.getColumnNameByField(getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass)) + "=?", new String[]{"" + id});
    }

    */
/**
     * @param klass
     * @param fieldstr 字段
     * @param value    要查找的值
     * @param <T>
     * @return
     *//*

    public <T> int deleteByColumn(Class<T> klass, String fieldstr, String value) {
        return deleteByColumnFull(geInnerTableName(klass), fieldstr, value);
    }

    */
/**
     * @param table    完整的表明
     * @param fieldstr
     * @param value
     * @param <T>
     * @return
     *//*

    public <T> int deleteByColumnFull(String table, String fieldstr, String value) {
        return delete(table, fieldstr + "=?", new String[]{"" + value});
    }

    public <T> int deleteByColumnLike(Class<T> klass, String fieldstr, String value) {
        return delete(klass, fieldstr + " like '%" + value + "%'", null);
    }

    public <T> int deleteByColumn(Class<T> klass, String[] fieldArr, String[] valueArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < fieldArr.length; i++) {
            String s = fieldArr[i];
            stringBuffer.append(s + "=?" + (i == fieldArr.length - 1 ? "" : " and "));
        }
        return delete(klass, stringBuffer.toString(), valueArr);
//        return delete(klass, fieldstr + "=? and " + filedStr1 + "=?", new String[]{"" + value, value1});
    }

    public <T> int deleteByColumnOr(Class<T> klass, String[] fieldArr, String[] valueArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < fieldArr.length; i++) {
            String s = fieldArr[i];
            stringBuffer.append(s + "=?" + (i == fieldArr.length - 1 ? "" : " or "));
        }
        return delete(klass, stringBuffer.toString(), valueArr);
//        return delete(klass, fieldstr + "=? and " + filedStr1 + "=?", new String[]{"" + value, value1});
    }

    */
/**
     * 删除所有
     *
     * @param klass
     * @return
     *//*

    public <T> int deleteAll(Class<T> klass) {
        return delete(klass, null, null);
    }

    public <T> int delete(Class<T> klass, String whereClause, String[] whereArgs) {

        return delete(geInnerTableName(klass), whereClause, whereArgs);
    }

    */
/**
     * 完整的tabName 不包含别名
     *
     * @param table
     * @param whereClause
     * @param whereArgs
     * @param <T>
     * @return
     *//*

    public <T> int delete(String table, String whereClause, String[] whereArgs) {

        return mSQLiteDatabase.delete(table, whereClause, whereArgs);
    }

    */
/**
     * 查询id=某某
     * android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed. # Open Cursors=2 (# cursors opened by this proc=2)
     *
     * @param id
     * @return
     *//*

    public <T> T queryByID(Class<T> klass, int id) {
        String selection = ReflectUtils.getColumnNameByField(getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass)) + "=?";
        String table = geInnerTableName(klass);
        Cursor cursor = mSQLiteDatabase.query(table, null, selection, new String[]{"" + id}, null, null, null, null);

        while (cursor.moveToNext()) {
            T object = getObjectByCurosr(klass, cursor);
            cursor.close();
            return object;
        }
        if (cursor != null) {
            cursor.close();
        }
//		return query(t, null, selection, new String []{""+id});
        return null;
    }

    public boolean queryIDExist(Class klass, int id) {
        return queryColumnExist(klass, ReflectUtils.getColumnNameByField(getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass)), id + "");
    }

    public boolean queryColumnExist(Class klass, String column, String value) {
        String selection = column + "=?";
        boolean flag = false;
        String table = geInnerTableName(klass);
        Cursor cursor = mSQLiteDatabase.query(table, null, selection, new String[]{"" + value}, null, null, null, null);
        while (cursor.moveToNext()) {
            flag = true;
        }

        cursor.close();
//		return query(t, null, selection, new String []{""+id});
        return flag;
    }

    public <T> T queryFinal(Class<T> klass) {
        String field = (getDeclared ? ReflectUtils.getDeclaredIDField(klass).getName() : ReflectUtils.getIDField(klass).getName());
        return queryFinal(klass, field);
    }

    public <T> T queryFinal(Class<T> klass, String field) {
        String table = geInnerTableName(klass);
        //select * from (select t.*,from table t order by pxColumn desc) where rownum =1
        Cursor cursor = mSQLiteDatabase.query(table, null, null, null, null, null, field + " asc", "0,1");
        while (cursor.moveToNext()) {
            T object = getObjectByCurosr(klass, cursor);
            cursor.close();
            return object;
        }
        cursor.close();
//		return query(t, null, selection, new String []{""+id});
        return null;
    }

    */
/*
      ArrayList<T> arrayList = null;
     *//*



    */
/**
     * @param klass
     * @param column 要查询的列
     * @param value  要查询列所等于的值
     * @param <T>
     * @return
     *//*

    public <T> T queryByColumn(Class<T> klass, String column, String value) {
        String selection = column + "=?";
        String table = geInnerTableName(klass);
        Cursor cursor = mSQLiteDatabase.query(table, null, selection, new String[]{"" + value}, null, null, null, null);
        while (cursor.moveToNext()) {
            T object = getObjectByCurosr(klass, cursor);
            cursor.close();
            return object;
        }
        cursor.close();
//		return query(t, null, selection, new String []{""+id});
        return null;
    }

    */
/**
     * 查询类名通过 指定的字段
     *
     * @param t
     * @param field
     * @param obj
     * @return
     *//*

    public <T> List<T> queryAllByField(Class<T> t, Field field, Object obj) {
        String columnName = ReflectUtils.getColumnNameByField(field);
        return queryAllByField(t, columnName, obj);
    }

    public <T> List<T> queryAllByField(Class<T> klass, String filedName, Object value) {
        String selection = filedName + "=?";
        return query(klass, null, selection, new String[]{"" + value.toString()});
    }

    public <T> List<T> queryAllByField(Class<T> klass, String filedName, Object value, String fieldName1, Object value1) {
        String selection = filedName + "=? and " + fieldName1 + "=?";
        return query(klass, null, selection, new String[]{"" + value.toString(), "" + value1.toString()});
    }

    public <T> List<T> queryAllByFieldLike(Class<T> klass, String filedName, Object value) {
        String selection = filedName + " like '%" + value + "%'";
        return query(klass, null, selection, null);
    }

    public <T> ArrayList<T> queryFinalList(Class<T> klass, int count) {
        String field = (getDeclared ? ReflectUtils.getDeclaredIDField(klass).getName() : ReflectUtils.getIDField(klass).getName());
        return queryFinalList(klass, field, count);
    }

    public <T> ArrayList<T> queryFinalList(Class<T> klass, String field, int count) {
        String table = geInnerTableName(klass);
        //select * from (select t.*,from table t order by pxColumn desc) where rownum =1
        ArrayList<T> list = null;

        Cursor cursor = mSQLiteDatabase.query(table, null, null, null, null, null, field + " asc", "0," + count);
        if (cursor.getCount() > 0) {
            list = new ArrayList<>();
            while (cursor.moveToNext()) {
                T object = getObjectByCurosr(klass, cursor);
                list.add(object);
            }
        }
        cursor.close();
//		return query(t, null, selection, new String []{""+id});
        return list;
    }

    */
/**
     * 查询所有
     *
     * @param klass
     * @return
     *//*

    public <T> List<T> queryAll(Class<T> klass) {
        return query(klass, null, null, null);
    }

    */
/**
     * @param klass
     * @param desc  是否从大到小 降序查询
     * @param <T>
     * @return
     *//*

    public <T> List<T> queryAllIsDesc(Class<T> klass, boolean desc) {
        return queryAllIsDesc(klass, desc, null);
    }

    public <T> List<T> queryAllIsDesc(Class<T> klass, boolean desc, String fieldStr) {
        */
/**
         * SELECT * FROM SearchBean ORDER BY id desc
         *//*

        return query(klass, null, null, null, null, null, (fieldStr == null ? ReflectUtils.getColumnNameByField(getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass)) : fieldStr) + " " + (desc ? "desc" : "asc"), null);
    }

    */
/**
     * 查询id=某某 所有
     *
     * @param id
     * @return
     *//*

    public <T> List<T> queryAllByID(Class<T> klass, int id) {
        String selection = ReflectUtils.getColumnNameByField(getDeclared ? ReflectUtils.getDeclaredIDField(klass) : ReflectUtils.getIDField(klass)) + "=?";
        return query(klass, null, selection, new String[]{"" + id});
    }

    */
/**
     * @param klass
     * @param columns
     * @param selection
     * @param selectionArgs
     * @return
     *//*

    public <T> List<T> query(Class<T> klass, String[] columns, String selection, String[] selectionArgs) {
        return query(klass, columns, selection, selectionArgs, null, null, null, null);
    }

    */
/**
     * @param klass
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @param <T>
     * @return
     *//*


    public <T> List<T> query(Class<T> klass, String[] columns, String selection, String[] selectionArgs, String orderBy) {
        return query(klass, columns, selection, selectionArgs, null, null, orderBy, null);
    }

    */
/**
     * @param klass
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param <T>
     * @return
     *//*

    public <T> List<T> query(Class<T> klass, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        */
/**
         * 查询所有还是弄一个对象来吧 查询的填充到一个对象是
         *
         *//*

        String table = geInnerTableName(klass);
        ArrayList<T> arrayList = null;
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){
        Cursor cursor = mSQLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
                T object = getObjectByCurosr(klass, cursor);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;
    }


    */
/**
     * SELECT DISTINCT name FROM COMPANY;
     *
     * @param klassTable    反悔的字节码对象
     * @param klass         字节码对象
     * @param queryFiledStr 查询的字段
     * @param selectionArgs 填写的参数
     * @param <T>
     * @return
     *//*

    public <T> List<T> queryDistinct(Class klassTable, Class<T> klass, String queryFiledStr, String[] selectionArgs) {
        */
/**
         * 查询所有还是弄一个对象来吧 查询的填充到一个对象是
         *
         *//*

        String table = geInnerTableName(klassTable);
        ArrayList<T> arrayList = null;
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){//SELECT DISTINCT name FROM COMPANY;
        Cursor cursor = mSQLiteDatabase.rawQuery("select distinct " + queryFiledStr + " from  " + table, selectionArgs);
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
//                mDataBind object = getObjectByOnlyCurosr(klass, cursor, stringBuffer.toString());
                T object = getObjectByOnlyCurosr(klass, cursor, queryFiledStr);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;
    }

    */
/**
     * SELECT DISTINCT name FROM COMPANY;
     *
     * @param klassTable    反悔的字节码对象
     * @param klass         字节码对象
     * @param queryFiledStr 查询的字段
     * @param selectionArgs 填写的参数
     * @param <T>
     * @return
     *//*

    public <T> List<T> queryDistinct(Class klassTable, Class<T> klass, String[] queryFiledStr, String[] selectionArgs) {
        */
/**
         * 查询所有还是弄一个对象来吧 查询的填充到一个对象是
         *
         *//*

        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < queryFiledStr.length; i++) {
            stringBuffer.append(queryFiledStr[i]);
            if (i != queryFiledStr.length - 1) {
                stringBuffer.append(",");
            }
        }
        String table = geInnerTableName(klassTable);
        ArrayList<T> arrayList = null;
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){//SELECT DISTINCT name FROM COMPANY;
        Cursor cursor = mSQLiteDatabase.rawQuery("select DISTINCT " + stringBuffer.toString() + " from  " + table, selectionArgs);
        if (cursor.getCount() > 0) {
            arrayList = new ArrayList<T>();
            while (cursor.moveToNext()) {
//                mDataBind object = getObjectByOnlyCurosr(klass, cursor, stringBuffer.toString());
                T object = getObjectByCurosr(klass, cursor);
                arrayList.add(object);
            }
        }
        cursor.close();
        return arrayList;
    }

    */
/**
     * 通过游标给制定字段赋值
     *
     * @param klass
     * @param cursor
     * @param str
     * @param <T>
     * @return
     *//*

    private <T> T getObjectByOnlyCurosr(Class<T> klass, Cursor cursor, String str) {
        T object = ReflectUtils.getInstance(klass);//创建一个对象
        Field field = getDeclared ? ReflectUtils.getDeclaredField(klass, str) : ReflectUtils.getField(klass, str);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
     */
/*       if(ReflectUtils.isIgnoreFiled(field)){
                return null;
            }
            if (ReflectUtils.isConstant(field)) {
               return;
            }*//*

        String columnName = ReflectUtils.getColumnNameByField(field);
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex != -1) {
            if (ReflectUtils.isIntType(field))//INT
            {
                //直接通过名字找值 以前是通过getInt(下标)找方法为
                int valueInt = cursor.getInt(columnIndex);//管它是-1还是啥都赋值
                ReflectUtils.setValue(object, field, valueInt);
            } else if (ReflectUtils.isStringType(field)) {//String
                String valueString = cursor.getString(columnIndex);
                ReflectUtils.setValue(object, field, valueString);
            } else if (ReflectUtils.isBooleanType(field)) {//Boolean
                boolean valueBoolean = cursor.getInt(columnIndex) == 1 ? true : false;
                ReflectUtils.setValue(object, field, valueBoolean);
            } else if (ReflectUtils.isBytesType(field)) {//byte[]
                byte[] valueBytes = cursor.getBlob(columnIndex);
                ReflectUtils.setValue(object, field, valueBytes);
            } else if (ReflectUtils.isShortType(field)) {//Short
                short valueShort = cursor.getShort(columnIndex);
                ReflectUtils.setValue(object, field, valueShort);
            } else if (ReflectUtils.isLongType(field)) {//Long
                long valueLong = cursor.getLong(columnIndex);
                ReflectUtils.setValue(object, field, valueLong);
            } else if (ReflectUtils.isFloatType(field)) {//Float
                float valueFloat = cursor.getFloat(columnIndex);
                ReflectUtils.setValue(object, field, valueFloat);
            } else if (ReflectUtils.isDoubleType(field)) {//Double
                double valueDouble = cursor.getDouble(columnIndex);
                ReflectUtils.setValue(object, field, valueDouble);
            } else {
                //这里无法解决boolean类型和一些对象类型，所以还是不推荐这么做
                String valueStr = cursor.getString(columnIndex);//管它是-1还是啥都赋值
                ReflectUtils.setValue(object, field, valueStr);//这样赋值是字符串
            }
        } else {
            Log.w(TAG, "抱歉 " + field + "对于的列" + columnName + "找不到index");
        }
        return object;

    }

    */
/**
     * 逆向过程通过游标 给字节码的所有 赋值  从数据库查询出来赋值给对象 这么做会导致一个问题那就是没法赋值给枚举了.
     *
     * @param klass
     * @param cursor
     * @param <T>
     * @return
     *//*

    private <T> T getObjectByCurosr(Class<T> klass, Cursor cursor) {
        T object = ReflectUtils.getInstance(klass);//创建一个对象
        Field[] fields = getDeclared ? ReflectUtils.getDeclaredFields(klass) : ReflectUtils.getFields(klass);
        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (ReflectUtils.isIgnoreFiled(field)) {
                continue;
            }
            if (ReflectUtils.isConstant(field)) {
                continue;
            }
            String columnName = ReflectUtils.getColumnNameByField(field);
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {

                if (ReflectUtils.isIntType(field))//INT
                {
                    //直接通过名字找值 以前是通过getInt(下标)找方法为
                    int valueInt = cursor.getInt(columnIndex);//管它是-1还是啥都赋值
                    ReflectUtils.setValue(object, field, valueInt);
                } else if (ReflectUtils.isStringType(field)) {//String
                    String valueString = cursor.getString(columnIndex);
                    ReflectUtils.setValue(object, field, valueString);
                } else if (ReflectUtils.isBooleanType(field)) {//Boolean
                    boolean valueBoolean = cursor.getInt(columnIndex) == 1 ? true : false;
                    ReflectUtils.setValue(object, field, valueBoolean);
                } else if (ReflectUtils.isBytesType(field)) {//byte[]
                    byte[] valueBytes = cursor.getBlob(columnIndex);
                    ReflectUtils.setValue(object, field, valueBytes);
                } else if (ReflectUtils.isShortType(field)) {//Short
                    short valueShort = cursor.getShort(columnIndex);
                    ReflectUtils.setValue(object, field, valueShort);
                } else if (ReflectUtils.isLongType(field)) {//Long
                    long valueLong = cursor.getLong(columnIndex);
                    ReflectUtils.setValue(object, field, valueLong);
                } else if (ReflectUtils.isFloatType(field)) {//Float
                    float valueFloat = cursor.getFloat(columnIndex);
                    ReflectUtils.setValue(object, field, valueFloat);
                } else if (ReflectUtils.isDoubleType(field)) {//Double
                    double valueDouble = cursor.getDouble(columnIndex);
                    ReflectUtils.setValue(object, field, valueDouble);
                } else {
                    //这里无法解决boolean类型和一些对象类型，所以还是不推荐这么做
                    String valueStr = cursor.getString(columnIndex);//管它是-1还是啥都赋值
                    ReflectUtils.setValue(object, field, valueStr);//这样赋值是字符串
                }
            } else {
                Log.w(TAG, "抱歉 " + field + "对于的列" + columnName + "找不到index");
            }

        }
        return object;
    }

    private boolean fillContentValues(Object object, Class<? extends Object> klass, ContentValues values) {
        boolean flag = false;
        Field[] fields = getDeclared ? ReflectUtils.getDeclaredFields(klass) : ReflectUtils.getFields(klass);

        for (Field field : fields) {
            if (!getDeclared) {
                field.setAccessible(true);
            }
            if (ReflectUtils.isIgnoreFiled(field)) {
                continue;
            }
            if (ReflectUtils.isConstant(field)) {
                continue;
            }
            if (field.isSynthetic()) {
                continue;
            }

            boolean currentflag = fillContentValue(object, klass, values, field);
            Log.i(TAG, "fillContentValue->" + field.getName() + ",RESULT:" + currentflag);
            if (currentflag && flag == false) {
                flag = true;
            }
        }
        return flag;
    }


    */
/**
     * @param object
     * @param klass
     * @param values
     * @param fieldStr 字符串字段 将根据字节码反射出字段对象
     *//*

    private boolean fillContentValue(Object object, Class<? extends Object> klass, ContentValues values, String fieldStr) {

        Field field = getDeclared ? ReflectUtils.getDeclaredField(klass, fieldStr) : ReflectUtils.getField(klass, fieldStr);
        return fillContentValue(object, klass, values, field);

    }

    public void clearAlias() {
        setAlias("");
    }


    public static class HashMapDBInfo {
        List<HashMap<String, Object>> list;
        HashMap<String, Object> maxInfo;

        */
/**
         * 表示每一行的的key,键名键z值
         *
         * @return
         *//*

        public List<HashMap<String, Object>> getList() {
            return list;
        }

        public void setList(List<HashMap<String, Object>> list) {
            this.list = list;
        }

        */
/**
         * 键名有用，键值没用
         *
         * @return
         *//*


        public HashMap<String, Object> getMaxInfo() {
            return maxInfo;
        }

        public void setMaxInfo(HashMap<String, Object> maxInfo) {
            if (this.maxInfo == null) {
                this.maxInfo = maxInfo;

            } else if (this.maxInfo.size() < maxInfo.size()) {
                this.maxInfo = maxInfo;
            }
            //忽略
        }
    }

    */
/**
     * short 等使用其他的存储
     *
     * @param cursor
     * @return
     *//*


    public static HashMap<String, Object> queryCurosrToHashMap(Cursor cursor) {

        HashMap<String, Object> map = new HashMap<>();

        int columnCount = cursor.getColumnCount();

        for (int i = 0; i < columnCount; i++) {


            String key = cursor.getColumnName(i);

            int columnIndex = cursor.getColumnIndex(key);
            int type = cursor.getType(columnIndex);
            Object value = null;
            if (type == Cursor.FIELD_TYPE_INTEGER) {
                value = cursor.getInt(i);

            } else if (type == Cursor.FIELD_TYPE_FLOAT) {

                value = cursor.getFloat(i);

            } else if (type == Cursor.FIELD_TYPE_BLOB) {


                value = cursor.getBlob(i);
            } else if (type == Cursor.FIELD_TYPE_NULL) {


            } else if (type == Cursor.FIELD_TYPE_STRING) {

                value = cursor.getString(i);
            }

            map.put(key, value);

        }
        return map;
    }


    public HashMapDBInfo queryAllSaveCollectionsByClass(Class klass, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        HashMapDBInfo info = new HashMapDBInfo();
        List<HashMap<String, Object>> list = new ArrayList<>();
        String table = geInnerTableName(klass);
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){
        Cursor cursor = mSQLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        //List<HashMap<String, String>> find = null;

        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                HashMap<String, Object> object = queryCurosrToHashMap(cursor);
                info.setMaxInfo(object);
                list.add(object);
            }
        }
        cursor.close();
        info.setList(list);
        return info;


    }


    public HashMapDBInfo queryAllSaveCollections(String sql) {
        HashMapDBInfo info = new HashMapDBInfo();
        List<HashMap<String, Object>> list = new ArrayList<>();
        Log.i(TAG, "数据库是是否打开" + mSQLiteDatabase.isOpen());
//		if(mSQLiteDatabase.isOpen()){
        Cursor cursor = mSQLiteDatabase.rawQuery(sql, null);

        //List<HashMap<String, String>> find = null;


        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                HashMap<String, Object> object = queryCurosrToHashMap(cursor);
                info.setMaxInfo(object);
                list.add(object);
            }
        }
        cursor.close();
        info.setList(list);
        return info;


    }


    */
/**
     * 正向过程 把对象中的值赋值写入数据库
     *
     * @param object 对象实例
     * @param klass  字节码
     * @param values 已经初始化的contentValues()
     * @param field  字段对象
     *//*

    private boolean fillContentValue(Object object, Class<? extends Object> klass, ContentValues values, Field field) {
        boolean flag = false;
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        if (field.isSynthetic()) {
            Log.w(TAG, "isSynthetic 忽略");
            return true;
        }
        if (field == null || TextUtils.isEmpty(field.getName())) {
            Log.w(TAG, object.getClass().getSimpleName() + ".class中包含字段" + field);
//			new RuntimeException(object.getClass().getSimpleName()+".class中包含字段"+field);
            return false;
        }

        if (ReflectUtils.isConstant(field)) {
            Log.w(TAG, "常量忽略:" + field.getName());
            return false;
        }

        try {
            if (ReflectUtils.isIDField(field)) {
                int valueInt = ReflectUtils.getIntValue(object, field);
                Log.w(TAG, "id子炖:" + field.getName() + ",value:" + valueInt);
                if (valueInt > 0)// 大于0说明指定了值
                {
                    values.put(ReflectUtils.getColumnNameByField(field), valueInt);
                } else {
                    Log.w(TAG, "id没有设置,因此由系统自动产生");
                }
            } else if (ReflectUtils.isShortType(field)) {//short
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getShortValue(object, field));
            } else if (ReflectUtils.isBooleanType(field)) {//boolean
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getBooleanValue(object, field));
            } else if (ReflectUtils.isIntType(field)) {//int
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getIntValue(object, field));
            } else if (ReflectUtils.isLongType(field)) {//int
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getLongValue(object, field));
            } else if (ReflectUtils.isStringType(field)) {//String
                String stringValue = ReflectUtils.getStringValue(object, field);

                values.put(ReflectUtils.getColumnNameByField(field), stringValue);
            } else if (ReflectUtils.isBytesType(field)) {//Bolb  字节数组 等待验证//TODO 等待验证
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getBytesValue(object));
            } else if (ReflectUtils.isFloatType(field)) {//Float
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getFloatValue(object, field));
            } else if (ReflectUtils.isDoubleType(field)) {//Double
                values.put(ReflectUtils.getColumnNameByField(field), ReflectUtils.getDoubleValue(object, field));
            } else {
                Log.w(TAG, "无法识别字段类型 " + field + ",type:" + field.getType());
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "E:" + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    public static String getCursorValue(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }


    public int queryCount(Class aClass) {
        Cursor cursor = mSQLiteDatabase.rawQuery("select count(*) as count from " + aliasName + ReflectUtils.getTableNameByClass(aClass), null);
        boolean result = cursor.moveToNext();
        int intvalue;
        if (result) {
            int index = cursor.getColumnIndex("count");
            intvalue = cursor.getInt(index);
        } else {
            intvalue = 0;
        }
        return intvalue;

    }

    public class DBHepler {


        private SQLiteDatabase sqLiteDatabase;

        public SQLiteDatabase getSQLiteDatabase() {
            if (sqLiteDatabase == null) {
                synchronized (DBHepler.class) {
                    if (sqLiteDatabase == null) {
                        sqLiteDatabase = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
                    }
                }

            } else {
                if (!sqLiteDatabase.isOpen()) {

                    sqLiteDatabase = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
                }
            }

            return sqLiteDatabase;
        }

        */
/**
         * 需要别名
         *
         * @param tableName
         * @return
         *//*

        public boolean tableExist(String tableName) {
            */
/**
             * upper表示转大写，所以所有转大写要么全都不转大写
             *//*

            // name,type字段 sql表示为建表语句
            //select count(*)  from sqlite_master where type='table' and name = 'yourtablename';
            String sql = "select count(*) from sqlite_master where type = 'table' and upper(name) =upper( ? )";
            Cursor cursor = mSQLiteDatabase.rawQuery(sql, new String[]{tableName});

            boolean result = cursor.moveToNext() && cursor.getInt(0) > 0;
            Log.i(TAG, "表是否存在:" + tableName + ",result:" + result);
            return result;
        }

        public List<String> queryAllTable() {
            return queryAllTable(false);
        }

        public List<String> queryAllTable(boolean isoldMethod) {
            */
/**
             * SELECT name FROM sqlite_master
             WHERE type='table'
             ORDER BY name;
             * upper表示转大写，所以所有转大写要么全都不转大写
             *//*

            // name,type字段 sql表示为建表语句
            //select count(*)  from sqlite_master where type='table' and name = 'yourtablename';
//            String sql = "select count(*),name from sqlite_master where type = 'table' order by name";
            String sql;
            if (isoldMethod) {
                sql = "select count(*),name from sqlite_master where type = 'table' order by name";
            } else {
                sql = "select name from sqlite_sequence";

            }
            try {

                Cursor cursor = mSQLiteDatabase.rawQuery(sql, null);
                List<String> arrayList = null;
                if (cursor.getCount() > 0) {
                    arrayList = new ArrayList<String>();
                    while (cursor.moveToNext()) {
                        String value = DBUtils.getCursorValue(cursor, "name");
                        arrayList.add(value);

                    }
                }
                cursor.close();
                return arrayList;
            } catch (SQLiteException e) {
                if (!isoldMethod) {
                    Log.w(TAG, "" + sql + " error" + e.toString() + "无法查询所有表!");
                    return queryAllTable(true);

                } else {
                    Log.e(TAG, "" + sql + " error" + e.toString());

                }
            }
            return null;
        }

        */
/**
         * 不包含别名
         *
         * @param table
         * @param filed
         *//*


        public void deleteColumn(String table, String filed) {
            String sql = " alter table " + aliasName + table + " drop column " + filed;
            mSQLiteDatabase.execSQL(sql);

        }

        */
/**
         * 不包含别名 但是调用的 addColumn(table,colun,type)会加上别名。
         *
         * @param table  ReflectUtils.getTableNameByClass(LocalMusicInfo.class)
         * @param column
         *//*

        public void addColumn(String table, String column) {
            addColumn(table, column, "varchar");

        }

        */
/**
         * 不包含别名
         *
         * @param table
         * @param column
         * @param type
         *//*

        public void addColumn(String table, String column, String type) {
            String sql = "alter table " + aliasName + table + " add column " + column + " " + type;//ALTER TABLE Teachers ADD COLUMN Sex text;

            mSQLiteDatabase.execSQL(sql);
        }

        */
/**
         * 不包含别名
         *
         * @param table
         * @param newTable
         *//*

        public void reNameTable(String table, String newTable) {
            mSQLiteDatabase.execSQL("alter table " + aliasName + table + " rename to " + newTable);
        }

        */
/**
         * 我会自动加上别名，因此传递的表明不能加上别名
         *
         * @param tableName
         * @param columnName
         * @return
         *//*

        public boolean columnExist1(String tableName
                , String columnName) {
            boolean result = false;
            Cursor cursor = null;
            try {
                //查询一行
                cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " + aliasName + tableName + " LIMIT 0"
                        , null);
                result = cursor != null && cursor.getColumnIndex(columnName) != -1;
            } catch (Exception e) {
                Log.e(TAG, "checkColumnExists1..." + e.getMessage());
            } finally {
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            return result;
        }
        */
/**
         *    2、通过查询sqlite的系统表 sqlite_master 来查找相应表里是否存在该字段，稍微换下语句也可以查找表是否存在
         *//*


        */
/**
         * 方法2：检查表中某列是否存在 这种方法查询确实是真的，但是安卓为毛对于aler 增加进去的查询不到
         * 安卓的问题还是数据库问题我就不知道了。列加进去了
         * <p>
         * 情随事迁 2016/10/27 11:10:18
         * 但是执行的时候找不到列 ，还有一种方法也查询不到列 原始的方法查询到了也么用
         *
         * @param tableName  表名
         * @param columnName 列名
         * @return
         *//*

        public boolean columnExist2(String tableName
                , String columnName) {
            boolean result = false;
            Cursor cursor = null;

            try {
                cursor = mSQLiteDatabase.rawQuery("select * from sqlite_master where name = ? and sql like ?"
                        , new String[]{aliasName + tableName, "%" + columnName + "%"});
                int a = cursor.getCount();
                result = null != cursor && cursor.getCount() > 0;
//                result = null != cursor && cursor.moveToFirst() ;
            } catch (Exception e) {
                Log.e(TAG, "columnExists2..." + e.getMessage());
            } finally {
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            return result;
        }

        */
/**
         * 表是否存在 不存在则创建表，但是必须保证此表能反射 否则将创建失败
         *
         * @param classTable 字节码对象
         * @return
         *//*

        public boolean tableExistOrCreate(Class classTable) {
            if (!tableExist(geInnerTableName(classTable))) {
                createTable(classTable);
                return false;
            }
            return true;
        }

        public void execSQL(String sql) {
            mSQLiteDatabase.execSQL(sql);
        }

        public void close() {
            mSQLiteDatabase.close();
            mSQLiteDatabase = null;

        }
        // public boolean
    }

    public void close() {
        if (dbHepler != null) {
            dbHepler.close();
        }
        dbHepler = null;
        context = null;
    }

    public boolean isClose() {
        return dbHepler == null;
    }

    public void setGetDeclared(boolean getDeclared) {
        this.getDeclared = getDeclared;
    }

    */
/*

     true,当前公共、保护、默认（包）访问和私有方法/ 成员，但不包括父类的方法 getFileds父类子类所有public

     *//*

    private boolean getDeclared = false;

}
*/
