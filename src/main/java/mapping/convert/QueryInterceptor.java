package mapping.convert;

import aggregationFramework.AggregationQuery;
import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import mapping.statement.DeleteClause;
import mapping.statement.InsertClause;
import mapping.statement.SimpleSelect;
import mapping.mediator.objectToNoSQL.*;
import mapping.mediator.queryToObject.DeleteToObject;
import mapping.mediator.queryToObject.InsertToObject;
import mapping.mediator.queryToObject.SelectToObject;
import mapping.statement.selectClause.SelectClause;
import connectionConfig.MongoConnection;
import net.sf.jsqlparser.JSQLParserException;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by martian on 2016/06/03.
 */
@WebService(serviceName = "interceptQuery")
@Stateless()
public class QueryInterceptor {

    public static DB database = MongoConnection.getInstance().getDB();

    public QueryInterceptor() {
    }

    /**
     * Web service operation
     *
     * @param query
     * @param queryType
     * @return
     */
    @WebMethod(operationName = "intercept")
    public ArrayList<String> intercept(@WebParam(name = "query") String query, @WebParam(name = "queryType") String queryType) {
        ArrayList<String> returnStr = new ArrayList<>();

        try {

            long initialExecuteTime, finalExecuteSelectTime, totalSelectExecutionTime = 0,
                    initialHeaderExecutionTime, finalHeaderExecutionTime, totalHeaderExecutionTime = 0,
                    initialResultsetExecutionTime, finalResultsetExecutionTime, totalResultsetExecutionTime = 0;
            long initialTime, finalTime;
            long initTime = System.currentTimeMillis();

            switch (queryType) {
                case "SELECT":
                    /* Create objects from the select statement */
                    SelectClause selectTranslate = new SelectToObject(query).selectClause;
                    long finalQueryToObj_time = System.currentTimeMillis();
                    long totalQueryToObj_time = finalQueryToObj_time - initTime;
                    DBCursor resultSimpleQuery = null;
                    String result_set = null,
                            header;
                    if (selectTranslate.isHasJoin()) {
                        SelectObjectWithJoinTranslate selectMongo = new SelectObjectWithJoinTranslate();
                        initialHeaderExecutionTime = System.currentTimeMillis();
                        header = AggregationQuery.returnHeader(selectTranslate);
                        finalHeaderExecutionTime = System.currentTimeMillis();
                        totalHeaderExecutionTime = finalHeaderExecutionTime - initialHeaderExecutionTime;

                        initialResultsetExecutionTime = System.currentTimeMillis();
                        result_set = selectMongo.executeMongoSelect(selectTranslate, header);
                        finalResultsetExecutionTime = System.currentTimeMillis();
                        totalResultsetExecutionTime = finalResultsetExecutionTime - initialResultsetExecutionTime;
                    } else {
                        //Decision to use Aggregation Framework or not
                        if (selectTranslate.getFunctions().isEmpty()) {

                            SelectSimpleObjectTranslate selectMongo = new SelectSimpleObjectTranslate(); // MAKE LESS COUPLED
                            initialExecuteTime = System.currentTimeMillis();

                            resultSimpleQuery = selectMongo.executeMongoSelect(selectTranslate);

                            System.out.println("5: " + resultSimpleQuery.toString());

                            finalExecuteSelectTime = System.currentTimeMillis();
                            totalSelectExecutionTime = finalExecuteSelectTime - initialExecuteTime;

                            initialHeaderExecutionTime = System.currentTimeMillis();
                            header = SimpleSelect.returnHeader(selectTranslate);
                            finalHeaderExecutionTime = System.currentTimeMillis();
                            totalHeaderExecutionTime = finalHeaderExecutionTime - initialHeaderExecutionTime;
                            initialResultsetExecutionTime = System.currentTimeMillis();
                            result_set = SimpleSelect.returnResultSet(resultSimpleQuery, header);

                            finalResultsetExecutionTime = System.currentTimeMillis();
                            totalResultsetExecutionTime = finalResultsetExecutionTime - initialResultsetExecutionTime;

                        } else {
                            AggregationOutput resultAggregation;
                            SelectAggObjectTranslate selectMongo = new SelectAggObjectTranslate();
                            resultAggregation = selectMongo.executeMongoSelect(selectTranslate);
                            header = AggregationQuery.returnHeader(selectTranslate);
                            result_set = AggregationQuery.returnResultSet(resultAggregation, header);
                        }
                    }

                    returnStr.add("header = {" + header + "}");
                    returnStr.add("result_set = {"+result_set+"}");
                    //System.out.println(returnStr.get(0));
                    //System.out.println(result_set);

                    //returnStr.add(header);
                    //returnStr.add(resultado_select.toString().substring(2,resultado_select.toString().length()-2));
                    long tempoFinal = System.currentTimeMillis();
                    //System.out.println("Tempo Query to Object: " + tempoTotalQueryToObject);
                    //System.out.println("Tempo Execute Select: " + tempoTotalExecuteSelect);
                    //System.out.println("Tempo Execute Header: " + tempoTotalExecuteHeader);
                    //System.out.println("tempo Execute Result Set: " + tempoTotalExecuteResultSet);
                    //System.out.println("Tempo Total de execução: " + (tempoFinal - tempoInicial) + " ms");

                    //System.out.println(returnStr.get(0));
                    //System.out.println(returnStr.get(1));
                    break;

                case "INSERT":
                    /* Create objects from the insert query */
                    InsertClause insertTranslate = new InsertToObject(query).insertClause;

                    InsertObjectTranslate insertMongo = new InsertObjectTranslate();
                    insertMongo.executeMongoInsert(insertTranslate);
                    break;

//                case "UPDATE":
//                    /*Cria objeto a partir da consulta update*/
//                    UpdateClause updateTranslate = new UpdateToObject(query).updateClause;
//                    UpdateObjectTranslate updateMongo = new UpdateObjectTranslate();
//                    updateMongo.executeMongoUpdate(updateTranslate);
//                    break;

                case "DELETE":

                    /* Create objects from the delete query */
                    DeleteClause deleteTranslate = new DeleteToObject(query).deleteClause;

                    DeleteObjectTranslate deleteMongo = new DeleteObjectTranslate();
                    deleteMongo.executeMongoDelete(deleteTranslate);
                    break;
            }
        } catch (JSQLParserException ex) {
            Logger.getLogger(QueryInterceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnStr;
    }
}
