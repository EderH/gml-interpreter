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
    private Map<String, Breakpoint> breakpoints;

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
            //mainInstance = this;
            Path path = Paths.get(data);
            if (path != null) {
                parsingJson = new ParsingJson(path.getParent());
                mainGraph = parsingJson.deserializeFile(path.getFileName().toString());
                parsingGraphs.push(new ParsingGraph(mainGraph));
            } else {
                return;
            }
        } else if (action == DebuggerUtils.DebugAction.SET_BP) {

            setBreakpoints(data);
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
                steppingIn = true;
            }
            getNextElement();
            steppingIn = false;
            if (endOfFile) {
                if (parsingGraphs.size() > 1) {
                    parsingGraphs.pop();
                    endOfFile = false;
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

    public String createResult() {
        StringBuilder response = new StringBuilder();
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
        stack.append(currentElement.getId());
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
        System.out.println(data);
        if (!data.isEmpty()) {
            String[] parts = data.split("[|]+");
            for (String s : parts) {
                System.out.println("Breakpoint: " + s);
                Breakpoint breakpoint = new Breakpoint(s);
                breakpoints.put(s, breakpoint);
            }
        }
    }


    public boolean actionAvailable(DebuggerUtils.DebugAction action) {
        return (action == DebuggerUtils.DebugAction.FILE || action == DebuggerUtils.DebugAction.SET_BP);
    }


    private void getNextElement() {
        ParsingGraph parser = parsingGraphs.peek();
        if (parser.parseNextElement() != null) {
            if (checkBreakpoint(parser.getLinkedList().peek())) {
                currentElement = parser.getLinkedList().peek();
            } else {
                currentElement = parser.getLinkedList().poll();
            }

        } else {
            endOfFile = true;
        }
    }

    private boolean checkBreakpoint(Element element) {
        if (!breakpoints.isEmpty() && breakpoints.containsKey(element.getId())) {
            Breakpoint breakpoint = breakpoints.get(element.getId());
            if (breakpoint.getHitCount() == 0) {
                breakpoint.increaseHitCount();
                return true;
            }
        }
        return false;
    }

    private void executeNextElement() {
        if (checkBreakpoint(currentElement)) {
            return;
        }
        currentElement.accept(new Interpreter());
    }

}
