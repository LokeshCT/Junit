package com.bt;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.web.staticresources.StaticResourceLoader;
import com.bt.rsqe.web.staticresources.UnableToLoadResourceException;
import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

import java.net.URL;

//Not able to load resources from common, so moving this to com.bt
public class ClasspathStaticResourceLoader implements StaticResourceLoader {

    private final String pathSuffix;
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    public ClasspathStaticResourceLoader(String pathSuffix) {
        this.pathSuffix = pathSuffix;
    }

    @Override
    public byte[] loadBinaryResource(String resourcePath) throws UnableToLoadResourceException {
        URL resource = null;
        String fullResourcePath = pathSuffix+resourcePath;
        try {
            LOG.logStaticResourceRequest("resource url : "+fullResourcePath);
            resource = Resources.getResource(getClass(), Joiner.on("").join(pathSuffix, resourcePath));
            return ByteStreams.toByteArray(resource.openStream());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.resourceNotFound("resource url : "+resource);
            throw new UnableToLoadResourceException(e);
        }
    }

    public interface Logger {
        @Log(level = LogLevel.DEBUG, format = "Loading static resource : %s")
        void logStaticResourceRequest(String resource);
        @Log(level = LogLevel.WARN, format = "Failed loading static resource : %s")
        void resourceNotFound(String resource);
    }

}
