package com.tayek.tablet;
import java.net.InetAddress;
public enum God {
    log {
        public void init() {
            TabletLoggingHandler.init();
        }
    },
    home {
        public void init() {
            Home.init();
        }
    },
    next {
        public void init() {
            System.out.println("also next");
        }
    };
    public void init() { throw new RuntimeException("override this!");}
}
