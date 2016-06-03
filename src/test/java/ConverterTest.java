import migration.connectionConfig.MySQLConnection;
import migration.model.Database;
import migration.sqltoNoSQLConverter.Converter;
import org.testng.annotations.*;

/**
 * Created by martian on 2016/05/12.
 */
public class ConverterTest {

    private Converter converter;
    private MySQLConnection conn;
    private Database DB;

    public ConverterTest() {

        converter = new Converter();
    }

    @Test
    public void ya(){

        DB = new Database();
        conn = new MySQLConnection("world");

        converter.createMetaData(   converter.mountObjects(conn.getConnection(), DB),
                                    conn.getConnection());
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
