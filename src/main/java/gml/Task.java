package gml;

import interpret.IElementVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

public  class Task extends Element {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private int duration;
    @Setter
    @Getter
    private String taskType;
    @Setter
    @Getter
    private Graph subGraph;

    public Task() {
    }

    @Override
    public void accept(IElementVisitor elementVisitor) {
        elementVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "name: " + name;
    }
}
