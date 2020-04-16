package interpreter;

import gml.IElementVisitor;
import statemachine.StateMachine;
import statemachine.StateNode;
import statemachine.Transition;

public interface IElementVisitorStateMachine extends IElementVisitor {

    void visit(StateMachine stateMachine);

    void visit(StateNode stateNode);

    void visit(Transition transition);
}
