package com.bt.usermanagement.handler;

import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.bt.usermanagement.dto.RoleMasterDTO;
import com.bt.usermanagement.repository.UserManagementRepository;
import com.bt.usermanagement.repository.entitiy.RoleMasterEntity;
import com.bt.usermanagement.util.UrlConfiguration;
import com.bt.usermanagement.util.UserManagementConstants;
import com.bt.usermanagement.web.WebUtils;
import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

/**
 * Created by 608143048 on 08/02/2016.
 */
@Path("/home")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class rSQEHomePageHandler extends ViewFocusedResourceHandler {

    private final String cqmUrl;
    private final String nrmUrl;
    private final UserManagementRepository userManagementRepository;

    public rSQEHomePageHandler(String cqmUrl, String nrmUrl, UserManagementRepository repository) {
        super(new Presenter());
        this.userManagementRepository = repository;
        this.cqmUrl = cqmUrl;
        this.nrmUrl = nrmUrl;
    }


    public String getCqmUrl() {
        return cqmUrl;
    }

    public String getNrmUrl() {
        return nrmUrl;
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getRSQEBasePage(@HeaderParam("SM_USER") String userId) throws JSONException, URISyntaxException {
        try {
            if(isNotNull(userId)) {
                boolean hasCQMRole = false;
                boolean hasNRMRole = false;

                List<RoleMasterEntity> roleEntities = userManagementRepository.getAllRoleForUserId(userId);
                List<RoleMasterDTO> roleDTOs = new ArrayList<RoleMasterDTO>();
                for(RoleMasterEntity roleEntity : roleEntities){
                    roleDTOs.add(roleEntity.toNewDTO());
                }

                for(RoleMasterDTO rm : roleDTOs){
                    if(rm.getRoleGroup().getRoleGroupId().equals(UserManagementConstants.roleGroupConstants.get("CQM_ROLE_GROUP_ID"))){
                        hasCQMRole = true;
                    }
                    if(rm.getRoleGroup().getRoleGroupId().equals(UserManagementConstants.roleGroupConstants.get("NRM_ROLE_GROUP_ID"))){
                        hasNRMRole = true;
                    }
                }

                if(hasCQMRole && !hasNRMRole){
                    return Response.seeOther(new URI(cqmUrl)).build();
                }
                else if(!hasCQMRole && hasNRMRole) {
                    return Response.seeOther(new URI(nrmUrl)).build();
                }
                else if(hasCQMRole && hasNRMRole) {
                    String page = new Presenter().render(view("userManagementBasePage.ftl")
                            .withContext("userId", userId)
                            .withContext("cqmUrl", getCqmUrl())
                            .withContext("nrmUrl", getNrmUrl())
                            .withContext("urlConfig", UrlConfiguration.build())
                            .withContext("roleGroupConstants", UserManagementConstants.roleGroupConstantsBuild()));
                    return WebUtils.responseOk(page);
                }
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        }catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

}
