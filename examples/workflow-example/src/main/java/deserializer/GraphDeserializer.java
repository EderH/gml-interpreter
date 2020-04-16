package deserializer;

import com.google.gson.*;
import gml.GElement;
import workflow.ActivityNode;
import workflow.Edge;
import workflow.Graph;
import workflow.TaskNode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GraphDeserializer implements JsonDeserializer<Graph> {

    @Override
    public Graph deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        final JsonElement jsonID = jsonObject.get("id");
        final JsonArray jsonChildrenArray = jsonObject.get("children").getAsJsonArray();

        List<GElement> GElementList = new ArrayList<>();
        List<Edge> edgeList = new ArrayList<>();
        List<TaskNode> taskList = new ArrayList<>();
        for (int i = 0; i < jsonChildrenArray.size(); i++) {
            final JsonObject child = jsonChildrenArray.get(i).getAsJsonObject();
            if(child.has("taskType")) {
                GElement task = jsonDeserializationContext.deserialize(child,TaskNode.class);
                GElementList.add(task);
                taskList.add((TaskNode)task);
            } else if( child.has("nodeType")) {
                GElement node = jsonDeserializationContext.deserialize(child, ActivityNode.class);
                GElementList.add(node);
            } else if(child.has("targetId")) {
                GElement edge = jsonDeserializationContext.deserialize(child, Edge.class);
                GElementList.add(edge);
                edgeList.add((Edge)edge);
            }
        }


        Graph graph = new Graph();
        graph.setId(jsonID.getAsString());
        graph.setElementList(GElementList);
        graph.setNodeList(taskList);
        graph.setEdgeList(edgeList);

        return graph;
    }
}
