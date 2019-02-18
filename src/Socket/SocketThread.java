package Socket;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

import static App.util.println;

public class SocketThread extends Thread{

    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    SocketManager sm;
    ConcurrentLinkedQueue<Message> waitingMsg;
    int id;
    boolean shouldStop = false;

    SocketThread(Socket socket, SocketManager socketManager) throws IOException {
        this.socket = socket;

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        sm = socketManager;
        id = socket.getInetAddress().getAddress()[3];
        waitingMsg = new ConcurrentLinkedQueue<>();
    }

    private void stopThread(){
        try{
            out.close();
            in.close();
            socket.close();
        }catch(IOException e){
        }

    }

    @Override
    public void run() {
        try{
            socket.setSoTimeout(100);
        }catch(SocketException e){
            println(e.getMessage());
        }
        while(true){
            try{
                if(waitingMsg.size() > 0) {
                    // send message
                    while (waitingMsg.size() > 0) {
                        Message msg = waitingMsg.poll();
                        out.writeObject(msg);
                    }
                    continue;
                }

                {   Message msg = (Message) in.readObject();
                    if (msg != null) {
                        sm.addMessageToQueue(msg);
                        continue;
                    }
                }

                if(shouldStop){
                    Message msg = new Message(id);
                    msg.leave = true;
                    out.writeObject(msg);
                    out.flush();
                    break;
                }

                // not thing to do
                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
                }

            }catch(IOException e){
                //System.err.println(e.getMessage());
                if(e.getMessage().equals("Connection reset")){
                    sm.removeSocket(id);
                    break;
                }
            }catch(ClassNotFoundException e){
                //System.err.println(e.getMessage());
            }
        }
        stopThread();
    }
}
