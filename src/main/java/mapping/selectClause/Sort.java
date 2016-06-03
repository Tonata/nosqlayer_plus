package mapping.selectClause;

/**
 * Created by martian on 2016/06/03.
 */
public class Sort {

    private String attribute;
    private int order;          //ASC(1) ou DESC (-1)
    private String referencedTable;

    public String getReferencedTable() {
        return referencedTable;
    }

    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
