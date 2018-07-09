package com.bt.nrm.web;

import com.bt.rsqe.web.staticresources.StaticResourceHandler;

import javax.ws.rs.Path;

@Path("/nrm/static")
public class NrmStaticResourceHandler extends StaticResourceHandler {

    public NrmStaticResourceHandler(NrmClasspathStaticResourceLoader resourceLoader) {
        super(resourceLoader);
    }

}
