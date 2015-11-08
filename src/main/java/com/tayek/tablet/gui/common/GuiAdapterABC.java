package com.tayek.tablet.gui.common;
import java.util.Observable;
import java.util.Observer;
import com.tayek.tablet.model.Model;
public abstract class GuiAdapterABC implements GuiAdapter,Observer {
    public GuiAdapterABC(Model model) {
        this.model=model;
    }
    @Override public void update(Observable o,Object hint) {
        for(int buttonId=1;buttonId<=model.buttons;buttonId++) {
            setState(buttonId,model.state(buttonId));
            if(model.state(buttonId).equals(true)) {
                Integer lastOnFrom=model.idToLastOnFrom.get(buttonId);
                if(lastOnFrom==null) {
                    setText(buttonId,pad("Room "+buttonId));
                } else setText(buttonId,pad("from: "+model.idToLastOnFrom.get(buttonId)));
            } else setText(buttonId,pad("Room "+buttonId));
        }
    }
    static int length=20;
    public static String pad(String string) {
        for(;string.length()<length;string+=' ')
            ;
        return string;
    }
    final Model model;
}
