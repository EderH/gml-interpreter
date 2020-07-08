package debugger;

import graph.GElement;
import launcher.ClientHandler;
import lombok.Getter;
import lombok.Setter;
import parser.ParsingGraph;
import utils.DebuggerUtils;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public abstract class DefaultDebugger {

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
    @Getter
    @Setter
    private boolean continueExc;
    @Getter
    @Setter
    private Map<String, HashMap<String, Breakpoint>> breakpoints;
    @Getter
    @Setter
    private Stack<ParsingGraph> parsingGraphs;

    @Inject
    public DefaultDebugger() {
        this.breakpoints = new HashMap<>();
        this.parsingGraphs = new Stack<>();
    }

    public abstract void processClientCommand(DebuggerUtils.DebugAction action, String dataInput);

    public void sendBack(DebuggerUtils.DebugAction responseToken, String response) {
        getClientHandler().sendBack(DebuggerUtils.responseToken(responseToken) + response);
    }

    public void setBreakpoints(String data) {
        if (!data.isEmpty()) {
            String[] parts = data.split("[|]+");
            String file = parts[0];
            HashMap<String, Breakpoint> bpMap = new HashMap<>();
            for (int i = 1; i < parts.length; i++) {
                bpMap.put(parts[i], new Breakpoint(parts[i]));
            }
            breakpoints.put(file, bpMap);
        } else {
            breakpoints.clear();
        }
    }

    public boolean checkBreakpoint(GElement element) {
        String filename = getFullPathOfCurrentGraph().getFileName().toString();
        if (!breakpoints.isEmpty() && breakpoints.containsKey(filename) && breakpoints.get(filename).containsKey(element.getId())) {
            Breakpoint breakpoint = breakpoints.get(filename).get(element.getId());
            if (breakpoint.getHitCount() == 0) {
                breakpoint.increaseHitCount();
                return true;
            }
        }
        return false;
    }


    public Path getFullPathOfCurrentGraph() {
        return parsingGraphs.peek().getGraph().getPath();
    }
}
