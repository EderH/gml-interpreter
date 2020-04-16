package gml;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;

public abstract class GGraph<T extends IElementVisitor> extends GElement {

    @Setter
    @Getter
    private Path path;
    @Getter
    @Setter
    private List<GElement> ElementList;

    public abstract void accept(T elementVisitor);
}
