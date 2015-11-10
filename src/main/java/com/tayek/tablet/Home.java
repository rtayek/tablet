package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import com.tayek.tablet.model.Model;
import com.tayek.utilities.*;
public class Home {
    //https://medium.com/android-news/8-ways-to-do-asynchronous-processing-in-android-and-counting-f634dc6fae4e
    public Home() {
        this(loadProperties(new SortedProperties(),"home.properties"));
    }
    public Home(Properties properties) {
        host=properties.getProperty("host");
        try {
            inetAddress=InetAddress.getByName(host);
        } catch(UnknownHostException e) {
            System.out.println("caught: "+e);
        }
        service=Integer.valueOf(properties.getProperty("service"));
        Set<Integer> tabletIds=new TreeSet<>();
        String groupPrefix="group",tabletPrefix="tablet";
        Integer groupId=0;
        for(Object object:properties.keySet()) {
            Object value=properties.getProperty(object.toString());
            String key=object.toString();
            System.out.println(key+" "+value);
            if(key.equals(groupPrefix)) {
                Integer id=Integer.valueOf(value.toString());
                groupId=id;
            }
            if(key.startsWith(tabletPrefix)) {
                Integer tabletId=Integer.valueOf(key.substring(tabletPrefix.length()));
                tabletIds.add(tabletId);
            }
        }
        System.out.println("tablet id's for home group are: "+tabletIds);
        group=new Group(groupId,tabletIds);
        System.out.println("home group is: "+group);
    }
    // every client has a group (all the same)
    // every client has info about tablet 0 (the current home)
    // home on the laptop needs to have the same group
    // he will have info about all of the tablets
    // maybe a tablet will be able to be a home.
    // so the groups need to be the same, but separate instances.
    // so we have clone.
    // write a command line ui for this! (working on it)
    //
    // looks like we can assign a static ip to the fire's.
    // so we will "know" each tablet's inet address when starting.
    // so we can go back to the old way
    // where each tablet listens and had n connections?
    // or:
    // always listen, but hang up after each message;
    // and do a connect, send, close for each outgoing message.
    // investigate the idea of a new device joining the group (someone's phone or tablet)
    //
    // async - lots of requirements for stuff to be run on threads
    // so roll my own, use handler, use async or use futures?
    // http://stackoverflow.com/questions/18153644/android-asynctask-and-threading
    // https://medium.com/android-news/8-ways-to-do-asynchronous-processing-in-android-and-counting-f634dc6fae4e
    // http://stackoverflow.com/questions/6964011/handler-vs-asynctask-vs-thread/9800870#9800870
    // http://stackoverflow.com/questions/11485482/how-come-androids-asynctask-doesnt-implement-future
    // http://stackoverflow.com/questions/536327/is-it-a-good-way-to-use-java-util-concurrent-futuretask/1780411#1780411
    //
    // ask conrad about little tokyo thing.
    
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
    // looks like we need a swicth:
    // if we are a client, use host
    // if we are home, set host to localhost
    static void init() { // droid needs this run on a thread!
    }
    public InetAddress getInetAddress() {
        return inetAddress;
    }
    public boolean isConnectedx(Socket socket) {
        return socket!=null&&socket.isBound()&&!socket.isClosed()&&socket.isConnected()&&!socket.isInputShutdown()&&!socket.isOutputShutdown();
        // if only one side is shut down, can we use the other side?
    }
    private Socket connect(int timeout) {
        System.out.println("connecting to: "+inetAddress+"/"+service+" "+timeout);
        SocketAddress socketAddress=new InetSocketAddress(inetAddress,service);
        System.out.println(socketAddress);
        Socket socket=new Socket();
        try {
            socket.connect(socketAddress,timeout);
            System.out.println("returning: "+socket);
            return socket;
        } catch(IOException e) {
            System.out.println(this+" caught: "+e);
        }
        return null;
    }
    class Connect implements Runnable {
        Connect(int timeout) {
            this.timeout=timeout;
        }
        @Override public void run() {
            socket=connect(timeout);
        }
        final int timeout;
        Socket socket;
    }
    public Socket connectUsingThread(int timeout) {
        System.out.println("connect using thread");
        System.out.flush();
        Connect connect=new Connect(timeout);
        Thread thread=new Thread(connect,"connect");
        thread.start();
        while(thread.isAlive())
            ;
        System.out.println("returning: "+connect.socket);
        return connect.socket;
    }
    static boolean loadPropertiesFile(Properties properties,String filename) {
        URL url=Home.class.getResource(filename);
        if(url!=null) try {
            InputStream inputStream=url.openStream();
            if(inputStream!=null) {
                properties.load(inputStream);
                inputStream.close();
                return true;
            }
        } catch(IOException e) {
            System.out.println("caught: "+e);
        }
        return false;
    }
    static boolean storeProperties(Properties properties,String filename) {
        File dir=new File("./src/main/resources/");
        File dir2=new File(dir,"com/tayek/tablet/");
        File file=new File(dir2,filename);
        Writer writer=null;
        try {
            writer=new FileWriter(file);
            properties.store(writer,null);
            writer.close();
            return true;
        } catch(IOException e) {
            System.out.println("caught: "+e);
        }
        return false;
    }
    static Properties defaultProperties() {
        Properties properties;
        properties=new SortedProperties();
        properties.put("host","192.168.1.104");
        properties.put("service","20000");
        Group group=Group.create(1,1);
        Model model=group.newModel();
        properties.put("group",group.groupId.toString());
        for(Integer i:group.info.keySet()) // will be home's info now!
            properties.put("tablet"+i,group.info.get(i).toString());
        properties.put("buttons",group.newModel().buttons.toString());
        for(int i=1;i<=model.buttons;i++)
            properties.put("button"+i,model.state(i).toString());
        return properties;
    }
    static Properties loadProperties(Properties properties,String filename) {
        System.out.println("enter load properties.");
        if(!loadPropertiesFile(properties,filename)) {
            System.out.println("no properties file, using default properties.");
            properties=defaultProperties();
            if(false) { // we can not normally do this as we may be in a jar.
                if(!storeProperties(properties,filename)) System.out.println("failed to store properties!");
            }
        } else System.out.println("loaded properties.");
        return properties;
    }
    public static void main(String[] arguments) throws IOException {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        Server.run(new Home());
    }
    private String host;
    private InetAddress inetAddress;
    private Integer service;
    private final Group group;
    public static final Logger logger=Logger.getLogger(Home.class.getName());
}
