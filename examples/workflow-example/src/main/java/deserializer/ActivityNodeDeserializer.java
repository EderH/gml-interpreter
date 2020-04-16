package deserializer;

import com.google.gson.*;
import workflow.ActivityNode;

import java.lang.reflect.Type;

public class ActivityNodeDeserializer implements JsonDeserializer<ActivityNode> {
    @Override
    public ActivityNode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        ActivityNode node = new ActivityNode();

        node.setId(jsonObject.get("id").getAsString());
        node.setNodeType(jsonObject.get("nodeType").getAsString());

        return node;
    }
}
