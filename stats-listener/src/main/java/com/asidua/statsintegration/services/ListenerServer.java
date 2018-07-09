package com.asidua.statsintegration.services;

import com.asidua.statsintegration.services.rest.TestManager;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.Context;

import java.util.HashMap;
import java.util.Map;

public class ListenerServer {

    private Server server;
    private int serverPort;
    private int stopPort;
    private String stopSecret;


    public ListenerServer(String home, Integer runningPort) {

        serverPort = runningPort;
        TestManager.appointForProjectHome(home);
        server = new Server(serverPort);
        ServletContextHandler context = new ServletContextHandler(server, "/", Context.NO_SESSIONS | Context.NO_SECURITY);
        context.setContextPath("/");

        Map<String, Object> initMap = new HashMap<String, Object>();
        initMap.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        initMap.put("com.sun.jersey.config.property.packages", "com.asidua.statsintegration.services.rest;org.codehaus.jackson.jaxrs");
        initMap.put("javax.ws.rs.Application", "com.asidua.statsintegration.services.ServiceApplication");
        context.addServlet(new ServletHolder(new ServletContainer(new PackagesResourceConfig(initMap))), "/*");
    }

    public void start() throws Exception {
        System.out.println("Starting Stats Listener on port " + serverPort);
        server.start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
        server.join();
        System.out.println("Stopped Stats listener on port " + serverPort);
    }

    public boolean isStarted() {
        return server.isStarted();
    }

    public boolean isStopped() {
        return server.isStopped();
    }
}