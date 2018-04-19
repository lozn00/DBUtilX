#### dbutil

* 支持javaclass自动创建表
* 支持javaobject查询
* 支持列名、表名别名、列名指定存储的数据库类型
* 遵循avaebann结构进行填充
* 支持javaobject传递插入
* 支持javaobject 传递查询
* 支持sql查询
* 支持 list<Map<String,Object>行列结构查询
* 支持一个class生成多个表
* 支持自动化升级数据库字段
* 内置多个语法糖,倒叙反向查询所有、删除、改表名，等等。


本项目本来一直没有用git,是需要什么功能自己添加什么功能，慢慢了有了这么多需求，于是我就完成了这么多.


dbutil于2015年创建，如今已经过了3个年头，其中较大的改动使javaobject的填充 以及子类父类问题，由之前的只能获取当前类是声明字段，或者只能获取所有类公开字段 ，有空直接根据public method 直接算出字段然后进行调用，
目前方式是先确定私有字段然后查询method方法是否存在一定程度上效率应该没有直接查询public method然后得出字段大法好!
添加依赖

```
implementation  "com.github.com:xxx"```//各位需要的话我马上发布 2018-4-18 23:33:24 我先睡觉

DBUtil对象语法糖概括 下面并不代表所有只是列举大概
```
 boolean result = dbUtils.createTable(TestTable2.class);
public int update(Object object)；
public int updateAllByField(Object object, String column, String value);
    public <T> int deleteById(Class<T> klass, int id) {
  public <T> int deleteByColumn(Class<T> klass, String fieldstr, String value) {
  public <T> int deleteByColumnLike(Class<T> klass, String fieldstr, String value) {
 public <T> int deleteAll(Class<T> klass) {
    public <T> int delete(Class<T> klass, String whereClause, String[] whereArgs) {
 public <T> T queryByID(Class<T> klass, int id) {
    public static <T> List<T> queryBeanListByCurosr(Class<T> klass, boolean getDeclared, Cursor cursor) {
    public boolean queryIDExist(Class klass, int id) {
    public boolean queryColumnExist(Class klass, String column, String value) {
  public <T> T queryFinal(Class<T> klass) {
 public <T> T queryByColumn(Class<T> klass, String column, String value) {
    public <T> List<T> queryAllByField(Class<T> t, Field field, Object obj) {
    public <T> List<T> queryAllIsDesc(Class<T> klass, boolean desc) {
    public <T> List<T> query(Class<T> klass, String[] columns, String selection, String[] selectionArgs) {
    public static HashMap<String, Object> queryCurosrToHashMap(Cursor cursor) {
    public HashMapDBInfo queryAllSaveCollections(String sql) {
    public <T> List<T> queryDistinct(Class klassTable, Class<T> klass, String queryFiledStr, String[] selectionArgs) {
```
**DBUtils.ToolHelper**静态工具方法
```
 public static boolean tableExist(SQLiteDatabase liteDatabase, String tableName) {
     public static void deleteColumn(SQLiteDatabase liteDatabase, String table, String filed) {
        public static boolean columnExist(SQLiteDatabase liteDatabase, String tableName  , String columnName) {
  public static void addColumn(SQLiteDatabase liteDatabase, String table, String column) {
        public static void addColumn(SQLiteDatabase liteDatabase, String table, String column, String type) {
       public static void reNameTable(SQLiteDatabase liteDatabase, String table, String newTable) {
    public static void deleteTable(SQLiteDatabaseObj sqLiteDatabaseObj, String tableName) {
```


使用方法



demo中的初始化方法

```

public static void init(DBUtils dbUtils) {

boolean exist = DBUtils.ToolHelper.tableExist(dbUtils.getDb(), getQQGroupWhiteNameDBUtil(dbUtils).geInnerTableName(GroupWhiteNameBean.class));

if (exist) {//标存在,修改为指定new table//如果表已经存在了.
    //多次执行不会出现任何问题
    DBUtils.insertNewCloumnFromClasss(DBHelper.getQQGroupWhiteNameDBUtil(dbUtils), GroupWhiteNameBean.class, dbUtils.getDb());


} else {

    boolean result = DBHelper.getQQGroupWhiteNameDBUtil(dbUtils).createTable(GroupWhiteNameBean.class);//

    if (result) {
        int count = 10;
        for (int i = 0; i < count; i++) {
            GroupWhiteNameBean table2 = new GroupWhiteNameBean();
            table2.setTest1("我是class 的第1个表");
            table2.setMoney(String.valueOf(1005.5 * i));
            dbUtils.insert(table2);//已经设置别名了，可以直接这样写。
            DBHelper.getQQGroupWhiteNameDBUtil(dbUtils).insert(table2);

        }

    }


}


exist = DBUtils.ToolHelper.tableExist(dbUtils.getDb(), getTableclassAlias2(dbUtils).geInnerTableName(GroupWhiteNameBean.class));

if (exist) {//标存在,修改为指定new table//如果表已经存在了.
    //多次执行不会出现任何问题  如果这个table可能 会升级字段的，那么创建表都需要这么写，
    DBUtils.insertNewCloumnFromClasss(DBHelper.getTableclassAlias2(dbUtils), GroupWhiteNameBean.class, dbUtils.getDb());
    //LogUtil.writeLog("发现老表" + groupTableOld + "存在，尝试导入老表数据到new new table " + groupTableNew + ",result:");
} else {//第一次创建，


    boolean result = DBHelper.getTableclassAlias2(dbUtils).createTable(GroupWhiteNameBean.class);//
    if (result) {
        int count = 5;
        for (int i = 0; i < count; i++) {
            GroupWhiteNameBean nameBean = new GroupWhiteNameBean();
            nameBean.setMoney(String.valueOf(105.5 * i));
            nameBean.setTest1("我是class 的第二个表");
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


java字段声明和javabean一样，只有public method set才可以读取

字段声明示范:

```

String test1;

int test2;

@ID
int idx;//如果没有这个默认是_id
int mydb;
@Column("helo")//列名不以java字段命名
        int mydb1;


@ColumnType("REAL")
private String money = "";//虽然这里是字符串，但是存储的时候是小数点哈!  为什么要这么高？这里可以直接实现依然查询大小，但是又不影响java字段.

```
如何使用
```
```
compile 'cn.qssq666:dbmodule:v0.1'
```