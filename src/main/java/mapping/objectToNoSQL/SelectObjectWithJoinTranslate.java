package mapping.objectToNoSQL;

import com.mongodb.*;
import com.mongodb.util.JSON;
import mapping.selectClause.ProjectionParams;
import mapping.selectClause.SelectClause;
import mapping.selectClause.Sort;
import migration.connectionConfig.MongoConnection;
import net.sf.jsqlparser.JSQLParserException;

import java.util.List;

/**
 * Created by martian on 2016/06/03.
 */
public class SelectObjectWithJoinTranslate {

    public static DB database = MongoConnection.getInstance().getDB();

    public String executeMongoSelect(SelectClause selectStatement, String header) throws JSQLParserException {

//        String subDocument = getSubDocument(selectStatement);
//
//        if (!subDocument.equals("")) {
//            switch (selectStatement.getJoinList().get(0).getTipoJoin()) {
//                case "INNER":
//                    InnerJoinExecute innerJoin = new InnerJoinExecute();
//                    return innerJoin.executeInnerJoinSubDoc(selectStatement, header, subDocument);
//                case "LEFT":
//                    LeftJoinExecute leftJoin = new LeftJoinExecute();
//                    return leftJoin.executeLeftJoinSubDoc(selectStatement, header, subDocument);
//                case "RIGHT":
//                    System.out.println("right sub doc");
//                    RightJoinExecute rightJoin = new RightJoinExecute();
//                    return rightJoin.executeRightJoinSubDoc(selectStatement, header, subDocument);
//            }
//        } else {
//            System.out.println("nao sub");
//            switch (selectStatement.getJoinList().get(0).getTipoJoin()) {
//                case "INNER":
//                    InnerJoinExecute innerJoin = new InnerJoinExecute();
//                    return innerJoin.executeInnerJoin(selectStatement, header);
//                case "LEFT":
//                    LeftJoinExecute leftJoin = new LeftJoinExecute();
//                    return leftJoin.executeLeftJoin(selectStatement, header);
//                case "RIGHT":
//                    RightJoinExecute rightJoin = new RightJoinExecute();
//                    return rightJoin.executeRightJoin(selectStatement, header);
//            }
//        }
//
//        return "";
        return "";
    }

    public String getSubDocument(SelectClause selectStatement) {
        DBCollection join_collection = database.getCollection("join_tables");
        String table_left = selectStatement.getJoinList().get(0).getTableLeftExpression();
        String table_right = selectStatement.getJoinList().get(0).getTableRightExpression();
        String subDocument;

        //Seeking only 1 SubDoc the left table for now
        DBObject search_table = new BasicDBObject("document", table_left);
        DBObject embedded_doc = new BasicDBObject("embbed", 1);
        DBCursor embedded_search = join_collection.find(search_table, embedded_doc);

        if (embedded_search.size() != 0) {
            while (embedded_search.hasNext()) {
                subDocument = embedded_search.next().get("embbed").toString();

                if (subDocument.equals(table_right)) {
                    return subDocument;
                }
            }
        } else {
            // Search subdoc in left table
            DBObject search_table_right = new BasicDBObject("document", table_right);
            DBObject embedded_doc_left = new BasicDBObject("embbed", 1);
            DBCursor embedded_search_right = join_collection.find(search_table_right, embedded_doc_left);
            while (embedded_search_right.hasNext()) {
                subDocument = embedded_search_right.next().get("embbed").toString();
                if (subDocument.equals(table_left)) {
                    return subDocument;
                }
            }
        }

        return "";
    }

    public DBCursor query_result(SelectClause selectStatement, String selectedTable) {
        String alias_table, table;
        DBCollection collection;
        List<ProjectionParams> projection_collection;
        DBCursor dbCursor = null;

        if (selectedTable.equals("left")) {

            alias_table = selectStatement.getJoinList().get(0).getTableAliasLeftExpression();
            if (alias_table != null) {
                table = selectStatement.convertAliasToName(alias_table);
            } else {
                table = selectStatement.getJoinList().get(0).getTableLeftExpression();
            }
        } else {
            alias_table = selectStatement.getJoinList().get(0).getTableAliasRightExpression();
            if (alias_table != null) {
                table = selectStatement.convertAliasToName(alias_table);
            } else {
                table = selectStatement.getJoinList().get(0).getTableRightExpression();
            }
        }

        collection = database.getCollection(table);

        projection_collection = selectStatement.returnAttributesByAlias(alias_table);
        if (projection_collection == null) {
            projection_collection = selectStatement.returnAttributesByName(table);
        }

        ProjectionParams attr_projection = new ProjectionParams();
        BasicDBObject projection = null;

        if (!returnProjection(projection_collection).equals("*")) {
            projection = returnProjection(projection_collection);
        }

        dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection);

        return dbCursor;
    }

    public DBCursor queryResultOrder(SelectClause selectStatement, String selectedTable, Sort sortOrder) {
        String alias_table, table;
        DBCollection collection;
        List<ProjectionParams> projection_collection;
        DBCursor dbCursor = null;

        table = sortOrder.getReferencedTable();
        collection = database.getCollection(table);
        DBObject order = (DBObject) JSON.parse(returnJoinOrder(sortOrder));
        alias_table = selectStatement.convertNameToAlias(table);


        if (alias_table != null) {
            projection_collection = selectStatement.returnAttributesByAlias(alias_table);
        } else {
            projection_collection = selectStatement.returnAttributesByName(table);
        }
        BasicDBObject projection = null;

        if (returnProjection(projection_collection) != null) {
            projection = returnProjection(projection_collection);
        } else {
            projection = new BasicDBObject("_id", 0);
        }

        dbCursor = collection.find(selectStatement.getCriteriaIdentifier().whereQuery, projection).sort(order);

        return dbCursor;
    }

    public BasicDBObject returnProjection(List<ProjectionParams> project_params) {
        BasicDBObject fields = new BasicDBObject();
        if (project_params.get(0).getName().equals("*")) {
            return null;
        } else {

            if (project_params.size() > 0) {
                for (int i = 0; i < project_params.size(); i++) {
                    if (project_params.get(i).getAlias() != null) {
                        String alias = project_params.get(i).getAlias();
                        fields.put(project_params.get(i).getName(), alias);
                    } else {
                        fields.put(project_params.get(i).getName(), 1);
                    }
                }
            } else {
                return null;
            }
            fields.put("_id", 0);
            return fields;
        }
    }

    public String returnJoinOrder(Sort order) {

        String queryMongo = "{" + order.getAttribute() + ": " + order.getOrder() + "}";

        return queryMongo;
    }
}
