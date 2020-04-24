package debugger;

import gml.GElement;
import lombok.Getter;
import lombok.Setter;
import parser.ParsingGraph;
import parser.ParsingJson;
import utils.DebuggerUtils;
import utils.ReflectionUtil;
import workflow.Graph;
import workflow.TaskNode;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WorkflowDebugger extends Debugger {



    private boolean continueExc;
    private ParsingJson parsingJson;
    private Map<String, HashMap<String, Breakpoint>> breakpoints;
    private Stack<ParsingGraph> parsingGraphs;


    public WorkflowDebugger() {
        this.breakpoints = new HashMap<>();
        this.parsingGraphs = new Stack<>();
    }

    @Override
    public void processClientCommand(DebuggerUtils.DebugAction action, String dataInput) {
        DebuggerUtils.DebugAction responseToken = action;
        StringBuilder response = new StringBuilder();
        int index = dataInput.indexOf("|");
        String data = "";
        if (index >= 0) {
            data = (index < dataInput.length() - 1 ? dataInput.substring(index + 1) : "").trim();
        }

        if (action == DebuggerUtils.DebugAction.FILE) {
            Path path = Paths.get(data);
            if (path == null) {
                processException(new ParsingException("Path to file not available"));
                return;
            }
            parsingJson = new ParsingJson(path.getParent(), this);
            try {
                Graph graph = parsingJson.deserializeFile(path.getFileName().toString());
                parsingGraphs.push(new ParsingGraph(graph));
            } catch (ParsingException exc) {
                processException(exc);
                return;
            }


        } else if (action == DebuggerUtils.DebugAction.SET_BP) {

            setBreakpoints(data);
            for(String k : breakpoints.keySet()) {
                Map<String, Breakpoint> m1 = breakpoints.get(k);
                System.out.println("File: " + k);
                for(String k1 : m1.keySet()) {
                    System.out.println("breakpoint: " + m1.get(k1).getId());
                }
            }

            return;

        } else if (action == DebuggerUtils.DebugAction.CONTINUE) {

            continueExc = true;
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STEP_IN) {
            setSteppingIn(true);
            continueExc = false;
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STEP_OUT) {
            setSteppingOut(true);
            continueExc = false;
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STACK) {

            response.append(getStack());

        } else if (action == DebuggerUtils.DebugAction.VARS) {

            response.append(getVariables());

        } else if (action == DebuggerUtils.DebugAction.STEP) {
            continueExc = false;
        } else {
            System.out.println("Unknown command: " + action.toString());
            return;
        }

        if (action == DebuggerUtils.DebugAction.STEP) {
            responseToken = DebuggerUtils.DebugAction.STEP;

            if (getCurrentElement() instanceof TaskNode && (((TaskNode) getCurrentElement()).getSubGraph() != null)) {
                parsingGraphs.push(new ParsingGraph(((TaskNode) getCurrentElement()).getSubGraph()));
                if (!isSteppingIn()) {
                    if (!executeBlock()) {
                        return;
                    }
                }
            }
            if (isSteppingOut() && parsingGraphs.size() > 1) {
                parsingGraphs.pop();
            }
            try {
                getNextElement();
            } catch (ParsingException exc) {
                processException(exc);
                return;
            }
            if (isEndOfFile()) {
                if (parsingGraphs.size() > 1) {
                    parsingGraphs.pop();
                    setEndOfFile(false);
                    try {
                        getNextElement();
                        executeNextElement();
                        response.append(createResult());
                    } catch (ParsingException exc) {
                        processException(exc);
                        return;
                    }
                } else {
                    responseToken = DebuggerUtils.DebugAction.END;
                }

            } else {

                executeNextElement();
                response.append(createResult());
            }

        }
        sendBack(responseToken, response.toString());
    }

    public boolean executeBlock() {

        while (true) {
            try {
                getNextElement();
            } catch (ParsingException exc) {
                processException(exc);
            }
            if (isEndOfFile()) {
                parsingGraphs.pop();
                setEndOfFile(false);
                return true;
            }
            if (!executeNextElement()) {
                sendBack(DebuggerUtils.DebugAction.STEP, createResult());
                return false;
            }
        }

    }

    public String createResult() {
        StringBuilder response = new StringBuilder();
        String filename = getFullPathOfCurrentGraph().toString();
        response.append(filename);
        response.append("\n");
        response.append(getCurrentElement().getId());
        response.append("\n");

        String vars = getVariables();
        int varsCount = vars.equals("") ? 0 : vars.split("\n").length;
        response.append(varsCount);
        response.append("\n");
        response.append(vars);

        String stack = getStack();
        response.append(stack);
        return response.toString();
    }

    public void sendBack(DebuggerUtils.DebugAction responseToken, String response) {
        getClientHandler().sendBack(DebuggerUtils.responseToken(responseToken) + response);
    }

    //TODO: Maybe change to StringBuilder
    private String getStack() {
        StringBuilder stack = new StringBuilder();
        ListIterator iterator = parsingGraphs.listIterator(parsingGraphs.size());

        while (iterator.hasPrevious()){
            ParsingGraph previous = (ParsingGraph)iterator.previous();
            String filename = previous.getGraph().getPath().getFileName().toString();
            stack.append(filename);
            stack.append("\n");
            stack.append(previous.getCurrentElement().getId());
            stack.append("\n");
        }

        return stack.toString();
    }

    private String getVariables() {
        StringBuilder vars = new StringBuilder();
        try {
                List<Field> fields = ReflectionUtil.getInheritedDeclaredFields(getCurrentElement().getClass(), Object.class);
                for (Field field : fields) {
                    String type = field.getType().toString();
                    field.setAccessible(true);
                    String value = "";
                    if (field.get(getCurrentElement()) != null) {
                        if(field.getType() == Graph.class) {
                            value = ReflectionUtil.getInheritedDeclaredFieldValue(field.get(getCurrentElement()), "id", Object.class).toString() + ".wf";
                        } else {
                            value = field.get(getCurrentElement()).toString();
                        }
                    } else {
                        value = "null";
                    }
                    field.setAccessible(false);
                    String name = field.getName();
                    // TODO local or global value
                    String var = "" + name + ":" + "0" + ":" + type + ":" + value;
                    vars.append(var);
                    vars.append("\n");
            }
        } catch (IllegalAccessException | NoSuchFieldException   ex) {
            ex.printStackTrace();
        }
        return vars.toString();
    }

    public void processException(ParsingException exc) {
        StringBuilder sb = new StringBuilder();
        sb.append("exc\n");
        sb.append(exc.getMessage());
        sb.append("\n");
        String vars = getVariables();
        int varsCount = vars.equals("") ? 0 : vars.split("\n").length;
        sb.append(varsCount);
        sb.append("\n");
        sb.append(vars);

        String stack = getStack();
        sb.append(stack);
        getClientHandler().sendBack(sb.toString());
    }

    private void setBreakpoints(String data) {
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

    private void getNextElement() throws ParsingException {
        ParsingGraph parser = parsingGraphs.peek();
        GElement element = parser.parseNextElement();
        if (element != null) {
            setCurrentElement(element);
        } else {
            setEndOfFile(true);
        }

    }

    private boolean checkBreakpoint(GElement GElement) {
        String filename = getFullPathOfCurrentGraph().getFileName().toString();
        if (!breakpoints.isEmpty() && breakpoints.containsKey(filename) && breakpoints.get(filename).containsKey(GElement.getId())) {
            Breakpoint breakpoint = breakpoints.get(filename).get(GElement.getId());
            if (breakpoint.getHitCount() == 0) {
                breakpoint.increaseHitCount();
                return true;
            }
        }
        return false;
    }

    private Path getFullPathOfCurrentGraph() {
        return parsingGraphs.peek().getGraph().getPath();
    }

    private boolean executeNextElement() {
        if (checkBreakpoint(getCurrentElement())) {
            return false;
        }
        // currentElement.accept(new Interpreter());
        return true;
    }
}
