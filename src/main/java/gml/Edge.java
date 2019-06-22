package gml;

import interpret.IElementVisitor;
import lombok.Getter;
import lombok.Setter;

public class Edge extends Element{
    @Getter
    @Setter
    private String sourceID;
    @Getter
    @Setter
    private String targetID;

    @Override
    public void accept(IElementVisitor elementVisitor) {
        elementVisitor.visit(this);
    }
}
