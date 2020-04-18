package utils;

public class DebuggerUtils {
    public enum DebugAction {NONE, FILE, STEP, CONTINUE, STEP_IN, STEP_OUT, SET_BP, VARS, STACK, END, BYE, EVENT, EVENT_FLOW}

    public static DebuggerUtils.DebugAction stringToAction(String str) {
        int index = str.indexOf("|");
        String cmd = "";
        if (index >= 0) {
            cmd = str.substring(0, index).trim();
        }
        DebuggerUtils.DebugAction action = DebuggerUtils.DebugAction.NONE;
        switch (cmd) {
            case "step":
                action = DebuggerUtils.DebugAction.STEP;
                break;
            case "continue":
                action = DebuggerUtils.DebugAction.CONTINUE;
                break;
            case "stepin":
                action = DebuggerUtils.DebugAction.STEP_IN;
                break;
            case "stepout":
                action = DebuggerUtils.DebugAction.STEP_OUT;
                break;
            case "file":
                action = DebuggerUtils.DebugAction.FILE;
                break;
            case "setbp":
                action = DebuggerUtils.DebugAction.SET_BP;
                break;
            case "vars":
                action = DebuggerUtils.DebugAction.VARS;
                break;
            case "stack":
                action = DebuggerUtils.DebugAction.STACK;
                break;
            case "eventFlow":
                action = DebuggerUtils.DebugAction.EVENT_FLOW;
                break;
            case "bye":
                action = DebuggerUtils.DebugAction.BYE;
                break;
        }
        return action;
    }

    public static String responseToken(DebuggerUtils.DebugAction action) {
        switch (action) {
            case STEP:
                return "next\n";
            case FILE:
                return "file\n";
            case SET_BP:
                return "set_bp\n";
            case END:
                return "end\n";
            case EVENT:
                return "event\n";
        }

        return "none\n";
    }
}
