package statemachine;

import gml.GEdge;
import interpreter.IElementVisitorStateMachine;
import lombok.Getter;
import lombok.Setter;

public class Transition extends GEdge<IElementVisitorStateMachine> {

    @Setter
    @Getter
    String event;
    @Setter
    @Getter
    String action;

    @Override
    public void accept(IElementVisitorStateMachine elementVisitor) {
        elementVisitor.visit(this);
    }
}
