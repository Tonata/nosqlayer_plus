package aggregationFramework;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import mapping.queryInterceptor.QueryInterceptor;
import mapping.selectClause.Functions;
import mapping.selectClause.SelectClause;
import net.sf.jsqlparser.JSQLParserException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by martian on 2016/05/12.
 */
public class AggregationQuery {

    public ArrayList<DBObject> returnVectorValues(DBObject criteria, DBObject projection,
                                                  DBObject groupBy, DBObject order, Long limit, Long offset,
                                                  SelectClause selectStatement) {

        ArrayList<DBObject> vector_values = new ArrayList<>();

        if (criteria != null) {
            BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);
            vector_values.add(criteria_ppl);
        }

        if (groupBy != null) {
            vector_values.add(groupBy);

            projection = null;
        }

        if (order != null) {
            BasicDBObject orderBy = new BasicDBObject("$sort", order);
            vector_values.add(orderBy);
        }

        if (projection != null) {
            BasicDBObject projection_ppl = new BasicDBObject("$project", projection);
            vector_values.add(projection_ppl);
        }

        if (limit != null) {
            BasicDBObject limit_statement = new BasicDBObject("$limit", selectStatement.getLimit().getRowCount());
            vector_values.add(limit_statement);
        }
        if (offset != null) {
            BasicDBObject skip = new BasicDBObject("$skip", selectStatement.getLimit().getOffset());
            vector_values.add(skip);
        }
        //In the case of select * from actor ( create a query that matches all attributes )
        if (vector_values.isEmpty()) {
            criteria = (DBObject) JSON.parse("{_id: { $ne : 0 }}");
            BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);
            vector_values.add(criteria_ppl);
        }

        return vector_values;
    }

    public AggregationOutput returnAggResult(ArrayList<DBObject> vector_values, DBCollection collection) {

        switch (vector_values.size()) {
            case 0:
                return null;
            case 1:
                return collection.aggregate(vector_values.get(0));
            case 2:
                return collection.aggregate(vector_values.get(0), vector_values.get(1));
            case 3:
                return collection.aggregate(vector_values.get(0), vector_values.get(1), vector_values.get(2));
            case 4:
                return collection.aggregate(vector_values.get(0), vector_values.get(1), vector_values.get(2), vector_values.get(3));
            case 5:
                return collection.aggregate(vector_values.get(0), vector_values.get(1), vector_values.get(2), vector_values.get(3), vector_values.get(4));
            case 6:
                return collection.aggregate(vector_values.get(0), vector_values.get(1), vector_values.get(2), vector_values.get(3), vector_values.get(4), vector_values.get(5));
        }

        return null;
    }

    public DBObject returnAggOperators(SelectClause select) {

        DBObject groupFields = new BasicDBObject();
        String attribute = "";
        Functions functions = new Functions();

        // If there is only the MAX () function ( ex) without the attributes to be sought
        if (select.getGroupBy().isEmpty()) {
            groupFields.put("_id", "null");
        } else {
            DBObject tmpObject = new BasicDBObject();
            for (int i = 0; i < select.getGroupBy().size(); i++) {
                attribute = select.getGroupBy().get(i).getColumnName();
                tmpObject.put(attribute, "$" + attribute);
            }
            groupFields.put("_id", tmpObject);
        }

        for (int i = 0; i < select.getFunctions().size(); i++) {
            if (!select.getFunctions().get(i).getParameters().isEmpty()) {
                attribute = select.getFunctions().get(i).getParameters().get(0);
            }
            switch (select.getFunctions().get(i).getName().toUpperCase()) {
                case "COUNT":
                    if (select.getFunctions().get(i).isDistinct()) {
                        groupFields.put(attribute, new BasicDBObject("$sum", 1));
                    } else if (select.getFunctions().get(i).getAlias() != null) {
                        groupFields.put(select.getFunctions().get(i).getAlias(), new BasicDBObject("$sum", 1));
                    } else {
                        groupFields.put(select.getFunctions().get(i).getNameComplete(), new BasicDBObject("$sum", 1));
                    }
                    break;
                default:
                    if (select.getFunctions().get(i).getAlias() != null) {
                        groupFields.put(select.getFunctions().get(i).getAlias(),
                                new BasicDBObject("$" + select.getFunctions().get(i).getName().toLowerCase(),
                                        "$" + attribute));
                    } else {
                        groupFields.put(select.getFunctions().get(i).getNameComplete(),
                                new BasicDBObject("$" + select.getFunctions().get(i).getName().toLowerCase(), "$" + attribute));
                    }
                    break;
            }
        }
        DBObject group = new BasicDBObject("$group", groupFields);
        return group;
    }

    public static String returnResultSet(AggregationOutput select_result, String header) throws JSQLParserException {

        String result_set = "";

        String json_str = select_result.results().toString();
        int result_size = json_str.length();

        //result not empty
        if (!json_str.equals("[ ]")) {
            //Instantiates a new JSONObject passing the string as input
            JSONObject my_obj = new JSONObject(json_str.substring(1, result_size - 1));
            my_obj.remove("_id");

            JSONArray array = new JSONArray(json_str);
            JSONObject my_obj_result_set;
            JSONArray array_header = new JSONArray("[" + header + "]");
            for (int i = 0; i < array.length(); i++) {
                result_set += "{";


                my_obj_result_set = array.getJSONObject(i);
                my_obj_result_set.remove("_id");

                for (int j = 0; j < array_header.length(); j++) {
                    String actual_attribute = array_header.get(j).toString();
                    if (my_obj_result_set.has(actual_attribute)) {
                        result_set += "'" + my_obj_result_set.get(actual_attribute) + "', ";
                    } else {
                        result_set += "'' , ";
                    }
                }
                result_set += "},";
            }
        }
        return result_set;
    }

    public static String returnResultSetWithJoin(String select_result, String header) throws JSQLParserException {

        String result_set = "";

        String json_str = select_result;
        int result_size = json_str.length();

        //result not empty
        if (!json_str.equals("[ ]")) {

            //instantiates a new JSONObject passing the string as input
            JSONObject my_obj = new JSONObject(json_str.substring(1, result_size - 1));
            my_obj.remove("_id");


            JSONArray array = new JSONArray(json_str);
            JSONObject my_obj_result_set;

            for (int i = 0; i < array.length(); i++) {
                result_set += "{";

                //Instantiates a new JSONObject passing the string as input
                my_obj_result_set = array.getJSONObject(i);
                my_obj_result_set.remove("_id");

                JSONArray array_header = new JSONArray("[" + header + "]");

                for (int j = 0; j < array_header.length(); j++) {
                    String actual_attribute = array_header.get(j).toString();
                    if (my_obj_result_set.has(actual_attribute)) {
                        result_set += "'" + my_obj_result_set.get(actual_attribute) + "', ";
                    } else {
                        result_set += "'' , ";
                    }
                }
                result_set += "},";
            }
        }
        return result_set;
    }
    /*
        responsible method to return the correct header
     *   The header should be the highest possible , by the fact that documents
        May have different attributes *
     */

    public static String returnHeader(SelectClause selectStatement) {
        String header = "";

        // If function is only one table
        if (selectStatement.getFunctions().isEmpty()) {
            for (int i = 0; i < selectStatement.getTablesQueried().size(); i++) {
                if (selectStatement.getTablesQueried().get(i).isIsAllColumns()) {
                    AggregationQuery agg = new AggregationQuery();
                    String table = selectStatement.getTablesQueried().get(i).getName().toString();
                    String allColumnsHeader = agg.returnAllColumnsHeader(table);
                    header += allColumnsHeader + ",";

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
                    header += "'" + selectStatement.getFunctions().get(i).getAlias().toString() + "',";
                } else {
                    header += "'" + selectStatement.getFunctions().get(i).getNameComplete() + "',";
                }
            }
        }

        return header;
    }

    public String returnAllColumnsHeader(String table) {
        DBCollection collection = QueryInterceptor.database.getCollection("metadata");
        DBObject projection = (DBObject) JSON.parse("{columns:1, _id:0}");
        BasicDBObject projection_ppl = new BasicDBObject("$project", projection);
        DBObject criteria = (DBObject) JSON.parse("{table:'" + table + "'}");
        BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);

        AggregationOutput exit = collection.aggregate(criteria_ppl, projection_ppl);
        String json_str = exit.results().toString();
        String attributes = "";
        int tamanho_resultado = json_str.length();
        if (!json_str.toString().equals("[ ]")) {
            JSONObject my_obj = new JSONObject(json_str.substring(1, tamanho_resultado - 1));
            attributes = my_obj.get("columns").toString().substring(1, my_obj.get("columns").toString().length() - 1);
            attributes = attributes.replaceAll("[\"]", "'");
        }
        return attributes;
    }
}
