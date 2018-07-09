package com.bt.rsqe.expedio.services;

import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.expedio.activity.AcceptActivityTaskDTO;
import com.bt.rsqe.expedio.activity.ReassignActivityTaskDTO;
import com.bt.rsqe.expedio.activity.RejectActivityTaskDTO;
import com.bt.rsqe.expedio.activity.WithdrawApprovalActivityTaskDTO;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;
import com.bt.rsqe.web.rest.exception.RestException;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

public class ActivityResource {

    private static final String ACTIVITY = "activity";
    private RestRequestBuilder restRequestBuilder;

    public ActivityResource(URI baseUri, String secret) {
        URI uri = UriBuilder.buildUri(baseUri, "rsqe", "expedio-services");
        this.restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public ActivityResource(ExpedioFacadeConfig clientConfig) {
        this(UriBuilder.buildUri(clientConfig.getApplicationConfig()), clientConfig.getRestAuthenticationClientConfig().getSecret());
    }

    public List<ActivityDTO> getActivityList(GetActivityRequestDTO requestDTO) {
        return restRequestBuilder.build("activities").put(requestDTO).getEntity(new GenericType<List<ActivityDTO>>() {
        });
    }

    public String create(ActivityDTO activityDTO) {
        return restRequestBuilder.build(ACTIVITY, "create").post(activityDTO).getEntity(String.class);
    }

    public boolean reassignActivityDelegation(ReassignActivityTaskDTO taskDTO) {
        boolean isSuccess = false;
        try {
            RestResponse resp = restRequestBuilder.build(ACTIVITY, "reassign").post(taskDTO);
            if (resp.getStatus() == Response.Status.OK.getStatusCode()) {
                isSuccess = true;
            }
        } catch (Exception ex) {

        }
        return isSuccess;
    }

    public boolean acceptActivityDelegation(AcceptActivityTaskDTO taskDTO) {
        boolean isSuccess = false;
        try {
            RestResponse resp = restRequestBuilder.build(ACTIVITY, "accept").post(taskDTO);
            if (resp.getStatus() == Response.Status.OK.getStatusCode()) {
                isSuccess = true;
            }
        } catch (Exception ex) {

        }
        return isSuccess;
    }

    public boolean rejectActivityDelegation(RejectActivityTaskDTO taskDTO) {
        boolean isSuccess = false;
        try {
            RestResponse resp = restRequestBuilder.build(ACTIVITY, "reject").post(taskDTO);
            if (resp.getStatus() == Response.Status.OK.getStatusCode()) {
                isSuccess = true;
            }
        } catch (Exception ex) {

        }
        return isSuccess;
    }

    public boolean withdrawApproval(WithdrawApprovalActivityTaskDTO taskDTO) throws RestException{
        boolean isSuccess = false;
        try {
            RestResponse resp = restRequestBuilder.build(ACTIVITY, "withdraw").post(taskDTO);
            if (resp.getStatus() == Response.Status.OK.getStatusCode()) {
                isSuccess = true;
            }
        }/* catch (RestException ex) {
             throw ex;
        }*/catch (Exception ex) {

        }
        return isSuccess;
    }

    public UpdateActivityResponseDTO updateStatus(UpdateStatusDTO updateStatusDTO) {
        return restRequestBuilder.build(ACTIVITY, "status").post(updateStatusDTO).getEntity(new GenericType<UpdateActivityResponseDTO>() {
        });
    }

    public UpdateActivityResponseDTO changeOwnership(ChangeOwnershipDTO changeOwnershipDTO) {
        return restRequestBuilder.build(ACTIVITY, "ownership").post(changeOwnershipDTO).getEntity(new GenericType<UpdateActivityResponseDTO>() {
        });
    }

    public MNCCustomersDTO getMncCustomers(String boatId){
        return restRequestBuilder.build("GetMNCCustomers",boatId).get().getEntity(new GenericType<MNCCustomersDTO>() {
        });
    }

}
