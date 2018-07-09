package com.bt.rsqe.projectengine.web.security;

import com.bt.rsqe.security.RestAuthenticationRequestFilter;
import com.bt.rsqe.web.rest.exception.UnauthorizedException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;

@PreMatching
public class RestOrWebAuthenticationRequestFilter implements ContainerRequestFilter {
    private final RestAuthenticationRequestFilter restAuthRequestFilter;
    private final WebAuthenticationRequestFilter webAuthRequestFilter;

    public RestOrWebAuthenticationRequestFilter(RestAuthenticationRequestFilter restAuthRequestFilter, WebAuthenticationRequestFilter webAuthRequestFilter) {
        this.restAuthRequestFilter = restAuthRequestFilter;
        this.webAuthRequestFilter = webAuthRequestFilter;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            restAuthRequestFilter.filter(requestContext);
        } catch (UnauthorizedException e) {
            webAuthRequestFilter.filter(requestContext);
        }
    }
}
