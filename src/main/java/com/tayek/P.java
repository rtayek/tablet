package com.tayek;

import java.util.Properties;

public class P {
    public static void main(String[] args) {
       Properties p=System.getProperties();
       for(Object x:p.keySet())
           System.out.println(x+":"+p.getProperty((String)x));
    }
}
