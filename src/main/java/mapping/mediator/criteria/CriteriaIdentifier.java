package mapping.mediator.criteria;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import mapping.statement.SimpleSelect;
import mapping.mediator.objectToNoSQL.SelectAggObjectTranslate;
import mapping.mediator.objectToNoSQL.SelectSimpleObjectTranslate;
import mapping.mediator.queryToObject.SelectToObject;
import mapping.statement.selectClause.SelectClause;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by martian on 2016/06/03.
 */
public class CriteriaIdentifier {

    public List<Criteria> criteria = new ArrayList<>();
    public List<String> connectiveLogic = new ArrayList<>();
    public List inList = new ArrayList<>();
    private String inColumn;
    public BasicDBObject whereQuery = new BasicDBObject();
    public BasicDBObject whereLeftTmp = new BasicDBObject();

    public void redirectQuery(Object statement) throws JSQLParserException {
        Expression expressionWhere = null;

        if (statement instanceof Select) {
            PlainSelect select = (PlainSelect) ((Select) statement).getSelectBody();
            expressionWhere = select.getWhere();
        } else if (statement instanceof Delete) {
            Delete delete = (Delete) statement;
            expressionWhere = delete.getWhere();
        } else if (statement instanceof Update) {
            Update update = (Update) statement;
            expressionWhere = update.getWhere();
        } else if (statement instanceof Insert) {
            //TODO
        }

        if (expressionWhere instanceof AndExpression) {
            AndExpression e = (AndExpression) expressionWhere;
            this.criteriaFragment(e.getLeftExpression(), e.getRightExpression(), "$and");
        } else if (expressionWhere instanceof OrExpression) {
            OrExpression e = (OrExpression) expressionWhere;
            this.criteriaFragment(e.getLeftExpression(), e.getRightExpression(), "$or");
        } else if (expressionWhere != null) {
            this.simpleCriterion(expressionWhere);
        }
    }
    /*
     * Seeking all the criteria in a compound WHERE clause
     */

    public void criteriaFragment(Expression leftExpression, Expression rightExpression, String connective) {

        if (rightExpression instanceof Parenthesis) {
            rightExpression = ((Parenthesis) rightExpression).getExpression();
        }

        if (rightExpression instanceof AndExpression) {
            AndExpression e = (AndExpression) rightExpression;
            criteriaFragment(e.getLeftExpression(), e.getRightExpression(), "$and");
        } else if (rightExpression instanceof OrExpression) {
            OrExpression e = (OrExpression) rightExpression;
            criteriaFragment(e.getLeftExpression(), e.getRightExpression(), "$or");
        }

        if (leftExpression instanceof Parenthesis) {
            leftExpression = ((Parenthesis) leftExpression).getExpression();
        }

        if (leftExpression instanceof AndExpression) {
            AndExpression e = (AndExpression) leftExpression;
            criteriaFragment(e.getLeftExpression(), e.getRightExpression(), "$and");

        } else if (leftExpression instanceof OrExpression) {
            OrExpression e = (OrExpression) leftExpression;
            criteriaFragment(e.getLeftExpression(), e.getRightExpression(), "$or");
        }

        //End of recursion
        if (leftExpression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) leftExpression;
            // saves the operations as are used in the document
            this.addCriteria(equalsTo.getLeftExpression().toString(), ":", equalsTo.getRightExpression().toString());

        } else if (leftExpression instanceof GreaterThan) {
            GreaterThan greaterThan = (GreaterThan) leftExpression;
            this.addCriteria(greaterThan.getLeftExpression().toString(), "$gt", greaterThan.getRightExpression().toString());

        } else if (leftExpression instanceof GreaterThanEquals) {
            GreaterThanEquals greaterThanEquals = (GreaterThanEquals) leftExpression;
            this.addCriteria(greaterThanEquals.getLeftExpression().toString(), "$gte", greaterThanEquals.getRightExpression().toString());

        } else if (leftExpression instanceof MinorThan) {
            MinorThan minorThan = (MinorThan) leftExpression;
            this.addCriteria(minorThan.getLeftExpression().toString(), "$lt", minorThan.getRightExpression().toString());

        } else if (leftExpression instanceof MinorThanEquals) {
            MinorThanEquals minorThanEquals = (MinorThanEquals) leftExpression;
            this.addCriteria(minorThanEquals.getLeftExpression().toString(), "$lte", minorThanEquals.getRightExpression().toString());

        } else if (leftExpression instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) leftExpression;
            this.addCriteria(notEqualsTo.getLeftExpression().toString(), "$ne", notEqualsTo.getRightExpression().toString());
        }

        // Check the right
        if (rightExpression instanceof EqualsTo) {

            EqualsTo equalsTo = (EqualsTo) rightExpression;
            this.addCriteria(equalsTo.getLeftExpression().toString(), ":", equalsTo.getRightExpression().toString());

        } else if (rightExpression instanceof GreaterThan) {
            GreaterThan greaterThan = (GreaterThan) rightExpression;
            this.addCriteria(greaterThan.getLeftExpression().toString(), "$gt", greaterThan.getRightExpression().toString());

        } else if (rightExpression instanceof GreaterThanEquals) {
            GreaterThanEquals greaterThanEquals = (GreaterThanEquals) rightExpression;
            this.addCriteria(greaterThanEquals.getLeftExpression().toString(), "$gte", greaterThanEquals.getRightExpression().toString());

        } else if (rightExpression instanceof MinorThan) {
            MinorThan minorThan = (MinorThan) rightExpression;
            this.addCriteria(minorThan.getLeftExpression().toString(), "$lt", minorThan.getRightExpression().toString());

        } else if (rightExpression instanceof MinorThanEquals) {
            MinorThanEquals minorThanEquals = (MinorThanEquals) rightExpression;
            this.addCriteria(minorThanEquals.getLeftExpression().toString(), "$lte", minorThanEquals.getRightExpression().toString());

        } else if (rightExpression instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) rightExpression;
            this.addCriteria(notEqualsTo.getLeftExpression().toString(), "$ne", notEqualsTo.getRightExpression().toString());
        }

        if (connective.equals("$and") || connective.equals("$or")) {
            List<BasicDBObject> obj = new ArrayList<>();
            for (int i = 0; i < criteria.size(); i++) {

                if (criteria.size() > 0 && criteria.get(i).getOperator().equals(":")) {
                    //Integer
                    if (criteria.get(i).getRightExpression().matches("^[0-9]*$")) {
                        obj.add(new BasicDBObject(criteria.get(i).getLeftExpression(), Long.parseLong(criteria.get(i).getRightExpression().trim())));
                    } else {
                        //String and remove single quotes
                        String right_value = criteria.get(i).getRightExpression().substring(1, criteria.get(i).getRightExpression().length() - 1);
                        obj.add(new BasicDBObject(criteria.get(i).getLeftExpression(), right_value));
                    }
                } else {
                    //Integer
                    if (criteria.get(i).getRightExpression().matches("^[0-9]*$")) {
                        BasicDBObject operator_cmp = new BasicDBObject(criteria.get(i).getOperator(), Long.parseLong(criteria.get(i).getRightExpression().trim()));
                        obj.add(new BasicDBObject(criteria.get(i).getLeftExpression(), operator_cmp));

                    } else {
                        //String and remove single qoutes
                        String right_value = criteria.get(i).getRightExpression().substring(1, criteria.get(i).getRightExpression().length() - 1);
                        BasicDBObject operator_cmp = new BasicDBObject(criteria.get(i).getOperator(), right_value);
                        obj.add(new BasicDBObject(criteria.get(i).getLeftExpression(), operator_cmp));
                    }
                }
            }
            if (whereQuery.isEmpty()) {
                whereQuery.put(connective, obj);
            } else {
                ArrayList connectiveList = new ArrayList();

                if (!obj.isEmpty()) {
                    //For clause type column1 = value 1 OR (column2 = value 2 AND column3 = value3)
                    if (obj.size() == 1) {
                        connectiveList.add(obj.get(0));
                        connectiveList.add(whereQuery);
                        whereQuery.remove(0);
                        whereQuery = new BasicDBObject(connective, connectiveList);

                        //For clause type (column1 = value 1 AND column4 = value4) OR (column2 = value 2 AND column3 = value3)
                    } else {
                        ArrayList listNvl = new ArrayList();
                        for (int i = 0; i < obj.size(); i++) {
                            listNvl.add(obj.get(i));
                        }
                        whereLeftTmp = new BasicDBObject(connective, listNvl);
                    }
                } else {
                    connectiveList.add(whereLeftTmp);
                    whereLeftTmp.remove(0);
                    connectiveList.add(whereQuery);
                    whereQuery.remove(0);
                    whereQuery = new BasicDBObject(connective, connectiveList);
                }

            }
        }
        criteria.clear();
        this.addConnectiveLogic(connective);
    }

    /*
     * Function that looks at simple criteria . Ex WHERE id = ' 1'
     */
    public void simpleCriterion(Expression expression) throws JSQLParserException {

        ArrayList<String> select_result;

        if (expression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) expression;
            if (equalsTo.getLeftExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) equalsTo.getLeftExpression()).getSelectBody().toString());
                //For subselect can return more than one value
                setValuesInSubSelect(select_result);
                this.setInColumn(equalsTo.getRightExpression().toString());
            } else if (equalsTo.getRightExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) equalsTo.getRightExpression()).getSelectBody().toString());

                setValuesInSubSelect(select_result);
                this.setInColumn(equalsTo.getLeftExpression().toString());
            } else {
                this.addCriteria(equalsTo.getLeftExpression().toString(), ":", equalsTo.getRightExpression().toString());
            }

        } else if (expression instanceof GreaterThan) {
            GreaterThan greaterThan = (GreaterThan) expression;
            if (greaterThan.getLeftExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) greaterThan.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$gt", greaterThan.getRightExpression().toString());
            } else if (greaterThan.getRightExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) greaterThan.getRightExpression()).getSelectBody().toString());
                this.addCriteria(greaterThan.getLeftExpression().toString(), "$gt", select_result.get(0));
            } else {
                this.addCriteria(greaterThan.getLeftExpression().toString(), "$gt", greaterThan.getRightExpression().toString());
            }
        } else if (expression instanceof GreaterThanEquals) {
            GreaterThanEquals greaterThanEqualsTo = (GreaterThanEquals) expression;
            if (greaterThanEqualsTo.getLeftExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) greaterThanEqualsTo.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$gte", greaterThanEqualsTo.getRightExpression().toString());
            } else if (greaterThanEqualsTo.getRightExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) greaterThanEqualsTo.getRightExpression()).getSelectBody().toString());
                this.addCriteria(greaterThanEqualsTo.getLeftExpression().toString(), "$gte", select_result.get(0));
            } else {
                this.addCriteria(greaterThanEqualsTo.getLeftExpression().toString(), "$gte", greaterThanEqualsTo.getRightExpression().toString());
            }

        } else if (expression instanceof MinorThan) {
            MinorThan minorThan = (MinorThan) expression;
            if (minorThan.getLeftExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) minorThan.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$lt", minorThan.getRightExpression().toString());
            } else if (minorThan.getRightExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) minorThan.getRightExpression()).getSelectBody().toString());
                this.addCriteria(minorThan.getLeftExpression().toString(), "$lt", select_result.get(0));
            } else {
                this.addCriteria(minorThan.getLeftExpression().toString(), "$lt", minorThan.getRightExpression().toString());
            }

        } else if (expression instanceof MinorThanEquals) {
            MinorThanEquals minorThanEquals = (MinorThanEquals) expression;
            if (minorThanEquals.getLeftExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) minorThanEquals.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$lte", minorThanEquals.getRightExpression().toString());
            } else if (minorThanEquals.getRightExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) minorThanEquals.getRightExpression()).getSelectBody().toString());
                this.addCriteria(minorThanEquals.getLeftExpression().toString(), "$lte", select_result.get(0));
            } else {
                this.addCriteria(minorThanEquals.getLeftExpression().toString(), "$lte", minorThanEquals.getRightExpression().toString());
            }

        } else if (expression instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) expression;
            if (notEqualsTo.getLeftExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) notEqualsTo.getLeftExpression()).getSelectBody().toString());
                this.addCriteria(select_result.get(0), "$ne", notEqualsTo.getRightExpression().toString());
            } else if (notEqualsTo.getRightExpression() instanceof SubSelect) {
                select_result = returnSubSelectValue(((SubSelect) notEqualsTo.getRightExpression()).getSelectBody().toString());
                this.addCriteria(notEqualsTo.getLeftExpression().toString(), "$ne", select_result.get(0));
            } else {
                this.addCriteria(notEqualsTo.getLeftExpression().toString(), "$ne", notEqualsTo.getRightExpression().toString());
            }

        } else if (expression instanceof LikeExpression) {
            LikeExpression likeExpression = (LikeExpression) expression;
            if (likeExpression.getLeftExpression() instanceof SubSelect) {
                //TODO
            } else if (likeExpression.getRightExpression() instanceof SubSelect) {
                //TODO
            } else {
                this.addCriteria(likeExpression.getLeftExpression().toString(), "$regex", likeExpression.getRightExpression().toString());
            }

        } else if (expression instanceof InExpression) {
            InExpression in = (InExpression) expression;
            List items;
            if (in.getItemsList() instanceof SubSelect) {
                items = returnSubSelectValue(((SubSelect) in.getItemsList()).getSelectBody().toString());
            } else {
                items = ((ExpressionList) in.getItemsList()).getExpressions();

            }
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).toString().matches("^[0-9]*$")) {

                    this.addInElementLong(Long.parseLong(items.get(i).toString()));
                } else {
                    this.addInElement(items.get(i).toString().replace("'", ""));
                }
            }
            this.setInColumn(in.getLeftExpression().toString());
        }

        if (criteria.size() > 0 && criteria.get(0).getOperator().equals(":")) {

            if (criteria.get(0).getRightExpression().matches("^[0-9]*$")) {
                whereQuery.put(criteria.get(0).getLeftExpression(), Long.parseLong(criteria.get(0).getRightExpression().trim()));

            } else {

                String valor_direita = criteria.get(0).getRightExpression().substring(1, criteria.get(0).getRightExpression().length() - 1);
                whereQuery.put(criteria.get(0).getLeftExpression(), valor_direita);
            }
        } else if (criteria.size() > 0 && criteria.get(0).getOperator().equals("$regex")) {
            String right_value = criteria.get(0).getRightExpression().substring(1, criteria.get(0).getRightExpression().length() - 1).replaceAll("%", ".*");
            BasicDBObject operator_cmp = new BasicDBObject(criteria.get(0).getOperator(), right_value);
            whereQuery.put(criteria.get(0).getLeftExpression(), operator_cmp);
        } else if (criteria.size() > 0) {

            if (criteria.get(0).getRightExpression().matches("^[0-9]*$")) {
                BasicDBObject operator_cmp = new BasicDBObject(criteria.get(0).getOperator(), Integer.parseInt(criteria.get(0).getRightExpression().trim()));
                whereQuery.put(criteria.get(0).getLeftExpression(), operator_cmp);

            } else {

                String right_value = criteria.get(0).getRightExpression().substring(1, criteria.get(0).getRightExpression().length() - 1);
                BasicDBObject operator_cmp = new BasicDBObject(criteria.get(0).getOperator(), right_value);
                whereQuery.put(criteria.get(0).getLeftExpression(), operator_cmp);

            }
        }

        if (inColumn != null) {
            whereQuery.put(inColumn, new BasicDBObject("$in", inList));
        }
    }

    public void addCriteria(String leftExpression, String operator, String rightExpression) {
        Criteria criteria = new Criteria();
        criteria.setLeftExpression(leftExpression);
        criteria.setOperator(operator);
        criteria.setRightExpression(rightExpression);
        this.criteria.add(criteria);
    }

    public ArrayList<String> returnSubSelectValue(String query) throws JSQLParserException {

        SelectClause selectTranslate = new SelectToObject(query).selectClause;
        DBCursor resultSimpleQuery;
        AggregationOutput resultAggregation;
        String result_set, header;
        ArrayList<String> toReturn = new ArrayList<>();
        if (selectTranslate.getFunctions().isEmpty()) {
            SelectSimpleObjectTranslate selectMongo = new SelectSimpleObjectTranslate();
            resultSimpleQuery = selectMongo.executeMongoSelect(selectTranslate);
            header = SimpleSelect.returnHeader(selectTranslate);
            while (resultSimpleQuery.hasNext()) {
                toReturn.add(resultSimpleQuery.next().get(header.replace(",", "")).toString());
            }
            return toReturn;
        } else {
            SelectAggObjectTranslate selectMongo = new SelectAggObjectTranslate();
            resultAggregation = selectMongo.executeMongoSelect(selectTranslate);

            String json_str = resultAggregation.results().toString();

            int result_size = json_str.length();

            JSONObject my_obj = new JSONObject(json_str.substring(1, result_size - 1));

            JSONArray array = new JSONArray(json_str);
            JSONObject my_obj_result_set;
            for (int i = 0; i < array.length(); i++) {

                my_obj_result_set = array.getJSONObject(i);
                my_obj_result_set.remove("_id");

                //Returns the keys of each document , because not all has all the attributes
                Iterator it = my_obj_result_set.keys();
                while (it.hasNext()) {
                    String next = it.next().toString();
                    if (my_obj_result_set.get(next) != null) {


                        if (my_obj_result_set.get(next).toString().matches("^[a-zA-ZÁÂÃÀÇÉÊÍÓÔÕÚÜáâãàçéêíóôõúü]*$")) {
                            toReturn.add("'" + my_obj_result_set.get(next) + "'");
                        } else if (my_obj_result_set.get(next).toString().matches("^[0-9]*$")) {
                            toReturn.add("" + my_obj_result_set.getInt(next) + "");
                        } else {
                            toReturn.add("" + my_obj_result_set.getString(next) + "");
                        }
                    }
                }
            }
            return toReturn;
        }

    }

    public void setValuesInSubSelect(ArrayList<String> select_result) {
        for (int i = 0; i < select_result.size(); i++) {
            if (select_result.get(i).toString().matches("^[0-9]{1,5}$")) {
                this.addInElementLong(Long.parseLong(select_result.get(i).toString()));
            } else {
                this.addInElement(select_result.get(i).toString().replace("'", ""));
            }
        }
    }

    public void addConnectiveLogic(String connective) {
        this.connectiveLogic.add(connective);
    }

    public void addInElement(String element) {
        this.inList.add(element);
    }

    public void addInElementLong(Long element) {
        this.inList.add(element);
    }

    public String getInColumn() {
        return inColumn;
    }

    public void setInColumn(String inColumn) {
        this.inColumn = inColumn;
    }

    public List<Criteria> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<Criteria> criteria) {
        this.criteria = criteria;
    }

    public List<String> getConnectiveLogic() {
        return connectiveLogic;
    }

    public void setConnectiveLogic(List<String> connectiveLogic) {
        this.connectiveLogic = connectiveLogic;
    }

    public List<String> getInList() {
        return inList;
    }

    public void setInList(List<String> inList) {
        this.inList = inList;
    }

    public boolean all(String s) {

        char[] c = s.toCharArray();
        boolean d = true;

        for (int i = 0; i < c.length; i++)
        {
            if (!Character.isDigit(c[ i])) {
                d = false;
                break;
            }
        }
        return d;
    }
}
