package debugger;

import utils.DebuggerUtils;

public interface IDebugger {

    void processClientCommand(DebuggerUtils.DebugAction action, String dataInput);
}
