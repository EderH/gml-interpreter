package launcher;

import di.DebuggerModule;
import lombok.Getter;
import lombok.Setter;

public abstract class DebuggerServerLauncher {

    @Setter
    @Getter
    public DebuggerModule module;


    public DebuggerServerLauncher() {
    }

    public DebuggerServerLauncher(DebuggerModule module) {
        this.module = module;
    }

    public void start(int port) {
        run(port);
    }

    protected abstract void run(int port);

    public abstract void shutdown();
}
