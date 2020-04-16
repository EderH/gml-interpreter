package gml;

public abstract class GNode<T extends IElementVisitor> extends GElement {

    public abstract void accept(T elementVisitor);
}
