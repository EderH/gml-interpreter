package debugger;

import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebuggerServer {

    private static ServerSocket serverSocket;
    private static final int PORT = 5056;

    public static void main(String[] args) throws IOException {

        serverSocket = new ServerSocket(PORT);
        System.out.println("Server started and ready to accept clients");
        while (true) {
            ExecutorService es = Executors.newCachedThreadPool();
            Socket s;

            try {
                s = serverSocket.accept();

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

    private static void stopServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}


class ClientHandler implements Runnable {
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket socket;
    @Setter
    @Getter
    private Debugger debugger;


    public ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
    }

    public void run() {
        String dataInput;
        debugger = new Debugger(this);

        try {
            while (true) {

                dataInput = dis.readUTF();

                int index = dataInput.indexOf("|");
                String cmd = "";
                String data = "";
                if (index >= 0) {
                    data = index < dataInput.length() - 1 ? dataInput.substring(index + 1) : "";
                    cmd = dataInput.substring(0, index).trim();
                }
                System.out.println(cmd);
                DebuggerUtils.DebugAction action = DebuggerUtils.stringToAction(cmd);

                if (action == DebuggerUtils.DebugAction.BYE) {
                    break;
                } else {
                    //debugger.processClientCommand(action, data);
                    debugger.processClientCommand(action, data);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket);
        }

        System.out.println("Closing this connection.");
        this.disconnect();
        System.out.println("Connection closed");

    }

    public void sendBack(String response) {
        try {

            dos.writeUTF(response);
            dos.flush();

        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket);
            e.printStackTrace();
            disconnect();
        }
    }

    private void disconnect() {
        try {
            dis.close();
            dos.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DebuggerUtils {
    public enum DebugAction {NONE, FILE, STEP, CONTINUE, STEP_IN, STEP_OUT, SET_BP, VARS, STACK, END, BYE}

    public static DebugAction stringToAction(String str) {
        DebugAction action = DebugAction.NONE;
        switch (str) {
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
        String response = "none\n";
        switch (action) {
            case END:
                response = "end\n";
                break;
        }

        return response;
    }
}
