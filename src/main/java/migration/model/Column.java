package migration.model;

public class Column {

    private String name;
    private String columnType;
    private boolean primaryKey;
    private boolean isUnique = false;
    private boolean isForeignKey = false;
    private String tableForeignKeyReference;
    private String columnForeignKeyReference;

    public String getColumnForeignKeyReference() {
        return columnForeignKeyReference;
    }

    public void setColumnForeignKeyReference(String columnForeignKeyReference) {
        this.columnForeignKeyReference = columnForeignKeyReference;
    }

    public boolean isIsForeignKey() {
        return isForeignKey;
    }

    public void setIsForeignKey(boolean isForeignKey) {
        this.isForeignKey = isForeignKey;
    }

    public String getTableForeignKeyReference() {
        return tableForeignKeyReference;
    }

    public void setTableForeignKeyReference(String tableForeignKeyReference) {
        this.tableForeignKeyReference = tableForeignKeyReference;
    }

    public boolean isIsUnique() {
        return isUnique;
    }

    public void setIsUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
}
