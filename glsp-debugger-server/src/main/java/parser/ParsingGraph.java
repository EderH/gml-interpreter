package parser;

import debugger.ParsingException;
import graph.GElement;
import graph.GGraph;
import lombok.Getter;
import lombok.Setter;

public abstract class ParsingGraph {

    @Setter
    @Getter
    private GGraph graph;

    public abstract GElement parseNextElement() throws ParsingException;

}
