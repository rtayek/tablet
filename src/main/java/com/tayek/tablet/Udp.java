package com.tayek.tablet;
import java.io.IOException;
import java.lang.Thread.State;
import java.net.*;
import java.util.logging.*;
import com.tayek.utilities.*;
import static com.tayek.utilities.Utility.*;
//http://www.uml-diagrams.org/examples/java-6-thread-state-machine-diagram-example.html
public class Udp {
    interface Receiver {
        void receive(String string);
    }
    private Udp(Group group,int tabletId,DatagramSocket socket,Udp.Receiver receiver) throws SocketException {
        this.group=group;
        this.tabletId=tabletId;
        this.socket=socket;
        this.receiver=receiver;
    }
    @Override public String toString() {
        String tid=""+(!tabletId.equals(Server.illegalTabletId)?tabletId.toString():"?");
        return group.groupId+":"+tid+" on "+socket.getLocalSocketAddress();
    }
    static Udp create(final Group group,final int tabletId,Udp.Receiver receiver) throws SocketException,IOException {
        DatagramSocket socket=bind(tabletId);
        return new Udp(group,tabletId,socket,receiver);
    }
    void start(Runnable runnable,String name) {
        thread=new Thread(runnable,name);
        thread.start();
    }
    void startTest() {
        Runnable runnable=new Thread(new Runnable() { // for testing
            @Override public void run() {
                try {
                    logger.fine(this+" blocked on read");
                    socket.receive(packet);
                    synchronized(this) {
                        Udp.this.testMessage=new String(packet.getData(),0,packet.getLength());
                    }
                    socket.close(); // try adding a close
                } catch(IOException e) {
                    synchronized(isShuttingDown) {
                        if(isShuttingDown) logger.fine(Udp.this+"shutting down, caught: "+e);
                        else throw new RuntimeException(e);
                    }
                }
            }
            final byte[] buffer=new byte[256];
            final DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
        },"receiver");
        start(runnable,"test");
    }
    void startFactory() {
        Runnable runnable=new Thread(new Runnable() { // special run() for
                                                      // factory
            @Override public void run() {
                while(true) {
                    String string=null;
                    try {
                        logger.fine(Udp.this+" waitiing to receive.");
                        socket.receive(packet);
                        synchronized(this) {
                            string=new String(packet.getData(),0,packet.getLength());
                        }
                        logger.fine(Udp.this+" received "+string);
                        // look for stop and shut down gracefully?
                        // System.out.println("tablet: "+tabletId+" received:
                        // "+string);
                        // sometimes we (the other run method for testing) do
                        // shut down gracefully
                        // seems like it's when we find all of the tablets in
                        // time
                        // let's try having the tablets stop
                        // after replying to one start message
                        String[] parts=string.split(" ");
                        if(tabletId==0) {
                            if(parts[0].equals("0")) logger.warning("strange: "+string);
                            else {
                                if(receiver!=null) receiver.receive(string);
                                else logger.warning("factory has null receiver of for me!");
                            }
                        } else {
                            if(parts[0].equals("0")) {
                                String reply=tabletId+" hello";
                                // System.out.println("replying with: "+reply);
                                Utility.send(reply,packet.getAddress(),packet.getPort(),socket);
                                logger.fine(Udp.this+" will exit now.");
                                break;
                            } else logger.warning("igoring: "+string);
                        }
                        // if(receiver!=null) receiver.receive(message);
                    } catch(IOException e) {
                        synchronized(isShuttingDown) {
                            if(isShuttingDown) {
                                logger.fine(Udp.this+" shutting down, caught:"+e);
                                break;
                            } else throw new RuntimeException(e);
                        }
                    }
                    if(false) break;
                }
                logger.fine(Udp.this+" exit run()");
            }
            final byte[] buffer=new byte[256];
            final DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
        },"receiver");
        start(runnable,"factory");
    }
    void send(String string) throws UnknownHostException,IOException {
        // get rid of the call to get local host!
        if(socket.isBound()&&!socket.isClosed()) Utility.send(string,InetAddress.getLocalHost(),Home.port(tabletId),socket);
        else logger.fine(this+" can not send!");
    }
    void waitForMessage(int sleep) throws InterruptedException {
        int t=sleep!=0?sleep/10:0;
        Et et=new Et();
        while(true) {
            synchronized(testMessage) {
                if(!testMessage.equals(initialValue)) {
                    logger.fine("value changed!");
                    break;
                }
            }
            if(et.etms()<sleep) Thread.sleep(t);
            else {
                logger.fine("timeout!");
                break;
            }
        }
    }
    void stop() throws InterruptedException {
        logger.fine("sync");
        synchronized(isShuttingDown) {
            isShuttingDown=true;
        }
        logger.fine("synced");
        if(thread!=null) {
            State state=thread.getState();
            logger.fine("thread is: "+state);
            switch(state) {
                // case NEW:
                // break;
                case RUNNABLE:
                    if(!socket.isClosed()) {
                        logger.fine("closing: "+Utility.toString(socket));
                        socket.close();
                        logger.fine("after closing: "+Utility.toString(socket));
                        logger.fine("thread is: "+Utility.toString(thread));
                        Thread.sleep(10);
                        logger.fine("interupt");
                        thread.interrupt();
                        logger.fine("thread is: "+Utility.toString(thread));
                    } else logger.fine("socket is closed.");
                    logger.fine("joining.");
                    thread.join();
                    logger.fine("joined.");
                    break;
                case BLOCKED:
                    logger.fine("joining.");
                    thread.join();
                    logger.fine("joined.");
                    break;
                case TERMINATED:
                    logger.fine("joining.");
                    thread.join();
                    logger.fine("joined.");
                    break;
                // case TIMED_WAITING:
                // break;
                // case WAITING:
                // break;
                default:
                    throw new RuntimeException(state+" implement this!");
            }
        }
    }
    void test4() throws IOException,InterruptedException {
        int tooToLongs=0;
        startTest();
        send(expected);
        Et et=new Et();
        boolean failed=false;
        if(true) Thread.sleep(100);
        else {
            waitForMessage(300);
        }
        if(failed) tooToLongs++;
        else logger.fine("took: "+et);
        stop();
        printThreads();
        if(failed) {
            logger.warning("took to long!");
        } else synchronized(testMessage) {
            if(!testMessage.equals(expected)) logger.warning("fail: "+testMessage);
        }
    }
    // maybe move this to a unit test
    void startSendAndWait() throws InterruptedException,IOException {
        logger.setLevel(Level.OFF);
        logger.info("fooo");
        // with the same udp!
        Et et=new Et();
        boolean failed=true;
        // logger.setLevel(Level.ALL);
        threads=Thread.activeCount();
        startTest();
        Thread.yield();
        // maybe make this the default runnable
        // and let people override it?
        // Thread.sleep(10);
        try {
            send(expected);
            logger.fine(this+" sent");
            Thread.yield();
            ;
            waitForMessage(5); // too short?
            logger.fine(this+" after wait");
            // testMessage="xx"; // force failure
        } catch(Exception e) {
            logger.info("caught "+e);
            e.printStackTrace();
        }
        double time=et.etms();
        if(testMessage.equals(initialValue)) logger.fine(this+" we did not receive a message!");
        else {
            if(testMessage.equals(expected)) {
                failed=false;
                logger.fine("it worked!");
            } else logger.warning(this+" failed, received: "+testMessage);
        }
        elapsedTimeHistogram.add(failed?Double.NaN:time);
        failureRateHistogram.add(failed?1:0);
        stop();
        if(threads!=Thread.activeCount()) {
            logger.warning("extra threads");
            printThreads();
        }
    }
    // void run() {}
    void runSome() throws InterruptedException,IOException {
        Et et=new Et();
        System.out.println("run some");
        boolean forever=true;
        int n=10;
        for(int i=1;i<=n;i++) {
            et.reset();
            startSendAndWait();
            // System.out.println(i+" et: "+et);
            if(i%1000==0) {
                System.out.println("---");
                System.out.println(i+" time: "+elapsedTimeHistogram);
                System.out.println(i+" failure rate: "+failureRateHistogram);
            }
            if(!forever) break;
            Thread.sleep(0);
        }
        System.out.println("time: "+elapsedTimeHistogram);
        System.out.println("failure rate: "+failureRateHistogram);
        if(elapsedTimeHistogram.missing()==0) {
            System.out.println("they all worked!");
            //System.exit(1);
        }
        if(failureRateHistogram.sum()==n) System.out.println("they all failed!");
    }
    public static void main(String[] arguments) throws InterruptedException,IOException {
        God.log.init();
        // printSocket(udp.socket,"Udp()");
        if(false) {
            Group group=Group.create(1,1,1);
            Udp udp=Udp.create(group,1,null);
            udp.logger.setLevel(Level.INFO);
            udp.test4();
            return;
        } else {
            for(int i=1;i<=10;i++) {
                Group group=Group.create(1,1,1);
                Udp udp=Udp.create(group,1,null);
                udp.logger.setLevel(Level.ALL);
                udp.runSome();
                staticElapsedTimeHistogram.add(udp.elapsedTimeHistogram.mean());
                staticFailureRateHistogram.add(udp.failureRateHistogram.mean());
                System.out.println("------");
                System.out.println("static time: "+staticElapsedTimeHistogram);
                System.out.println("static failure rate: "+staticFailureRateHistogram);
            }
        }
    }
    final Group group;
    final Integer tabletId;
    final DatagramSocket socket;
    Udp.Receiver receiver; // not final so we can set it sometimes
    Thread thread;
    int threads;
    volatile Boolean isShuttingDown=false;
    final String expected="bar";
    final String initialValue="foo";
    volatile String testMessage=initialValue;
    Histogram elapsedTimeHistogram=new Histogram(10,0,100);
    Histogram failureRateHistogram=new Histogram(10,0,1);
    static Histogram staticElapsedTimeHistogram=new Histogram(10,0,100);
    static Histogram staticFailureRateHistogram=new Histogram(10,0,1);
    static DatagramSocket staticSocket;
    public final Logger logger=Logger.getLogger(getClass().getName());
}
