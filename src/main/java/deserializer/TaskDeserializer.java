package deserializer;

import com.google.gson.*;
import debugger.ParsingJson;
import gml.Task;

import java.lang.reflect.Type;
import java.nio.file.Path;

public class TaskDeserializer implements JsonDeserializer<Task> {

    private Path path;

    public TaskDeserializer(Path path) {
        this.path = path;
    }

    @Override
    public Task deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Task task = new Task();

        task.setId(jsonObject.get("id").getAsString());
        task.setName(jsonObject.get("name").getAsString());
        task.setTaskType(jsonObject.get("taskType").getAsString());
        ParsingJson parsingJson = new ParsingJson(path);
        task.setSubGraph(parsingJson.deserializeFile(task.getId() + ".wf"));

        return task;
    }
}
