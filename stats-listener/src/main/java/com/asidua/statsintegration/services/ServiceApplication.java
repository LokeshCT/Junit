package com.asidua.statsintegration.services;

import com.asidua.statsintegration.services.rest.RestSimpleSubmissionService;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class ServiceApplication extends Application {
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);


    public ServiceApplication(){
        super();
        logger.initialise();
    }

    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(RestSimpleSubmissionService.class);
        return s;
    }
    interface Logger {
        @Log(level = LogLevel.DEBUG, format = "Service Application Initialising")
        void initialise();
    }

}