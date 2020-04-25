package debugger;

public class StateMachineExampleDebuggerServerLauncher extends DefaultDebuggerServerLauncher {

    public static void main(String[] args) {
        DebuggerServerLauncher launcher;

        launcher = new DefaultDebuggerServerLauncher(new StateMachineDefaultDebugger());
        launcher.start(5056);

    }
}
