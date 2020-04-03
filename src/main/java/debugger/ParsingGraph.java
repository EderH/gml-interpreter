package debugger;

import gml.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ParsingGraph {

    @Setter
    @Getter
    private LinkedList<Element> linkedList;
    private Edge currentEdge;
    @Getter
    @Setter
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
                if ((element = findElementWithTarget(currentEdge, graph)) != null) {
                    currentElement = element;
                } else {
                    throw new ParsingException("No source element found for edge " + currentEdge.getId());
                }
            }
        }
        if (currentElement instanceof Task) {
            linkedList.add(currentElement);
        }
        return currentElement;
    }

    private Edge findEdgeWithSource(Element source, Graph graph) throws ParsingException {
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

    private Edge processWeightedEdge(List<Edge> sourceList) throws ParsingException {
        if (currentElement instanceof Task || (currentElement instanceof Node && ((Node) currentElement).getNodeType().equals("mergeNode"))) {
            throw new ParsingException("Point of diversion is not allowed at element: " + currentElement.getId() + "! Use decision node instead!");
        }

        WeightedRandomBag<WeightedEdge> bag = new WeightedRandomBag<WeightedEdge>();
        for (Edge edge: sourceList) {
            if(edge instanceof WeightedEdge) {
                WeightedEdge weightedEdge = (WeightedEdge) edge;
                bag.addEntry(weightedEdge, weightedEdge.getProbability());
            } else {
                throw new ParsingException("Decision node " + currentElement.getId() + " requires weighted edge!");
            }
        }
        return bag.getRandom();
    }
}



class WeightedRandomBag<T extends Object> {

    private class Entry {
        double accumulatedWeight;
        T object;
    }

    private List<Entry> entries = new ArrayList<>();
    private double accumulatedWeight;
    private Random rand = new Random();

    public void addEntry(T object, double weight) {
        accumulatedWeight += weight;
        Entry e = new Entry();
        e.object = object;
        e.accumulatedWeight = accumulatedWeight;
        entries.add(e);
    }

    public T getRandom() {
        double r = rand.nextDouble() * accumulatedWeight;

        for (Entry entry: entries) {
            if (entry.accumulatedWeight >= r) {
                return entry.object;
            }
        }
        return null;
    }
}
