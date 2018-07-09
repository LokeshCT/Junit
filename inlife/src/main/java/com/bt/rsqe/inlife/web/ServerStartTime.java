package com.bt.rsqe.inlife.web;

import java.util.Date;

import static com.bt.rsqe.utils.AssertObject.*;

//Singleton
public class ServerStartTime {

    private static Date timeStamp;

    public static synchronized void set() {
        if( isNull(timeStamp) ) {
            timeStamp = new Date();
            return;
        }
        throw new IllegalStateException(String.format("Server start time already set.!"));
    }

    public static Date get() {
        return timeStamp;
    }

    public static void destroy_testOnly() {
        timeStamp = null;
    }
}
