package debugger;

import gml.Element;
import gml.Graph;
import gml.Task;
import interpret.Interpreter;
import lombok.Getter;
import lombok.Setter;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Debugger {

    @Getter
    @Setter
    public static Debugger mainInstance;

    private boolean continueExc;
    private boolean steppingIn;
    private boolean steppingOut;
    private boolean endOfFile;
    private Element currentElement;
    private Element previousElement;
    private ParsingJson parsingJson;
    private ClientHandler clientHandler;
    private Map<String, HashMap<String, Breakpoint>> breakpoints;

    private Graph mainGraph;
    private Stack<ParsingGraph> parsingGraphs;
    private Graph currentGraph;


    public Debugger(ClientHandler clientHandler) {
        // mainInstance = Debugger.getMainInstance() == null ? this : Debugger.getMainInstance();
        this.clientHandler = clientHandler;
        this.breakpoints = new HashMap<>();
        this.parsingGraphs = new Stack<>();
    }

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
                mainGraph = parsingJson.deserializeFile(path.getFileName().toString());
            } catch (ParsingException exc) {
                processException(exc);
                return;
            }
            parsingGraphs.push(new ParsingGraph(mainGraph));

        } else if (action == DebuggerUtils.DebugAction.SET_BP) {

            setBreakpoints(data);
            /*for(String k : breakpoints.keySet()) {
                Map<String, Breakpoint> m1 = breakpoints.get(k);
                System.out.println("File: " + k);
                for(String k1 : m1.keySet()) {
                    System.out.println("breakpoint: " + m1.get(k1).getId());
                }
            } */

            return;

        } else if (action == DebuggerUtils.DebugAction.CONTINUE) {

            continueExc = true;
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STEP_IN) {
            steppingIn = true;
            continueExc = false;
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STEP_OUT) {
            steppingOut = true;
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

            if (currentElement instanceof Task && (((Task) currentElement).getSubGraph() != null)) {
                parsingGraphs.push(new ParsingGraph(((Task) currentElement).getSubGraph()));
                if (!steppingIn) {
                    if (!executeBlock()) {
                        return;
                    }
                }
            }
            if (steppingOut && parsingGraphs.size() > 1) {
                parsingGraphs.pop();
            }
            try {
                getNextElement();
            } catch (ParsingException exc) {
                processException(exc);
                return;
            }
            if (endOfFile) {
                if (parsingGraphs.size() > 1) {
                    parsingGraphs.pop();
                    endOfFile = false;
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
            if (endOfFile) {
                parsingGraphs.pop();
                endOfFile = false;
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
        response.append(currentElement.getId());
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
        clientHandler.sendBack(DebuggerUtils.responseToken(responseToken) + response);
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
            if (currentElement instanceof Task) {
                Task task = (Task) currentElement;
                Field[] fields = task.getClass().getDeclaredFields();
                //HashMap<String, Object> fieldsValues = getMemberFields(task);
                for (Field field : fields) {
                    String type = field.getType().toString();
                    field.setAccessible(true);
                    String value = "";
                    if (field.get(task) != null) {
                        value = field.get(task).toString();
                    }
                    field.setAccessible(false);
                    String name = field.getName();
                    // TODO local or global value
                    String var = "" + name + ":" + "0" + ":" + type + ":" + value;
                    vars.append(var);
                    vars.append("\n");
                }
            }
        } catch (IllegalAccessException ex) {
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
        clientHandler.sendBack(sb.toString());
    }

    private static HashMap<String, Object> getMemberFields(Object obj) throws IllegalAccessException {
        HashMap<String, Object> fieldValues = new HashMap<String, Object>();
        if (obj != null) {
            Class<?> objClass = obj.getClass();

            Field[] fields = objClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                fieldValues.put(field.getName(), field.get(obj));
                if (!field.getType().isPrimitive() && !field.getType().getName().contains("java.lang")) {
                    getMemberFields(field.get(obj));
                }
            }
        }
        return fieldValues;
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


    public boolean actionAvailable(DebuggerUtils.DebugAction action) {
        return (action == DebuggerUtils.DebugAction.FILE || action == DebuggerUtils.DebugAction.SET_BP);
    }


    private void getNextElement() throws ParsingException {
        ParsingGraph parser = parsingGraphs.peek();
        Element element = parser.parseNextElement();
        if (element != null) {
            currentElement = element;
        } else {
            endOfFile = true;
        }

    }

    private boolean checkBreakpoint(Element element) {
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

    private Path getFullPathOfCurrentGraph() {
        return parsingGraphs.peek().getGraph().getPath();
    }

    private boolean executeNextElement() {
        if (checkBreakpoint(currentElement)) {
            return false;
        }
        currentElement.accept(new Interpreter());
        return true;
    }
}
