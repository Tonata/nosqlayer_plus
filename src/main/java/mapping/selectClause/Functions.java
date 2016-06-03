package mapping.selectClause;

import java.util.ArrayList;

/**
 * Created by martian on 2016/06/03.
 */
public class Functions {

    public String name;
    public String alias;
    public boolean allColumns;
    public boolean distinct;
    public String nameComplete;
    public ArrayList<String> parameters = new ArrayList<>();

    public String getNameComplete() {
        return nameComplete;
    }

    public void setNameComplete(String nameComplete) {
        this.nameComplete = nameComplete;
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public void addParameters(String parameters){
        this.parameters.add(parameters);
    }

    public void setParameters(ArrayList<String> parameters) {
        this.parameters = parameters;
    }

    public boolean isAllColumns() {
        return allColumns;
    }

    public void setAllColumns(boolean allColumns) {
        this.allColumns = allColumns;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}

