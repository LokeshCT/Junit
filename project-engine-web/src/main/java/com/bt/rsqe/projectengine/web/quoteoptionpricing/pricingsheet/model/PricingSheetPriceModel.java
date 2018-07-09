package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.domain.ContractTermHelper;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetExportException;
import com.bt.rsqe.utils.Lists;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.Date;

import static com.bt.rsqe.utils.AssertObject.*;

public class PricingSheetPriceModel {
    private final String pmfId;
    private final PriceLine oneTimePrice;
    private PriceLine rentalPrice;
    private final PriceLine usagePrice;
    private ProductInstance owningInstance;
    private Optional<ProductInstance> asIs;
    private final int contractTerm;
    private final String priceType;

    public PricingSheetPriceModel(String pmfId, PriceLine oneTimePrice, PriceLine rentalPrice, PriceLine usagePrice, String contractTerm, String priceType, ProductInstance owningInstance, Optional<ProductInstance> asIs) {
        this.pmfId = pmfId;
        this.oneTimePrice = oneTimePrice;
        this.rentalPrice = rentalPrice;
        this.usagePrice = usagePrice;
        this.owningInstance = owningInstance;
        this.asIs = asIs;
        this.contractTerm = Integer.parseInt(contractTerm);
        this.priceType = priceType;
    }

    public int getContractTerm(){
        return this.contractTerm;
    }

    public int getRemainingContractTerm(){
        if(asIs.isPresent()) {
            ProductInstance asIsInstance = asIs.get();
            return ContractTermHelper.getMaxRemainingMonths(asIsInstance.getEarlyBillingStartDate(), asIsInstance.getContractTerm());
        }

        return contractTerm;
    }

    public Number getRecurringEupPrice() {
        return rentalPrice == null ? 0.00 : rentalPrice.getChargePrice().getDiscountedPrice();
    }

    public Number getNonRecurringEupPrice() {
        return oneTimePrice == null ? 0.00 : oneTimePrice.getChargePrice().getDiscountedPrice();
    }

    public Number getNonRecurringPtpPrice() {
        return oneTimePrice == null ? 0.00 : oneTimePrice.getEupPrice().getPrice();
    }

    public Number getRecurringPtpPrice() {
        return rentalPrice == null ? 0.00 : rentalPrice.getEupPrice().getPrice();
    }

    public Number getRecurringEupPriceForContract() {
        return rentalPrice == null ? 0.00 : (rentalPrice.getChargePrice().getDiscountedPrice().doubleValue() * contractTerm);
    }

    public Number getRecurringPtpPriceForContract() {
        return rentalPrice == null ? 0.00 : (rentalPrice.getChargePrice().getDiscountedPrice().doubleValue() * contractTerm);
    }

    public Number getUsageBasedEupPrice() {
        return usagePrice == null ? 0.00 : usagePrice.getChargePrice().getDiscountedPrice();
    }

    public Number getUsageBasedPtpPrice() {
        return usagePrice == null ? 0.00 : usagePrice.getEupPrice().getPrice();
    }

    public String getPmfId() {
        return pmfId;
    }

    public String getDescription(){
        return isNotNull(oneTimePrice)? oneTimePrice.getPriceLineName() : isNotNull(rentalPrice)? rentalPrice.getPriceLineName():usagePrice.getPriceLineName();
    }

    public String getSummary(){
        String summary = "";
        for (Attribute attribute: owningInstance.getProductOffering().getAttributes()) {
            if (attribute.isVisibleInSummary()) {
                try {
                    summary += " " + owningInstance.getInstanceCharacteristic(attribute.getName()).getValue() + ",";
                } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                    // Instance Characteristic may not exist
                }
            }
        }
        return summary.contains(",") ? summary.substring(0, summary.lastIndexOf(",")) : summary;
    }

    public PriceLine getOneTimePrice(){
        return oneTimePrice;
    }

    public PriceLine getRentalPrice() {
        return rentalPrice;
    }

    public PriceLine getUsagePrice() {
        return usagePrice;
    }

    public String getPriceType(){
        return priceType;
    }

    public static final String DUMMY_PMF_ID = "DummyPmfId";
    private static final PricingSheetPriceModel DUMMY_PRICE = new PricingSheetPriceModel(DUMMY_PMF_ID, new PriceLine(), new PriceLine(), new PriceLine(), "0","NEW", null, Optional.<ProductInstance>absent());
    static PricingSheetPriceModel dummyPriceModel() {
        return DUMMY_PRICE;
    }

    static Predicate<PricingSheetPriceModel> notDummyPriceModelPredicate() {
        return new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(@Nullable PricingSheetPriceModel input) {
                return !DUMMY_PMF_ID.equals(input.pmfId);
            }
        };
    }

    public String getChargingSchemeName() {
       if(isNotNull(oneTimePrice) && isNotNull(rentalPrice) && !oneTimePrice.getChargingSchemeName().equalsIgnoreCase(rentalPrice.getChargingSchemeName())){
           throw new PricingSheetExportException("Charging Schemes does not match for one time and recurring");
       }
       return isNotNull(oneTimePrice)? oneTimePrice.getChargingSchemeName(): isNotNull(rentalPrice)? rentalPrice.getChargingSchemeName() : usagePrice.getChargingSchemeName();
    }
 public String getInstanceCharacteristic(String attributeName) {
        if(owningInstance != null) {
            try {
                return owningInstance.getInstanceCharacteristic(attributeName).getStringValue();
            } catch (InstanceCharacteristicNotFound e) {
                // Keep calm!
            }
        }

        //To set the Default Quantity to 1, when called from Pricing Sheet
        if("QUANTITY".equals(attributeName)) {
            return "1";
        }

        return "";
    }

    public ProductInstance getOwningInstance() {
        return owningInstance;
    }

    public Optional<ProductInstance> getAsIsInstance() {
        return asIs;
    }

  public String getTariffType() {
        return (isNotNull(oneTimePrice))? oneTimePrice.getTariffType(): isNotNull(rentalPrice) ? rentalPrice.getTariffType() : usagePrice.getTariffType();
    }

    public Date getBillingStartDate() {
        if(asIs.isPresent() && !asIs.get().getPriceLines().isEmpty())  {
            return asIs.get().getPriceLines().get(0).getBillingStartDate();
        }
        return null;
    }

    public void setRentalPrice(PriceLine rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public Number getMinCharge() {
        return null; // standard charges don't have this price
    }

    public Number getFixedCharge() {
        return null; // standard charges don't have this price
    }

    public Number getChargeRate() {
        return null; // standard charges don't have this price
    }

    public boolean isUsageChargePriceLine() {
        return null != usagePrice && !Lists.isNullOrEmpty(usagePrice.getUsageCharges());
    }
}
