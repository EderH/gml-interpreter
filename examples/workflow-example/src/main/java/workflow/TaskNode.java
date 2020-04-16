package workflow;

import gml.GNode;
import interpreter.IElementVisitorWorkflow;
import lombok.Getter;
import lombok.Setter;

public  class TaskNode extends GNode<IElementVisitorWorkflow> {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private int duration;
    @Setter
    @Getter
    private String taskType;
    @Setter
    @Getter
    private Graph subGraph;
    @Setter
    @Getter
    private String label;

    public TaskNode() {
    }

    @Override
    public void accept(IElementVisitorWorkflow elementVisitor) {
        elementVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "name: " + name;
    }
}
