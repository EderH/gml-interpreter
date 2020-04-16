package debugger;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class ParsingException extends IOException {

    @Setter
    @Getter
    private String message;

    public ParsingException(String message) {
        this.message = message;
    }
}
