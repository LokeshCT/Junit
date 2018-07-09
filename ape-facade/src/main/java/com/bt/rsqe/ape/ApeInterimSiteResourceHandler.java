package com.bt.rsqe.ape;

import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeInterimSiteDTO;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.UserDTO;
import com.google.common.base.Strings;
import static org.apache.commons.lang.StringUtils.*;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import static com.bt.rsqe.utils.AssertObject.*;

@Path("/rsqe/ape-facade/interim-site")
public class ApeInterimSiteResourceHandler {

    private final APEClient apeClient;

    public ApeInterimSiteResourceHandler(APEClient apeClient ) {
        this.apeClient = apeClient;
    }

    @POST
    public Response getInterimSite(ApeQrefRequestDTO requestDTO){

        SqeAccessInputDetails inputDetails = getInputsFromSite(requestDTO.siteDetail());
        setUserDetails(requestDTO.user(),inputDetails);
        setCustomerDetails(requestDTO.customerDetail(),inputDetails);
        MultisiteResponse multisiteResponse = apeClient.provideQuoteForGlobalPricing(inputDetails);

        GenericEntity<ApeInterimSiteDTO> stubEntity = new GenericEntity<ApeInterimSiteDTO>( toDto(multisiteResponse) ) {
        };
        return ResponseBuilder.anOKResponse().withEntity(stubEntity).build();
    }

    private SqeAccessInputDetails getInputsFromSite(SiteDTO siteDTO) {

        SqeAccessInputDetails inputDetails = new SqeAccessInputDetails();

        SqeQuoteInputDetails quoteInput = new SqeQuoteInputDetails();
        inputDetails.setSyncURI("");

        //Site Details
        quoteInput.setSiteName(siteDTO.getSiteName());
        quoteInput.setCity(siteDTO.getCity());
        quoteInput.setCountryName(siteDTO.getCountryName());
        quoteInput.setPostCode(siteDTO.getPostCode());
        quoteInput.setKgiData(siteDTO.getPostCode() + " ");
        quoteInput.setTelephoneAreaCode(siteDTO.getTelephoneAreaCode());
        quoteInput.setStreetNo(siteDTO.getStreetNumber());
        quoteInput.setTelephoneNo(siteDTO.getTelephoneNumber());
        quoteInput.setLocalCompanyName(siteDTO.getLocalCompanyName());
        quoteInput.setCountryISOCode(siteDTO.getCountryISOCode());
        quoteInput.setCountyStateProvince(siteDTO.getProvince());
        quoteInput.setSubLocality(siteDTO.getSubLocality());
        quoteInput.setBuildingNumber(siteDTO.buildingNumber);
        quoteInput.setBuilding(siteDTO.getBuilding());
        quoteInput.setSubStreet(siteDTO.getSubStreet());
        quoteInput.setSubBuilding(siteDTO.getSubBuilding());
        quoteInput.setSubCountyStateProvince(siteDTO.subStateCountyProvince);
        quoteInput.setPostalOrganisation(siteDTO.postalOrg);
        quoteInput.setPOBox(siteDTO.postBox);
        quoteInput.setStateCode(siteDTO.stateCode);
        quoteInput.setStreet(siteDTO.getStreetName());
        quoteInput.setLocality(siteDTO.getLocality());
        quoteInput.setAccuracylevel(siteDTO.getAccuracyLevel());
        quoteInput.setMBPFlag(MBPFlag.MBP);
        quoteInput.setQuoteType(QuoteType.Price);
        quoteInput.setRequestType(RequestType.Standard);
        String latitude = siteDTO.getLatitude();
        quoteInput.setLatitude(Double.valueOf(Strings.isNullOrEmpty(latitude) ? "0" : latitude));
        String longitude = siteDTO.getLongitude();
        quoteInput.setLongitude(Double.valueOf(Strings.isNullOrEmpty(longitude) ? "0" : longitude));

        quoteInput.setInterimFlag(InterimFlag.Interim);

        quoteInput.setApply_fcm(FCMOption.No);
        quoteInput.setApply_fcm_include_y1(FCMOption.No);

        SqeQuoteInputDetails[] quoteInputDetails = new SqeQuoteInputDetails[1];
        quoteInputDetails[0] = quoteInput;
        inputDetails.setQuoteInput(quoteInputDetails);

        return inputDetails;
    }

    private void setUserDetails(UserDTO user, SqeAccessInputDetails sqeAccessInput) {
        SqeUserDetails sqeUserDetails = new SqeUserDetails();
        sqeUserDetails.setSalesUserFirstName(user.getForename());
        sqeUserDetails.setSalesUserLastName(user.getSurname());
        sqeUserDetails.setSalesUserEmailID(user.getEmail());
        sqeUserDetails.setSalesUserPhoneNo(user.getPhoneNumber());
        sqeUserDetails.setSalesChannel(user.getSalesRepName());
        String ein = user.getEin();
        sqeUserDetails.setSalesUserEin(Strings.isNullOrEmpty(ein) ? 0 : Integer.parseInt(ein.trim()));
        sqeAccessInput.setSqeUserDetails(sqeUserDetails);
    }

    private void setCustomerDetails(CustomerDTO customerDTO, SqeAccessInputDetails sqeAccessInput) {
        sqeAccessInput.setCustomerName(customerDTO.name);
        sqeAccessInput.setType("Customer");
        sqeAccessInput.setDistributorID(customerDTO.getGfrCode());
    }

    private ApeInterimSiteDTO toDto(MultisiteResponse multisiteResponse) {

        String interimSiteRequestId= EMPTY;
        String comments = EMPTY;

        if(isNotNull(multisiteResponse)){
            if(!multisiteResponse.getComments().contains("[Failure]")){
                interimSiteRequestId = multisiteResponse.getRequestId();
            }
        }
        return new ApeInterimSiteDTO(interimSiteRequestId,comments);
    }
}
