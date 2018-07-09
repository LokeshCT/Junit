package com.bt.nrm.web;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

public class NRMAuthenticationRequestFilter implements ContainerRequestFilter {

    public NRMAuthenticationRequestFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String userId = requestContext.getHeaderString("SM_USER");
        if (userId != null && userId.length() > 0) {
            int startIndx = userId.lastIndexOf("/");
            if (startIndx < 0) {
                startIndx = userId.lastIndexOf("\\");
            }
            if (startIndx > 0) {
                String retUser = userId.substring(startIndx + 1);
                requestContext.getHeaders().putSingle("SM_USER", retUser);
            }
        }

    }
}
