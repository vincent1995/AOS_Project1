package App.ChatRoom;

import Socket.Message;
import Socket.SocketManager;

import java.io.*;

import static App.util.print;
import static App.util.println;

public class P2P_ChatRoom {
    SocketManager sm;
    String yName;
    BufferedReader kIn;
    int myID;
    public P2P_ChatRoom(SocketManager sm){
        this.sm = sm;
        kIn = new BufferedReader(new InputStreamReader(System.in));
        myID = sm.myID;
    }


    private void stop(){
        try{
            kIn.close();
            sm.disconnect();
        }catch(IOException e){
        }
    }

    public void start(){
        try{

            print("Input your name: ");
            yName = kIn.readLine();
            println("Welcome " + yName + "! Current user: " + (sm.getSocketsNumber()+1));
            sm.broadcastMessage(yName + " join the chatroom. Now " +
                    (sm.getSocketsNumber()+1) +" people online");
            String line;
            while(true){
                if(kIn.ready()){
                    line = kIn.readLine();
                    if(line.equals("\\leave")){
                        break;
                    }
                    else{
                        sm.broadcastMessage(yName + ": "+line);
                    }
                }else if(sm.hasMessage()){
                    while(sm.hasMessage()){
                        Message ms = sm.readMessage();
                        if(ms.msg != null)
                            println(ms.msg);
                    }
                }
                else{
                    Thread.sleep(10);
                }
            }
            sm.broadcastMessage(yName + " leave the chatroom. Now " +
                    (sm.getSocketsNumber()) +" people online");

        }catch(IOException |InterruptedException e){
            println(e.getMessage());
        }

        stop();
    }
}
