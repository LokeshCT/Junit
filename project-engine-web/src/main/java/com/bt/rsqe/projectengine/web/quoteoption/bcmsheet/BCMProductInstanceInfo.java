package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.project.Price;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Maps.*;

public class BCMProductInstanceInfo {

    private BCMSiteDetails siteDetails;
    private Map<BCMPriceLineInfoKey, List<BCMPriceLineInfo>> bcmPriceLineInfoList;
    private String sCode;
    private String displayName;
    private String description;
    private String versionNumber;
    private String productInstanceId;
    private Long productInstanceVersion;
    private String action;
    private String priceStatus;
    private boolean isIndirect;
    private List<PriceLine> priceLines;
    private QuoteOptionDTO quoteOption;
    private Map<String, List<BCMProductInstanceInfo>> relatedInstances;
    private Map<String, String> bcmInstanceCharacteristicsMap;
    private boolean isSpecialBid;
    private String currency;
    private String quoteOptionItemId;
    private static final String USER_ENTERED = "User Entered";

    public BCMProductInstanceInfo(BCMSiteDetails siteDetails,
                                  Map<BCMPriceLineInfoKey,
                                          List<BCMPriceLineInfo>> bcmPriceLineInfoList,
                                  String sCode, String displayName,
                                  String versionNumber,
                                  String productInstanceId,
                                  Long productInstanceVersion,
                                  String action,
                                  String priceStatus,
                                  boolean isIndirect,
                                  Map<String,
                                          String> bcmInstanceCharacteristicsMap,
                                  boolean isSpecialBid,
                                  String currency,
                                  String quoteOptionItemId,
                                  List<PriceLine> priceLines, QuoteOptionDTO quoteOption, String description) {
        this.siteDetails = siteDetails;
        this.bcmPriceLineInfoList = bcmPriceLineInfoList;
        this.sCode = sCode;
        this.displayName = displayName;
        this.description = description;
        this.versionNumber = versionNumber;
        this.productInstanceId = productInstanceId;
        this.productInstanceVersion = productInstanceVersion;
        this.action = action;
        this.priceStatus = priceStatus;
        this.isIndirect = isIndirect;
        this.priceLines = priceLines;
        this.quoteOption = quoteOption;
        this.relatedInstances = newLinkedHashMap();
        this.bcmInstanceCharacteristicsMap = bcmInstanceCharacteristicsMap;
        this.isSpecialBid = isSpecialBid;
        this.currency = currency;
        this.quoteOptionItemId = quoteOptionItemId;
    }

    public BCMProductInstanceInfo() {
        this.bcmPriceLineInfoList = newHashMap();
        this.sCode = "";
        this.displayName = "";
        this.versionNumber = "";
        this.productInstanceId = "";
        this.action = "";
        this.priceStatus = "";
        this.relatedInstances = newHashMap();
        this.bcmInstanceCharacteristicsMap = newHashMap();
        this.isSpecialBid = false;
        this.currency = "";
        this.quoteOptionItemId = "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public String isBranchOrCentralSite() {
        return isSiteInstallable() ? "Branch" : "Central";
    }

    public boolean isSpecialBid() {
        return isSpecialBid;
    }

    public boolean isSiteInstallable() {
        return siteDetails.isSiteInstallable();
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public String getQuoteOptionItemId() {
        return quoteOptionItemId;
    }

    public String getCurrency() {
        return currency;
    }

    public BCMSiteDetails getSite() {
        return siteDetails;
    }

    public Integer getSiteId() {
        return siteDetails.getId();
    }

    public String getSCode() {
        return sCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public String getProductInstanceId() {
        return productInstanceId;
    }

    public Long getProductInstanceVersion() {
        return productInstanceVersion;
    }

    public String getAction() {
        return action;
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public String getPriceStatus() {
        return priceStatus;
    }

    private Map<BCMPriceLineInfoKey, List<BCMPriceLineInfo>> getBcmPriceLineInfoMap() {
        return bcmPriceLineInfoList;
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public List<BCMPriceLineInfo> getAllPriceLines() {
        return (List) bcmPriceLineInfoList.values();
    }

    public Map<String, List<BCMProductInstanceInfo>> getRelatedInstancesMap() {
        return relatedInstances;
    }

    public BCMProductInstanceInfo getRelated(String relationshipName, int index) {
        String relationshipNameUpper = relationshipName.toUpperCase();
        if (isNotNull(this.relatedInstances.get(relationshipNameUpper))
                && index < relatedInstances.get(relationshipNameUpper).size()) {
            return relatedInstances.get(relationshipNameUpper).get(index);
        }
        return new BCMProductInstanceInfo();
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public BCMProductInstanceInfo getRelated(String relationshipName) {
        return getRelated(relationshipName, 0);
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public BCMProductInstanceInfo getRelatedBasedOnAttribute(String relationName, final String attributeName, final String value) {
        final List<BCMProductInstanceInfo> bcmProductInstanceInfos = this.relatedInstances.get(relationName.toUpperCase());
        if (isNotNull(bcmProductInstanceInfos)) {
            final Optional<BCMProductInstanceInfo> productInstanceInfoOptional = Iterables.tryFind(bcmProductInstanceInfos, new Predicate<BCMProductInstanceInfo>() {
                @Override
                public boolean apply(BCMProductInstanceInfo productInstanceInfo) {
                    final Object attributeValue = productInstanceInfo.getValue(attributeName);
                    return isNotNull(attributeValue) && attributeValue.toString().equalsIgnoreCase(value);
                }
            });
            if (productInstanceInfoOptional.isPresent()) {
                return productInstanceInfoOptional.get();
            }
        }
        return new BCMProductInstanceInfo();
    }

    public Object getOneTimePrice(String mCode, int index, String tariffType) {
        List<BCMPriceLineInfo> priceLineInfoList = bcmPriceLineInfoList.get(new BCMPriceLineInfoKey(mCode, PriceType.ONE_TIME.getValue()));
        if (isNotNull(priceLineInfoList) && isNotNull(priceLineInfoList.get(index))) {
            return getPrice(priceLineInfoList.get(index), tariffType);
        }
        return "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getOneTimePrice(String mCode, String tariffType) {
        return getOneTimePrice(mCode, 0, tariffType);
    }

    public Object getRecurringPrice(String mCode, int index, String tariffType) {
        List<BCMPriceLineInfo> priceLineInfoList = bcmPriceLineInfoList.get(new BCMPriceLineInfoKey(mCode, PriceType.RECURRING.getValue()));
        if (isNotNull(priceLineInfoList) && isNotNull(priceLineInfoList.get(index))) {
            return getPrice(priceLineInfoList.get(index), tariffType);
        }
        return "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getRecurringPrice(String mCode, String tariffType) {
        return getRecurringPrice(mCode, 0, tariffType);
    }

    public String getMcodePriceLineStatus(String mCode) {  // specific to icg and icr access
        String status = "";
        boolean userEntered = false;

        for (PriceLine priceLine : priceLines) {
            if (priceLine.getPmfId().equals(mCode)) {
                userEntered = (userEntered || "Y".equalsIgnoreCase(priceLine.getUserEntered()));
                status = userEntered ? USER_ENTERED : priceLine.getStatus().getDescription();
            }
        }
        return status;
    }

    private Object getPrice(BCMPriceLineInfo priceLineInfo, String tariffType) {
        if (isIndirect) {
            if (tariffType.equalsIgnoreCase("PTP")) {
                return getFormattedPrice(priceLineInfo.getChargePrice());
            } else {
                return getFormattedPrice(priceLineInfo.getEupPrice());
            }
        } else if (tariffType.equalsIgnoreCase("EUP")) {
            return getFormattedPrice(priceLineInfo.getChargePrice());
        }
        return "";
    }


    public Object getOneTimeCost(String mCode, int index) {
        BCMPriceLineInfoKey key = new BCMPriceLineInfoKey(mCode, PriceType.ONE_TIME.getValue());
        List<BCMPriceLineInfo> priceLineInfoList = bcmPriceLineInfoList.get(key);

        return isNotNull(priceLineInfoList) && isNotNull(priceLineInfoList.get(index))? getFormattedPrice(priceLineInfoList.get(index).getChargePrice()) : "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getOneTimeCost(String mCode) {
        return getOneTimeCost(mCode, 0);
    }

    public Object getRecurringCost(String mCode, int index) {
        BCMPriceLineInfoKey key = new BCMPriceLineInfoKey(mCode, PriceType.RECURRING.getValue());
        List<BCMPriceLineInfo> priceLineInfoList = bcmPriceLineInfoList.get(key);

        return isNotNull(priceLineInfoList) && isNotNull(priceLineInfoList.get(index))? getFormattedPrice(priceLineInfoList.get(index).getChargePrice()) : "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getRecurringCost(String mCode) {
        return getRecurringCost(mCode, 0);
    }

    public Object getOneTimeDiscount(String mCode, int index) {
        BCMPriceLineInfoKey key = new BCMPriceLineInfoKey(mCode, PriceType.ONE_TIME.getValue());
        return isNotNull(bcmPriceLineInfoList.get(key)) && isNotNull(bcmPriceLineInfoList.get(key).get(index)) ?
            getFormattedDiscount(bcmPriceLineInfoList.get(key).get(index).getDiscountPercentage()): "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getOneTimeDiscount(String mCode) {
        return getOneTimeDiscount(mCode, 0);
    }

    public Object getMonthlyDiscount(String mCode, int index) {
        BCMPriceLineInfoKey key = new BCMPriceLineInfoKey(mCode, PriceType.RECURRING.getValue());
        return isNotNull(bcmPriceLineInfoList.get(key)) && isNotNull(bcmPriceLineInfoList.get(key).get(index)) ?
            getFormattedDiscount(bcmPriceLineInfoList.get(key).get(index).getDiscountPercentage()) : "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getMonthlyDiscount(String mCode) {
        return getMonthlyDiscount(mCode, 0);
    }

    public Object getValue(String attributeName) {
        if (attributeName.equalsIgnoreCase("CONFIGURATION CATEGORY")) {
            for (Map.Entry<BCMPriceLineInfoKey, List<BCMPriceLineInfo>> entry : getBcmPriceLineInfoMap().entrySet()) {
                if (!entry.getKey().tariffType.equalsIgnoreCase(PriceCategory.COST.getLabel())) {
                    return entry.getValue().get(0).getPriceLineName();
                }
            }
        } else if(attributeName.contains("QUANTITY") || attributeName.equalsIgnoreCase("Ad hoc consultancy days")){
            return Integer.parseInt(bcmInstanceCharacteristicsMap.get(attributeName.toUpperCase()));
        }
        return isNotNull(attributeName)? bcmInstanceCharacteristicsMap.get(attributeName.toUpperCase()): "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public BCMPriceLineInfo getPriceLine(String mCode, String chargeType) {
        for (Map.Entry<BCMPriceLineInfoKey, List<BCMPriceLineInfo>> entry : getBcmPriceLineInfoMap().entrySet()) {
            if (entry.getKey().mCode.equalsIgnoreCase(mCode) && entry.getKey().chargeType.equalsIgnoreCase(chargeType)) {
                return entry.getValue().get(0);
            }
        }
        return new BCMPriceLineInfo();
    }

     private String getTariffTypeKey(String tariffType) {
        if(tariffType.equalsIgnoreCase("PTP")) {
            return PriceCategory.PRICE_TO_PARTNER.getLabel();
        }
        else if (tariffType.equalsIgnoreCase("EUP")) {
            return PriceCategory.END_USER_PRICE.getLabel();
        }
        return "";
    }

    private BigDecimal getFormattedPrice(Price price) {
        return price.getPrice().setScale(2, RoundingMode.CEILING);
    }

    private BigDecimal getFormattedDiscount(BigDecimal discount) {
        return discount.movePointLeft(2);
    }

    //Special Bid methods below

   @SuppressWarnings("unused") //used within BCM Export template
    public Object getOneTimePrice(String tariffType) {
        return getPriceLinePrice(getTariffTypeKey(tariffType), PriceType.ONE_TIME.getValue());
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getRecurringPrice(String tariffType) {
        return getPriceLinePrice(getTariffTypeKey(tariffType), PriceType.RECURRING.getValue());
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getOneTimeCost() {
        return getPriceLinePrice(PriceCategory.COST.getLabel(), PriceType.ONE_TIME.getValue());
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getRecurringCost() {
        return getPriceLinePrice(PriceCategory.COST.getLabel(), PriceType.RECURRING.getValue());
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getDeInstallPrice(String tariffType) {
        return this.getAction().equalsIgnoreCase("CEASE") ? getPriceLinePrice(getTariffTypeKey(tariffType), "ignore") : "";
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getSpecialBidOneTimeDiscount(String tariffType) {
        return getPriceLineDiscount(getTariffTypeKey(tariffType), PriceType.ONE_TIME.getValue());
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public Object getSpecialBidMonthlyDiscount(String tariffType) {
        return getPriceLineDiscount(getTariffTypeKey(tariffType), PriceType.RECURRING.getValue());
    }

    @SuppressWarnings("unused") //used within BCM Export template
    public BCMPriceLineInfo getPriceLine(String tariffType, String mCode, String chargeType) {
        for (BCMPriceLineInfoKey key : this.getBcmPriceLineInfoMap().keySet()) {
            if (key.tariffType.equalsIgnoreCase(getTariffTypeKey(tariffType)) && key.chargeType.equalsIgnoreCase(chargeType) && ("".equalsIgnoreCase(mCode) || key.mCode.equalsIgnoreCase(mCode))) {
                return this.getBcmPriceLineInfoMap().get(key).get(0);
            }
        }
        return new BCMPriceLineInfo();
    }

    private Object getPriceLinePrice(String tariffType, String chargeType) {
        for (BCMPriceLineInfoKey key : this.getBcmPriceLineInfoMap().keySet()) {
            if (key.tariffType.equalsIgnoreCase(tariffType) && (key.chargeType.equalsIgnoreCase(chargeType) || "ignore".equalsIgnoreCase(chargeType))) {
                return getFormattedPrice(this.getBcmPriceLineInfoMap().get(key).get(0).getChargePrice());
            }
        }
        return "";
    }

    public String getQuoteOptionStatus() {
        return quoteOption.getStatus().getDescription();
    }

    private Object getPriceLineDiscount(String tariffType, String chargeType) {
        for (BCMPriceLineInfoKey key : this.getBcmPriceLineInfoMap().keySet()) {
            if (key.tariffType.equalsIgnoreCase(tariffType) && key.chargeType.equalsIgnoreCase(chargeType)) {
                return getFormattedDiscount(this.getBcmPriceLineInfoMap().get(key).get(0).getDiscountPercentage());
            }
        }
        return "";
    }
    @SuppressWarnings("unused") //used within BCM Export template
    public int getCount(String relationshipName) {
        int count = 0;
        String relationshipNameUpper = relationshipName.toUpperCase();
        if (isNotNull(this.relatedInstances.get(relationshipNameUpper))) {
            count = relatedInstances.get(relationshipNameUpper).size();
        }
        return count;
    }

}
