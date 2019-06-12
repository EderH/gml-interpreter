package gml;

import interpret.ElementVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Graph extends Element{
    @Setter
    @Getter
    private List<Task> taskList;
    @Setter
    @Getter
    private List<Edge> edgeList;
    @Getter
    @Setter
    private List<Element> elementList;

    public Graph() {

    }

    @Override
    public void accept(ElementVisitor elementVisitor) {
        elementVisitor.visit(this);
    }

    public LinkedList<Element> transformGraphToList() {
        Task firstTask = taskList.get(0);
        LinkedList<Element> linkedList = new LinkedList<>();
        linkedList.add(firstTask);
        Element currentElement = firstTask;
        while (true) {
            List<Edge> sourceList = findEdgeWithSource(currentElement);
            Edge currentEdge;
            if(sourceList.size() > 1) {
                currentEdge = processWeightedEdge(sourceList);
            } else if( sourceList.size() == 1) {
                currentEdge = sourceList.get(0);
            } else {
                break;
            }
            linkedList.add(currentEdge);
            currentElement = findElementWithTarget(currentEdge);
            linkedList.add(currentElement);

        }
        return linkedList;
    }

    private List<Edge> findEdgeWithSource(Element source) {
        List<Edge> sourceList = new ArrayList<>();
        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);
            if (edge.getSourceID().equals(source.getID())) {
                sourceList.add(edge);
            }
        }
        return sourceList;
    }

    private Element findElementWithTarget(Edge target) {
        for (int i = 0; i < elementList.size(); i++) {
            Element element = elementList.get(i);
            if (element.getID().equals(target.getTargetID())) {
                return element;
            }
        }
        return null;
    }

    private Edge processWeightedEdge(List<Edge> sourceList) {
        double random = Math.random();
        WeightedEdge firstEdge = (WeightedEdge)sourceList.get(0);
        WeightedEdge secondEdge = (WeightedEdge)sourceList.get(1);
        if(random < firstEdge.getProbability()) {
            return firstEdge;
        } else if( random < secondEdge.getProbability()) {
            return secondEdge;
        } else {
            return firstEdge;
        }
    }

}
