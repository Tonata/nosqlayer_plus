import connectionConfig.MySQLConnection;
import migration.model.Database;
import migration.sqltoNoSQLConverter.Migrate;
import org.testng.annotations.*;

/**
 * Created by martian on 2016/05/12.
 */
public class MigrationTest {

    private Migrate migrate;
    private MySQLConnection mysqlConnection;
    private Database DB;

    public MigrationTest() {


    }

    @Test
    public void ya(){

        DB = new Database();
        mysqlConnection = new MySQLConnection("world");
        migrate = new Migrate();

        migrate.createMetaData(   migrate.mountObjects(mysqlConnection.getConnection(), DB),
                                    mysqlConnection.getConnection());
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

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
