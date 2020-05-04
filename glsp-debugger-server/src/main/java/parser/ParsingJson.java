package parser;

import debugger.ParsingException;
import graph.GGraph;

public abstract class ParsingJson {

    public abstract GGraph deserializeFile(String sourceFile) throws ParsingException;

}
