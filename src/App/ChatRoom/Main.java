package App.ChatRoom;

import Socket.SocketManager;

public class Main {
    public static void main(String[] args) {
        SocketManager sm = new SocketManager();
        P2P_ChatRoom chatRoom = new P2P_ChatRoom(sm);
        chatRoom.start();
    }
}
