package com.tayek.tablet.gui.swing;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
class Gui2 implements ActionListener {
	Gui2() {}
	void start() throws InvocationTargetException,InterruptedException {
	javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
		@Override public void run() {
		createAndShowGUI();
		}
	});
	}
	@Override public void actionPerformed(ActionEvent e) {
	System.out.println("action performed: "+e);
	}
	void createAndShowGUI() {
	frame=new JFrame("Tablet");
	frame.setUndecorated(false);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.addComponentListener(new ComponentAdapter() {
		@Override public void componentMoved(ComponentEvent ce) {
		Component c=ce.getComponent();
		System.out.println("frame moved to "+c.getLocation());
		}
	});
	ChangeListener changeListener=new ChangeListener() {
		@Override public void stateChanged(ChangeEvent e) {
		System.out.println(e);
		}
	};
	ActionListener actionListener=new ActionListener() {
		@Override public void actionPerformed(ActionEvent e) {
		System.out.println(e);
		}
	};
	build2(changeListener,actionListener);
	frame.pack();
	frame.setVisible(true);
	}
void build2(ChangeListener changeListener,ActionListener actionListener) {
	JPanel top=new JPanel();
	JLabel topLabel=new JLabel("top");
	Font current=topLabel.getFont();
	System.out.println(topLabel.getFont());
	JPanel middle=new JPanel();
	middle.setLayout(new BoxLayout(middle,BoxLayout.Y_AXIS));
	for(Integer i:defaultIdToColor.keySet()) {
		JToggleButton button=new JCheckBox("                 "+i);
		current=button.getFont();
		Font large=new Font(current.getName(),current.getStyle(),3*current.getSize()/2);
		button.setFont(large);
		button.setName(""+i);
		button.addChangeListener(changeListener);
		button.addActionListener(actionListener);
		middle.add(button);
		button.setBackground(defaultIdToColor.get(i));
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
	public static void main(String[] args) throws IOException {
	new Gui2().createAndShowGUI();
	}
	int n=5;
	JFrame frame;
	static final Map<Integer,Color> defaultIdToColor;
	static {
	Map<Integer,Color> temp=new LinkedHashMap<>();
	temp.put(1,Color.red);
	temp.put(2,Color.orange);
	temp.put(3,Color.yellow);
	temp.put(4,Color.green);
	temp.put(5,Color.blue);
	temp.put(6,Color.magenta);
	defaultIdToColor=Collections.unmodifiableMap(temp);
	}}
