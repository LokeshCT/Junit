package com.bt.usermanagement.web;

import com.bt.rsqe.web.staticresources.ClasspathStaticResourceLoader;
import com.bt.rsqe.web.staticresources.StaticResourceLoader;

public class UserManagementClasspathStaticResourceLoader extends ClasspathStaticResourceLoader implements StaticResourceLoader {

    public UserManagementClasspathStaticResourceLoader(String pathSuffix) {
        super(pathSuffix);
    }
}
