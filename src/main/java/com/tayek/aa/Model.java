package com.tayek.aa;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import com.tayek.aa.Factory.Receiver;
import com.tayek.audio.Audio.Sound;
import com.tayek.utilities.*;
public class Model extends Observable implements Receiver<Message>,Cloneable {
    public Model() {
        this(Factory.defaultButtons);
    }
    Model(Integer buttons) {
        this.buttons=buttons;
        states=new Boolean[buttons];
        reset();
    }
    public void reset() {
        synchronized(states) {
            for(Integer i=1;i<=buttons;i++)
                setState(i,false);
        }
    }
    public void setChangedAndNotify(Object object) {
        setChanged();
        notifyObservers(object);
    }
    public int fromBoolean(boolean state) {
        return !state?0:1;
    }
    public void setState(Integer buttonId,Boolean state) {
        synchronized(states) {
            states[buttonId-1]=state;
            setChangedAndNotify(buttonId);
        }
    }
    public static class Hint extends Triple<Integer,Integer,Boolean> {
        public Hint(Message message) {
            super(message.tabletId,message.button,message.state);
        }
    }
    @Override public void receive(Message message) {
        logger.fine("received message: "+message);
        switch(message.type) {
            case normal:
                if(message.state.equals(true)) {
                    synchronized(idToLastOnFrom) {
                        idToLastOnFrom.put(message.button,message.tabletId);
                    }
                    if(!state(message.button).equals(message.state)) {
                        // hint from set state is/was button id.
                        // new hint should be state changed (boolean,who)
                        // maybe send him a triple(button,state,who)?
                        Hint hint=new Hint(message);
                        int n=random.nextInt(Sound.values().length);
                        setChangedAndNotify(Sound.values()[n]);
                    } else System.out.println("no change in model");
                }
                setState(message.button,message.state);
                break;
            case start:
                InetAddress inetAddress=Utility.inetAddress(message.button);
                logger.fine("message had ip address: "+inetAddress+" "+message);
                break;
            case hello:
                inetAddress=Utility.inetAddress(message.button);
                logger.fine("message had ip address: "+inetAddress+" "+message);
                break;
            case goodbye:
                break;
            default:
                throw new RuntimeException("message type: "+message.type+" was not handled!");
        }
    }
    public Boolean state(Integer id) {
        synchronized(states) {
            return states[id-1];
        }
    }
    public Boolean[] states() {
        Boolean[] copy=new Boolean[buttons];
        synchronized(states) {
            System.arraycopy(states,0,copy,0,buttons);
            return copy;
        }
    }
    @Override public String toString() {
        String s="{";
        synchronized(states) {
            for(boolean state:states)
                s+=state?'T':"F";
            s+='}';
            return s;
        }
    }
    /* String f="\u22A5"; */
    public boolean areAllButtonsInTheSameState(Model model) {
        return areAllButtonsInTheSameState(this,model);
    }
    public static synchronized boolean areAllButtonsInTheSameState(Model model,Model model2) {
        boolean areEqual=true;
        final Boolean[] states=model.states(),states2=model2.states();
        for(Integer i=0;i<model.buttons;i++)
            if(!states[i].equals(states2[i])) {
                areEqual=false;
                break;
            }
        return areEqual;
    }
    public Object clone() {
        Model clone=new Model(buttons);
        return clone;
    }
    public final Integer serialNumber=++ids;
    public final Integer buttons;
    private final Boolean[] states;
    public final Map<Integer,Integer> idToLastOnFrom=new TreeMap<>();
    final Random random=new Random();
    static int ids=0;
    public final Logger logger=Logger.getLogger(getClass().getName());
}
