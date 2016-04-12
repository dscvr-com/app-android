package co.optonaut.optonaut.record;

/**
 * @author Nilan Marktanner
 * @date 2016-02-10
 */
public class Edge {
    private SelectionPoint a;
    private SelectionPoint b;

    public Edge(SelectionPoint a, SelectionPoint b) {
        this.a = a;
        this.b = b;
    }

    /**
     * @return concatenated globalIds of SelectionPoint A and B. ":" is the concat String.
     */
    public String getGlobalIds() {
        return a.getGlobalId() + ":" + b.getGlobalId();
    }

    @Override
    public String toString() {
        return a.toString() + b.toString();
    }
}
