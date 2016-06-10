package connectionConfig;

import redis.clients.jedis.Jedis;

/**
 * Created by martian on 2016/05/12.
 */
public class RedisConnection {

    private String DATABASE;
    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    Jedis jedis = new Jedis(HOST, PORT);

    public RedisConnection(String DBNAME) {
        this.DATABASE = DBNAME;
    }

    public String getDATABASE() {
        return DATABASE;
    }

    public void setDATABASE(String DATABASE) {
        this.DATABASE = DATABASE;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public static String getHOST() {
        return HOST;
    }

    public static int getPORT() {
        return PORT;
    }
}
