package com.tayek.tablet;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import com.tayek.utilities.Range;

public enum Parameters { // properties
    buttons(5),tablets(7);
    Parameters(Object defaultValue) {
        this.defaultValue=currentValue=defaultValue;
    }
    public Object currentValue() {
        return currentValue;
    }
    public void setCurrentValue(Object currentValue) {
        this.currentValue=currentValue;
    }
    @Override public String toString() {
        return name()+"="+currentValue+"("+defaultValue+")";
    }
    public static void loadPropertiesFile(Properties properties,String filename) {
        URL url=Parameters.class.getResource(filename);
        if(url!=null) try {
            InputStream in=url.openStream();
            if(in!=null) {
                logger.config("before load: properties were: "+properties);
                properties.load(in);
                logger.config("properties loaded from url: "+url);
                logger.config("properties loaded were: "+properties);
            } else logger.warning("properties stream is null!");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        else logger.warning("url is null for filename: "+filename);
    }
    public void loadPropertiesFile(Properties properties) {
        Parameters.loadPropertiesFile(properties,Parameters.propertiesFilename);
    }
    public static void writePropertiesFile(Properties properties,String filename) {
        try {
            File file=new File(new File("resources/tayek/com/tablet"),filename);
            properties.store(new FileOutputStream(file),"initial");
        } catch(FileNotFoundException e) {
            logger.warning("properties"+" "+"caught: "+e+" property file was not written!");
        } catch(IOException e) {
            logger.warning("properties"+" "+"caught: "+e+" property file was not written!");
        }
    }
    public final Object defaultValue;
    private Object currentValue;
    public static final String propertiesFilename="home.properties";
    public static final Properties defaultProperties=new Properties();
    static {
        for(Parameters property:Parameters.values())
            defaultProperties.put(property.name(),property.defaultValue.toString());
        if(false) writePropertiesFile(defaultProperties,propertiesFilename);
        // only once to get the file created in the right place
    };
    public static final Range<Integer> buttons_=new Range<>(2,10);
    public static final Range<Integer> tablets_=new Range<>(2,20);
    public static final List<Integer> sizes=Collections.unmodifiableList(Arrays.asList(new Integer[] {5,7,9,11,13,15,17,19,21,23,25}));
    public static final Logger logger=Logger.getLogger(Parameters.class.getName());
}
