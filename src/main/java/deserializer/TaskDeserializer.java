package deserializer;

import com.google.gson.*;
import gml.Task;

import java.lang.reflect.Type;

public class TaskDeserializer implements JsonDeserializer<Task> {

    @Override
    public Task deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Task task = new Task();

        task.setID(jsonObject.get("id").getAsString());
        task.setName(jsonObject.get("name").getAsString());
        task.setDuration(jsonObject.get("duration").getAsInt());
        task.setTaskType(jsonObject.get("taskType").getAsString());

        return task;
    }
}
