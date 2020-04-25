package parser;

import debugger.ParsingException;
import gml.GGraph;

public abstract class ParsingJson {

    public abstract GGraph deserializeFile(String sourceFile) throws ParsingException;

}
