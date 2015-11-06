package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import com.tayek.tablet.Message.*;
import com.tayek.utilities.*;
import com.tayek.tablet.Home.GetSocket;
import com.tayek.tablet.Message;
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
            return inetAddress!=null?inetAddress.toString():"none";
        }
        public TcpClient client;
        public InetAddress inetAddress;
    }
    public class Tablet { // keep this an inner class?
        public Tablet(int tabletId) throws IOException {
            model=newModel();
            client=new TcpClient(Group.this,tabletId,model);
            model.setState(1,true);
            System.out.println("model id is ="+model.serialNumber);
            this.tabletId=tabletId;
        }
        public Group group() {
            return Group.this;
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
    public void setInetAddress(int tabletId,InetAddress inetAddress) {
        info.get(tabletId).inetAddress=inetAddress;
    }
    public InetAddress inetAddress(int tabletId) {
        Info stuff=info.get(tabletId);
        return stuff!=null?stuff.inetAddress:null;
    }
    public void addAddressToMessage(int tabletId,Message message) {
        InetAddress inetAddress=inetAddress(tabletId);
        int address=Utility.toInteger(inetAddress);
        // check for zero?
        if(message.button==0) {
            if(message.tabletId.equals(tabletId)) {
                message.button=address;
            } else System.out.println("different tablet id's!");
        } else System.out.println("already has an addresss.");
    }
    public Tablet createTablet(int tabletId) throws IOException {
        return new Tablet(tabletId);
    }
    public static Group create(int id,int start) {
        return create(id,start,defaultTablets);
    }
    public static Group create(int id,int start,int n) {
        Group group=new Group(id,null);
        Set<Integer> ids=new TreeSet<>();
        for(int tabletId=start;tabletId<start+n;tabletId++)
            ids.add(tabletId);
        return new Group(id,ids);
    }
    private Group(int id,Set<Integer> ids) {
        // group now always has home in it with a tablet id of zero!
        this.groupId=id;
        info.put(0,new Info());
        if(ids!=null) for(int tabletId:ids)
            if(info.size()<maxTablets+1) info.put(tabletId,new Info());
            else System.out.println("tablet: "+tabletId+" too many tablets!");
    }
    public InetAddress inetAddress(int tabletId,Socket socket) {
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
    public InetAddress checkForInetAddress(int tabletId,Socket socket) {
        InetAddress inetAddress=inetAddress(tabletId); // throws
        if(inetAddress==null) inetAddress=inetAddress(tabletId,socket);
        if(inetAddress!=null) { // looks like we discovered a new one!
            if(info.containsKey(tabletId)) {
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
    public void print() {
        System.out.println(this);
        for(int i:info.keySet())
            System.out.println("\t"+i+": "+info.get(i));
    }
    @Override public String toString() {
        return "group: "+groupId+": "+info.keySet();
    }
    public static void captureInetAddress(Group group,Socket socket,Message message) {
        if(message!=null) {
            if(message.groupId==group.groupId) {
                InetAddress inetAddress=group.inetAddress(message.tabletId);
                if(inetAddress==null) if(socket!=null) {
                    SocketAddress socketAddress=socket.getRemoteSocketAddress();
                    if(socketAddress instanceof InetSocketAddress) {
                        InetSocketAddress inetSocketAddress=(InetSocketAddress)socketAddress;
                        group.setInetAddress(message.tabletId,inetSocketAddress.getAddress());
                        group.logger.info("tablet: "+message.tabletId+" is on: "+inetSocketAddress.getAddress());
                    } else System.out.println("not an inet socket address!");
                }
            } else {
                System.out.println("message is from foreigh group! "+message);
                // maybe give him some address info
            }
        } else System.out.println("message is null!");
    }
    public static void test() {
        System.out.println(create(0,1));
        System.out.println(create(1,1));
        System.out.println(create(5,1));
        System.out.println(create(1,5));
        System.out.println(create(10,5));
    }
    Properties properties() { // this will not have the right buttons for each
                              // tablet!
        // move it to the client or the tablet or the model!
        Properties properties=new SortedProperties();
        properties.put("buttons",model.buttons.toString());
        for(int i=1;i<=model.buttons;i++)
            properties.put("button"+i,model.state(i).toString());
        for(Integer i:info.keySet())
            properties.put("tablet"+i,info.get(i).toString());
        return properties;
    }
    protected Object clone() {
        Group clone=new Group(groupId,info.keySet());
        return clone;
    }
    public final Model model=new Model(); // default for now
    public final Integer groupId;
    public final Map<Integer,Info> info=new TreeMap<>(); // usually 1-n
    public final Logger logger=Logger.getLogger(this.getClass().getName());
    public static final Integer defaultButtons=5;
    public static final Integer defaultTablets=defaultButtons+2;
    public static final Integer maxTablets=100;
    public static final Integer maxButtons=20;
}
