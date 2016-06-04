package mapping.statement;

import mapping.mediator.criteria.CriteriaIdentifier;
import net.sf.jsqlparser.schema.Table;

/**
 * Created by martian on 2016/06/03.
 */
public class DeleteClause {

    private Table table;
    private CriteriaIdentifier criteriaIdentifier = new CriteriaIdentifier();

    public CriteriaIdentifier getCriteriaIdentifier() {
        return criteriaIdentifier;
    }

    public void setCriteriaIdentifier(CriteriaIdentifier criteriaIdentifier) {
        this.criteriaIdentifier = criteriaIdentifier;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
