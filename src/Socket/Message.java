package Socket;

import java.io.Serializable;

public class Message implements Serializable {
    public String msg;
    public int srcId;
    public boolean newJoin =false;
    public boolean leave = false;


    public Message(int id){
        srcId = id;
        msg = null;
    }
    public Message(String msg,int id){
        this.msg = msg;
        srcId = id;
    }
}
