package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.model.Message;
import com.tayek.tablet.model.Message.*;
import com.tayek.utilities.*;
// tcp on laptop works fine
// and can discover all of the tablets if the phone home on startup once.
// 10/26/15
// no central server, peer to peer
// we can assign ip addresses of 192.168.1.11-11+n-1 (for n tablets)
// or use the ones that the laptop found provided we sent them to each tablet.
// https://plus.google.com/+AndroidDevelopers/posts/Z1Wwainpjhd
//10/27/15
// get all of the ip addresses 
// and send them to each tablet!
// then switch modes
// 10/30/15
// collect all of the small utility methods like toString(socket)!
// this is a server, so quit calling it a home!
// home is just a host/service pair!
public class Server implements Runnable {
    static class TcpConnection { // make this an inner class?
        public TcpConnection(Socket socket) throws IOException {
            this.socket=socket;
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=new OutputStreamWriter(socket.getOutputStream());
            // see if we can figure out who this is?
            // look in group info
            // how do we get rid of these when they throw?
            // we could maybe figure who it is if we knew the ip address
            // but that begs the question
        }
        @Override public String toString() {
            return socket.toString()+" "+socket.isBound()+" "+socket.isClosed()+" "+socket.isConnected()+" "+socket.isInputShutdown()+" "
                    +socket.isOutputShutdown();
        }
        private final Socket socket;
        private final BufferedReader in;
        private final Writer out;
        Integer tabletId=illegalTabletId; // of from
    }
    static class Acceptor implements Runnable {
        public Acceptor(ServerSocket serverSocket) {
            this.serverSocket=serverSocket;
        }
        public TcpConnection accept(Socket socket) throws IOException {
            TcpConnection connection=new TcpConnection(socket);
            logger.info("home accepted connection from: "+socket);
            return connection;
        }
        @Override public void run() {
            logger.info("home is listening for connections on: "+serverSocket);
            while(true) {
                try {
                    TcpConnection connection=accept(serverSocket.accept());
                    synchronized(connections) {
                        connections.add(connection);
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("accept");
                }
            }
        }
        private final ServerSocket serverSocket;
        final Set<TcpConnection> connections=new LinkedHashSet<>();
    }
    public Server(Group group,ServerSocket serverSocket) {
        this.group=group;
        homeInetAddress=serverSocket.getInetAddress();
        System.out.println(serverSocket);
        if(serverSocket!=null) {
            acceptor=new Acceptor(serverSocket);
            System.out.println(acceptor);
        } else acceptor=null;
    }
    void processControlMessage(Message message) {
        if(message.type.equals(Type.start)) {
            System.out.println("start from: "+message.tabletId);
            System.out.println("-----------------------------");
            System.out.print("home knows:");
            group.print();
            if(group.inetAddress(message.tabletId)!=null) { // unknown?
                // leave this non static
                int address=Utility.toInteger(homeInetAddress);
                Message start=new Message(group.groupId,0,Type.start,address);
                if(false) // just to see what happens
                    messages.addElement(start); // send him a start
                // the above does not make sense
                // he knows me, since he sent me a message.
       
                // if i do not know him, maybe i should send
                // him a start then. or maybe a hello is more appropriate?
                //
                if(special.contains(message.tabletId)) {
                    // System.out.println(message);
                }
            } else {
                System.out.println(" i don't know: "+message.tabletId);
            }
            System.out.println("-----------------------------");
        }
    }
    void sendMessageToClient(Message message,Iterator<TcpConnection> it,TcpConnection connection) {
        try {
            logger.finest("home is sending: "+message+" to: "+connection.socket.getRemoteSocketAddress());
            // this is the send!
            connection.out.write(message.toString()+"\n");
            connection.out.flush();
            if(message.tabletId!=illegalTabletId) if(special.contains(message.tabletId)) {
                System.out.println("sent to special: "+message);
            }
        } catch(IOException e) {
            logger.warning("connection: "+connection+", caught: "+e);
            logger.warning("tabletId is "+connection.tabletId);
            // we need to remove this connection
            it.remove();
            System.out.println("lost connection to: "+connection.tabletId);
            if(!connection.tabletId.equals(illegalTabletId)) {
                Info info=group.info.get(connection.tabletId);
                System.out.println("we know: "+info);
            }
            // and send a start to that guy
        }
    }
    void processMessage(Message message) {
        synchronized(acceptor.connections) {
            for(Iterator<TcpConnection> it=acceptor.connections.iterator();it.hasNext();) {
                TcpConnection connection=it.next();
                sendMessageToClient(message,it,connection);
            }
        }
    }
    void start() {
        if(acceptor==null) {
            System.out.println("no acceptor!");
            return;
        }
        System.out.println("starting acceptor");
        new Thread(acceptor,"acceptor").start();
        new Thread(this,"server").start(); // server
        new Thread(new Runnable() {
            @Override public void run() { // broadcast
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
                        if(message.isControl()) processControlMessage(message);
                        logger.fine("home is removing: "+message+" from queue.");
                        processMessage(message);
                        Thread.yield();
                    }
            }
        },"broadcast").start();
    }
    @Override public void run() { // loops through connection and reads
        logger.info(this+" is running");
        int n=0;
        loop:while(true) {
            synchronized(acceptor.connections) {
                for(Iterator<TcpConnection> it=acceptor.connections.iterator();it.hasNext();) {
                    TcpConnection connection=it.next();
                    try {
                        if(connection.in.ready()) {
                            logger.info(this+" is reading from: "+connection.socket.getRemoteSocketAddress());
                            String string=connection.in.readLine();
                            if(string.equals(null)) {
                                // eof
                            } else if(string.equals("")) {
                                // blank
                            } else {
                                Message message=Message.from(string);
                                if(message.groupId.equals(group.groupId)) {
                                    if(connection.tabletId.equals(illegalTabletId)) connection.tabletId=message.tabletId;
                                    group.captureInetAddress(connection.socket,message);
                                    logger.fine("added: "+message+" to queue.");
                                    messages.addElement(message);
                                } else System.out.println("group: "+group.groupId+" received message from foreign group! "+message);
                                // maybe give him some address info
                            }
                        }
                    } catch(IOException e) {
                        logger.warning(this+" caught: e");
                        e.printStackTrace();
                    } catch(Exception e) {
                        logger.warning(this+" caught: e");
                        e.printStackTrace();
                        break loop;
                    }
                    Thread.yield();
                }
            }
            Thread.yield();
        }
        Message.logger.info(this+" is exiting");
    }
    @Override public String toString() {
        return "home has: "+acceptor.connections.size()+" connections.";
    }
    public static void main(String[] args) throws IOException,InterruptedException {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        Home home=new Home();
        ServerSocket serverSocket=home.getServerSocket();
        Group group=home.group();
        Server server=new Server(group,serverSocket);
        server.start();
    }
    private final Group group;
    private final InetAddress homeInetAddress;
    final Set<Integer> special=new TreeSet<>();
    { // working android tablets
        special.add(1);
        special.add(2);
    }
    final Map<Integer,TcpConnection> map=new TreeMap<>();
    final Acceptor acceptor;
    final Vector<Message> messages=new Vector<>(100);
    public static final Integer illegalTabletId=Integer.MIN_VALUE;
    // this should be localhost
    // maybe make non static if we want a tablet to be home?
    public static final Logger logger=Logger.getLogger(Server.class.getName());
}
