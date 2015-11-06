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
        public GetSocket(String host,int service) {
            this.host=host;
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
            Socket socket=null;
            try {
                socket=new Socket(host,service);
            } catch(IOException e) {
                logger.info("socket for: "+host+"/"+service+" caught: "+e);
            }
            logger.info("socket: "+socket);
            return socket;
        }
        final String host;
        final int service;
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
    public static int port(int tabletId) {
        return service+tabletId;
    }
    public static Group group() {
        return group;
    }
    public static Properties load(final InputStream inputStream) { // from jat/
        final Properties p=new Properties(/*defaultProperties*/); // add
                                                                  // defaults
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
    public static void init() { // droid needs this run on a thread!
        Properties properties=load(new File("home.properties"));
        if(properties!=null) properties.list(System.out);
        else System.out.println("failed to load propertiies!");
        if(host==null)
            host="192.168.1.101";
        if(service==null)
            service=30_000;
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        God.home.init();
    }
    public static String host;
    public static Integer service=30_000;
    private static Group group=Group.create(1,1);
    public static final Logger logger=Logger.getLogger(Server.class.getName());
}
