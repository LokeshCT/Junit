package com.bt.usermanagement.web;

import com.bt.rsqe.web.staticresources.StaticResourceHandler;

import javax.ws.rs.Path;

@Path("/user-management/static")
public class UserManagementStaticResourceHandler extends StaticResourceHandler {

    public UserManagementStaticResourceHandler(UserManagementClasspathStaticResourceLoader resourceLoader) {
        super(resourceLoader);
    }

}
