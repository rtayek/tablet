package com.tayek.tablet;
import java.io.IOException;
import java.net.*;
import com.tayek.utilities.Utility;
public class Finder {
    Finder(int tabletId) {
        this.tabletId=tabletId;
    }
    class Client { // me finding ip address of tabletId
        Client() throws SocketException {
            socket=new DatagramSocket();
        }
        InetAddress send(String string) throws IOException {
            byte[] buffer=string.toString().getBytes();
            InetAddress address=InetAddress.getLocalHost();
            DatagramPacket packet=new DatagramPacket(buffer,buffer.length,address,Home.port(tabletId));
            socket.send(packet);
            socket.receive(packet);
            // String response=new
            // String(packet.getData(),0,packet.getLength());
            InetAddress address2=packet.getAddress();
            // int port2=packet.getPort();
            return address2;
        }
        final DatagramSocket socket;
    }
    class FactoryServer implements Runnable {
        FactoryServer() throws SocketException { // a tablet that will be on it's port
            socket=new DatagramSocket(Home.port(tabletId));
            socket.setSoTimeout(1000);
        }
        void receive() throws IOException {
            byte[] buffer=new byte[256];
            DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
            System.out.println("tablet "+tabletId+" waiting on: "+Utility.toString(socket));
            socket.receive(packet);
            String string=new String(packet.getData(),0,packet.getLength());
            // System.out.println("received: "+string);
            InetAddress address=packet.getAddress();
            int port=packet.getPort();
            String reply=""+tabletId;
            buffer=reply.getBytes();
            packet=new DatagramPacket(buffer,buffer.length,address,port);
            socket.send(packet);
        }
        @Override public void run() {
            try { // just once
                receive();
                hasReplied=true;
            } catch(SocketTimeoutException e) {
                System.out.println("caught: "+e);
                
            }catch(IOException e) {
                System.out.println("caught: "+e);
                e.printStackTrace();
            }
            Utility.printThreads();
            System.out.println("exit run()");
            if(hasReplied)
            System.out.println("tablet: "+tabletId+" has told! &&&&&&&&&&&&&&&&&");
            else System.out.println("tablet: "+tabletId+" did not tell! &&&&&&&&&&&&&&&&&");
        }
        final DatagramSocket socket;
        boolean hasReplied;
    }
    public static InetAddress find(int tabletId) {
        Finder finder=new Finder(tabletId);
        Client client=null;
        try {
            client=finder.new Client();
        } catch(SocketException e) {
            e.printStackTrace();
        }
        InetAddress address=null;
        try {
            address=client.send("foo");
            client.socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return address;
    }
    public static Thread tell(int tabletId) {
        Finder finder=new Finder(tabletId);
        FactoryServer server=null;
        try {
            server=finder.new FactoryServer();
        } catch(SocketException e) {
            e.printStackTrace();
            return null;
        }
        Thread thread=new Thread(server,"server");
        thread.start();
        return thread;
    }
    public static void main(String[] args) {
        int tabletId=1;
        Thread thread=tell(tabletId);
        System.out.println(thread);
        InetAddress address=find(tabletId);
        System.out.println("tablet: "+tabletId+" is at: "+address);
        // Utility.printThreads();
        // System.out.println("exit main");
    }
    final int tabletId;
}
