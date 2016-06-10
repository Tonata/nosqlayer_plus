import connectionConfig.MySQLConnection;
import connectionConfig.RedisConnection;
import migration.model.Database;
import migration.model.Table;
import migration.sqlToNoSQLConverter.Migrate;
import migration.sqlToNoSQLDAO.TableDAO;
import org.testng.annotations.*;
import redis.clients.jedis.Jedis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by martian on 2016/05/12.
 */
public class MigrationTest {

    private static Migrate migrate;
    private static MySQLConnection mysqlConnection;
    private static Database DB;
    private static Database DB_new;
    private static long initObjExec_time, finalObjExec_time             = 0;
    private static long initNoSQlExec_time, finalNoSQLExec_time         = 0;
    private static double totalExecTime                                 = 0;
    private static Jedis jedis;

    public MigrationTest() {

    }
//    @Test
//    public void redisTest(){
//        jedis = new Jedis("localhost", 6379);
//        Map<String, Object> mapTable = new HashMap<>();
//
//
//
//
//        DB_new = migrate.mountObjects(mysqlConnection.getConnection(), DB);
//
//        finalObjExec_time = System.nanoTime();
//
//        System.out.println("Time 1: " +(finalObjExec_time - initObjExec_time)/1e6);
//
//        for (Table table : DB_new.getTables()){
//
//            String sql2 = "SELECT * FROM " + table.getName()+ ";"; /*+ " LIMIT "+start+","+sub_total;*/
//
//            try (PreparedStatement stmt2 = mysqlConnection.getConnection().prepareStatement(sql2)) {
//                ResultSet resultSet2 = stmt2.executeQuery();
//                ResultSetMetaData mD = resultSet2.getMetaData();
//                int columns = mD.getColumnCount();
//                while (resultSet2.next()) {
//
////                    new TableDAO().save(table, resultSet2);
//                    for (int i = 1; i <= columns; i++){
//                        mapTable.put(mD.getColumnName(i), resultSet2.getObject(i));
////                System.out.println("Column Name: " + mD.getColumnName(i));
////                System.out.println("Tuple Obj: " + tuple.getObject(i));
//
//                    }
//
//                    jedis.lpush(mapTable.keySet().toString(), mapTable.values().toString());
//
//
//                }
//                finalNoSQLExec_time = System.nanoTime();
//                System.out.println("Time 2: " +(finalNoSQLExec_time - initObjExec_time)/1e6);
//            }catch(SQLException e){
//
//            }
//        }
//
//
//
////        jedis.lpush();
//    }


    @Test
    public void mountObjects_test(){
        DB_new = migrate.mountObjects(mysqlConnection.getConnection(), DB);


        finalObjExec_time = System.nanoTime();

        System.out.println("Time 1: " +(finalObjExec_time - initObjExec_time)/1e6);
    }

    @Test(dependsOnMethods = "mountObjects_test")
    public void createMetaData_test(){

        migrate.createMetaData(DB_new,
                               mysqlConnection.getConnection());

        finalNoSQLExec_time = System.nanoTime();

        System.out.println("Time 2: " +(finalNoSQLExec_time - initObjExec_time)/1e6);

    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        initObjExec_time   = System.nanoTime();
        DB                  = new Database();
        mysqlConnection     = new MySQLConnection("videoFour"); // Name of RDBMS relational schema to migrate
        migrate             = new Migrate();


    }

    @AfterClass
    public static void tearDownClass() throws Exception {

    }

    @BeforeMethod
    public static void setUpMethod() throws Exception {

    }

    @AfterMethod
    public static void tearDownMethod() throws Exception {

    }
}
