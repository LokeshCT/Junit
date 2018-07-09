package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.AccessConfig;
import com.bt.rsqe.ape.AccessConfigToBe;
import com.bt.rsqe.ape.AendSitedetails;
import com.bt.rsqe.ape.AsIs;
import com.bt.rsqe.ape.Configuration;
import com.bt.rsqe.ape.DetailsAsis;
import com.bt.rsqe.ape.DetailsTobe;
import com.bt.rsqe.ape.LegConfiguration;
import com.bt.rsqe.ape.LegType;
import com.bt.rsqe.ape.MBPFlag;
import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.QuoteType;
import com.bt.rsqe.ape.RequestType;
import com.bt.rsqe.ape.SalesUserDetails;
import com.bt.rsqe.ape.SiteDetailsModify;
import com.bt.rsqe.ape.SqeModifyAccessInputDetails;
import com.bt.rsqe.ape.ToBe;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.dto.AsIsAsset;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.Bandwidth;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.security.UserDTO;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.ape.LegType.*;
import static com.bt.rsqe.ape.source.ActionCodeStrategy.*;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.bt.rsqe.utils.Lists.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

public class ModifyQuoteForGlobalPricingStrategy extends QrefScenarioStrategy {
    private CustomerResource customerResource;
    private static final Map<String, ActionCodeStrategy> ACTION_CODE_MAP = new HashMap<String, ActionCodeStrategy>() {
        {
            put("Update", Update);
            put("None", None);
            put("Add", ActionCodeStrategy.Add);
        }
    };

    private static final Map<ChangeType, ActionCodeStrategy> CHANGE_TYPE_ACTION_CODE_MAP = new HashMap<ChangeType, ActionCodeStrategy>() {
        {
            put(ChangeType.ADD, Add);
            put(ChangeType.NONE, None);
            put(ChangeType.UPDATE, Update);
        }
    };
    public static final List<ActionCodeStrategy> AS_IS_CONFIGURATION_REQUIRING_ACTIONS = newArrayList(Update, None);

    public ModifyQuoteForGlobalPricingStrategy(ApeQrefRequestDTO request, String syncUri, CustomerResource customerResource) {
        super(request, syncUri);
        this.customerResource = customerResource;
    }

    public SqeModifyAccessInputDetails transformToRequest() {
        SqeModifyAccessInputDetails sqeModifyAccessInputDetails = new SqeModifyAccessInputDetails();
        sqeModifyAccessInputDetails.setSites(new SiteDetailsModify[]{new SiteDetailsModify()});
        sqeModifyAccessInputDetails.setSalesUserDetails(new com.bt.rsqe.ape.SalesUserDetails());
        sqeModifyAccessInputDetails.setSyncURI(getSyncUri());
        sqeModifyAccessInputDetails.setReferenceID("");

        setCustomerDetails(sqeModifyAccessInputDetails);
        setSiteDetails(sqeModifyAccessInputDetails);
        setConfigurations(sqeModifyAccessInputDetails);
        setSalesUserDetails(sqeModifyAccessInputDetails);
        return sqeModifyAccessInputDetails;
    }

    private void setCustomerDetails(SqeModifyAccessInputDetails sqeModifyAccessInputDetails) {
        sqeModifyAccessInputDetails.setTerm(toYears(getAttributeValue(ProductOffering.ACCESS_CONTRACT_TERM)));
        sqeModifyAccessInputDetails.setCustomerName(nullToEmpty(getRequest().customerDetail().getName()));
        sqeModifyAccessInputDetails.setType("Customer");
        sqeModifyAccessInputDetails.setDescription("Description");
        sqeModifyAccessInputDetails.setPartnerRequestId("PartnerRequestId");
        sqeModifyAccessInputDetails.setAutoSelection("1");
        sqeModifyAccessInputDetails.setDistributorID(nullToEmpty(getRequest().customerDetail().gfrCode));
        sqeModifyAccessInputDetails.setCountryTelephoneCode(nullToEmpty(getRequest().siteDetail().countryISOCode));
        sqeModifyAccessInputDetails.setTelephoneAreaCode(nullToEmpty(getRequest().siteDetail().stateCode));
        sqeModifyAccessInputDetails.setTelephoneNo(nullToEmpty(getRequest().siteDetail().phoneNumber));
        sqeModifyAccessInputDetails.setProductName(getAttributeValue(EMPTY, PRODUCT_NAME));
        sqeModifyAccessInputDetails.setProductId(getAttributeValue(EMPTY, PRODUCT_ID));
        sqeModifyAccessInputDetails.setQuoteCurrency(nullToEmpty(getRequest().currency()));
        sqeModifyAccessInputDetails.setProcessType(getProcessType());
        String diversity = getAttributeValue(ProductOffering.DIVERSITY);
        sqeModifyAccessInputDetails.setResilient(!Strings.isNullOrEmpty(diversity) && !DIVERSITY_STANDARD.equalsIgnoreCase(diversity));
        sqeModifyAccessInputDetails.setProductSLA(diversity);
        sqeModifyAccessInputDetails.setResiliencyType(diversity);
        String mbfFlag = getAttributeValue(ProductOffering.PRICING_REQUEST_TYPE);
        if (isNotNull(mbfFlag)) {
            sqeModifyAccessInputDetails.setMbpFlag(MBPFlag.fromString(mbfFlag));
        }
        String quoteType = getAttributeValue(ProductOffering.QUOTE_TYPE);
        if (isNotNull(quoteType)) {
            sqeModifyAccessInputDetails.setQuoteType(QuoteType.fromString(quoteType));
        }

        boolean isCustomAccess = "CustomAccess".equals(getAttributeValue(ProductOffering.REQUESTTYPE));
        if(!isCustomAccess){
            sqeModifyAccessInputDetails.setRequestType(RequestType.Standard);
        }
        else {
            sqeModifyAccessInputDetails.setRequestType(RequestType.CustomAccess);
        }
    }

    private String getProcessType() {
        if (getRequest().getProcessType().equals(ApeQrefRequestDTO.ProcessType.MODIFY))
            {
                return resolveStrategyBasedOnAssetAction().equals(ActionCodeStrategy.None) ? "Inlife Renewal" : getRequest().getProcessType().name();
            }
        return getRequest().getProcessType().name();
    }

    private String toYears(String months) {
        return !Strings.isNullOrEmpty(months) ? String.valueOf(Integer.parseInt(months) / 12) : null;
    }

    private void setSiteDetails(SqeModifyAccessInputDetails sqeModifyAccessInputDetails) {
        SiteDTO site = getRequest().siteDetail();

        SiteDetailsModify siteDetailsModify = sqeModifyAccessInputDetails.getSites()[0];
        siteDetailsModify.setAEnd(new AendSitedetails());
        AendSitedetails aEndSiteDetails = siteDetailsModify.getAEnd();

        aEndSiteDetails.setAsIsAend(new AsIs());
        AsIs asIsSiteDetails = aEndSiteDetails.getAsIsAend();

        asIsSiteDetails.setSiteid(nullToEmpty(site.getSiteId().toString()));
        asIsSiteDetails.setSiteName(nullToEmpty(site.getSiteName()));
        asIsSiteDetails.setCountryName(nullToEmpty(site.country));
        asIsSiteDetails.setCountryISOCode(nullToEmpty(site.countryISOCode));
        asIsSiteDetails.setCity(nullToEmpty(site.city));
        asIsSiteDetails.setPostCode(nullToEmpty(site.postCode));
        asIsSiteDetails.setLatitude(site.getLatitude());
        asIsSiteDetails.setLongitude(site.getLongitude());
        asIsSiteDetails.setAccuracylevel(Integer.toString(site.getAccuracyLevel()));
        asIsSiteDetails.setBuildingNumber(nullToEmpty(site.buildingNumber));
        asIsSiteDetails.setBuilding(nullToEmpty(site.building));
        asIsSiteDetails.setSubStreet(nullToEmpty(site.subStreet));
        asIsSiteDetails.setSubBuilding(nullToEmpty(site.subBuilding));
        asIsSiteDetails.setSubCountyStateProvince(nullToEmpty(site.subStateCountyProvince));
        asIsSiteDetails.setPostalOrganisation(nullToEmpty(site.postalOrg));
        asIsSiteDetails.setPoBox(nullToEmpty(site.postBox));
        asIsSiteDetails.setStateCode(nullToEmpty(site.stateCode));
        asIsSiteDetails.setStreet(nullToEmpty(site.streetName));
        asIsSiteDetails.setLocality(nullToEmpty(site.locality));

        if (this.getRequest().getProcessType().equals(ApeQrefRequestDTO.ProcessType.MOVE)) {
            setExistingSiteDetails(aEndSiteDetails);
        } else {
            setExistingSiteToEmpty(aEndSiteDetails);
        }
    }

    private void setExistingSiteToEmpty(AendSitedetails aEndSiteDetails) {
        aEndSiteDetails.setToBeAend(new AsIs());
    }

    private void setExistingSiteDetails(AendSitedetails aEndSiteDetails) {
        SiteDTO oldSite = getRequest().getOldSite();

        aEndSiteDetails.setToBeAend(new AsIs());
        AsIs existingSiteDetails = aEndSiteDetails.getToBeAend();

        existingSiteDetails.setSiteid(nullToEmpty(oldSite.getSiteId().toString()));
        existingSiteDetails.setSiteName(nullToEmpty(oldSite.getSiteName()));
        existingSiteDetails.setCountryName(nullToEmpty(oldSite.country));
        existingSiteDetails.setCountryISOCode(nullToEmpty(oldSite.countryISOCode));
        existingSiteDetails.setCity(nullToEmpty(oldSite.city));
        existingSiteDetails.setPostCode(nullToEmpty(oldSite.postCode));
        existingSiteDetails.setBuildingNumber(nullToEmpty(oldSite.buildingNumber));
        existingSiteDetails.setBuilding(nullToEmpty(oldSite.building));
        existingSiteDetails.setSubStreet(nullToEmpty(oldSite.subStreet));
        existingSiteDetails.setSubBuilding(nullToEmpty(oldSite.subBuilding));
        existingSiteDetails.setSubCountyStateProvince(nullToEmpty(oldSite.subStateCountyProvince));
        existingSiteDetails.setPostalOrganisation(nullToEmpty(oldSite.postalOrg));
        existingSiteDetails.setPoBox(nullToEmpty(oldSite.postBox));
        existingSiteDetails.setStateCode(nullToEmpty(oldSite.stateCode));
        existingSiteDetails.setStreet(nullToEmpty(oldSite.streetName));
        existingSiteDetails.setLocality(nullToEmpty(oldSite.locality));
    }

    private void setConfigurations(SqeModifyAccessInputDetails sqeModifyAccessInputDetails) {
        SiteDetailsModify siteDetailsModify = sqeModifyAccessInputDetails.getSites()[0];
        siteDetailsModify.setLegConfigurations(new LegConfiguration());

        LegConfiguration legConfiguration = siteDetailsModify.getLegConfigurations();

        if (isDiverse()) {
            legConfiguration.setLegDetails(new Configuration[]{new Configuration(), new Configuration()});
            configurePrimaryLeg(legConfiguration.getLegDetails()[0]);
            configureSecondaryLeg(legConfiguration.getLegDetails()[1]);
        } else {
            legConfiguration.setLegDetails(new Configuration[]{new Configuration()});
            configurePrimaryLeg(legConfiguration.getLegDetails()[0]);
        }
    }

    private void configureSecondaryLeg(Configuration configuration) {
        ActionCodeStrategy actionCodeStrategy = hasAttribute(PRIMARY_ACTION_CODE)
            ? getActionCodeStrategy(Primary, getAttributeValue(PRIMARY_ACTION_CODE))
            : resolveStrategyBasedOnAssetAction();

        boolean isMove = this.getRequest().getProcessType().equals(ApeQrefRequestDTO.ProcessType.MOVE);
        actionCodeStrategy = isMove ? ActionCodeStrategy.Move : actionCodeStrategy;
        setupLeg(configuration, Secondary, actionCodeStrategy);

        configuration.setDetailsAsis(new DetailsAsis());
        DetailsAsis detailsAsis = configuration.getDetailsAsis();
        configureAsIsSite(detailsAsis, getRequest().getBendSecondarySiteID());    // move scenario it has to be AsIs BendSecondarySiteId
        configureAsIsConfig(getRequest().getAsIsAsset(), configuration, Secondary);

        configuration.setDetailsTobe(new DetailsTobe());
        DetailsTobe detailsTobe = configuration.getDetailsTobe();
        if (isMove) {
            configureToBeSite(detailsTobe, getRequest().getBendSecondarySiteID());
        } else {
            configureToBeSiteEmpty(detailsTobe);
        }
        configureToBeConfig(configuration, Secondary);
    }

    private void configurePrimaryLeg(Configuration configuration) {
        ActionCodeStrategy actionCodeStrategy = hasAttribute(PRIMARY_ACTION_CODE)
            ? getActionCodeStrategy(Primary, getAttributeValue(PRIMARY_ACTION_CODE))
            : resolveStrategyBasedOnAssetAction();

        boolean isMove = this.getRequest().getProcessType().equals(ApeQrefRequestDTO.ProcessType.MOVE);
        actionCodeStrategy = isMove ? ActionCodeStrategy.Move : actionCodeStrategy;
        setupLeg(configuration, Primary, actionCodeStrategy);

        configuration.setDetailsAsis(new DetailsAsis());
        DetailsAsis detailsAsis = configuration.getDetailsAsis();
        configureAsIsSite(detailsAsis, getRequest().getBendPrimarySiteID());    // move scenario it has to be AsIs BendPrimarySiteId
        configureAsIsConfig(getRequest().getAsIsAsset(), configuration, Primary);

        configuration.setDetailsTobe(new DetailsTobe());
        DetailsTobe detailsTobe = configuration.getDetailsTobe();
        if (isMove) {
            configureToBeSite(detailsTobe, getRequest().getBendPrimarySiteID());
        } else {
            configureToBeSiteEmpty(detailsTobe);
        }
        configureToBeConfig(configuration, Primary);
    }

    private void configureToBeSiteEmpty(DetailsTobe detailsTobe) {
        detailsTobe.setToBeBendSite(new ToBe());
    }

    private void configureAsIsSite(DetailsAsis detailsAsis, String asIsBendSiteID) {
        detailsAsis.setExistingBendSite(new AsIs());
        AsIs asIs = detailsAsis.getExistingBendSite();
        if(!asIsBendSiteID.isEmpty()){
            SiteResource siteResource = customerResource.siteResource(getRequest().siteDetail().customerId);
            SiteDTO siteDTO = siteResource.getSiteDetails(asIsBendSiteID);
            if(siteDTO != null){
                asIs.setSiteid(siteDTO.bfgSiteID);
                asIs.setSiteName(siteDTO.getSiteName());
                asIs.setCountryName(siteDTO.getCountryName());
                asIs.setCountryISOCode(siteDTO.getCountryISOCode());
                asIs.setCity(siteDTO.getCity());
                asIs.setPostCode(siteDTO.getPostCode());
                asIs.setLatitude(siteDTO.getLatitude());
                asIs.setLongitude(siteDTO.getLongitude());
                asIs.setAccuracylevel(Integer.toString(siteDTO.getAccuracyLevel()));
                asIs.setSubLocality(siteDTO.getSubLocality());
                asIs.setBuildingNumber(siteDTO.getBuildingNumber());
                asIs.setBuilding(siteDTO.getBuilding());
                asIs.setSubStreet(siteDTO.getSubStreet());
                asIs.setSubBuilding(siteDTO.getSubBuilding());
                asIs.setSubCountyStateProvince(siteDTO.getStateCountySProvince());
                asIs.setPostalOrganisation(siteDTO.getPostalOrg());
                asIs.setPoBox(siteDTO.getPostBox());
                asIs.setStateCode(siteDTO.getStateCode());
                asIs.setStreet(siteDTO.getStreetName());
                asIs.setLocality(siteDTO.getLocality());
            }
        }
        else {
            asIs.setSiteid(null);
            asIs.setSiteName(null);
            asIs.setCountryName(null);
            asIs.setCountryISOCode(null);
            asIs.setCity(null);
            asIs.setPostCode(null);
            asIs.setLatitude(null);
            asIs.setLongitude(null);
            asIs.setAccuracylevel(null);
            asIs.setSubLocality(null);
            asIs.setBuildingNumber(null);
            asIs.setBuilding(null);
            asIs.setSubStreet(null);
            asIs.setSubBuilding(null);
            asIs.setSubCountyStateProvince(null);
            asIs.setPostalOrganisation(null);
            asIs.setPoBox(null);
            asIs.setStateCode(null);
            asIs.setStreet(null);
            asIs.setLocality(null);
        }
    }

    private void configureToBeSite(DetailsTobe detailsTobe, String asIsBendSiteID) {
        detailsTobe.setToBeBendSite(new ToBe());
        ToBe toBe = detailsTobe.getToBeBendSite();
        if(!asIsBendSiteID.isEmpty()){
            SiteResource siteResource = customerResource.siteResource(getRequest().siteDetail().customerId);
            SiteDTO siteDTO = siteResource.getSiteDetails(asIsBendSiteID);
            if(siteDTO != null){
                toBe.setSiteid(siteDTO.bfgSiteID);
                toBe.setSiteName(siteDTO.getSiteName());
                toBe.setCountryName(siteDTO.getCountryName());
                toBe.setCountryISOCode(siteDTO.getCountryISOCode());
                toBe.setCity(siteDTO.getCity());
                toBe.setPostCode(siteDTO.getPostCode());
                toBe.setLatitudeBend(siteDTO.getLatitude());
                toBe.setLongitudeBend(siteDTO.getLongitude());
                toBe.setAccuracylevel(Integer.toString(siteDTO.getAccuracyLevel()));
                toBe.setSubLocality(siteDTO.getSubLocality());
                toBe.setBuildingNumber(siteDTO.getBuildingNumber());
                toBe.setBuilding(siteDTO.getBuilding());
                toBe.setSubStreet(siteDTO.getSubStreet());
                toBe.setSubBuilding(siteDTO.getSubBuilding());
                toBe.setSubCountyStateProvince(siteDTO.getStateCountySProvince());
                toBe.setPostalOrganisation(siteDTO.getPostalOrg());
                toBe.setPoBox(siteDTO.getPostBox());
                toBe.setStateCode(siteDTO.getStateCode());
                toBe.setStreet(siteDTO.getStreetName());
                toBe.setLocality(siteDTO.getLocality());
            }
        }
        else {
            toBe.setSiteid(null);
            toBe.setSiteName(null);
            toBe.setCountryName(null);
            toBe.setCountryISOCode(null);
            toBe.setCity(null);
            toBe.setPostCode(null);
            toBe.setLatitudeBend(null);
            toBe.setLongitudeBend(null);
            toBe.setAccuracylevel(null);
            toBe.setSubLocality(null);
            toBe.setBuildingNumber(null);
            toBe.setBuilding(null);
            toBe.setSubStreet(null);
            toBe.setSubBuilding(null);
            toBe.setSubCountyStateProvince(null);
            toBe.setPostalOrganisation(null);
            toBe.setPoBox(null);
            toBe.setStateCode(null);
            toBe.setStreet(null);
            toBe.setLocality(null);
        }
    }

    private void setupLeg(Configuration leg, LegType legType, ActionCodeStrategy actionCodeStrategy) {
        leg.setType(legType);
        leg.setActionCode(StringUtils.EMPTY); // APE is not using this tag and expecting empty.
    }

    private ActionCodeStrategy getActionCodeStrategy(LegType legType, String action) {
        ActionCodeStrategy actionCode = ACTION_CODE_MAP.get(action);
        return isNotNull(actionCode) ? resolveActionCodeBasedOnAsIsAsset(legType, actionCode) : ActionCodeStrategy.Add;
    }

    private ActionCodeStrategy resolveStrategyBasedOnAssetAction() {
        ChangeType assetAction = getRequest().getAssetAction();
        ActionCodeStrategy actionCodeStrategy = CHANGE_TYPE_ACTION_CODE_MAP.get(assetAction);
        return isNotNull(actionCodeStrategy) ? actionCodeStrategy : ActionCodeStrategy.Add;
    }

    private ActionCodeStrategy resolveActionCodeBasedOnAsIsAsset(LegType legType, ActionCodeStrategy actionCode) {
        if (AS_IS_CONFIGURATION_REQUIRING_ACTIONS.contains(actionCode)) {
            AsIsAsset asIsAsset = getRequest().getAsIsAsset();
            if (Primary.equals(legType) && isNullOrEmpty(asIsAsset.getPrimaryLegAttributes())) {
                return ActionCodeStrategy.Add;
            } else if (Secondary.equals(legType) && isNullOrEmpty(asIsAsset.getSecondaryLegAttributes())) {
                return ActionCodeStrategy.Add;
            }
        }
        return actionCode;
    }

    private void configureAsIsConfig(AsIsAsset asIsAsset, Configuration configuration, LegType legType) {
        DetailsAsis detailsAsis = configuration.getDetailsAsis();
        detailsAsis.setExistingConfig(new AccessConfig());
        AccessConfig asIsConfig = detailsAsis.getExistingConfig();

        List<ApeQrefRequestDTO.AssetAttribute> accessAttributes = Primary.equals(legType) ? asIsAsset.getPrimaryLegAttributes() : asIsAsset.getSecondaryLegAttributes();

        String accessSpeed = getAttributeValueFrom(accessAttributes, ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE);
        Bandwidth accessSpeedBandwidth = Bandwidth.parse(accessSpeed);

        String accessTechnology = getAttributeValueFrom(accessAttributes, ACCESS_TECHNOLOGY);
        String accessType = getAttributeValueFrom(accessAttributes, ACCESS_TYPE);
        String interfaceType = getAttributeValueFrom(accessAttributes, INTERFACE_TYPE_RESERVED_NAME);
        String framing = getAttributeValueFrom(accessAttributes, ACCESS_FRAMING);
        String connector = getAttributeValueFrom(accessAttributes, ACCESS_CONNECTOR);
        String accessSupplierCircuitId = getAttributeValueFrom(accessAttributes, ACCESS_SUPPLIER_CIRCUIT_ID);
        String noOfChannels = getAttributeValueFrom(accessAttributes, ACCESS_NO_OF_CHANNELS);

        asIsConfig.setAccessSpeed(accessSpeed.isEmpty() ? DEFAULT_ACCESS_SPEED : accessSpeedBandwidth.getUnitStringValue());
        asIsConfig.setAccessSpeedUom(Strings.isNullOrEmpty(accessSpeedBandwidth.getUOM()) ? DEFAULT_UNIT_OF_MEASUREMENT : accessSpeedBandwidth.getUOM());
        asIsConfig.setAccessTechnology(isNotEmpty(accessTechnology) ? accessTechnology : "");
        asIsConfig.setAccessTypeName(isNotEmpty(accessType) ? accessType : "");
        asIsConfig.setInterfaceType(isNotEmpty(interfaceType) ? interfaceType : "");
        asIsConfig.setFramingType(isNotEmpty(framing) ? framing : "");
        asIsConfig.setConnectorType(isNotEmpty(connector) ? connector : "");
        asIsConfig.setCircuitId(isNotEmpty(accessSupplierCircuitId) ? accessSupplierCircuitId : "");
        asIsConfig.setNumberOfChannels(isNotEmpty(noOfChannels) ? noOfChannels : "");
    }

    private void configureToBeConfig(Configuration configuration, LegType legType) {
        DetailsTobe detailsToBe = configuration.getDetailsTobe();
        detailsToBe.setToBeConfig(new AccessConfigToBe());
        AccessConfigToBe toBeConfig = detailsToBe.getToBeConfig();

        String accessSpeed;
        Bandwidth accessSpeedBandwidth;

        String accessTechnology;
        String accessType;
        String interfaceType;
        String framing;
        String connector;
        String noOfChannels;
        if (Primary.equals(legType)) {
            accessSpeed = getAttributeValue("", PRIMARY_ACCESS_SPEED);
            accessSpeedBandwidth = Bandwidth.parse(accessSpeed);

            accessTechnology = getAttributeValue("", PRIMARY_ACCESS_TECHNOLOGY);
            accessType = getAttributeValue("", PRIMARY_ACCESS_TYPE);
            interfaceType = getAttributeValue("", PRIMARY_INTERFACE_TYPE);
            framing = getAttributeValue("", PRIMARY_FRAMING);
            connector = getAttributeValue("", PRIMARY_CONNECTOR);
            noOfChannels = getAttributeValue("", PRIMARY_NUMBER_OF_CHANNELS);
        } else {
            accessSpeed = getAttributeValue("", SECONDARY_ACCESS_SPEED);
            accessSpeedBandwidth = Bandwidth.parse(accessSpeed);

            accessTechnology = getAttributeValue("", SECONDARY_ACCESS_TECHNOLOGY);
            accessType = getAttributeValue("", SECONDARY_ACCESS_TYPE);
            interfaceType = getAttributeValue("", SECONDARY_INTERFACE_TYPE);
            framing = getAttributeValue("", SECONDARY_FRAMING);
            connector = getAttributeValue("", SECONDARY_CONNECTOR);
            noOfChannels = getAttributeValue("", SECONDARY_NUMBER_OF_CHANNELS);
        }

        toBeConfig.setAccessSpeed(accessSpeed.isEmpty() ? DEFAULT_ACCESS_SPEED : accessSpeedBandwidth.getUnitStringValue());
        toBeConfig.setAccessSpeedUom(Strings.isNullOrEmpty(accessSpeedBandwidth.getUOM()) ? DEFAULT_UNIT_OF_MEASUREMENT : accessSpeedBandwidth.getUOM());
        toBeConfig.setAccessTechnology(isNotEmpty(accessTechnology) ? accessTechnology : "");
        toBeConfig.setAccessTypeName(isNotEmpty(accessType) ? accessType : "");
        toBeConfig.setInterfaceType(isNotEmpty(interfaceType) ? interfaceType : "");
        toBeConfig.setFramingType(isNotEmpty(framing) ? framing : "");
        toBeConfig.setConnectorType(isNotEmpty(connector) ? connector : "");
        toBeConfig.setNumberOfChannels(isNotEmpty(noOfChannels) ? noOfChannels : "");
    }

    private boolean isDiverse() {
        String diversity = getAttributeValue(ProductOffering.DIVERSITY);
        return !Strings.isNullOrEmpty(diversity) && !DIVERSITY_STANDARD.equalsIgnoreCase(diversity);
    }

    private void setSalesUserDetails(SqeModifyAccessInputDetails sqeModifyAccessInputDetails) {
        SalesUserDetails salesUserDetails = sqeModifyAccessInputDetails.getSalesUserDetails();
        UserDTO user = getRequest().user();
        salesUserDetails.setSalesUserFirstName(nullToEmpty(user.forename));
        salesUserDetails.setSalesUserLastName(nullToEmpty(user.surname));
        salesUserDetails.setSalesUserEmailID(nullToEmpty(user.email));
        salesUserDetails.setSalesUserPhoneNo(nullToEmpty(user.phoneNumber));
        salesUserDetails.setSalesChannel(nullToEmpty(getRequest().customerDetail().getSalesChannel()));
        salesUserDetails.setQref("");
        salesUserDetails.setSalesUserEin(Integer.parseInt(Strings.isNullOrEmpty(user.ein) ? "0" : user.ein));
        salesUserDetails.setPersonID(nullToEmpty(user.ein));
    }

    @Override
    public MultisiteResponse getMultiSiteResponse(APEClient apeClient) {
        SqeModifyAccessInputDetails sqeBulkModifyInput = transformToRequest();
        return apeClient.modifyQuoteForGlobalPricing(sqeBulkModifyInput);
    }
}
