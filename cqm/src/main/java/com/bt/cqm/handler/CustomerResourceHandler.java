package com.bt.cqm.handler;

import com.bt.cqm.dto.CreateCustomerResponseDTO;
import com.bt.cqm.exception.DuplicateCustomerException;
import com.bt.cqm.model.CustomerListModel;
import com.bt.rsqe.customerinventory.client.resource.ContractResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.ClientGroupDTO;
import com.bt.rsqe.customerinventory.dto.SiteLocationDTO;
import com.bt.rsqe.customerinventory.dto.contract.ContractDTO;

import com.bt.rsqe.customerinventory.dto.contract.PortDistributorDTO;
import com.bt.rsqe.customerinventory.dto.customer.CustomerAddDetailDTO;
import com.bt.rsqe.customerinventory.dto.customer.CustomerDTO;
import com.bt.rsqe.customerinventory.dto.le.CusLeDTO;
import com.bt.rsqe.customerinventory.dto.site.CountryDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteUpdateDTO;
import com.bt.rsqe.customerinventory.pagination.PageAttribute;
import com.bt.rsqe.customerinventory.pagination.QueryResult;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.customerinventory.resources.SiteLocationResource;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.customerinventory.utils.Utility;
import com.bt.rsqe.customerrecord.ExpedioSalesChannelDto;
import com.bt.rsqe.domain.AttachmentDTO;
import com.bt.rsqe.domain.product.Identifier;
import com.bt.rsqe.emppal.attachmentresource.EmpPalResource;
import com.bt.rsqe.expedio.services.ActivityResource;
import com.bt.rsqe.expedio.services.MNCCustomersDTO;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.bt.rsqe.web.rest.exception.RestException;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bt.rsqe.expedio.services.MNCCustDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bt.cqm.utils.Utility.buildGenericError;
import static java.lang.String.format;

@Path("/cqm/customer")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResourceHandler {
    private static final String CUSTOMER_NAME = "customerName";
    private static final String SALES_CHANNEL = "salesChannel";
    private static final String START_INDEX = "startIndex";
    private static final String PAGE_SIZE = "pageSize";
    private CustomerResource customerResource;
    private com.bt.rsqe.customerrecord.CustomerResource expedioCustomerResource;
    private final SiteResourceClient siteResourceClient;
    private SiteLocationResource siteLocationResource;
    private final ContractResourceClient contractResource;
    private ActivityResource activityResource;
    private EmpPalResource empPalResource;
    private final Gson gson;
    private static final String EXCEPTION_MESSAGE = " Error while processing. ";
    private static Logger LOGGER = LoggerFactory.getLogger(CustomerResourceHandler.class);

    public CustomerResourceHandler(com.bt.rsqe.customerrecord.CustomerResource expedioCustomerResource, CustomerResource customerResource, SiteResourceClient siteResourceClient, SiteLocationResource siteLocationResource, ContractResourceClient contractResource,ActivityResource activityResource,EmpPalResource empPalResource) {
        this.customerResource = customerResource;
        this.expedioCustomerResource = expedioCustomerResource;
        this.siteResourceClient = siteResourceClient;
        this.siteLocationResource = siteLocationResource;
        this.contractResource = contractResource;
        this.activityResource = activityResource;
        this.empPalResource = empPalResource;
        this.gson = new Gson();
    }

    @POST
    public Response createCustomer(@HeaderParam("SM_USER") String userId, CustomerDTO customerDTO, @QueryParam("contractCeaseTerm") Long contractCeaseTerm,@QueryParam("contractLinkedCeaseTerm") Long contractLinkedCeaseTerm,@QueryParam("contractFriendlyName") String contractFriendlyName,@HeaderParam("IS_MNC") String isMnc) throws Exception {
        if (AssertObject.anyEmpty(userId, customerDTO) || AssertObject.anyEmpty(customerDTO.getCusName())) {
            return ResponseBuilder.badRequest().build();
        }

        long startTime = System.currentTimeMillis();
        customerDTO.setCusReference(userId + new Date());
        CustomerDTO respCustomerDto = customerResource.createCustomer(userId, customerDTO);

        if ("0".equals(respCustomerDto.getCusId())) {
            throw new DuplicateCustomerException("Customer with same name exists!");
        }

        // Create default central site for newly created customer
        Long siteId = createDefaultCentralSite(userId, customerDTO.getCusName(), Long.valueOf(respCustomerDto.getCusId()), respCustomerDto.getCusReference());

        // Create Default Location for newly created customer
        createDefaultCentralSiteLocation(userId, Long.valueOf(siteId));

        // Create default customer contract for newly created customer
        String conManagedSolFlag = null;
        if("Y".equalsIgnoreCase(isMnc)){
            conManagedSolFlag="Y";
        }
        ContractDTO contractDTO = createDefaultCustomerContract(userId, Long.valueOf(respCustomerDto.getCusId()), siteId, customerDTO.getSalesChannel(), contractFriendlyName,conManagedSolFlag,contractCeaseTerm,contractLinkedCeaseTerm);
        respCustomerDto.setContractDTO(contractDTO);

        try{
            String sharePointFldr = com.bt.cqm.utils.Utility.buildSharePointFolderStructure(customerDTO.getSalesChannel(), respCustomerDto.getCusId().toString());
            LOGGER.info("Going to create Share Point folder for new Customer. Path :: "+sharePointFldr);

            empPalResource.createFolderWithPath(new AttachmentDTO(sharePointFldr,"Sales"));
            empPalResource.createFolder(new AttachmentDTO(sharePointFldr,"Service Assurance"));
            empPalResource.createFolder(new AttachmentDTO(sharePointFldr,"Service Delivery"));
            empPalResource.createFolder(new AttachmentDTO(sharePointFldr,"Bid Manager"));
            empPalResource.createFolder(new AttachmentDTO(sharePointFldr+"/Service Assurance",siteId.toString()));

        }catch(Exception ex){
           LOGGER.warn("Couldn't create Sharepoint folder .",ex);
        }

        LOGGER.debug("Time Taken: {}", System.currentTimeMillis() - startTime);

        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<CustomerDTO>(respCustomerDto) {
        }).build();
    }

    @GET
    @Path("getCustomerDetail")
    public Response getCustomerDetail(@QueryParam("customerId") String customerId) {
        if (AssertObject.isEmpty(customerId)) {
            return ResponseBuilder.badRequest().build();
        }
        CustomerDTO customerDTO = customerResource.getCustomer(customerId);

        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<CustomerDTO>(customerDTO) {
        }).build();
    }

    @POST
    @Path("associate")
    public Response associateCustomer(CustomerDTO customerDTO) {
        if (customerDTO == null) {
            return ResponseBuilder.badRequest().build();
        }

        CreateCustomerResponseDTO responseDTO = new CreateCustomerResponseDTO();
        responseDTO.setCustomerId(customerDTO.getCusId().toString());
        responseDTO.setCustomerName(customerDTO.getCusName());
        responseDTO.setCustomerReference(customerDTO.getCusReference());

        long startTime = System.currentTimeMillis();
        Long siteId = createDefaultCentralSite(customerDTO.getUserId(), customerDTO.getCusName(), customerDTO.getCusId(), customerDTO.getCusReference());
        createDefaultCentralSiteLocation(customerDTO.getUserId(), Long.valueOf(siteId));
        String conManagedSolFlag = null;
        if(Constants.MNC_CONTRACT_NAME.equalsIgnoreCase(customerDTO.getSalesChannel())){
            conManagedSolFlag="Y";
        }
        ContractDTO contractDTO = createDefaultCustomerContract(customerDTO.getUserId(), customerDTO.getCusId(), siteId, customerDTO.getSalesChannel(), null,conManagedSolFlag,null,null);
        responseDTO.setContractDTO(contractDTO);

        LOGGER.debug("Time Taken: {}", System.currentTimeMillis() - startTime);

        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<CreateCustomerResponseDTO>(responseDTO) {
        }).build();

    }

    @GET
    @Path("new")
    public Response createNewCustomer(@QueryParam(CUSTOMER_NAME) String customerName, @QueryParam("pageIndex") Integer pageIndex, @QueryParam(PAGE_SIZE) Integer pageSize) {
        CreateCustomerResponseDTO responseDTO = new CreateCustomerResponseDTO();
        responseDTO.setCustomerName(customerName);

        if (Strings.isNullOrEmpty(customerName)) {
            responseDTO.setResponseCode(CustomerCreationResponse.NO_CUSTOMER_NAME_PROVIDED.getCode());
            responseDTO.setMessage(CustomerCreationResponse.NO_CUSTOMER_NAME_PROVIDED.getMessage());
            return ResponseBuilder.badRequest().withEntity(new GenericEntity<CreateCustomerResponseDTO>(responseDTO) {
            }).build();
        }

        CustomerDTO customerDTO = customerResource.findCustomer(customerName);

        if (null != customerDTO) {
            responseDTO.setResponseCode(CustomerCreationResponse.CUSTOMER_ALREADY_EXISTS.getCode());
            responseDTO.setMessage(CustomerCreationResponse.CUSTOMER_ALREADY_EXISTS.getMessage());
            responseDTO.setData(customerDTO);
        } else {
            //PageAttribute pageAttribute = new PageAttribute(pageIndex, pageSize);
            Long similarCustomerCount = customerResource.similarCustomerCount(customerName);
            responseDTO.setTotalDataCount(similarCustomerCount);

            if (similarCustomerCount.intValue() == 0) {
                responseDTO.setResponseCode(CustomerCreationResponse.NO_MATCHING_OR_SIMILAR_CUSTOMERS.getCode());
                responseDTO.setMessage(CustomerCreationResponse.NO_MATCHING_OR_SIMILAR_CUSTOMERS.getMessage());
            } else {
                List<CustomerDTO> customerDTOList = customerResource.findSimilarCustomers(customerName, pageIndex, pageSize);
                responseDTO.setResponseCode(CustomerCreationResponse.SIMILAR_CUSTOMERS_FOUND.getCode());
                responseDTO.setMessage(CustomerCreationResponse.SIMILAR_CUSTOMERS_FOUND.getMessage());
                responseDTO.setData(customerDTOList);
            }
        }

        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<CreateCustomerResponseDTO>(responseDTO) {
        }).build();
    }


    @GET
    @Path("/similarCustomersList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showCustomersListScreen(@QueryParam(CUSTOMER_NAME) String customerName, @QueryParam(START_INDEX) Integer pageIndex, @QueryParam(PAGE_SIZE) Integer pageSize) {
        if (Strings.isNullOrEmpty(customerName)) {
            return ResponseBuilder.badRequest().build();
        }

        try {
            List<CustomerDTO> customerDTOList = customerResource.findSimilarCustomers(customerName, pageIndex, pageSize);

            GenericEntity<List<CustomerDTO>> genericEntityCustomerDTOList = new GenericEntity<List<CustomerDTO>>(customerDTOList) {
            };
            return ResponseBuilder.anOKResponse().withEntity(genericEntityCustomerDTOList).build();
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/customersList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showCustomersListScreen(@QueryParam(SALES_CHANNEL) String salesChannel,
                                            @QueryParam(CUSTOMER_NAME) String customerName,
                                            @QueryParam(START_INDEX) String pageIndex,
                                            @QueryParam(PAGE_SIZE) String pageSize,
                                            @QueryParam("sortColumn") String sortColumn,
                                            @QueryParam("sortOrder") String sortOrder) {

        if (salesChannel == null || customerName == null) {
            return ResponseBuilder.notFound().build();
        }

        PageAttribute pAttrb = null;
        try {
            if (pageIndex != null && pageSize != null) {
                pAttrb = new PageAttribute(Integer.parseInt(pageIndex), Integer.parseInt(pageSize));
                pAttrb.setOrderBy(sortColumn, PageAttribute.getOrder(sortOrder));
            }
        } catch (Exception ex) {

        }

        try {
            QueryResult<CustomerDTO> queryResult = this.customerResource.getCustomers(salesChannel, customerName, null, pAttrb);

            List<CustomerDTO> custDtoList = null;
            String errorMsg = "";
            if (queryResult != null && queryResult.getItemWrapper() != null) {
                custDtoList = queryResult.getItemWrapper().getItems();
            } else {
                errorMsg = "Customer Not Found";
                return ResponseBuilder.notFound().withEntity(buildGenericError(errorMsg)).build();
            }

            CustomerListModel model = new CustomerListModel(false, customerName, salesChannel, errorMsg, queryResult);

            GenericEntity<CustomerListModel> customerDTOs = new GenericEntity<CustomerListModel>(model) {
            };

            return ResponseBuilder.anOKResponse().withEntity(customerDTOs).build();
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }


    }

    @GET
    @Path("identifiers")
    public Response getCustomerForChannel(@QueryParam(SALES_CHANNEL) String salesChannel,@HeaderParam("BOAT_ID") String boatId) {

        if (AssertObject.anyEmpty(salesChannel, boatId)) {
            return ResponseBuilder.badRequest().build();
        }
        JsonObject jsonObject;
        JsonArray customers = new JsonArray();
        List<Identifier> availableCustomers =null;
        if(Constants.MNC_CONTRACT_NAME.equalsIgnoreCase(salesChannel)){
            MNCCustomersDTO mncCusts = activityResource.getMncCustomers(boatId);

            if(mncCusts!=null){
                List<MNCCustDTO> mncCustList =  mncCusts.getCustomers();

                if(mncCustList!=null){
                    availableCustomers = new ArrayList<Identifier>();
                    for(MNCCustDTO mncCustDTO :mncCustList){
                        availableCustomers.add(new Identifier(mncCustDTO.getCustomerID(),mncCustDTO.getCustomerName()));
                    }
                }
            }

        }else{
            availableCustomers = customerResource.getAvailableCustomer(salesChannel);
        }

        for (Identifier availableCustomer : availableCustomers) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("cusId", availableCustomer.getId());
            jsonObject.addProperty("cusName", availableCustomer.getName());
            customers.add(jsonObject);
        }
        return ResponseBuilder.anOKResponse().withEntity(gson.toJson(customers)).build();
    }

    @GET
    @Path(SALES_CHANNEL)
    public Response searchCustomerForChannel(@QueryParam(SALES_CHANNEL) String salesChannel) {
        LOGGER.trace("Entering searchCustomerForChannel");
        if (salesChannel == null) {
            return ResponseBuilder.notFound().build();
        }
        QueryResult customerDTOList;
        try {
            long startTime = System.currentTimeMillis();
            customerDTOList = customerResource.getCustomers(salesChannel, null, null, null);
            LOGGER.debug("Received response in {} ms", System.currentTimeMillis() - startTime);
        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }

        GenericEntity<QueryResult> entity = new GenericEntity<QueryResult>(customerDTOList) {
        };
        LOGGER.trace("Exiting searchCustomerForChannel");
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @GET
    public Response searchCustomerForName(@QueryParam(SALES_CHANNEL) String salesChannel, @QueryParam(CUSTOMER_NAME) String customerName) {
        if (customerName == null) {
            return ResponseBuilder.badRequest().build();
        }

        QueryResult customerDTOList;
        try {
            long startTime = System.currentTimeMillis();
            customerDTOList = customerResource.getCustomers(salesChannel, customerName, null, null);
            LOGGER.debug("Received response in {} ms", System.currentTimeMillis() - startTime);
        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }

        GenericEntity<QueryResult> entity = new GenericEntity<QueryResult>(customerDTOList) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @GET
    @Path("contract")
    public Response getContractsForCustomer(@QueryParam(SALES_CHANNEL) String salesChannel, @QueryParam("customerId") Long customerId) {
        LOGGER.trace("Entering getContractsForCustomer");
        if (AssertObject.anyEmpty(salesChannel,customerId)) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid salesChannel/customerId .")).build();
        }
        List<ContractDTO> contractDTOList;
        try {
            long startTime = System.currentTimeMillis();
            contractDTOList = contractResource.getContracts(salesChannel, customerId);

            if (contractDTOList == null || contractDTOList.size() < 1) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            LOGGER.debug("Received response in {} ms", System.currentTimeMillis() - startTime);
        } catch (RestException e) {
            LOGGER.debug("Error occurred: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            LOGGER.debug("Error occurred: {}", ex.getMessage());
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }

/*        JsonArray jsonArray = new JsonArray();
        for (ContractDTO contractDTO : contractDTOList) {
            jsonArray.add(gson.toJsonTree(contractDTO));
        }*/

        LOGGER.trace("Exiting getContractsForCustomer");
        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<ContractDTO>>(contractDTOList) {
        }).build();
    }

    @GET
    @Path("contractsForCustomer")
    public Response getContractsForCustomer(@QueryParam("customerId") Long customerId) {
        LOGGER.trace("Entering getContractsForCustomer");
        if (customerId == null) {
            return ResponseBuilder.badRequest().build();
        }
        List<ContractDTO> contractDTOList;
        try {
            long startTime = System.currentTimeMillis();
            contractDTOList = contractResource.getContracts(customerId);
            LOGGER.debug("Received response in {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception ex) {
            LOGGER.debug("Error occurred: {}", ex.getMessage());
            return ResponseBuilder.notFound().withEntity(buildGenericError(ex.getMessage())).build();
        }

        JsonArray jsonArray = new JsonArray();
        for (ContractDTO contractDTO : contractDTOList) {
            jsonArray.add(gson.toJsonTree(contractDTO));
        }

        LOGGER.trace("Exiting getContractsForCustomer");
        return ResponseBuilder.anOKResponse().withEntity(jsonArray.toString()).build();
    }

    @PUT
    @Path("contract")
    public Response updateContract(ContractDTO contractDTO, @QueryParam("userID") String userId) {
        LOGGER.trace("Entering updateContract");
        if (!AssertObject.areNotNull(contractDTO, userId)) {
            return ResponseBuilder.badRequest().build();
        }


        Boolean isSuccess = contractResource.updateContract(contractDTO, userId);

        if (isSuccess) {
            return ResponseBuilder.anOKResponse().build();
        } else {
            return ResponseBuilder.internalServerError().build();
        }
    }


    @GET
    @Path("countries")
    public Response getCountries() {
        List<CountryDTO> countryDTOList;
        try {
            long startTime = System.currentTimeMillis();
            countryDTOList = siteResourceClient.getCountries();
            LOGGER.debug("Received response in {} ms", System.currentTimeMillis() - startTime);
        } catch (RestException e) {
            throw e;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }
        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<CountryDTO>>(countryDTOList) {
        }).build();
    }

    @GET
    @Path("legal-entities")
    public Response getLegalEntities(@QueryParam("customerID") Long customerId) {

        if (customerId == null) {
            return ResponseBuilder.badRequest().build();
        }

        List<CusLeDTO> cusLeDTOs = customerResource.getLegalEntities(customerId);

        if (cusLeDTOs == null || cusLeDTOs.size() < 1) {
            return ResponseBuilder.notFound().build();
        }

        GenericEntity<List<CusLeDTO>> genericLEList = new GenericEntity<List<CusLeDTO>>(cusLeDTOs) {
        };
        return ResponseBuilder.anOKResponse().withEntity(genericLEList).build();
    }

    @POST
    @Path("updateCustomerAdditionalDetail")
    public Response updateCustomerAdditionalDetail(@HeaderParam("SM_USER") String userId, CustomerAddDetailDTO customerAddDetailDTO) {

        if (AssertObject.anyEmpty(userId, customerAddDetailDTO) || AssertObject.anyEmpty(customerAddDetailDTO.getDefaultCacId(), customerAddDetailDTO.getCadCustomerId())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid Input - userId/accountId/customerId")).build();
        }
        LOGGER.info("Associating Billing Id -" + customerAddDetailDTO.getDefaultCacId() + " to Customer -" + customerAddDetailDTO.getCadCustomerId());
        customerResource.updateCustomerAdditionalDetail(userId, customerAddDetailDTO);
        LOGGER.info("Billing Associated successfully ..");
        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Path("customerAdditionalDetail")
    public Response getCustomerAdditionalDetail(@QueryParam("customerID") String customerId) {
        if (AssertObject.anyEmpty(customerId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid customerId")).build();
        }
        CustomerAddDetailDTO customerAddDetailDTO = customerResource.getCustomerAdditionalDetail(customerId);

        if (customerAddDetailDTO != null) {
            GenericEntity<CustomerAddDetailDTO> genericEntity = new GenericEntity<CustomerAddDetailDTO>(customerAddDetailDTO) {
            };
            return ResponseBuilder.anOKResponse().withEntity(genericEntity).build();
        } else {
            return ResponseBuilder.notFound().build();
        }

    }

    @GET
    @Path("allAvailableSalesChannel")
    public Response getAllAvailableSalesChannel() {
        try {
            List<PortDistributorDTO> portDistributorDTOs = customerResource.getAvailableSalesChannels();

            if (portDistributorDTOs != null) {
                return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<PortDistributorDTO>>(portDistributorDTOs) {
                }).build();
            } else {
                return ResponseBuilder.notFound().build();
            }

        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Failed to get SalesChannels from BFG !!", ex);
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }

    }

    @POST
    @Path("associateSalesChannelToContract/{contractId}")
    public Response associateSalesChannelToContract(@QueryParam(SALES_CHANNEL) String salesChannelName, @PathParam("contractId") Long contractId, @HeaderParam("SM_USER") String userId) {
        if (AssertObject.anyEmpty(salesChannelName, contractId, userId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Empty salesChannel/ContractId/UserId !!")).build();
        }

        PortDistributorDTO portDistributorDTO = customerResource.getPortDistributor(salesChannelName,null);

        String gfrCode = null;
        try {
            gfrCode = expedioCustomerResource.getGfrCode(salesChannelName);
        } catch (ResourceNotFoundException ex) {
        }

        if (AssertObject.isEmpty(gfrCode)) {
            return Response.status(Response.Status.NOT_FOUND).entity(buildGenericError("Empty GFR Code found for sales channel :" + salesChannelName)).build();
        }

        ContractDTO contractDTO = contractResource.getContract(contractId);

        if (contractDTO != null) {
            contractDTO.setConGfrCode(gfrCode);
            contractDTO.setConRoleId(portDistributorDTO.getRoleId());

            contractResource.updateContract(contractDTO, userId);
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(buildGenericError("No contract by the id :" + contractId)).build();
        }

    }

    @GET
    @Path("getPortDistributor")
    public Response getPortDistributor(@QueryParam("portRoleId") Long portRoleId){
        if (AssertObject.isNull(portRoleId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Empty portRoleId !!")).build();
        }

        PortDistributorDTO portDistributorDTO = customerResource.getPortDistributor(null,portRoleId.toString());

        if(portDistributorDTO!=null){
            return Response.status(Response.Status.OK).entity(new GenericEntity<PortDistributorDTO>(portDistributorDTO){}).build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("getContractById")
    public  Response getContract(@QueryParam("contractID") Long contractId){
         if(AssertObject.isNull(contractId)){
             return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Empty contractId !!")).build();
         }

        ContractDTO contractDTO = contractResource.getContract(contractId);

        return Response.status(Response.Status.OK).entity(new GenericEntity<ContractDTO>(contractDTO){}).build();
    }

    @POST
    @Path("updateContract")
    public  Response updateContract(@HeaderParam("SM_USER") String userId,ContractDTO contractDTO){
        if(AssertObject.anyEmpty(contractDTO.getId(),userId)){
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(String.format("Invalid Data. UserId -[%s], Contract ID -[%s]",userId,contractDTO.getId()))).build();
        }

        if(!AssertObject.isEmpty(contractDTO.getStartDateInString())){
            contractDTO.setStartDate(Utility.stringToDate(contractDTO.getStartDateInString()));
        }

        contractResource.updateContract(contractDTO,userId);

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("clientGroups")
    public Response getClientGroups(){
       List<ClientGroupDTO> clientGroupDTOs = contractResource.getClientGroups();

        return Response.status(Response.Status.OK).entity(new GenericEntity<List<ClientGroupDTO>>(clientGroupDTOs){}).build();
    }

    @GET
    @Path("allSalesChannelWithGfr")
    public Response getAllSalesChannelWithGfrCode(){
        List<ExpedioSalesChannelDto> expedioSalesChannelDtos = expedioCustomerResource.getAllSalesChannelsWithGfr();

        if(expedioSalesChannelDtos!=null && expedioSalesChannelDtos.size()>0){
            return Response.status(Response.Status.OK).entity(new GenericEntity<List<ExpedioSalesChannelDto>>(expedioSalesChannelDtos){}).build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private Long createDefaultCentralSite(String userId, String customerName, Long customerId, String customerRef) {
        SiteUpdateDTO siteCreationDTO = new SiteUpdateDTO();
        siteCreationDTO.setSitName(new StringBuilder().append("CITY").append(" - ").append(customerId).append(" - 1").toString());
        siteCreationDTO.setSitCusId(customerId);
        siteCreationDTO.setAdrStreetNumber(Constants.DEFAULT_STREETNUM);
        siteCreationDTO.setAdrSitePremises(Constants.DEFAULT_SITEPREMISIES);
        siteCreationDTO.setAdrTown(Constants.DEFAULT_TOWN);
        siteCreationDTO.setAdrCountry(Constants.DEFAULT_COUNTRY);
        siteCreationDTO.setSitType(Constants.DEFAULT_CENTRAL_SITE_TYPE);
        siteCreationDTO.setAdrStreetName(Constants.DEFAULT_CENTRAL_SITE_TYPE);
        siteCreationDTO.setAdrLocality(Constants.DEFAULT_CENTRAL_SITE_TYPE);
        siteCreationDTO.setSitComment(Constants.DEFAULT_CENTRAL_SITE_TYPE);
        siteCreationDTO.setAdrPostZipCode(Constants.DEFAULT_CENTRAL_SITE_TYPE);
        siteCreationDTO.setCustomerRef(customerRef);
        siteCreationDTO.setCustomerId(customerId);
        siteCreationDTO.setSitCusReference(customerRef);
        siteCreationDTO.setSitLanguage(Constants.DEFAULT_LANGUAGE);
        siteCreationDTO.setSitCurrency(Constants.DEFAULT_CURRENCY);
        siteCreationDTO.setAdrValidationLevel(Constants.DEFAULT_VALIDATION_LEVEL);
        siteCreationDTO.setAdrAccuracyLevel(Constants.DEFAULT_ACCURACY_LEVEL);
        String nextVal = customerResource.getNextCustomerNo();
        String prefix = null;
        if (customerName.length() > 4) {
            prefix = customerName.substring(0, 5);
        } else {
            prefix = customerName;
        }
        siteCreationDTO.setSitName(prefix + " - CS" + nextVal);
        siteCreationDTO.setCusName(customerName);
        return siteResourceClient.createSite(userId, siteCreationDTO);
    }

    private String createDefaultCentralSiteLocation(String userId, Long siteId) {
        SiteLocationDTO siteLocationDTO = new SiteLocationDTO();
        siteLocationDTO.setLocSitId(siteId);
        siteLocationDTO.setLocSubPremise(Constants.DEFAULT_SUB_PREMISE);
        siteLocationDTO.setLocRoom(Constants.DEFAULT_ROOM);
        siteLocationDTO.setLocFloor(Constants.DEFAULT_FLOOR);
        return siteLocationResource.createLocation(userId, siteLocationDTO);
    }

    private ContractDTO createDefaultCustomerContract(String userId, Long customerId, Long centralSiteId, String salesChannel, String contractFriendlyName,String conManagedFlag,Long contractCeaseTerm,Long contractLinkedCeaseTerm) {
        ContractDTO contractDTO = new ContractDTO();
        contractDTO.setCusId(customerId);
        SiteDTO centralSite = new SiteDTO();
        centralSite.setSiteId(centralSiteId);
        contractDTO.setCentralSite(centralSite);
        contractDTO = populateDefaultContract(contractDTO, customerId);
        contractDTO.setContractualCeaseTerm(contractCeaseTerm);
        contractDTO.setLinkedContractualCeaseTerm(contractLinkedCeaseTerm);
        //contractDTO.setCon
        if (!AssertObject.anyEmpty(contractFriendlyName)) {
            contractDTO.setCustRefNumber(contractFriendlyName);
        }
        contractDTO.setManagedSolutionFlag(conManagedFlag);
        return contractResource.createContract(contractDTO, userId, salesChannel);
    }

    private ContractDTO populateDefaultContract(ContractDTO contractDTO, Long customerId) {

        if (customerId != null && contractDTO != null) {
            Date createDate = new Date();
            SimpleDateFormat sdf= new SimpleDateFormat("dMyyyyHHmmss");
            contractDTO.setConRoleId(-1L);
            contractDTO.setConGfrCode(null);
            contractDTO.setBchId(Constants.DEFAULT_BCH_ID);
            contractDTO.setCgpId(Constants.DEFAULT_CGP_ID);
            contractDTO.setRefNumber(new StringBuilder().append(customerId).append(sdf.format(createDate)).toString());
            contractDTO.setCustRefNumber(new StringBuilder().append(customerId).append(sdf.format(createDate)).toString());
            contractDTO.setCustomerCode(Constants.DEFAULT_CUSTOMER_CODE);
            contractDTO.setBillFrequency(Constants.DEFAULT_CON_BILL_FREQUENCY);
            contractDTO.setDuration(Constants.DEFAULT_CON_DURATION);
            contractDTO.setSignedDate(createDate);
            contractDTO.setStartDate(createDate);
            return contractDTO;
        } else {
            throw new RuntimeException("Couldn't create default contract as input argument is invalid !!");
        }

    }

}
