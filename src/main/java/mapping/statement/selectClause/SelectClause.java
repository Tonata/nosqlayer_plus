package mapping.statement.selectClause;

import mapping.mediator.criteria.CriteriaIdentifier;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Limit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martian on 2016/06/03.
 */
public class SelectClause {

    private Limit limit;
    private List<TablesQueried> tablesQueried = new ArrayList<>();
    private List<Sort> order = new ArrayList<>();
    private List<Functions> functions = new ArrayList<>();
    private CriteriaIdentifier criteriaIdentifier = new CriteriaIdentifier();
    private List<Column> groupBy = new ArrayList<>();
    private List<JoinClause> joinList = new ArrayList<>();
    private boolean hasJoin;

    public boolean isHasJoin() {
        return hasJoin;
    }

    public void setHasJoin(boolean hasJoin) {
        this.hasJoin = hasJoin;
    }

    public List<JoinClause> getJoinList() {
        return joinList;
    }

    public void setJoinList(List<JoinClause> joinList) {
        this.joinList = joinList;
    }

    public List<TablesQueried> getTablesQueried() {
        return tablesQueried;
    }

    public void setTablesQueried(List<TablesQueried> tablesQueried) {
        this.tablesQueried = tablesQueried;
    }

    public List<Column> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<Column> groupBy) {
        this.groupBy = groupBy;
    }

    public CriteriaIdentifier getCriteriaIdentifier() {
        return criteriaIdentifier;
    }

    public void setCriteriaIdentifier(CriteriaIdentifier criteriaIdentifier) {
        this.criteriaIdentifier = criteriaIdentifier;
    }

    public List<Functions> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Functions> functions) {
        this.functions = functions;
    }

    public void addFunction(Functions funcao){
        this.getFunctions().add(funcao);
    }

    public void addColumnGroupBy(Column coluna){
        this.groupBy.add(coluna);
    }

    public void addJoin(JoinClause join){
        this.joinList.add(join);
    }

    public void  addTablesQueried(TablesQueried table){
        this.tablesQueried.add(table);
    }

    public List<Sort> getOrder() {
        return order;
    }

    public void setOrder(List<Sort> order) {
        this.order = order;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public void addOrder(Sort sort){
        this.order.add(sort);
    }

    public String convertAliasToName(String alias){

        for(int i=0; i<this.tablesQueried.size();i++){
            if(tablesQueried.get(i).getAlias() != null){
                if(alias.equals(tablesQueried.get(i).getAlias())){
                    return tablesQueried.get(i).getName();
                }
            }
        }
        return null;
    }

    public String convertNameToAlias(String name){
        for(int i=0; i<this.tablesQueried.size();i++){
            if(name.equals(tablesQueried.get(i).getName())){
                return tablesQueried.get(i).getAlias();
            }
        }
        return null;
    }

    public List<ProjectionParams> returnAttributesByAlias(String alias){

        for(int i=0; i<tablesQueried.size();i++){
            if(tablesQueried.get(i).getAlias()!= null &&
                    tablesQueried.get(i).getAlias().equals(alias) ){
                if(tablesQueried.get(i).isIsAllColumns()){
                    ProjectionParams temp_projection = new ProjectionParams();
                    List<ProjectionParams> list_tmp = new ArrayList<>();
                    temp_projection.setName("*");
                    list_tmp.add(temp_projection);

                    return list_tmp;
                }else{
                    return tablesQueried.get(i).getParam_projection();
                }
            }
        }

        return null;
    }

    public List<ProjectionParams> returnAttributesByName(String tableNome){

        for(int i=0; i<this.tablesQueried.size();i++){
            if(tableNome.equals(tablesQueried.get(i).getName())){
                if(tablesQueried.get(i).isIsAllColumns()){
                    ProjectionParams projecao_tmp = new ProjectionParams();
                    List<ProjectionParams> list_tmp = new ArrayList<>();
                    projecao_tmp.setName("*");
                    list_tmp.add(projecao_tmp);

                    return list_tmp;
                }else{
                    return tablesQueried.get(i).getParam_projection();
                }
            }
        }

        return null;
    }
}
