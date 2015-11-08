package com.tayek.tablet;

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.*;
import com.tayek.audio.*;
import com.tayek.tablet.model.*;

public class ModelTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testModelObserver() throws IOException {
        System.out.println("model");
        model.addObserver(ModelObserver.instance);
        Message message=new Message(1,2,Message.Type.normal,3,true);
        model.receive(message);

        //model.setChangedAndNotify(Sound.atari);
    }
    @Test public void testSound() throws InterruptedException {
        System.out.println("main");
        Audio.main(new String[0]);
    }
    Group group=Group.create(1,1);
    Model model=group.newModel();
}
