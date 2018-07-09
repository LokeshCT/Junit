package com.bt.rsqe.projectengine.web.security;

import com.bt.rsqe.error.RsqeAuthorizationException;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import com.bt.rsqe.session.client.PermissionResource;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.PathSegment;
import java.io.IOException;
import java.util.List;


public class CustomerAuthorizationFilter implements ContainerRequestFilter {

    private PermissionResource permissionResource;
    public static final String FAVICON_URI = "favicon.ico";
    public static final String RSQE_STATIC_URI = "static";
    public static final String RSQE_MONITORING_URI = "monitoring";
    public static final String RSQE_SERVER_STATUS_URI = "serverStatus";
    public static final String WEB_METRICS = "web-metrics";
    public static final String ERROR_METRICS = "error-metrics";

    public CustomerAuthorizationFilter(PermissionResource permissionResource) {
        this.permissionResource = permissionResource;
    }


    public void filter(ContainerRequestContext requestContext) throws IOException {
        authorizeCustomer(requestContext);
    }

    private void authorizeCustomer(ContainerRequestContext request) throws IOException {
        final String normalisedRequestPath = request.getUriInfo().getPath().toLowerCase();
        if (normalisedRequestPath.contains(RSQE_STATIC_URI.toLowerCase())
            || normalisedRequestPath.contains(RSQE_MONITORING_URI.toLowerCase())
            || normalisedRequestPath.contains(FAVICON_URI.toLowerCase())
            || normalisedRequestPath.contains(WEB_METRICS.toLowerCase())
            || normalisedRequestPath.contains(ERROR_METRICS.toLowerCase())
            || normalisedRequestPath.contains(RSQE_SERVER_STATUS_URI.toLowerCase())) {
            return;
        }

        String customerId = null;
        List<PathSegment> segments = request.getUriInfo().getPathSegments(true);
        for (int i = 0; i < segments.size(); i++) {
            if ("customers".equals(segments.get(i).getPath())) {
                customerId = segments.get(i + 1).getPath();
            }
        }

        if (customerId == null) {
            throw new NullPointerException("Customer Id must be in the request url - /customers/{customerId}/ " + request.getUriInfo().getPath());
        }

        final PermissionsDTO permissionsDTO = permissionResource.userPermissionsForCustomer(customerId);

        final UserContext current = UserContextManager.getCurrent();
        if (!permissionsDTO.customerAccess) {
            final String loginName = current.getLoginName();
            throw new RsqeAuthorizationException(loginName, request.getUriInfo().getPath());
        } else {
            UserContext withPermissions = new UserContext(new UserPrincipal(current.getLoginName()), current.getRsqeToken(), permissionsDTO);
            UserContextManager.setCurrent(withPermissions);
        }
    }
}
