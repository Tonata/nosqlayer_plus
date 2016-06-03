package migration.connectionConfig;

import java.net.UnknownHostException;
import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Created by martian on 2016/05/12.
 */
public class MongoConnection {

    private static final String HOST = "localhost";
    private static final int PORT = 27017;
    private static final String DB_NAME = "world";  /* remove, so that name is dynamically assigned */
    private static MongoConnection uniqueInstance;
    private static int mongoInstance = 1;

    private Mongo mongo;
    private DB db;

    private MongoConnection(){
    }

    public static synchronized MongoConnection getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new MongoConnection();
        }
        return uniqueInstance;
    }


    public DB getDB() {

        if (mongo == null) {

            mongo = new Mongo(HOST, PORT);
            db = mongo.getDB(DB_NAME);
        }
        return db;
    }
}
