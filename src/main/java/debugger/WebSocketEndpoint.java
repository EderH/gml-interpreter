package debugger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/endpoint")
public class WebSocketEndpoint {

    private Map<Session,Debugger> debuggers = new HashMap<>();
    private Map<Integer, Session> sessions = new HashMap<>();
    private static int ID = 0;


    @OnOpen
    public void onOpen(Session session) {
        Debugger debugger;
        System.out.println("onOpen::" + session.getId());
        try {
            session.getBasicRemote().sendText("You are connected. Your ID is " + session.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("onClose::" + session.getId());
        try {
            session.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("onMessage::From=" + session.getId() + " Message=" + message);

        int index = message.indexOf("|");
        String cmd = "";
        String data = "";
        if (index >= 0) {
            data = (index < message.length() - 1 ? message.substring(index + 1) : "").trim();

            cmd = message.substring(0, index).trim();
        } else {
            cmd = message.trim();
        }
        System.out.println(cmd);
        DebuggerUtils.DebugAction action = DebuggerUtils.stringToAction(cmd);

        if (action == DebuggerUtils.DebugAction.BYE) {
            System.out.println("Client ended connection");
        } else {
           // debuggers.get(session).processClientCommand(action, data);
        }
    }

    @OnError
    public void onError(Throwable t) {
        System.out.println("onError::" + t.getMessage());
    }
}

class DebuggerUtils {
    public enum DebugAction {NONE, FILE, STEP, CONTINUE, STEP_IN, STEP_OUT, SET_BP, VARS, STACK, END, BYE}

    public static DebugAction stringToAction(String str) {
        int index = str.indexOf("|");
        String cmd = "";
        if (index >= 0) {
            cmd = str.substring(0, index).trim();
        }
        DebugAction action = DebugAction.NONE;
        switch (cmd) {
            case "step":
                action = DebugAction.STEP;
                break;
            case "continue":
                action = DebugAction.CONTINUE;
                break;
            case "stepin":
                action = DebugAction.STEP_IN;
                break;
            case "stepout":
                action = DebugAction.STEP_OUT;
                break;
            case "file":
                action = DebugAction.FILE;
                break;
            case "setbp":
                action = DebugAction.SET_BP;
                break;
            case "vars":
                action = DebugAction.VARS;
                break;
            case "stack":
                action = DebugAction.STACK;
                break;
            case "bye":
                action = DebugAction.BYE;
                break;
        }
        return action;
    }

    public static String responseToken(DebugAction action) {
        switch (action) {
            case STEP:
                return "next\n";
            case FILE:
                return "file\n";
            case SET_BP:
                return "set_bp\n";
            case END:
                return "end\n";
        }

        return "none\n";
    }
}