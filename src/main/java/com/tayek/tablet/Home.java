package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.logging.*;
import com.tayek.utilities.*;
public class Home {
    public Home(Group group) {
        this.group=group;
        if(staticInetAddress==null) {
            God.home.init();
            if(staticInetAddress==null) throw new RuntimeException(" can not get inetAddress!");
        }
        inetAddress=staticInetAddress;
    }
    public Home() {
        this(Group.create(1,1));
    }
    // every client has a group (all the same)
    // every client has info about tablet 0 (the current home)
    // home on the laptop needs to have the same group
    // he will have info about all of the tablets
    // maybe a tablet will be able to be a home.
    // so the groups need to be the same, but separate instances.
    // so we have clone.
    // write a command line ui for this!
    // http://stackoverflow.com/questions/18153644/android-asynctask-and-threading
    public ServerSocket getServerSocket() {
        try {
            return new ServerSocket(port(0));
        } catch(IOException e) {
            System.out.println("caught: "+e);
        }
        return null;
    }
    public int port(int tabletId) {
        return service+tabletId;
    }
    public Group group() {
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
    // looks like we need a swicth:
    // if we are a client, use host
    // if we are home, set host to localhost
    static void init() { // droid needs this run on a thread!
        if(false) { // get as much as possoble from properties file
            Properties properties=load(new File("home.properties"));
            if(properties!=null) properties.list(System.out);
            else System.out.println("failed to load propertiies!");
        }
        if(host==null) host="192.168.1.101";
        if(service==null) service=30_000;
        if(staticInetAddress==null) try {
            staticInetAddress=InetAddress.getByName(host);
            System.out.println("home inet address: "+staticInetAddress);
        } catch(UnknownHostException e) {
            System.out.println("home caught: "+e);
            System.out.println("home init failed!");
        }
    }
    Properties properties() { // this will not have the right buttons for each
        // tablet!
        // move it to the client or the tablet or the model!
        // perhaps, but maybe exclude the stat and just use to configure the
        // tablets.
        Properties properties=new SortedProperties();
        properties.put("home",Home.host.toString());
        properties.put("service",Home.service.toString());
        properties.put("buttons",group.newModel().buttons.toString());
        // for(int i=1;i<=model.buttons;i++)
        // properties.put("button"+i,model.state(i).toString());
        for(Integer i:group.info.keySet()) // will be home's info now!
            properties.put("tablet"+i,group.info.get(i).toString());
        return properties;
    }
    public InetAddress getInetAddress() {
        return inetAddress;
    }
    public boolean isConnectedx(Socket socket) {
        return socket!=null&&socket.isBound()&&!socket.isClosed()&&socket.isConnected()&&!socket.isInputShutdown()&&!socket.isOutputShutdown();
        // if only one side is shut down, can we use the other side?
    }
    public Socket connect() {
        SocketAddress socketAddress=new InetSocketAddress(inetAddress,service);
        System.out.println(socketAddress);
        Socket socket=new Socket();
        try {
            socket.connect(socketAddress,200);
            System.out.println("returning: "+socket);
            return socket;
        } catch(IOException e) {
            System.out.println(this+" caught: "+e);
        }
        return null;
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        Home home=new Home();
        ServerSocket serverSocket=new ServerSocket(home.port(0));
        Group group=home.group();
        // belongs in some config or properties file.
        Properties properties=home.properties();
        Writer writer=new FileWriter(new File("home.properties"));
        properties.store(writer,null);
        writer.close();
        Server server=new Server(group,serverSocket);
        server.start();
    }
    private InetAddress inetAddress;
    private final Group group;
    private static String host;
    private static Integer service=30_000;
    private static InetAddress staticInetAddress;
    public static final Logger logger=Logger.getLogger(Home.class.getName());
}
