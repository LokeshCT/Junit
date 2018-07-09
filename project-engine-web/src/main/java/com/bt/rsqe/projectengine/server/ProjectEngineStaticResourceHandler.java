package com.bt.rsqe.projectengine.server;

import com.bt.rsqe.web.staticresources.StaticResourceHandler;
import com.bt.rsqe.web.staticresources.StaticResourceLoader;

import javax.ws.rs.Path;

@Path("/rsqe/project-engine/static")
public class ProjectEngineStaticResourceHandler extends StaticResourceHandler {
    public ProjectEngineStaticResourceHandler(StaticResourceLoader resourceLoader) {
        super(resourceLoader);
    }
}
