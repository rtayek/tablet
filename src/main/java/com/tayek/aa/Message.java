package com.tayek.aa;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import com.tayek.aa.Factory.*;
import com.tayek.utilities.*;
public class Message implements java.io.Serializable {
    public enum Type {
        normal,start,hello,goodbye;
        public boolean isNormal() {
            return this.equals(normal);
        }
        public boolean isControl() {
            return !this.equals(normal);
        }
    }
    private static final long serialVersionUID=1L;
    public Message(Integer groupId,Integer from,Type type,InetAddress address) {
        this(groupId,from,type,Utility.toInteger(address)); // hello
    }
    public Message(Integer groupId,Integer from,Type type,Integer extra) {
        this(groupId,from,type,extra,false);
    }
    public Message(Integer groupId,Integer from,Type type,Integer button,boolean state) {
        this.groupId=groupId;
        this.tabletId=from;
        this.type=type;
        this.button=button;
        this.state=state;
    }
    public interface Sender { // not sure i need this one
        Integer tabletId(); // may not be a tablet>
        void send(Object object) throws IOException;
        Logger logger=Logger.getLogger(Sender.class.getName());
    }
    public boolean isNormal() {
        return type.equals(Type.normal);
    }
    public boolean isControl() {
        return !type.equals(Type.normal);
    }
    @Override public String toString() {
        return groupId+" "+tabletId+" "+type+" "+button+" "+state;
    }
    public static Message from(String string) {
        String[] parts=string.split(" ");
        if(parts.length!=5) System.out.println("bad message: "+string);
        Integer groupId=new Integer(parts[0]);
        Integer fromId=new Integer(parts[1]);
        Type type=Type.valueOf(parts[2]);
        Integer button=null;
        if(type.isControl()) {
            if(parts[3].startsWith("/")) ;// button=Utility.i
        }
        // add error checking so the value of's can never through
        button=Integer.valueOf(parts[3]);
        Boolean state=Boolean.valueOf(parts[4]);
        Message message=new Message(groupId,fromId,type,button,state);
        logger.fine("Message.from is returning: "+message);
        return message;
    }
    public static Message process(int groupId,Receiver<Message> receiver,SocketAddress socketAddress,String string) {
        Message message=null;
        try {
            message=Message.from(string);
            if(message!=null) {
                if(message.groupId.equals(groupId)) {
                    if(receiver!=null) try {
                        receiver.receive(message);
                    } catch(Exception e) {
                        System.out.println("Message caught: "+e);
                    }
                    else System.out.println("receiver is null!");
                } else System.out.println("received a message from another group: "+message.groupId);
            } else System.out.println("received a null messagefrom : "+socketAddress);
        } catch(Exception e) {
            System.out.println("Message caught: "+e);
        }
        return message;
    }
    public final Integer groupId;
    public final Integer tabletId;
    public final Type type;
    public /*final*/ Integer button; // hack for address;
    public final Boolean state;
    public static final Logger logger=Logger.getLogger(Message.class.getName());
}
