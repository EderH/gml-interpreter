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

    public static void main(String[] args) throws IOException {

        serverSocket = new ServerSocket(PORT);
        System.out.println("Server started and ready to accept clients");
        ExecutorService es = Executors.newCachedThreadPool();
        Socket socket;
        while (true) {

            try {
                socket = serverSocket.accept();

                System.out.println("A new client is connected : " + socket);

                System.out.println("Assigning new thread for this client");

                es.execute(new ClientHandler(socket));

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

    private BufferedReader br;
    private BufferedOutputStream bos;
    private OutputStream os;
    private final Socket socket;
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
            this.os = socket.getOutputStream();
            this.bos = new BufferedOutputStream(os);

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
            os.write(byteMsgLength, 0, byteMsgLength.length);
            os.flush();
            System.out.println(byteMsgLength.length);
            os.write(msg, 0, msg.length);
            os.flush();
            Thread.sleep(2000);
            System.out.println(msg.length);
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket);
            e.printStackTrace();
            disconnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            os.close();
            br.close();
            bos.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}