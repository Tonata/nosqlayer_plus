package migration.sqlToNoSQLDAO;

import migration.model.Table;
import migration.sqltoNoSQLConverter.TableMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by martian on 2016/05/12.
 */
public class TableDAO extends EntityDAO<Table> {

    public void save(Table table, ResultSet tuple) throws SQLException {


        setCollection(table.getName());
        Map<String, Object> mapTable =  new TableMapper().converterToMap(table,tuple);
        save(mapTable, table);

        //System.out.println("Save > "+tuple);
    }

}
