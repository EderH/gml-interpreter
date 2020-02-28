package debugger;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
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

                InputStreamReader isr = new InputStreamReader(s.getInputStream());
                OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                es.execute(new ClientHandler(s, isr, osw));

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

    private final BufferedReader br;
    private final BufferedWriter bw;
    private final Socket socket;
    @Setter
    @Getter
    private Debugger debugger;


    public ClientHandler(Socket socket, InputStreamReader inputStreamReader, OutputStreamWriter outputStreamWriter) {
        this.socket = socket;
        this.br = new BufferedReader(inputStreamReader);
        this.bw = new BufferedWriter(outputStreamWriter);
    }

    public void run() {
        String dataInput;
        debugger = new Debugger(this);
        try {
            while (true) {
                dataInput = br.readLine();
                System.out.println(dataInput);
                int index = dataInput.indexOf("|");
                String cmd = "";
                String data = "";
                if (index >= 0) {
                    data = (index < dataInput.length() - 1 ? dataInput.substring(index + 1) : "").trim();

                    cmd = dataInput.substring(0, index).trim();
                }
                System.out.println(cmd);
                DebuggerUtils.DebugAction action = DebuggerUtils.stringToAction(cmd);

                if (action == DebuggerUtils.DebugAction.BYE) {
                    break;
                } else {
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

            bw.write(response);
            System.out.println("Response: " + response);
            bw.flush();

        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket);
            e.printStackTrace();
            disconnect();
        }
    }

    private void disconnect() {
        try {
            br.close();
            bw.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}