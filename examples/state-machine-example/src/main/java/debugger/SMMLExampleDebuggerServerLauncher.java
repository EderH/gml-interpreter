package debugger;

import launcher.DebuggerServerLauncher;
import launcher.DefaultDebuggerServerLauncher;

public class SMMLExampleDebuggerServerLauncher extends DefaultDebuggerServerLauncher {

    public static void main(String[] args) {
        DebuggerServerLauncher launcher;

        launcher = new DefaultDebuggerServerLauncher(new SMMLDebuggerModule());
        launcher.start(5057);

    }
}
