package mapping.mediator.objectToNoSQL;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import mapping.statement.InsertClause;
import connectionConfig.MongoConnection;
import net.sf.jsqlparser.JSQLParserException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by martian on 2016/06/03.
 */
public class InsertObjectTranslate {

    public DB database = MongoConnection.getInstance().getDB();

    public void executeMongoInsert(InsertClause insertStatement) throws JSQLParserException {

        String mongoQuery = this.returnMongoQuery(insertStatement);
        DBObject dbObject = (DBObject) JSON.parse(mongoQuery);
        DBCollection collection = database.getCollection(insertStatement.getTable().getName());

        AutoIncrementInsert auto_inc = new AutoIncrementInsert();
        String next_id = auto_inc.getNextId(database, collection.getName());
        int id_final = Integer.parseInt(next_id);
        String atributeAutoInc = auto_inc.getAtributeAutoInc(database, collection.getName());
        dbObject.put(atributeAutoInc, id_final);
        collection.insert(dbObject);

    }

    public String returnMongoQuery(InsertClause insert) {
        String mongoQuery;

        mongoQuery = "{";
        for (int i = 0; i < insert.getColumns().size(); i++) {

            mongoQuery += insert.getColumns().get(i).toString() + ": ";
            if (insert.getValues().getExpressions().get(i).toString().equals("NOW")) {
                mongoQuery += "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).toString() + "'";
            } else {
                mongoQuery += insert.getValues().getExpressions().get(i).toString();
            }
            if (i != insert.getColumns().size() - 1) {
                mongoQuery += ",";
            }
        }
        mongoQuery += "}";
        return mongoQuery;
    }
}
