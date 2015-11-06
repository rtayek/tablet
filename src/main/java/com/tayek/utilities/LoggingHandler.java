package com.tayek.utilities;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;
import com.tayek.tablet.Message;
public class LoggingHandler extends Formatter {
    @Override public String format(LogRecord record) {
        String threadName=Thread.currentThread().getName();
        String className=record.getSourceClassName();
        int x=className.lastIndexOf(".");
        className=className.substring(x+1);
        long time=System.currentTimeMillis();
        long dt=(time-t0)%1_000_000;
        if(threadName.length()>maxThreadNameLength) threadName=threadName.substring(0,maxThreadNameLength-2)+threadName.substring(threadName.length()-1);
        return String.format(format,dt,record.getSequenceNumber(),record.getLevel(),record.getMessage(),threadName,
                className+"."+record.getSourceMethodName()+"()");
    }
    public static void foo() throws SecurityException,IOException {
        Handler fh=new FileHandler("%t/mq.log");
        fh.setLevel(Level.FINE);
        // Get Logger for "javax.jms.connection" domain.
        Logger logger=Logger.getLogger("javax.jms.connection");
        logger.addHandler(fh);
        logger.setLevel(Level.FINE);
    }
    public static void addMyHandlerAndSetLevel(Logger logger,Level level) {
        Handler handler=new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        logger.setUseParentHandlers(false);
        handler.setFormatter(new LoggingHandler());
        logger.addHandler(handler);
        logger.setLevel(level);
    }
    public static void test(Class<?> clazz,Logger logger) {
        for(Level level:levels) {
            logger.setLevel(level);
            logger.severe("set level to: "+level);
            for(Level level2:levels)
                logger.log(level2,level2+" "+clazz.getSimpleName());
        }
    }
    public static void test(Map<Class<?>,Logger> map) {
        for(Class<?> clazz:map.keySet()) {
            Logger logger=map.get(clazz);
            test(clazz,logger);
        }
    }
    public static Map<Class<?>,Logger> makeMapAndSetLevels(Set<Class<?>> classes) {
        LoggingHandler.addMyHandlerAndSetLevel(Logger.getGlobal(),Level.FINEST);
        Map<Class<?>,Logger> map=new LinkedHashMap<Class<?>,Logger>();
        for(Class<?> clazz:classes) {
            Logger logger=Logger.getLogger(clazz.getName());
            LoggingHandler.addMyHandlerAndSetLevel(logger,Level.FINEST);
            map.put(clazz,logger);
        }
        return map;
    }
    public static void setLevel(Level level) {
        for(Logger logger:Message.map.values())
            logger.setLevel(level);
    }
    public static long t0=0;
    static final Level levels[]= {Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST};
    static final Integer maxThreadNameLength=10;
    public static final String format="%06d %05d %7s %-45s in %"+maxThreadNameLength+"s %s\n";
}
