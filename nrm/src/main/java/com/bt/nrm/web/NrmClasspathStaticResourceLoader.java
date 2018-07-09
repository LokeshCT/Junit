package com.bt.nrm.web;

import com.bt.rsqe.web.staticresources.ClasspathStaticResourceLoader;
import com.bt.rsqe.web.staticresources.StaticResourceLoader;

public class NrmClasspathStaticResourceLoader extends ClasspathStaticResourceLoader implements StaticResourceLoader {

    public NrmClasspathStaticResourceLoader(String pathSuffix) {
        super(pathSuffix);
    }
}
