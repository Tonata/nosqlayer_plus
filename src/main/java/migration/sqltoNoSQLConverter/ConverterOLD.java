package migration.sqltoNoSQLConverter;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import connectionConfig.MongoConnection;
import migration.model.*;
import migration.sqlToNoSQLDAO.EntityDAO;
import migration.sqlToNoSQLDAO.TableDAO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

import java.sql.ResultSet;

/**
 * Created by martian on 2016/05/12.
 */
public class ConverterOLD {

    static final String DATABASE = "w3c";
    static final String HOST = "localhost";
    static final int PORT = 3306;
    static final String USER = "root";
    static final String PASS = "root";
    public static DBCollection dbCollection;
    private static final String DBURL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?user=" + USER + "&password=" + PASS + "&useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
    private static final String DBDRIVER = "org.gjt.mm.mysql.Driver";

    static {
        try {
            Class.forName(DBDRIVER).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        }
    }

    private static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DBURL);
        } catch (Exception e) {
        }
        return connection;
    }

    public static void main(String[] args) throws SQLException {
        Connection con = getConnection();
        Database dataBase = new Database();
        dataBase.setName(DATABASE);
        dataBase.setHost(HOST);
        dataBase.setPort(PORT);


        try {
            DatabaseMetaData dbmd = con.getMetaData();

            ResultSet result_tables = dbmd.getTables(null, null, null, null);
            System.out.println("");
            while (result_tables.next()) {
                Table tabela = new Table();
                tabela.setName(result_tables.getString("TABLE_NAME"));
                ResultSet result_columns = dbmd.getColumns(null, null, tabela.getName(), null);

                ResultSet foreignKeys = dbmd.getImportedKeys(null, null, result_tables.getString("TABLE_NAME"));
                ArrayList<String> colunasContainsForeignKey = new ArrayList<String>();
                ArrayList<String> colunasReferenciadas = new ArrayList<String>();
                ArrayList<String> tabelasReferenciadas = new ArrayList<String>();
                while (foreignKeys.next()) {
                    String tabelaChaveEstrangeira = foreignKeys.getString("FKTABLE_NAME");
                    String colunaChaveEstrangeira = foreignKeys.getString("FKCOLUMN_NAME");
                    String tabelaReferenciadaChaveEstrangeira = foreignKeys.getString("PKTABLE_NAME");
                    String colunaReferenciadaChaveEstrangeira = foreignKeys.getString("PKCOLUMN_NAME");
                    colunasContainsForeignKey.add(colunaChaveEstrangeira);
                    colunasReferenciadas.add(colunaReferenciadaChaveEstrangeira);
                    tabelasReferenciadas.add(tabelaReferenciadaChaveEstrangeira);
                    /*System.out.println(tabelaChaveEstrangeira+" - "+colunaChaveEstrangeira+
                     " - "+tabelaReferenciadaChaveEstrangeira+" - "+
                     colunaReferenciadaChaveEstrangeira);*/
                }

                while (result_columns.next()) {
                    Column coluna = new Column();
                    coluna.setName(result_columns.getString("COLUMN_NAME"));
                    coluna.setColumnType(result_columns.getString("TYPE_NAME"));
                    if (result_columns.getString("IS_AUTOINCREMENT").equals("YES")) {
                        coluna.setPrimaryKey(true);
                    }
                    ResultSet index_information = dbmd.getIndexInfo(null, null, tabela.getName(), true, true);
                    /*Percorre a lista de índices da tabela*/
                    while (index_information.next()) {

                        String index_column = index_information.getString("COLUMN_NAME");
                        if (index_column.equals(result_columns.getString("COLUMN_NAME"))) {
                            coluna.setIsUnique(true);
                        }
                    }

                    for (int i = 0; i < colunasContainsForeignKey.size(); i++) {
                        if (coluna.getName().equals(colunasContainsForeignKey.get(i))) {
                            coluna.setIsForeignKey(true);
                            coluna.setColumnForeignKeyReference(colunasReferenciadas.get(i));
                            coluna.setTableForeignKeyReference(tabelasReferenciadas.get(i));
                        }
                    }

                    tabela.addColumns(coluna);
                }
                dataBase.addTable(tabela);
            }
        } catch (SQLException e) {
            e.getSQLState();
        }
        System.out.println("Mounted Objects");
        /*
         System.out.println("Verificando as chaves estrangeiras");
         for (Table tabela : dataBase.getTables()) {
         System.out.println("TABELA: " +tabela.getName());
         for(Column coluna : tabela.getColumns()){
         System.out.println("Coluna: "+coluna.getName());
         if(coluna.isIsForeignKey()){
         System.out.println("FOREIGN KEY");
         System.out.println("Tabela referenciada: "+coluna.getTableForeignKeyReference());
         System.out.println("Coluna referenciada: "+coluna.getColumnForeignKeyReference());
         }
         }
         }
         */
        System.out.println("Creating MetaData");
        dbCollection = MongoConnection.getInstance().getDB().getCollection("metadata");
        // System.out.println("Informações sobre as tabelas");
        for (Table tabela : dataBase.getTables()) {
            // System.out.println("Tabela: " + tabela.getName());
            BasicDBObject table_metadata = new BasicDBObject("table", tabela.getName());
            ArrayList<String> columns = new ArrayList<String>();
            for (Column coluna : tabela.getColumns()) {
                if (coluna.isPrimaryKey()) {
                    table_metadata.append("auto_inc", coluna.getName());
                }
                // System.out.println(coluna.getName());
                columns.add(coluna.getName());
                //System.out.println("   UNICO? "+coluna.isIsUnique());
            }
            table_metadata.append("columns", columns);
            dbCollection.save(table_metadata);
        }
        System.out.println("metadata created");
        Connection connection = getConnection();
        //Realizar consultas nas tabelas buscando os dados

        for (Table tabela : dataBase.getTables()) {
            int valor_salvo = 1;
            int subPartes = 10;
            long inicio = 0;

            long total = 20198310;
            long sub_total = total/subPartes;

            for (int i = 0; i < subPartes; i++) {
                String sql2 = "SELECT * FROM " + tabela.getName() + " LIMIT "+inicio+","+sub_total;
                try (PreparedStatement stmt2 = connection.prepareStatement(sql2)) {
                    ResultSet resultado2 = stmt2.executeQuery();
                    while (resultado2.next()) {

                        new TableDAO().save(tabela, resultado2);
                        System.out.println(tabela.getName() + " - " + valor_salvo);
                        valor_salvo++;
                    }
                }
                inicio = inicio+sub_total;
            }

            /*
             int cont = 0;
             while (resultado.next()) {
             if (tabela.getName().equals("user")) {
             System.out.println(cont);
             cont++;
             }
             new TabelaDao().save(tabela, resultado);
             }*/
            /*
             int total_tmp = 154167620 / 100;
             int inicio = 5792694;
             int cont = 0;
             while (cont < 100) {
             String sql2 = "SELECT * FROM " + tabela.getName() + " LIMIT " + inicio + "," + total_tmp;
             try (PreparedStatement stmt2 = connection.prepareStatement(sql2)) {
             ResultSet resultado2 = stmt2.executeQuery();
             while (resultado2.next()) {

             new TabelaDao().save(tabela, resultado2);
             System.out.println(valor_salvo);
             valor_salvo++;
             }
             }
             cont++;
             inicio = cont*total_tmp;
             }
             */

            DBObject criteria = (DBObject) JSON.parse("{table:'" + tabela.getName() + "'}");
            DBObject projection = (DBObject) JSON.parse("{auto_inc:1}");
            BasicDBObject criteria_ppl = new BasicDBObject("$match", criteria);
            BasicDBObject projection_ppl = new BasicDBObject("$project", projection);
            AggregationOutput busca_auto_inc = dbCollection.aggregate(criteria_ppl, projection_ppl);
            String json_str = busca_auto_inc.results().toString();

            JSONArray array = new JSONArray(json_str);
            JSONObject result_set;
            String valor_auto_inc = null;
            for (int i = 0; i < array.length(); i++) {
                result_set = array.getJSONObject(i);
                result_set.remove("_id");
                if (result_set.has("auto_inc")) {
                    valor_auto_inc = result_set.get("auto_inc").toString();
                }
            }

            String find_max_auto_inc = "SELECT MAX(" + valor_auto_inc + ") AS maximo_id FROM " + tabela.getName() + " ";

            int maximo_id = 0;
            try (PreparedStatement stmt = connection.prepareStatement(find_max_auto_inc)) {
                ResultSet resultado = stmt.executeQuery();

                while (resultado.next()) {
                    maximo_id = resultado.getInt("maximo_id");
                }
            }
            String sequence_field = "seq"; // the name of the field which holds the sequence
            DBCollection seq = MongoConnection.getInstance().getDB().getCollection("seq"); // get the collection (this will create it if needed)
            DBObject new_seq = (DBObject) JSON.parse("{'_id':'" + tabela.getName() + "', 'seq':" + maximo_id + "}");
            seq.insert(new_seq);

            new EntityDAO<>().ensureIndex(tabela);
        }
    }
}
