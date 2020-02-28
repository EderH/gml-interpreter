package debugger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import deserializer.EdgeDeserializer;
import deserializer.GraphDeserializer;
import deserializer.NodeDeserializer;
import deserializer.TaskDeserializer;
import gml.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            /*ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            URL url = classloader.getResource(sourceFile);
            if( url != null) {
                Reader reader = new InputStreamReader( ParsingJson.class.getResourceAsStream("/" + sourceFile), "UTF-8");*/
            if(sourceFile != null) {
                Path file = Paths.get(sourceFile);
                if(Files.exists(file)){
                    FileReader reader = new FileReader(sourceFile);
                    Graph graph = gson.fromJson(reader, Graph.class);
                    elements = graph.transformGraphToList();
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return elements;
    }
}
