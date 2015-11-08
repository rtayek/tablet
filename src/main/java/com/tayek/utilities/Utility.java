package com.tayek.utilities;
import java.io.IOException;
import java.net.*;
import com.tayek.tablet.*;
public class Utility {
    public static Integer toInteger(String argument) {
        Integer n=null;
        try {
            n=Integer.valueOf(argument);
        } catch(NumberFormatException e) {
            System.out.println(argument+" is not a valid tabletId");
        }
        return n;
    }
    public static int pack(byte[] bytes) {
        int val=0;
        for(int i=0;i<bytes.length;i++) {
            val<<=8;
            val|=bytes[i]&0xff;
        }
        return val;
    }
    public static byte[] unpack(int bytes) {
        return new byte[] {(byte)((bytes>>>24)&0xff),(byte)((bytes>>>16)&0xff),(byte)((bytes>>>8)&0xff),(byte)((bytes)&0xff)};
    }
    public static InetAddress inetAddress(int address) {
        String string=null;
        InetAddress inetAddress=null;
        try {
            string=InetAddress.getByAddress(unpack(address)).getHostAddress();
        } catch(UnknownHostException e) {}
        try {
            inetAddress=InetAddress.getByName(string);
        } catch(UnknownHostException e) {}
        return inetAddress;
    }
    public static InetAddress inetAddressOld(int n) {
        InetAddress inetAddress=null;
        try {
            inetAddress=InetAddress.getByName(""+n);
        } catch(UnknownHostException e) {}
        return inetAddress;
    }
    public static int toInteger(InetAddress inetAddress) {
        int result=0;
        if(inetAddress!=null) for(byte b:inetAddress.getAddress())
            result=result<<8|(b&0xFF);
        return result;
    }
    public static String toString(Thread thread) {
        return thread.getName()+": "+thread.getState()+" alive: "+thread.isAlive()+" has been interrupted: "+thread.isInterrupted();
    }
    public static String toString(DatagramSocket socket) {
        return "was bound: "+socket.isBound()+", was closed: "+socket.isClosed()+", was connected: "+socket.isConnected();
    }
    public static void print(Thread[] threads,int now) {
        System.out.println("threads:");
        for(int i=0;i<now;i++)
            System.out.println(threads[i]);
    }
    public static void printSocket(DatagramSocket socket,String prefix) throws SocketException {
        System.out.println(prefix+":"+"inetAddress: "+socket.getInetAddress());
        System.out.println(prefix+":"+"port: "+socket.getPort());
        System.out.println(prefix+":"+"local port: "+socket.getLocalPort());
        System.out.println(prefix+":"+"local address: "+socket.getLocalAddress());
        System.out.println(prefix+":"+"local socket address: "+socket.getLocalSocketAddress());
        System.out.println(prefix+":"+"remote socket address: "+socket.getRemoteSocketAddress());
        System.out.println(prefix+":"+"reuse address: "+socket.getReuseAddress());
    }
    public static DatagramSocket createSocket() throws SocketException {
        return new DatagramSocket(null);
    }
    public static void send(Object message,InetAddress ipAddress,int destinationPort,DatagramSocket socket) throws IOException {
        byte[] buffer=message.toString().getBytes();
        DatagramPacket packet=new DatagramPacket(buffer,buffer.length,ipAddress,destinationPort);
        if(socket.isBound()&&!socket.isClosed()) socket.send(packet);
        else System.out.println("can not send: "+Utility.toString(socket));
    }
    public static Pair<Integer,Thread[]> getThreads() {
        int big=2*Thread.activeCount();
        Thread[] threads=new Thread[big];
        int active=Thread.enumerate(threads);
        return new Pair<>(active,threads);
    }
    public static void waitForThreads(int then) {
        boolean ok=false;
        while(!ok) {
            Pair<Integer,Thread[]> pair=getThreads();
            int active=pair.first;
            ok=active<=then;
            if(ok) {
                // System.out.println(active+"-"+then+"="+(active-then));
                // print(threads,then);
                break;
            }
            Thread.yield();
        }
    }
    public static boolean checkAndPrintThreads(int then,boolean print,String string) {
        if(string!=null) System.out.println(string);
        Pair<Integer,Thread[]> pair=getThreads();
        int now=pair.first;
        boolean ok=true;
        if(then>=0) {
            ok=now<=then;
            if(!ok) System.out.println(now+"-"+then+"="+(now-then)+" extra thread(s).");
            if(!ok||print) print(pair.second,now);
        } else print(pair.second,now);
        return ok;
    }
    public static boolean checkThreads(int threads) {
        return checkAndPrintThreads(threads,false,null);
    }
    public static void printThreads() {
        printThreads(null);
    }
    public static void printThreads(String string) {
        checkAndPrintThreads(-1,true,string);
    }
}
