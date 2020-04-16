package interpreter;

import gml.IElementVisitor;
import workflow.ActivityNode;
import workflow.Edge;
import workflow.Graph;
import workflow.TaskNode;

public interface IElementVisitorWorkflow extends IElementVisitor {

    void visit(Graph graph);

    void visit(TaskNode taskNode);

    void visit(Edge edge);

    void visit(ActivityNode activityNode);
}
