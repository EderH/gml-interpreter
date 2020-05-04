package parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import debugger.StateMachineDebugger;
import debugger.ParsingException;
import deserializer.StateMachineDeserializer;
import deserializer.StateNodeDeserializer;
import deserializer.TransitionDeserializer;
import statemachine.StateMachine;
import statemachine.StateNode;
import statemachine.Transition;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ParsingJson {

    private Gson gson;
    private Path sourceDirectory;

    public ParsingJson(Path path, StateMachineDebugger stateMachineDebugger) {
        this.sourceDirectory = path;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(StateMachine.class, new StateMachineDeserializer());
        gsonBuilder.registerTypeAdapter(StateNode.class, new StateNodeDeserializer(path, stateMachineDebugger));
        gsonBuilder.registerTypeAdapter(Transition.class, new TransitionDeserializer());
        gson = gsonBuilder.create();

    }

    public StateMachine deserializeFile(String sourceFile, StateNode parent) throws ParsingException {
        StateMachine stateMachine = null;
        try {
            if(sourceFile != null) {
                Path file = Paths.get(sourceDirectory.toString(),sourceFile);
                if(Files.exists(file)){
                    FileReader reader = new FileReader(file.toString());
                    stateMachine = gson.fromJson(reader, StateMachine.class);
                    stateMachine.setPath(file);
                    stateMachine.setParent(parent);
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new ParsingException("Could not find file " + sourceFile);
        }
        return stateMachine;
    }



}
