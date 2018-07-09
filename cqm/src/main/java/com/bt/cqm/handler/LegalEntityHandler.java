package com.bt.cqm.handler;

import com.bt.cqm.repository.user.CountryVatMapEntity;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.rsqe.customerinventory.dto.le.CusLeDTO;
import com.bt.rsqe.customerinventory.dto.le.LegalEntityDTO;
import com.bt.rsqe.customerinventory.dto.location.AddressDTO;
import com.bt.rsqe.customerinventory.dto.site.CountryDTO;
import com.bt.rsqe.customerinventory.resources.ILegalEntityResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;

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
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 10/15/14
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/cqm/legalEntity")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class LegalEntityHandler {

    private static final String CUSTOMER_ID = "customerId";
    private static final String LE_ID = "leId";
    private static final String LE_ASSOCIATION_TYPE = "leAssociationType";
    private static final String SM_USER = "SM_USER";
    private ILegalEntityResource resource;
    private UserManagementRepository cqmRepository;

    public LegalEntityHandler(ILegalEntityResource resource, UserManagementRepository cqmRepository) {
        this.resource = resource;
        this.cqmRepository = cqmRepository;
    }

    @GET
    public Response getLegalEntities(@QueryParam("customerID") Long customerId) {
        GenericEntity<List<CusLeDTO>> genericLEList = null;

        if (customerId == null) {
            return ResponseBuilder.badRequest().build();
        }

        try {
            List<CusLeDTO> cusLeDTOs = resource.getLegalEntities(customerId);

            if (cusLeDTOs == null || cusLeDTOs.size() < 1) {
                return ResponseBuilder.notFound().build();
            }

            genericLEList = new GenericEntity<List<CusLeDTO>>(cusLeDTOs) {
            };
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }
        return ResponseBuilder.anOKResponse().withEntity(genericLEList).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/create")
    public Response createLegalEntity(@HeaderParam(SM_USER) String userId, @QueryParam(CUSTOMER_ID) String cusId, @QueryParam("salesChannel") String channel, Form form) {
        String leId = null;

        if (AssertObject.isEmpty(userId) || AssertObject.isEmpty(cusId)
            || AssertObject.isEmpty(channel) || form == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LegalEntityDTO legalEntityDTO = processFormData(cusId, form);
        if (AssertObject.isNull(legalEntityDTO)) {
            return ResponseBuilder.badRequest().withEntity("NULL Input").build();
        }

        try {
            leId = resource.createLegalEntity(userId, legalEntityDTO);
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }

        return ResponseBuilder.anOKResponse().withEntity(leId).build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/update")
    public Response updateLegalEntity(@HeaderParam(SM_USER) String userId, @QueryParam(CUSTOMER_ID) String cusId, @QueryParam("salesChannel") String channel, Form form) {
        GenericEntity<Boolean> genericLEList;

        if (AssertObject.isEmpty(userId) || AssertObject.isEmpty(cusId)
            || AssertObject.isEmpty(channel) || form == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LegalEntityDTO legalEntityDTO = processFormData(cusId, form);
        try {
            Boolean status = resource.updateLegalEntity(userId, legalEntityDTO);
            genericLEList = new GenericEntity<Boolean>(status) {
            };
        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }

        return ResponseBuilder.anOKResponse().withEntity(genericLEList).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/createlinktocustomer")
    public Response createLegalEntityToCustomer(@HeaderParam(SM_USER) String userId, @QueryParam(CUSTOMER_ID) String customerId, @QueryParam(LE_ID) String leId, @QueryParam(LE_ASSOCIATION_TYPE) String leAssociationType) {


        if (AssertObject.isNull(customerId) || AssertObject.isNull(leId) || AssertObject.isNull(leAssociationType)) {
            return ResponseBuilder.badRequest().withEntity("NULL Input").build();
        }
        try {
            CusLeDTO cusLeDTO = new CusLeDTO();
            cusLeDTO.setCusId(Long.parseLong(customerId));
            cusLeDTO.setLeId(Long.parseLong(leId));
            cusLeDTO.setLeAssociationType(leAssociationType);
            resource.createLegalEntityLinkToCustomer(userId, cusLeDTO);

        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }

        return ResponseBuilder.anOKResponse().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/updatelinktocustomer")
    public Response updateLegalEntityToCustomer(@HeaderParam(SM_USER) String userId, @QueryParam(CUSTOMER_ID) String customerId, @QueryParam(LE_ID) String leId, @QueryParam(LE_ASSOCIATION_TYPE) String leAssociationType, @QueryParam("oldLeId") String oldLeId) {

        if (AssertObject.isNull(userId) || AssertObject.isNull(customerId) || AssertObject.isNull(leId) || AssertObject.isNull(leAssociationType) || AssertObject.isNull(oldLeId)) {
            return ResponseBuilder.badRequest().withEntity("NULL Input").build();
        }


        try {
            CusLeDTO cusLeDTO = new CusLeDTO();
            cusLeDTO.setCusId(Long.parseLong(customerId));
            cusLeDTO.setLeId(Long.parseLong(leId));
            cusLeDTO.setLeAssociationType(leAssociationType);
            //Same Le Id needs to be passed for both new and old Le id fields.
            resource.updateLegalEntityLinkToCustomer(userId, cusLeDTO, Long.parseLong(customerId), Long.parseLong(leId));

        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }

        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Path("/vatnumbervalidation")
    @Produces(MediaType.TEXT_PLAIN)
    public Response validateVatNo(@QueryParam("countryName") String countryName) {
        //remove unnecessary space from country name.
        String[] strArray = countryName.split(" ");
        StringBuffer sb = new StringBuffer();
        for (String s : strArray) {//remove doubel space from countryName.
            if (s != null && !s.equals("") && !s.equals(" ")) {
                sb.append(s).append(" ");
            }
        }
        countryName = sb.toString().trim().toUpperCase();

        CountryVatMapEntity countryVatMapEntity = cqmRepository.getCountryVatPrefix(countryName);
        if (AssertObject.isNull(countryVatMapEntity)) {
            return ResponseBuilder.anOKResponse().withEntity(null).build();
        } else {
            String vatPrefix = countryVatMapEntity.getVatPrefix();
            return ResponseBuilder.anOKResponse().withEntity(vatPrefix).build();
        }
    }

    private LegalEntityDTO processFormData(String cusId, Form form) {

        LegalEntityDTO legalEntityDTO = new LegalEntityDTO();
        AddressDTO addressDTO = new AddressDTO();
        CountryDTO countryDTO = new CountryDTO();
        // we should rename the field properly to local company name
        // legalEntityDTO.setUserId(userId);
        if (form.asMap().getFirst(LE_ID) != null && !"".equals(form.asMap().getFirst(LE_ID).trim())) {
            legalEntityDTO.setLeId(Long.parseLong(form.asMap().getFirst(LE_ID)));
        }
        legalEntityDTO.setCusId(Long.parseLong(cusId));
        legalEntityDTO.setLeName(form.asMap().getFirst("legalCompanyName"));
        addressDTO.setSitePremise(form.asMap().getFirst("buildingName"));
        addressDTO.setSubBuilding(form.asMap().getFirst("subBuilding"));
        addressDTO.setStreetNo(form.asMap().getFirst("buildingNumber"));
        addressDTO.setStreetName(form.asMap().getFirst("street"));
        addressDTO.setSubStreet(form.asMap().getFirst("subStreet"));
        addressDTO.setLocality(form.asMap().getFirst("locality"));
        addressDTO.setSubLocality(form.asMap().getFirst("subLocality"));
        addressDTO.setTown(form.asMap().getFirst("city"));
        addressDTO.setCounty(form.asMap().getFirst("state"));
        addressDTO.setSubCountyStateProvince(form.asMap().getFirst("subState"));
        countryDTO.setName(form.asMap().getFirst("country[name]"));
        countryDTO.setCodeAlpha2(form.asMap().getFirst("country[codeAlpha2]"));
        addressDTO.setCountry(countryDTO);
        addressDTO.setCountryName(form.asMap().getFirst("country[name]"));
        addressDTO.setPostZipCode(form.asMap().getFirst("postCode"));
        addressDTO.setSubPostCode(form.asMap().getFirst("subPostCode"));
        addressDTO.setPoBoxNo(form.asMap().getFirst("POBox"));
        // Commented  seems to be defect in BFG, for Every Update New address is getting created. No updation of the existing address.
        /*    if (form.asMap().getFirst("adrId") != null && !"".equals(form.asMap().getFirst("adrId").trim())) {
            addressDTO.setAdrId(Long.parseLong(form.asMap().getFirst("adrId")));
        }*/
        addressDTO.setAdrId(null);
        legalEntityDTO.setLeVatNo(form.asMap().getFirst("vatNo"));
        legalEntityDTO.setLeTaxExemptRef(form.asMap().getFirst("taxRef"));

        legalEntityDTO.setAddress(addressDTO);
        legalEntityDTO.setCompRegNo(form.asMap().getFirst("compRegNo"));
        return legalEntityDTO;

    }
}
