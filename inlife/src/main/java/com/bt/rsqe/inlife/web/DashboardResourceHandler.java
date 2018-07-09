package com.bt.rsqe.inlife.web;

import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/rsqe/inlife/dashboard")
@Produces({MediaType.TEXT_HTML})
public class DashboardResourceHandler extends ViewFocusedResourceHandler {

    public DashboardResourceHandler(final Presenter presenter) {
        super(presenter);
    }

    @GET
    public Response getDashBoardPage() {
        return responseOk(presenter.render(view("dashboard.ftl")));
    }
}
