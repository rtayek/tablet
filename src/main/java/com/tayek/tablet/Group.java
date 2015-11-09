package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import javax.print.attribute.standard.PrinterInfo;
import com.tayek.utilities.*;
import com.tayek.tablet.model.*;
import com.tayek.tablet.model.Message.*;
public class Group implements Cloneable {
    // https://www.ibm.com/developerworks/community/blogs/738b7897-cd38-4f24-9f05-48dd69116837/entry/understanding_some_common_socketexceptions_in_java3?lang=en
    // http://www.codeproject.com/Articles/37490/Detection-of-Half-Open-Dropped-TCP-IP-Socket-Conne
    // send a hello or a i'm still here message
    // this will detect half open connection
    // not to be confused with half closed connection (when one side shuts dow
    // output)
    // looks like we may need keep alives on *both* sides
    // when we can't send, the we know the connection is broken
    // reading will wait forever
    // http://www.javaworld.com/article/2076498/core-java/simple-handling-of-network-timeouts.html?page=2
    // https://guides.codepath.com/android/Sending-and-Receiving-Data-with-Sockets
    public static class Info {
        @Override public String toString() {
            return inetAddress!=null?inetAddress.toString():"";
        }
        public TcpClient client;
        public InetAddress inetAddress;
    }
    public class Tablet { // keep this an inner class?
        public Tablet(Home home,Integer tabletId) {
            model=newModel();
            client=new TcpClient(home,Group.this,tabletId,model);
            System.out.println("model id is = "+model.serialNumber);
            this.tabletId=tabletId;
        }
        public Group group() {
            return Group.this;
        }
        public Integer tabletId() {
            return tabletId;
        }
        public Model model() {
            return model;
        }
        public TcpClient client() {
            return client;
        }
        @Override public String toString() {
            return groupId+":"+client().tabletId();
        }
        private TcpClient client;
        private final Model model;
        private final Integer tabletId;
        public final Logger logger=Logger.getLogger(this.getClass().getName());
    }
    Group(Integer groupId,Set<Integer> tabletIds) {
        // nobody should call this ctor except for home!
        this(groupId,tabletIds,++serialNumbers);
    }
    private Group(Integer groupId,Set<Integer> tabletIds,Integer serialNumber) {
        // group now always has home in it with a tablet id of zero!
        this.serialNumber=serialNumber;
        this.groupId=groupId;
        info.put(0,new Info());
        if(tabletIds!=null) for(int tabletId:tabletIds)
            if(info.size()<maxTablets+1) info.put(tabletId,new Info());
            else System.out.println("tablet: "+tabletId+" too many tablets!");
    }
    public static Group create(Integer groupId,Integer start) {
        return create(groupId,start,defaultTablets);
    }
    public static Group create(Integer groupId,Integer start,Integer n) {
        Group group=new Group(groupId,null);
        Set<Integer> tabletIds=new TreeSet<>();
        for(Integer tabletId=start;tabletId<start+n;tabletId++)
            tabletIds.add(tabletId);
        return new Group(groupId,tabletIds);
    }
    public void setInetAddress(Integer tabletId,InetAddress inetAddress) {
        info().get(tabletId).inetAddress=inetAddress;
    }
    // move this stuff to info class!
    public InetAddress inetAddress(Integer tabletId) {
        Info stuff=info().get(tabletId);
        Object object=stuff!=null?stuff.inetAddress:null;
        return stuff!=null?stuff.inetAddress:null;
    }
    public void addAddressToMessage(Integer tabletId,Message message) {
        InetAddress inetAddress=inetAddress(tabletId);
        int address=Utility.toInteger(inetAddress);
        // check for zero?
        if(message.button==0) {
            if(message.tabletId.equals(tabletId)) {
                message.button=address;
            } else {
                System.out.println("different tablet id's!");
                throw new RuntimeException();
            }
        } else System.out.println("already has an addresss.");
    }
    public InetAddress inetAddress(Integer tabletId,Socket socket) {
        // maybe belongs in group or tablet?
        InetAddress inetAddress=null;
        if(socket!=null) {
            if(inetAddress(tabletId)!=null) {
                inetAddress=inetAddress(tabletId);
            } else {
                SocketAddress socketAddress=socket.getLocalSocketAddress();
                if(socketAddress instanceof InetSocketAddress) inetAddress=((InetSocketAddress)socketAddress).getAddress();
            }
        }
        return inetAddress;
    }
    public InetAddress checkForInetAddress(Integer tabletId,Socket socket) {
        InetAddress inetAddress=inetAddress(tabletId); // throws
        if(inetAddress==null) inetAddress=inetAddress(tabletId,socket);
        if(inetAddress!=null) { // looks like we discovered a new one!
            if(info().containsKey(tabletId)) {
                this.setInetAddress(tabletId,inetAddress); // here
            } else System.out.println("found a new tablet: "+tabletId);
        }
        return inetAddress;
    }
    public Group newGroup() {
        return (Group)clone();
    }
    public Model newModel() {
        return (Model)model.clone();
    }
    public void print(Integer tabletId) {
        System.out.println("group: "+groupId+"("+serialNumber+"):"+tabletId);
        Map<Integer,Info> copy=info();
        for(int i:copy.keySet())
            System.out.println("\t"+i+": "+copy.get(i));
    }
    public Map<Integer,Info> info() {
        Map<Integer,Info> copy=new TreeMap<>();
        synchronized(info) {
            copy.putAll(info);
        }
        return Collections.unmodifiableMap(copy);
    }
    @Override public String toString() {
        return "group: "+groupId+"("+serialNumber+"): "+info().keySet();
    }
    public void captureInetAddress(Integer tabletId,Socket socket,Message message) {
        if(message!=null) {
            if(message.groupId.equals(groupId)) {
                InetAddress inetAddress=inetAddress(message.tabletId);
                if(inetAddress==null) if(socket!=null) {
                    SocketAddress socketAddress=socket.getRemoteSocketAddress();
                    if(socketAddress instanceof InetSocketAddress) {
                        InetSocketAddress inetSocketAddress=(InetSocketAddress)socketAddress;
                        if(inetSocketAddress!=null) {
                            setInetAddress(message.tabletId,inetSocketAddress.getAddress());
                            logger.info("group: "+this+" tablet: "+message.tabletId+" is on: "+inetSocketAddress.getAddress());
                            logger.info("group: "+this+" info now is: "+info().get(message.tabletId));
                            print(tabletId);
                        }
                    } else System.out.println("not an inet socket address!");
                }
            } else System.out.println("group: "+this+" received message from foreign group! "+message);
        } else System.out.println("message is null!");
    }
    public boolean weHaveThemAll() {
        for(Info info:info().values())
            if(info.inetAddress==null) return false;
        return true;
    }
    public static void test() {
        System.out.println(create(0,1));
        System.out.println(create(1,1));
        System.out.println(create(5,1));
        System.out.println(create(1,5));
        System.out.println(create(10,5));
    }
    protected Object clone() {
        Group clone=new Group(groupId,info.keySet());
        return clone;
    }
    public final Integer serialNumber;
    public final Model model=new Model(defaultButtons); // default for now
    public final Integer groupId;
    public final Map<Integer,Info> info=new TreeMap<>(); // usually 1-n
    public final Logger logger=Logger.getLogger(this.getClass().getName());
    private static int serialNumbers;
    public static final Integer defaultButtons=5;
    public static final Integer defaultTablets=defaultButtons+2;
    public static final Integer maxTablets=100;
    public static final Integer maxButtons=20;
}
