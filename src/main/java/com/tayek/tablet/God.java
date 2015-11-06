package com.tayek.tablet;
import java.net.InetAddress;
public enum God {
    log {
        @Override public void init() {
            TabletLoggingHandler.init();
        }
    },
    home {
        @Override public void init() {
            Home.init();
        }
    },
    next {
        @Override public void init() {
            System.out.println("also next");
        }
    };
    public void init() {
        throw new RuntimeException("override this!");
    }
}
