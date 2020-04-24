package parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import debugger.WorkflowDebugger;
import debugger.ParsingException;
import deserializer.EdgeDeserializer;
import deserializer.GraphDeserializer;
import deserializer.ActivityNodeDeserializer;
import deserializer.TaskNodeDeserializer;
import workflow.ActivityNode;
import workflow.Edge;
import workflow.Graph;
import workflow.TaskNode;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ParsingJson {

    private Gson gson;
    private Path sourceDirectory;

    public ParsingJson(Path path, WorkflowDebugger workflowDebugger) {
        this.sourceDirectory = path;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new GraphDeserializer());
        gsonBuilder.registerTypeAdapter(TaskNode.class, new TaskNodeDeserializer(path, workflowDebugger));
        gsonBuilder.registerTypeAdapter(ActivityNode.class, new ActivityNodeDeserializer());
        gsonBuilder.registerTypeAdapter(Edge.class, new EdgeDeserializer());
        gson = gsonBuilder.create();

    }

    public Graph deserializeFile(String sourceFile) throws ParsingException {
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
            throw new ParsingException("Could not find file " + sourceFile);
        }
        return graph;
    }



}
