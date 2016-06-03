package mapping.queryToObject;

import mapping.InsertClause;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.insert.Insert;

import java.io.StringReader;

/**
 * Created by martian on 2016/06/03.
 */
public class InsertToObject {

    public InsertClause insertClause = new InsertClause();

    public InsertToObject(String sql) throws JSQLParserException {
        CCJSqlParserManager pm = new CCJSqlParserManager();
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Insert insertStatement = (Insert) statement;

        insertClause.setTable(insertStatement.getTable());
        insertClause.setColumns(insertStatement.getColumns());
        insertClause.setValues(((ExpressionList) insertStatement.getItemsList()));
    }
}
