package deserializer;

import com.google.gson.*;
import debugger.StateMachineDebugger;
import debugger.ParsingException;
import parser.ParsingJson;
import statemachine.StateNode;

import java.lang.reflect.Type;
import java.nio.file.Path;

public class StateNodeDeserializer implements JsonDeserializer<StateNode> {


    private Path path;
    private StateMachineDebugger stateMachineDebugger;

    public StateNodeDeserializer(Path path, StateMachineDebugger stateMachineDebugger) {
        this.path = path;
        this.stateMachineDebugger = stateMachineDebugger;
    }

    @Override
    public StateNode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        StateNode state = new StateNode();

        state.setId(jsonObject.get("id").getAsString());
        state.setName(jsonObject.get("name").getAsString());
        state.setStateTyp(jsonObject.get("kind").getAsString());

        JsonArray taskChildren = jsonObject.get("children").getAsJsonArray();
        for (int i = 0; i < taskChildren.size(); i++) {
            JsonObject child = taskChildren.get(i).getAsJsonObject();
            if(child.has("id")) {
                String childID = child.get("id").getAsString();
                if(childID.equals(state.getId() +"_header")) {
                    JsonArray taskHeaderChildren = child.get("children").getAsJsonArray();
                    for (int j = 0; j < taskHeaderChildren.size(); j++) {
                        JsonObject headerChild = taskHeaderChildren.get(j).getAsJsonObject();
                        if (headerChild.has("id")) {
                            String headerChildID = headerChild.get("id").getAsString();
                            if (headerChildID.equals(state.getId() + "_classname")) {
                                String text = headerChild.get("text").getAsString();
                                if(text.isEmpty()) {
                                    if(state.getStateTyp().equals("initialState")) {
                                        state.setLabel("Initial");
                                    } else{
                                        state.setLabel("Final");
                                    }
                                } else {
                                    state.setLabel(text);
                                }
                            }
                        }
                    }
                }
            }

        }
        ParsingJson parsingJson = new ParsingJson(path, stateMachineDebugger);
        try {
            state.setSubStateMachine(parsingJson.deserializeFile(state.getLabel() + ".sm", state));
        } catch (ParsingException exc) {
            stateMachineDebugger.processException(exc);
        }

        return state;
    }
}
