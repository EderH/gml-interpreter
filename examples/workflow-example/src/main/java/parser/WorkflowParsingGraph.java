package parser;

import debugger.ParsingException;
import gml.GElement;
import gml.GGraph;
import lombok.Getter;
import lombok.Setter;
import utils.WeightedRandomBagUtil;
import workflow.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WorkflowParsingGraph extends ParsingGraph{

    @Setter
    @Getter
    private LinkedList<GElement> linkedList;
    private Edge currentEdge;
    @Getter
    @Setter
    private GElement currentElement;
    @Setter
    @Getter
    private GGraph graph;

    public WorkflowParsingGraph(Graph graph) {
        this.graph = graph;
        this.linkedList = new LinkedList<>();
    }

    public GElement parseNextElement() throws ParsingException {
        if (((Graph)graph).getNodeList().isEmpty()) {
            throw new ParsingException("There is no TaskNode GElement in Diagram");
        }
        if (currentElement == null) {
            currentElement = ((Graph)graph).getNodeList().get(0);
        } else {
            if (!(currentElement instanceof Edge)) {
                currentEdge = findEdgeWithSource(currentElement, ((Graph)graph));
                if (currentEdge != null) {
                    currentElement = currentEdge;
                } else {
                    return null;
                }
            } else {
                GElement GElement;
                if ((GElement = findElementWithTarget(currentEdge, ((Graph)graph))) != null) {
                    currentElement = GElement;
                } else {
                    throw new ParsingException("No source GElement found for edge " + currentEdge.getId());
                }
            }
        }
        if (currentElement instanceof TaskNode) {
            linkedList.add(currentElement);
        }
        return currentElement;
    }

    private Edge findEdgeWithSource(GElement source, Graph graph) throws ParsingException {
        List<Edge> sourceList = new ArrayList<>();
        for (int i = 0; i < graph.getEdgeList().size(); i++) {
            Edge edge = graph.getEdgeList().get(i);
            if (edge.getSourceID().equals(source.getId())) {
                sourceList.add(edge);
            }
        }
        if (sourceList.size() > 1) {
            return processDecision(sourceList);
        } else if (sourceList.size() == 1) {
            return sourceList.get(0);
        } else {
            return null;
        }
    }

    private GElement findElementWithTarget(Edge target, Graph graph) {
        for (int i = 0; i < graph.getElementList().size(); i++) {
            GElement element = graph.getElementList().get(i);
            if (element.getId().equals(target.getTargetID())) {
                return element;
            }
        }
        return null;
    }

    private Edge processDecision(List<Edge> sourceList) throws ParsingException {
        if (currentElement instanceof TaskNode || (currentElement instanceof ActivityNode && ((ActivityNode) currentElement).getNodeType().equals("mergeNode"))) {
            throw new ParsingException("Point of diversion is not allowed at element: " + currentElement.getId() + "! Use decision node instead!");
        }

        WeightedRandomBagUtil<WeightedEdge> bag = new WeightedRandomBagUtil<WeightedEdge>();
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

    }
}



