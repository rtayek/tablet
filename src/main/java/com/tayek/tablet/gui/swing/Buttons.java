package com.tayek.tablet.gui.swing;
import com.tayek.utilities.MainGui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class Buttons extends MainGui {
    Buttons(MyJApplet applet) {
        super(applet);
    }
    @Override public String title() {
        return "Buttons";
    }
    @Override public void addContent() {
        add(panel);
        panel.setPreferredSize(new Dimension(800,600));
    }
    void printColors() {
        for(float hue:hues) {
            Color on=Color.getHSBColor(hue,1,1);
            Color off=Color.getHSBColor(hue,.8f,.4f);
            System.out.println(on+" "+off);
        }
    }
    public static void main(String[] args) {
        Buttons buttons=new Buttons(null);
        buttons.printColors();
    }
    private static final long serialVersionUID=1L;
    final int buttons=7;
    float intensity=.8f,saturation=.4f;
    Float[] hues=new Float[buttons];
    {
        for(int i=0;i<hues.length;i++)
            hues[i]=(float)(i*1./buttons);
    }
    Boolean[] states=new Boolean[buttons];
    {
        for(int i=0;i<states.length;i++)
            states[i]=new Boolean(false);
    }
    ActionListener l=new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() instanceof JButton) {
                JButton b=(JButton)e.getSource();
                String name=b.getName();
                Integer i=Integer.valueOf(name);
                states[i]=!states[i];
                Color color=states[i]?Color.getHSBColor(hues[i],1,1):Color.getHSBColor(hues[i],intensity,saturation);
                System.out.println("set color of "+i+" to "+color);
                b.setBackground(color);
                System.out.println(b.getName());
            }
        }
    };
    JPanel panel=new JPanel();
    {
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        Dimension d=new Dimension(500,100);
        for(int i=0;i<buttons;i++) {
            JButton button=new JButton("Room "+i+" "+hues[i]);
            button.setName(""+i);
            button.setPreferredSize(d);
            button.setMaximumSize(d);
            button.addActionListener(l);
            Color c=Color.getHSBColor((float)(i*1./buttons),intensity,saturation);
            button.setBackground(c);
            panel.add(button);
        }
    }
}
