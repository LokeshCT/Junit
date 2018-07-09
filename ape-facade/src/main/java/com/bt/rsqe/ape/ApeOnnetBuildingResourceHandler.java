package com.bt.rsqe.ape;

import com.bt.rsqe.ape.client.ApeOnNetBuildingClient;
import com.bt.rsqe.ape.config.RedirectUriConfig;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.dto.EFMAddressDTO;
import com.bt.rsqe.ape.dto.OnnetAvailabilityStatus;
import com.bt.rsqe.ape.dto.OnnetBuildingAvailabilityPerSiteDTO;
import com.bt.rsqe.ape.dto.OnnetBuildingDTO;
import com.bt.rsqe.ape.dto.OnnetBuildingsPerSiteWithEFMDTO;
import com.bt.rsqe.ape.dto.OnnetBuildingsWithEFMAddressDTOForAcess;
import com.bt.rsqe.ape.dto.AddressInfo;
import com.bt.rsqe.ape.dto.OnnetBuildingForAccess;
import com.bt.rsqe.ape.dto.OnnetCheckRequestDTO;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.onnet.EFMAddress;
import com.bt.rsqe.ape.onnet.OnnetBuilding;
import com.bt.rsqe.ape.onnet.OnnetBuildingAvailabilityPerSite;
import com.bt.rsqe.ape.onnet.OnnetBuildingsPerSite;
import com.bt.rsqe.ape.onnet.OnnetBuildingsPerSiteWithEFM;
import com.bt.rsqe.ape.onnet.SiteDetails;
import com.bt.rsqe.ape.repository.entities.EFMAddressEntity;
import com.bt.rsqe.ape.repository.entities.OnnetAvailabilityEntity;
import com.bt.rsqe.ape.repository.entities.OnnetBuildingEntity;
import com.bt.rsqe.ape.repository.entities.OnnetBuildingsWithEFMEntity;
import com.bt.rsqe.ape.source.OnnetDetailsOrchestrator;
import com.bt.rsqe.ape.source.processor.RequestBuilder;
import com.bt.rsqe.ape.source.processor.SupplierCheckRequestProcessor;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.Icon;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static com.bt.rsqe.ape.dto.OnnetAvailabilityType.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Path("/rsqe/ape-facade/on-net/")
public class ApeOnnetBuildingResourceHandler {

    private final ApeOnNetBuildingClient apeOnNetBuildingClient;
    private SupplierCheckRequestProcessor requestProcessor;
    private CustomerResource customerResource;
    private SupplierCheckConfig config;
    ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient;
    private OnnetDetailsOrchestrator orchestrator;

    private ApeOnNetBuildingResourceHandlerLogger logger = LogFactory.createDefaultLogger(ApeOnNetBuildingResourceHandlerLogger.class);

    public ApeOnnetBuildingResourceHandler(ApeOnNetBuildingClient apeOnNetBuildingClient,
                                           SupplierCheckConfig config,
                                           CustomerResource customerResource,
                                           RequestBuilder requestBuilder,
                                           ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient, OnnetDetailsOrchestrator orchestrator) {
        this.config = config;
        this.customerResource = customerResource;
        this.apeOnNetBuildingClient = apeOnNetBuildingClient;
        this.apeOnnetBuildingResourceHandlerClient = apeOnnetBuildingResourceHandlerClient;
        this.requestProcessor = new SupplierCheckRequestProcessor(config, customerResource, requestBuilder, apeOnnetBuildingResourceHandlerClient, orchestrator);
        this.orchestrator = orchestrator;
    }

    @PUT
    @Path("buildings")
    public Response getOnNetBuildings(OnnetCheckRequestDTO dto) {
        logger.getOnNetBuildings(dto);

        OnnetBuildingsPerSite[] onNetBuildings = apeOnNetBuildingClient.getOnNetBuildings(fromDto(dto));

        logger.getOnNetBuildings(onNetBuildings);

        GenericEntity<List<OnnetBuildingDTO>> stubEntity = new GenericEntity<List<OnnetBuildingDTO>>(toDto(onNetBuildings[0])) {
        };

        return ResponseBuilder.anOKResponse().withEntity(stubEntity).build();
    }

    //To retrieve the both Onnetbuidlings & listOfEFMAddress in Singe request
    @PUT
    @Path("onNet-buildings-with-EFMAddress")
    public Response getOnNetBuildingsWithEFMAddress(OnnetCheckRequestDTO dto) {

        logger.getOnNetBuildingsWithEFMAddress(dto);

        GenericEntity<OnnetBuildingsPerSiteWithEFMDTO> stubEntity;
        List<OnnetBuildingDTO> onnetBuildingDTOList;
        List<EFMAddressDTO> efmAddressDTOList;

        OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity = orchestrator.getOnnetBuildingsWithEFMEntity(dto.getSiteId());

        logger.getOnNetBuildingsWithEFMAddress(dto.getSiteId(),onnetBuildingsWithEFMEntity);

        if (onnetBuildingsWithEFMEntity != null) {
            onnetBuildingDTOList = toOnnetBuildingDTOFromEntity(onnetBuildingsWithEFMEntity.getOnnetBuildingEntityList());
            efmAddressDTOList = toEFMAddressDTOFromEntity(onnetBuildingsWithEFMEntity.getEfmAddressEntityList());

            stubEntity = new GenericEntity<OnnetBuildingsPerSiteWithEFMDTO>(new OnnetBuildingsPerSiteWithEFMDTO(dto.getSiteId(), onnetBuildingDTOList, efmAddressDTOList)) {
            };
        } else {

            OnnetBuildingsPerSiteWithEFM[] onNetBuildingsEFM = apeOnNetBuildingClient.getOnNetBuildingsEFM(fromDto(dto));

            onnetBuildingDTOList = toDto(onNetBuildingsEFM[0]);
            efmAddressDTOList = toEFMAddressDTO(onNetBuildingsEFM[0]);

            orchestrator.storeOnnetBuildingsPerSiteWithEFMRespone(dto.getSiteId(), onnetBuildingDTOList, efmAddressDTOList);

            logger.getOnNetBuildingsWithEFMAddress(dto.getSiteId(), onnetBuildingDTOList, efmAddressDTOList);

            stubEntity = new GenericEntity<OnnetBuildingsPerSiteWithEFMDTO>(new OnnetBuildingsPerSiteWithEFMDTO(dto.getSiteId(), onnetBuildingDTOList, efmAddressDTOList)) {
            };
        }


        return ResponseBuilder.anOKResponse().withEntity(stubEntity).build();
    }

    // To retrieve the Onnetbuidlings alone
    @PUT
    @Path("onNet-buildings-EFM")
    public Response getOnNetBuildingsEFM(OnnetCheckRequestDTO dto) {

        OnnetBuildingsPerSiteWithEFM[] onNetBuildingsEFM = apeOnNetBuildingClient.getOnNetBuildingsEFM(fromDto(dto));

        GenericEntity<List<OnnetBuildingDTO>> stubEntity = new GenericEntity<List<OnnetBuildingDTO>>(toDto(onNetBuildingsEFM[0])) {
        };

        return ResponseBuilder.anOKResponse().withEntity(stubEntity).build();
    }

    // To retrieve the listOfEFMAddress alone
    @PUT
    @Path("onNet-buildings-EFMAddress")
    public Response getEFMAddress(OnnetCheckRequestDTO dto) {

        OnnetBuildingsPerSiteWithEFM[] onNetBuildingsEFM = apeOnNetBuildingClient.getOnNetBuildingsEFM(fromDto(dto));

        GenericEntity<List<EFMAddressDTO>> stubEntity = new GenericEntity<List<EFMAddressDTO>>(toEFMAddressDTO(onNetBuildingsEFM[0])) {
        };

        return ResponseBuilder.anOKResponse().withEntity(stubEntity).build();
    }

    @PUT
    @Path("onNetAvailability")
    public Response getOnnetBuildingsStatusPerSite(List<OnnetCheckRequestDTO> dtoList) {
        logger.getOnnetBuildingsStatusPerSite(dtoList);

        OnnetBuildingAvailabilityPerSite[] onnetBuildingAvailabilityPerSites = apeOnNetBuildingClient.getOnnetBuildingsStatusPerSite(fromDto(dtoList));

        logger.getOnnetBuildingsStatusPerSite(onnetBuildingAvailabilityPerSites);
        GenericEntity<List<OnnetBuildingAvailabilityPerSiteDTO>> stubEntity = new GenericEntity<List<OnnetBuildingAvailabilityPerSiteDTO>>(toDto(Arrays.asList(onnetBuildingAvailabilityPerSites))) {
        };

        return ResponseBuilder.anOKResponse().withEntity(stubEntity).build();
    }

    // SQE will use this service
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get-onNet-buildings")
    public Response getOnNetBuildings(@QueryParam("siteId") String siteId) {

        if (AssertObject.isEmpty(siteId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("siteId should not be null").build();
        }
        GenericEntity<OnnetBuildingsWithEFMAddressDTOForAcess> stubEntity = null;
        OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity = orchestrator.getOnnetBuildingsWithEFMEntity(siteId);
        List<OnnetBuildingForAccess> onnetBuildings;
        List<AddressInfo> addressInfos;

        logger.getOnNetBuildingsWithEFMAddress(siteId, onnetBuildingsWithEFMEntity);

        if (onnetBuildingsWithEFMEntity != null) {
            onnetBuildings = toOnnetBuildingForAccessFromEntity(onnetBuildingsWithEFMEntity.getOnnetBuildingEntityList());
            addressInfos = toAddressInfoFromEntity(onnetBuildingsWithEFMEntity.getEfmAddressEntityList());

            stubEntity = new GenericEntity<OnnetBuildingsWithEFMAddressDTOForAcess>(new OnnetBuildingsWithEFMAddressDTOForAcess(siteId, onnetBuildings, addressInfos)) {
            };
        }

        return ResponseBuilder.anOKResponse().withEntity(stubEntity).build();

    }

    //SQE will use this Service
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get-onNet-building-status")
    public Response getOnNetBuildingStatusForCustomer(@QueryParam("customerId") String customerId,
                                                      @QueryParam("quoteId") String quoteId) {
        logger.getOnNetBuildingStatusForCustomer(customerId, quoteId);
        if (AssertObject.isEmpty(customerId) || AssertObject.isEmpty(quoteId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid customerId or quoteId").build();
        }
        List<OnnetAvailabilityStatus> statuses = newArrayList();
        try {
            List<SiteDTO> availableSites = customerResource.siteResource(customerId).getBranchSites(quoteId);
            statuses = orchestrator.getOnNetAvailabilityEntityForAvailableSites(orchestrator.filterAvailableOnNetSites(availableSites, orchestrator.getExistingOnNetSites(Long.parseLong(customerId))));
/*            if (statuses.size() == 0) {
                return ResponseBuilder.anOKResponse().withEntity(new StatusResponse(ERROR, FAILED, "No OnNet supported sites exist for this customer", FAILURE_CODE)).build();

            }*/
        } catch (Exception e) {
            logger.error(e);
        }
        return Response.ok().entity(new GenericEntity<List<OnnetAvailabilityStatus>>(statuses) {
        }).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get-onNet-Building-availability")
    public Response getOnNetBuildingAvailability(@QueryParam("customerId") String customerId,
                                                 @QueryParam("siteId") String siteId,
                                                 @QueryParam("projectId") String projectId) throws Exception {
        logger.getOnNetBuildingAvailability(customerId,siteId);

        if (AssertObject.isEmpty(customerId) || AssertObject.isEmpty(siteId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid customerId or quoteId").build();
        }

        Icon icon = new Icon();
        String url = null;
        OnnetAvailabilityEntity onnetAvailabilityEntity = orchestrator.getOnNetAvailabilityEntity(Long.parseLong(siteId));
        if (onnetAvailabilityEntity != null) {
            icon.setId(orchestrator.getOnNetAvailabilityEntity(Long.parseLong(siteId)).getOnNetAvailabilityTypeId());
        } else {
            icon.setId(OrangeIcon.getId());
            requestProcessor.initiateOnNetAvailabilityRequest(prepareOnNetRequestData(siteId, customerId));
        }
        if (GreyIcon.getId().equals(icon.getId()) || GreenIcon.getId().equals(icon.getId()) || OrangeIcon.getId().equals(icon.getId())) {
            String uri = config.getRedirectUriConfig(RedirectUriConfig.ONNET_BUILDING_PAGE).getUri();
            url = String.format(uri, customerId, siteId);
        }

        icon.setRedirectUri(url);
        icon.setTitle(getAvailabilityTypeById(icon.getId()).getHelpText());

        logger.getOnNetBuildingAvailability(icon);
        return ResponseBuilder.anOKResponse().withEntity(icon).build();
    }

    @POST
    @Path("save-onNet-buildings-EFMAddress")
    public Response saveOnnetDetails(OnnetBuildingsPerSiteWithEFMDTO request) {

        logger.saveOnnetDetails(request);
        if (isNull(request)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build();
        }

        orchestrator.saveOnnetBuildingsWithEFMEntity(request);

        return ResponseBuilder.anOKResponse().build();
    }

    private SiteDetails fromDto(OnnetCheckRequestDTO onnetCheckRequestDTO) {
        return new SiteDetails(onnetCheckRequestDTO.getSiteId(), onnetCheckRequestDTO.getSqeBFGsiteID(),
                               -1,
                               onnetCheckRequestDTO.getAccuracyLevel(),
                               isEmpty(onnetCheckRequestDTO.getLatitude()) ? 0d : Double.parseDouble(onnetCheckRequestDTO.getLatitude()),
                               isEmpty(onnetCheckRequestDTO.getLongitude()) ? 0d : Double.parseDouble(onnetCheckRequestDTO.getLongitude()),
                               onnetCheckRequestDTO.getCountryName(), onnetCheckRequestDTO.getPostCode(), onnetCheckRequestDTO.getCity(), onnetCheckRequestDTO.getStreet(), onnetCheckRequestDTO.getPhoneNumber(), "", "");
    }

    private List<SiteDetails> fromDto(List<OnnetCheckRequestDTO> onnetCheckRequestDTOList) {
        return newArrayList(transform(newArrayList(onnetCheckRequestDTOList), new Function<OnnetCheckRequestDTO, SiteDetails>() {
            @Override
            public SiteDetails apply(OnnetCheckRequestDTO input) {
                return fromDto(input);
            }
        }));

    }


    private List<OnnetBuildingDTO> toDto(final OnnetBuildingsPerSiteWithEFM onnetBuildingsPerSiteWithEFM) {
        if (isNull(onnetBuildingsPerSiteWithEFM.getOnnetbuildings())) {
            return newArrayList();
        }

        return newArrayList(transform(newArrayList(onnetBuildingsPerSiteWithEFM.getOnnetbuildings()), new Function<OnnetBuilding, OnnetBuildingDTO>() {
            @Override
            public OnnetBuildingDTO apply(OnnetBuilding input) {
                return new OnnetBuildingDTO(input.getBuildingCode(), input.getAddress(), input.getStreetNumber(), input.getStreetName(), input.getPostCode(),
                                            input.getCity(), input.getState(), input.getCountry(), input.getKgi(), input.getAccurcay(), input.getLatitude(),
                                            input.getLongitude(), isNull(input.getFloors()) ? Lists.<String>newArrayList() : newArrayList(input.getFloors()),
                                            input.getDistance(), input.getGroupNumber(), null, toCommaSeparatedValues(isNull(input.getFloors()) ? Lists.<String>newArrayList() : newArrayList(input.getFloors())));
            }
        }));
    }

    private List<OnnetBuildingForAccess> toOnnetBuildingForAccessFromEntity(final List<OnnetBuildingEntity> onnetBuildingEntityList) {
        if (isNull(onnetBuildingEntityList)) {
            return newArrayList();
        }

        return newArrayList(transform(newArrayList(onnetBuildingEntityList), new Function<OnnetBuildingEntity, OnnetBuildingForAccess>() {
            @Override
            public OnnetBuildingForAccess apply(OnnetBuildingEntity input) {
                return input.toOnnetBuilding();
            }
        }));
    }


    private List<AddressInfo> toAddressInfoFromEntity(final List<EFMAddressEntity> efmAddressEntityList) {
        if (isNull(efmAddressEntityList)) {
            return newArrayList();
        }

        return newArrayList(transform(newArrayList(efmAddressEntityList), new Function<EFMAddressEntity, AddressInfo>() {
            @Override
            public AddressInfo apply(EFMAddressEntity input) {
                return input.toAddressInfo();
            }
        }));
    }

    private List<OnnetBuildingDTO> toOnnetBuildingDTOFromEntity(final List<OnnetBuildingEntity> onnetBuildingEntityList) {
        if (isNull(onnetBuildingEntityList)) {
            return newArrayList();
        }

        return newArrayList(transform(newArrayList(onnetBuildingEntityList), new Function<OnnetBuildingEntity, OnnetBuildingDTO>() {
            @Override
            public OnnetBuildingDTO apply(OnnetBuildingEntity input) {
                return input.toOnnetBuildingDTO();
            }
        }));
    }


    private List<EFMAddressDTO> toEFMAddressDTOFromEntity(final List<EFMAddressEntity> efmAddressEntityList) {
        if (isNull(efmAddressEntityList)) {
            return newArrayList();
        }

        return newArrayList(transform(newArrayList(efmAddressEntityList), new Function<EFMAddressEntity, EFMAddressDTO>() {
            @Override
            public EFMAddressDTO apply(EFMAddressEntity input) {
                return input.toEFMAddressDto();
            }
        }));
    }

    private List<EFMAddressDTO> toEFMAddressDTO(final OnnetBuildingsPerSiteWithEFM onnetBuildingsPerSiteWithEFM) {
        if (isNull(onnetBuildingsPerSiteWithEFM.getListOfAddress())) {
            return newArrayList();
        }

        return newArrayList(transform(newArrayList(Arrays.asList(onnetBuildingsPerSiteWithEFM.getListOfAddress())), new Function<EFMAddress, EFMAddressDTO>() {
            @Override
            public EFMAddressDTO apply(EFMAddress input) {
                return new EFMAddressDTO(input.getBritishAddress(), input.getAddressReference(), null);
            }
        }));
    }

    private List<OnnetBuildingDTO> toDto(final OnnetBuildingsPerSite onnetBuildingsPerSite) {
        if (isNull(onnetBuildingsPerSite.getOnnetbuildings())) {
            return newArrayList();
        }

        return newArrayList(transform(newArrayList(onnetBuildingsPerSite.getOnnetbuildings()), new Function<OnnetBuilding, OnnetBuildingDTO>() {
            @Override
            public OnnetBuildingDTO apply(OnnetBuilding input) {
                return new OnnetBuildingDTO(input.getBuildingCode(), input.getAddress(), input.getStreetNumber(), input.getStreetName(), input.getPostCode(),
                                            input.getCity(), input.getState(), input.getCountry(), input.getKgi(), input.getAccurcay(), input.getLatitude(),
                                            input.getLongitude(), isNull(input.getFloors()) ? Lists.<String>newArrayList() : newArrayList(input.getFloors()),
                                            input.getDistance(), input.getGroupNumber(), null, toCommaSeparatedValues(isNull(input.getFloors()) ? Lists.<String>newArrayList() : newArrayList(input.getFloors())));
            }
        }));
    }

    private List<OnnetBuildingAvailabilityPerSiteDTO> toDto(final List<OnnetBuildingAvailabilityPerSite> onnetBuildingAvailabilityPerSiteList) {

        return newArrayList(transform(newArrayList(onnetBuildingAvailabilityPerSiteList), new Function<OnnetBuildingAvailabilityPerSite, OnnetBuildingAvailabilityPerSiteDTO>() {
            @Override
            public OnnetBuildingAvailabilityPerSiteDTO apply(OnnetBuildingAvailabilityPerSite input) {
                return new OnnetBuildingAvailabilityPerSiteDTO(input.getSiteID(), input.getOnnetAvailable());
            }
        }));
    }


    private SupplierCheckRequest prepareOnNetRequestData(String siteId, String customerId) throws Exception {

        List<OnnetAvailabilityEntity> onnetAvailabilityEntityList = newArrayList();
        List<SupplierSite> supplierSiteList = newArrayList(new SupplierSite(Long.parseLong(siteId)));

        onnetAvailabilityEntityList.add(new OnnetAvailabilityEntity(Long.parseLong(siteId),
                                                                    Long.parseLong(customerId),
                                                                    null,
                                                                    null,
                                                                    null,
                                                                    null,
                                                                    null,
                                                                    null,
                                                                    InProgress.value(),
                                                                    OrangeIcon.getId(),
                                                                    null));

        orchestrator.storeOnNetAvailabilityList(onnetAvailabilityEntityList);

        return new SupplierCheckRequest(supplierSiteList, customerId);
    }


    private String toCommaSeparatedValues(List<String> toBeGrouped) {
        StringBuffer group = new StringBuffer(" ");
        for (String value : toBeGrouped) {
            if (!isNullOrEmpty(value)) {
                group.append(value).append(",");
            }
        }
        return group.toString().substring(0, group.toString().length() - 1).trim();
    }

    private interface ApeOnNetBuildingResourceHandlerLogger {

        @Log(level = LogLevel.INFO, format = "Entering into  getOnNetBuildings ...OnnetCheckRequestDTO is  '%s'")
        void getOnNetBuildings(OnnetCheckRequestDTO dto);

        @Log(level = LogLevel.INFO, format = "Response from APE for onNetBuildings ....onNetBuildings is  '%s'")
        void getOnNetBuildings(OnnetBuildingsPerSite[] onNetBuildings);

        @Log(level = LogLevel.ERROR, format = "getOnNetBuildingStatusForCustomer : '%s'")
        void error(Exception error);

        @Log(level = LogLevel.INFO, format = "Entering into  getOnNetBuildingsWithEFMAddress ...OnnetCheckRequestDTO is  '%s'")
        void getOnNetBuildingsWithEFMAddress(OnnetCheckRequestDTO dto);

        @Log(level = LogLevel.INFO, format = "Result  onnetBuildingsWithEFMEntity from DB for site id '%s' is...==>  '%s'")
        void getOnNetBuildingsWithEFMAddress(String siteId,OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity);

        @Log(level = LogLevel.INFO, format = " Onnet details are not available in DB and onnetBuildingsWithEFMEntity is null...Inside Else loop.. for site id ==> '%s' ... onnetBuildingDTOList==>  '%s'...efmAddressDTOList==>'%s'")
        void getOnNetBuildingsWithEFMAddress(String siteId, List<OnnetBuildingDTO> onnetBuildingDTOList, List<EFMAddressDTO> efmAddressDTOList);

        @Log(level = LogLevel.INFO, format = "Entering into  getOnnetBuildingsStatusPerSite dto is...==>  '%s'")
        void getOnnetBuildingsStatusPerSite(List<OnnetCheckRequestDTO> dtoList);

        @Log(level = LogLevel.INFO, format = "response from ape for  getOnnetBuildingsStatusPerSite  is...==>  '%s'")
        void getOnnetBuildingsStatusPerSite(OnnetBuildingAvailabilityPerSite[] onnetBuildingAvailabilityPerSites);

        @Log(level = LogLevel.INFO, format = "Entering into  getOnNetBuildingStatusForCustomer customerId is...==>  '%s' ... quoteId is ==>'%s'")
        void getOnNetBuildingStatusForCustomer(String customerId, String quoteId);

        @Log(level = LogLevel.INFO, format = "Entering into  getOnNetBuildingStatusForCustomer customerId is...==>  '%s' ... siteId is ==>'%s'")
        void getOnNetBuildingAvailability(String customerId, String siteId);

        @Log(level = LogLevel.INFO, format = "End of getOnNetBuildingAvailability.. Icon values are...==>  '%s'")
        void getOnNetBuildingAvailability(Icon icon);

        @Log(level = LogLevel.INFO, format = "Entering into  OnnetBuildingsPerSiteWithEFMDTO is...==>  '%s'")
        void saveOnnetDetails(OnnetBuildingsPerSiteWithEFMDTO request);


    }

}
