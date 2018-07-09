package com.bt.rsqe.projectengine.web.security;

import com.bt.rsqe.config.CookieConfig;
import com.bt.rsqe.security.Credentials;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;

import static java.lang.Boolean.*;

public class WebAuthenticationResponseFilter implements ContainerResponseFilter {

    private final CookieConfig cookieConfig;

    public WebAuthenticationResponseFilter(CookieConfig cookieConfig) {
        this.cookieConfig = cookieConfig;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final UserContext userContext = UserContextManager.getCurrent();
        if (userContext != null) {
            NewCookie cookie = new NewCookie(Credentials.RSQE_TOKEN,
                                             userContext.getRsqeToken(),
                                             "/",
                                             cookieDomain(requestContext),
                                             1,
                                             "",
                                             -1,
                                             false);
            responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, cookie);
        }
        UserContextManager.clear();
    }

    private String cookieDomain(ContainerRequestContext requestContext) {
        final boolean isCookieDomainOverWriteAllowed = parseBoolean(cookieConfig.getCookieDomainConfig().getOn());

        return isCookieDomainOverWriteAllowed ?
            cookieConfig.getCookieDomainConfig().getValue() :
            requestContext.getHeaderString("Host").split(":")[0];
    }
}
