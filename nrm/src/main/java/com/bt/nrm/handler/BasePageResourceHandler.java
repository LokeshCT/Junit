package com.bt.nrm.handler;

import com.bt.nrm.util.Constants;
import com.bt.nrm.util.UrlConfiguration;
import com.bt.nrm.web.WebUtils;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.bt.usermanagement.resources.UserResource;
import com.google.gson.Gson;
import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/nrm")
public class BasePageResourceHandler extends ViewFocusedResourceHandler {
    private Gson gson;
    private UserResource userResource;

    public BasePageResourceHandler(UserResource userResource) {
        super(new Presenter());
        this.gson = new Gson();
        this.userResource = userResource;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getNrmBasePage() throws JSONException {
        String page = new Presenter().render(view("nrmBasePage.ftl")
                                                 .withContext("urlConfig", UrlConfiguration.build())
                                                 .withContext("requestStateConstants", Constants.requestStateConstantsBuild())
                                                 .withContext("requestResponseType", Constants.requestResponseTypeBuild())
                                                 .withContext("requestEvaluatorStateConstants", Constants.requestEvaluatorStateConstantsBuild())
                                                 .withContext("requestEvaluatorResponseConstants", Constants.requestEvaluatorResponseConstantsBuild())
                                                 .withContext("nrmUserRoles", Constants.nrmUserRolesBuild()));
        return WebUtils.responseOk(page);
    }

    @GET
    @Path("/logout")
    public Response logout(@QueryParam("userId") String nrmUserEIN) {
        return null;
    }
}
