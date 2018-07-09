package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.InterimFlag;
import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.OnNetBuilding;
import com.bt.rsqe.ape.SupplierProduct;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.domain.OnNetAddressFormat;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.utils.Lists;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

public abstract class QrefScenarioStrategy {
    protected static final String DIVERSITY_STANDARD = "Standard";
    protected static final String SORT_BY = "SORT BY";
    protected static final String DEFAULT_UNIT_OF_MEASUREMENT = "Kbps";
    protected static final String DEFAULT_ACCESS_SPEED = "-1";
    protected static final String CUSTOMER_PROVIDED_ACCESS = "CUSTOMER PROVIDED ACCESS";
    protected static final String DUAL_HOUSE_ENTRY = "DUAL HOUSE ENTRY";
    protected static final String SEPARACY_REQUIRED = "SEPARACY REQUIRED";
    protected static final String MULTIPLE_CARRIERS_REQUIRED = "MULTIPLE CARRIERS REQUIRED";
    protected static final String NETWORK_DIVERSITY_REQUIRED = "NETWORK DIVERSITY REQUIRED";
    protected static final String CPE_RESILIENCE = "CPE RESILIENCE";
    protected static final String BFD = "BFD";
    protected static final String FAST_CONVERGENCE = "FAST CONVERGENCE";
    protected static final String COLO = "COLO";
    protected static final String PRIMARY_COS_ASSIGNMENT = "PRIMARY COS ASSIGNMENT";
    protected static final String SECONDARY_COS_ASSIGNMENT = "SECONDARY COS ASSIGNMENT";
    protected static final String PRIMARY_ROUTING = "PRIMARY ROUTING";
    protected static final String SECONDARY_ROUTING = "SECONDARY ROUTING";
    protected static final String PRIMARY_EF_REQ = "PRIMARY EF REQ";
    protected static final String SECONDARY_EF_REQ = "SECONDARY EF REQ";
    protected static final String PRIMARY_AF_REF = "PRIMARY AF REQ";
    protected static final String SECONDARY_AF_REF = "SECONDARY AF REQ";
    protected static final String PRIMARY_MVPN_REQ = "PRIMARY MVPN REQ";
    protected static final String SECONDARY_MVPN_REQ = "SECONDARY MVPN REQ";
    protected static final String PRIMARY_MFS = "PRIMARY MFS (MAX FRAME SIZE - MTU)";
    protected static final String SECONDARY_MFS = "SECONDARY MFS (MAX FRAME SIZE - MTU)";
    protected static final String PRIMARY_MAC_ADDRESS_LIMIT = "PRIMARY MAC ADDRESS LIMIT";
    protected static final String SECONDARY_MAC_ADDRESS_LIMIT = "SECONDARY MAC ADDRESS LIMIT";
    protected static final String PRIMARY_LAYER_2_PROTOCOL = "PRIMARY LAYER 2 PROTOCOL TRANSPARENCY";
    protected static final String SECONDARY_LAYER_2_PROTOCOL = "SECONDARY LAYER 2 PROTOCOL TRANSPARENCY";

    protected static final String PRIMARY_INCLUDE_SUPPLIER_LIST = "PRIMARY INCLUDE SUPPLIER LIST";
    protected static final String PRIMARY_EXCLUDE_SUPPLIER_LIST = "PRIMARY EXCLUDE SUPPLIER LIST";
    protected static final String PRIMARY_GPOP_INCLUDE_LIST = "PRIMARY GPOP INCLUDE";
    protected static final String PRIMARY_GPOP_EXCLUDE_LIST = "PRIMARY GPOP EXCLUDE";

    protected static final String SECONDARY_INCLUDE_SUPPLIER_LIST = "SECONDARY INCLUDE SUPPLIER LIST";
    protected static final String SECONDARY_EXCLUDE_SUPPLIER_LIST = "SECONDARY EXCLUDE SUPPLIER LIST";
    protected static final String SECONDARY_GPOP_INCLUDE_LIST = "SECONDARY GPOP INCLUDE";
    protected static final String SECONDARY_GPOP_EXCLUDE_LIST = "SECONDARY GPOP EXCLUDE";
    protected static final String PRODUCT_NAME = "PRODUCT NAME";
    protected static final String PRODUCT_ID = "PRODUCT ID";
    protected static final String IPV4 = "IPV4";
    protected static final String IPV6 = "IPV6";



    private ApeQrefRequestDTO request;
    private String syncUri;

    public QrefScenarioStrategy(ApeQrefRequestDTO request, String syncUri) {
        this.request = request;
        this.syncUri = syncUri;
    }

    public abstract MultisiteResponse getMultiSiteResponse(APEClient apeClient);

    protected ApeQrefRequestDTO getRequest() {
        return request;
    }

    protected String getSyncUri() {
        return syncUri;
    }

    protected boolean getDssEnabledFlag() {
        String attributeValue = getAttributeValue("Yes", ProductOffering.DSS_ENABLED_FLAG);
        return !"No".equals(attributeValue);
    }

    protected String getAttributeValue(String defaultValue, String... attributeNames) {
        for (String attributeName : attributeNames) {
            String attributeValue = getAttributeValue(attributeName);

            if (!Strings.isNullOrEmpty(attributeValue)) {
                return attributeValue;
            }
        }

        return defaultValue;
    }

    protected String getAttributeValue(final String attributeName) {
        return getAttributeValueFrom(getRequest().attributes(), attributeName);
    }

    protected boolean hasAttribute(final String attribute) {
        return Iterables.tryFind(getRequest().attributes(), new Predicate<ApeQrefRequestDTO.AssetAttribute>() {
            @Override
            public boolean apply(ApeQrefRequestDTO.AssetAttribute input) {
                return input.getAttributeName().equals(attribute);
            }
        }).isPresent();
    }

    protected String getAttributeValueFrom(List<ApeQrefRequestDTO.AssetAttribute> attributes, final String... attributeName)   {
        if (!Lists.isNullOrEmpty(attributes)) {
            com.google.common.base.Optional<ApeQrefRequestDTO.AssetAttribute> attribute = Iterables.tryFind(attributes, new Predicate<ApeQrefRequestDTO.AssetAttribute>() {
                @Override
                public boolean apply(ApeQrefRequestDTO.AssetAttribute input) {
                    for(String attribute:attributeName){
                    if(attribute.equalsIgnoreCase(input.getAttributeName())){
                        return true;
                    }
                    }
                    return false;
                }
            });

            if(attribute.isPresent()) {
                return attribute.get().getAttributeValue();
            } else {
                return EMPTY;
            }
        }
        return EMPTY;
    }

    protected boolean getAttributeBooleanValue(String attributeValue) {
        return "Yes".equals(attributeValue);
    }

    protected int getAttributeIntegerValue(String attributeValue) {
        return isEmpty(attributeValue) ? 0 : Integer.parseInt(attributeValue);
    }

    protected String[] getAttributeArrayValue(String attributeName){
        String attributeValue = getAttributeValue(EMPTY,attributeName);
        List<String> valueList = newArrayList();
        for(String value : attributeValue.split(ProductOffering.MULTIVALUE_DELIMITER) ){
            if(!isNullOrEmpty(value)) {
                valueList.add(value);
            }
        }
        return valueList.toArray(new String[valueList.size()]);
    }

    protected OnNetBuilding[] getOnNetBuildings() {

        String onNetBuildingAttribute = getAttributeValue(EMPTY,ProductOffering.ONNET_BUILDING);
        List<OnNetBuilding> onNetBuildingsList = newArrayList();

        for(String building : onNetBuildingAttribute.split(ProductOffering.MULTIVALUE_DELIMITER) ){
            OnNetBuilding onNetBuilding = new OnNetBuilding();
            String buildingCode = OnNetAddressFormat.getBuildingId(building);
            if(!isNullOrEmpty(buildingCode)) {
                onNetBuilding.setOnNetBuildingCode(buildingCode);
                onNetBuildingsList.add(onNetBuilding);
            }
        }

        return onNetBuildingsList.toArray(new OnNetBuilding[onNetBuildingsList.size()]);
    }

    protected SupplierProduct[] getSupplierProducts() {
        return request.getSupplierProducts() != null ? request.getSupplierProducts().toArray(new SupplierProduct[ request.getSupplierProducts().size()]) : null;
    }

    protected boolean isDiverse(String diversity) {
        return !Strings.isNullOrEmpty(diversity) && !DIVERSITY_STANDARD.equalsIgnoreCase(diversity);
    }

    protected InterimFlag getInterimFlag(String interimSiteId){
        return (isNullOrEmpty(interimSiteId)) ? InterimFlag.Interim : InterimFlag.Final;
    }
}
