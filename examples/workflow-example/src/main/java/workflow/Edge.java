package workflow;

import graph.GEdge;
import interpreter.IElementVisitorWorkflow;
import lombok.Getter;
import lombok.Setter;

public class Edge extends GEdge<IElementVisitorWorkflow> {

    @Setter
    @Getter
    private boolean statement;

    public Edge(){
        super();
    }

    @Override
    public void accept(IElementVisitorWorkflow elementVisitor) {
        elementVisitor.visit(this);
    }
}
