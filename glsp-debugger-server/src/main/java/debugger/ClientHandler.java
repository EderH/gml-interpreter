package debugger;

import lombok.Getter;
import lombok.Setter;
import utils.DebuggerUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private BufferedReader br;
    private BufferedOutputStream bos;
    private Socket socket;
    @Getter
    @Setter
    private Debugger debugger;


    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
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