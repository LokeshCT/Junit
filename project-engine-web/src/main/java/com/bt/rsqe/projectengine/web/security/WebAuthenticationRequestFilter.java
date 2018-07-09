package com.bt.rsqe.projectengine.web.security;

import com.bt.rsqe.error.RsqeAuthenticationException;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.UserContextResolver;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;

@PreMatching
public class WebAuthenticationRequestFilter implements ContainerRequestFilter {

    public static final String RSQE_STATIC_URI = "/static/";
    private final UserContextResolver userContextResolver;

    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    public WebAuthenticationRequestFilter(UserContextResolver userContextResolver) {
        this.userContextResolver = userContextResolver;
    }


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (!isMethodAllowed(requestContext)) {
            throw new MethodNotAllowedException(requestContext);
        }

        if (requestPathIsNotExemptFromAuthentication(requestContext.getUriInfo().getPath())) {
            try {
                UserContextManager.setCurrent(userContextResolver.resolve(requestContext));
            } catch (RsqeAuthenticationException e) {
                LOG.failedToResolveUserContext(requestContext.getUriInfo().getPath(), e);
                throw e;
            }
        }
    }


    private boolean requestPathIsNotExemptFromAuthentication(String path) {
        final boolean requestPathIsNotForStaticResource = !path.toLowerCase().contains(RSQE_STATIC_URI.toLowerCase());
        final boolean requestIsNotForFavicon = !path.toLowerCase().contains("favicon.ico");

        return requestPathIsNotForStaticResource && requestIsNotForFavicon;
    }

    private boolean isMethodAllowed(ContainerRequestContext request) {
        return request.getMethod().equals(HttpMethod.GET)
               || request.getMethod().equals(HttpMethod.POST)
               || request.getMethod().equals(HttpMethod.PUT);
    }


    public static class MethodNotAllowedException extends RuntimeException {

        public MethodNotAllowedException(ContainerRequestContext requestContext) {
            super(requestContext.getMethod() + " not allowed for " + requestContext.getUriInfo().getPath());
        }
    }

    private interface Logger {

        @Log(level = LogLevel.WARN, format = "Failed to resolve user context for URI '%s'.")
        void failedToResolveUserContext(String requestUri, Exception error);
    }

}
