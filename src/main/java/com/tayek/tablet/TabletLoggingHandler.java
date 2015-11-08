package com.tayek.tablet;
import java.util.*;
import java.util.logging.Logger;
import com.tayek.aa.Factory;
import com.tayek.aa.Message;
import com.tayek.aa.Factory.Receiver;
import com.tayek.aa.Message.Sender;
import com.tayek.tablet.model.Model;
import com.tayek.utilities.*;
public class TabletLoggingHandler extends LoggingHandler {
    static void init() {
        Set<Class<?>> set=new LinkedHashSet<>();
        set.add(Message.class);
        set.add(Sender.class);
        set.add(Receiver.class);
        set.add(Model.class);
        set.add(MyTimer.class);
        set.add(Home.class);
        set.add(Server.class);
        LoggingHandler.makeMapAndSetLevels(set);
        System.out.println("logging was initialized.");
    }
}
