package debugger;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class DebuggerServer {

    private static ServerSocket serverSocket;
    private static final int PORT = 5056;
    private static ExecutorService es = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {

        try {
            System.out.println("Starting Server");
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started and ready to accept clients");

            while (true) {
                try {
                    Socket socket = serverSocket.accept();

                    System.out.println("A new client is connected : " + socket);

                    System.out.println("Assigning new thread for this client");

                    es.submit(new ClientHandler(socket));

                } catch(IOException ioe) {
                    System.out.println("Error accepting connection");
                    ioe.printStackTrace();
                }
            }

        } catch(IOException e) {
            System.out.println("Error starting Server on " + serverSocket);
            e.printStackTrace();
        }
    }

    private static void stopServer() {
        es.shutdown();
        try {
            //Stop accepting requests.
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error in server shutdown");
            e.printStackTrace();
        }
        System.exit(0);
    }
}


class ClientHandler implements Runnable {

    private BufferedReader br;
    private BufferedOutputStream bos;
    private Socket socket;
    @Setter
    @Getter
    private Debugger debugger;


    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        debugger = new Debugger(this);
        try {
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bos = new BufferedOutputStream(socket.getOutputStream());

            while (true) {
                String dataInput = br.readLine();

                DebuggerUtils.DebugAction action = DebuggerUtils.stringToAction(dataInput);

                if (action == DebuggerUtils.DebugAction.BYE) {
                    break;
                } else {
                    debugger.processClientCommand(action, dataInput);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket + " caused by " + e.getMessage());
        } finally {
            System.out.println("Closing this connection.");
            this.disconnect();
            System.out.println("Connection closed");
        }
    }

    public void sendBack(String response) {
        try {
            byte[] msg = response.getBytes("UTF-8");
            String msgLength = msg.length + "\n";
            byte[] byteMsgLength = msgLength.getBytes("UTF-8");
            bos.write(byteMsgLength, 0, byteMsgLength.length);
            bos.flush();
            System.out.println(byteMsgLength.length);
            bos.write(msg, 0, msg.length);
            bos.flush();
            System.out.println(msg.length);
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket);
            e.printStackTrace();
            disconnect();
        }
    }

    private void disconnect() {
        try {
            br.close();
            bos.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
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