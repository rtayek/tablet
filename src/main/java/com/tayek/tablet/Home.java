package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.logging.*;
import com.tayek.tablet.Home.GetSocket;
import com.tayek.utilities.*;
public class Home {
    // every client has a group (all the same)
    // every client has info about tablet 0 (the current home)
    // home on the laptop needs to have the same group
    // he will have info about all of the tablets
    // maybe a tablet will be able to be a home.
    // so the groups need to be the same, but separate instances.
    // now that we have clone, maybe each clieint should have a home
    // since it's just a group with an ip and a port
    // also, if we could clone the models from it
    // then it's like a factory that can move around
    public static class GetSocket implements Runnable {
        GetSocket(InetAddress inetAddress,int service) {
            this.inetAddress=inetAddress;
            this.service=service;
        }
        // expand this class. make it more usable
        // also, make this work like it does on the droid
        // i.e. use the thread and wait or time out
        @Override public void run() {
            socket=get();
            logger.info("got socket: "+socket);
        }
        public Socket socket() {
            return socket;
        }
        public Socket get() { // for clients to connect to
            logger.info("host: "+inetAddress);
            Socket socket=null;
            try {
                socket=new Socket(inetAddress,service);
            } catch(IOException e) {
                logger.info("socket for: "+inetAddress+"/"+service+" caught: "+e);
            }
            logger.info("socket: "+socket);
            return socket;
        }
        private final InetAddress inetAddress;
        private final int service;
        private Socket socket;
    }
    void run() throws IOException { // only run on the server
        ServerSocket serverSocket=new ServerSocket(port(0));
        Group group=Group.create(1,1);
        // belongs in some config or properties file.
        Properties properties=group.properties();
        Writer writer=new FileWriter(new File("home.properties"));
        properties.store(writer,null);
        writer.close();
        Server server=new Server(group,serverSocket);
        server.start();
    }
    public static InetAddress inetAddress() {
        return inetAddress;
    }
    public static int port(int tabletId) {
        return port+tabletId;
    }
    public static Group group() {
        return group;
    }
    public static Properties load(final InputStream inputStream) { // from jat/
        final Properties p=new Properties(/*defaultProperties*/); // add defaults
        try {
            p.load(inputStream);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return p;
    }
    public static Properties load(final File propertiesFile) { // from jat/
        Properties p=null;
        try {
            final InputStream in=new FileInputStream(propertiesFile);
            p=load(in);
        } catch(FileNotFoundException e) {
            System.out.println(e);
        }
        return p;
    }
    static void init() {
        Properties properties=load(new File("home.properties"));
        properties.list(System.out);
        if(inetAddress==null) {
            try {
                inetAddress=InetAddress.getLocalHost(); // must be done for
                                                        // clients
                // that's sill, localhost is probably the wrong thing
            } catch(UnknownHostException e) {
                System.out.println("can not get internet address of localhost!");
            }
        }
        if(port==null) port=20000; // must be done for home, so he knows what
                                   // port to use
        // clients need this also
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        God.home.init();
        if(false) {
            // looks like this is all bogus
            // he just gets a server socket on some port!
            // so all these addresses are for the tablets! (clients)
            InetAddress inetAddress2=InetAddress.getByAddress(new byte[] {(byte)192,(byte)168,1,101});
            Home.inetAddress=inetAddress2;
            God.home.init();
            if(arguments.length>0) // allow override from command line
            {
                inetAddress=InetAddress.getByName(arguments[0]);
                Home.inetAddress=inetAddress;
            }
        }
        Home home=new Home();
        home.run();
    }
    // if we are the laptop home server, host is localhost or some variant and
    // port is home+0
    // if we are not the home server, then host is some other guy (the laptop),
    // and his port is 0
    // what if we are a client, say 1 and we want to be home also?
    // if we are 1, then host is local host and port is home+0
    // if we are not 1, then host is 1's ip address and port is home+0
    // or since we know everyone's ip address, we can just have a conection to
    // each.
    public static Integer port;
    public static InetAddress inetAddress;
    private static Group group=Group.create(1,1);
    public static final Logger logger=Logger.getLogger(Server.class.getName());
}
