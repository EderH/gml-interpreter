package debugger;

import lombok.Getter;
import lombok.Setter;

public abstract class DebuggerServerLauncher {

    @Setter
    @Getter
    public Debugger debugger;

    public DebuggerServerLauncher() {
    }

    public DebuggerServerLauncher(Debugger debugger) {
        this.debugger = debugger;
    }

    public void start(int port) {

        run(port);
    }

    protected abstract void run(int port);

    public abstract void shutdown();
}
