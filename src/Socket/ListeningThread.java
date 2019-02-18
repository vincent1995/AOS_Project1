package Socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This Thread will be waiting for connections from other process.
 *
 * When got incoming connection, return socket back to socketManager.
 */
public class ListeningThread  extends Thread{


    private ServerSocket serverSocket;
    private int port;
    private SocketManager socketManager;
    public boolean shouldStop = false;

    ListeningThread(int port, SocketManager mg){
        this.port = port;
        socketManager = mg;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(100);

            while (true){

                if(shouldStop){
                    break;
                }
                // got connection, return it back to socket manager
                try{
                    Socket socket = serverSocket.accept();
                    socketManager.addSocket(socket);
                }catch(IOException e){
                }
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }

        try{
            serverSocket.close();
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
}
