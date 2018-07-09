package com.bt.cqm.handler;

import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.rsqe.customerinventory.client.resource.BillingAccountResourceClient;
import com.bt.rsqe.customerinventory.client.resource.ContactResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.CurrencyCodeDTO;
import com.bt.rsqe.customerinventory.dto.billing.CustomerBillingDetailDTO;
import com.bt.rsqe.customerinventory.dto.contact.ContactRoleDTO;
import com.bt.rsqe.customerinventory.dto.contact.CustomerContactDTO;
import com.bt.rsqe.customerinventory.dto.le.LegalEntityDTO;
import com.bt.rsqe.customerinventory.dto.location.AddressDTO;
import com.bt.rsqe.customerinventory.dto.site.CountryDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteUpdateDTO;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.customerinventory.utils.Utility;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.expedio.project.ClarityProjectDto;
import com.bt.rsqe.expedio.project.ClarityProjectRequestDto;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.rest.exception.RestException;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import static com.bt.cqm.utils.Utility.buildGenericError;


@Path("/cqm/billing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class BillingAccountHandler {

    private static final String SUB_STREET = "subStreet";
    private static final String CITY = "city";
    private static final String ADR_ID = "adrId";
    private static final String STATE = "state";
    private static final String POST_CODE = "postCode";
    private static final String SITE_ID = "siteId";
    private static final String SITE_NAME = "siteName";
    private static final String BUILDING_NAME = "buildingName";
    private static final String SUB_BUILDING = "subBuilding";
    private static final String BUILDING_NUMBER = "buildingNumber";
    private static final String STREET = "street";
    private static final String LOCALITY = "locality";
    private static final String SUB_LOCALITY = "subLocality";
    private static final String SUB_STATE = "subState";
    private static final String BILLING = "BILLING";
    private static final String SUB_POST_CODE = "subPostCode";
    private static final String PO_BOX = "POBox";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private BillingAccountResourceClient billingAccountResourceClient;
    private UserManagementRepository userManagementRepository;
    private SiteResourceClient siteResourceClient;
    private CustomerResource customerResource;
    private ContactResourceClient contactResourceClient;
    ExpedioClientResources expedioClientResources;
    private Gson gson;
    private static final String PRIMARY_BILLING_CONTACT_TYPE = "Primary Billing";

    public BillingAccountHandler(BillingAccountResourceClient billingAccountResourceClient, SiteResourceClient siteResourceClient, CustomerResource customerResource,
                                 ContactResourceClient contactResourceClient, UserManagementRepository userManagementRepository, ExpedioClientResources expedioClientResources) {
        this.billingAccountResourceClient = billingAccountResourceClient;
        this.siteResourceClient = siteResourceClient;
        this.customerResource = customerResource;
        this.contactResourceClient = contactResourceClient;
        this.userManagementRepository = userManagementRepository;
        this.expedioClientResources = expedioClientResources;
        this.gson = new Gson();
    }

    @GET
    @Path("getBillingAccounts")
    public Response showSearchBillingAccounts(@QueryParam("customerID") String customerId, @QueryParam("contractID") String contractId) {
        try {
            List<CustomerBillingDetailDTO> billingAccountList = billingAccountResourceClient.getBillingAccountForCustomer(customerId, contractId);

            if (billingAccountList == null) {
                return ResponseBuilder.notFound().build();
            }
            ListIterator li = billingAccountList.listIterator();
            List<CustomerBillingDetailDTO> updatedBillingAccountList= new ArrayList<CustomerBillingDetailDTO>(billingAccountList.size());
            CustomerBillingDetailDTO customerBillingDetailDTO=null;
            while (li.hasNext()) {
                customerBillingDetailDTO=(CustomerBillingDetailDTO)li.next();
                if(!StringUtils.isEmpty(customerBillingDetailDTO.getOriginatorGfrCode()))
                {
                    //String salesChannelName = billingAccountResourceClient.getSalesChannelFromGfrCode(customerBillingDetailDTO.getOriginatorGfrCode());
                    String salesChannelName = expedioClientResources.getCustomerResource().getSalesChannelFromGfrCode(customerBillingDetailDTO.getOriginatorGfrCode());
                    if(!StringUtils.isEmpty(salesChannelName))
                        customerBillingDetailDTO.setOriginatorGfrCode(salesChannelName.toUpperCase());
                    }
                updatedBillingAccountList.add(customerBillingDetailDTO);
            }
            GenericEntity<List<CustomerBillingDetailDTO>> genericDto = new GenericEntity<List<CustomerBillingDetailDTO>>(updatedBillingAccountList) {
            };

            return ResponseBuilder.anOKResponse().withEntity(genericDto).build();
        } catch (RestException ex) {
            throw ex;
        } catch (Exception e) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(e.getMessage())).build();
        }
    }

    @GET
    @Path("currencyCodes")
    public Response getCurrencyCodes() {
        try {

            //Modified the code to Support only 3 Currencies, GBP,USD,EUR
             List<CurrencyCodeDTO> currencyCodeDTOs = billingAccountResourceClient.getCurrencyCodes();
            if(currencyCodeDTOs!=null && currencyCodeDTOs.size()>0)
            {
                ListIterator<CurrencyCodeDTO> lt = currencyCodeDTOs.listIterator();
                CurrencyCodeDTO currencyCodeDTO =null;
                while(lt.hasNext())
                {
                      currencyCodeDTO=lt.next();
                    if(currencyCodeDTO.getCurrId()!=1 && currencyCodeDTO.getCurrId()!=157 && currencyCodeDTO.getCurrId()!=4)
                    {
                          lt.remove();
                    }
                }
            }
            GenericEntity<List<CurrencyCodeDTO>> genericDtos = new GenericEntity<List<CurrencyCodeDTO>>(currencyCodeDTOs) {
            };

            return ResponseBuilder.anOKResponse().withEntity(genericDtos).build();

        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/createBilling")
    public Response createBilling(@HeaderParam("SM_USER") String userId,
                                  @QueryParam("contractID") Long contractId,
                                  @QueryParam("customerId") Long cusId, Form form) {

        if (AssertObject.anyEmpty(userId, cusId)) {
            return ResponseBuilder.badRequest().build();
        }

        try {

            /*Create Site*/
            SiteUpdateDTO siteUpdateDTO = processSiteFormData(form);
            siteUpdateDTO.setSitName(userId + (new Date()).toString());
            siteUpdateDTO.setSitCusId(cusId);
            siteUpdateDTO.setCustomerId(cusId);
            siteUpdateDTO.setSitReference(null);
            Long siteId = siteResourceClient.createSite(userId, siteUpdateDTO);

            SiteDTO siteDTO = siteResourceClient.getSite(siteId.toString());

            /*Create Contact*/
            ContactRoleDTO contactRoleDTO = processContactFormData(cusId, form);
            CustomerContactDTO contactDTO =contactRoleDTO.getContact();
            contactDTO.setContactId(null);
            contactRoleDTO.setSiteId(siteId);

            ContactRoleDTO retContactRole = contactResourceClient.createContact(userId, contactRoleDTO);


            /*Create Billing Account*/
            CustomerBillingDetailDTO billingAccountDTO = processBillingFormData(cusId, form);
            billingAccountDTO.setContactRole(retContactRole);
            billingAccountDTO.setContractId(contractId);
            billingAccountDTO.setSiteId(siteId);
            AddressDTO addressDTO = billingAccountDTO.getAddress();
            if (addressDTO == null) {
                addressDTO = new AddressDTO();
                billingAccountDTO.setAddress(addressDTO);
            }
            addressDTO.setAdrId(siteDTO.getAddressId());

            String billingId = billingAccountResourceClient.createBillingAccount(userId, billingAccountDTO);

            String retStr = "Account ID: "+billingId+" ,BFG Contact ID: "+retContactRole.getContact().getContactId()+" ,BFG Site ID: "+siteId;
            return ResponseBuilder.anOKResponse().withEntity(retStr).build();


        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/updateBilling")
    public Response updateBilling(@HeaderParam("SM_USER") String userId,
                                  @QueryParam("customerId") Long cusId,
                                  Form form) {

        if (AssertObject.anyEmpty(userId, cusId)) {
            return ResponseBuilder.badRequest().build();
        }

        try {
            /*Update Site*/
            SiteUpdateDTO siteUpdateDTO = processSiteFormData(form);
            CustomerBillingDetailDTO billingAccountDTO = processBillingFormData(cusId, form);

            SiteDTO siteDTO = null;
            siteUpdateDTO.setSitCusId(cusId);
            siteUpdateDTO.setCustomerId(cusId);
            if (!AssertObject.anyEmpty(siteUpdateDTO.getSitId())) {
                siteResourceClient.updateSite(userId, siteUpdateDTO);
            } else {
                siteUpdateDTO.setSitName(userId + (new Date()).toString());
                Long siteId = siteResourceClient.createSite(userId, siteUpdateDTO);
                siteDTO = siteResourceClient.getSite(siteId.toString());
            }

            /*Update Contact*/
            ContactRoleDTO contactRoleDTO = processContactFormData(cusId, form);
            contactResourceClient.updateContact(userId, cusId, contactRoleDTO);

            /*Update Billing Account*/

            AddressDTO addressDTO = billingAccountDTO.getAddress();
            if (addressDTO == null) {
                addressDTO = new AddressDTO();
                billingAccountDTO.setAddress(addressDTO);
            }

            if (siteDTO != null) {
                addressDTO.setAdrId(siteDTO.getAddressId());
            }
            billingAccountResourceClient.updateBillingAccount(userId, billingAccountDTO);

            return ResponseBuilder.anOKResponse().build();

        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }
    }

    @GET
    @Path("/clarityProject")
    public Response getClarityProject(@QueryParam("clarityProjectCode") String clarityProjectCode, @QueryParam("clarityProjectName") String clarityProjectName, @QueryParam("sacId") String sacId) {

        if (AssertObject.isEmpty(clarityProjectCode)&& AssertObject.isEmpty(clarityProjectName) && AssertObject.isEmpty(sacId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Values are empty. ClarityProjectId:" + clarityProjectCode + ":ClarityProjectName:"
                                                                       + clarityProjectName + ":sacId:" + sacId).build();
        }

        ClarityProjectRequestDto requestDto = new ClarityProjectRequestDto(clarityProjectCode, clarityProjectName, sacId);
        List<ClarityProjectDto> dtoList = expedioClientResources.projectResource().getClarityProject(requestDto);
        GenericEntity<List<ClarityProjectDto>> clarityProjectList = new GenericEntity<List<ClarityProjectDto>>(dtoList) {
        };

        return ResponseBuilder.anOKResponse().withEntity(clarityProjectList).build();
    }


    private ContactRoleDTO processContactFormData(Long cusId, Form form) {
        ContactRoleDTO contactRoleDTO = new ContactRoleDTO();


        CustomerContactDTO contactDTO = new CustomerContactDTO();
        String contactId = form.asMap().getFirst("contactId");
        if (contactId != null) {
            contactDTO.setContactId(Long.parseLong(contactId));
        }
        contactDTO.setFirstName(form.asMap().getFirst("firstName"));
        contactDTO.setMiddleName(form.asMap().getFirst("middleName"));
        contactDTO.setLastName(form.asMap().getFirst("lastName"));
        contactDTO.setPosition(form.asMap().getFirst("jobTitle"));
        contactDTO.setPhoneNumber(form.asMap().getFirst("phoneNumber"));
        contactDTO.setMobileNumber(form.asMap().getFirst("mobileNo"));
        contactDTO.setFax(form.asMap().getFirst("fax"));
        contactDTO.setEmail(form.asMap().getFirst("email"));

        String contactRoleId = form.asMap().getFirst("contactRoleId");
        contactRoleDTO.setCtpType(PRIMARY_BILLING_CONTACT_TYPE);
        contactRoleDTO.setCustomerId(cusId);

        if (contactRoleId != null) {
            contactRoleDTO.setId(Long.parseLong(contactRoleId));
        }
        contactRoleDTO.setContact(contactDTO);

        return contactRoleDTO;
    }

    private SiteUpdateDTO processSiteFormData(Form form) {
        SiteUpdateDTO siteUpdateDTO = new SiteUpdateDTO();
        String city = form.asMap().getFirst(CITY);
        String adrId = form.asMap().getFirst(ADR_ID);
        String state = form.asMap().getFirst(STATE);
        String postCode = form.asMap().getFirst(POST_CODE);
        String siteId = form.asMap().getFirst(SITE_ID);
        String siteName = form.asMap().getFirst(SITE_NAME);


        siteUpdateDTO.setAdrSitePremises(form.asMap().getFirst(BUILDING_NAME));
        siteUpdateDTO.setSubBuilding(form.asMap().getFirst(SUB_BUILDING));
        siteUpdateDTO.setAdrStreetNumber(form.asMap().getFirst(BUILDING_NUMBER));
        siteUpdateDTO.setAdrStreetName(form.asMap().getFirst(STREET));
        siteUpdateDTO.setSubStreet(form.asMap().getFirst(SUB_STREET));
        siteUpdateDTO.setAdrLocality(form.asMap().getFirst(LOCALITY));
        siteUpdateDTO.setSubLocality(form.asMap().getFirst(SUB_LOCALITY));
        siteUpdateDTO.setSubCountyStateProvince(form.asMap().getFirst(SUB_STATE));
        siteUpdateDTO.setAdrTown(city);
        siteUpdateDTO.setAdrCounty(state);
        siteUpdateDTO.setSubCountyStateProvince(form.asMap().getFirst(SUB_STATE));
        siteUpdateDTO.setSitType(BILLING);
        siteUpdateDTO.setSitReference(form.asMap().getFirst("sitReference"));


        if (!AssertObject.isEmpty(postCode)) {
            siteUpdateDTO.setAdrPostZipCode(postCode);
        }

        if (!AssertObject.isEmpty(siteName)) {
            siteUpdateDTO.setSitName(siteName);
        }

        if (!AssertObject.isEmpty(siteId)) {
            siteUpdateDTO.setSitId(Long.parseLong(siteId));
        }
        siteUpdateDTO.setSubPostCode(form.asMap().getFirst(SUB_POST_CODE));
        siteUpdateDTO.setAdrPoBoxNumber(form.asMap().getFirst(PO_BOX));
        if (StringUtils.isNotBlank(form.asMap().getFirst(LATITUDE)) && StringUtils.isNotBlank(form.asMap().getFirst(LONGITUDE))) {
            siteUpdateDTO.setAdrLatitude(Double.valueOf(form.asMap().getFirst(LATITUDE)));
            siteUpdateDTO.setAdrLongitude(Double.valueOf(form.asMap().getFirst(LONGITUDE)));
        }


        siteUpdateDTO.setSubStreet(form.asMap().getFirst(SUB_STREET));

        String countryName = form.asMap().getFirst("country[name]");
        String countryAlpha2Code = form.asMap().getFirst("country[codeAlpha2]");

        if (AssertObject.anyNonEmpty(countryName, countryAlpha2Code)) {
            siteUpdateDTO.setAdrCountry(form.asMap().getFirst("country[name]"));
            siteUpdateDTO.setAdrCountryCode(form.asMap().getFirst("country[codeAlpha2]"));
        }


        return siteUpdateDTO;
    }

    private CustomerBillingDetailDTO processBillingFormData(Long cusId, Form form) {
        CustomerBillingDetailDTO billingDetailDTO = new CustomerBillingDetailDTO();
        billingDetailDTO.setCustomerId(cusId);
        billingDetailDTO.setAccountFriendlyName(form.asMap().getFirst("accountFriendlyName"));

        if (!AssertObject.isEmpty(form.asMap().getFirst("infoCurrId"))) {
            billingDetailDTO.setInfoCurrId(Long.parseLong(form.asMap().getFirst("infoCurrId")));
        }

        String currencyId = form.asMap().getFirst("billingCurrencyId");
        if (!AssertObject.isEmpty(currencyId)) {
            billingDetailDTO.setBillingCurrencyId(Long.parseLong(currencyId));
        }


        if (!AssertObject.isEmpty(form.asMap().getFirst("billingAccountId"))) {
            billingDetailDTO.setBillingAccountId(Long.parseLong(form.asMap().getFirst("billingAccountId")));
        }

        String activationDate = form.asMap().getFirst("activationDate");
        if (!AssertObject.isEmpty(activationDate)) {
            billingDetailDTO.setActivationDate(Utility.stringToDate(activationDate));
        }

        billingDetailDTO.setAccountReference(form.asMap().getFirst("accountReference"));

        String contractId = form.asMap().getFirst("contractId");
        if (!AssertObject.isEmpty(contractId)) {
            billingDetailDTO.setContractId(Long.parseLong(contractId));
        }

        billingDetailDTO.setLanguage(form.asMap().getFirst("invoiceLanguage"));

        String billPeriod = form.asMap().getFirst("billPeriod");
        if (billPeriod != null) {
            String[] periodNUnit = billPeriod.split(" ");
            billingDetailDTO.setBillPeriod(periodNUnit[0].trim());
            if (periodNUnit.length > 1) {
                billingDetailDTO.setBillPeriodUnit(periodNUnit[1]);
            } else {
                billingDetailDTO.setBillPeriodUnit("M");
            }
        }


        billingDetailDTO.setPaymentOption(form.asMap().getFirst("paymentMethod"));
        billingDetailDTO.setCreditClassdays(form.asMap().getFirst("paymentDays"));
        billingDetailDTO.setTouchBillingOption(form.asMap().getFirst("usScenario"));
        billingDetailDTO.setAccountClassification(form.asMap().getFirst("accountClassification"));
        billingDetailDTO.setClientBillRef(form.asMap().getFirst("customerBillingReference"));
        billingDetailDTO.setOverrideTaxExemptRef(form.asMap().getFirst("taxExemptionCode"));
        billingDetailDTO.setOverrideVatNumber(form.asMap().getFirst("vatNumber"));
        billingDetailDTO.setClarityProjectCode(form.asMap().getFirst("clarityProjectCode"));
        try {

            String salesChannel = form.asMap().getFirst("originatorGfrCode");
            //String gfrCode = userManagementRepository.getSalesChannelGfrCode(salesChannel);
          //   String gfrCode=billingAccountResourceClient.getGfrCodeFromSalesChannel(salesChannel);
               String gfrCode= expedioClientResources.getCustomerResource().getGfrCode(salesChannel);
            if (null != gfrCode) {
                billingDetailDTO.setOriginatorGfrCode(gfrCode);
            }

        } catch (Exception e) {
            //log error
            e.printStackTrace();
        }

        billingDetailDTO.setReceiverGrfCode(form.asMap().getFirst("receiverGrfCode"));
        billingDetailDTO.setAccType(form.asMap().getFirst("accType"));
        billingDetailDTO.setOriginatorOuc(form.asMap().getFirst("originatorOuc"));
        billingDetailDTO.setReceiverOuc(form.asMap().getFirst("receiverOuc"));
        billingDetailDTO.setTransfrChgAgrmntCode(form.asMap().getFirst("transfrChgAgrmntCode"));
        billingDetailDTO.setBillingMode(form.asMap().getFirst("billingMode"));
        String city = form.asMap().getFirst(CITY);
        String adrId = form.asMap().getFirst(ADR_ID);
        String state = form.asMap().getFirst(STATE);
        String postCode = form.asMap().getFirst(POST_CODE);

        if (AssertObject.anyNonEmpty(adrId, state, city, postCode)) {
            AddressDTO address = new AddressDTO();

            if (adrId != null) {
                address.setAdrId(new Long(adrId));
            }

            address.setSitePremise(form.asMap().getFirst(BUILDING_NAME));
            address.setSubBuilding(form.asMap().getFirst(SUB_BUILDING));
            address.setStreetNo(form.asMap().getFirst(BUILDING_NUMBER));
            address.setStreetName(form.asMap().getFirst(STREET));
            address.setSubStreet(form.asMap().getFirst(SUB_STREET));
            address.setLocality(form.asMap().getFirst(LOCALITY));
            address.setSubLocality(form.asMap().getFirst(SUB_LOCALITY));
            address.setTown(city);
            address.setCounty(state);
            address.setSubCountyStateProvince(form.asMap().getFirst(SUB_STATE));


            address.setPostZipCode(postCode);
            address.setSubPostCode(form.asMap().getFirst(SUB_POST_CODE));
            address.setPoBoxNo(form.asMap().getFirst(PO_BOX));
            if (StringUtils.isNotBlank(form.asMap().getFirst(LATITUDE)) && StringUtils.isNotBlank(form.asMap().getFirst(LONGITUDE))) {
                address.setLatitude(Double.valueOf(form.asMap().getFirst(LATITUDE)));
                address.setLongitude(Double.valueOf(form.asMap().getFirst(LONGITUDE)));
            }

            address.setSitePremise(form.asMap().getFirst(BUILDING_NAME));

            address.setSubStreet(form.asMap().getFirst(SUB_STREET));

            String countryName = form.asMap().getFirst("country[name]");
            String countryAlpha2Code = form.asMap().getFirst("country[codeAlpha2]");

            if (AssertObject.anyNonEmpty(countryName, countryAlpha2Code)) {
                CountryDTO country = new CountryDTO();
                country.setName(form.asMap().getFirst("country[name]"));
                country.setCodeAlpha2(form.asMap().getFirst("country[codeAlpha2]"));

                address.setCountry(country);
            }

            billingDetailDTO.setAddress(address);
        }

        String leId = form.asMap().getFirst("leID");


        if (!AssertObject.isEmpty(leId)) {
            LegalEntityDTO leDto = new LegalEntityDTO();

            try {
                leDto.setLeId(Long.parseLong(leId));
            } catch (Exception ex) {
            }
            billingDetailDTO.setLegalEntity(leDto);
        }

        return billingDetailDTO;
    }

}

