package statemachine;

import graph.GNode;
import interpreter.IElementVisitorStateMachine;
import lombok.Getter;
import lombok.Setter;

public class StateNode extends GNode<IElementVisitorStateMachine> {

    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String stateTyp;
    @Setter
    @Getter
    private StateMachine subStateMachine;
    @Setter
    @Getter
    private String label;

    @Override
    public void accept(IElementVisitorStateMachine elementVisitor) {
        elementVisitor.visit(this);
    }
}
