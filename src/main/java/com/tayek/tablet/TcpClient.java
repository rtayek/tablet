package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import com.tayek.tablet.model.Message;
import com.tayek.tablet.model.Message.*;
import com.tayek.utilities.*;
//
// public interface OnMessageReceived {
// public void messageReceived(String message);
// }
// ...
// public TcpClient(OnMessageReceived listener) ...
//
// maybe all of my stuff should use a callback
public class TcpClient implements Sender,Runnable { // sender?
    // one instance per tablet - uses home
    public TcpClient(Group group,int tabletId,Receiver<Message> receiver) {
        // reduce visibility and put create back in when the dust settles
        this.group=group;
        this.tabletId=tabletId;
        this.receiver=receiver;
        // do this here, but maybe no-op it out for android to see what
        // happens?
        System.out.println(this+" has address: "+group.inetAddress(tabletId));
        if(socket()!=null) {
            group.checkForInetAddress(tabletId,socket());
            // maybe get rid of in and out?
        }
    }
    public Socket socket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket=socket;
        try {
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=new OutputStreamWriter(socket.getOutputStream());
            System.out.println(this+" connected to: "+socket);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void start() {
        shuttingDown=false;
        Socket socket=home.connectUsingThread(100);
        System.out.println("start sees socket: "+socket);
        if(socket!=null) {
            setSocket(socket);
            logger.info("starting new thread for: "+this);
            thread=new Thread(this,"client "+tabletId);
            thread.start();
        } else System.out.println(this+" no connection!, not started!");
    }
    public void stop() {
        shuttingDown=true;
        if(socket!=null) {
            System.out.println("not null");
            if(!socket.isConnected()) System.out.println("socket was never connected!");
            try {
                socket.shutdownOutput();
                System.out.println("did it work? "+socket.isOutputShutdown());
                socket.shutdownInput();
                System.out.println("did it work? "+socket.isInputShutdown());
                socket.close();
                System.out.println("did it work? "+socket.isClosed());
                System.out.println("did it work? "+socket.isConnected());
            } catch(IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if(!socket.isClosed()) try {
                socket.close();
            } catch(IOException e) {
                logger.warning(this+" caught1: "+e);
            }
            socket=null;
        } else System.out.println("socket was null in stop!");
    }
    @Override public void run() {
        logger.info(this+" is reading from: "+socket);
        while(true) {
            String string=null;
            try {
                string=in.readLine();
                if(string==null) {
                    System.out.println("eof run()!");
                    stop();
                    break;
                }
            } catch(SocketException e) {
                if(!shuttingDown) {
                    logger.warning(this+" caught: "+e);
                    logger.warning(this+" message is  "+e.getMessage());
                    // if this is a reset connection
                    // die and reconnect to server?
                }
                break;
            } catch(IOException e) {
                logger.warning(this+" caught: "+e);
                // if this is a reset connection
                // die and reconnect to server?
                e.printStackTrace();
                break;
            }
            read++;
            Message message=Message.from(string);
            if(message.tabletId.equals(tabletId)) logger.finest(tabletId+" received: "+message+" (from self).");
            else {
                logger.finer(tabletId+" received: "+message);
                group.captureInetAddress(socket,message);
                if(receiver!=null) {
                    receiver.receive(message);
                } else logger.warning(this+" has a null receiver!");
            }
            Thread.yield();
        }
        System.out.println(this+" exit run()");
    }
    public void send(Message message) {
        if(socket!=null&&socket.isClosed()) {
            System.out.println(this+" socket is closed, "+message+" not sent!");
            return;
        }
        if(message.isControl()) {
            group.addAddressToMessage(tabletId,message);
        }
        logger.fine(this+" is sending: "+message);
        try {
            out.write(message.toString()+"\n");
            out.flush();
        } catch(IOException e) {
            System.out.println(this+" write failed: "+e);
        }
        sent++;
    }
    @Override public String toString() {
        return "tablet: "+tabletId;
    }
    public String toString2() {
        return toString()+" sent: "+sent+", received: "+read;
    }
    @Override public Integer tabletId() {
        return tabletId;
    }
    public Group group() {
        return group;
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        Home home=new Home();
        Set<TcpClient> clients=new LinkedHashSet<>();
        for(int tabletId:home.group().info.keySet()) { // use home's id's
            Group group=home.group().newGroup(); // clone the group
            TcpClient client=new TcpClient(group,tabletId,null);
            clients.add(client);
            client.start();
            if(client.socket()!=null) {
                // see if we can set this at startup?
                InetAddress inetAddress=group.checkForInetAddress(tabletId,client.socket());
                int address=inetAddress!=null?Utility.toInteger(inetAddress):0;
                Message message=new Message(group.groupId,tabletId,Message.Type.start,address);
                client.send(message);
            }
        }
        Thread.sleep(5000);
        for(TcpClient client:clients)
            client.stop();
        System.out.println("--------------------");
        for(TcpClient client:clients) {
            System.out.println(client.tabletId+" knows: ");
            client.group.print();
        }
    }
    Home home=new Home();
    Thread thread;
    final Group group;
    public final Integer tabletId;
    private Socket socket;
    private BufferedReader in;
    private Writer out;
    final Receiver<Message> receiver;
    Integer sent=0,read=0;
    private boolean shuttingDown;
    // public static final Logger
    // staticLogger=Logger.getLogger(TcpClient.class.getName());
}
