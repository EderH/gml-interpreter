package debugger;

import java.io.IOException;
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


