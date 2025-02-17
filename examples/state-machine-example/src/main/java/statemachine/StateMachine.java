package statemachine;

import graph.GGraph;
import interpreter.IElementVisitorStateMachine;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class StateMachine extends GGraph<IElementVisitorStateMachine> {

    @Setter
    @Getter
    private List<StateNode> stateList;
    @Setter
    @Getter
    private List<Transition> transitionList;
    @Setter
    @Getter
    private StateNode parent;

    public StateMachine() {

    }

    @Override
    public void accept(IElementVisitorStateMachine elementVisitor) {
        elementVisitor.visit(this);
    }
}
