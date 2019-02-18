package Socket;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static App.util.println;

public class SocketManager {

    Map<Integer,SocketThread> sockets = new ConcurrentHashMap<>(); // store all exists connections
    ListeningThread listener;
    public ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<>();// message queue
    String myIP;
    public int myID;

    /**
     * Create the socket manager
     *
     * Socket manager manages all the sockets connections in a machine,
     *
     * it listening connections request from other machine,
     *
     * and reconnect the socket if it is broke.
     */
    public SocketManager(){
        /**
         * When a new machine join the network,
         * it establish it's listener,
         * then scan the existing machine and try to make connection with them
         */
        listener = new ListeningThread(1995,this);
        listener.start();
        try{
            myIP = InetAddress.getLocalHost().getHostAddress();
            myID = InetAddress.getLocalHost().getAddress()[3];
        }catch(UnknownHostException e){
            println(e.getMessage());
        }




        for(String ip: scanIP()){
            if(ip.equals(myIP))
                continue;
            try{
                Socket socket = new Socket(ip,1995);
                addSocket(socket);
            }catch(Exception e){
                continue;
            }
        }

        // send join message
        Message joinMsg = new Message(myID);
        joinMsg.newJoin = true;
        broadcastMessage(joinMsg);

        System.out.println("Socket Manager Created, " + sockets.size() + " sockets connected ");
        println("My ip: " + myIP +" my id: " +myID);
    }

    /**
     * Disconnect from all the sockets
     */
    public void disconnect(){

        for(SocketThread thread: sockets.values()){
            thread.shouldStop = true;
        }
        listener.shouldStop = true;

        for(SocketThread thread: sockets.values()){
            try{
                thread.join();
            }catch(InterruptedException e){
            }
        }

        try{
            listener.join();
        }catch (InterruptedException e){
        }

    }

    /**
     * Create thread to handle socket and store it.
     *
     * No duplicated connection is needed,
     * before storing, check whether socket with that machine already exists.
     *
     * TODO: should be synchronized
     * @param socket: socket needed to be maintained
     */
     void addSocket(Socket socket){

        int id = socket.getInetAddress().getAddress()[3];
        try{
            if(!sockets.containsKey(id)){
                SocketThread thread = new SocketThread(socket,this);
                thread.start();
                sockets.put(id,thread);
            }
            else{
                try{
                    socket.close();
                }catch(IOException e){
                }
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }

    void removeSocket(int id){
         sockets.remove(id);
    }

    /**
     * Add message to message queue
     *
     * invoked by socket thread, when they received message
     */
    void addMessageToQueue(Message msg){
        messages.add(msg);
    }

    public void sendMessage(String content, int targetId){
        sockets.get(targetId).waitingMsg.add(new Message(content,myID));
    }
    public void sendMessage( Message msg,int targetId){
        sockets.get(targetId).waitingMsg.add(msg);
    }

    public void broadcastMessage(Message msg) {
        for(int i: sockets.keySet()){
            if(i != myID)
                sendMessage(msg,i);
        }
    }

    public void broadcastMessage(String content){
        // test
        for(int i: sockets.keySet()){
            if(i != myID)
                sendMessage(content,i);
        }
    }
    public int getSocketsNumber(){
        return sockets.size();
    }
    public Message readMessage(){
        Message msg = null;
        if(messages.size()> 0)
            msg = messages.poll();
        return msg;
    }
    public boolean hasMessage(){
        return messages.size() > 0;
    }


    /**
     *  scan the available ip on LAN
     */
    public static ConcurrentLinkedQueue<String> scanIP(){
        final byte[] ip;

        ConcurrentLinkedQueue<String> ips = new ConcurrentLinkedQueue<>();
        try {
            ip = InetAddress.getLocalHost().getAddress();
        }catch(UnknownHostException e){
            return ips;
        }

        for(int i=1;i<=254;i++) {
            final int j = i;  // i as non-final variable cannot be referenced from inner class
            new Thread(new Runnable() {   // new thread for parallel execution
                public void run() {
                    try {
                        ip[3] = (byte)j;
                        InetAddress address = InetAddress.getByAddress(ip);
                        String output = address.toString().substring(1);
                        if (address.isReachable(1000)) {
                            ips.add(output);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();     // dont forget to start the thread
        }
        return ips;
    }
}
