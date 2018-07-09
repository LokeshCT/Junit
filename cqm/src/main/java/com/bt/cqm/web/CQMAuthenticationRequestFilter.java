package com.bt.cqm.web;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

public class CQMAuthenticationRequestFilter implements ContainerRequestFilter {


    public CQMAuthenticationRequestFilter() {
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

        if(requestContext.getUriInfo().getAbsolutePath().toString().equals("/cqm/logout")){
            requestContext.getHeaders().remove("SM_USER");
            requestContext.getHeaders().remove("authn_secret");
            requestContext.getHeaders().remove("USER_ROLE");
            requestContext.getHeaders().remove("USER_EMAIL");
            requestContext.getHeaders().remove("USER_NAME");
        }

    }

}
