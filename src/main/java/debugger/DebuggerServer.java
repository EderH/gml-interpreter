package debugger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebuggerServer {

    private ServerSocket ss;

    public void runServer(int port) throws IOException {

        this.ss = new ServerSocket(port);

        while (true) {
            ExecutorService es = Executors.newCachedThreadPool();
            Socket s;

            try {
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                es.execute(new ClientHandler(s, dis, dos));

            } catch (Exception e) {
                es.shutdown();
                stopServer();
            }
        }
    }

    private void stopServer() {
        if (this.ss != null) {
            try {
                this.ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}


class ClientHandler implements Runnable {
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket s;
    private Debugger debugger;


    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    public void run() {
        String dataInput;
        debugger = new Debugger();
        while (true) {
            try {

                dataInput = dis.readUTF();

                DebuggerUtils.DebugAction action = DebuggerUtils.stringToAction(dataInput);

                if (action == DebuggerUtils.DebugAction.BYE) {
                    break;
                } else {
                    //TODO: handle action at Debugger
                }

            } catch (IOException e) {
                System.out.println("Client disconnected: " + this.s);
                e.printStackTrace();
            }
        }

        System.out.println("Closing this connection.");
        this.disconnect();
        System.out.println("Connection closed");

    }

    public boolean sendBack(String string) {
        try {

            dos.writeUTF(string);
            dos.flush();

        } catch (IOException e) {
            System.out.println("Client disconnected: " + this.s);
            e.printStackTrace();
            disconnect();
            return false;
        }
        return true;
    }

    private void disconnect() {
        try {
            this.dis.close();
            this.dos.close();
            this.s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DebuggerUtils {
    public enum DebugAction {NONE, NEXT, CONTINUE, STEP_IN, STEP_OUT, SET_BP, VARS, STACK, BYE}

    public static DebugAction stringToAction(String str) {
        DebugAction action = DebugAction.NONE;
        switch (str) {
            case "next":
                action = DebugAction.NEXT;
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
}
