package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.BendSitedetails;
import com.bt.rsqe.ape.FCMOption;
import com.bt.rsqe.ape.InterimFlag;
import com.bt.rsqe.ape.MBPFlag;
import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.QuoteType;
import com.bt.rsqe.ape.RequestType;
import com.bt.rsqe.ape.SqeAccessInputDetails;
import com.bt.rsqe.ape.SqeQuoteInputDetails;
import com.bt.rsqe.ape.SqeUserDetails;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.Bandwidth;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.utils.GsonUtil;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

public class ProvideQuoteForGlobalPricingStrategy extends QrefScenarioStrategy {
    private final JsonObject request;
    private CustomerResource customerResource;

    public ProvideQuoteForGlobalPricingStrategy(ApeQrefRequestDTO requestDTO, String syncUri,CustomerResource customerResource) {
        super(requestDTO, syncUri);
        String json = new Gson().toJson(requestDTO);
        request = new Gson().fromJson(json, JsonElement.class).getAsJsonObject();
        this.customerResource = customerResource;
    }

    public SqeAccessInputDetails transform() {
        SqeAccessInputDetails sqeAccessInput = new SqeAccessInputDetails();
        sqeAccessInput.setQuoteInput(new SqeQuoteInputDetails[]{new SqeQuoteInputDetails()});
        sqeAccessInput.setSqeUserDetails(new SqeUserDetails());
        sqeAccessInput.setSyncURI(getSyncUri());

        setCustomerDetails(sqeAccessInput);
        setSiteDetails(sqeAccessInput);
        setAssetDetails(sqeAccessInput);
        setUserDetails(sqeAccessInput);

        return sqeAccessInput;
    }

    private void setCustomerDetails(SqeAccessInputDetails sqeAccessInput) {
        sqeAccessInput.setTerm(toYears(getAttributeValue(ProductOffering.ACCESS_CONTRACT_TERM)));
        sqeAccessInput.setCustomerName(getStringValueFromPath(request, "customer.name"));
        sqeAccessInput.setType("Customer");
        sqeAccessInput.setDistributorID(getStringValueFromPath(request, "customer.gfrCode"));
    }

    private String toYears(String months) {
        return !Strings.isNullOrEmpty(months) ? String.valueOf(Integer.parseInt(months) / 12) : null;
    }

    private void setSiteDetails(SqeAccessInputDetails sqeAccessInput) {
        SqeQuoteInputDetails sqeQuoteInput = sqeAccessInput.getQuoteInput()[0];
        sqeQuoteInput.setBfgSiteId(getStringValueFromPath(request, "site.bfgSiteID"));
        sqeQuoteInput.setSiteName(getStringValueFromPath(request, "site.name"));
        sqeQuoteInput.setCity(getStringValueFromPath(request, "site.city"));
        sqeQuoteInput.setCountryName(getStringValueFromPath(request, "site.country"));
        sqeQuoteInput.setPostCode(getStringValueFromPath(request, "site.postCode"));
        sqeQuoteInput.setKgiData(getStringValueFromPath(request, "site.postCode") + " ");
        sqeQuoteInput.setTelephoneAreaCode(getStringValueFromPath(request, "site.telephoneAreaCode"));
        sqeQuoteInput.setStreetNo(getStringValueFromPath(request, "site.buildingNumber"));
        sqeQuoteInput.setTelephoneNo(getStringValueFromPath(request, "site.phoneNumber"));
        sqeQuoteInput.setLocalCompanyName(getStringValueFromPath(request, "site.localCompanyName"));
        sqeQuoteInput.setCountryISOCode(getStringValueFromPath(request, "site.countryISOCode"));
        sqeQuoteInput.setCountyStateProvince(getStringValueFromPath(request, "site.stateCountySProvince"));
        sqeQuoteInput.setSubLocality(getStringValueFromPath(request, "site.subLocality"));
        sqeQuoteInput.setBuildingNumber(getStringValueFromPath(request, "site.buildingNumber"));
        sqeQuoteInput.setBuilding(getStringValueFromPath(request, "site.building"));
        sqeQuoteInput.setSubStreet(getStringValueFromPath(request, "site.subStreet"));
        sqeQuoteInput.setSubBuilding(getStringValueFromPath(request, "site.subBuilding"));
        sqeQuoteInput.setSubCountyStateProvince(getStringValueFromPath(request, "site.subStateCountyProvince"));
        sqeQuoteInput.setPostalOrganisation(getStringValueFromPath(request, "site.postalOrg"));
        sqeQuoteInput.setPOBox(getStringValueFromPath(request, "site.postBox"));
        sqeQuoteInput.setStateCode(getStringValueFromPath(request, "site.stateCode"));
        sqeQuoteInput.setStreet(getStringValueFromPath(request, "site.streetName"));
        sqeQuoteInput.setLocality(getStringValueFromPath(request, "site.locality"));
        sqeQuoteInput.setAccuracylevel(Integer.valueOf(getStringValueFromPath(request, "site.accuracyLevel")));
        sqeQuoteInput.setProcessType(getRequest().getProcessType().getType());
        String mbfFlag = getAttributeValue(ProductOffering.PRICING_REQUEST_TYPE);
        if (isNotNull(mbfFlag)) {
            sqeQuoteInput.setMBPFlag(MBPFlag.fromString(mbfFlag));
        }
        String quoteType = getAttributeValue(ProductOffering.QUOTE_TYPE);
        if (isNotNull(quoteType)) {
            sqeQuoteInput.setQuoteType(QuoteType.fromString(quoteType));
        }

        boolean isCustomAccess = "CustomAccess".equals(getAttributeValue(ProductOffering.REQUESTTYPE));
        if(!isCustomAccess){
            sqeQuoteInput.setRequestType(RequestType.Standard);
        }
        else {
            sqeQuoteInput.setRequestType(RequestType.CustomAccess);
        }
        String latitude = getStringValueFromPath(request, "site.latitude");
        sqeQuoteInput.setLatitude(Double.valueOf(Strings.isNullOrEmpty(latitude) ? "0" : latitude));
        String longitude = getStringValueFromPath(request, "site.longitude");
        sqeQuoteInput.setLongitude(Double.valueOf(Strings.isNullOrEmpty(longitude) ? "0" : longitude));

        sqeQuoteInput.setListOfOnNetBuildingCodes(getOnNetBuildings());
        sqeQuoteInput.setSupplierProductDetails(getSupplierProducts());
    }

    private void setAssetDetails(SqeAccessInputDetails sqeAccessInput) {
        boolean isCustomAccess = "CustomAccess".equals(getAttributeValue(ProductOffering.REQUESTTYPE));
        Bandwidth primaryAccessSpeed = null;
        Bandwidth secondaryAccessSpeed = null;
        ArrayList<String> accessSpeedList = new ArrayList<String>();
        sqeAccessInput.setAutoSelection(getAttributeValue("", SORT_BY));

        SqeQuoteInputDetails sqeQuoteInput = sqeAccessInput.getQuoteInput()[0];
        sqeQuoteInput.setQuoteCurrency(getStringValueFromPath(request, "currency"));

        String primaryPortServiceSpeed = getPortSpeed(getAttributeValue(EMPTY, newArrayList(ProductOffering.MIN_REQUIRED_SPEED, ProductOffering.PRIMARY_SERVICE_SPEED)));
        String secondaryPortServiceSpeed = getPortSpeed(getAttributeValue(EMPTY, ProductOffering.SECONDARY_SERVICE_SPEED));

        Bandwidth primaryPortBandwidth = Bandwidth.parse(primaryPortServiceSpeed);
        Bandwidth secondaryPortBandwidth = Bandwidth.parse(secondaryPortServiceSpeed);

        if(validateAccessSpeedFormat(getAttributeValue(EMPTY, newArrayList(ProductOffering.PRIMARY_ACCESS_SPEED)),
                                  getAttributeValue(EMPTY, newArrayList(ProductOffering.SECONDARY_ACCESS_SPEED))))
        {
            accessSpeedList =  extractAccessSpeedandUOM(getAttributeValue(EMPTY, newArrayList(ProductOffering.PRIMARY_ACCESS_SPEED)),
                                     getAttributeValue(EMPTY, newArrayList(ProductOffering.SECONDARY_ACCESS_SPEED)));
            if(accessSpeedList.size()>0){
                sqeQuoteInput.setAccessSpeed1(accessSpeedList.get(0));
                sqeQuoteInput.setAccessSpeedUom1(accessSpeedList.get(1));
            }
        }
       else {
        primaryAccessSpeed = Bandwidth.parse(getAttributeValue(EMPTY, newArrayList(ProductOffering.PRIMARY_ACCESS_SPEED)));
        secondaryAccessSpeed = Bandwidth.parse(getAttributeValue(EMPTY, newArrayList(ProductOffering.SECONDARY_ACCESS_SPEED)));

        sqeQuoteInput.setAccessSpeed1(primaryAccessSpeed.isNull() ? "-1" : primaryAccessSpeed.getUnitStringValue());
        sqeQuoteInput.setAccessSpeedUom1(primaryAccessSpeed.isNull() || Strings.isNullOrEmpty(primaryAccessSpeed.getUOM()) ? "Kbps" : primaryAccessSpeed.getUOM());
        sqeQuoteInput.setAccessSpeed2(secondaryAccessSpeed.isNull() ? "-1" : secondaryAccessSpeed.getUnitStringValue());
        sqeQuoteInput.setAccessSpeedUom2(secondaryAccessSpeed.isNull() || Strings.isNullOrEmpty(secondaryAccessSpeed.getUOM()) ? "Kbps" : secondaryAccessSpeed.getUOM());

        if (isNotNull(primaryAccessSpeed.getTechnology())) {
              sqeQuoteInput.setAccessTechnology(primaryAccessSpeed.getTechnology());
           }

        if (isNotNull(secondaryAccessSpeed.getTechnology())) {
              sqeQuoteInput.setAccessTechnology2(secondaryAccessSpeed.getTechnology());
            }
        }
        String diversity = getAttributeValue(ProductOffering.DIVERSITY);
        String productDiversity = getAttributeValue(ProductOffering.PRODUCT_DIVERSITY);
        sqeQuoteInput.setResilient(!Strings.isNullOrEmpty(diversity) && !DIVERSITY_STANDARD.equalsIgnoreCase(diversity));
        sqeQuoteInput.setProductName1(getAttributeValue(EMPTY, PRODUCT_NAME));
        sqeQuoteInput.setProductID(getAttributeValue(EMPTY, PRODUCT_ID));

        sqeQuoteInput.setCPEResilience(getAttributeValue(EMPTY, CPE_RESILIENCE));

        setPrimaryLegConfiguration(sqeQuoteInput);

        if(isDiverse(diversity)){
            setSecondaryLegConfiguration(sqeQuoteInput);
        }
        if(isCustomAccess){
            sqeQuoteInput.setBFD(null);
            sqeQuoteInput.setSiteAvalibiltyTarget(null);
            sqeQuoteInput.setInterimSiteId("-1");
            sqeQuoteInput.setInterimFlag(InterimFlag.Final);
            sqeQuoteInput.setDualHouseEntry(Boolean.FALSE);
            sqeQuoteInput.setPortSpeed1(null);
            sqeQuoteInput.setPortSpeedUom1(null);
            sqeQuoteInput.setColo(null);
            sqeQuoteInput.setPortSpeed2(null);
            sqeQuoteInput.setPortSpeedUom2(null);
            sqeQuoteInput.setAccess_Type_Name(getAttributeValue(EMPTY, ProductOffering.PRIMARY_ACCESS_TECHNOLOGY));
            sqeQuoteInput.setLeg2_Access_Type_Name(getAttributeValue(EMPTY, ProductOffering.SECONDARY_ACCESS_TECHNOLOGY));
            sqeQuoteInput.setResiliencyType(diversity);
            sqeQuoteInput.setProductSLA(diversity);
            sqeQuoteInput.setApply_fcm(FCMOption.No);
            sqeQuoteInput.setApply_fcm_include_y1(FCMOption.No);
            sqeQuoteInput.setAccessTechnology(getAttributeValue(EMPTY, ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE));
            sqeQuoteInput.setDSSEnabled(Boolean.FALSE);
            sqeQuoteInput.setAccessTechnology2(getAttributeValue(EMPTY, ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE));
            sqeQuoteInput.setPer100DiversitySeparacyRequired(Boolean.FALSE);
            sqeQuoteInput.setMultipleCarriersRequired(Boolean.FALSE);
            sqeQuoteInput.setNetworkDiversityRequired(Boolean.FALSE);
            sqeQuoteInput.setIPv4(getAttributeValue("N", IPV4));
            sqeQuoteInput.setIPv6(getAttributeValue("N", IPV6));
            sqeQuoteInput.setFastConvergence(null);
            sqeQuoteInput.setCustomerProvidedAccess(null);


            //site1 of bend
            sqeQuoteInput.setBendsite1(setBendSiteDetails("bEndSiteID"));
            sqeQuoteInput.setBendsite2(setBendSiteDetails("secondaryBEndSiteID"));
            sqeQuoteInput.setAcfTag(getAttributeValue(EMPTY, ProductOffering.ACF_TAG));
            sqeQuoteInput.setListOfPhysicalConnector(getAttributeArrayValue(ProductOffering.PRIMARY_CONNECTOR));
            sqeQuoteInput.setListOfPhysicalConnectorLeg2(getAttributeArrayValue(ProductOffering.SECONDARY_CONNECTOR));
            sqeQuoteInput.setListOfInterfaceType(getAttributeArrayValue(ProductOffering.PRIMARY_INTERFACE_TYPE));
            sqeQuoteInput.setListOfInterfaceTypeLeg2(getAttributeArrayValue(ProductOffering.SECONDARY_INTERFACE_TYPE));
            sqeQuoteInput.setFramingType(getAttributeValue(EMPTY, ProductOffering.PRIMARY_FRAMING));
            sqeQuoteInput.setFramingTypeLeg2(getAttributeValue(EMPTY, ProductOffering.SECONDARY_FRAMING));
            sqeQuoteInput.setBundled(getAttributeValue(EMPTY, ProductOffering.PRIMARY_BUNDLED));
            sqeQuoteInput.setIpAddressAssignment(getAttributeValue(EMPTY, ProductOffering.PRIMARY_IP_ADDRESS_ASSIGNMENT));
            sqeQuoteInput.setDslTelephoneNumber(getAttributeValue(EMPTY, ProductOffering.PRIMARY_DSL_TELEPHONE_NUMBER));
            sqeQuoteInput.setNumberOfIpAddress(getAttributeValue(EMPTY, ProductOffering.PRIMARY_NUMBER_OF_IP_ADDRESS));
            sqeQuoteInput.setServiceLevel(getAttributeValue(EMPTY, ProductOffering.PRIMARY_SERVICE_LEVEL));
            sqeQuoteInput.setContentionRatio(getAttributeValue(EMPTY, ProductOffering.PRIMARY_CONTENTION_RATIO));
            sqeQuoteInput.setNumberOfChannels(getAttributeValue(EMPTY, ProductOffering.PRIMARY_NUMBER_OF_CHANNELS));
            //String BendID = getAttributeValue(EMPTY, ProductOffering.PRIMARY_BEND_SITE_ID);

        } else{
            sqeQuoteInput.setCustomerProvidedAccess(getAttributeValue(EMPTY, CUSTOMER_PROVIDED_ACCESS));
            sqeQuoteInput.setFastConvergence(getAttributeValue(EMPTY, FAST_CONVERGENCE));
            sqeQuoteInput.setBFD(getAttributeValue(EMPTY, BFD));
            sqeQuoteInput.setColo(getAttributeValue(EMPTY, COLO));
            sqeQuoteInput.setSiteAvalibiltyTarget(productDiversity);
            String interimSiteId = getAttributeValue(EMPTY, ProductOffering.INTERIM_SITE_ID);
            sqeQuoteInput.setInterimSiteId(interimSiteId);
            sqeQuoteInput.setInterimFlag(getInterimFlag(interimSiteId));
            sqeQuoteInput.setDualHouseEntry(getAttributeBooleanValue(getAttributeValue(EMPTY, DUAL_HOUSE_ENTRY)));
            sqeQuoteInput.setAccessTechnology2(getAttributeValue(EMPTY, ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE));
            sqeQuoteInput.setPer100DiversitySeparacyRequired(getAttributeBooleanValue(getAttributeValue(EMPTY, SEPARACY_REQUIRED)));
            sqeQuoteInput.setMultipleCarriersRequired(getAttributeBooleanValue(getAttributeValue(EMPTY, MULTIPLE_CARRIERS_REQUIRED)));
            sqeQuoteInput.setNetworkDiversityRequired(getAttributeBooleanValue(getAttributeValue(EMPTY, NETWORK_DIVERSITY_REQUIRED)));

            sqeQuoteInput.setPortSpeed1(primaryPortBandwidth.isNull() ? "-1" : primaryPortBandwidth.getUnitStringValue());
            sqeQuoteInput.setPortSpeedUom1(primaryPortBandwidth.isNull() || Strings.isNullOrEmpty(primaryPortBandwidth.getUOM()) ? "Kbps" : primaryPortBandwidth.getUOM());

            sqeQuoteInput.setPortSpeed2(secondaryPortBandwidth.isNull() ? "-1" : secondaryPortBandwidth.getUnitStringValue());
            sqeQuoteInput.setPortSpeedUom2(secondaryPortBandwidth.isNull() || Strings.isNullOrEmpty(secondaryPortBandwidth.getUOM()) ? "Kbps" : secondaryPortBandwidth.getUOM());
            sqeQuoteInput.setAccess_Type_Name(getAttributeValue(EMPTY, ProductOffering.PRIMARY_ACCESS_TECHNOLOGY));
            sqeQuoteInput.setLeg2_Access_Type_Name(getAttributeValue(EMPTY, ProductOffering.SECONDARY_ACCESS_TECHNOLOGY));
            sqeQuoteInput.setResiliencyType(diversity);
            sqeQuoteInput.setProductSLA(!Strings.isNullOrEmpty(diversity) && !DIVERSITY_STANDARD.equalsIgnoreCase(diversity) ? diversity : null);
            sqeQuoteInput.setApply_fcm(FCMOption.No);
            sqeQuoteInput.setApply_fcm_include_y1(FCMOption.No);
            sqeQuoteInput.setAccessTechnology(getAttributeValue(EMPTY, ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE));
            sqeQuoteInput.setDSSEnabled(getDssEnabledFlag());
            sqeQuoteInput.setIPv4(getAttributeValue("Yes", IPV4));
            sqeQuoteInput.setIPv6(getAttributeValue("No", IPV6));
        }
    }

    private BendSitedetails setBendSiteDetails(String bEndSiteID) {
        BendSitedetails sitedetails = new BendSitedetails();
        String bEndSiteId = getStringValueFromPath(request, bEndSiteID);
        if(!bEndSiteId.isEmpty()){
                SiteResource siteResource = customerResource.siteResource(getStringValueFromPath(request, "site.customerId"));
                SiteDTO siteDTO = siteResource.getSiteDetails(bEndSiteId);
        if(siteDTO != null){
            sitedetails.setBendSiteid(siteDTO.bfgSiteID);
            sitedetails.setSiteName(siteDTO.getSiteName());
            sitedetails.setCountryName(siteDTO.getCountryName());
            sitedetails.setCountryISOCode(siteDTO.getCountryISOCode());
            sitedetails.setCity(siteDTO.getCity());
            sitedetails.setPostCode(siteDTO.getPostCode());
            sitedetails.setLatitudeBend(siteDTO.getLatitude());
            sitedetails.setLongitudeBend(siteDTO.getLongitude());
            sitedetails.setAccuracylevel(Integer.toString(siteDTO.getAccuracyLevel()));
            sitedetails.setSubLocality(siteDTO.getSubLocality());
            sitedetails.setBuildingNumber(siteDTO.getBuildingNumber());
            sitedetails.setBuilding(siteDTO.getBuilding());
            sitedetails.setSubStreet(siteDTO.getSubStreet());
            sitedetails.setSubBuilding(siteDTO.getSubBuilding());
            sitedetails.setSubCountyStateProvince(siteDTO.getStateCountySProvince());
            sitedetails.setPostalOrganisation(siteDTO.getPostalOrg());
            sitedetails.setPOBox(siteDTO.getPostBox());
            sitedetails.setStateCode(siteDTO.getStateCode());
            sitedetails.setStreet(siteDTO.getStreetName());
            sitedetails.setLocality(siteDTO.getLocality());
           }
        }
        else {
            sitedetails.setBendSiteid(null);
            sitedetails.setSiteName(null);
            sitedetails.setCountryName(null);
            sitedetails.setCountryISOCode(null);
            sitedetails.setCity(null);
            sitedetails.setPostCode(null);
            sitedetails.setLatitudeBend(null);
            sitedetails.setLongitudeBend(null);
            sitedetails.setAccuracylevel(null);
            sitedetails.setSubLocality(null);
            sitedetails.setBuildingNumber(null);
            sitedetails.setBuilding(null);
            sitedetails.setSubStreet(null);
            sitedetails.setSubBuilding(null);
            sitedetails.setSubCountyStateProvince(null);
            sitedetails.setPostalOrganisation(null);
            sitedetails.setPOBox(null);
            sitedetails.setStateCode(null);
            sitedetails.setStreet(null);
            sitedetails.setLocality(null);
          }
        return sitedetails;
    }

    private void setUserDetails(SqeAccessInputDetails sqeAccessInput) {
        SqeUserDetails sqeUserDetails = sqeAccessInput.getSqeUserDetails();
        sqeUserDetails.setSalesUserFirstName(getStringValueFromPath(request, "user.forename"));
        sqeUserDetails.setSalesUserLastName(getStringValueFromPath(request, "user.surname"));
        sqeUserDetails.setSalesUserEmailID(getStringValueFromPath(request, "user.email"));
        sqeUserDetails.setSalesUserPhoneNo(getStringValueFromPath(request, "user.phoneNumber"));
        sqeUserDetails.setSalesChannel(getStringValueFromPath(request, "customer.salesChannel"));
        String ein = getStringValueFromPath(request, "user.ein");
        sqeUserDetails.setSalesUserEin(Strings.isNullOrEmpty(ein) ? 0 : Integer.parseInt(ein.trim()));
    }

    private void setPrimaryLegConfiguration(SqeQuoteInputDetails sqeQuoteInput) {
        boolean isCustomAccess = "CustomAccess".equals(getAttributeValue(ProductOffering.REQUESTTYPE));
        if(!isCustomAccess){
            sqeQuoteInput.setRouting(getAttributeValue(EMPTY, PRIMARY_ROUTING));
            sqeQuoteInput.setCosAssignment(getAttributeValue(EMPTY, PRIMARY_COS_ASSIGNMENT));
            sqeQuoteInput.setEFreq(getAttributeIntegerValue(getAttributeValue(EMPTY, PRIMARY_EF_REQ)));
            sqeQuoteInput.setAFreq(getAttributeIntegerValue(getAttributeValue(EMPTY, PRIMARY_AF_REF)));
            sqeQuoteInput.setMVPNreq(getAttributeIntegerValue(getAttributeValue(EMPTY, PRIMARY_MVPN_REQ)));
            sqeQuoteInput.setMFS(getAttributeValue(EMPTY, PRIMARY_MFS));
            sqeQuoteInput.setMACAddressLimit(getAttributeValue(EMPTY, PRIMARY_MAC_ADDRESS_LIMIT));
            sqeQuoteInput.setLayer2Protocol(getAttributeValue(EMPTY, PRIMARY_LAYER_2_PROTOCOL));
            sqeQuoteInput.setListOfincludeSupplier(getAttributeArrayValue(PRIMARY_INCLUDE_SUPPLIER_LIST));
            sqeQuoteInput.setListOfexcludeSupplier(getAttributeArrayValue(PRIMARY_EXCLUDE_SUPPLIER_LIST));
            sqeQuoteInput.setListOfGpopInclude(getAttributeArrayValue(PRIMARY_GPOP_INCLUDE_LIST));
            sqeQuoteInput.setListOfGpopExclude(getAttributeArrayValue(PRIMARY_GPOP_EXCLUDE_LIST));
        }
        else{
            sqeQuoteInput.setRouting(null);
            sqeQuoteInput.setCosAssignment(null);
            sqeQuoteInput.setEFreq(getAttributeIntegerValue(null));
            sqeQuoteInput.setAFreq(getAttributeIntegerValue(null));
            sqeQuoteInput.setMVPNreq(0);
            sqeQuoteInput.setMFS(null);
            sqeQuoteInput.setMACAddressLimit(null);
            sqeQuoteInput.setLayer2Protocol(null);
            sqeQuoteInput.setListOfincludeSupplier(null);
            sqeQuoteInput.setListOfexcludeSupplier(null);
            sqeQuoteInput.setListOfGpopInclude(null);
            sqeQuoteInput.setListOfGpopExclude(null);
        }
    }

    private void setSecondaryLegConfiguration(SqeQuoteInputDetails sqeQuoteInput) {
        boolean isCustomAccess = "CustomAccess".equals(getAttributeValue(ProductOffering.REQUESTTYPE));
        if(!isCustomAccess){
            sqeQuoteInput.setRoutingLeg2(getAttributeValue(EMPTY, SECONDARY_ROUTING));
            sqeQuoteInput.setCosAssignmentLeg2(getAttributeValue(EMPTY, SECONDARY_COS_ASSIGNMENT));
            sqeQuoteInput.setEFreqLeg2(getAttributeIntegerValue(getAttributeValue(EMPTY, SECONDARY_EF_REQ)));
            sqeQuoteInput.setAFreqLeg2(getAttributeIntegerValue(getAttributeValue(EMPTY, SECONDARY_AF_REF)));
            sqeQuoteInput.setMVPNreqLeg2(getAttributeIntegerValue(getAttributeValue(EMPTY, SECONDARY_MVPN_REQ)));
            sqeQuoteInput.setMFSLeg2(getAttributeValue(EMPTY, SECONDARY_MFS));  // need a check
            sqeQuoteInput.setMACAddressLimitLeg2(getAttributeValue(EMPTY, SECONDARY_MAC_ADDRESS_LIMIT));
            sqeQuoteInput.setLayer2ProtocolLeg2(getAttributeValue(EMPTY, SECONDARY_LAYER_2_PROTOCOL));
            sqeQuoteInput.setListOfincludeSupplierLeg2(getAttributeArrayValue(SECONDARY_INCLUDE_SUPPLIER_LIST));
            sqeQuoteInput.setListOfexcludeSupplierLeg2(getAttributeArrayValue(SECONDARY_EXCLUDE_SUPPLIER_LIST));
            sqeQuoteInput.setListOfGpopIncludeLeg2(getAttributeArrayValue(SECONDARY_GPOP_INCLUDE_LIST));
            sqeQuoteInput.setListOfGpopExcludeLeg2(getAttributeArrayValue(SECONDARY_GPOP_EXCLUDE_LIST));
        }
        else{
            sqeQuoteInput.setRoutingLeg2(null);
            sqeQuoteInput.setCosAssignmentLeg2(null);
            sqeQuoteInput.setEFreqLeg2(0);
            sqeQuoteInput.setAFreqLeg2(0);
            sqeQuoteInput.setMVPNreqLeg2(0);
            sqeQuoteInput.setMFSLeg2(null);
            sqeQuoteInput.setMACAddressLimitLeg2(null);
            sqeQuoteInput.setLayer2ProtocolLeg2(null);
            sqeQuoteInput.setListOfincludeSupplierLeg2(null);
            sqeQuoteInput.setListOfexcludeSupplierLeg2(null);
            sqeQuoteInput.setListOfGpopIncludeLeg2(null);
            sqeQuoteInput.setListOfGpopExcludeLeg2(null);
        }
    }

    private String getStringValueFromPath(JsonObject jsonObject, String path) {
        return Strings.nullToEmpty(GsonUtil.getStringValueFromPath(jsonObject, path));
    }

    private String getAttributeValue(String defaultValue, String attributeName) {
        return getAttributeValue(defaultValue, newArrayList(attributeName));
    }

    private String getAttributeValue(String defaultValue, List<String> attributeNames) {
        for (String attributeName : attributeNames) {
            String attributeValue = getAttributeValue(attributeName);

            if (!Strings.isNullOrEmpty(attributeValue)) {
                return attributeValue;
            }
        }

        return defaultValue;
    }
    private static final List<String> exemptedPortSpeedValueList = newArrayList("No PortKbps","NoPort Kbps","NoPortKbps","NoPort", "No Port");
    private String getPortSpeed(String portSpeed) {
        if (exemptedPortSpeedValueList.contains(portSpeed)){
            return EMPTY;
        }

        return portSpeed;
    }

    @Override
    protected String getAttributeValue(final String attributeName) {
        if (request.has("assetAttributes")) {
            JsonArray assetAttributes = GsonUtil.findPath(request, "assetAttributes").getAsJsonArray();
            Optional<JsonElement> attribute = tryFind(assetAttributes, new Predicate<JsonElement>() {
                @Override
                public boolean apply(JsonElement input) {
                    return input.getAsJsonObject().get("attributeName").getAsString().equals(attributeName);
                }
            });
            return attribute.isPresent() ? attribute.get().getAsJsonObject().get("attributeValue").getAsString() : null;
        }
        return null;
    }

    @Override
    public MultisiteResponse getMultiSiteResponse(APEClient apeClient) {
        SqeAccessInputDetails sqeAccessInput = transform();
        return apeClient.provideQuoteForGlobalPricing(sqeAccessInput);
    }

    private boolean validateAccessSpeedFormat(String primaryAccessSpeed, String secondaryAccessSpeed){
        boolean formatFlag = false;
        if(primaryAccessSpeed.trim().contains("/")|| secondaryAccessSpeed.trim().contains("/"))
            formatFlag = true;
        return formatFlag;
    }

    private ArrayList<String> extractAccessSpeedandUOM(String primaryAccessSpeed, String secondaryAccessSpeed){
           ArrayList<String> extractedAccessSpeedList = new ArrayList<String>();
           if(!primaryAccessSpeed.isEmpty()){
               String upStreamPrimaryAccessSpeed = primaryAccessSpeed.substring(0,primaryAccessSpeed.indexOf("/"));
               String downStreamPrimaryAccessSpeed = primaryAccessSpeed.substring(primaryAccessSpeed.indexOf("/")+1,primaryAccessSpeed.length());
               Bandwidth upStreamPrimaryAccessSpeedBandwidth = Bandwidth.parse(upStreamPrimaryAccessSpeed.trim());
               Bandwidth downStreamPrimaryAccessSpeedBandwidth = Bandwidth.parse(downStreamPrimaryAccessSpeed.trim());
               extractedAccessSpeedList.add(upStreamPrimaryAccessSpeedBandwidth.getUnitStringValue()+"/"+downStreamPrimaryAccessSpeedBandwidth.getUnitStringValue());
               extractedAccessSpeedList.add(upStreamPrimaryAccessSpeedBandwidth.getUOM()+"/"+downStreamPrimaryAccessSpeedBandwidth.getUOM());
           }
         return extractedAccessSpeedList;
       }
}
