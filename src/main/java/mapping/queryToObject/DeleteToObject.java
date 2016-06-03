package mapping.queryToObject;

import mapping.DeleteClause;
import mapping.criteria.CriteriaIdentifier;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;

import java.io.StringReader;

/**
 * Created by martian on 2016/06/03.
 */
public class DeleteToObject {

    public DeleteClause deleteClause = new DeleteClause();

    public DeleteToObject(String sql) throws JSQLParserException {
        CCJSqlParserManager pm = new CCJSqlParserManager();
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Delete deleteStatement = (Delete) statement;

        deleteClause.setTable(deleteStatement.getTable());

        if(deleteStatement.getWhere() != null){
            CriteriaIdentifier criteriaIdentifier = new CriteriaIdentifier();
            criteriaIdentifier.redirectQuery(deleteStatement);
            deleteClause.setCriteriaIdentifier(criteriaIdentifier);
        }

    }
}
