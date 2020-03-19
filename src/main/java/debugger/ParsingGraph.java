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
    private LinkedList<Element> linkedList = new LinkedList<>();
    private Edge currentEdge;
    private Element currentElement;
    private Graph graph;

    public ParsingGraph(Graph graph) {
        this.graph = graph;
    }

    public Element parseNextElement() {
        if(graph.getTaskList().size() > 0) {
            if(currentElement == null) {
                currentElement = graph.getTaskList().get(0);
                linkedList.add(graph.getTaskList().get(0));
            } else {
                if(currentElement instanceof Task || currentElement instanceof Node) {
                    List<Edge> sourceList = findEdgeWithSource(currentElement, graph);
                    if (sourceList.size() > 1) {
                        currentEdge = processWeightedEdge(sourceList);
                    } else if (sourceList.size() == 1) {
                        currentEdge = sourceList.get(0);
                    } else {
                        return null;
                    }
                    linkedList.add(currentEdge);
                    currentElement = currentEdge;
                } else {
                    currentElement = findElementWithTarget(currentEdge, graph);
                    linkedList.add(currentElement);
                }
            }
        } else {
            // TODO: throw error when no Task in diagram
        }
        return currentElement;
    }

    private List<Edge> findEdgeWithSource(Element source, Graph graph) {
        List<Edge> sourceList = new ArrayList<>();
        for (int i = 0; i < graph.getEdgeList().size(); i++) {
            Edge edge = graph.getEdgeList().get(i);
            if (edge.getSourceID().equals(source.getId())) {
                sourceList.add(edge);
            }
        }
        return sourceList;
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
        WeightedEdge firstEdge = (WeightedEdge)sourceList.get(0);
        WeightedEdge secondEdge = (WeightedEdge)sourceList.get(1);
        if(firstEdge.getProbability() > secondEdge.getProbability()) {
            WeightedEdge temp = firstEdge;
            firstEdge = secondEdge;
            secondEdge = temp;
        }
        if(random < firstEdge.getProbability()) {
            return firstEdge;
        } else if( random < secondEdge.getProbability()) {
            return secondEdge;
        } else {
            return firstEdge;
        }
    }
}
