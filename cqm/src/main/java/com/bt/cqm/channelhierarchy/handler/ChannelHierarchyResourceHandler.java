package com.bt.cqm.channelhierarchy.handler;

import com.bt.cqm.dto.ChannelHierarchyDTO;
import com.bt.cqm.exception.ChannelHierarchyNoDataFoundException;
import com.bt.cqm.handler.ChannelHierarchyResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.collect.Lists.*;

@Path("/cqm/channelHierarchy")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ChannelHierarchyResourceHandler {

    private ChannelHierarchyResource channelHierarchyResource;

    public ChannelHierarchyResourceHandler(ChannelHierarchyResource channelHierarchyResource) {
        this.channelHierarchyResource = channelHierarchyResource;
    }

    @GET
    @Path("accountType")
    public Response getParentAccountNames(@QueryParam("accountType") String accountType, @QueryParam("salesChannel") String salesChannel) {

        if (null == accountType || null == salesChannel || accountType.trim().length() == 0 || salesChannel.trim().length() == 0) {
            return ResponseBuilder.notFound().withEntity("Mandatory Parameters are Missing").build();
        }
        List<ChannelHierarchyDTO> channelHierarchyDTO = null;
        List<String> parentAccountNameList = null;
        try {
            String accountTypeFramed=getAccountTypeString(accountType);
            channelHierarchyDTO = channelHierarchyResource.getParentAccountNames(accountTypeFramed, salesChannel);
            // channelHierarchyDTO = getParentAccountNameDTO(parentAccountNameList);

        } catch (ChannelHierarchyNoDataFoundException e) {
            return handleException(e);

        } catch (Exception e) {
            return handleException(e);

        }
        GenericEntity<List<ChannelHierarchyDTO>> entity = new GenericEntity<List<ChannelHierarchyDTO>>(channelHierarchyDTO) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }


    private GenericEntity<List<ChannelHierarchyDTO>> getParentAccountNameDTO(List<String> parentAccountNames) {
        List<ChannelHierarchyDTO> parentAccountNameModel = newArrayList();
        ListIterator listItr = parentAccountNames.listIterator();
        while (listItr.hasNext()) {
            String parentName = (String) listItr.next();
            parentAccountNameModel.add(new ChannelHierarchyDTO(parentName));
        }
        GenericEntity<List<ChannelHierarchyDTO>> entity = new GenericEntity<List<ChannelHierarchyDTO>>(parentAccountNameModel) {
        };
        return entity;
    }

    @GET
    @Path("getChannelCreationDetails")
    public Response getChannelCreationDetails(@QueryParam("parentAccountName") String parentAccountName, @QueryParam("customerId") String customerId) {

        if (AssertObject.isEmpty(parentAccountName) || AssertObject.isEmpty(customerId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        ChannelHierarchyDTO channelHierarchyDTO;
        try {

            channelHierarchyDTO = channelHierarchyResource.getChannelCreationDetails(parentAccountName,customerId);
        } catch (Exception e) {
            return handleException(e);

        }
        return ResponseBuilder.anOKResponse().withEntity(channelHierarchyDTO).build();
    }

    @GET
    @Path("loadChannelPartnerDetails")
    public Response loadChannelPartnerDetailsOfCustomer(@QueryParam("customerId") String customerId) {

        if (AssertObject.isEmpty(customerId)) {
            return ResponseBuilder.notFound().build();
        }
        ChannelHierarchyDTO channelHierarchyDTO;
        ChannelHierarchyDTO channelHierarchyBillingAccountDTO;
        try {
            channelHierarchyDTO = channelHierarchyResource.loadChannelPartnerDetailsOfCustomer(customerId);
            channelHierarchyBillingAccountDTO = channelHierarchyResource.getBillingAccount(customerId);
            channelHierarchyDTO.setBillingAccount(channelHierarchyBillingAccountDTO.getBillingAccount());
            if(channelHierarchyDTO == null){
                return ResponseBuilder.badRequest().withEntity("CustomerId null.").build();
            }
        }  catch (Exception e) {
            return handleException(e);

        }
        return ResponseBuilder.anOKResponse().withEntity(channelHierarchyDTO).build();
    }


    @GET
    @Path("createChannelPartnerCustomerId")
    public Response createChannelPartner(@QueryParam("customerId") String customerId,
                                         @QueryParam("accountType") String accountType,
                                         @QueryParam("parentCustomerName") String parentCustomerName,
                                         @QueryParam("parentAccountReference") String parentAccountReference,
                                         @QueryParam("billingAccount") String billingAccount,
                                         @QueryParam("yearlyCommittedRev") String yearlyCommittedRev,
                                         @QueryParam("salesChannelType") String salesChannelType,
                                         @QueryParam("tradeLevel") String tradeLevel,
                                         @QueryParam("salesChannelOrg") String salesChannelOrg,
                                         @QueryParam("customerName") String customerName)

    {
        String id = null;

        // Required parameters not available... return
        if (AssertObject.isEmpty(customerId)) {
            return ResponseBuilder.notFound().build();
        }

        try {

            if ("null".equals(parentCustomerName)) {
                parentCustomerName = "";
            }
            if ("null".equals(parentAccountReference)) {
                parentAccountReference = "";
            }
            if ("null".equals(billingAccount)) {
                billingAccount = "";
            }
            if ("null".equals(yearlyCommittedRev)) {
                yearlyCommittedRev = "";
            }

            if (null != accountType) {
                id =  channelHierarchyResource.createChannelPartner(customerId, accountType, parentCustomerName, parentAccountReference, billingAccount, yearlyCommittedRev, salesChannelType, tradeLevel, salesChannelOrg, customerName);
            }
            return ResponseBuilder.anOKResponse().withEntity(id).build();

        } catch (Exception e) {
            return handleException(e);
        }
    }

    private String getAccountTypeString(String accountType) {
        StringBuffer sb = null;
        if (accountType != null) {
            sb = new StringBuffer();
            if (accountType.equalsIgnoreCase("CP")) {
                sb.append("");
            } else if (accountType.equalsIgnoreCase("SUB-CP")) {
                sb.append("'CP'");
            } else if (accountType.equalsIgnoreCase("CP-ROOT-CUSTOMER")) {
                sb.append("'CP','SUB-CP'");
            } else if (accountType.equalsIgnoreCase("CP-SUB-CUSTOMER")) {
                sb.append("'CP','SUB-CP','CP-ROOT-CUSTOMER'");
            }
        }
        return sb.toString();
    }

    private Response handleException(Exception ex) {
        String errorMsg = ex.getMessage();
        return ResponseBuilder.notFound().withEntity(errorMsg).build();
    }


}

