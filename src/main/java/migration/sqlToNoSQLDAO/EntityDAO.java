package migration.sqlToNoSQLDAO;


import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import connectionConfig.MongoConnection;
import migration.model.Column;
import migration.model.Table;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by martian on 2016/05/12.
 */
public class EntityDAO<T> {

    private DBCollection dbCollection;


    public void setCollection(String collection){
        this.dbCollection = MongoConnection.getInstance().getDB().getCollection(collection);
    }

    protected DBCollection getDbCollection() {
        return dbCollection;
    }

    public void save(Map<String, Object> mapEntity, Table table) {

        BasicDBObject document = new BasicDBObject(mapEntity);
        dbCollection.save(document);


//         System.out.println("Save :> " + document);
    }

    public void ensureIndex(Table table){
        setCollection(table.getName());

        String str_indices = "{";

        for(Column column:table.getColumns()){
            if(column.isIsUnique()){
                str_indices += "\""+column.getName()+"\" : 1,";
            }
        }
        str_indices = str_indices.substring(0, str_indices.length()-1);
        str_indices += "}";

//        System.out.println(str_indices);

        if(!str_indices.equals("{}") && !str_indices.equals("}")) {
            DBObject indices = (DBObject) JSON.parse(str_indices);

//            dbCollection.ensureIndex(indices, new BasicDBObject("unique", true));
            dbCollection.createIndex(indices, new BasicDBObject("unique", true));
        }

    }

}
