package debugger;

import lombok.Getter;
import lombok.Setter;

public class Breakpoint {

    @Setter
    @Getter
    private String id;
    @Setter
    @Getter
    private int hitCount;

    public Breakpoint(String name) {
        this.id = name;
        this.hitCount = 0;
    }

    public void increaseHitCount() {
        this.hitCount++;
    }
}
