package debugger;

import gml.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ParsingGraph {

    @Setter
    @Getter
    private LinkedList<Element> linkedList;
    private Edge currentEdge;
    private Element currentElement;
    @Setter
    @Getter
    private Graph graph;

    public ParsingGraph(Graph graph) {
        this.graph = graph;
        this.linkedList = new LinkedList<>();
    }

    public Element parseNextElement() throws ParsingException {
        if (graph.getTaskList().isEmpty()) {
            throw new ParsingException("There is no Task Element in Diagram");
        }
        if (currentElement == null) {
            currentElement = graph.getTaskList().get(0);
        } else {
            if (!(currentElement instanceof Edge)) {
                currentEdge = findEdgeWithSource(currentElement, graph);
                if (currentEdge != null) {
                    currentElement = currentEdge;
                } else {
                    return null;
                }
            } else {
                Element element;
                if( (element = findElementWithTarget(currentEdge, graph)) != null) {
                    currentElement = element;
                } else {
                    throw new ParsingException("No source element found for edge " + currentEdge.getId());
                }
            }
        }
        if(currentElement instanceof Task) {
            linkedList.add(currentElement);
        }
        return currentElement;
    }

    private Edge findEdgeWithSource(Element source, Graph graph) {
        List<Edge> sourceList = new ArrayList<>();
        for (int i = 0; i < graph.getEdgeList().size(); i++) {
            Edge edge = graph.getEdgeList().get(i);
            if (edge.getSourceID().equals(source.getId())) {
                sourceList.add(edge);
            }
        }
        if (sourceList.size() > 1) {
            return processWeightedEdge(sourceList);
        } else if (sourceList.size() == 1) {
            return sourceList.get(0);
        } else {
            return null;
        }
    }

    private Element findElementWithTarget(Edge target, Graph graph) {
        for (int i = 0; i < graph.getElementList().size(); i++) {
            Element element = graph.getElementList().get(i);
            if (element.getId().equals(target.getTargetID())) {
                return element;
            }
        }
        return null;
    }

    private Edge processWeightedEdge(List<Edge> sourceList) {
        double random = Math.random();
        WeightedEdge firstEdge = (WeightedEdge) sourceList.get(0);
        WeightedEdge secondEdge = (WeightedEdge) sourceList.get(1);
        if (firstEdge.getProbability() > secondEdge.getProbability()) {
            WeightedEdge temp = firstEdge;
            firstEdge = secondEdge;
            secondEdge = temp;
        }
        if (random < firstEdge.getProbability()) {
            return firstEdge;
        } else if (random < secondEdge.getProbability()) {
            return secondEdge;
        } else {
            return firstEdge;
        }
    }
}
