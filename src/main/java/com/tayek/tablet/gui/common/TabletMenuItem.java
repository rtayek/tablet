package com.tayek.tablet.gui.common;
import static com.tayek.utilities.Utility.printThreads;
import com.tayek.tablet.gui.swing.Gui;
import com.tayek.tablet.model.Message;
import com.tayek.tablet.Group.Tablet;
public enum TabletMenuItem {
    Reset,Ping,Disconnect,Connect,Log;
    public void doItem(Tablet tablet) {
        doItem(this,tablet);
    }
    public static boolean isIem(int ordinal) {
        return 0<=ordinal&&ordinal<values().length?true:false;
    }
    public static void doItem(int ordinal,Tablet tablet) { // this one is called from android!
        if(tablet!=null) if(0<=ordinal&&ordinal<values().length) values()[ordinal].doItem(tablet);
        else System.out.println(ordinal+" is invalid ordinal for!");
        else System.out.println("tablet is null in do item!");
    }
    public static void doItem(TabletMenuItem tabletMenuItem,Tablet tablet) {
        // maybe move 
        switch(tabletMenuItem) {
            case Reset:
                tablet.model().reset();
                break;
            case Ping:
                if(tablet.client()!=null) {
                    // maybe i have to close down my connection?
                    // and then reconnect?
                    Message message=new Message(tablet.group().groupId,tablet.client().tabletId(),Message.Type.start,0);
                    tablet.client().send(message);
                }
                break;
            case Disconnect:
                if(tablet.client()!=null) {
                    tablet.client().stop();
                    try {
                        Thread.sleep(100);
                    } catch(InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    printThreads();
                }
                break;
            case Connect:
                if(tablet.client()!=null) {
                    try {
                        tablet.client().start();
                    } catch(Exception e2) {
                        e2.printStackTrace();
                    }
                }
                break;
            case Log:
                //gui.textView.setVisible(!gui.textView.isVisible());
                break;
        }
    }
}
