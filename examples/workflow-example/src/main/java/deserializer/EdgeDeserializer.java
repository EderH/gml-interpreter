package deserializer;

import com.google.gson.*;
import workflow.Edge;
import workflow.WeightedEdge;

import java.lang.reflect.Type;

public class EdgeDeserializer implements JsonDeserializer<Edge> {
    @Override
    public Edge deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Edge edge;
        String edgeType = jsonObject.get("type").getAsString();
        if(edgeType.endsWith("weighted")) {
            edge = new WeightedEdge();
            double randomNumber = (double)Math.round(Math.random()*100)/100;
            ((WeightedEdge) edge).setProbability(randomNumber);

        } else {
            edge = new Edge();
        }
        if(jsonObject.has("statement")) {
            edge.setStatement(jsonObject.get("statement").getAsBoolean());
        }
        /*if(jsonObject.has("probability")) {
            edge = new WeightedEdge();
            if(jsonObject.get("probability").getAsString().equals("high")) {
                ((WeightedEdge) edge).setProbability(1);
            } else {
                ((WeightedEdge) edge).setProbability(0.2);
            }
        } else {
            edge = new Edge();
        }*/

        edge.setId(jsonObject.get("id").getAsString());
        edge.setSourceID(jsonObject.get("sourceId").getAsString());
        edge.setTargetID(jsonObject.get("targetId").getAsString());

        return edge;
    }
}
