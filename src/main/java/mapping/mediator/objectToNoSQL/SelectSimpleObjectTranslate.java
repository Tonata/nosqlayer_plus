package mapping.mediator.objectToNoSQL;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import mapping.statement.selectClause.SelectClause;
import connectionConfig.MongoConnection;
import net.sf.jsqlparser.JSQLParserException;

import static mapping.convert.QueryInterceptor.database;


/**
 * Created by martian on 2016/06/03.
 */
public class SelectSimpleObjectTranslate {

    public DBCursor executeMongoSelect(SelectClause selectStatement) throws JSQLParserException{
//        database = MongoConnection.getInstance().getDB(); // MAKE LESS COUPLED

        DBCollection collection = database.getCollection(selectStatement.getTablesQueried().get(0).getName());

//        System.out.println("1 - DBCollection: " + collection.toString());

        DBCursor dbCursor;
        DBObject order = (DBObject) JSON.parse(returnOrder(selectStatement));
        int limit = 0, offset = 0;
        BasicDBObject projection;
        BasicDBObject projection_ppl = new BasicDBObject();

        if (returnProjection(selectStatement) != null) {
            projection = returnProjection(selectStatement);
        } else {
            projection = new BasicDBObject("_id", 0);
        }

//        if (selectStatement.getLimit() != null) {
//            limit = (int) selectStatement.getLimit().getRowCount();
//            offset = (int) selectStatement.getLimit().getOffset();
//        }
        if (order != null) {
            dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection).sort(order).skip(offset).limit(limit);
//            System.out.println("1 hallo: " + order);
        } else {
            dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection).skip(offset).limit(limit);
//            System.out.println("2 hallo: " + order);
        }
//        dbCursor = collection.find(projection_ppl, projection);
//        dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection).skip(offset).limit(limit);

        return dbCursor;
    }

    public BasicDBObject returnProjection(SelectClause select) {
        BasicDBObject fields = new BasicDBObject();
        if (select.getTablesQueried().get(0).isIsAllColumns()) {
//            System.out.println("yes");
            return null;

        } else {

            if (select.getTablesQueried().get(0).getParam_projection().size() > 0) {

                if (!(select.getTablesQueried().get(0).isIsAllColumns())) {
                    for (int i = 0; i < select.getTablesQueried().get(0).getParam_projection().size(); i++) {
                        if (select.getTablesQueried().get(0).getParam_projection().get(i).getAlias() != null) {
                            String alias = select.getTablesQueried().get(0).getParam_projection().get(i).getAlias();
                            fields.put(select.getTablesQueried().get(0).getParam_projection().get(i).getName(), alias);

                        } else {
                            fields.put(select.getTablesQueried().get(0).getParam_projection().get(i).getName(), 1);
                            System.out.println("yes 1 " + select.getTablesQueried().get(0).getParam_projection().get(i).getName() );
                        }

                        System.out.println("4 - Select Order size: " + select.getOrder().size());
                    }
                }
            } else {
//                System.out.println("yes");
                return null;

            }
        }
//        fields.put("_id", 0);
        return fields;
    }

    public String returnOrder(SelectClause select) {

        String queryMongo = null;

        System.out.println(": " + select.getOrder().size());
      //  if (select.getOrder().size() > 0) {
        //    System.out.println("2 - Select Order size: " + select.getOrder().size());
            queryMongo = "{";

            for (int i = 0; i < select.getOrder().size(); i++) {
                queryMongo += select.getOrder().get(i).getAttribute() + ": " + select.getOrder().get(i).getOrder() + ",";

            }


            queryMongo += "}";
            System.out.println("3 - queryMongo: " + queryMongo);
      //  }

        return queryMongo;
    }
}
