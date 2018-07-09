package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerrecord.ExpedioServicesResource;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.expedio.services.CloseBidManagerActivityDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalRequestDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalResponseDTO;

public class ExpedioServicesFacade {

    private ExpedioServicesResource expedioServices;
    private ExpedioProjectResource expedioProjectsResource;
    private UserFacade userFacade;

    public ExpedioServicesFacade(ExpedioServicesResource expedioServices, ExpedioProjectResource expedioProjectsResource, UserFacade userFacade) {
        this.expedioServices = expedioServices;
        this.expedioProjectsResource = expedioProjectsResource;
        this.userFacade = userFacade;
    }

    public BidManagerApprovalResponseDTO requestDiscountApproval(BidManagerApprovalRequestDTO bidManagerApprovalRequestDTO) {
        return expedioServices.postBidManagerDiscountApprovalRequest(bidManagerApprovalRequestDTO);
    }

    public BidManagerApprovalResponseDTO requestIcbApproval(BidManagerApprovalRequestDTO bidManagerApprovalRequestDTO) {
        return expedioServices.postBidManagerIcbApprovalRequest(bidManagerApprovalRequestDTO);
    }


    public ProjectDTO getExpedioProject(String projectId) {
        return expedioProjectsResource.getProject(projectId);
    }

    public void putExpedioProject(String projectId, ProjectDTO projectDTO) {
        expedioProjectsResource.put(projectId, projectDTO);
    }

    public void postExpedioProject(String projectId, ProjectDTO projectDTO) {
        expedioProjectsResource.post(projectId, projectDTO);
    }

    public void closeBidManagerDiscountApprovalRequestActivity(CloseBidManagerActivityDTO closeRequest) {
         expedioServices.postBidManagerDiscountApprovalCloseRequest(closeRequest);
    }

    public void closeBidManagerIcbApprovalRequestActivity(CloseBidManagerActivityDTO closeRequest) {
         expedioServices.postBidManagerIcbApprovalCloseRequest(closeRequest);
    }

    public UserDTO getUserDetails(String loginName) {
        return userFacade.findUser(loginName);
    }
}
