package debugger;

import gml.GElement;
import lombok.Getter;
import lombok.Setter;
import utils.DebuggerUtils;

public abstract class Debugger implements IDebugger{

    @Setter
    @Getter
    private ClientHandler clientHandler;
    @Setter
    @Getter
    private boolean steppingIn;
    @Setter
    @Getter
    private boolean steppingOut;
    @Setter
    @Getter
    private boolean endOfFile;
    @Setter
    @Getter
    private GElement currentElement;

    @Override
    public void processClientCommand(DebuggerUtils.DebugAction action, String dataInput) {

    }
}
