package graph;


import lombok.Getter;
import lombok.Setter;

public abstract class GElement<T extends IElementVisitor> {
    @Getter
    @Setter
    private String id;

    public abstract void accept(T elementVisitor);
}
