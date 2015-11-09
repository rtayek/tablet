package com.tayek.aa;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import com.tayek.aa.Factory.Group.Info;
import com.tayek.aa.Message.*;
import com.tayek.tablet.God;
import com.tayek.utilities.*;
public class Factory {
    public interface Receiver<T> {
        void receive(T t) throws IOException;
    }
    public static class TabletConnection implements Receiver<Message>,Runnable {
        // had static ConnectionReceiver implements Receiver
        // here, but looks like we do not need this.
        TabletConnection(Group group,int tabletId,Socket socket) {
            this.socket=socket;
            this.group=group;
            this.tabletId=tabletId;
            try {
                in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out=new OutputStreamWriter(socket.getOutputStream());
            } catch(IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        public void send(Message message) {
            System.out.println(this+" sending "+message+" to "+socket);
            if(message.isControl()) group.addAddressToMessage(tabletId,message);
            try {
                out.write(message.toString()+"\n");
                out.flush();
            } catch(IOException e) {
                System.out.println(this+" write failed: "+e);
            }
        }
        @Override public void run() {
            System.out.println(this+" is running");
            while(true)
                try {
                    if(in.ready()) {
                        String string=in.readLine(); // blocks
                        if(string.equals(null)) break;
                        Message message=Message.from(string);
                        receive(message);
                    } else Thread.yield();
                } catch(IOException e) {
                    e.printStackTrace();
                    break;
                }
        }
        @Override public void receive(Message message) throws IOException {
            System.out.println(this+" received: "+message);
            if(message!=null) {
                group.captureInetAddress(socket,message);
            } else System.out.println("badness");
        }
        final Socket socket;
        final Group group;
        /*final*/ int tabletId;
        final BufferedReader in;
        final Writer out;
    }
    // home server does broadcast
    // individual servers may not
    // server wants to send home or ??
    // client wants to send his address
    public static class TabletServerConnection extends TabletConnection {
        public TabletServerConnection(Group group,int tabletId,Socket socket) {
            super(group,tabletId,socket);
        }
        @Override public void receive(Message message) throws IOException {
            super.receive(message); // make sure address gets captured
            if(group.inetAddress(message.tabletId)!=null) {
                int address=group.getCurrentHostAddressAsInteger(); // keep non
                                                                    // static!
                Message hello=new Message(group.groupId,0,Message.Type.hello,address);
                System.out.println("hello is ready to send!");
                // messages.addElement(hello);
            }
        }
    }
    public static class TabletClientConnection extends TabletConnection {
        public TabletClientConnection(Group group,int tabletId,Socket socket) {
            super(group,tabletId,socket);
        }
        @Override public void receive(Message message) throws IOException {
            super.receive(message); // make sure address gets captured
            model.receive(message);
        }
        final Model model=group.cloneModel();
    }
    class Acceptor implements Receiver<Socket>,Runnable {
        public Acceptor(ServerSocket serverSocket) {
            // maybe make this guy static and add group to ctor?
            this.serverSocket=serverSocket;
        }
        @Override public void run() {
            logger.info(this+" is listening for connections from "+serverSocket);
            while(true) {
                try {
                    Socket socket=serverSocket.accept();
                    System.out.println("accepted "+socket);
                    receive(socket);
                } catch(IOException e) {
                    e.printStackTrace();
                    // can we continue?
                    throw new RuntimeException("accept");
                }
            }
        }
        @Override public void receive(Socket socket) throws IOException {
            System.out.println("received connectioon to "+socket);
            connections.add(new TabletServerConnection(homeGroup,illegalTabletId,socket));
            System.out.println(connections);
        }
        final ServerSocket serverSocket;
        final Set<TabletServerConnection> connections=new LinkedHashSet<>();
    }
    public class HomeServer implements Receiver<Message>,Runnable {
        public HomeServer(ServerSocket serverSocket) {
            if(serverSocket!=null) acceptor=new Acceptor(serverSocket);
            else acceptor=null;
        }
        void read(Iterator<TabletServerConnection> it,TabletServerConnection connection) throws IOException {
            if(connection.in.ready()) {
                logger.info(this+" is reading from: "+connection.socket.getRemoteSocketAddress());
                String string=connection.in.readLine();
                if(string.equals(null)) { // end of file
                    System.out.println("eof");
                    it.remove(); // remove this guy!
                } else {
                    Message message=Message.from(string);
                    if(connection.tabletId==illegalTabletId) connection.tabletId=message.tabletId;
                    messages.addElement(message);
                }
            }
        }
        @Override public void run() {
            // loops through connection and reads
            logger.info(this+" is running");
            int n=0;
            loop:while(true) {
                synchronized(acceptor.connections) {
                    for(Iterator<TabletServerConnection> it=acceptor.connections.iterator();it.hasNext();) {
                        TabletServerConnection connection=it.next();
                        try {
                            read(it,connection);
                        } catch(IOException e) {
                            e.printStackTrace();
                            break loop;
                        }
                        try {
                            Thread.sleep(10);
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Thread.yield();
            }
            Message.logger.info(this+" is exiting");
        }
        @Override public void receive(Message message) throws IOException {
            System.out.println(this+" received: "+message);
        }
        void maybeAddHello(Message message) {
            if(message.isControl()) {
                if(message.type.equals(Type.start)) {
                    System.out.println("start from: "+message.tabletId);
                    System.out.println("home knows:");
                    homeGroup.print();
                    // do i know this guys ip?
                    if(homeGroup.inetAddress(message.tabletId)!=null) {
                        // maybe should be ==null?
                        int address=homeGroup.getCurrentHostAddressAsInteger();
                        Message hello=new Message(homeGroup.groupId,0,Type.hello,address);
                        messages.addElement(hello);
                        if(special.contains(message.tabletId)) {
                            System.out.println("<<<<<<<<<<<<<<<<<<<<");
                            System.out.println(message);
                            System.out.println(">>>>>>>>>>>>>>>>>>>>");
                        }
                    }
                }
            }
        }
        void broadcast(Message message) {
            synchronized(acceptor.connections) {
                for(Iterator<TabletServerConnection> it=acceptor.connections.iterator();it.hasNext();) {
                    TabletServerConnection connection=it.next();
                    try { // use send(message) ??? send does not
                          // throw
                        connection.send(message);
                        logger.fine("home is sending: "+message+" to: "+connection.socket.getRemoteSocketAddress());
                        // this is the send!
                        connection.out.write(message.toString()+"\n");
                        connection.out.flush();
                        if(message.tabletId!=illegalTabletId) if(special.contains(message.tabletId)) {
                            System.out.println("<<<<<<<<<<<<<<<<<<<<");
                            System.out.println("sent to special: "+message);
                            System.out.println(">>>>>>>>>>>>>>>>>>>>");
                        }
                    } catch(IOException e) {
                        logger.warning("connection: "+connection+", caught: "+e);
                        logger.warning("tabletId is "+connection.tabletId);
                        System.out.println("removing: "+connection);
                        it.remove();
                        System.out.println("lost connection to: "+connection.tabletId);
                        if(!((Integer)connection.tabletId).equals(illegalTabletId)) {
                            Info info=homeGroup.info.get(connection.tabletId);
                            System.out.println("we know: "+info);
                        }
                        // and send a start to that guy
                        // how, we lost connection?
                        // looks like it's his job to reconnect?
                    }
                }
            }
        }
        void start() {
            if(acceptor==null) {
                System.out.println("no acceptor!");
                return;
            }
            new Thread(acceptor,"acceptor").start();
            new Thread(this,"home").start(); // server
            new Thread(new Runnable() {
                @Override public void run() { // broadcast
                    System.out.println("broadcaster is running:");
                    loop:while(true)
                        if(messages.isEmpty()) {
                            if(false) Thread.yield();
                            else try {
                                Thread.sleep(10);
                            } catch(InterruptedException e) {
                                logger.warning("caught: "+e);
                            }
                        } else {
                            Message message=messages.remove(0);
                            logger.fine("home removed: "+message+" from queue.");
                            maybeAddHello(message);
                            broadcast(message);
                            Thread.yield();
                        }
                }
            },"broadcast").start();
        }
        ServerSocket serverSocket;
        Acceptor acceptor;
        final Map<Integer,TabletServerConnection> map=new TreeMap<>();
        final Vector<Message> messages=new Vector<>(100);
    }
    public static class Group {
        public static class Info {
            @Override public String toString() {
                return inetAddress!=null?inetAddress.toString():"none";
            }
            public TabletConnection client; // current, last?
            public InetAddress inetAddress;
        }
        public static Group create(int groupId) {
            return create(groupId,1,defaultTablets);
        }
        public static Group create(int groupId,int start,int n) {
            Set<Integer> tabletIds=new TreeSet<>();
            for(int tabletId=start;tabletId<start+n;tabletId++)
                tabletIds.add(tabletId);
            return new Group(groupId,tabletIds);
        }
        private Group(int groupId,Set<Integer> tabletIds) {
            this.groupId=groupId;
            info.put(0,new Info()); // always has a home
            if(tabletIds!=null) for(int tabletId:tabletIds)
                if(info.size()<maxTablets+1) info.put(tabletId,new Info());
                else System.out.println("tablet: "+tabletId+" too many tablets!");
        }
        public void print() {
            System.out.println(this);
            for(int i:info.keySet())
                System.out.println("\t"+i+": "+info.get(i));
        }
        public InetAddress inetAddress(int tabletId) {
            Info stuff=info.get(tabletId);
            return stuff!=null?stuff.inetAddress:null;
        }
        public void setInetAddress(int tabletId,InetAddress inetAddress) {
            if(inetAddress!=null) {
                info.get(tabletId).inetAddress=inetAddress;
            }
        }
        public void captureInetAddress(Socket socket,Message message) {
            InetAddress inetAddress=inetAddress(message.tabletId);
            if(inetAddress==null) {
                SocketAddress socketAddress=socket.getRemoteSocketAddress();
                if(socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress inetSocketAddress=(InetSocketAddress)socketAddress;
                    setInetAddress(message.tabletId,inetSocketAddress.getAddress());
                    logger.info("tablet: "+message.tabletId+" is on: "+inetSocketAddress.getAddress());
                } else System.out.println("not an inet socket address!");
            }
        }
        public int getCurrentHostAddressAsInteger() {
            InetAddress x=getCurrentHostAddress();
            int address=Utility.toInteger(x);
            return address;
        }
        public InetAddress getCurrentHostAddress() {
            // we do know ours
            InetAddress inetAddress=null;
            try {
                inetAddress=InetAddress.getByName(host);
            } catch(UnknownHostException e) {
                e.printStackTrace();
            }
            return inetAddress;
        }
        public void addAddressToMessage(int tabletId,Message message) { // now 0
            InetAddress inetAddress=inetAddress(tabletId);
            int address=Utility.toInteger(inetAddress);
            if(message.button==0) {
                if(message.tabletId.equals(tabletId)) {
                    message.button=address;
                } else {
                    System.out.println("different tablet id's!");
                    System.out.println("tablet id is "+tabletId+", but from is "+message.tabletId);
                }
            } else System.out.println("already has an addresss.");
        }
        public Group cloneGroup() {
            return (Group)clone();
        }
        public Model cloneModel() {
            return (Model)model.clone();
        }
        @Override public String toString() {
            return "group: "+groupId+": "+info.keySet();
        }
        protected Object clone() { // do not call this unless you are me!
            Group clone=new Group(groupId,info.keySet());
            return clone;
        }
        public final Integer groupId;
        public final Model model=new Model() {
            // subclass this and throw if anyone uses it!
        };
        // usually my pc
        // will become whatever the laptop is
        public final String host="192.168.1.101";// will change!
        public final int service=55555;
        public final Map<Integer,Info> info=new TreeMap<>(); // usually 1-n
    }
    public static class GetSocket implements Runnable { // conect to
        public GetSocket(String host,int service) {
            this.host=host;
            this.service=service;
        }
        @Override public void run() {
            logger.info("getting socket for: "+host+"/"+service);
            socket=get();
            logger.info("got socket: "+socket);
        }
        public Socket get() {
            InetAddress netAddress=null;
            try {
                netAddress=InetAddress.getByName(host);
            } catch(UnknownHostException e) {
                e.printStackTrace();
            }
            logger.info("host: "+netAddress);
            Socket socket=null;
            try {
                socket=new Socket(netAddress,service);
            } catch(IOException e) {
                e.printStackTrace();
            }
            logger.info("socket: "+socket);
            return socket;
        }
        public final String host;
        public final int service;
        public Socket socket;
    }
    public Factory(Group homeGroup) {
        this.homeGroup=homeGroup;
        homeGroupCopy=homeGroup;
    }
    private void run() throws IOException {
        LoggingHandler.setLevel(Level.OFF);
        ServerSocket serverSocket=new ServerSocket(homeGroup.service);
        // make a home server and launch it
        HomeServer homeServer=new HomeServer(serverSocket);
        homeServer.start();
        // now, make a tablet and launch it
        // tablet is just a group and a connection with model so
        int tabletId=4;
        Group group=homeGroup.cloneGroup();
        GetSocket getSocket=new GetSocket(group.host,group.service);
        Socket socket=getSocket.get();
        TabletClientConnection tabletClientConnection=new TabletClientConnection(group,tabletId,socket);
        Thread thread=new Thread(tabletClientConnection,"client "+tabletId);
        thread.start();
        tabletClientConnection.send(new Message(group.groupId,tabletId,Type.start,0));
    }
    public static void main(String[] args) throws IOException {
        God.log.init();
        Group homeGroup=Group.create(1);
        Factory factory=new Factory(homeGroup);
        factory.run();
    }
    final Group homeGroup; // only used by the server
    static Group homeGroupCopy; // hack for acceptor
    final Set<Integer> special=new TreeSet<>();
    { // working android tablets
        special.add(1);
        special.add(2);
    }
    public static final int illegalTabletId=Integer.MIN_VALUE;
    public static final int defaultButtons=4;
    public static final int defaultTablets=defaultButtons+2;
    public static final int maxTablets=20;
    public static final int maxButtons=10;
    public static final Logger logger=Logger.getLogger(Factory.class.getName());
}
