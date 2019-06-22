package interpret;

import gml.Edge;
import gml.Graph;
import gml.Node;
import gml.Task;

public interface IElementVisitor {

    public void visit(Graph graph);

    public void visit(Task task);

    public void visit(Edge edge);

    public void visit(Node node);

}
