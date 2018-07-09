package com.bt.rsqe.inlife.web;

import com.bt.rsqe.web.staticresources.StaticResourceLoader;
import com.bt.rsqe.web.staticresources.UnableToLoadResourceException;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class InlifeStaticResourceLoader implements StaticResourceLoader{
    @Override
    public byte[] loadBinaryResource(String resourcePath) throws UnableToLoadResourceException {
        try {
            URL resource = Resources.getResource(getClass(), resourcePath);
            return ByteStreams.toByteArray(resource.openStream());
        } catch (IOException e) {
            throw new UnableToLoadResourceException(e);
        }
    }

    public InputStream loadResourceAsStream (String resourcePath) throws UnableToLoadResourceException {
        try {
            URL resource = Resources.getResource(getClass(), resourcePath);
            return resource.openStream() ;
        } catch (IOException e) {
            throw new UnableToLoadResourceException(e);
        }
    }
}
