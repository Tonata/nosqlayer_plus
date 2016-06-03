package mapping.selectClause;

/**
 * Created by martian on 2016/06/03.
 */
public class JoinClause {

    private String joinType;
    private String tableAliasLeftExpression;
    private String tableLeftExpression;
    private ProjectionParams leftExpression;
    private String tableAliasRightExpression;
    private String tableRightExpression;
    private ProjectionParams rightExpression;
    private String operator;

    public String getTableLeftExpression() {
        return tableLeftExpression;
    }

    public void setTableLeftExpression(String tableLeftExpression) {
        this.tableLeftExpression = tableLeftExpression;
    }

    public String getTableRightExpression() {
        return tableRightExpression;
    }

    public void setTableRightExpression(String tableRightExpression) {
        this.tableRightExpression = tableRightExpression;
    }

    public ProjectionParams getLeftExpression() {
        return leftExpression;
    }

    public String getTableAliasLeftExpression() {
        return tableAliasLeftExpression;
    }

    public void setTableAliasLeftExpression(String tableAliasLeftExpression) {
        this.tableAliasLeftExpression = tableAliasLeftExpression;
    }

    public String getTableAliasRightExpression() {
        return tableAliasRightExpression;
    }

    public void setTableAliasRightExpression(String tableAliasRightExpression) {
        this.tableAliasRightExpression = tableAliasRightExpression;
    }

    public void setLeftExpression(ProjectionParams leftExpression) {
        this.leftExpression = leftExpression;
    }

    public ProjectionParams getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(ProjectionParams rightExpression) {
        this.rightExpression = rightExpression;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }
}
