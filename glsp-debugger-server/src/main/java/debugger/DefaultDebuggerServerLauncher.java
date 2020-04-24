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

public class DefaultDebuggerServerLauncher extends DebuggerServerLauncher{

    private static ServerSocket serverSocket;
    private static ExecutorService es = Executors.newCachedThreadPool();

    public DefaultDebuggerServerLauncher(){}

    public DefaultDebuggerServerLauncher(Debugger debugger) {
        super(debugger);
    }


    public void run(int port) {

        try {
            System.out.println("Starting Server");
            serverSocket = new ServerSocket(port);
            System.out.println("Server started and ready to accept clients");

            while (true) {
                try {
                    Socket socket = serverSocket.accept();

                    System.out.println("A new client is connected : " + socket);

                    System.out.println("Assigning new thread for this client");

                    Debugger debugger = getDebugger();
                    ClientHandler handler = new ClientHandler(socket);
                    handler.setDebugger(debugger);
                    debugger.setClientHandler(handler);

                    es.submit(handler);

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

    @Override
    public void shutdown() {
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




