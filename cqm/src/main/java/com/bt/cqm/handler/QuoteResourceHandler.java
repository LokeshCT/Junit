package com.bt.cqm.handler;

import com.bt.cqm.config.BundlingAppConfig;
import com.bt.cqm.config.ReportAppConfig;
import com.bt.cqm.config.SqeAppConfig;
import com.bt.cqm.ldap.SearchBTDirectoryHandler;
import com.bt.cqm.ldap.model.LdapSearchModel;
import com.bt.cqm.repository.user.UserEntity;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.cqm.utils.Utility;
import com.bt.rsqe.EmailService;
import com.bt.rsqe.customerinventory.client.resource.ContractResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.contract.ContractDTO;
import com.bt.rsqe.customerinventory.dto.contract.PortDistributorDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteStatusDto;
import com.bt.rsqe.customerinventory.dto.site.SqeQuoteStatusDto;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.domain.AttachmentDTO;
import com.bt.rsqe.emppal.attachmentresource.EmpPalResource;
import com.bt.rsqe.expedio.services.quote.ChannelContactCreateDTO;
import com.bt.rsqe.expedio.services.quote.DeleteChannelContactDTO;
import com.bt.rsqe.expedio.services.quote.QrefGenGuidDTO;
import com.bt.rsqe.expedio.services.quote.QuoteChannelContactDTO;
import com.bt.rsqe.expedio.services.quote.QuoteCreationDTO;
import com.bt.rsqe.expedio.services.quote.QuoteDetailsDTO;
import com.bt.rsqe.expedio.services.quote.QuoteLaunchConfiguratorDTO;
import com.bt.rsqe.expedio.services.quote.QuotePriceBookDTO;
import com.bt.rsqe.expedio.services.quote.QuoteResource;
import com.bt.rsqe.expedio.services.quote.QuoteUpdateDTO;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.ClasspathConfiguration;
import com.bt.rsqe.web.rest.exception.RestException;
import com.google.common.base.Strings;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.cqm.utils.Constants.*;
import static com.bt.cqm.utils.Utility.*;
import static java.lang.String.*;

@Path("/cqm/quotes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class QuoteResourceHandler {
    private static final String EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL = "FAIL";
    private static final String SQE_COOKIE_NAME = "SQE_GUID";
    private final ReportAppConfig reportAppConfig;
    private static final String QUOTE_CREATION_SUCCESS_TEMPLATE = "com/bt/cqm/quote-creation-email-template.ftl";
    private static final String QUOTE_CREATION_SUCCESS_SUBJECT = "New Quote: %s Created under Customer name: %s";
    private static final String QUOTE_CREATION_SUCCESS_NOTE = "The Quote below has been successfully Created.";
    private final UserManagementRepository userManagementRepository;

    private final QuoteResource quoteResource;

    private final ContractResourceClient contractResource;

    private final BundlingAppConfig bundlingAppConfig;
    private final SqeAppConfig sqeAppConfig;

    private final EmailService emailService;

    private final CustomerResource customerResource;
   private final SiteResourceClient siteResourceClient;
    private final EmpPalResource empPalResource;

    private static final Logger LOG = LoggerFactory.getLogger(QuoteResourceHandler.class);

    public QuoteResourceHandler(UserManagementRepository userManagementRepository,
                                QuoteResource quoteResource,
                                ContractResourceClient contractResource,
                                BundlingAppConfig bundlingAppConfig,
                                SqeAppConfig sqeAppConfig,
                                EmailService emailService,
                                CustomerResource customerResource,
                                ReportAppConfig reportAppConfig,
                                SiteResourceClient siteResourceClient,
                                EmpPalResource empPalResource) {
        this.userManagementRepository = userManagementRepository;
        this.quoteResource = quoteResource;
        this.contractResource = contractResource;
        this.bundlingAppConfig = bundlingAppConfig;
        this.sqeAppConfig = sqeAppConfig;
        this.emailService = emailService;
        this.customerResource = customerResource;
        this.reportAppConfig = reportAppConfig;
        this.siteResourceClient=siteResourceClient;
        this.empPalResource = empPalResource;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createQuote(@HeaderParam("SM_USER") String userId, @HeaderParam("USER_EMAIL") String userEmail, @HeaderParam("USER_TYPE") String userType, Form form,@QueryParam("quotePriceBooks") String quotePriceBooks) {
        if (AssertObject.isEmpty(userId) || form == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String guid = null;
        UserEntity userEntity = getUser(userId);
        QuoteCreationDTO quoteCreationDTO = null;
        LdapSearchModel ldapSearchResult = null;
        String quoteRefId = "";
        try {
            ldapSearchResult = searchBTDirectory(userEntity);
            quoteCreationDTO = createPayload(form, userEntity);
            quoteCreationDTO.setUserEmailId(userEmail);
            if(null!=quotePriceBooks)
            {
                List<QuotePriceBookDTO> quotePriceBookDTOList=formQuotePriceBookList(quotePriceBooks);
                quoteCreationDTO.setPriceBookDetails(quotePriceBookDTOList);
            }
            if (ldapSearchResult == null) {
                quoteCreationDTO.setRoleType("InDirect");
                quoteCreationDTO.setBoatID(userId);
            } else {
                quoteCreationDTO.setRoleType(userType);
                quoteCreationDTO.setBoatID(ldapSearchResult.getBoatId());
            }


            guid = quoteResource.createQuote(quoteCreationDTO);
            int len = guid.length();
            if (len > 15) {
                quoteRefId = guid.substring(len - 15, len);
            }
        } catch (com.bt.rsqe.web.rest.exception.RestException ex) {
            throw ex;
        } catch (Exception ex) {
            ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }

        String configuratorURL = String.format(bundlingAppConfig.getUrl(), guid);
        try {
            String mailSubject = constructMessageSubject(form.asMap().getFirst("quoteName"), form.asMap().getFirst("customerName"));
            String mailBody = constructMessageBody(quoteRefId, quoteCreationDTO, form.asMap().getFirst("customerName"));
            emailService.sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT, mailSubject, mailBody, userEmail);
        } catch (Exception e) {
            LOG.error("Failed to send Email for USER_ID :" + userId + "  having EMAIL_ID :" + userEmail, e);
        }

        // Create Share Point Folders
        try{
            String sharePointFolderStructure = Utility.buildSharePointFolderStructure(quoteCreationDTO.getSalesChannel(),quoteCreationDTO.getBfgCustomerID());
            try{
                //Assuming the parent folder structure is existing
                empPalResource.createFolder(new AttachmentDTO(sharePointFolderStructure+"/Bid Manager",quoteRefId));
                empPalResource.createFolder(new AttachmentDTO(sharePointFolderStructure+"/Sales",quoteRefId));
            }catch (Exception ex){
                //Create Folder Structure along with parent folders.
                empPalResource.createFolderWithPath(new AttachmentDTO(sharePointFolderStructure+"/Bid Manager",quoteRefId));
                empPalResource.createFolderWithPath(new AttachmentDTO(sharePointFolderStructure+"/Sales",quoteRefId));
            }
        }catch (Exception ex){
            LOG.warn("Failed to Create Share Point Folder for Quote Create. Quote Id : "+quoteRefId, ex);
        }

        return ResponseBuilder.anOKResponse().withEntity(configuratorURL).build();
    }

    @PUT
    public Response updateQuote(QuoteUpdateDTO quoteUpdateDTO) {
        if (AssertObject.isNull(quoteUpdateDTO) || AssertObject.anyEmpty(quoteUpdateDTO.getQuoteRefID())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        quoteResource.updateQuote(quoteUpdateDTO);

        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuoteList(@QueryParam("salesChannel") String salesChannel,
                                 @QueryParam("customerID") String customerID,
                                 @QueryParam("siteId") String siteId,
                                 @HeaderParam("CONTRACT_ID") String contractId
                                 ) {
        List<QuoteDetailsDTO> quoteList = null;
        List<QuoteDetailsDTO> resultQuoteList = new ArrayList<QuoteDetailsDTO>();
        List<QuoteDetailsDTO> resultQuoteListFilterSiteId = new ArrayList<QuoteDetailsDTO>();
        try {
            quoteList = quoteResource.getQuotes(salesChannel, customerID,contractId);

            for (QuoteDetailsDTO quoteDetailsDTO : quoteList) {
                if (contractId.equals(quoteDetailsDTO.getContractId())) {
                    resultQuoteList.add(quoteDetailsDTO);
                }

            }
            //Search By Site Id  - Begin
            if (siteId != null && siteId.trim().length() > 0) {
                try {
                    SiteStatusDto siteStatusDto = siteResourceClient.getAssociatedQuotes(siteId.trim());
                    List<SqeQuoteStatusDto> quotesAssociatedList = null;
                    if (null != siteStatusDto) {
                        quotesAssociatedList = siteStatusDto.getData();
                    }
                    if (quotesAssociatedList != null && !quotesAssociatedList.isEmpty()) {
                        for (SqeQuoteStatusDto quoteAssociated : quotesAssociatedList) {
                            String quoteId = quoteAssociated.getQuoteId();
                            for (QuoteDetailsDTO quoteDetail : resultQuoteList) {
                                if (quoteId.equals(quoteDetail.getQuoteReferenceId())) {
                                    resultQuoteListFilterSiteId.add(quoteDetail);
                                    break;
                                }
                            }

                        }
                        //resultQuoteList = resultQuoteListFilterSiteId;
                    }
                    resultQuoteList = resultQuoteListFilterSiteId;
                } catch (Exception ex) {
                    return ResponseBuilder.internalServerError().withEntity(buildGenericError("Failed to get List of Quotes for the Site")).build();
                }
            }

            //Search By Site Id-ENd


        } catch (RestException rEx) {
            LOG.warn("Couldnt Fetch Quote " + rEx);
            throw rEx;
        } catch (Exception e) {
            String message = "Error occured while fetching quote for customer: " + customerID + ", Sales Channel: " + salesChannel;
            LOG.warn(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(message)).build();
        }
        GenericEntity<List<QuoteDetailsDTO>> entity = new GenericEntity<List<QuoteDetailsDTO>>(resultQuoteList) {
        };



        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @GET
    @Path("/bundlingAppUrl")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getBundlingAppURL(@QueryParam("quoteId") String quoteId,
                                      @QueryParam("quoteVersion") String quoteVersion,
                                      @HeaderParam("SM_USER") String userId,
                                      @HeaderParam("USER_EMAIL") String userEmail,
                                      @HeaderParam("USER_TYPE") String userType,
                                      @HeaderParam("BOAT_ID") String boatId,
                                      @HeaderParam("IS_MNC") String isMnc) {

        if (AssertObject.anyEmpty(quoteId, quoteVersion, userId)) {
            return ResponseBuilder.badRequest().withEntity(" Invalid Input - QuoteId/QuoteVersion/UserId").build();
        }

        try {
            String guid = createSession(quoteId, quoteVersion, userId, userEmail, userType, boatId, isMnc);

            return ResponseBuilder.anOKResponse().withEntity(
                    String.format(bundlingAppConfig.getUrl(), guid)
            ).build();

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/sqeAppUrl")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSqeAppURL(@QueryParam("quoteId") String quoteId,
                                 @QueryParam("quoteVersion") String quoteVersion,
                                 @QueryParam("quoteHeaderId") String quoteHeaderId,
                                 @HeaderParam("SM_USER") String userId,
                                 @HeaderParam("USER_EMAIL") String userEmail,
                                 @HeaderParam("USER_TYPE") String userType,
                                 @HeaderParam("BOAT_ID") String boatId,
                                 @HeaderParam("IS_MNC") String isMnc) {

        if (AssertObject.anyEmpty(quoteId, quoteVersion, userId)) {
            return ResponseBuilder.badRequest().withEntity(" Invalid Input - QuoteId/QuoteVersion/UserId").build();
        }

        try {
            String guid = createSession(quoteId, quoteVersion, userId, userEmail, userType, boatId, isMnc);

            return ResponseBuilder.anOKResponse().withEntity(
                    String.format(sqeAppConfig.getUrl(), guid, quoteHeaderId)
            ).build();

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }


    public String createSession(String quoteId,
                                String quoteVersion,
                                String userId,
                                String userEmail,
                                String userType,
                                String boatId,
                                String isMnc) throws Exception {

        UserEntity userEntity = getUser(userId);

        if (userEntity != null) {
            QuoteLaunchConfiguratorDTO configuratorParamData = QuoteLaunchConfiguratorDTO.builder()
                    .withRoleType(userEntity.getUserType().getRoleTypeName())
                    .withEIN(userEntity.getUserId())
                    .withSalesRepName(userEntity.getUserName())
                    .withQuoteID(quoteId)
                    .withQuoteVersion(quoteVersion)
                    .withManagedCustomer("Y".equalsIgnoreCase(isMnc) ? "Y" : "N")
                    .withBoatID(INDIRECT_USER_TYPE.equalsIgnoreCase(userType) && AssertObject.anyEmpty(boatId) ? userId : boatId.toLowerCase())
                    .withUserEmailId(!AssertObject.anyEmpty(userEmail) ? userEmail : null)
                    .withUserRole(userEntity.hasCeaseRole() ? "Sales User" : userEntity.getRole())
                    .withCeaseOptimizationFlag(userEntity.hasCeaseRole() ? "Yes" : "No")
                    .build();

            String sessionId = quoteResource.generateGUID(configuratorParamData);

            if( sessionId == null) {
                throw new RuntimeException("GUID is null");
            }
            return sessionId;
        }
        throw new UnsupportedOperationException(String.format("Invalid user : %s", userId));
    }


    private LdapSearchModel searchBTDirectory(UserEntity userEntity) {
        LdapSearchModel ldapSearchResult = null;
        if (userEntity != null) {
            Map<String, String> args = new HashMap<String, String>();
            args.put("ein", userEntity.getUserId());
            List<LdapSearchModel> resultList = new SearchBTDirectoryHandler().searchBTDirectory(args);
            if (resultList != null && !resultList.isEmpty()) {
                ldapSearchResult = resultList.get(0);
            }
        }
        return ldapSearchResult;
    }

    private Long getContractID(Form form) {
        Long contractID = null;
        String contractIdStr = form.asMap().getFirst("contractId");

        if (contractIdStr != null) {
            try {
                contractID = Long.parseLong(contractIdStr);
            } catch (Exception ex) {
            }
        }

        if (contractID == null) {
            String salesOrgName = form.asMap().getFirst("salesOrgName");
            String cusIdStr = form.asMap().getFirst("customerId");
            Long customerId = (Strings.isNullOrEmpty(cusIdStr)) ? new Long(0L) : Long.parseLong(cusIdStr);
            List<ContractDTO> contractDTOList = contractResource.getContracts(salesOrgName, customerId);
            if (contractDTOList != null) {
                contractID = contractDTOList.get(0).getId();
            }
        }
        return contractID;
    }

    private UserEntity getUser(String userId) {
        return userManagementRepository.findUserByUserId(userId);
    }

    private QuoteCreationDTO createPayload(Form form, UserEntity userEntity) {
        QuoteCreationDTO quoteCreationDTO = new QuoteCreationDTO();
        quoteCreationDTO.setSalesChannel(form.asMap().getFirst("salesOrgName"));
        if (userEntity != null) {
            quoteCreationDTO.setEIN(userEntity.getUserId());
        }
        quoteCreationDTO.setBfgCustomerID(form.asMap().getFirst("customerId"));

        Long contractID = getContractID(form);
        quoteCreationDTO.setBfgContractID(contractID);

        quoteCreationDTO.setSalesRepName(form.asMap().getFirst("salesRepName"));
        quoteCreationDTO.setOrderType(form.asMap().getFirst("orderType[name]"));
        quoteCreationDTO.setQuoteName(form.asMap().getFirst("quoteName"));
        quoteCreationDTO.setContractTerm(form.asMap().getFirst("contractTerm"));
        quoteCreationDTO.setBidNumber(form.asMap().getFirst("bidNumber"));
        quoteCreationDTO.setOpportunityReferenceNumber(form.asMap().getFirst("orNumber"));
        quoteCreationDTO.setCurrency(form.asMap().getFirst("currency[name]"));
        quoteCreationDTO.setQuoteIndicativeFlag(form.asMap().getFirst("quoteIndicativeFlag[name]"));
        quoteCreationDTO.setSubGroup(form.asMap().getFirst("subGroup"));
        String subOrderType = form.asMap().getFirst("subOrderType[name]");
        quoteCreationDTO.setSubOrderType((subOrderType == null) ? "" : subOrderType);

        if (userEntity != null && userEntity.getUserRoleConfig() != null && userEntity.getUserRoleConfig().size() > 0) {
            quoteCreationDTO.setUserRole(userEntity.getUserRoleConfig().get(0).getRole().getRoleName());
        }
        // quoteCreationDTO.setTradeLevel( ); //TODO: find how to get trade Level

        return quoteCreationDTO;
    }


    @POST
    @Path("/createQuoteChannelContact")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createQuoteChannelContact(ChannelContactCreateDTO quoteChannelContactDTO) {
        try {
            if (quoteChannelContactDTO != null && quoteChannelContactDTO.getEin() != null) {
                String message = quoteResource.createQuoteChannelContact(quoteChannelContactDTO);
                if (message != null && message.trim().equalsIgnoreCase(SUCCESS)) {
                    return ResponseBuilder.anOKResponse().withEntity(message).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("FAIL").build();
                }
            } else {
                return ResponseBuilder.badRequest().withEntity("Bad Data.").build();
            }

        } catch (com.bt.rsqe.web.rest.exception.RestException rEx) {
            throw rEx;
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/updateQuoteChannelContact")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateQuoteChannelContact(QuoteChannelContactDTO quoteChannelContactDTO) {

        try {
            if (quoteChannelContactDTO != null && quoteChannelContactDTO.getChannelContactID() != null) {
                String message = quoteResource.updateQuoteChannelContact(quoteChannelContactDTO);
                if (message != null && message.trim().equalsIgnoreCase(SUCCESS)) {
                    return ResponseBuilder.anOKResponse().withEntity(message).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(FAIL).build();
                }
            } else {
                return ResponseBuilder.badRequest().withEntity("Bad Data.").build();
            }

        } catch (com.bt.rsqe.web.rest.exception.RestException rEx) {
            throw rEx;
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @POST
    @Path("/deleteQuoteChannelContact")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteQuoteChannelContact(QuoteChannelContactDTO quoteChannelContactDTO) {
        try {
            if (quoteChannelContactDTO != null && quoteChannelContactDTO.getChannelContactID() != null && quoteChannelContactDTO.getQuoteID() != null) {
                List<DeleteChannelContactDTO> deleteList = new ArrayList<DeleteChannelContactDTO>();
                DeleteChannelContactDTO deleteChannelContactDTO = new DeleteChannelContactDTO();
                deleteChannelContactDTO.setChannelContactID(quoteChannelContactDTO.getChannelContactID());
                deleteChannelContactDTO.setQuoteID(quoteChannelContactDTO.getQuoteID());
                deleteList.add(deleteChannelContactDTO);

                String message = quoteResource.deleteQuoteChannelContact(deleteList);
                if (message != null && message.trim().equalsIgnoreCase(SUCCESS)) {
                    return ResponseBuilder.anOKResponse().withEntity(message).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("FAIL").build();
                }
            } else {
                return ResponseBuilder.badRequest().withEntity("Bad Data.").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * @param quoteId
     * @return
     */

    @GET
    @Path("/getChannelContacts")
    public Response getChannelContacts(@QueryParam("quoteId") String quoteId) {
        try {
            if (quoteId != null) {
                List<QuoteChannelContactDTO> channelList = getChannelContactData(quoteId);
                GenericEntity<List<QuoteChannelContactDTO>> entity = new GenericEntity<List<QuoteChannelContactDTO>>(channelList) {
                };
                return ResponseBuilder.anOKResponse().withEntity(entity).build();
            } else {
                return ResponseBuilder.badRequest().build();
            }

        } catch (com.bt.rsqe.web.rest.exception.RestException e) {
            throw e;
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildGenericError(e.getMessage())).build();
        }

    }

    @POST
    @Path("/generateQrefGuid")
    @Produces(MediaType.TEXT_PLAIN)
    public Response generateQrefGuid(@HeaderParam("USER_EMAIL") String userEmail, @HeaderParam("USER_TYPE") String userType, @HeaderParam("USER_NAME") String userName, @HeaderParam("USER_ROLE") String role, QrefGenGuidDTO qrefGenGuidDTO) {
        if (AssertObject.anyEmpty(userEmail, userType, userName, role, qrefGenGuidDTO) || AssertObject.anyEmpty(qrefGenGuidDTO.getSalesChannel())) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Invalid Input - UserEmail/UserName/UserRole/UserType/SalesChannel/QrefGenGuidDTO is null or empty").build();
        }

        try {
            String firstName = "";
            String lastName = "";
            String[] nameSplit = userName.split(" ");

            if (nameSplit.length >= 2) {
                firstName = nameSplit[0];
                lastName = nameSplit[nameSplit.length - 1];
            }


            PortDistributorDTO portDistributorDTO = customerResource.getPortDistributor(qrefGenGuidDTO.getSalesChannel(), null);
            if ("N".equals(portDistributorDTO.getOrgIndirectFlag())) {
                qrefGenGuidDTO.setSalesChannelType("Direct");
            } else {
                qrefGenGuidDTO.setSalesChannelType("Indirect");
            }
            qrefGenGuidDTO.setUserEmailID(userEmail);
            qrefGenGuidDTO.setUserFirstName(firstName);
            qrefGenGuidDTO.setUserLastName(lastName);
            qrefGenGuidDTO.setUserRole(role);
            qrefGenGuidDTO.setSubmitterSystem("CQM");
            qrefGenGuidDTO.setQrefReportType("Display Qref History");
            String qrefGuid = quoteResource.generateQrefGuid(qrefGenGuidDTO);
            if (qrefGuid != null) {
                qrefGuid = qrefGuid.substring(1, qrefGuid.length() - 1);
                String configuratorURL = String.format(reportAppConfig.getUrl(), qrefGuid);
                return ResponseBuilder.anOKResponse().withEntity(configuratorURL).withCookie(new NewCookie(SQE_COOKIE_NAME, qrefGuid, null, "bt.com", "Set from CQM.", 1800, false, false)).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("GUID is null").build();
            }

        } catch (com.bt.rsqe.web.rest.exception.RestException e) {
            throw e;
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    /**
     * Need to implemented business logic to fetch the data from Expedio.
     *
     * @param quoteId
     * @return
     */
    private List<QuoteChannelContactDTO> getChannelContactData(String quoteId) {

        List<QuoteChannelContactDTO> list = quoteResource.getQuoteChannelContacts(quoteId);

        return list;

    }

    private String constructMessageBody(String quoteRefId, QuoteCreationDTO quoteCreationDTO, String customerName) throws IOException, TemplateException {
        Configuration config = new ClasspathConfiguration();

        Map<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("messageNote", QUOTE_CREATION_SUCCESS_NOTE);
        rootMap.put("salesUser", quoteCreationDTO.getSalesRepName());
        rootMap.put("customerName", customerName);
        rootMap.put("salesChannel", quoteCreationDTO.getSalesChannel());
        rootMap.put("quoteName", quoteCreationDTO.getQuoteName());
        rootMap.put("quoteRefId", quoteRefId);
        rootMap.put("quoteVersion", "1.0");
        rootMap.put("quoteType", quoteCreationDTO.getOrderType());
        rootMap.put("contractTerm", quoteCreationDTO.getContractTerm() == null ? "NA" : quoteCreationDTO.getContractTerm());
        rootMap.put("currency", quoteCreationDTO.getCurrency());
        Template emailTemplate = config.getTemplate(QUOTE_CREATION_SUCCESS_TEMPLATE);

        Writer out = new StringWriter();
        emailTemplate.process(rootMap, out);

        return out.toString();
    }

    private String constructMessageSubject(String quoteName, String customerName) {
        return format(QUOTE_CREATION_SUCCESS_SUBJECT, quoteName, customerName);
    }


    private List<QuotePriceBookDTO> formQuotePriceBookList(String quotePriceBooks) {
          List<QuotePriceBookDTO> quotePriceBookDTOList=new ArrayList<QuotePriceBookDTO>();
        QuotePriceBookDTO quotePriceBookDTO =null;
        try{
        JSONArray quotePriceJSONArray=new JSONArray(quotePriceBooks);
         int noOfPriceBooks=quotePriceJSONArray.length();
            for(int i=0; i<noOfPriceBooks;i++)
            {
                quotePriceBookDTO =new QuotePriceBookDTO();
                JSONObject priceBookJSON=quotePriceJSONArray.getJSONObject(i);
                quotePriceBookDTO.setTradeLevelEntity(priceBookJSON.getString("tradeLevelEntity"));
                quotePriceBookDTO.setTradeLevel(priceBookJSON.getString("tradeLevel"));
                quotePriceBookDTO.setProductName(priceBookJSON.getString("productName"));
                quotePriceBookDTO.setRrpPriceBook(priceBookJSON.getString("rrpPriceBook"));
                quotePriceBookDTO.setPtpPriceBook(priceBookJSON.getString("ptpPriceBook"));
                quotePriceBookDTO.setScode(priceBookJSON.getString("scode"));
                quotePriceBookDTO.setHcode(priceBookJSON.getString("hcode"));
                quotePriceBookDTOList.add(quotePriceBookDTO);
            }
        }
        catch(Exception e)
        {

        }
        return quotePriceBookDTOList;
    }
}
