package debugger;

public class Debugger {

    private boolean continueExc;
    private boolean steppingIn;
    private boolean steppingOut;
    private boolean executing;

    public Debugger() {

    }

    public void processClientCommands(String data) {

        DebuggerUtils.DebugAction action = DebuggerUtils.stringToAction(data);

        if(action == DebuggerUtils.DebugAction.NEXT) {

        }
    }


}
