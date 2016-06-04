package mapping.statement;

import com.mongodb.*;
import com.mongodb.util.JSON;
import mapping.convert.QueryInterceptor;
import mapping.statement.selectClause.SelectClause;
import net.sf.jsqlparser.JSQLParserException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by martian on 2016/06/03.
 */
public class SimpleSelect {

    public static String returnResultSet(DBCursor select_result, String header) throws JSQLParserException {

        String result_set = "";
        DBObject currentObj;
        JSONArray array_header = new JSONArray("[" + header + "]");

        long this_select = System.currentTimeMillis();
        while (select_result.hasNext()) {
            result_set += "{";

            currentObj = select_result.next();

            for (int j = 0; j < array_header.length(); j++) {
                String current_attr = array_header.get(j).toString();
                if (currentObj.containsField(current_attr)) {
                    result_set += "\"" + currentObj.get(current_attr) + "\", ";
                } else {
                    result_set += "\" \" , ";
                }
            }
            result_set += "},";
        }
        long fim = System.currentTimeMillis();
        //}

        return result_set;
    }


    public static String returnHeader(SelectClause selectStatement) {
        String header = "";


        if (selectStatement.getFunctions().isEmpty()) {
            for (int i = 0; i < selectStatement.getTablesQueried().size(); i++) {
                if (selectStatement.getTablesQueried().get(i).isIsAllColumns()) {
                    SimpleSelect agg = new SimpleSelect();
                    String table = selectStatement.getTablesQueried().get(i).getName().toString();
                    String allColumnHeaders = agg.returnAllColumnHeaders(table);
                    header += allColumnHeaders + ",";

                } else {
                    for (int j = 0; j < selectStatement.getTablesQueried().get(i).getParam_projection().size(); j++) {
                        if (selectStatement.getTablesQueried().get(i).getParam_projection().get(j).getAlias() != null) {
                            header += "'" + selectStatement.getTablesQueried().get(i).getParam_projection().get(j).getAlias() + "',";
                        } else {
                            header += "'" + selectStatement.getTablesQueried().get(i).getParam_projection().get(j).getName() + "',";
                        }

                    }
                }
            }

        } else {
            for (int i = 0; i < selectStatement.getFunctions().size(); i++) {
                if (selectStatement.getFunctions().get(i).getAlias() != null) {
                    header += selectStatement.getFunctions().get(i).getAlias().toString() + ",";
                } else {
                    header += selectStatement.getFunctions().get(i).getNameComplete() + ",";
                }
            }
        }

        return header;
    }

    public String returnAllColumnHeaders(String table) {
        DBCollection collection = QueryInterceptor.database.getCollection("metadata");
        DBObject projection = (DBObject) JSON.parse("{columns:1, _id:0}");
        BasicDBObject projection_ppl = new BasicDBObject("$project", projection);
        DBObject criteria = (DBObject) JSON.parse("{table:'" + table + "'}");
        BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);

        AggregationOutput output = collection.aggregate(criteria_ppl, projection_ppl);
        String json_str = output.results().toString();
        String attributes = "";
        int result_size = json_str.length();
        if (!json_str.toString().equals("[ ]")) {
            JSONObject my_obj = new JSONObject(json_str.substring(1, result_size - 1));
            attributes = my_obj.get("columns").toString().substring(1, my_obj.get("columns").toString().length() - 1);
            attributes = attributes.replaceAll("[\"]", "'");
        }
        return attributes;
    }
}
