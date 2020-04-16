package deserializer;

import com.google.gson.*;
import statemachine.Transition;

import java.lang.reflect.Type;

public class TransitionDeserializer implements JsonDeserializer<Transition> {

    @Override
    public Transition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Transition transition = new Transition();

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

        transition.setId(jsonObject.get("id").getAsString());
        transition.setSourceID(jsonObject.get("sourceId").getAsString());
        transition.setTargetID(jsonObject.get("targetId").getAsString());
        transition.setTrigger(jsonObject.get("trigger").getAsString());
        transition.setEffect(jsonObject.get("effect").getAsString());

        return transition;
    }
}
