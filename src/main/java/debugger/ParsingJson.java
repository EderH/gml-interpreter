package debugger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import deserializer.EdgeDeserializer;
import deserializer.GraphDeserializer;
import deserializer.NodeDeserializer;
import deserializer.TaskDeserializer;
import gml.*;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.LinkedList;

public class ParsingJson {

    GsonBuilder gsonBuilder;
    Gson gson;

    public ParsingJson() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new GraphDeserializer());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskDeserializer());
        gsonBuilder.registerTypeAdapter(Node.class, new NodeDeserializer());
        gsonBuilder.registerTypeAdapter(Edge.class, new EdgeDeserializer());
        gson = gsonBuilder.create();
    }

    public LinkedList<Element> parse(String sourceFile) {
        LinkedList<Element> elements = new LinkedList<>();
        try {
            URL url = ParsingJson.class.getResource("/" + sourceFile + ".wf");
            if( url != null) {
                Reader reader = new InputStreamReader( ParsingJson.class.getResourceAsStream("/" + sourceFile + ".wf"), "UTF-8");
                Graph graph = gson.fromJson(reader, Graph.class);
                elements = graph.transformGraphToList();
            }
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return elements;
    }
}
