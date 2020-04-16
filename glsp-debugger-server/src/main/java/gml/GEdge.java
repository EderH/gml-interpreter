package gml;

import lombok.Getter;
import lombok.Setter;

public abstract class GEdge<T extends IElementVisitor> extends GElement{
    @Getter
    @Setter
    private String sourceID;
    @Getter
    @Setter
    private String targetID;

    public abstract void accept(T elementVisitor);
}
