package mapping.mediator.objectToNoSQL;

import aggregationFramework.AggregationQuery;
import com.mongodb.*;
import com.mongodb.util.JSON;
import mapping.statement.selectClause.SelectClause;
import connectionConfig.MongoConnection;
import net.sf.jsqlparser.JSQLParserException;

import java.util.ArrayList;

/**
 * Created by martian on 2016/06/03.
 */
public class SelectAggObjectTranslate {

    public DB database = MongoConnection.getInstance().getDB();

    public AggregationOutput executeMongoSelect(SelectClause selectStatement) throws JSQLParserException {

        // Considering only a  collection
        DBCollection collection = database.getCollection(selectStatement.getTablesQueried().get(0).getName());
        DBCursor dbCursor;
        DBObject criteria = (DBObject) JSON.parse(returnCriteria(selectStatement));
        DBObject order = (DBObject) JSON.parse(returnOrder(selectStatement));
        DBObject groupBy = null;
        Long limit = null, offset = null;

        AggregationQuery aggregation = new AggregationQuery();

        if (!selectStatement.getFunctions().isEmpty()) {
            groupBy = aggregation.returnAggOperators(selectStatement);
        }
        DBObject projection = null, projection_ppl = null;

        if (this.returnProjection(selectStatement) != null && !returnProjection(selectStatement).equals("*")) {
            projection = (DBObject) JSON.parse(returnProjection(selectStatement));
        }

        if (selectStatement.getLimit() != null) {
            limit = selectStatement.getLimit().getRowCount();
            offset = selectStatement.getLimit().getOffset();
        }
        long tempoInicial = System.currentTimeMillis();
        ArrayList<DBObject> vetor_valores = aggregation.returnVectorValues(criteria, projection, groupBy, order, limit, offset, selectStatement);
        AggregationOutput output_agg = aggregation.returnAggResult(vetor_valores, collection);
        long tempoFinal = System.currentTimeMillis();

        return output_agg;
    }

    public String returnProjection(SelectClause select) {
        if (select.getTablesQueried().get(0).isIsAllColumns()) {
            return "*";
        } else {
            String queryMongo = "{";

            if (select.getTablesQueried().get(0).getParam_projection().size() > 0) {
                if (!(select.getTablesQueried().get(0).isIsAllColumns())) {
                    for (int i = 0; i < select.getTablesQueried().get(0).getParam_projection().size(); i++) {
                        if (select.getTablesQueried().get(0).getParam_projection().get(i).getAlias() != null) {
                            String alias = select.getTablesQueried().get(0).getParam_projection().get(i).getAlias();
                            queryMongo += " '" + alias + "': '$" + select.getTablesQueried().get(0).getParam_projection().get(i).getName() + "' ,";
                        } else {
                            queryMongo += select.getTablesQueried().get(0).getParam_projection().get(i).getName() + " : 1,";
                        }
                    }
                }
                queryMongo += " _id: 0}";
            } else {
                return null;
            }

            return queryMongo;
        }
    }

    public String returnCriteria(SelectClause select) {
        return select.getCriteriaIdentifier().whereQuery.toString();
    }

    public String returnOrder(SelectClause select) {

        String queryMongo = null;

        if (select.getOrder().size() > 0) {
            queryMongo = "{";

            for (int i = 0; i < select.getOrder().size(); i++) {
                queryMongo += select.getOrder().get(i).getAttribute() + ": " + select.getOrder().get(i).getOrder() + ",";
            }
            queryMongo += "}";
        }

        return queryMongo;
    }
}
