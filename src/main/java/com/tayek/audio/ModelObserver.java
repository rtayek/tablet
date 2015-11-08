package com.tayek.audio;
import java.util.*;
import com.tayek.audio.Audio.Sound;
import com.tayek.tablet.model.Model;
public class ModelObserver implements Observer {
    @Override public void update(Observable model,Object hint) {
        if(model instanceof Model) {
            if(hint instanceof Sound) {
                if(System.getProperty("os.name").contains("indows")) {
                    Audio.play((Sound)hint);
                } else System.out.println("should play sound: "+hint);
            }
        } else System.out.println("not our model!!");
    }
    public static final ModelObserver instance=new ModelObserver();
}
