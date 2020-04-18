package statemachine;

import lombok.Getter;
import lombok.Setter;

public class EventFlowEntry {

    @Setter
    @Getter
    private String element;
    @Setter
    @Getter
    private String event;

    public EventFlowEntry(String element, String event) {
        this.element = element;
        this.event = event;
    }
}
