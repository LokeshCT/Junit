package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.SQEBulkModifyInput;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.dto.AsIsAsset;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.Bandwidth;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.security.UserDTO;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import pricing.ape.bt.com.schemas.AsIs.AsIs;
import pricing.ape.bt.com.schemas.Configurations.Configurations;
import pricing.ape.bt.com.schemas.ExistingSiteDetails.ExistingSiteDetails;
import pricing.ape.bt.com.schemas.KGIDetails.KGIDetails;
import pricing.ape.bt.com.schemas.Leg.Leg;
import pricing.ape.bt.com.schemas.Leg.LegType;
import pricing.ape.bt.com.schemas.LegConfiguration.LegConfiguration;
import pricing.ape.bt.com.schemas.SalesUserDetails.SalesUserDetails;
import pricing.ape.bt.com.schemas.SiteDetails.SiteDetails;
import pricing.ape.bt.com.schemas.ToBe.ToBe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.ape.source.ActionCodeStrategy.*;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.bt.rsqe.utils.Lists.isNullOrEmpty;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static pricing.ape.bt.com.schemas.Leg.LegType.*;

public class BulkModifyQuoteStrategy extends QrefScenarioStrategy {
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

    public BulkModifyQuoteStrategy(ApeQrefRequestDTO request, String syncUri) {
        super(request, syncUri);
    }

    public SQEBulkModifyInput transformToRequest() {
        SQEBulkModifyInput sqeBulkModifyInput = new SQEBulkModifyInput();
        sqeBulkModifyInput.setSites(new SiteDetails[]{new SiteDetails()});
        sqeBulkModifyInput.setSalesUserDetails(new SalesUserDetails());
        sqeBulkModifyInput.setSyncURI(getSyncUri());
        sqeBulkModifyInput.setReferenceID("");

        setCustomerDetails(sqeBulkModifyInput);
        setSiteDetails(sqeBulkModifyInput);
        setKGIDetails(sqeBulkModifyInput);
        setConfigurations(sqeBulkModifyInput);
        setSalesUserDetails(sqeBulkModifyInput);
        return sqeBulkModifyInput;
    }

    private void setCustomerDetails(SQEBulkModifyInput sqeBulkModifyInput) {
        sqeBulkModifyInput.setTerm(toYears(getAttributeValue(ProductOffering.ACCESS_CONTRACT_TERM)));
        sqeBulkModifyInput.setCustomerName(nullToEmpty(getRequest().customerDetail().getName()));
        sqeBulkModifyInput.setType("Customer");
        sqeBulkModifyInput.setDescription("Description");
        sqeBulkModifyInput.setPartnerRequestId("PartnerRequestId");
        sqeBulkModifyInput.setAutoSelection("1");
        sqeBulkModifyInput.setDistributorID(nullToEmpty(getRequest().customerDetail().gfrCode));
        sqeBulkModifyInput.setCountryTelephoneCode(nullToEmpty(getRequest().siteDetail().countryISOCode));
        sqeBulkModifyInput.setTelephoneAreaCode(nullToEmpty(getRequest().siteDetail().stateCode));
        sqeBulkModifyInput.setTelephoneNo(nullToEmpty(getRequest().siteDetail().phoneNumber));
    }

    private String toYears(String months) {

        return !Strings.isNullOrEmpty(months) ? String.valueOf(Integer.parseInt(months) / 12) : null;
    }

    private void setSiteDetails(SQEBulkModifyInput sqeBulkModifyInput) {
        SiteDTO site = getRequest().siteDetail();

        SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
        siteDetails.setSiteName(nullToEmpty(site.name));
        siteDetails.setCity(nullToEmpty(site.city));
        siteDetails.setCountryName(nullToEmpty(site.country));
        siteDetails.setPostCode(nullToEmpty(site.postCode));
        siteDetails.setStreetNo(nullToEmpty(site.buildingNumber));
        siteDetails.setCountryISOCode(nullToEmpty(site.countryISOCode));
        siteDetails.setCountyStateProvince(nullToEmpty(site.stateCountySProvince));
        siteDetails.setSubLocality(nullToEmpty(site.subLocality));
        siteDetails.setBuildingNumber(nullToEmpty(site.buildingNumber));
        siteDetails.setBuilding(nullToEmpty(site.building));
        siteDetails.setSubStreet(nullToEmpty(site.subStreet));
        siteDetails.setSubBuilding(nullToEmpty(site.subBuilding));
        siteDetails.setSubCountyStateProvince(nullToEmpty(site.subStateCountyProvince));
        siteDetails.setPostalOrganisation(nullToEmpty(site.postalOrg));
        siteDetails.setPOBox(nullToEmpty(site.postBox));
        siteDetails.setStateCode(nullToEmpty(site.stateCode));
        siteDetails.setStreet(nullToEmpty(site.streetName));
        siteDetails.setLocality(nullToEmpty(site.locality));

        siteDetails.setApply_fcm(pricing.ape.bt.com.schemas.SiteDetails.FCMOption.No);
        siteDetails.setApply_fcm_include_y1(pricing.ape.bt.com.schemas.SiteDetails.FCMOption.No);
        siteDetails.setDSSEnabled(getDssEnabledFlag());
        if(this.getRequest().getProcessType().equals(ApeQrefRequestDTO.ProcessType.MOVE)){
        setExistingSiteDetails(siteDetails);
        }
        siteDetails.setFastconvergence(StringUtils.EMPTY);   //APE expects this element as a empty tag
    }

    private void setKGIDetails(SQEBulkModifyInput sqeBulkModifyInput) {
        SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
        siteDetails.setKgi(new KGIDetails());
        KGIDetails kgiDetails = siteDetails.getKgi();
        String latitude = getRequest().siteDetail().latitude;
        kgiDetails.setLatitude(Double.valueOf(Strings.isNullOrEmpty(latitude) ? "0" : latitude));
        String longitude = getRequest().siteDetail().longitude;
        kgiDetails.setLongitude(Double.valueOf(Strings.isNullOrEmpty(longitude) ? "0" : longitude));
        Integer accuracyLevel= getRequest().siteDetail().accuracyLevel;
        kgiDetails.setAccuracyLevel(isNotNull(accuracyLevel) ? accuracyLevel:0);
    }

    private void setExistingSiteDetails(SiteDetails siteDetails) {
        SiteDTO oldSite = getRequest().getOldSite();

        siteDetails.setExistingSiteDetails(new ExistingSiteDetails());
        ExistingSiteDetails existingSiteDetails = siteDetails.getExistingSiteDetails();
        existingSiteDetails.setSiteName(nullToEmpty(oldSite.name));
        existingSiteDetails.setCity(nullToEmpty(oldSite.city));
        existingSiteDetails.setCountryName(nullToEmpty(oldSite.country));
        existingSiteDetails.setPostCode(nullToEmpty(oldSite.postCode));
        existingSiteDetails.setCountryISOCode(nullToEmpty(oldSite.countryISOCode));
        existingSiteDetails.setLocality(nullToEmpty(oldSite.locality));
        existingSiteDetails.setCountyStateProvince(nullToEmpty(oldSite.stateCountySProvince));
        existingSiteDetails.setSubLocality(nullToEmpty(oldSite.subLocality));
        existingSiteDetails.setBuildingNumber(nullToEmpty(oldSite.buildingNumber));
        existingSiteDetails.setBuilding(nullToEmpty(oldSite.building));
        existingSiteDetails.setSubStreet(nullToEmpty(oldSite.subStreet));
        existingSiteDetails.setSubBuilding(nullToEmpty(oldSite.subBuilding));
        existingSiteDetails.setSubCountyStateProvince(nullToEmpty(oldSite.subStateCountyProvince));
        existingSiteDetails.setPostalOrganisation(nullToEmpty(oldSite.postalOrg));
        existingSiteDetails.setPOBox(nullToEmpty(oldSite.postBox));
        existingSiteDetails.setStateCode(nullToEmpty(oldSite.stateCode));
        existingSiteDetails.setStreet(nullToEmpty(oldSite.streetName));
        setExistingKGIDetails(existingSiteDetails, oldSite);
    }

    private void setExistingKGIDetails(ExistingSiteDetails existingSiteDetails, SiteDTO oldSite) {
        existingSiteDetails.setKgi(new KGIDetails());
        KGIDetails kgiDetails = existingSiteDetails.getKgi();
        String latitude = oldSite.latitude;
        kgiDetails.setLatitude(Double.valueOf(Strings.isNullOrEmpty(latitude) ? "0" : latitude));
        String longitude = oldSite.longitude;
        kgiDetails.setLongitude(Double.valueOf(Strings.isNullOrEmpty(longitude) ? "0" : longitude));
        Integer accuracyLevel= oldSite.accuracyLevel;
        kgiDetails.setAccuracyLevel(isNotNull(accuracyLevel) ? accuracyLevel:0);

    }

    private void setConfigurations(SQEBulkModifyInput sqeBulkModifyInput) {
        SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
        siteDetails.setConfigurations(new Configurations());
        Configurations configurations = siteDetails.getConfigurations();
        configurations.setProcessType(getRequest().getProcessType().getType());
        configurations.setProductName(nullToEmpty(getRequest().getLegacyIdentifier().getProductName()));
        configurations.setQuoteCurrency(nullToEmpty(getRequest().currency()));
        configurations.setIsPortOnly(false);
        configurations.setLocalCompanyName(nullToEmpty(getRequest().getOldSite().localCompanyName));

        String diversity = getAttributeValue(ProductOffering.DIVERSITY);
        String productDiversity = getAttributeValue(ProductOffering.PRODUCT_DIVERSITY);
        configurations.setResiliencyType(diversity);
        configurations.setProductSLA(productDiversity);

        if (isDiverse()) {
            configurations.setResilient(true);
            configurations.setLegDetails(new Leg[]{new Leg(), new Leg()});

            configurePrimaryLeg(configurations.getLegDetails()[0]);
            configureSecondaryLeg(configurations.getLegDetails()[1]);
        }   else {
            configurations.setResilient(false);
            configurations.setLegDetails(new Leg[]{new Leg()});

            configurePrimaryLeg(configurations.getLegDetails()[0]);
        }
    }

    private void configurePrimaryLeg(Leg leg) {
        ActionCodeStrategy actionCodeStrategy = hasAttribute(PRIMARY_ACTION_CODE)
            ? getActionCodeStrategy(Primary, getAttributeValue(PRIMARY_ACTION_CODE))
            : resolveStrategyBasedOnAssetAction();

        boolean isMove = this.getRequest().getProcessType().equals(ApeQrefRequestDTO.ProcessType.MOVE);
        actionCodeStrategy = isMove ? ActionCodeStrategy.Move : actionCodeStrategy;
        setupLeg(leg, Primary, actionCodeStrategy);
        configureLeg(leg,
                     getAttributeValue("", MIN_REQUIRED_SPEED, PRIMARY_SERVICE_SPEED),
                     getAttributeValue("", PRIMARY_ACCESS_TECHNOLOGY), getRequest().getSupplierDetails(),
                     getAttributeValue("", PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE),
                     getAttributeValue("", PRIMARY_ACCESS_SPEED),
                     actionCodeStrategy,
                     Primary);

        if (isMove) {
            leg.setDifferentSiteMove(this.getRequest().getSubProcessType().equals(ApeQrefRequestDTO.SubProcessType.DIFFERENT_SITE));
        }

    }

    private void configureSecondaryLeg(Leg leg) {
        ActionCodeStrategy actionCodeStrategy = hasAttribute(SECONDARY_ACTION_CODE)
            ? getActionCodeStrategy(Secondary, getAttributeValue(SECONDARY_ACTION_CODE))
            : resolveStrategyBasedOnAssetAction();

        setupLeg(leg, Secondary, actionCodeStrategy);
        configureLeg(leg,
                     getAttributeValue("", SECONDARY_SERVICE_SPEED),
                     getAttributeValue("", SECONDARY_ACCESS_TECHNOLOGY),
                     getRequest().getSecondarySupplierDetails(),
                     getAttributeValue("", SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE),
                     getAttributeValue("", SECONDARY_ACCESS_SPEED),
                     actionCodeStrategy,
                     Secondary);
    }

    private void setupLeg(Leg leg, LegType legType, ActionCodeStrategy actionCodeStrategy) {
        leg.setType(legType);
        leg.setActionCode(actionCodeStrategy.getApeActionCodes());
        leg.setLegConfiguration(new LegConfiguration());
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

    private void configureLeg(Leg leg, String portSpeed, String accessType, ApeQrefRequestDTO.SupplierDetails supplierDetails, String accessTechSubType, String accessSpeed, ActionCodeStrategy actionCodeStrategy, LegType legType) {
        LegConfiguration legConfiguration = leg.getLegConfiguration();

        if (actionCodeStrategy.requiresAsIs()) {
            createAsIS(getRequest().getAsIsAsset(), legConfiguration, legType, supplierDetails);
        }
        if (actionCodeStrategy.requiresToBe()) {
            createToBe(portSpeed, accessType, accessSpeed, supplierDetails, accessTechSubType, legConfiguration);
        }
    }

    private void createAsIS(AsIsAsset asIsAsset, LegConfiguration legConfiguration, LegType legType, ApeQrefRequestDTO.SupplierDetails supplierDetails) {
        legConfiguration.setAsIs(new AsIs());
        AsIs asIs = legConfiguration.getAsIs();
        List<ApeQrefRequestDTO.AssetAttribute> accessAttributes = Primary.equals(legType) ? asIsAsset.getPrimaryLegAttributes() : asIsAsset.getSecondaryLegAttributes();

        String serviceSpeed;
        if (Primary.equals(legType)) {
            serviceSpeed = getAttributeValueFrom(asIsAsset.getAssetAttributes(), PRIMARY_SERVICE_SPEED, MIN_REQUIRED_SPEED);
        } else {
            serviceSpeed = getAttributeValueFrom(asIsAsset.getAssetAttributes(), SECONDARY_SERVICE_SPEED,MIN_REQUIRED_SPEED);
        }
        Bandwidth portBandwidth =  Bandwidth.parse(serviceSpeed);
        String accessSpeed = getAttributeValueFrom(accessAttributes, ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE);
        Bandwidth accessSpeedBandwidth = Bandwidth.parse(accessSpeed);

        String accessTechnology = getAttributeValueFrom(accessAttributes, ACCESS_TECHNOLOGY);
        String accessType = getAttributeValueFrom(accessAttributes, ACCESS_TYPE);
        String gPopNode =  getAttributeValueFrom(accessAttributes, GPOP_NODE_NAME);
        String accessSupplierName = getAttributeValueFrom(accessAttributes, ACCESS_SUPPLIER_NAME);
        String supplierProduct = getAttributeValueFrom(accessAttributes, SUPPLIER_PRODUCT);
        String accessSupplierCircuitId = getAttributeValueFrom(accessAttributes, ACCESS_SUPPLIER_CIRCUIT_ID);

        asIs.setPortSpeed( portBandwidth.isNotNull() ? portBandwidth.getUnitStringValue() : "");
        asIs.setPortSpeedUOM(portBandwidth.isNotNull() && !Strings.isNullOrEmpty(portBandwidth.getUOM()) ? portBandwidth.getUOM() : DEFAULT_UNIT_OF_MEASUREMENT);
        asIs.setAccessSpeed(accessSpeedBandwidth.isNull() ? DEFAULT_ACCESS_SPEED : accessSpeedBandwidth.getUnitStringValue());
        asIs.setAccessSpeedUOM(accessSpeedBandwidth.isNull() || Strings.isNullOrEmpty(accessSpeedBandwidth.getUOM()) ? DEFAULT_UNIT_OF_MEASUREMENT : accessSpeedBandwidth.getUOM());
        asIs.setAccesTypeName(isNotEmpty(accessTechnology) ? accessTechnology : "");
        asIs.setAccessTechnology(isNotEmpty(accessType) ? accessType : "");
        asIs.setGpopNode(isNotEmpty(gPopNode) ? gPopNode : "");
        asIs.setSupplier(accessSupplierName);
        asIs.setSupplierProduct(isNotEmpty(supplierProduct) ? supplierProduct : "");
        asIs.setServiceVariant(isNotEmpty(accessSupplierCircuitId) ? accessSupplierCircuitId : "");
        asIs.setCircuitID(isNotEmpty(accessSupplierCircuitId) ? accessSupplierCircuitId : "");
        asIs.setNonStandardPortFlag(false);
        if(this.getRequest().getProcessType().equals(ApeQrefRequestDTO.ProcessType.MOVE) && supplierDetails != null){
            asIs.setOldQref(supplierDetails.getSupplierCircuitId());
        }
    }

    private void createToBe(String portSpeed, String accessType, String accessSpeed, ApeQrefRequestDTO.SupplierDetails supplierDetails, String accessTechSubType, LegConfiguration legConfiguration) {
        Bandwidth portBandwidth = Bandwidth.parse(portSpeed);
        Bandwidth accessSpeedBandwidth = Bandwidth.parse(accessSpeed);
        legConfiguration.setToBe(new ToBe());
        ToBe toBe = legConfiguration.getToBe();

        toBe.setPortSpeed( portBandwidth.isNotNull() ? portBandwidth.getUnitStringValue() : "");
        toBe.setPortSpeedUOM( portBandwidth.isNotNull() && !Strings.isNullOrEmpty(portBandwidth.getUOM()) ? portBandwidth.getUOM() : DEFAULT_UNIT_OF_MEASUREMENT);
        toBe.setAccessSpeed(accessSpeedBandwidth.isNull() ? DEFAULT_ACCESS_SPEED : accessSpeedBandwidth.getUnitStringValue());
        toBe.setAccessSpeedUOM(accessSpeedBandwidth.isNull() || Strings.isNullOrEmpty(accessSpeedBandwidth.getUOM()) ? DEFAULT_UNIT_OF_MEASUREMENT : accessSpeedBandwidth.getUOM());
        toBe.setAccesTypeName(accessType);
        toBe.setAccessTechnology(accessTechSubType);
        toBe.setNonStandardPortFlag(false);

        if(!this.getRequest().getProcessType().equals(ApeQrefRequestDTO.ProcessType.MOVE) && null != supplierDetails) {
            toBe.setOldQref(supplierDetails.getSupplierCircuitId());
        }
    }

    private boolean isDiverse() {
        String diversity = getAttributeValue(ProductOffering.DIVERSITY);
        return !Strings.isNullOrEmpty(diversity) && !DIVERSITY_STANDARD.equalsIgnoreCase(diversity);
    }

    private void setSalesUserDetails(SQEBulkModifyInput sqeBulkModifyInput) {
        SalesUserDetails salesUserDetails = sqeBulkModifyInput.getSalesUserDetails();
        UserDTO user = getRequest().user();
        salesUserDetails.setSalesUserFirstName(nullToEmpty(user.forename));
        salesUserDetails.setSalesUserLastName(nullToEmpty(user.surname));
        salesUserDetails.setSalesUserEmailID(nullToEmpty(user.email));
        salesUserDetails.setSalesUserPhoneNo(nullToEmpty(user.phoneNumber));
        salesUserDetails.setSalesChannel(nullToEmpty(getRequest().customerDetail().getSalesChannel()));
        salesUserDetails.setQref("");
        salesUserDetails.setSalesUserEin(Integer.parseInt(Strings.isNullOrEmpty(user.ein) ? "0" : user.ein));
    }


    @Override
    public MultisiteResponse getMultiSiteResponse(APEClient apeClient) {
        SQEBulkModifyInput sqeBulkModifyInput = transformToRequest();
        return apeClient.bulkModifyQuote(sqeBulkModifyInput);
    }
}
