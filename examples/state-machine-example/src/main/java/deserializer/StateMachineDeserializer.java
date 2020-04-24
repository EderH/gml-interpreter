package deserializer;

import com.google.gson.*;
import gml.GElement;
import statemachine.StateMachine;
import statemachine.StateNode;
import statemachine.Transition;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StateMachineDeserializer implements JsonDeserializer<StateMachine> {

    @Override
    public StateMachine deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        final JsonElement jsonID = jsonObject.get("id");
        final JsonArray jsonChildrenArray = jsonObject.get("children").getAsJsonArray();

        StateMachine stateMachine = new StateMachine();
        stateMachine.setId(jsonID.getAsString());

        List<GElement> GElementList = new ArrayList<>();
        List<Transition> transitionList = new ArrayList<>();
        List<StateNode> stateList = new ArrayList<>();
        for (int i = 0; i < jsonChildrenArray.size(); i++) {
            final JsonObject child = jsonChildrenArray.get(i).getAsJsonObject();
            if(child.has("kind")) {
                StateNode state = jsonDeserializationContext.deserialize(child,StateNode.class);
                GElementList.add(state);
                stateList.add(state);
            } else if(child.has("targetId")) {
                Transition edge = jsonDeserializationContext.deserialize(child, Transition.class);
                GElementList.add(edge);
                transitionList.add(edge);
            }
        }



        stateMachine.setElementList(GElementList);
        stateMachine.setStateList(stateList);
        stateMachine.setTransitionList(transitionList);

        return stateMachine;
    }
}
