package com.bt.usermanagement.web;

import com.bt.rsqe.web.staticresources.UnableToLoadResourceException;

public interface StaticResourceLoader {
    byte[] loadBinaryResource(String resourceName) throws UnableToLoadResourceException;
}
