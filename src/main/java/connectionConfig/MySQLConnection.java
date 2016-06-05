package connectionConfig;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Created by martian on 2016/05/12.
 */
public class MySQLConnection  {

    private String DATABASE;
    private static final String HOST = "localhost";
    private static final int PORT = 3306;

//    private static DBCollection dbCollection;

    //  Database credentials
    private static final String USER = "root";
    private static final String PASS = "user316";

    private static final String DB_URL = "jdbc:mysql://localhost/"; /* should not be hard coded
                                                                            (concatenate variable DATABASE) */
//    private static final String JDBC_Driver = "com.mysql.jdbc.Driver";

    private static final String JDBC_Driver = "com.mysql.cj.jdbc.Driver";

    private Connection connection;

    public MySQLConnection(String DB_NAME) {
        this.DATABASE = DB_NAME;

    }

    public MySQLConnection() {
        super();

    }


    public Connection getConnection() {

        Connection connection = null;

        try {

            Class.forName(JDBC_Driver).newInstance();
            connection = DriverManager.getConnection(DB_URL + DATABASE,USER,PASS);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    public void setConnection(Connection conn){
        this.connection = conn;
    }

    public  String getDATABASE() {
        return DATABASE;
    }

    public String getHOST() {
        return HOST;
    }

    public int getPORT() {
        return PORT;
    }


}
