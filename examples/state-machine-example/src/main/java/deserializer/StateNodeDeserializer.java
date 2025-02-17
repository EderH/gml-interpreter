package deserializer;

import com.google.gson.*;
import debugger.SMMLDebugger;
import debugger.ParsingException;
import parser.SMMLParsingJson;
import statemachine.StateNode;

import java.lang.reflect.Type;
import java.nio.file.Path;

public class StateNodeDeserializer implements JsonDeserializer<StateNode> {


    private Path path;
    private SMMLDebugger SMMLDebugger;

    public StateNodeDeserializer(Path path, SMMLDebugger SMMLDebugger) {
        this.path = path;
        this.SMMLDebugger = SMMLDebugger;
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
        SMMLParsingJson SMMLParsingJson = new SMMLParsingJson(path, SMMLDebugger);
        try {
            state.setSubStateMachine(SMMLParsingJson.deserializeFile(state.getLabel() + ".sm", state));
        } catch (ParsingException exc) {
            SMMLDebugger.processException(exc);
        }

        return state;
    }
}
