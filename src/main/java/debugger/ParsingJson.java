package debugger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import deserializer.EdgeDeserializer;
import deserializer.GraphDeserializer;
import deserializer.NodeDeserializer;
import deserializer.TaskDeserializer;
import gml.*;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ParsingJson {

    private Gson gson;
    private Path sourceDirectory;

    public ParsingJson(Path path) {
        this.sourceDirectory = path;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new GraphDeserializer());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskDeserializer(path));
        gsonBuilder.registerTypeAdapter(Node.class, new NodeDeserializer());
        gsonBuilder.registerTypeAdapter(Edge.class, new EdgeDeserializer());
        gson = gsonBuilder.create();

    }

    public Graph deserializeFile(String sourceFile) {
        Graph graph = null;
        try {
            if(sourceFile != null) {
                Path file = Paths.get(sourceDirectory.toString(),sourceFile);
                if(Files.exists(file)){
                    FileReader reader = new FileReader(file.toString());
                    graph = gson.fromJson(reader, Graph.class);
                    graph.setPath(file);
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return graph;
    }



}
