package com.tayek.tablet;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import com.tayek.utilities.*;
public class UdpFactory { // really a finder for the tablets
    public UdpFactory() throws SocketException,IOException {
        this(Group.create(1,1,1));
    }
    public UdpFactory(Group group) throws SocketException,IOException {
        this.group=group;
        me=Udp.create(group,0,null);
        // udps.put(0,me); // maybe not
    }
    // uses Udp
    void find() throws SocketException,IOException,UnknownHostException,InterruptedException {
        received.clear();
        int threads=Thread.activeCount();
        me.receiver=new Udp.Receiver() { // just me pass along hellos
            @Override public void receive(String string) {
                String[] parts=string.split(" ");
                if(!parts[0].equals("0")) if(parts[1].equals("hello")) {
                    // replace these with control names
                    // maybe allow this to find myself
                    Integer i=Utility.toInteger(parts[0]);
                    received.put(i,string);
                }
            }
        };
        // me.startFactory();
        System.out.println("udps: "+udps);
        for(Udp udp:udps.values())
            udp.startFactory();
        Thread.sleep(10);
        InetAddress address=InetAddress.getLocalHost();
        // loop through a range of ip addresses
        // and loop through a range of ports
        // but for now, it's just one range of ports on local host
        Et et=new Et();
        String message="0 start";
        for(int i:udps.keySet()) {
            int port=Home.port(i);
            try {
                if(me.socket.isBound()&&!me.socket.isClosed()) {
                    System.out.println("sending: "+message+" to "+address+"/"+port);
                    Utility.send(message,address,port,me.socket);
                } else me.logger.fine(this+" can not send!");
            } catch(IOException e) {
                System.out.println("send: tablet "+i+" caught: "+e);
            }
        }
        Thread.sleep(10);
        System.out.println("timer");
        MyTimer timer=new MyTimer(new Runnable() {
            @Override public void run() {
                while(received.size()<group.info.keySet().size())
                    if(true) {
                        try {
                            Thread.sleep(1);
                        } catch(InterruptedException e) {
                            if(true) { // replace with flag?
                                ;// System.out.println("timer caught: "+e);
                                 // e.printStackTrace();
                            }
                            break;
                        }
                    } else Thread.yield();
                // System.out.println("found: "+received.size());
            }
        },10);
        System.out.println("after timer");
        double dt=timer.time();
        // timer.check(); // seems to work ok (some of the time)
        System.out.println("found "+received.size()+" in "+dt+" ms.");
        timeHistogram.add(dt);
        foundHistogram.add(received.size()*1./udps.size());
        // System.out.println("done in: "+dt+" ms.");
        System.out.println(received);
        timer=new MyTimer(new Runnable() {
            @Override public void run() {
                for(Udp udp:udps.values())
                    try {
                        // System.out.println("stopping: "+udp);
                        udp.stop();
                    } catch(Exception e) {
                        // e.printStackTrace();
                        break;
                    }
                try {
                    me.stop();
                } catch(InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        },200);
        dt=timer.time();
        System.out.println("shutdown took: "+dt+" ms.");
        // timer.check(); // seems to hang
        // System.out.println("after shutdown");
        if(Thread.activeCount()>threads) {
            ;// System.out.println("extra threads!");
            ;// Utility.printThreads();
        }
        // System.out.println("exit");
    }
    void useFind() throws SocketException,UnknownHostException,IOException,InterruptedException {
        for(int i=1;i<=1;i++) {
            // System.out.println(i);
            me.logger.fine(""+i);
            find();
            if(received.size()==udps.size()) {
                System.out.println("we found it! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                System.out.flush();
                if(true) System.exit(1);
            }
            if(i%10==0) {
                System.out.println("---");
                System.out.println("time: "+timeHistogram);
                System.out.println("found rate: "+foundHistogram);
                // add shutdown time
            }
        }
    }
    public static void main(String[] args) throws SocketException,UnknownHostException,IOException,InterruptedException {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        for(Class<?> x:Message.map.keySet())
            System.out.println(x+":"+Message.map.get(x).getLevel());
        Group group=Group.create(1,1);
        // factory is a not a good name
        UdpFactory factory=new UdpFactory(group);
        // x.me.logger.setLevel(Level.ALL);
        // x.me.logger.finest("foo");
        if(false) {
            for(int i=1;i<=1;i++) {
                factory=new UdpFactory(group); // let's try a new factory each
                                               // time
                System.out.println("outer: "+i);
                Udp udp=Udp.create(group,3,null);
                factory.udps.put(3,udp);
                factory.useFind();
                staticTimeHistogram.add(factory.timeHistogram.mean());
                staticFoundHistogram.add(factory.foundHistogram.mean());
                System.out.println("------");
                System.out.println("static time: "+staticTimeHistogram);
                System.out.println("static found rate: "+staticFoundHistogram);
            }
        } else {
            for(int tabletId:group.info.keySet()) {
                Udp udp=Udp.create(group,tabletId,null);
                factory.udps.put(tabletId,udp);
            }
            factory.useFind();
        }
    }
    final Udp me;
    final Map<Integer,String> received=new TreeMap<>();
    final Map<Integer,Udp> udps=new TreeMap<>(); // just the ones we are looking
                                                 // for
    final Group group;
    Histogram timeHistogram=new Histogram(10,0,100);
    Histogram foundHistogram=new Histogram(10,0,1);
    static Histogram staticTimeHistogram=new Histogram(10,0,100);
    static Histogram staticFoundHistogram=new Histogram(10,0,1);
}
