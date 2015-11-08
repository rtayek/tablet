package com.tayek.tablet.gui.swing;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.tayek.audio.ModelObserver;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.Tablet;
import com.tayek.tablet.gui.common.*;
import com.tayek.tablet.model.*;
import com.tayek.utilities.*;
import com.tayek.gui.*;
public class Gui implements Observer,ActionListener {
    public Gui(Tablet tablet,Map<Integer,Color> map) {
        this.tablet=tablet;
        this.model=tablet.model();
        this.tabletId=tablet.client().tabletId();
        this.idToColor=map;
        String prefix="tablet "+tabletId;
        if(true) {
            textView=addTextView(prefix);
        } else {
            Tee tee=Tee.tee(new File("out.txt"));
            textView=TextView.createAndShowGui(prefix);
            tee.addOutputStream(textView.taOutputStream);
        }
        if(textView!=null) textView.frame.setVisible(true);
    }
    public void start() throws InvocationTargetException,InterruptedException {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    @Override public void actionPerformed(ActionEvent e) {
        logger.info("action performed: "+e);
        TabletMenuItem x=TabletMenuItem.valueOf(e.getActionCommand());
        if(x!=null) {
            if(x.equals(TabletMenuItem.Log)) textView.setVisible(!textView.isVisible());
            else x.doItem(tablet);
        } else if(e.getActionCommand().equals("Open ...")) {
            logger.info("not implemented: "+e.getActionCommand());
        } else if(e.getActionCommand().equals("Save ...")) {
            logger.info("not implemented: "+e.getActionCommand());
        } else if(e.getActionCommand().equals("New Game")) {
            logger.info("not implemented: "+e.getActionCommand());
        } else if(e.getActionCommand().equals("About")) JOptionPane.showMessageDialog(null,"Tablet (alpha)");
        else {
            logger.info("action not handled: "+e.getActionCommand());
        }
    }
    public JMenuBar createMenuBar() {
        JMenuBar menuBar=new JMenuBar();
        JMenu menu=new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("File menu");
        menuBar.add(menu);
        JMenuItem menuItem=new JMenuItem("Open ...",KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Open file dialog");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menuItem=new JMenuItem("Save ...",KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save file dialog");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menu=new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.getAccessibleContext().setAccessibleDescription("Edit menu");
        menuBar.add(menu);
        menuItem=new JMenuItem("Configure",KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Configure");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menu.addSeparator();
        menuItem=new JMenuItem("Buttons",KeyEvent.VK_B);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Buttons");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menu=new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription("View menu");
        menuBar.add(menu);
        menu.addSeparator();
        menuItem=new JMenuItem("Colors",KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Colors");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menu=new JMenu("Options");
        menu.setMnemonic(KeyEvent.VK_O);
        menu.getAccessibleContext().setAccessibleDescription("Options menu");
        // Reset,Ping,Disconnect,Connect,Log;
        if(true) for(TabletMenuItem x:TabletMenuItem.values()) {
            menuItem=new JMenuItem(x.name());
            int vk=(KeyEvent.VK_A-1)+(x.name().toUpperCase().charAt(0)-'A');
            menuItem.setAccelerator(KeyStroke.getKeyStroke(vk,ActionEvent.ALT_MASK));
            menuItem.getAccessibleContext().setAccessibleDescription(x.name());
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }
        else {
            menuItem=new JMenuItem("Reset");
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.ALT_MASK));
            menuItem.getAccessibleContext().setAccessibleDescription("Reset");
            menuItem.addActionListener(this);
            menu.add(menuItem);
            menuItem=new JMenuItem("Ping"); // better make these enums rsn!
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,ActionEvent.ALT_MASK));
            menuItem.getAccessibleContext().setAccessibleDescription("Ping");
            menuItem.addActionListener(this);
            menu.add(menuItem);
            menuItem=new JMenuItem("Disconnect");
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,ActionEvent.ALT_MASK));
            menuItem.getAccessibleContext().setAccessibleDescription("Disconnect");
            menuItem.addActionListener(this);
            menu.add(menuItem);
            menuItem=new JMenuItem("Connect");
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.ALT_MASK));
            menuItem.getAccessibleContext().setAccessibleDescription("Cconnect");
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }
        if(false) {
            menuItem=new JMenuItem("Log",KeyEvent.VK_C);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,ActionEvent.ALT_MASK));
            menuItem.getAccessibleContext().setAccessibleDescription("Log");
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }
        menuBar.add(menu);
        menu=new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.getAccessibleContext().setAccessibleDescription("Help menu");
        menuItem=new JMenuItem("About",KeyEvent.VK_A);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("About");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menuBar.add(menu);
        return menuBar;
    }
    void build(ChangeListener changeListener,ActionListener actionListener) {
        JPanel middle=new JPanel();
        middle.setLayout(new BoxLayout(middle,BoxLayout.X_AXIS));
        middle.setPreferredSize(new Dimension(500,40));
        for(Integer id:idToColor.keySet()) {
            JToggleButton button=new JCheckBox(""+id);
            Font current=button.getFont();
            Font large=new Font(current.getName(),current.getStyle(),3*current.getSize()/2);
            button.setFont(large);
            button.setName(""+id);
            idToButton.put(id,button);
            button.setBackground(idToColor.get(id));
            // button.setPreferredSize(new Dimension(100,25));
            button.addChangeListener(changeListener);
            button.addActionListener(actionListener);
            middle.add(button);
        }
        JPanel screen=new JPanel();
        screen.setLayout(new BoxLayout(screen,BoxLayout.Y_AXIS));
        JPanel top=new JPanel();
        JLabel topLabel=new JLabel("top");
        Font current=topLabel.getFont();
        System.out.println(topLabel.getFont());
        Font small=new Font(current.getName(),current.getStyle(),2*current.getSize()/3);
        topLabel.setFont(small);
        top.add(topLabel);
        JPanel bottom=new JPanel();
        JLabel bottomLabel=new JLabel("bottom");
        bottomLabel.setFont(small);
        bottom.add(bottomLabel);
        screen.add(top);
        screen.add(middle);
        screen.add(bottom);
        frame.getContentPane().add(screen,BorderLayout.CENTER);
    }
    void build2(ChangeListener changeListener,ActionListener actionListener) {
        JPanel top=new JPanel();
        JLabel topLabel=new JLabel("top");
        Font current=topLabel.getFont();
        System.out.println(topLabel.getFont());
        JPanel middle=new JPanel();
        middle.setLayout(new BoxLayout(middle,BoxLayout.Y_AXIS));
        for(Integer id=1;id<=model.buttons;id++) {
            JToggleButton button=new JCheckBox(GuiAdapterABC.pad("Room "+id));
            current=button.getFont();
            Font large=new Font(current.getName(),current.getStyle(),3*current.getSize()/2);
            button.setFont(large);
            button.setName(""+id);
            idToButton.put(id,button);
            button.addChangeListener(changeListener);
            button.addActionListener(actionListener);
            middle.add(button);
            button.setBackground(idToColor.get(id));
            middle.add(new JLabel());
        }
        Font small=new Font(current.getName(),current.getStyle(),2*current.getSize()/3);
        topLabel.setFont(small);
        top.add(topLabel);
        JPanel bottom=new JPanel();
        JLabel bottomLabel=new JLabel("bottom");
        bottomLabel.setFont(small);
        bottom.add(bottomLabel);
        frame.getContentPane().add(top,BorderLayout.PAGE_START);
        frame.getContentPane().add(middle,BorderLayout.CENTER);
        frame.getContentPane().add(bottom,BorderLayout.PAGE_END);
    }
    void createAndShowGUI() {
        frame=new JFrame("Tablet "+tabletId);
        frame.setUndecorated(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent ce) {
                Component c=ce.getComponent();
                // logger.info("frame moved to "+c.getLocation());
            }
        });
        JMenuBar jMenuBar=createMenuBar();
        frame.setJMenuBar(jMenuBar);
        ChangeListener changeListener=new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                logger.finer("model "+tabletId+", button "+((JToggleButton)e.getSource()).getName()+" is "+((JToggleButton)e.getSource()).isSelected());
            }
        };
        ActionListener actionListener=new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                logger.fine("model "+tabletId+", button "+((JToggleButton)e.getSource()).getName()+" is "+((JToggleButton)e.getSource()).isSelected());
                int id=new Integer(((JToggleButton)e.getSource()).getName());
                boolean state=((JToggleButton)e.getSource()).isSelected();
                model.setState(id,state);
                if(tablet.client()!=null) {
                    Message message=new Message(tablet.group().groupId,tablet.client().tabletId(),Message.Type.normal,id,state);
                    tablet.client().send(message);
                } else System.out.println("no client!");
            }
        };
        build2(changeListener,actionListener);
        frame.pack();
        frame.setVisible(true);
    }
    @Override public void update(Observable o,Object hint) {
        logger.fine("model "+tabletId+", hint: "+hint);
        if(!(o instanceof Model&&o.equals(model))) throw new RuntimeException("oops");
        adapter.update(o,hint);
    }
    static int length=20;
    public static String pad2(String string) {
        for(;string.length()<length;string+=' ')
            ;
        return string;
    }
    static void startGui(Tablet tablet) {
        try {
            final Gui gui=new Gui(tablet,defaultIdToColor);
            GuiAdapterABC adapter=new GuiAdapterABC(gui.model) {
                @Override public void setText(int id,String string) {
                    gui.idToButton.get(id).setText(string);
                }
                @Override public void setState(int id,boolean state) {
                    gui.idToButton.get(id).setSelected(state);
                }
            };
            gui.adapter=adapter;
            System.out.println("tablet "+tablet.client().tabletId()+" model: "+tablet.model());
            System.out.println("start gui id is ="+tablet.model().serialNumber);
            tablet.model().addObserver(gui);
            tablet.model().addObserver(ModelObserver.instance);
            gui.start();
        } catch(InvocationTargetException|InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static TextView addTextView(String prefix) {
        Tee tee=new Tee(System.out);
        TextView textView=TextView.createAndShowGui(prefix);
        tee.addOutputStream(textView.taOutputStream);
        PrintStream printStream=new PrintStream(tee,true);
        System.setOut(printStream);
        System.setErr(printStream);
        System.out.println(""+" "+"tee'd");
        return textView;
    }
    // idea: make the gui classes inner to the mediator
    public static void start(Home home,Integer tabletId) {
        Group group=home.group().newGroup();
        Tablet tablet=group.new Tablet(tabletId);
        tablet.client().start();
        if(tablet.client().socket()!=null) {
            // see if we can set this at startup?
            InetAddress inetAddress=group.checkForInetAddress(tabletId,tablet.client().socket());
            int address=inetAddress!=null?Utility.toInteger(inetAddress):0;
            Message message=new Message(group.groupId,tabletId,Message.Type.start,address);
            tablet.client().send(message);
        } // get a clone
        startGui(tablet);
    }
    public static void start(Home home,String[] arguments) {
        for(String arg:arguments) {
            Integer tabletId=Utility.toInteger(arg);
            if(tabletId!=null) {
                start(home,tabletId);
            } else System.out.println(arg+" is not a valid tabletId");
        }
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        // set ip here
        Home home=new Home();
        // android tablets are 1 and 2 for now, so start uo a few other ones.
        if(arguments.length==0) arguments=new String[] {"4","5"};
        start(home,arguments);
    }
    final int tabletId;
    final Model model;
    public Tablet tablet;
    public final TextView textView;
    /*final*/ GuiAdapterABC adapter;
    final Map<Integer,Color> idToColor;
    final Map<Integer,JToggleButton> idToButton=new LinkedHashMap<>();
    public JFrame frame;
    final Logger logger=Logger.getLogger(getClass().getName());
    public static final Map<Integer,Color> defaultIdToColor;
    static {
        Map<Integer,Color> temp=new LinkedHashMap<>();
        temp.put(1,Color.red);
        temp.put(2,Color.orange);
        temp.put(3,Color.yellow);
        temp.put(4,Color.green);
        temp.put(5,Color.blue);
        temp.put(6,Color.magenta);
        temp.put(7,Color.cyan);
        defaultIdToColor=Collections.unmodifiableMap(temp);
    }
    static Integer tablets=0;
}
