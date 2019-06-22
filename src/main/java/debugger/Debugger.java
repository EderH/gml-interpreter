package debugger;

import gml.Element;
import gml.Task;
import interpret.Interpreter;
import lombok.Getter;
import lombok.Setter;
import sun.awt.image.ImageWatched;

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
            //TODO: Get FILE FROM Client
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
            //TODO: return Stack
            response = "STACK";
        } else if (action == DebuggerUtils.DebugAction.VARS) {
            //TODO: return Variables from state...
            response = "VARS";
        } else if (action == DebuggerUtils.DebugAction.STEP) {
            continueExc = false;
        } else {
            System.out.println("Unknown command: " + action.toString());
            return;
        }

        if (action == DebuggerUtils.DebugAction.STEP) {
            if ( elementsStack.peek().size() == 0) {
                responseToken = DebuggerUtils.DebugAction.END;
            } else {
                //TODO: check if step in or not
                /*if (debuggerStack.size() > 0) {
                    //Debugger stepIn = debuggerStack.
                    return;
                }*/

                execute();

                if (elementsStack.size() == 0) {
                    responseToken = DebuggerUtils.DebugAction.END;
                } else {
                    getNextElement(elementsStack.peek());
                }
            }
        }
        response = DebuggerUtils.responseToken(responseToken) + response;
        clientHandler.sendBack(response);
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
