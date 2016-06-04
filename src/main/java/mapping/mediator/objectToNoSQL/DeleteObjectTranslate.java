package mapping.mediator.objectToNoSQL;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import mapping.statement.DeleteClause;
import connectionConfig.MongoConnection;
import net.sf.jsqlparser.JSQLParserException;

/**
 * Created by martian on 2016/06/03.
 */
public class DeleteObjectTranslate {

    public DB database = MongoConnection.getInstance().getDB();

    public void executeMongoDelete(DeleteClause deleteObject) throws JSQLParserException{

        DBObject dbObj = returnDeletedMongoObj(deleteObject);
        DBCollection collection = database.getCollection(deleteObject.getTable().getName());
        collection.remove(dbObj);
    }

    public DBObject returnDeletedMongoObj(DeleteClause deleteStatement) throws JSQLParserException{
        String queryMongo = deleteStatement.getCriteriaIdentifier().whereQuery.toString();

        return (DBObject) JSON.parse(queryMongo);
    }
}
