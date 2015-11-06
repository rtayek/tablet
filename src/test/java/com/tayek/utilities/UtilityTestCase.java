package com.tayek.utilities;
import static org.junit.Assert.*;
import java.net.*;
import org.junit.*;
import static com.tayek.utilities.Utility.*;

public class UtilityTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testInetAddressPack() throws UnknownHostException {
        InetAddress inetAddress=InetAddress.getLocalHost();
        int expected=Utility.toInteger(inetAddress);
        byte[] bytes=unpack(expected);
        InetAddress inetAddress2=inetAddress(expected);
        int actual=pack(inetAddress2.getAddress());
        assertEquals(expected,actual);
        String x=inetAddress2.toString();
        System.out.println(x);
    }
}
