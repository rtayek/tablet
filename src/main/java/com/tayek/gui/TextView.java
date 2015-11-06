package com.tayek.gui;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.PrintStream;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
@SuppressWarnings("serial") public class TextView extends JPanel {
    public TextView(String prefix) {
        taOutputStream=new TextAreaOutputStream(textArea,prefix);
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }
    public static TextView createAndShowGui(String prefix) {
        TextView textView=new TextView(prefix);
        JFrame frame=new JFrame("SysOut");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(textView);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        textView.frame=frame;
        return textView;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                TextView textView=createAndShowGui("foo");
                System.setOut(new PrintStream(textView.taOutputStream));
            }
        });
    }
    public JFrame frame;
    JTextArea textArea=new JTextArea(30,60);
    {
        String[] x=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        System.out.println(""+Arrays.asList(x));
        textArea.setFont(new Font("Lucida Console",Font.PLAIN,16));
    }
    public TextAreaOutputStream taOutputStream;
}