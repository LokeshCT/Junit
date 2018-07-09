package com.bt.cqm.handler;

import com.bt.cqm.dto.SiteMinDataDTO;
import com.bt.cqm.exception.NonUniqueRemedyLogException;
import com.bt.cqm.exception.RemedySysLogNotFoundException;
import com.bt.rsqe.customerinventory.client.resource.ContractResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.SiteLocationDTO;
import com.bt.rsqe.customerinventory.dto.contract.ContractDTO;
import com.bt.rsqe.customerinventory.dto.customer.CustomerDTO;
import com.bt.rsqe.customerinventory.dto.site.LocationDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteRegionDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteStatusDto;
import com.bt.rsqe.customerinventory.dto.site.SiteUpdateDTO;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.customerrecord.CustomerNotFoundException;
import com.bt.rsqe.error.web.UserExceptionForAjaxRequestHandler;
import com.bt.rsqe.ppsr.client.dto.GPOPFilterDTO;
import com.bt.rsqe.ppsr.client.dto.POPDetailDTO;
import com.bt.rsqe.ppsr.client.pop.PpsrPOPDetailClient;
import com.bt.rsqe.projectengine.SiteModifiedResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.rest.exception.ConflictException;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.bt.rsqe.web.rest.exception.RestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bt.cqm.utils.Utility.*;

@Path("/cqm/site")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class SiteResourceHandler {

    private static final String SM_USER = "SM_USER";
    private static final String CONTRACT_ID = "contractID";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CUSTOMER_NAME = "customerName";
    private static final String SITE_ID = "siteId";
    private final CustomerResource customerResource;
    private final SiteResourceClient siteResourceClient;
    private final ContractResourceClient contractResource;
    private final SiteModifiedResource siteModifiedResource;
    private final PpsrPOPDetailClient ppsrPOPDetailClient;
    private final String SEMI_COLON_CONSTANT = ";";

    private static Logger LOG = LoggerFactory.getLogger(SiteResourceHandler.class);

    public SiteResourceHandler(CustomerResource customerResource, SiteResourceClient siteResourceClient, ContractResourceClient contractResource, SiteModifiedResource siteModifiedResource, PpsrPOPDetailClient ppsrPOPDetailClient) {
        this.customerResource = customerResource;
        this.siteResourceClient = siteResourceClient;
        this.contractResource = contractResource;
        this.siteModifiedResource = siteModifiedResource;
        this.ppsrPOPDetailClient = ppsrPOPDetailClient;
    }

    @GET
    @Path("centralSite")
    public Response getCentralSite(@HeaderParam(SM_USER) String userId, @QueryParam(CONTRACT_ID) Long contractId, @QueryParam("customerID") Long customerId) {
        if (!AssertObject.isNotNull(contractId)) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid ContractId!!")).build();
        }

        SiteDTO centralSiteDTO = null;

        try {
            centralSiteDTO = siteResourceClient.getCentralSite(contractId);

        } catch (ResourceNotFoundException ex) {

        }

        if (centralSiteDTO == null) {

            if (customerId == null) {
                LOG.warn("No contract Level Central Site is found for Contract Id:" + contractId + " . Input Customer Id is null for further processing !!");
                return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("No contract Level Central Site is found. Input Customer Id is null for further processing !!")).build();
            }
            // Find If Central Site is available in BFG_SITES
            List<SiteDTO> siteList = siteResourceClient.getSites(customerId, "CENTRAL");

            if (siteList == null || siteList.size() < 1) {
                LOG.info("No Central Sites Found for Customer ID:" + customerId + " , Contract ID:" + contractId);
                return ResponseBuilder.notFound().build();
            } else if (siteList.size() == 1) {
                LOG.info("No Central Sites found in Contract. However could identify one Central Site in BFG Sites for Customer ID:" + customerId + " , Contract ID:" + contractId);
                SiteDTO siteDTO = siteList.get(0);

                ContractDTO contractDTO = contractResource.getContract(contractId);
                contractDTO.setCentralSite(siteDTO);

                contractResource.updateContract(contractDTO, userId);
                centralSiteDTO = siteDTO;
                LOG.info("Updated the BFG Central Site to Contract. Customer ID:" + customerId + " , Contract ID:" + contractId);
            } else {
                GenericEntity<List<SiteDTO>> listGenericEntity = new GenericEntity<List<SiteDTO>>(siteList) {
                };
                return Response.status(Response.Status.CONFLICT).entity(listGenericEntity).build();
            }

            //return ResponseBuilder.notFound().build();
        }
        /* Central Site values shouldnt be displayed to Front end User*/
        if ("CENTRAL".equalsIgnoreCase(centralSiteDTO.getCity())) {
            centralSiteDTO.setBuildingNumber("");
            centralSiteDTO.setBuildingName("");
            centralSiteDTO.setSubBuilding("");
            centralSiteDTO.setStreet("");
            centralSiteDTO.setLocality("");
            centralSiteDTO.setSubLocality("");
            centralSiteDTO.setCity("");
            centralSiteDTO.setState("");
            centralSiteDTO.setPostCode("");
            centralSiteDTO.setSubPostCode("");
            centralSiteDTO.setPoBoxNumber("");
            centralSiteDTO.setPhoneNum("");
            centralSiteDTO.setLatitude("");
            centralSiteDTO.setLongitude("");
            centralSiteDTO.setCountry("");
            centralSiteDTO.setFloor("");
            centralSiteDTO.setSubPremises("");
            centralSiteDTO.setRoom("");
            centralSiteDTO.setComments("");
        }

        GenericEntity<SiteDTO> genericCentralSiteDTO = new GenericEntity<SiteDTO>(centralSiteDTO) {
        };
        return ResponseBuilder.anOKResponse().withEntity(genericCentralSiteDTO).build();
    }

    @POST
    @Path("associateCentralSite")
    public Response associateCentralSite(@HeaderParam(SM_USER) String userId, @QueryParam("contractId") Long contractId, @QueryParam(SITE_ID) String centralSiteId) {
        if (AssertObject.anyEmpty(userId, contractId, centralSiteId)) {
            return ResponseBuilder.badRequest().build();
        }

        SiteDTO siteDTO = siteResourceClient.getSite(centralSiteId);

        if (siteDTO != null && "CENTRAL".equalsIgnoreCase(siteDTO.getSiteType())) {
            ContractDTO contractDTO = contractResource.getContract(contractId);

            if (contractDTO != null) {
                if (contractDTO.getCentralSite() == null) {
                    contractDTO.setCentralSite(siteDTO);
                    contractResource.updateContract(contractDTO, userId);
                } else {
                    return Response.status(Response.Status.CONFLICT).entity(buildGenericError("Central Site :" + contractDTO.getCentralSite().getSiteId() + " already associated to the contract -" + contractId)).build();
                }

            } else {
                return ResponseBuilder.notFound().withEntity(buildGenericError("No Contract exist with contractId :" + contractId)).build();
            }

        } else {
            return ResponseBuilder.notFound().withEntity(buildGenericError("No CentralSite exist with siteId :" + centralSiteId)).build();
        }

        return ResponseBuilder.anOKResponse().build();
    }


    @POST
    @Path("/updateCentralSite")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateCentralSiteDetails(@HeaderParam(SM_USER) String userId, @QueryParam(CONTRACT_ID) Long contractId, SiteUpdateDTO siteUpdateDTO) {

        if (siteUpdateDTO == null || AssertObject.anyEmpty(userId, siteUpdateDTO.getCustomerId(), siteUpdateDTO.getCustomerName())) {
            return ResponseBuilder.badRequest().build();
        }
        CustomerDTO customerDTO = null;
        siteUpdateDTO.setUserId(userId);
        Long centralSiteId = null;
        Long customerId = siteUpdateDTO.getCustomerId();
        try {
            SiteDTO centralSiteDTO = null;
            try {
                centralSiteDTO = siteResourceClient.getCentralSite(contractId);
            } catch (ResourceNotFoundException ex) {

            }

            if (centralSiteDTO != null) {
                /*Update Central Site*/
                centralSiteId = new Long(centralSiteDTO.getSiteId());
                siteUpdateDTO.setSitId(centralSiteId);
                siteUpdateDTO.setSitName(centralSiteDTO.getName());
                siteResourceClient.updateSite(userId, siteUpdateDTO);
                customerDTO = customerResource.getCustomer(customerId.toString());

            } else {
                /*Create Central Site*/

                customerDTO = customerResource.getCustomer(customerId.toString());
                if (customerDTO != null) {
                    String siteName = "CITY - " + customerId + " - 1";
                    siteUpdateDTO.setSitName(siteName);
                    siteUpdateDTO.setSitType(Constants.DEFAULT_CENTRAL_SITE_TYPE);
                    siteUpdateDTO.setSitComment(Constants.DEFAULT_CENTRAL_SITE_TYPE);
                    siteUpdateDTO.setSitCusId(customerId);
                    siteUpdateDTO.setCustomerId(customerId);
                    siteUpdateDTO.setSitCusReference(customerDTO.getCusReference());
                    SiteDTO dummySiteDTO = null;
                    try {
                        centralSiteId = siteResourceClient.createSite(userId, siteUpdateDTO);
                        dummySiteDTO = new SiteDTO();
                        dummySiteDTO.setSiteId(centralSiteId);
                    } catch (ConflictException ex) {
                        dummySiteDTO = siteResourceClient.getSiteByName(siteName);
                    }

                    ContractDTO contractDTO = contractResource.getContract(contractId);

                    contractDTO.setCentralSite(dummySiteDTO);

                    contractResource.updateContract(contractDTO, userId);

                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity("No customer found with customer Id :" + customerId).build();
                }
            }
            updateCustomer(userId, siteUpdateDTO, customerDTO);
            return ResponseBuilder.anOKResponse().withEntity(String.valueOf(siteUpdateDTO.getSitId())).build();
        } catch (RestException ex) {
            throw ex;
        } catch (Exception e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/createBranchSite")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createBranchSiteDetails(@HeaderParam(SM_USER) String userId,
                                            @QueryParam(CUSTOMER_ID) String cusId,
                                            @QueryParam(CUSTOMER_NAME) String cusName,
                                            SiteUpdateDTO siteUpdateDTO
    ) {

        if (AssertObject.anyEmpty(userId, cusId, cusName, siteUpdateDTO)) {
            return ResponseBuilder.badRequest().build();
        }
        String ein = userId;
        try {

            createRegion(userId, siteUpdateDTO);

            siteUpdateDTO.setSitType("BRANCH");
            siteUpdateDTO.setAdrCountryCode(siteResourceClient.getCountryIsoCode(siteUpdateDTO.getAdrCountry()));
            try {
                siteUpdateDTO.getLocation().setLocSubPremise(getSubPremiseValue(siteUpdateDTO.getLocation()));
            } catch (Exception e) {
            }
            Long siteId = this.siteResourceClient.createSite(userId, siteUpdateDTO);
            return ResponseBuilder.anOKResponse().withEntity(String.valueOf(siteId)).build();
        } catch (Exception ex) {
            return new UserExceptionForAjaxRequestHandler().convertToResponse(ex);
        }
    }

    @POST
    @Path("/updateBranchSite")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateBranchSiteDetails(@HeaderParam(SM_USER) String userId, @QueryParam(CUSTOMER_ID) String cusId, @QueryParam(CUSTOMER_NAME) String cusName, SiteUpdateDTO siteUpdateDTO, @HeaderParam("IS_MNC") String isMnc) {
        boolean status = false;
        if (AssertObject.anyEmpty(userId, cusId, cusName, siteUpdateDTO)) {
            return ResponseBuilder.badRequest().build();
        }
        try {
            try {
                if (!"Y".equalsIgnoreCase(isMnc)) {
                    String subPremise = getSubPremiseValue(siteUpdateDTO.getLocation());
                    siteUpdateDTO.getLocation().setLocSubPremise(subPremise);
                }
            } catch (Exception e) {
            }

            createRegion(userId, siteUpdateDTO);
            siteUpdateDTO.setAdrCountryCode(siteResourceClient.getCountryIsoCode(siteUpdateDTO.getAdrCountry()));
            String siteId = this.siteResourceClient.updateSite(userId, siteUpdateDTO);
            try {
                if (siteUpdateDTO.isNotifySiteUpdate()) {
                    //Notify SQE
                    try {
                        LOG.info("Going to notify site update to SQE. Site Id -> " + siteUpdateDTO.getSitId());
                        status = siteResourceClient.sendSiteModificationNotification(siteUpdateDTO.getSitId().toString());
                        LOG.info("SQE notify-site-update status -> " + status);
                    } catch (Exception e) {
                        LOG.warn("FAILED to notify site update to SQE. Site Id -> " + siteUpdateDTO.getSitId(),e);
                    }

                    //Notify RSQE
                    try {
                        LOG.info("Going to notify site update to RSQE. Site Id -> " + siteUpdateDTO.getSitId());
                        siteModifiedResource.siteModified(siteUpdateDTO.getSitId().toString());
                        LOG.info("RSQE notify-site-update status -> SUCCESS");
                    } catch (Exception e) {
                        LOG.warn("FAILED to notify site update to RSQE. Site Id -> " + siteUpdateDTO.getSitId(),e);
                    }

                }
            } catch (Exception e) {

            }
            return ResponseBuilder.anOKResponse().withEntity(String.valueOf(status)).build();
        } catch (RestException e) {
            throw e;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/isSiteActive")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSiteStatus(@QueryParam(SITE_ID) String siteId) {
        if (AssertObject.anyEmpty(siteId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid siteId")).build();
        }

        try {
            SiteStatusDto siteStatusDto = siteResourceClient.getSiteStatus(siteId.trim());
            return ResponseBuilder.anOKResponse().withEntity(siteStatusDto).build();
        } catch (RestException e) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(e.getMessage())).build();
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }
    }

    @GET
    @Path("/notifySiteUpdate")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendSiteUpdateNotification(@QueryParam(SITE_ID) String siteId) {
        try {
            boolean status = siteResourceClient.sendSiteModificationNotification(siteId);
            return ResponseBuilder.anOKResponse().withEntity(String.valueOf(status)).build();
        } catch (RestException e) {
            throw e;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/createLocation")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createLocation(@HeaderParam(SM_USER) String userId, SiteLocationDTO siteLocationDTO) {
        if (siteLocationDTO == null || AssertObject.anyEmpty(userId, siteLocationDTO.getLocSubPremise(), siteLocationDTO.getLocFloor(), siteLocationDTO.getLocRoom(), siteLocationDTO.getLocSitId())) {
            return ResponseBuilder.badRequest().withEntity(String.format("Invalid SiteId/SubLocationName/Floor/Room !! .SiteId =%s, Sub Location Name = %s, Floor =%s, Room =%s", siteLocationDTO.getLocSitId(), siteLocationDTO.getLocSubPremise(), siteLocationDTO.getLocFloor(), siteLocationDTO.getLocRoom())).build();
        }
        /*try {
            siteLocationDTO.setLocSubPremise(getSubPremiseValue(siteLocationDTO));
        } catch (Exception e) {
        }*/
        String locId = siteResourceClient.createLocation(userId, siteLocationDTO);
        return ResponseBuilder.anOKResponse().withEntity(locId).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getBranchSite")
    public Response getBranchSite(@QueryParam(CUSTOMER_ID) Long customerId) {
        if (AssertObject.anyEmpty(customerId)) {
            return ResponseBuilder.badRequest().build();
        }

        List<SiteDTO> branchSiteDTOList = null;
        try {
            long startTime = System.currentTimeMillis();
            branchSiteDTOList = flattenSiteDto(siteResourceClient.getAllBranchSite(customerId));
            LOG.debug("Received response in {} ms", System.currentTimeMillis() - startTime);

        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }

        if (branchSiteDTOList != null) {
            GenericEntity<List<SiteDTO>> genericSiteDTOList = new GenericEntity<List<SiteDTO>>(branchSiteDTOList) {
            };

            return ResponseBuilder.anOKResponse().withEntity(genericSiteDTOList).build();
        } else {
            return ResponseBuilder.notFound().withEntity(buildGenericError("Error when looking for SITE DTO's")).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getBranchSiteNamesIds")
    public Response getBranchSiteNamesIds(@QueryParam(CUSTOMER_ID) Long customerId) {
        if (AssertObject.anyEmpty(customerId)) {
            return ResponseBuilder.badRequest().build();
        }

        List<SiteMinDataDTO> branchSiteDTOList = null;
        try {
            long startTime = System.currentTimeMillis();
            branchSiteDTOList = cleanUpSiteData(siteResourceClient.getAllBranchSite(customerId));

            LOG.debug("Received response in {} ms", System.currentTimeMillis() - startTime);

        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }

        if (branchSiteDTOList != null) {
            GenericEntity<List<SiteMinDataDTO>> genericSiteMinDataDTOList = new GenericEntity<List<SiteMinDataDTO>>(branchSiteDTOList) {
            };

            return ResponseBuilder.anOKResponse().withEntity(genericSiteMinDataDTOList).build();
        } else {
            return ResponseBuilder.notFound().withEntity(buildGenericError("Error when looking for SITE DTO's")).build();
        }
    }

    @GET
    @Path("getBranchSiteCount")
    public Response getBranchSiteCount(@QueryParam(CUSTOMER_ID) Long customerId) {
        if (AssertObject.anyEmpty(customerId)) {
            return ResponseBuilder.badRequest().build();
        }

        Long count = siteResourceClient.getAllBranchSiteCount(customerId);

        GenericEntity<Long> entity = new GenericEntity<Long>(count) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @POST
    @Path("GPOPS")
    public Response findGPOPs(GPOPFilterDTO gpopFilterDTO) {
        if (AssertObject.anyEmpty(gpopFilterDTO)) {
            return ResponseBuilder.badRequest().build();
        }

        List<POPDetailDTO> gpopDetails = ppsrPOPDetailClient.filterGPOPs(gpopFilterDTO);

        GenericEntity<List<POPDetailDTO>> entity = new GenericEntity<List<POPDetailDTO>>(gpopDetails) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @GET
    @Path("APOPS")
    public Response getAPOPs() {

        List<POPDetailDTO> apopDetails = ppsrPOPDetailClient.getAPOPDetails();

        GenericEntity<List<POPDetailDTO>> entity = new GenericEntity<List<POPDetailDTO>>(apopDetails) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    private List<SiteDTO> flattenSiteDto(List<SiteDTO> siteDTOs) {
        if (siteDTOs == null) {
            return null;
        }
        List<SiteDTO> retSiteDTOList = new ArrayList<SiteDTO>();
        for (SiteDTO site : siteDTOs) {
            List<LocationDTO> locationDTOs = site.getLocations();

            try {
                SiteDTO siteClone = null;
                if (locationDTOs != null) {

                    for (LocationDTO locationDTO : locationDTOs) {
                        siteClone = site.clone();
                        siteClone.setLocationId(locationDTO.getLocationId());
                        siteClone.setFloor(locationDTO.getFloor());
                        siteClone.setRoom(locationDTO.getRoom());
                        siteClone.setSubPremises(locationDTO.getSubPremises());
                        siteClone.setLocations(null);
                        retSiteDTOList.add(siteClone);
                    }
                } else {
                    retSiteDTOList.add(site);
                }

            } catch (CloneNotSupportedException e) {
                LOG.warn("SiteResourceHandler :: Couldn't Clone SiteDTO ", e);
            }
        }

        return retSiteDTOList;
    }

    private List<SiteMinDataDTO> cleanUpSiteData(List<SiteDTO> siteDTOs) {
        if (siteDTOs == null) {
            return null;
        }
        List<SiteMinDataDTO> siteMinDataDTOList = new ArrayList<SiteMinDataDTO>();
        SiteMinDataDTO siteClone = null;
        for (SiteDTO site : siteDTOs) {
            siteClone = new SiteMinDataDTO();
            siteClone.setSiteId(site.getSiteId());
            siteClone.setSiteName(site.getName());
            siteMinDataDTOList.add(siteClone);
        }

        return siteMinDataDTOList;
    }

    @GET
    @Path("/getSiteRegions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSiteRegions(@QueryParam(CUSTOMER_ID) String customerId, @QueryParam("countryCode") String countryCode) {
        if (AssertObject.anyEmpty(customerId) || AssertObject.anyEmpty(countryCode)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid Customer Id/Country Code")).build();
        }

        try {
            List<SiteRegionDTO> siteRegionDTOList = siteResourceClient.getSiteRegions(customerId.trim(), countryCode.trim());
            return ResponseBuilder.anOKResponse().withEntity(siteRegionDTOList).build();
        } catch (RestException e) {
            throw e;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }
    }

    @POST
    @Path("/createSiteRegion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSiteRegion(@HeaderParam("SM_USER") String userId, SiteRegionDTO siteRegionDTO) {
        if (AssertObject.anyEmpty(userId, siteRegionDTO, siteRegionDTO.getCustomerId(), siteRegionDTO.getRegion(), siteRegionDTO.getIsoCountryName())) {
            String msg;
            if (siteRegionDTO != null) {
                msg = String.format("Invalid Input!! UserId =%s, Customer Id =%s, Region =%s, IsoCountryName =%s", userId, siteRegionDTO.getCustomerId(), siteRegionDTO.getRegion(), siteRegionDTO.getIsoCountryName());
            } else {
                msg = String.format("Invalid Input!! UserId =%s, SiteRegionDTO =%s", userId, siteRegionDTO);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(msg)).build();
        }

        try {
            siteResourceClient.createSiteRegion(userId, siteRegionDTO);
            return ResponseBuilder.anOKResponse().build();
        } catch (RestException e) {
            throw e;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }
    }

    private void createRegion(String userId, SiteUpdateDTO siteUpdateDTO) {
        if (siteUpdateDTO.isCreateNewRegion() && !AssertObject.isEmpty(siteUpdateDTO.getSitCusRegion()) && !Constants.REGION_NOT_APPLICABLE.equalsIgnoreCase(siteUpdateDTO.getSitCusRegion())) {
            /*Insert New Entry to Region Lookup Table*/
            SiteRegionDTO siteRegionDTO = new SiteRegionDTO();
            siteRegionDTO.setCustomerId(siteUpdateDTO.getCustomerId());
            siteRegionDTO.setCustomerName(siteUpdateDTO.getCustomerName());
            siteRegionDTO.setIsoCountryName(siteUpdateDTO.getAdrCountry());
            siteRegionDTO.setIsoCountryCode(siteResourceClient.getCountryIsoCode(siteUpdateDTO.getAdrCountry()));
            siteRegionDTO.setRegion(siteUpdateDTO.getSitCusRegion());
            createSiteRegion(userId, siteRegionDTO);
        }
    }


    private void updateCustomer(String userId, SiteUpdateDTO siteUpdateDTO, CustomerDTO customerDto) throws CustomerNotFoundException, RemedySysLogNotFoundException, NonUniqueRemedyLogException {

        if (customerDto != null && customerDto.getCustValidStatus() != null && !customerDto.getCustValidStatus().equals(siteUpdateDTO.getCustValidStatus())) {
            setCustInfo(customerDto, siteUpdateDTO.getUserId());
            customerDto.setCustValidStatus(siteUpdateDTO.getCustValidStatus());
            customerResource.updateCustomer(userId, customerDto);
        }

    }


    private void setCustInfo(CustomerDTO updateDTO, String userId) {
        updateDTO.setUserId(userId);
        updateDTO.setUniqueId(new StringBuilder().append(userId).append(new Date()).toString());
        updateDTO.setModifiedDate(new java.sql.Date(new Date().getTime()));
    }


    private String getSubPremiseValue(SiteLocationDTO siteLocationDTO) {
        String subPremise = SEMI_COLON_CONSTANT;
        try {
            String roomNo = siteLocationDTO.getLocRoom();
            String floorNo = siteLocationDTO.getLocFloor();
            if (null != roomNo && null != floorNo && roomNo.trim().length() > 0 && floorNo.trim().length() > 0) {
                subPremise = roomNo.toUpperCase() + subPremise + floorNo.toUpperCase();
            } else if (null != roomNo && roomNo.trim().length() > 0) {
                subPremise = roomNo.toUpperCase();
            } else if (null != floorNo && floorNo.trim().length() > 0) {
                subPremise = floorNo.toUpperCase();
            }
            return subPremise;
        } catch (Exception e) {
            return subPremise;
        }
    }


}
