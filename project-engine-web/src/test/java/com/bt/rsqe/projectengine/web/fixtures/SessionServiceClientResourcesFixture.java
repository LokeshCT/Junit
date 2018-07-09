package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.security.ExpedioRsqeCredentials;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserPrincipal;
import com.bt.rsqe.session.client.ExpedioSessionResource;
import com.bt.rsqe.session.client.PermissionResource;
import com.bt.rsqe.session.client.SessionServiceClientResources;

import java.net.URI;

public class SessionServiceClientResourcesFixture {
    private PermissionsDTO permissionsDTO = new PermissionsDTO(true, false, false, false, false, false);

    public static SessionServiceClientResourcesFixture aFakeSessionService() {
        return new SessionServiceClientResourcesFixture();
    }

    public SessionServiceClientResourcesFixture withPermissions(PermissionsDTO permissionsDTO){
        this.permissionsDTO = permissionsDTO;
        return this;
    }

    public SessionServiceClientResources build(){
        return new SessionServiceClientResources((URI) null, ""){
            @Override
            public PermissionResource getPermissionResource() {
                return new PermissionResource(URI.create("")){
                    @Override
                    public PermissionsDTO userPermissionsForCustomer(String customerId) {
                        return permissionsDTO;
                    }
                };
            }

            @Override
            public ExpedioSessionResource getExpedioSessionResource() {
                return new ExpedioSessionResource(URI.create("")){
                    @Override
                    public UserContext get(ExpedioRsqeCredentials credentials) {
                        return new UserContext(new UserPrincipal(""), "", permissionsDTO);
                    }

                    @Override
                    public UserContext post(ExpedioRsqeCredentials credentials) {
                        return new UserContext(new UserPrincipal(""), "", permissionsDTO);
                    }
                };
            }
        };
    }
}
