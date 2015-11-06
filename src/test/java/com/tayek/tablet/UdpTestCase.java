package com.tayek.tablet;
import static com.tayek.utilities.Utility.*;
import com.tayek.utilities.Utility;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.utilities.*;
public class UdpTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @Before public void setUp() throws Exception {
        System.out.println("<<<");
        threads=Thread.activeCount();
        Group group=Group.create(1,1,1);
        udp=Udp.create(group,1,null);
        printSocket(udp.socket,"Udp()");

    }
    @After public void tearDown() throws Exception {
        System.out.println(">>>");
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        if(tooToLongs>0) System.out.println(tooToLongs+" timeouts!");
    }
    @Test public void test1() throws SocketException,UnknownHostException,InterruptedException {
        udp.stop();
        assertEquals(threads,Thread.activeCount());
    }
    @Test public void test2() throws SocketException,UnknownHostException,InterruptedException {
        udp.startTest();
        udp.stop();
        assertEquals(threads,Thread.activeCount());
    }
    @Test public void test3() throws InterruptedException,IOException {
        udp.startTest();
        udp.send(expected);
        udp.stop();
        assertEquals(threads,Thread.activeCount());
    }
    public @Test void xtest42() throws IOException,InterruptedException {
        System.out.println(udp.socket);
        udp.startTest();
        udp.send(expected);
        Et et=new Et();
        boolean failed=false;
        udp.waitForMessage(300);
        if(failed) tooToLongs++;
        else System.out.println("took: "+et);
        udp.stop();
        if(failed) {
            System.out.println("took to long!");
            fail("took to long!");
        } else synchronized(udp.testMessage) {
            assertEquals(expected,udp.testMessage);
        }
        assertEquals(threads,Thread.activeCount());
    }
    @Test public void test4() throws InterruptedException,IOException {
        udp.startTest();
        udp.send(expected);
        Et et=new Et();
        udp.waitForMessage(300);
        failed=true;
        if(udp.testMessage.equals(udp.expected)) {
            failed=false;
            System.out.println("took: "+et);
        } else if(udp.testMessage.equals(udp.initialValue)) tooToLongs++;
        else; // we got a bogus value!
        udp.stop();
        assertEquals(threads,Thread.activeCount());
        if(failed) {
            System.out.println("took to long!");
            fail("took to long!");
        } else synchronized(udp.testMessage) {
            assertEquals(expected,udp.testMessage);
        }
    }
    @Test public void test5() throws InterruptedException,IOException {
        udp.logger.setLevel(Level.ALL);
        udp.threads=Thread.activeCount();
        udp.startTest();
        udp.send(udp.expected);
        udp.logger.info(udp+" sent");
        Et et=new Et();
        if(true) Thread.sleep(100);
        else while(true) {
            synchronized(udp.testMessage) {
                if(!udp.testMessage.equals(udp.initialValue)) {
                    System.out.println("value changed!");
                    break;
                }
            }
            if(et.etms()<100) Thread.sleep(10);
            else {
                System.out.println(Utility.toString(udp.thread));
                System.out.println("timeout!");
                break;
            }
        }
        udp.logger.fine(udp+" after wait");
        double time=et.etms();
        boolean failed=true;
        if(udp.testMessage.equals(udp.initialValue)) udp.logger.warning(udp+" we did not receive a message!");
        else if(udp.testMessage.equals(udp.expected)) {
            failed=false;
            System.out.println("it worked! &&&&&&&&&&&&&&&");
        } else udp.logger.warning(udp+" failed, received: "+udp.testMessage);
        udp.elapsedTimeHistogram.add(failed?Double.NaN:time);
        udp.failureRateHistogram.add(failed?1:0);
        udp.stop();
        if(udp.threads!=Thread.activeCount()) {
            udp.logger.warning("extra threads");
            printThreads();
        }
        // if(!failed) Thread.sleep(2000);
    }
    final String expected="bar";
    int threads;
    Udp udp;
    boolean failed;
    static int tooToLongs;
}
