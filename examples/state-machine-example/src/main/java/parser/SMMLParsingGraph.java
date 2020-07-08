package parser;

import launcher.ClientHandler;
import debugger.SMMLDebugger;
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

public class SMMLParsingGraph extends ParsingGraph {

    @Setter
    @Getter
    private LinkedList<GElement> linkedList;
    @Setter
    @Getter
    private Transition currentEdge;
    @Getter
    @Setter
    private GElement currentElement;
    @Getter
    @Setter
    private LinkedList<EventFlowEntry> eventFlow;
    @Setter
    @Getter
    private GGraph graph;
    private SMMLDebugger debugger;

    public SMMLParsingGraph(StateMachine stateMachine, SMMLDebugger debugger) {
        this.graph = stateMachine;
        this.linkedList = new LinkedList<>();
        this.eventFlow = new LinkedList<>();
        this.debugger = debugger;
    }

    public GElement parseNextElement() throws ParsingException {
        if (((StateMachine) graph).getStateList().isEmpty()) {
            throw new ParsingException("There is no State GElement in Diagram");
        }
        if (currentElement == null) {
            currentElement = getInitialElement();
            if (currentElement == null) {
                throw new ParsingException("Could not find any initial state within the diagram");
            }
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
                    throw new ParsingException("No source GElement found for edge: ID: " + currentEdge.getId() + " Name: " + currentEdge.getEvent());
                }
            }
        }
        if (currentElement instanceof StateNode) {
            linkedList.add(currentElement);
        }
        return currentElement;
    }

    private GElement getInitialElement() {
        for (StateNode state : ((StateMachine) graph).getStateList()) {
            if (state.getStateTyp().equals("initialState")) {
                return state;
            }
        }
        return null;
    }

    private HashMap<String, Transition> getTargetListOfState(StateMachine stateMachine, GElement source) {
        HashMap<String, Transition> targetList = new HashMap<>();
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
        ListIterator iterator = debugger.getParsingGraphs().listIterator(debugger.getParsingGraphs().size());
        StateNode node = (StateNode) source;
        boolean listContainsDefault = false;
        while (iterator.hasPrevious()) {
            SMMLParsingGraph previous = (SMMLParsingGraph) iterator.previous();
            if (node != null) {
                HashMap<String, Transition> targetListOfState = getTargetListOfState(((StateMachine) previous.graph), node);
                for (Map.Entry<String, Transition> target : targetListOfState.entrySet()) {
                    if (!listContainsDefault && target.getValue().getEvent().equals("default")) {
                        targetList.put(target.getKey(), target.getValue());
                        listContainsDefault = true;
                    } else if(!target.getValue().getEvent().equals("default")) {
                        targetList.put(target.getKey(), target.getValue());
                    }
                }
            }
            node = ((StateMachine) previous.graph).getParent();
        }
        Transition transition = null;
        if (targetList.size() > 1) {
            transition = processEvent(targetList);
            if (transition != null) {
                eventFlow.add(new EventFlowEntry(((StateNode) source).getLabel(), transition.getEvent()));
            }
        } else if (targetList.size() == 1) {
            transition = targetList.entrySet().iterator().next().getValue();
            eventFlow.add(new EventFlowEntry(((StateNode) source).getLabel(), transition.getEvent()));
        }
        return transition;
    }


    private GElement findElementByTargetID(Transition target) {
        for (int i = 0; i < graph.getElementList().size(); i++) {
            GElement element = ((StateMachine) graph).getElementList().get(i);
            if (element.getId().equals(target.getTargetID())) {
                return element;
            }
        }
        return null;
    }

    private String getEventsForState(HashMap<String, Transition> sourceList) {
        StringBuilder stringBuilder = new StringBuilder();
        String filename = debugger.getFullPathOfCurrentGraph().getFileName().toString();
        for (Map.Entry<String, Transition> entry : sourceList.entrySet()) {
            String event;
            if (entry.getValue().getEvent().isEmpty()) {
                event = "" + "default";
            } else {
                event = "" + entry.getValue().getEvent();
            }
            String glspEvent = "" + filename + ":" + entry.getValue().getSourceID() + ":" + event;
            stringBuilder.append(glspEvent);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private Transition processEvent(HashMap<String, Transition> sourceList) throws ParsingException {

        StringBuilder result = new StringBuilder();

        String triggers = getEventsForState(sourceList);
        int triggersCount = triggers.equals("") ? 0 : triggers.split("\n").length;
        result.append(triggersCount);
        result.append("\n");
        result.append(triggers);
        debugger.sendBack(DebuggerUtils.DebugAction.EVENT, result.toString());

        ClientHandler clientHandler = debugger.getClientHandler();
        try {
            String dataInput = clientHandler.getBr().readLine();
            int index = dataInput.indexOf("|");
            String data = "";
            if (index >= 0) {
                data = (index < dataInput.length() - 1 ? dataInput.substring(index + 1) : "").trim();
            }
            for (Map.Entry<String, Transition> entry : sourceList.entrySet()) {
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
