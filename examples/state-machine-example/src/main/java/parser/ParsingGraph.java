package parser;

import launcher.ClientHandler;
import debugger.StateMachineDebugger;
import debugger.ParsingException;
import graph.GElement;
import graph.GGraph;
import lombok.Getter;
import lombok.Setter;
import statemachine.EventFlowEntry;
import statemachine.StateMachine;
import statemachine.StateNode;
import statemachine.Transition;
import utils.DebuggerUtils;

import java.io.IOException;
import java.util.*;

public class ParsingGraph {

    @Setter
    @Getter
    private LinkedList<GElement> linkedList;
    private Transition currentEdge;
    @Getter
    @Setter
    private GElement currentElement;
    @Getter
    @Setter
    private LinkedList<EventFlowEntry> eventFlow;
    @Setter
    @Getter
    private GGraph stateMachine;
    private StateMachineDebugger stateMachineDebugger;

    public ParsingGraph(StateMachine stateMachine, StateMachineDebugger stateMachineDebugger) {
        this.stateMachine = stateMachine;
        this.linkedList = new LinkedList<>();
        this.eventFlow = new LinkedList<>();
        this.stateMachineDebugger = stateMachineDebugger;
    }

    public GElement parseNextElement() throws ParsingException {
        if (((StateMachine)stateMachine).getStateList().isEmpty()) {
            throw new ParsingException("There is no TaskNode GElement in Diagram");
        }
        if (currentElement == null) {
            currentElement = getInitialElement();
        } else {
            if (!(currentElement instanceof Transition)) {
                currentEdge = findEdgeBySourceID(currentElement);
                if (currentEdge != null) {
                    currentElement = currentEdge;
                } else {
                    return null;
                }
            } else {
                GElement GElement;
                if ((GElement = findElementByTargetID(currentEdge)) != null) {
                    currentElement = GElement;
                } else {
                    throw new ParsingException("No source GElement found for edge " + currentEdge.getId());
                }
            }
        }
        if (currentElement instanceof StateNode) {
            linkedList.add(currentElement);
        }
        return currentElement;
    }

    private GElement getInitialElement() {
        for (StateNode state : ((StateMachine)stateMachine).getStateList()) {
            if (state.getStateTyp().equals("initialState")) {
                return state;
            }
        }
        return null;
    }

    private HashMap<String,Transition> getTargetList(StateMachine stateMachine, GElement source) {
        HashMap<String,Transition> targetList = new HashMap<>();
        for (int i = 0; i < stateMachine.getTransitionList().size(); i++) {
            Transition transition = stateMachine.getTransitionList().get(i);
            if (transition.getSourceID().equals(source.getId())) {
                targetList.put(transition.getId(), transition);
            }
        }
        return targetList;
    }

    private Transition findEdgeBySourceID(GElement source) throws ParsingException {
        LinkedHashMap<String, Transition> targetList = new LinkedHashMap<>();
            ListIterator iterator = stateMachineDebugger.getParsingGraphs().listIterator(stateMachineDebugger.getParsingGraphs().size());
            StateNode node = (StateNode)source;
            while (iterator.hasPrevious()){
                ParsingGraph previous = (ParsingGraph)iterator.previous();
                if(node != null) {

                    targetList.putAll(getTargetList(((StateMachine)previous.stateMachine), node));
                }
                node = ((StateMachine)previous.stateMachine).getParent();
            }
        Transition transition = null;
        if (targetList.size() > 1) {
            transition = processEvent(targetList);
            if(transition != null) {
                eventFlow.add(new EventFlowEntry(((StateNode)source).getLabel(), transition.getEvent()));
            }
        } else if (targetList.size() == 1) {
            transition = targetList.entrySet().iterator().next().getValue();
            eventFlow.add(new EventFlowEntry(((StateNode)source).getLabel(), transition.getEvent()));
        }
        return transition;
    }


    private GElement findElementByTargetID(Transition target) {
        for (int i = 0; i < stateMachine.getElementList().size(); i++) {
            GElement element = ((StateMachine)stateMachine).getElementList().get(i);
            if (element.getId().equals(target.getTargetID())) {
                return element;
            }
        }
        return null;
    }

    private Transition processEvent(HashMap<String, Transition> sourceList) throws ParsingException {


        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String,Transition> entry : sourceList.entrySet()) {
            if (entry.getValue().getEvent().isEmpty()) {
                stringBuilder.append("default");
            } else {
                stringBuilder.append(entry.getValue().getEvent());
            }
            stringBuilder.append("\n");
        }
        String triggers = stringBuilder.toString();
        int triggersCount = triggers.equals("") ? 0 : triggers.split("\n").length;
        result.append(triggersCount);
        result.append("\n");
        result.append(triggers);
        stateMachineDebugger.sendBack(DebuggerUtils.DebugAction.EVENT, result.toString());

        ClientHandler clientHandler = stateMachineDebugger.getClientHandler();
        try {
            String dataInput = clientHandler.getBr().readLine();
            int index = dataInput.indexOf("|");
            String data = "";
            if (index >= 0) {
                data = (index < dataInput.length() - 1 ? dataInput.substring(index + 1) : "").trim();
            }
            for (Map.Entry<String,Transition> entry : sourceList.entrySet()) {
                if (entry.getValue().getEvent().equals(data)) {
                    return entry.getValue();
                }
            }
            System.out.println(dataInput);
        } catch (IOException ex) {
            ex.printStackTrace();
            clientHandler.disconnect();
        }
        return null;
    }
}
