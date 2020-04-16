package parser;

import debugger.ParsingException;
import gml.GElement;
import lombok.Getter;
import lombok.Setter;
import statemachine.StateMachine;
import statemachine.StateNode;
import statemachine.Transition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ParsingGraph {

    @Setter
    @Getter
    private LinkedList<GElement> linkedList;
    private Transition currentEdge;
    @Getter
    @Setter
    private GElement currentElement;
    @Setter
    @Getter
    private StateMachine stateMachine;

    public ParsingGraph(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
        this.linkedList = new LinkedList<>();
    }

    public GElement parseNextElement() throws ParsingException {
        if (stateMachine.getStateList().isEmpty()) {
            throw new ParsingException("There is no TaskNode GElement in Diagram");
        }
        if (currentElement == null) {
            currentElement = getInitialElement();
        } else {
            if (!(currentElement instanceof Transition)) {
                currentEdge = findEdgeWithSource(currentElement);
                if (currentEdge != null) {
                    currentElement = currentEdge;
                } else {
                    return null;
                }
            } else {
                GElement GElement;
                if ((GElement = findElementWithTarget(currentEdge)) != null) {
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

    private GElement getInitialElement(){
        for (StateNode state: stateMachine.getStateList()) {
            if(state.getStateTyp().equals("initialState")) {
                return state;
            }
        }
        return null;
    }

    private Transition findEdgeWithSource(GElement source) throws ParsingException {
        List<Transition> sourceList = new ArrayList<>();
        for (int i = 0; i < stateMachine.getTransitionList().size(); i++) {
            Transition transition = stateMachine.getTransitionList().get(i);
            if (transition.getSourceID().equals(source.getId())) {
                sourceList.add(transition);
            }
        }
        if (sourceList.size() > 1) {
            return processEvent(sourceList);
        } else if (sourceList.size() == 1) {
            return sourceList.get(0);
        } else {
            return null;
        }
    }

    private GElement findElementWithTarget(Transition target) {
        for (int i = 0; i < stateMachine.getElementList().size(); i++) {
            GElement element = stateMachine.getElementList().get(i);
            if (element.getId().equals(target.getTargetID())) {
                return element;
            }
        }
        return null;
    }

    private Transition processEvent(List<Transition> sourceList) throws ParsingException {


        return null;
    }

   /* private Transition processDecision(List<Transition> sourceList) throws ParsingException {
        if (currentElement instanceof StateNode || (currentElement instanceof ActivityNode && ((ActivityNode) currentElement).getNodeType().equals("mergeNode"))) {
            throw new ParsingException("Point of diversion is not allowed at element: " + currentElement.getId() + "! Use decision node instead!");
        }

        WeightedRandomBag<WeightedEdge> bag = new WeightedRandomBag<WeightedEdge>();
        boolean weightedDecision = true;
        for (Edge edge : sourceList) {
            if(edge instanceof WeightedEdge) {
                WeightedEdge weightedEdge = (WeightedEdge) edge;
                bag.addEntry(weightedEdge, weightedEdge.getProbability());
            } else {
                weightedDecision = false;
               // throw new ParsingException("Decision node " + currentElement.getId() + " requires weighted Edge!");

            }
        }
        if(!weightedDecision) {
            if (currentElement instanceof ActivityNode) {
                for (Edge edge : sourceList) {
                    if (edge.isStatement() && ((ActivityNode) currentElement).isStatement() || !edge.isStatement() && !((ActivityNode) currentElement).isStatement()) {
                        return edge;
                    }
                }
            }
        }
            return bag.getRandom();

    }*/
}
