package com.tayek.utilities;
import java.lang.Thread.State;
import java.util.logging.*;
public class MyTimer implements Runnable {
    public MyTimer(Runnable runnable,int maxTime) {
        this.runnable=runnable;
        this.maxTime=maxTime;
    }
    @Override public void run() {
        et=new Et();
        runnable.run();
        etms=et.etms();
    }
    public void check() throws InterruptedException {
        logger.fine("check timer thread: "+Utility.toString(thread));
        State state=thread.getState();
        logger.fine("thread is: "+state);
        switch(state) {
            // case NEW:
            // break;
            case RUNNABLE:
                logger.fine("joining.");
                thread.join();
                logger.fine("joined.");
                break;
            case BLOCKED:
                logger.fine("joining.");
                thread.join();
                logger.fine("joined.");
                break;
            case TERMINATED:
                logger.fine("joining.");
                thread.join();
                logger.fine("joined.");
                break;
            case TIMED_WAITING:
                logger.fine("joining.");
                thread.interrupt();
                thread.join();
                logger.fine("joined.");
                break;
            case WAITING:
                thread.interrupt();
                thread.interrupt();
                thread.join();
                logger.fine("joined.");
                break;
            default:
                throw new RuntimeException(state+" implement this!");
        }
    }
    public double time() throws InterruptedException {
        thread=new Thread(this,"timer");
        thread.start();
        thread.join(maxTime);
        double dt=(thread.isAlive()||etms>maxTime)?Double.NaN:etms;
        return dt;
    }
    public static double time(Runnable runnable,int maxTime) throws InterruptedException {
        return new MyTimer(runnable,maxTime).time();
    }
    static void test(final int sleep,int maxTime,Histogram histogram) throws InterruptedException {
        System.out.print("sleep: "+sleep+", max time: "+maxTime);
        Runnable runnable=new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(sleep);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        double time=time(runnable,maxTime);
        System.out.println(" took: "+time+" ms.");
        histogram.add(time);
    }
    public static void main(String[] args) throws InterruptedException {
        Histogram histogram=new Histogram(10,95,105);
        for(int i=0;i<200;i+=20)
            test(100,i,histogram);
        System.out.println(histogram);
        System.out.println(histogram.toStringFrequency());
    }
    Et et;
    Thread thread;
    final Runnable runnable;
    final int maxTime;
    double etms;
    public final Logger logger=Logger.getLogger(getClass().getName());
    {
       // logger.setLevel(Level.ALL);
    }
}
