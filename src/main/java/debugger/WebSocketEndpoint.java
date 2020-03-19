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

