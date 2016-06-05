package migration.sqlToNoSQLDAO;

import migration.model.Column;
import migration.model.Table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by martian on 2016/05/12.
 */
public class TableMapper {

    public Map<String, Object> converterToMap(Table table, ResultSet tuple) throws SQLException {
        Map<String, Object> tableMap = new HashMap<String,Object>();

        for(Column column: table.getColumns()){

            switch (column.getColumnType().toUpperCase()) {
                case "TINYINT":

                case "TINYINT UNSIGNED":

                case "SMALLINT":

                case "SMALLINT UNSIGNED":

                case "MEDIUMINT":

                case "MEDIUMINT UNSIGNED":

                case "INT":

                case "INT UNSIGNED":

                case "BIGINT":

                case "BIGINT UNSIGNED":
                    tableMap.put(column.getName(), tuple.getLong(column.getName()));
                    break;

                case "DECIMAL":

                case "FLOAT":

                case "DOUBLE":

                case "REAL":
                    tableMap.put(column.getName(), tuple.getFloat(column.getName()));
                    break;

                case "BIT":

                case "SERIAL":
                    tableMap.put(column.getName(), tuple.getInt(column.getName()));
                    break;

                case "BOOLEAN":
                    tableMap.put(column.getName(), tuple.getBoolean(column.getName()));
                    break;

                case "DATE":
                    tableMap.put(column.getName(), tuple.getString(column.getName()));
                    break;

                default:
                    tableMap.put(column.getName(), tuple.getString(column.getName()));
                    break;
            }
        }
        return tableMap;
    }
}
