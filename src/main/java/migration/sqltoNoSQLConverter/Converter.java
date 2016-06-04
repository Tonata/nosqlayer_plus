package migration.sqltoNoSQLConverter;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import connectionConfig.MongoConnection;
import connectionConfig.MySQLConnection;
import migration.model.Column;
import migration.model.Database;
import migration.model.Table;
import migration.sqlToNoSQLDAO.EntityDAO;
import migration.sqlToNoSQLDAO.TableDAO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by martian on 2016/05/12.
 */
public class Converter {

    MySQLConnection mySQLConnection =  new MySQLConnection();
    DBCollection dbCollection;
//    Database dataBase = new Database();


    public Converter() {
    }

    public Database mountObjects(Connection conn, Database dataBase ){

//        Connection connection = null;
//
//        connection = mySQLConnection.getConnection();

        dataBase.setName(mySQLConnection.getDATABASE());
        dataBase.setHost(mySQLConnection.getHOST());
        dataBase.setPort(mySQLConnection.getPORT());

        try{

            DatabaseMetaData DB_MD = conn.getMetaData();

            ResultSet result_tables = DB_MD.getTables(null, null, null, null);
//            System.out.println("");
            while (result_tables.next()){

                Table table = new Table();
                table.setName(result_tables.getString("TABLE_NAME"));
                ResultSet result_columns = DB_MD.getColumns(null, null, table.getName(), null);

                ResultSet foreignKeys = DB_MD.getImportedKeys(null, null, result_tables.getString("TABLE_NAME"));

                ArrayList<String> columnsContainsForeignKey = new ArrayList<>();
                ArrayList<String> columnsReferenced = new ArrayList<>();
                ArrayList<String> tablesReferenced = new ArrayList<>();

                while (foreignKeys.next()) {
                    String tableForeignKey = foreignKeys.getString("FKTABLE_NAME");
                    String columnForeignKey = foreignKeys.getString("FKCOLUMN_NAME");
                    String tableReferencedForeignKey = foreignKeys.getString("PKTABLE_NAME");
                    String columnReferencedForeignKey = foreignKeys.getString("PKCOLUMN_NAME");

                    columnsContainsForeignKey.add(columnForeignKey);
                    columnsReferenced.add(columnReferencedForeignKey);
                    tablesReferenced.add(tableReferencedForeignKey);
                    /*System.out.println(tabelaChaveEstrangeira+" - "+colunaChaveEstrangeira+
                     " - "+tabelaReferenciadaChaveEstrangeira+" - "+
                     colunaReferenciadaChaveEstrangeira);*/
                }

                while (result_columns.next()) {
                    Column column = new Column();
                    column.setName(result_columns.getString("COLUMN_NAME"));
                    column.setColumnType(result_columns.getString("TYPE_NAME"));
                    if (result_columns.getString("IS_AUTOINCREMENT").equals("YES")) {
                        column.setPrimaryKey(true);
                    }
                    ResultSet index_information = DB_MD.getIndexInfo(null, null, table.getName(), true, true);

                    while (index_information.next()) {

                        String index_column = index_information.getString("COLUMN_NAME");
                        if (index_column.equals(result_columns.getString("COLUMN_NAME"))) {
                            column.setIsUnique(true);
                        }
                    }

                    for (int i = 0; i < columnsContainsForeignKey.size(); i++) {
                        if (column.getName().equals(columnsContainsForeignKey.get(i))) {
                            column.setIsForeignKey(true);
                            column.setColumnForeignKeyReference(columnsReferenced.get(i));
                            column.setTableForeignKeyReference(tablesReferenced.get(i));
                        }
                    }

                    table.addColumns(column);
                }
                dataBase.addTable(table);
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }

        System.out.println("Mounted Objects");
        return dataBase;
    }

    public void createMetaData(Database dataBase, Connection conn)  /* 2nd parameter needed for generic NoSQL collection */{

        dbCollection = MongoConnection.getInstance().getDB().getCollection("metadata");

        try{

            for (Table table : dataBase.getTables()) {
                // System.out.println("Tabela: " + table.getName());
                BasicDBObject table_metadata = new BasicDBObject("table", table.getName());
                ArrayList<String> columns = new ArrayList<>();
                for (Column column : table.getColumns()) {
                    if (column.isPrimaryKey()) {
                        table_metadata.append("auto_inc", column.getName());
                    }
                    // System.out.println(coluna.getName());
                    columns.add(column.getName());
                    //System.out.println("   UNICO? "+coluna.isIsUnique());
                }
                table_metadata.append("columns", columns);
                dbCollection.save(table_metadata);
            }

            System.out.println("metadata created");
//            Connection connection = mySQLConnection.getConnection();

            for (Table table : dataBase.getTables()){

                int value_ = 1;
                int subParts = 10;
                long start = 0;

                long total = 20198310;
                long sub_total = total/subParts;

                for (int i = 0; i < subParts; i++) {
                    String sql2 = "SELECT * FROM " + table.getName() + " LIMIT "+start+","+sub_total;
                    try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                        ResultSet resultSet2 = stmt2.executeQuery();
                        while (resultSet2.next()) {

                            new TableDAO().save(table, resultSet2);
                            System.out.println(table.getName() + " - " + value_);
                            value_++;
                        }
                    }
                    start = start+sub_total;
                }

                DBObject criteria = (DBObject) JSON.parse("{table:'" + table.getName() + "'}");
                DBObject projection = (DBObject) JSON.parse("{auto_inc:1}");
                BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);
                BasicDBObject projection_ppl = new BasicDBObject("$project", projection);
                AggregationOutput busca_auto_inc = dbCollection.aggregate(criteria_ppl, projection_ppl);
                String json_str = busca_auto_inc.results().toString();

                JSONArray array = new JSONArray(json_str);
                JSONObject result_set;
                String value_auto_inc = null;

                for (int i = 0; i < array.length(); i++) {
                    result_set = array.getJSONObject(i);
                    result_set.remove("_id");
                    if (result_set.has("auto_inc")) {
                        value_auto_inc = result_set.get("auto_inc").toString();
                    }
                }

                String find_max_auto_inc = "SELECT MAX(" + value_auto_inc + ") AS maximo_id FROM " + table.getName() + " ";

                int maximo_id = 0;
                try (PreparedStatement stmt = conn.prepareStatement(find_max_auto_inc)) {
                    ResultSet result= stmt.executeQuery();

                    while (result.next()) {
                        maximo_id = result.getInt("maximo_id");
                    }
                }

                String sequence_field = "seq"; // the name of the field which holds the sequence
                DBCollection seq = MongoConnection.getInstance().getDB().getCollection("seq"); // get the collection (this will create it if needed)
                DBObject new_seq = (DBObject) JSON.parse("{'_id':'" + table.getName() + "', 'seq':" + maximo_id + "}");
                seq.insert(new_seq);

                new EntityDAO<>().ensureIndex(table);

            }

        }catch (SQLException ex){
            ex.printStackTrace();
        }


    }
}
