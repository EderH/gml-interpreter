package gml;

import interpret.IElementVisitor;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Graph extends Element{

    @Setter
    @Getter
    private Path path;
    @Setter
    @Getter
    private List<Task> taskList;
    @Setter
    @Getter
    private List<Edge> edgeList;
    @Getter
    @Setter
    private List<Element> elementList;

    public Graph() {

    }

    @Override
    public void accept(IElementVisitor elementVisitor) {
        elementVisitor.visit(this);
    }
}
