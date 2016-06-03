package migration.model;

import java.util.ArrayList;
import java.util.Collection;

public class Table {

    private String name;
    private Collection<Column> columns = new ArrayList<Column>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Column> getColumns() {
        return columns;
    }

    public void addColumns(Column column) {
        this.columns.add(column);
    }
    
}
