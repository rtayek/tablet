package com.tayek.tablet.gui.android;
/*
import android.app.Activity;
import android.text.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.tablet.utility.*;
import com.tayek.tablet.model.*;

import java.util.*;
import java.util.logging.Logger;
class Build2 implements View.OnClickListener, Observer {
    Build2(final Activity activity) {
        this.activity=activity;
        linearLayout=new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        int buttons=5;
        final Tablet tablet=new Tablet(42,5);
        final Tablet tablet2=new Tablet(99,5);
        gui=new AndroidGui(tablet,new Toaster() {
            @Override
            public void toast(String string) {
                Toast.makeText(activity,string,Toast.LENGTH_LONG).show();
            }
        });
        final GuiAdapterABC adapterFor1=new GuiAdapterABC(tablet.model) {
            @Override
            public void setText(final int id,final String string) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((CheckBox)gui.idToButton.get(id)).setText(string);
                            }
                        });
                    }
                },0);
            }
            @Override
            public void setState(final int id,final boolean state) {
                // maybe we can get this from the model and do it on updaye?
                // or is that the problem
                //
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((CheckBox)gui.idToButton.get(id)).setChecked(state);
                            }
                        });
                    }
                },0);
            }
        };
        gui.adapter=adapterFor1;
        logger.info("set gui adapter to: "+gui.adapter);
        gui2=new AndroidGui(tablet2,new Toaster() {
            @Override
            public void toast(String string) {
                System.out.println("toast: "+string);
            }
        });
        final GuiAdapterABC adapterFor2=new GuiAdapterABC(tablet2.model) {
            @Override
            public void setText(final int id,final String string) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((RadioButton)gui2.idToButton.get(id)).setText(string);
                            }
                        });
                    }
                },0);
            }
            @Override
            public void setState(final int id,final boolean state) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((RadioButton)gui2.idToButton.get(id)).setChecked(state);
                            }
                        });
                    }
                },0);
            }
        };
        gui2.adapter=adapterFor2;
        logger.info("set gui2 adapter to: "+gui2.adapter);
        Button button=new CheckBox(activity);
        logger.info((button instanceof CheckBox)+" "+(button instanceof RadioButton));
        button=new RadioButton(activity);
        logger.info((button instanceof CheckBox)+" "+(button instanceof RadioButton));
        tablet.model.addObserver(this);
        tablet2.model.addObserver(this);
        Et dt=new Et();
        gui.startClient();
        gui2.startClient();
        gui.sendMessages(200,100);
        gui2.sendMessages(200,100);
        logger.info("start client and send messages took: "+dt.etms()+" ms.");
        addCheckBoxesToLayout(buttons,linearLayout,gui);
        addRadioButtonsToLayout(buttons,linearLayout,gui2);
    }
    void addCheckBox(int id,ViewGroup viewGroup,AndroidGui gui) {
        Button button=new CheckBox(activity);
        button.setId(id);
        button.setText("Room "+id);
        button.setOnClickListener(this);
        viewGroup.addView(button);
        gui.idToButton.put(id,button);
    }
    void addRadioButton(int id,ViewGroup viewGroup,AndroidGui gui) {
        Button button=new RadioButton(activity);
        button.setId(id);
        button.setText("Room "+id); // name should come from mode!
        button.setOnClickListener(this);
        viewGroup.addView(button);
        gui.idToButton.put(id,button);
    }
    void addCheckBoxesToLayout(int buttons,ViewGroup viewGroup,AndroidGui gui) {
        LinearLayout linearLayout=new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        viewGroup.addView(linearLayout);
        TextView textView=new TextView(activity);
        textView.setText("Tablet "+gui.tablet.id());
        for(int i=1;i<=buttons;i++)
            addCheckBox(i,linearLayout,gui);
    }
    void addRadioButtonsToLayout(int buttons,ViewGroup viewGroup,AndroidGui gui) {
        LinearLayout linearLayout=new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        viewGroup.addView(linearLayout);
        TextView textView=new TextView(activity);
        textView.setText("Tablet "+gui.tablet.id());
        linearLayout.addView(textView);
        for(int i=1;i<=buttons;i++)
            addRadioButton(i,linearLayout,gui);
    }
    @Override
    public void onClick(final View v) {
        if(v instanceof Button) {
            if(v instanceof CheckBox) {
                logger.info("checkbox");
                gui.onClick(new Integer(v.getId()),((CheckBox)v).isChecked());
            }
            if(v instanceof RadioButton) {
                logger.info("radio button");
                gui2.onClick(new Integer(v.getId()),((RadioButton)v).isChecked());
            }
        } else
            logger.warning("not a button!");
    }
    @Override
    public void update(Observable o,Object hint) {
        if(o==gui.tablet.model) {
            if(gui.adapter!=null)
                gui.adapter.update(o,hint);
            else
                logger.info("adapter for gui is null! ");
        } else if(o==gui2.tablet.model) {
            if(gui2.adapter!=null)
                gui2.adapter.update(o,hint);
            else
                logger.info("adapter for gui2 is null! ");
        } else
            throw new RuntimeException("no gui for model: "+o);
    }
    final LinearLayout linearLayout;
    final Activity activity;
    final AndroidGui gui, gui2;
    final Logger logger=Logger.getLogger(getClass().getName());
}
*/
