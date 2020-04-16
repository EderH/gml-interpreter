package workflow;

import lombok.Getter;
import lombok.Setter;

public class WeightedEdge extends Edge {

    @Setter
    @Getter
    private double probability;


    public WeightedEdge() {

    }
}
