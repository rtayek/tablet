package com.tayek.tablet;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Message;
import com.tayek.utilities.LoggingHandler;
public class HomeTestCase {
    // maybe home and group are the same class????
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        God.home.init(); // maybe have to change for testing?
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() throws IOException,InterruptedException {
        // use get socket?
        ServerSocket serverSocket=new ServerSocket(Home.port(0));
        Group homeGroup=Group.create(1,1);
        Home home=new Home();
        Server server=new Server(homeGroup,serverSocket);
        server.start();
        Set<TcpClient> clients=new LinkedHashSet<>();
        // make sure these are the same
        // but are different instances
        for(int i:homeGroup.info.keySet()) {
            System.out.println("working with tablet: "+i);
            if(i==0) continue; // no client for home
            Group group=homeGroup.newGroup();
            TcpClient client=new TcpClient(group,i,group.newModel());
            clients.add(client);
            client.start();
        }
        Thread.sleep(100);
        // we need home to reply with a hello
        for(TcpClient client:clients) {
            client.send(new Message(client.group().groupId,client.tabletId,Message.Type.start,0));
        }
        Thread.sleep(100);
        for(TcpClient client:clients)
            System.out.println("client: "+client);
        for(TcpClient client:clients) {
            System.out.println(client+" "+client.tabletId()+" is on "+client.socket().toString()+" (from client socket)");
            System.out.println(client+" info has: "+client.group().inetAddress(client.tabletId()));
            System.out.println("home info has: "+client+" "+homeGroup.inetAddress(client.tabletId()));
        }
        Thread.sleep(500);
        int failures=0;
        boolean ok=true;
        for(int i:homeGroup.info.keySet())
            if(i!=0) {
                if(homeGroup.info.get(i).inetAddress==null) {
                    System.out.println("home does not know about tablet: "+i);
                    ok=false;
                }
            }
        if(!ok) failures++;
        System.out.println(failures);
        for(TcpClient client:clients) {
            Group group=client.group();
            group.print();
            ok=true;
            for(int i:group.info.keySet())
                if(group.info.get(i).inetAddress==null) {
                    System.out.println("tablet: "+client.tabletId+" does not know about tablet: "+i);
                    ok=false;
                }
            if(!ok) failures++;
        }
        System.out.println(failures+" failures.");
        assertTrue(failures==0);
        // now shut it all down!
    }
    Server home;
}
