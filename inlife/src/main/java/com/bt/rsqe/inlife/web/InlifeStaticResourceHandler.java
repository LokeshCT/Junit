package com.bt.rsqe.inlife.web;

import com.bt.rsqe.web.staticresources.StaticResourceHandler;
import com.bt.rsqe.web.staticresources.StaticResourceLoader;

import javax.ws.rs.Path;

@Path("/rsqe/inlife/static")
public class InlifeStaticResourceHandler extends StaticResourceHandler {
    public InlifeStaticResourceHandler(StaticResourceLoader resourceLoader) {
        super(resourceLoader);
    }
}
