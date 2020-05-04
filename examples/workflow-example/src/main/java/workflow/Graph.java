package workflow;

import graph.GGraph;
import interpreter.IElementVisitorWorkflow;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Graph extends GGraph<IElementVisitorWorkflow> {

    @Setter
    @Getter
    private List<TaskNode> nodeList;
    @Setter
    @Getter
    private List<Edge> edgeList;

    public Graph() {
        super();
    }


    @Override
    public void accept(IElementVisitorWorkflow elementVisitor) {
        elementVisitor.visit(this);
    }
}
