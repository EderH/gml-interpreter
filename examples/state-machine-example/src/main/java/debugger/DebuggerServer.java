package debugger;

import lombok.Getter;
import lombok.Setter;
import utils.DebuggerUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebuggerServer {

    private static ServerSocket serverSocket;
    private static final int PORT = 5057;
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

