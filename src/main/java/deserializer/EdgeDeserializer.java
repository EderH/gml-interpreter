package deserializer;

import com.google.gson.*;
import gml.Edge;
import gml.WeightedEdge;

import java.lang.reflect.Type;

public class EdgeDeserializer implements JsonDeserializer<Edge> {
    @Override
    public Edge deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Edge edge;
        if(jsonObject.has("probability")) {
            edge = new WeightedEdge();
            if(jsonObject.get("probability").getAsString().equals("high")) {
                ((WeightedEdge) edge).setProbability(1);
            } else {
                ((WeightedEdge) edge).setProbability(0.2);
            }
        } else {
            edge = new Edge();
        }

        edge.setId(jsonObject.get("id").getAsString());
        edge.setSourceID(jsonObject.get("sourceId").getAsString());
        edge.setTargetID(jsonObject.get("targetId").getAsString());

        return edge;
    }
}
