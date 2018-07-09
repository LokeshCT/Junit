package com.bt.nrm.handler;

import com.bt.rsqe.EmailService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/nrm/emailService")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NRMEmailHandler {

    private EmailService emailService = null;

    public NRMEmailHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @POST
    @Path("/sendEmail")
    public Response sendEmail(@QueryParam("subject") String subject, @QueryParam("messageBody") String messageBody, @QueryParam("emailId") String emailId) {
        try {
            emailService.sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT, subject, messageBody, emailId);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }
}