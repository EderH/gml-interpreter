package debugger;

import launcher.DebuggerServerLauncher;
import launcher.DefaultDebuggerServerLauncher;

public class StateMachineExampleDebuggerServerLauncher extends DefaultDebuggerServerLauncher {

    public static void main(String[] args) {
        DebuggerServerLauncher launcher;

        launcher = new DefaultDebuggerServerLauncher(new StateMachineDebuggerModule());
        launcher.start(5056);

    }
}
