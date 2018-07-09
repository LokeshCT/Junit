package com.bt.cqm.web;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.staticresources.StaticResourceContentType;
import com.bt.rsqe.web.staticresources.StaticResourceLoader;
import com.bt.rsqe.web.staticresources.UnableToLoadResourceException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.UUID;



@Path("/cqm/static")
public class StaticResourceHandler {

    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    private static final String applicationLifeTimeEtag = UUID.randomUUID().toString();
    private static final Date applicationModified = new Date();

    private final StaticResourceLoader resourceLoader;

    public StaticResourceHandler(StaticResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GET
    @Path("/{resource: [\\.a-zA-Z0-9\\-+_/]+}")
    public Response getResource(@PathParam("resource") String resource) {
        return loadStaticResource(resource, true);
    }

    private Response loadStaticResource(String resource, boolean cached) {
        LOG.logStaticResourceRequest(resource);
        try {
            final byte[] entity = resourceLoader.loadBinaryResource(resource);
            if (cached) {
                return ResponseBuilder.anOKResponse().cached(ResponseBuilder.upTo(1).days())
                        .withEntity(entity)
                        .withEtag(applicationLifeTimeEtag)
                        .withLastModifiedDate(applicationModified)
                        .withHeader("Content-Type", StaticResourceContentType.fromFileName(resource).contentType())
                        .build();
            } else {
                return ResponseBuilder.anOKResponse().withEntity(entity).build();
            }

        } catch (UnableToLoadResourceException e) {
            LOG.resourceNotFound(resource);
            return notFound();
        }
    }

    private Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public interface Logger {
        @Log(level = LogLevel.DEBUG, format = "Static resource requested: %s")
        void logStaticResourceRequest(String resource);

        @Log(level = LogLevel.WARN, format = "Resource not found: %s")
        void resourceNotFound(String resource);
    }

}
