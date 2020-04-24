package debugger;

public class WorkflowExampleDebuggerServerLauncher extends DefaultDebuggerServerLauncher {

    public static void main(String[] args) {
        DebuggerServerLauncher launcher;

        launcher = new DefaultDebuggerServerLauncher(new WorkflowDebugger());
        launcher.start(5056);

    }
}
