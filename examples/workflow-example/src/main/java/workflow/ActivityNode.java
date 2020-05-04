package workflow;

import graph.GNode;
import interpreter.IElementVisitorWorkflow;
import lombok.Getter;
import lombok.Setter;

public class ActivityNode extends GNode<IElementVisitorWorkflow> {
    @Getter
    @Setter
    private String nodeType;
    @Getter
    @Setter
    private boolean statement;

    @Override
    public void accept(IElementVisitorWorkflow elementVisitor) {
        elementVisitor.visit(this);
    }
}
