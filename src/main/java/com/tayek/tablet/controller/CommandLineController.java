package com.tayek.tablet.controller;
import java.io.*;
import java.util.logging.Level;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.Tablet;
import com.tayek.tablet.model.*;
import com.tayek.tablet.view.CommandLineView;
import com.tayek.utilities.LoggingHandler;
public class CommandLineController {
    CommandLineController(Home home,final int tabletId) {
        this.tabletId=tabletId;
        tablet=home.group().newGroup().new Tablet(home,tabletId);
    }
    private static void usage() {
        System.out.println("usage:");
        System.out.println("c - add a command line view");
        System.out.println("p - print view");
        System.out.println("q - quit");
        System.out.println("r - reset");
        System.out.println("s - start client");
        System.out.println("t - stop client");
        System.out.println("1 - send start message");
        System.out.println("2 - send hello message");
        System.out.println("3 - send on message");
        System.out.println("4 - send off message");
    }
    private String[] splitNext(String command,int i) {
        while(command.charAt(i)==' ')
            i++;
        String[] tokens=command.substring(i).split(" ");
        return tokens;
    }
    boolean process(String command) {
        if(command.length()==0) return true;
        String[] tokens=null;
        switch(command.charAt(0)) {
            case 'h':
                usage();
                break;
            case 'b':
                if(command.charAt(1)==' ') {
                    tokens=splitNext(command,2);
                    if(tokens.length==2) try {
                        int buttonId=Integer.valueOf(tokens[0]);
                        boolean state=Boolean.valueOf(tokens[1]);
                        tablet.model().setState(buttonId,state);
                    } catch(Exception e) {
                        System.out.println("caught: "+e);
                        System.out.println("syntax error: "+command);
                    }
                    else System.out.println("too many tokens!");
                } else System.out.println("syntax error: "+command);
                break;
            case 'o': // send start form foreign group
                    Message message=new Message(99,tabletId,Message.Type.startup,0);
                    tablet.client().send(message);
                break;
            case 'c':
                tablet.model().addObserver(new CommandLineView(tablet.model()));
                break;
            case 'g':
                // gui.Main.observe(null,model,null);
                break;
            case 'r':
                tablet.model().reset();
                break;
            case 's':
                tablet.client().start();
                break;
            case '1':
                    message=new Message(tablet.group().groupId,tabletId,Message.Type.startup,0);
                    tablet.client().send(message);
                    //tablet.model().setChangedAndNotify(tablet.client().group());
                break;
            case '2':
                message=new Message(tablet.group().groupId,tabletId,Message.Type.hello,0);
                tablet.client().send(message);
                //tablet.model().setChangedAndNotify(tablet.client().group());
                break;
            case '3':
                    message=new Message(tablet.group().groupId,tabletId,Message.Type.normal,1,true);
                    tablet.client().send(message);
                break;
            case '4':
                    message=new Message(tablet.group().groupId,tabletId,Message.Type.normal,1,false);
                    tablet.client().send(message);
                break;
            case 't':
                tablet.client().stop();
                break;
            case 'q':
                return false;
            default:
                System.out.println("got a: "+command.charAt(0));
                usage();
                break;
        }
        return true;
    }
    void run() {
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(System.in));
        String string=null;
        usage();
        prompt();
        try {
            while((string=bufferedReader.readLine())!=null) {
                if(!process(string)) {
                    System.out.println("quitting.");
                    return;
                }
                prompt();
            }
        } catch(IOException e) {
            System.out.println("caught: "+e);
            System.out.println("quitting.");
            return;
        }
        System.out.println("end of file.");
    }
    static void prompt() {
        System.out.print(lineSeparator+">");
        System.out.flush();
    }
    public static void main(String[] argu1entss) {
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        Home home=new Home();
        Integer tabletId=0;
        if(argu1entss.length==0) tabletId=1;
        else tabletId=Integer.valueOf(argu1entss[0]);
        if(home.getInetAddress()==null) System.out.println("can not get ip address for host!");
        else {
            CommandLineController commandLineController=new CommandLineController(home,tabletId);
            commandLineController.run();
        }
    }
    final Integer tabletId;
    final Tablet tablet;
    public static final String lineSeparator=System.getProperty("line.separator");
}
