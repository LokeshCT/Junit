package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.FCMOption;
import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.SqeAccessInput;
import com.bt.rsqe.ape.SqeQuoteInput;
import com.bt.rsqe.ape.SqeUserDetails;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.domain.Bandwidth;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.utils.GsonUtil;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;

public class MultipleProvideQuoteStrategy extends QrefScenarioStrategy {
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private final JsonObject request;

    public MultipleProvideQuoteStrategy(ApeQrefRequestDTO requestDTO, String syncUri) {
        super(requestDTO, syncUri);
        // TODO does this need to be JSON.  Can't we just use the ApeQrefRequestDTO object as is?
        String json = new Gson().toJson(requestDTO);
        request = new Gson().fromJson(json, JsonElement.class).getAsJsonObject();
    }

    public SqeAccessInput transform() {
        SqeAccessInput sqeAccessInput = new SqeAccessInput();
        sqeAccessInput.setQuoteInput(new SqeQuoteInput[]{new SqeQuoteInput()});
        sqeAccessInput.setSqeUserDetails(new SqeUserDetails());
        sqeAccessInput.setSyncURI(getSyncUri());

        setCustomerDetails(sqeAccessInput);
        setSiteDetails(sqeAccessInput);
        setAssetDetails(sqeAccessInput);
        setUserDetails(sqeAccessInput);

        return sqeAccessInput;
    }

    private void setCustomerDetails(SqeAccessInput sqeAccessInput) {
        sqeAccessInput.setTerm(toYears(getAttributeValue(ProductOffering.ACCESS_CONTRACT_TERM)));
        sqeAccessInput.setCustomerName(getStringValueFromPath(request, "customer.name"));
        sqeAccessInput.setType("Customer");
        sqeAccessInput.setDistributorID(getStringValueFromPath(request, "customer.gfrCode"));
    }

    private String toYears(String months) {
        return !Strings.isNullOrEmpty(months) ? String.valueOf(Integer.parseInt(months) / 12) : null;
    }

    private void setSiteDetails(SqeAccessInput sqeAccessInput) {
        SqeQuoteInput sqeQuoteInput = sqeAccessInput.getQuoteInput()[0];
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

        String latitude = getStringValueFromPath(request, "site.latitude");
        sqeQuoteInput.setLatitude(Double.valueOf(Strings.isNullOrEmpty(latitude) ? "0" : latitude));
        String longitude = getStringValueFromPath(request, "site.longitude");
        sqeQuoteInput.setLongitude(Double.valueOf(Strings.isNullOrEmpty(longitude) ? "0" : longitude));
        sqeQuoteInput.setFastconvergence(StringUtils.EMPTY); //APE Expects this empty tag.
        sqeQuoteInput.setListOfOnNetBuildingCodes(getOnNetBuildings());
    }

    private void setAssetDetails(SqeAccessInput sqeAccessInput) {

        sqeAccessInput.setAutoSelection(getAttributeValue("", SORT_BY));

        SqeQuoteInput sqeQuoteInput = sqeAccessInput.getQuoteInput()[0];
        sqeQuoteInput.setQuoteCurrency(getStringValueFromPath(request, "currency"));

        Bandwidth primaryPortBandwidth = Bandwidth.parse(getAttributeValue("", newArrayList(ProductOffering.MIN_REQUIRED_SPEED, ProductOffering.PRIMARY_SERVICE_SPEED)));
        Bandwidth secondaryPortBandwidth = Bandwidth.parse(getAttributeValue("", ProductOffering.SECONDARY_SERVICE_SPEED));
        logger.bandwidth("secondaryPortBandwidth", secondaryPortBandwidth);

        Bandwidth primaryAccessSpeed = Bandwidth.parse(getAttributeValue("", newArrayList(ProductOffering.PRIMARY_ACCESS_SPEED)));
        Bandwidth secondaryAccessSpeed = Bandwidth.parse(getAttributeValue("", newArrayList(ProductOffering.SECONDARY_ACCESS_SPEED)));

        sqeQuoteInput.setAccessSpeed1(primaryAccessSpeed.isNull() ? "-1" : primaryAccessSpeed.getUnitStringValue());
        sqeQuoteInput.setAccessSpeedUom1(primaryAccessSpeed.isNull() || Strings.isNullOrEmpty(primaryAccessSpeed.getUOM()) ? "Kbps" : primaryAccessSpeed.getUOM());
        sqeQuoteInput.setAccessSpeed2(secondaryAccessSpeed.isNull() ? "-1" : secondaryAccessSpeed.getUnitStringValue());
        sqeQuoteInput.setAccessSpeedUom2(secondaryAccessSpeed.isNull() || Strings.isNullOrEmpty(secondaryAccessSpeed.getUOM()) ? "Kbps" : secondaryAccessSpeed.getUOM());

        sqeQuoteInput.setPortSpeed1(primaryPortBandwidth.isNull() ? "" : primaryPortBandwidth.getUnitStringValue());
        sqeQuoteInput.setPortSpeedUom1(primaryPortBandwidth.isNull() || Strings.isNullOrEmpty(primaryPortBandwidth.getUOM()) ? "Kbps" : primaryPortBandwidth.getUOM());

        sqeQuoteInput.setPortSpeed2(secondaryPortBandwidth.isNull() ? "-1" : secondaryPortBandwidth.getUnitStringValue());
        sqeQuoteInput.setPortSpeedUom2(secondaryPortBandwidth.isNull() ? "" : secondaryPortBandwidth.getUOM());

        sqeQuoteInput.setAccess_Type_Name(getAttributeValue("", ProductOffering.PRIMARY_ACCESS_TECHNOLOGY));
        sqeQuoteInput.setLeg2_Access_Type_Name(getAttributeValue("", ProductOffering.SECONDARY_ACCESS_TECHNOLOGY));

        String diversity = getAttributeValue(ProductOffering.DIVERSITY);
        String productDiversity = getAttributeValue(ProductOffering.PRODUCT_DIVERSITY);
        sqeQuoteInput.setResiliencyType(diversity);
        sqeQuoteInput.setResilient(!Strings.isNullOrEmpty(diversity) && !DIVERSITY_STANDARD.equalsIgnoreCase(diversity));
        sqeQuoteInput.setProductSLA(!Strings.isNullOrEmpty(diversity) && !DIVERSITY_STANDARD.equalsIgnoreCase(productDiversity) ? productDiversity : null);

        sqeQuoteInput.setProductName1(getStringValueFromPath(request, "legacyIdentifier.productName"));
        sqeQuoteInput.setProductID(getStringValueFromPath(request, "legacyIdentifier.productId"));
        sqeQuoteInput.setApply_fcm(FCMOption.No);
        sqeQuoteInput.setApply_fcm_include_y1(FCMOption.No);

        sqeQuoteInput.setAccessTechnology(getAttributeValue("", ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE));
        sqeQuoteInput.setAccessTechnology2(getAttributeValue("", ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE));

        if (isNotNull(primaryAccessSpeed.getTechnology())) {
            sqeQuoteInput.setAccessTechnology(primaryAccessSpeed.getTechnology());
        }
        if (isNotNull(secondaryAccessSpeed.getTechnology())) {
            sqeQuoteInput.setAccessTechnology2(secondaryAccessSpeed.getTechnology());
        }

        sqeQuoteInput.setDSSEnabled(getDssEnabledFlag());
    }

    private void setUserDetails(SqeAccessInput sqeAccessInput) {
        SqeUserDetails sqeUserDetails = sqeAccessInput.getSqeUserDetails();
        sqeUserDetails.setSalesUserFirstName(getStringValueFromPath(request, "user.forename"));
        sqeUserDetails.setSalesUserLastName(getStringValueFromPath(request, "user.surname"));
        sqeUserDetails.setSalesUserEmailID(getStringValueFromPath(request, "user.email"));
        sqeUserDetails.setSalesUserPhoneNo(getStringValueFromPath(request, "user.phoneNumber"));
        sqeUserDetails.setSalesChannel(getStringValueFromPath(request, "customer.salesChannel"));
        String ein = getStringValueFromPath(request, "user.ein");
        sqeUserDetails.setSalesUserEin(Strings.isNullOrEmpty(ein) ? 0 : Integer.parseInt(ein.trim()));
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
        SqeAccessInput sqeAccessInput = transform();
        return apeClient.multipleProvideQuote(sqeAccessInput);
    }

    public static interface Logger {
        @Log(level = LogLevel.DEBUG, format = "MultipleProvideQuote %s Bandwidth %s")
        void bandwidth(String name, Bandwidth bandwidth);
    }
}
