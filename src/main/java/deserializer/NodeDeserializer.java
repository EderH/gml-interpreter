package deserializer;

import com.google.gson.*;
import gml.Node;

import java.lang.reflect.Type;

public class NodeDeserializer implements JsonDeserializer<Node> {
    @Override
    public Node deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Node node = new Node();

        node.setId(jsonObject.get("id").getAsString());
        node.setNodeType(jsonObject.get("nodeType").getAsString());

        return node;
    }
}
