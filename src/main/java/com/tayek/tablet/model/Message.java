package com.tayek.tablet.model;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import com.tayek.tablet.*;
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
    public Message(int groupId,int from,Type type,int extra) {
        this(groupId,from,type,extra,false);
    }
    public Message(Integer groupId,Integer from,Type type,Integer button,boolean state) {
        this.groupId=groupId;
        this.tabletId=from;
        this.type=type;
        this.button=button;
        this.state=state;
    }
    public interface Sender {
        Integer tabletId();
        void send(Message message) throws IOException;
        Logger logger=Logger.getLogger(Sender.class.getName());
    }
    public interface Receiver<T> {
        // change this so it does not throw!
        // let's try that now!
        void receive(T message) /*throws IOException*/;
        Logger logger=Logger.getLogger(Receiver.class.getName());
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
        if(string==null) {
            System.out.println("string is null!");
            // maybe invent a no-op? or null?
            return null;
        }
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
        logger.finest("Message.from is returning: "+message);
        return message;
    }
    public static Message process(int groupId,Receiver<Message> receiver,SocketAddress socketAddress,String string) {
        Message message=Message.from(string);
        if(message!=null) {
            if(message.groupId.equals(groupId)) {
                if(receiver!=null) try {
                    receiver.receive(message);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
                else System.out.println("receiver is null!");
            } else System.out.println("received a message from another group: "+message.groupId);
        } else System.out.println("received a null messagefrom : "+socketAddress);
        return message;
    }
    public final Integer groupId;
    public final Integer tabletId;
    public final Type type;
    public /*final*/ Integer button; // hack for address;
    public final Boolean state;
    public static final Set<Class<?>> set=new LinkedHashSet<>();
    public static final Logger logger=Logger.getLogger(Message.class.getName());
    static {
        set.add(Server.class);
        set.add(Message.class);
        set.add(Sender.class);
        set.add(Receiver.class);
        set.add(Model.class);
        set.add(MyTimer.class);
    }
    public static final Map<Class<?>,Logger> map=LoggingHandler.makeMapAndSetLevels(set);
}
