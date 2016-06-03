package mapping.selectClause;

import net.sf.jsqlparser.expression.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martian on 2016/06/03.
 */
public class TablesQueried {

    private String name;
    private String alias;
    private List<ProjectionParams> param_projection = new ArrayList<>();
    private List<Function> functions = new ArrayList<>();
    private List<String> aliasFunctions = new ArrayList<>();
    private boolean isAllColumns;

    public boolean isIsAllColumns() {
        return isAllColumns;
    }

    public void setIsAllColumns(boolean isAllColumns) {
        this.isAllColumns = isAllColumns;
    }

    public List<String> getAliasFunctions() {
        return aliasFunctions;
    }

    public void setAliasFunctions(List<String> aliasFunctions) {
        this.aliasFunctions = aliasFunctions;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    public void addFunctions(Function function){
        this.getFunctions().add(function);
    }

    public void addAliasFunctions(String aliasFunction){
        this.aliasFunctions.add(aliasFunction);
    }

    public void addParameters(ProjectionParams parameter){
        this.param_projection.add(parameter);
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

    public List<ProjectionParams> getParam_projection() {
        return param_projection;
    }

    public void setParam_projection(List<ProjectionParams> param_projection) {
        this.param_projection = param_projection;
    }
}
