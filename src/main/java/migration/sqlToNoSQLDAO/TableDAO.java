package migration.sqlToNoSQLDAO;

import migration.model.Table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by martian on 2016/05/12.
 */
public class TableDAO extends EntityDAO<Table> {

    public void save(Table table, ResultSet tuple) throws SQLException {


        setCollection(table.getName());
//        Map<String, Object> mapTable =  new TableMapper().converterToMap(table,tuple);
        Map<String, Object> mapTable = new HashMap<>();

        ResultSetMetaData mD = tuple.getMetaData();
        int columns = mD.getColumnCount();

        while (tuple.next()){
//            System.out.print(": " + tuple.getObject(1).toString());
            for (int i = 1; i <= columns; i++){
                mapTable.put(mD.getColumnName(i), tuple.getObject(i));
//                System.out.println("Column Name: " + mD.getColumnName(i));
//                System.out.println("Tuple Obj: " + tuple.getObject(i));

            }
            save(mapTable, table);

        }



//        System.out.println("Save > "+tuple);
    }

}
