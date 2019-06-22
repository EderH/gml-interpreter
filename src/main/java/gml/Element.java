package gml;

import interpret.IElementVisitor;
import lombok.Getter;
import lombok.Setter;

public abstract class Element {
    @Getter
    @Setter
    private String id;

    public abstract void accept(IElementVisitor elementVisitor);
}
