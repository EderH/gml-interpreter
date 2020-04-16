package interpreter;

import workflow.ActivityNode;
import workflow.Edge;
import workflow.Graph;
import workflow.TaskNode;

public class Interpreter implements IElementVisitorWorkflow {

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
    public void visit(ActivityNode activityNode) {
        activityNode.setStatement(true);
        System.out.println("Visit ActivityNode: " + activityNode.getId());
    }

    @Override
    public void visit(TaskNode taskNode) {
        System.out.println("Visit TaskNode: " + taskNode.getId());
    }
}
