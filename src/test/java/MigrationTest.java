import connectionConfig.MySQLConnection;
import migration.model.Database;
import migration.sqlToNoSQLConverter.Migrate;
import org.testng.annotations.*;

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

    public MigrationTest() {

    }

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
        mysqlConnection     = new MySQLConnection("world");
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
