package debugger;

import launcher.DebuggerServerLauncher;
import launcher.DefaultDebuggerServerLauncher;

public class WorkflowExampleDebuggerServerLauncher extends DefaultDebuggerServerLauncher {

    public static void main(String[] args) {
        DebuggerServerLauncher launcher;

        launcher = new DefaultDebuggerServerLauncher(new WorkflowDebuggerModule());
        launcher.start(5056);

    }
}
