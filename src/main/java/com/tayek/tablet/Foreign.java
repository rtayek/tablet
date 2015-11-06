package com.tayek.tablet;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import com.tayek.utilities.LoggingHandler;

public class Foreign {
    public static void main(String[] args) throws IOException {
        God.log.init();
        LoggingHandler.setLevel(Level.OFF);
        ServerSocket serverSocket=new ServerSocket(Home.port(-1));
        Group group=Group.create(2,1);
        Server server=new Server(group,serverSocket);
        server.start();

    }
}
