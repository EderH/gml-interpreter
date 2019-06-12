package gml;

import interpret.ElementVisitor;
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
    public void accept(ElementVisitor elementVisitor) {
        elementVisitor.visit(this);
    }
}
