package gml;

import lombok.Getter;
import lombok.Setter;

public abstract class GEdge<T extends IElementVisitor> extends GElement<T>{
    @Getter
    @Setter
    private String sourceID;
    @Getter
    @Setter
    private String targetID;

}
