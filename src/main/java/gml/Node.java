package gml;

import interpret.IElementVisitor;
import lombok.Getter;
import lombok.Setter;

public class Node extends Element{
    @Getter
    @Setter
    private String nodeType;

    @Override
    public void accept(IElementVisitor elementVisitor) {
        elementVisitor.visit(this);
    }
}
