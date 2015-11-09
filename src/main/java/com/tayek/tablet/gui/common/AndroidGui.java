package com.tayek.tablet.gui.common;
import static com.tayek.utilities.Utility.*;
import java.util.LinkedHashMap;
import java.util.Map;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.model.Message;
import com.tayek.tablet.model.Message.Type;
import static com.tayek.tablet.model.Message.Type.*;
import com.tayek.utilities.*;
// could be more than one instance
public class AndroidGui {
    public AndroidGui(Tablet tablet,Toaster toaster) {
        this.tablet=tablet;
        this.toaster=toaster;
        adapter=true?null:new GuiAdapterABC(tablet.model()) {
            @Override public void setText(int id,String string) {
                throw new RuntimeException("implement this !!!!!!!!!!!!!!!!!!!");
            }
            @Override public void setState(int id,boolean state) {
                throw new RuntimeException("implement this !!!!!!!!!!!!!!!!!!!");
            }
        };
    }
    // not the same as the one click in the android code
    // ??? - need to make this work the same way
    public void onClick(final int buttonId,final boolean state) {
        tablet.logger.info("on click in "+this+" for tablet"+tablet.client().tabletId());
        toaster.toast("on click in "+this);
        tablet.model().setState(buttonId,state); // maybe move up?
        Thread thread=new Thread(new Runnable() {
            @Override public void run() {
                if(tablet.client()!=null) {
                    Et dt=new Et();
                    Message message=new Message(tablet.group().groupId,tablet.client().tabletId(),Message.Type.normal,buttonId,state);
                    tablet.client().send(message);
                    tablet.logger.info("set state took "+dt);
                }
            }
        },"broadcast");
        thread.start();
        // join(thread);
    }
    public void start() {
        toaster.toast("start client in "+this);
        thread=new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(2_000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                Et dt=new Et();
                if(tablet.client()!=null) tablet.client().start();
                printThreads();
                tablet.logger.info("start client took: "+dt);
            }
        },"start client");
        thread.start();
        // join(thread);
    }
    // maybe we should always run this on a thread!
    // we need to for android!
    public void sendMessage(Message message) {
        thread=new Thread(new Runnable() {
            @Override public void run() {
                Et dt=new Et();
                int address=0;
                if(tablet.group().inetAddress(tablet.client().tabletId())!=null)
                    address=Utility.toInteger(tablet.group().inetAddress(tablet.client().tabletId()));
                Message message=new Message(tablet.group().groupId,tablet.client().tabletId(),
                       Type.startup,address);
                // wtf! why am i creating a start message instead of sending what the caller game me?
                tablet.client().send(message);
                tablet.logger.info("send messages took "+dt);
            }
        },"send message");
        thread.start();
        // join(thread);
    }
    final Toaster toaster;
    Thread thread;
    public GuiAdapterABC adapter;
    public final Tablet tablet;
    public final Map<Integer,Object> idToButton=new LinkedHashMap<>();
}
