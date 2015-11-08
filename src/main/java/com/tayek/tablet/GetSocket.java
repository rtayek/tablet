package com.tayek.tablet;
import java.io.IOException;
import java.net.Socket;
public class GetSocket implements Runnable {
    public GetSocket(String host,int service) {
        this.host=host;
        this.service=service;
        /*
        InetAddress inetAddress=InetAddress.getByName(host);
        SocketAddress socketAddress=new InetSocketAddress(inetAddress,service);
        Socket socket=new Socket();
        socket.connect(socketAddress,100);
        */
    }
    // expand this class. make it more usable
    // also, make this work like it does on the droid
    // i.e. use the thread and wait or time out
    @Override public void run() {
        socket=get();
        System.out.println("got socket: "+socket);
    }
    public Socket socket() {
        return socket;
    }
    // http://stackoverflow.com/questions/5632279/how-to-set-timeout-on-client-socket-connection
    public Socket get() { // for clients to connect to
        Socket socket=null;
        try {
            socket=new Socket(host,service);
        } catch(IOException e) {
            System.out.println("socket for: "+host+"/"+service+" caught: "+e);
        }
        System.out.println("socket: "+socket);
        return socket;
    }
    final String host;
    final int service;
    private Socket socket;
}
