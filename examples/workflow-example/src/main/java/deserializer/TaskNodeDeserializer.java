package deserializer;

import com.google.gson.*;
import debugger.WorkflowDebugger;
import debugger.ParsingException;
import parser.WorkflowParsingGraph;
import parser.WorkflowParsingJson;
import workflow.Graph;
import workflow.TaskNode;

import java.lang.reflect.Type;
import java.nio.file.Path;

public class TaskNodeDeserializer implements JsonDeserializer<TaskNode> {

    private Path path;
    private WorkflowDebugger workflowDebugger;

    public TaskNodeDeserializer(Path path, WorkflowDebugger workflowDebugger) {
        this.path = path;
        this.workflowDebugger = workflowDebugger;
    }

    @Override
    public TaskNode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        TaskNode task = new TaskNode();

        task.setId(jsonObject.get("id").getAsString());
        task.setName(jsonObject.get("name").getAsString());
        task.setTaskType(jsonObject.get("taskType").getAsString());

        JsonArray taskChildren = jsonObject.get("children").getAsJsonArray();
        for (int i = 0; i < taskChildren.size(); i++) {
            JsonObject child = taskChildren.get(i).getAsJsonObject();
            if(child.has("id")) {
                String childID = child.get("id").getAsString();
                if(childID.equals(task.getId() +"_header")) {
                    JsonArray taskHeaderChildren = child.get("children").getAsJsonArray();
                    for (int j = 0; j < taskHeaderChildren.size(); j++) {
                        JsonObject headerChild = taskHeaderChildren.get(j).getAsJsonObject();
                        if (headerChild.has("id")) {
                            String headerChildID = headerChild.get("id").getAsString();
                            if (headerChildID.equals(task.getId() + "_classname")) {
                                task.setLabel(headerChild.get("text").getAsString());
                            }
                        }
                    }
                }
            }

        }
        WorkflowParsingJson parsingJson = new WorkflowParsingJson(path, workflowDebugger);
        try {
            task.setSubGraph(parsingJson.deserializeFile(task.getId() + ".wf"));
        } catch (ParsingException exc) {
            workflowDebugger.processException(exc);
        }

        return task;
    }
}
