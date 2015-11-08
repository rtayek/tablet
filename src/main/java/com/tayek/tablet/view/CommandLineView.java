package com.tayek.tablet.view;
import java.util.*;
import com.tayek.tablet.Group;
import com.tayek.tablet.model.Model;
public class CommandLineView implements Observer {
    public CommandLineView(Model model) {
        this.model=model;
    }
    @Override public void update(Observable observable,Object hint) {
        if(observable instanceof Model) if(observable==model) {
            System.out.println(this+" "+id+" received update: "+observable+" "+hint);
            if(hint instanceof Group) {
                System.out.print("in update: ");
                ((Group)hint).print();
            }
        } else System.out.println(this+" "+id+" not our model!");
        else System.out.println(this+" "+id+" not a model!");
    }
    private final int id=++n;
    private final Model model;
    private static int n=0;
}
