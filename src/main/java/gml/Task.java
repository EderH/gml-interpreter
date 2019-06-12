package gml;

import lombok.Getter;
import lombok.Setter;

public abstract class Task extends Element {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private int duration;
    @Setter
    @Getter
    private String taskType;

    public Task() {

    }

    @Override
    public String toString() {
        return "name: " + name;
    }
}
