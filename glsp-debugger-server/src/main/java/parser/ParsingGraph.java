package parser;

import debugger.ParsingException;
import gml.GElement;
import gml.GGraph;
import lombok.Getter;
import lombok.Setter;

public abstract class ParsingGraph {

    @Setter
    @Getter
    private GGraph graph;

    public abstract GElement parseNextElement() throws ParsingException;

}
