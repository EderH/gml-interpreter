package debugger;

import lombok.Getter;
import lombok.Setter;

public class Breakpoint {

    @Setter
    @Getter
    private String elementID;
    @Setter
    @Getter
    private int hitCount;

    public Breakpoint(String elementID) {
        this.elementID = elementID;
        this.hitCount = 0;
    }

    public void increaseHitCount() {
        this.hitCount++;
    }
}
