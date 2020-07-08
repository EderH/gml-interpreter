package debugger;

import graph.GElement;
import interpreter.StateMachineInterpreter;
import parser.ParsingGraph;
import parser.SMMLParsingGraph;
import parser.SMMLParsingJson;
import statemachine.EventFlowEntry;
import statemachine.StateMachine;
import statemachine.StateNode;
import statemachine.Transition;
import utils.DebuggerUtils;
import utils.ReflectionUtil;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SMMLDebugger extends DefaultDebugger {

    private SMMLParsingJson parsingJson;

    public SMMLDebugger() {
        super();
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
            parsingJson = new SMMLParsingJson(path.getParent(), this);
            try {
                StateMachine stateMachine = parsingJson.deserializeFile(path.getFileName().toString(), null);
                getParsingGraphs().push(new SMMLParsingGraph(stateMachine, this));
            } catch (ParsingException exc) {
                processException(exc);
                return;
            }


        } else if (action == DebuggerUtils.DebugAction.SET_BP) {

            setBreakpoints(data);
            return;

        } else if (action == DebuggerUtils.DebugAction.CONTINUE) {

            setContinueExc(true);
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STEP_IN) {
            setSteppingIn(true);
            setContinueExc(false);
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STEP_OUT) {
            setSteppingOut(true);
            setContinueExc(false);
            action = DebuggerUtils.DebugAction.STEP;

        } else if (action == DebuggerUtils.DebugAction.STACK) {

            response.append(getStack());

        } else if (action == DebuggerUtils.DebugAction.VARS) {

            response.append(getVariables());

        } else if (action == DebuggerUtils.DebugAction.EVENT_FLOW) {

            response.append(getEventFlow());

        } else if (action == DebuggerUtils.DebugAction.STEP) {
            setContinueExc(false);
        } else {
            System.out.println("Unknown command: " + action.toString());
            return;
        }

        if (action == DebuggerUtils.DebugAction.STEP) {
            responseToken = DebuggerUtils.DebugAction.STEP;
                if (getCurrentElement() instanceof StateNode && (((StateNode) getCurrentElement()).getSubStateMachine() != null) && isSteppingIn()) {
                    getParsingGraphs().push(new SMMLParsingGraph(((StateNode) getCurrentElement()).getSubStateMachine(), this));
                    /*if (!isSteppingIn()) {
                        if (!executeBlock()) {
                            return;
                        }
                    } */
                }
                if (isSteppingOut() && getParsingGraphs().size() > 1) {
                    getParsingGraphs().pop();
                }
                try {
                    getNextElement();
                } catch (ParsingException exc) {
                    processException(exc);
                    return;
                }
                if (isEndOfFile()) {
                    if (getParsingGraphs().size() > 1) {
                        getParsingGraphs().pop();
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
                getParsingGraphs().pop();
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
        int stackCount = stack.equals("") ? 0 : stack.split("\n").length;
        response.append(stackCount);
        response.append("\n");
        response.append(stack);

        String eventFlow = getEventFlow();
        int eventFlowCount = eventFlow.equals("") ? 0 : eventFlow.split("\n").length;
        response.append(eventFlowCount);
        response.append("\n");
        response.append(eventFlow);

        return response.toString();

    }

    public void sendBack(DebuggerUtils.DebugAction responseToken, String response) {
        getClientHandler().sendBack(DebuggerUtils.responseToken(responseToken) + response);
    }

    private String getStack() {
        StringBuilder stack = new StringBuilder();
        ListIterator iterator = getParsingGraphs().listIterator(getParsingGraphs().size());

        while (iterator.hasPrevious()) {
            ParsingGraph previous = (ParsingGraph) iterator.previous();
            String filename = previous.getGraph().getPath().getFileName().toString();
            String frame = "" + filename + ":" + ((SMMLParsingGraph)previous).getCurrentElement().getId();
            stack.append(frame);
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
                String value;
                if (field.get(getCurrentElement()) != null) {
                    if (field.getType() == StateMachine.class) {
                        value = ReflectionUtil.getInheritedDeclaredFieldValue(field.get(getCurrentElement()), "id", Object.class).toString() + ".sm";
                    } else {
                        value = field.get(getCurrentElement()).toString();
                    }
                } else {
                    value = "empty";
                }
                field.setAccessible(false);
                String name = field.getName();

                String var = "" + name + ":" + "0" + ":" + type + ":" + value;
                vars.append(var);
                vars.append("\n");
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return vars.toString();
    }

    private String getEventFlow() {
        StringBuilder events = new StringBuilder();

        ListIterator iterator = ((SMMLParsingGraph)getParsingGraphs().peek()).getEventFlow().listIterator(((SMMLParsingGraph)getParsingGraphs().peek()).getEventFlow().size());
        while (iterator.hasPrevious()) {
            EventFlowEntry previous = (EventFlowEntry) iterator.previous();
            String filename = getFullPathOfCurrentGraph().getFileName().toString();
            String event = "" + filename + ":" + previous.getElement() + ":" + previous.getEvent();
            events.append(event);
            events.append("\n");
        }

        return events.toString();
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
        int stackCount = stack.equals("") ? 0 : stack.split("\n").length;
        sb.append(stackCount);
        sb.append("\n");
        sb.append(stack);

        String eventFlow = getEventFlow();
        int eventFlowCount = eventFlow.equals("") ? 0 : eventFlow.split("\n").length;
        sb.append(eventFlowCount);
        sb.append("\n");
        sb.append(eventFlow);
        getClientHandler().sendBack(sb.toString());
    }

    private void getNextElement() throws ParsingException {
        ParsingGraph parser = getParsingGraphs().peek();
        GElement element = parser.parseNextElement();

        if (element != null) {
            setCurrentElement(element);
            if(!parser.getGraph().getElementList().contains(getCurrentElement())){
                getParsingGraphs().pop();
                ((SMMLParsingGraph)getParsingGraphs().peek()).setCurrentElement(element);
                ((SMMLParsingGraph)getParsingGraphs().peek()).setCurrentEdge((Transition)element);
            }
        } else {
            setEndOfFile(true);
        }

    }

    private boolean executeNextElement() {
        if (checkBreakpoint(getCurrentElement())) {
            return false;
        }
        getCurrentElement().accept(new StateMachineInterpreter());
        return true;
    }
}
