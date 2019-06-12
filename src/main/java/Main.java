import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gml.*;
import deserializer.EdgeDeserializer;
import deserializer.GraphDeserializer;
import deserializer.NodeDeserializer;
import deserializer.TaskDeserializer;

import java.io.*;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {


        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new GraphDeserializer());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskDeserializer());
        gsonBuilder.registerTypeAdapter(Node.class, new NodeDeserializer());
        gsonBuilder.registerTypeAdapter(Edge.class, new EdgeDeserializer());
        Gson gson = gsonBuilder.create();

        try(Reader reader = new InputStreamReader(Main.class.getResourceAsStream("example1.wf"), "UTF-8")){

            // Parse JSON to Java
            Graph graph = gson.fromJson(reader, Graph.class);
            LinkedList<Element> linkedList = graph.transformGraphToList();
            for (int i = 0; i < linkedList.size(); i++) {
                System.out.println(linkedList.get(i).getID());
            }

            System.out.println(graph.getID());
            System.out.println(graph.getElementList().size());
            System.out.println(graph.getEdgeList().size());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
