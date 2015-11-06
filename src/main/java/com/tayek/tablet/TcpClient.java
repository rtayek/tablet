package com.tayek.tablet;
import java.io.*;
import java.net.*;
import com.tayek.tablet.Home.GetSocket;
import com.tayek.tablet.Message.*;
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
    public TcpClient(Group group,int tabletId,Receiver receiver) throws IOException {
        // reduce visibility and put create back in when the dust settles
        this.group=group.newGroup();
        this.tabletId=tabletId;
        this.receiver=receiver;
        // do this here, but maybe no-op it out for android to see what
        // hapens?
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
    public void connect() throws IOException,InterruptedException {
        if(socket()==null) {
            GetSocket getSocket=new GetSocket(Home.inetAddress(),Home.port(0));
            new Thread(getSocket).start();
            while(getSocket.socket()==null)
                Thread.sleep(100);
            setSocket(getSocket.socket());
        }
    }
    public void start() throws IOException,InterruptedException {
        shuttingDown=false;
        if(socket()==null) connect();
        logger.info("starting new thread for: "+this);
        thread=new Thread(this,"client "+tabletId);
        thread.start();
    }
    public void stop() {
        shuttingDown=true;
        if(socket!=null) {
            System.out.println("not null");
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
            try {
                // if server goes down, we loop forever
                // getting connection reset thrown from here
                // we loop forever beause i keep forgetting
                // to put in a break in the catch to get out of the while
                // loop!
                String string=in.readLine();
                if(string==null) {
                    System.out.println("eof run()!");
                    stop();
                    break;
                }
                read++;
                Message message=Message.from(string);
                Group.captureInetAddress(group,socket,message);
                if(receiver!=null) {
                    receiver.receive(message);
                } else logger.warning(this+" has a null receiver!");
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
    Thread thread;
    final Group group;
    public final Integer tabletId;
    private Socket socket;
    private BufferedReader in;
    private Writer out;
    final Receiver receiver;
    Integer sent=0,read=0;
    private boolean shuttingDown;
    // public static final Logger
    // staticLogger=Logger.getLogger(TcpClient.class.getName());
}
