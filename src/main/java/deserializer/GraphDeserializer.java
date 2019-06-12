package deserializer;

import com.google.gson.*;
import gml.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GraphDeserializer implements JsonDeserializer<Graph> {

    @Override
    public Graph deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        final JsonElement jsonID = jsonObject.get("id");
        final JsonArray jsonChildrenArray = jsonObject.get("children").getAsJsonArray();

        List<Element> elementList = new ArrayList<>();
        List<Edge> edgeList = new ArrayList<>();
        List<Task> taskList = new ArrayList<>();
        for (int i = 0; i < jsonChildrenArray.size(); i++) {
            final JsonObject child = jsonChildrenArray.get(i).getAsJsonObject();
            if(child.has("taskType")) {
                Element task = jsonDeserializationContext.deserialize(child,Task.class);
                elementList.add(task);
                taskList.add((Task)task);
            } else if( child.has("nodeType")) {
                Element node = jsonDeserializationContext.deserialize(child, Node.class);
                elementList.add(node);
            } else if(child.has("targetId")) {
                Element edge = jsonDeserializationContext.deserialize(child, Edge.class);
                elementList.add(edge);
                edgeList.add((Edge)edge);
            }
        }


        Graph graph = new Graph();
        graph.setID(jsonID.getAsString());
        graph.setElementList(elementList);
        graph.setTaskList(taskList);
        graph.setEdgeList(edgeList);

        return graph;
    }
}
