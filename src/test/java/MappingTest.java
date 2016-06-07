import aggregationFramework.AggregationQuery;
import com.mongodb.AggregationOutput;
import mapping.convert.QueryInterceptor;
import mapping.mediator.queryToObject.SelectToObject;
import mapping.statement.selectClause.SelectClause;
import mapping.statement.selectClause.TablesQueried;
import net.sf.jsqlparser.JSQLParserException;
import org.testng.annotations.*;

import java.util.ArrayList;

/**
 * Created by martian on 2016/06/04.
 */
public class MappingTest {

    public MappingTest() {
    }

//    @Test
//    public void queryToObject_test(){
//        String query = "SELECT Percentage FROM CountryLanguage;";
//        try{
//
//            SelectClause selectTranslate = new SelectToObject(query).selectClause;
//
////            for (TablesQueried qry : selectTranslate.getTablesQueried()){
////                System.out.println(qry.getAlias() + ": " + qry.getName());
////            }
//
//            System.out.println(selectTranslate.getFunctions().isEmpty());
//
//
//
//        }catch (JSQLParserException ex){
//
//        }
//
//
//    }

    @Test
    public void ya(){


        QueryInterceptor interceptor = new QueryInterceptor();
//        QueryInterceptor interceptor = new QueryInterceptor();
//
//        interceptor.intercept("SELECT Percentage FROM CountryLanguage;", "SELECT");

        ArrayList<String> returnStr = new ArrayList<>();

        returnStr = interceptor.intercept("SELECT Language FROM CountryLanguage;", "SELECT");

        System.out.println(returnStr.toString());

//        String query = "SELECT MAX(Percentage) FROM CountryLanguage;";
//
//        AggregationQuery resultAggregation = new AggregationQuery();
//
//        resultAggregation.returnAggOperators(query);

    }

    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    @AfterClass
    public static void tearDownClass() throws Exception {

    }

    @BeforeMethod
    public static void setUpMethod() throws Exception {

    }

    @AfterMethod
    public static void tearDownMethod() throws Exception {

    }
}
