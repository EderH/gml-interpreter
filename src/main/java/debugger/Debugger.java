package debugger;

import gml.Element;
import gml.Task;
import interpret.Interpreter;
import lombok.Getter;
import lombok.Setter;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class Debugger {

    @Getter
    @Setter
    public static Debugger mainInstance;

    private boolean continueExc;
    private boolean steppingIn;
    private boolean endOfFile;
    private Element currentElement;
    private Element previousElement;
    private ParsingJson parsingJson;
    private ClientHandler clientHandler;
    private Map<String, Breakpoint> breakpoints;
    private Stack<LinkedList<Element>> elementsStack;
    private Stack<Element> parentStack;



    public Debugger(ClientHandler clientHandler) {
        this.elementsStack = new Stack<>();
        this.parentStack = new Stack<>();
        this.clientHandler = clientHandler;
        this.breakpoints = new HashMap<>();
    }

    public void processClientCommand(DebuggerUtils.DebugAction action, String data) {
        DebuggerUtils.DebugAction responseToken = action;
        String response = "";

        if (action == DebuggerUtils.DebugAction.FILE) {

            parsingJson = new ParsingJson();
            elementsStack.push(parsingJson.parse(data));
            currentElement = elementsStack.peek().getFirst();

        } else if (action == DebuggerUtils.DebugAction.SET_BP) {

            setBreakpoints(data);
            return;

        } else if (action == DebuggerUtils.DebugAction.CONTINUE) {

            continueExc = true;
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STEP_IN) {

            if( currentElement instanceof Task && (!(((Task) currentElement).getSubTasks().isEmpty()))) {
                steppingIn = true;
                continueExc = false;
                elementsStack.push(((Task) currentElement).getSubTasks());
                parentStack.push(currentElement);
            }
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STEP_OUT) {

            continueExc = false;
            if(!parentStack.empty()) {
                elementsStack.pop();
                currentElement = parentStack.pop();
            }
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STACK) {

            response = getStack();

        } else if (action == DebuggerUtils.DebugAction.VARS) {

            response = getVariables();

        } else if (action == DebuggerUtils.DebugAction.STEP) {
            continueExc = false;
        } else {
            System.out.println("Unknown command: " + action.toString());
            return;
        }

        if (action == DebuggerUtils.DebugAction.STEP) {
            responseToken = DebuggerUtils.DebugAction.STEP;
            if ( elementsStack.isEmpty()) {
                responseToken = DebuggerUtils.DebugAction.END;
            } else {
                //TODO: check if step in or not
                /*if (debuggerStack.size() > 0) {
                    //Debugger stepIn = debuggerStack.
                    return;
                }*/

                execute();
                response = currentElement.getId() + "\n";

                String vars = getVariables();
                int varsCount = vars.equals("") ? 0 : vars.split("\n").length;
                    response += varsCount + "\n";
                    response += vars;

                String stack = getStack();
                response += stack;

                if (elementsStack.size() == 0) {
                    responseToken = DebuggerUtils.DebugAction.END;
                } else {
                    getNextElement(elementsStack.peek());
                }
            }
        }

        if (action == DebuggerUtils.DebugAction.END) {
            responseToken = DebuggerUtils.DebugAction.END;
        }

        response = DebuggerUtils.responseToken(responseToken) + response;

        /*try {
            if(!response.equals("none")) {
                session.getBasicRemote().sendText(response);
            }
            if(responseToken == DebuggerUtils.DebugAction.END) {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "End of file"));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/

        clientHandler.sendBack(response);

    }

    //TODO: Maybe change to StringBuilder
    private String getStack(){
        return currentElement.getId();
    }

    //TODO: Maybe change to StringBuilder
    private String getVariables() {
        String vars = "";
        try {
            if(currentElement instanceof Task) {
                Task task = (Task) currentElement;
                Field[] fields = task.getClass().getDeclaredFields();
                for (Field field : fields) {
                    String type = field.getType().toString();
                    field.setAccessible(true);
                    String value = field.get(task).toString();
                    field.setAccessible(false);
                    String name = field.getName();
                    // TODO local or global value
                    String var = "" + name + ":" + "0" + ":" + type + ":" + value;
                    vars += var + "\n";
                }
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return vars;
    }

    private void setBreakpoints(String data) {
        System.out.println(data);
        String[] parts = data.split("[|]+");
        for (String s : parts) {
            Breakpoint breakpoint = new Breakpoint(s);
            breakpoints.put(s, breakpoint);
        }
    }


    public boolean actionAvailable(DebuggerUtils.DebugAction action) {
        return (action == DebuggerUtils.DebugAction.FILE || action == DebuggerUtils.DebugAction.SET_BP);
    }


    private void getNextElement(LinkedList<Element> elements) {
        int currentIndex = 0;
        if (previousElement == null) {
            currentElement = elements.get(currentIndex);
        } else {
            currentIndex = elements.indexOf(previousElement) + 1;
            if (currentIndex < elements.size()) {
                currentElement = elements.get(currentIndex);
            } else {
                elementsStack.pop();
                if(!parentStack.empty()) {
                    currentElement = parentStack.pop();
                } else {
                    endOfFile = true;
                    continueExc = false;
                }
            }
        }
    }

    private boolean checkBreakpoint(Element element) {
        if (!breakpoints.isEmpty() && breakpoints.containsKey(element.getId())) {
            Breakpoint breakpoint = breakpoints.get(element.getId());
            if (breakpoint.getHitCount() < 1) {
                breakpoint.setHitCount(1);
                return true;
            }
        }
        return false;
    }

    private void execute() {
        if(currentElement == null || endOfFile) {
            return;
        }
        if(checkBreakpoint(currentElement)) {
            return;
        }

        if (currentElement instanceof Task && (!(((Task) currentElement).getSubTasks().isEmpty()))) {
            if(!steppingIn) {
                for (Element e : ((Task) currentElement).getSubTasks()) {
                    e.accept(new Interpreter());
                }
            }
        } else {
            currentElement.accept(new Interpreter());
        }
        previousElement = currentElement;

        if (continueExc) {
            getNextElement(elementsStack.peek());
            execute();
        }
    }

}
