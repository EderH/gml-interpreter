package gml;

import interpret.ElementVisitor;
import lombok.Getter;
import lombok.Setter;

public abstract class Element {
    @Getter
    @Setter
    private String ID;

    public abstract void accept(ElementVisitor elementVisitor);
}
