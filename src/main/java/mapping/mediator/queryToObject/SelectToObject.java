package mapping.mediator.queryToObject;

import mapping.mediator.criteria.CriteriaIdentifier;
import mapping.statement.selectClause.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.io.StringReader;
import java.util.List;

/**
 * Created by martian on 2016/06/03.
 */
public class SelectToObject {

    public SelectClause selectClause = new SelectClause();

    public SelectToObject(String sql) throws JSQLParserException {
        CCJSqlParserManager pm = new CCJSqlParserManager();
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Select selectStatement = (Select) statement;
        PlainSelect plain = (PlainSelect) selectStatement.getSelectBody();
        TablesQueried table_queried = new TablesQueried();

        //Considerando o from com apenas uma table por enquanto
        FromItem fromItem = plain.getFromItem();
        if (fromItem.getAlias() != null) {
            table_queried.setName(fromItem.toString().split(" ")[0]);
            table_queried.setAlias(fromItem.getAlias());
        } else {
            table_queried.setName(fromItem.toString());
        }

        addParameterProjection(plain, table_queried);
        selectClause.addTablesQueried(table_queried);
        //Se houver cláusulas joins
        if (plain.getJoins() != null && plain.getJoins().size() > 0) {
            selectClause.setHasJoin(true);
            addOrderBy(plain);
            executeJoin(plain);
            //Se não houver joins
        } else {
            selectClause.setHasJoin(false);
            addWhere(plain, selectStatement);
            addOrderBy(plain);
            addGroupBy(plain);
            addLimit(plain);
        }

    }

    public void addParameterProjection(PlainSelect plain, TablesQueried table_queried) {
        List selectItems = plain.getSelectItems();

        for (int i = 0; i < selectItems.size(); i++) {
            ProjectionParams parameter = new ProjectionParams();
            //Faz a verificação se o atributo pertence a tabela em casos de a.actor
            if (table_queried.getAlias() != null) {
                String alias_tabela = table_queried.getAlias() + ".";
                //Se o atributo for da tabela em questão
                if (selectItems.get(i).toString().contains(alias_tabela)) {
                    if (selectItems.get(i).toString().replaceFirst(alias_tabela, "").equals("*")) {
                        table_queried.setIsAllColumns(true);
                    } else {
                        //Verifica se algum item buscado é uma função
                        if (((SelectExpressionItem) selectItems.get(i)).getExpression() instanceof Function) {
                            Function function = (Function) ((SelectExpressionItem) selectItems.get(i)).getExpression();
                            Functions functions = new Functions();
                            functions.setName(function.getName());
                            functions.setNameComplete(((SelectExpressionItem) selectItems.get(i)).getExpression().toString());

                            if (function.getParameters() != null) {
                                for (int j = 0; j < function.getParameters().getExpressions().size(); j++) {
                                    functions.addParameters(function.getParameters().getExpressions().get(j).toString());
                                }
                            }

                            if (((SelectExpressionItem) selectItems.get(i)).getAlias() != null) {
                                String alias = ((SelectExpressionItem) selectItems.get(i)).getAlias();
                                functions.setAlias(alias);
                            }
                            selectClause.addFunction(functions);
                        } else {  // Se nao for, por enquanto consideramos que é uma coluna
                            if (((SelectExpressionItem) selectItems.get(i)).getAlias() != null) {
                                String alias = ((SelectExpressionItem) selectItems.get(i)).getAlias();
                                parameter.setName(selectItems.get(i).toString().split(" ")[0].toString().replaceFirst(alias_tabela, ""));
                                parameter.setAlias(alias);
                                table_queried.addParameters(parameter);
                            } else {
                                parameter.setName(selectItems.get(i).toString().replaceFirst(alias_tabela, ""));
                                table_queried.addParameters(parameter);
                            }

                        }
                    }
                }
                //Se a tabela não possui alias, consideramos que seja somente uma tabela
            } else {
                if (selectItems.get(i).toString().equals("*")) {
                    table_queried.setIsAllColumns(true);
                } else {
                    //Verifica se algum item buscado é uma função
                    if (((SelectExpressionItem) selectItems.get(i)).getExpression() instanceof Function) {
                        String alias;
                        Function function = (Function) ((SelectExpressionItem) selectItems.get(i)).getExpression();
                        Functions functions = new Functions();
                        functions.setName(function.getName());
                        functions.setNameComplete(((SelectExpressionItem) selectItems.get(i)).getExpression().toString());

                        if (function.getParameters() != null) {
                            for (int j = 0; j < function.getParameters().getExpressions().size(); j++) {
                                functions.addParameters(function.getParameters().getExpressions().get(j).toString());
                            }
                        }

                        if (((SelectExpressionItem) selectItems.get(i)).getAlias() != null) {
                            alias = ((SelectExpressionItem) selectItems.get(i)).getAlias();
                            functions.setAlias(alias);
                        }
                        selectClause.addFunction(functions);
                    } else {
                        //Em casos de Customer.customer
                        String table = ((Column) ((SelectExpressionItem) selectItems.get(i)).getExpression()).getTable().getName();

                        if (table != null) {
                            if (table_queried.getName().equals(table)) {
                                if (((SelectExpressionItem) selectItems.get(i)).getAlias() != null) {
                                    String alias = ((SelectExpressionItem) selectItems.get(i)).getAlias();
                                    parameter.setAlias(alias);
                                }
                                parameter.setName(((Column) ((SelectExpressionItem) selectItems.get(i)).getExpression()).getColumnName().split(" ")[0].toString());
                                table_queried.addParameters(parameter);
                            }

                        } else {
                            // Se nao for, por enquanto consideramos que é uma coluna
                            if (((SelectExpressionItem) selectItems.get(i)).getAlias() != null) {
                                String alias = ((SelectExpressionItem) selectItems.get(i)).getAlias();
                                parameter.setAlias(alias);
                            }
                            parameter.setName(((Column) ((SelectExpressionItem) selectItems.get(i)).getExpression()).getColumnName().split(" ")[0].toString());
                            table_queried.addParameters(parameter);
                        }

                    }
                }
            }
        }
    }

    public void addParameterProjectionJoin(ProjectionParams param1, String alias_table1,
                                           ProjectionParams param2, String alias_table2, String name_table1,
                                           String name_table2) {

        for (int i = 0; i < selectClause.getTablesQueried().size(); i++) {

            if (alias_table1 != null) {
                if (selectClause.getTablesQueried().get(i).getAlias() != null) {
                    if (selectClause.getTablesQueried().get(i).getAlias().equals(alias_table1)) {
                        TablesQueried table_queried = selectClause.getTablesQueried().get(i);
                        table_queried.addParameters(param1);
                    }
                }
            }
            if (alias_table2 != null) {
                if (selectClause.getTablesQueried().get(i).getAlias() != null) {
                    if (selectClause.getTablesQueried().get(i).getAlias().equals(alias_table2)) {
                        TablesQueried table_queried = selectClause.getTablesQueried().get(i);
                        table_queried.addParameters(param2);
                    }
                }
            }

            if (name_table1 != null) {
                if (selectClause.getTablesQueried().get(i).getName().equals(name_table1)) {
                    TablesQueried table_queried = selectClause.getTablesQueried().get(i);
                    table_queried.addParameters(param1);
                }
            }
            if (name_table2 != null) {
                if (selectClause.getTablesQueried().get(i).getName().equals(name_table2)) {
                    TablesQueried table_queried = selectClause.getTablesQueried().get(i);
                    table_queried.addParameters(param2);
                }
            }
        }
    }

    public void executeJoin(PlainSelect plain) {
        JoinClause join = new JoinClause();

        //Considerando apenas 1 join
        if (((Join) plain.getJoins().get(0)).isInner()) {
            join.setJoinType("INNER");
        } else if (((Join) plain.getJoins().get(0)).isFull()) {
            join.setJoinType("FULL");
        } else if (((Join) plain.getJoins().get(0)).isLeft()) {
            join.setJoinType("LEFT");
        } else if (((Join) plain.getJoins().get(0)).isNatural()) {
            join.setJoinType("NATURAL");
        } else if (((Join) plain.getJoins().get(0)).isOuter()) {
            join.setJoinType("OUTER");
        } else if (((Join) plain.getJoins().get(0)).isRight()) {
            join.setJoinType("RIGHT");
        } else if (((Join) plain.getJoins().get(0)).isSimple()) {
            join.setJoinType("SIMPLE");
            //Quando é Simple Join, é da forma FROM customer a, client c
            //Nesse caso so possui o from (customer a) e o rightexpression (client c)
        }

        //Deve-se verificar se possui alias ou name (ex: a.name ou Aluno.Nome)
        String left_expression_alias = ((Column) ((EqualsTo) ((Join) plain.getJoins().get(0)).getOnExpression()).getLeftExpression()).getTable().getAlias();
        String left_expression_nome = ((Column) ((EqualsTo) ((Join) plain.getJoins().get(0)).getOnExpression()).getLeftExpression()).getTable().getName();

        String right_expression_alias = ((Column) ((EqualsTo) ((Join) plain.getJoins().get(0)).getOnExpression()).getRightExpression()).getTable().getAlias();
        String right_expression_nome = ((Column) ((EqualsTo) ((Join) plain.getJoins().get(0)).getOnExpression()).getRightExpression()).getTable().getName();


        String left_expression[] = ((Column) ((EqualsTo) ((Join) plain.getJoins().get(0)).getOnExpression()).getLeftExpression()).getWholeColumnName().split("\\.");
        String right_expression[] = ((Column) ((EqualsTo) ((Join) plain.getJoins().get(0)).getOnExpression()).getRightExpression()).getWholeColumnName().split("\\.");

        ProjectionParams param1 = new ProjectionParams();
        param1.setName(left_expression[1]);
        ProjectionParams param2 = new ProjectionParams();
        param2.setName(right_expression[1]);

        join.setTableAliasLeftExpression(left_expression_alias);
        join.setTableAliasRightExpression(right_expression_alias);
        join.setTableLeftExpression(left_expression_nome);
        join.setTableRightExpression(right_expression_nome);

        join.setLeftExpression(param1);
        join.setRightExpression(param2);

        join.setOperator(":");
        selectClause.addJoin(join);

        TablesQueried table_join = new TablesQueried();
        //Adiciona a parte depois do INNER na tablequeried
        table_join.setName(((Join) plain.getJoins().get(0)).getRightItem().toString().split(" ")[0]);
        table_join.setAlias(((Join) plain.getJoins().get(0)).getRightItem().getAlias());
        addParameterProjection(plain, table_join);
        selectClause.addTablesQueried(table_join);
        //Add os parameters de junção à lista de params da tabela
        addParameterProjectionJoin(param1, left_expression_alias, param2, right_expression_alias, left_expression_nome, right_expression_nome);

        /*
         System.out.println("Informações sobre dados buscados");
         for(int i=0;i<selectClause.getTablesQueried().size();i++){
         System.out.println("Tabela:"+i+" "+
         selectClause.getTablesQueried().get(i).getName()+ " Alias "+
         selectClause.getTablesQueried().get(i).getAlias());
         System.out.print("Parâmetros: ");
         if(selectClause.getTablesQueried().get(i).isIsAllColumns()){
         System.out.println("Todas as colunas");
         }else{
         for(int j=0; j< selectClause.getTablesQueried().get(i).getParam_projection().size();j++){
         System.out.println("Nome: "+
         selectClause.getTablesQueried().get(i).getParam_projection().get(j).getName()+
         " Alias: "+
         selectClause.getTablesQueried().get(i).getParam_projection().get(j).getAlias());
         }
         }
         }*/
    }

    public void addGroupBy(PlainSelect plain) {
        //GROUP BY
        if (plain.getGroupByColumnReferences() != null) {
            for (int i = 0; i < plain.getGroupByColumnReferences().size(); i++) {
                selectClause.addColumnGroupBy((Column) plain.getGroupByColumnReferences().get(i));
            }
        }
    }

    public void addLimit(PlainSelect plain) {
        //LIMIT
        if (plain.getLimit() != null) {
            Limit limit = new Limit();
            if (plain.getLimit().isLimitAll()) {
                limit.setLimitAll(true);
            } else {
                limit.setOffset(plain.getLimit().getOffset());
                limit.setRowCount(plain.getLimit().getRowCount());
            }
            selectClause.setLimit(limit);
        }
    }

    public void addOrderBy(PlainSelect plain) {
        //ORDER BY
        if (plain.getOrderByElements() != null) {
            for (int i = 0; i < plain.getOrderByElements().size(); i++) {
                Sort sort = new Sort();

                if (((Column) ((OrderByElement) plain.getOrderByElements().get(i)).getExpression()).getTable() != null) {
                    String alias = ((Column) ((OrderByElement) plain.getOrderByElements().get(i)).getExpression()).getTable().toString();
                    String referenced_table = selectClause.convertAliasToName(alias);

                    if (referenced_table == null) {
                        referenced_table = alias;
                    }
                    sort.setReferencedTable(referenced_table);
                }
                sort.setAttribute(((Column) ((OrderByElement) plain.getOrderByElements().get(i)).getExpression()).getColumnName());
                if (((OrderByElement) plain.getOrderByElements().get(i)).isAsc()) {
                    sort.setOrder(1);   //ASC
                } else {
                    sort.setOrder(-1);   //DESC
                }
                selectClause.addOrder(sort);
            }
        }
    }

    public void addWhere(PlainSelect plain, Select selectStatement) throws JSQLParserException {
        if (plain.getWhere() != null) {
            CriteriaIdentifier criteriaIdentifier = new CriteriaIdentifier();
            criteriaIdentifier.redirectQuery(selectStatement);
            selectClause.setCriteriaIdentifier(criteriaIdentifier);
        }
    }
}
