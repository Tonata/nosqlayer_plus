package mapping;

import migration.model.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martian on 2016/06/03.
 */
public class InsertClause {

    private Table table;
    private List<Column> columns = new ArrayList<>();
    private ExpressionList values;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public ExpressionList getValues() {
        return values;
    }

    public void setValues(ExpressionList values) {
        this.values = values;
    }
}

