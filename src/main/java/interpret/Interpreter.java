package interpret;

import gml.Edge;
import gml.Graph;
import gml.Node;
import gml.Task;

public class Interpreter implements IElementVisitor {

    public Interpreter() {

    }

    @Override
    public void visit(Graph graph) {
        System.out.println("Visit Graph:" + graph.getId());
    }

    @Override
    public void visit(Edge edge) {
        System.out.println("Visit Edge: " + edge.getId());
    }

    @Override
    public void visit(Node node) {
        System.out.println("Visit Node: " + node.getId());
    }

    @Override
    public void visit(Task task) {
        System.out.println("Visit Task: " + task.getId());
    }
}
