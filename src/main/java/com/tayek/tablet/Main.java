package com.tayek.tablet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import com.tayek.utilities.Dispatcher;
import com.tayek.tablet.gui.swing.Gui;
import com.tayek.aa.Factory;
import com.tayek.tablet.Group.*;

import static com.tayek.utilities.Utility.*;
public class Main {
    public static void main(String[] arguments)
            throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,NoSuchMethodException,SecurityException,IOException {
        Dispatcher dispatcher=new Dispatcher(arguments) {
            {
                while(entryPoints.size()>0)
                    remove(1);
                add(Server.class);
                add(Tablet.class);
                add(Gui.class);
                add(Udp.class);
                add(UdpFactory.class);
                add(Factory.class);
            }
        };
        dispatcher.run();
        printThreads();
    }
}
