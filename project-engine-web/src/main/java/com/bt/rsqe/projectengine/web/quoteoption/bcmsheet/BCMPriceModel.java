package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.bom.parameters.OrderType;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.security.UserContextManager;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

import static com.bt.rsqe.utils.AssertObject.*;

public class BCMPriceModel {

    PriceLine oneTimePriceLine;
    PriceLine monthlyPriceLine;
    String tariffType;
    String oneTimeEUPValue;
    String recurringEUPValue;
    String oneTimePTPPrice;
    String recurringEUPPrice;
    String recurringPTPPrice;
    ProductChargingScheme scheme;
    ProductInstance productInstance;
    String lineItemAction;

    public BCMPriceModel(PriceLine oneTimePriceLine, PriceLine monthlyPriceLine, String tariffType, ProductChargingScheme scheme, ProductInstance productInstance, String lineItemAction) {
        this.oneTimePriceLine = oneTimePriceLine;
        this.monthlyPriceLine = monthlyPriceLine;
        this.tariffType = tariffType;
        this.scheme = scheme;
        this.productInstance = productInstance;
        this.lineItemAction = lineItemAction;
    }

    public BCMPriceModel(String oneTimeEUPValue, String recurringEUPValue) {
        this.oneTimeEUPValue = oneTimeEUPValue;
        this.recurringEUPValue = recurringEUPValue;
    }

    public String getOneTimeEUPValue() {
        return oneTimeEUPValue;
    }

    public String getRecurringEUPValue() {
        return recurringEUPValue;
    }

    public PriceLine getOneTimePriceLine() {
        return oneTimePriceLine;
    }

    public PriceLine getMonthlyPriceLine() {
        return monthlyPriceLine;
    }

    public String getTariffType() {
        return tariffType;
    }

    public ProductInstance getProductInstance() {
        return productInstance;
    }

    public ProductChargingScheme getScheme() {
        return scheme;
    }

    public String getPriceDescription() {
        return oneTimePriceLine.getPriceLineName();
    }

    public String getVisibility() {
        return scheme.getPriceVisibility().name();
    }

    public String getOneTimePriceLineId() {
        return oneTimePriceLine.getId();
    }

    public String getRecurringPriceLineId() {
        return monthlyPriceLine.getId();
    }

    public String getOnetimeEUPPrice() {
        // if(!(LineItemAction.NONE.getDescription().equalsIgnoreCase(lineItemAction))){
        if(isNotNull(oneTimePriceLine)){
            if (!isIndirect()) {
                oneTimeEUPValue = BCMUtil.getPriceInStr(oneTimePriceLine.getChargePrice().getPrice());
            } else {
                oneTimeEUPValue = BCMUtil.getPriceInStr(oneTimePriceLine.getEupPrice().getPrice());
            }
        }
        //  }
        return oneTimeEUPValue;
    }

    public String getOneTimePTPPrice() {
        // if(!(LineItemAction.NONE.getDescription().equalsIgnoreCase(lineItemAction))){
        if(isNotNull(oneTimePriceLine)){
            if (!isIndirect()) {
                oneTimePTPPrice = BCMUtil.getPriceInStr(oneTimePriceLine.getEupPrice().getPrice());
            } else {
                oneTimePTPPrice = BCMUtil.getPriceInStr(oneTimePriceLine.getChargePrice().getPrice());
                //    }
            }
        }
        return oneTimePTPPrice;
    }

    private boolean isIndirect() {
        return UserContextManager.getCurrent().getPermissions().indirectUser;
    }

    public String getRecurringEUPPrice() {
        if (!(OrderType.CEASE.getValue().equalsIgnoreCase(lineItemAction))) {
            if(isNotNull(monthlyPriceLine)){
                if (!isIndirect()) {
                    recurringEUPPrice = BCMUtil.getPriceInStr(monthlyPriceLine.getChargePrice().getPrice());
                } else {
                    recurringEUPPrice = BCMUtil.getPriceInStr(monthlyPriceLine.getEupPrice().getPrice());
                }
            }
        }
        return recurringEUPPrice;
    }

    public String getRecurringPTPPrice() {
        if (!(OrderType.CEASE.getValue().equalsIgnoreCase(lineItemAction))) {
            if(isNotNull(monthlyPriceLine)){
                if (!isIndirect()) {
                    recurringPTPPrice = BCMUtil.getPriceInStr(monthlyPriceLine.getEupPrice().getPrice());
                } else {
                    recurringPTPPrice = BCMUtil.getPriceInStr(monthlyPriceLine.getChargePrice().getPrice());
                }
            }
        }
        return recurringPTPPrice;
    }

    public BigDecimal getOneTimeDiscount() {
        if(isNotNull(oneTimePriceLine)){
            BigDecimal value = oneTimePriceLine.getChargePrice().getDiscountPercentage();
            return value != null && !value.equals(BigDecimal.ZERO) ? BCMUtil.changeDiscountToDecimalAndRound(value.toString()) : BigDecimal.ZERO;
        }
         return BigDecimal.ZERO;
    }

    public BigDecimal getMonthlyDiscount() {
        if (isNotNull(monthlyPriceLine) && (!(OrderType.CEASE.getValue().equalsIgnoreCase(lineItemAction)))) {
            BigDecimal value = monthlyPriceLine.getChargePrice().getDiscountPercentage();
            return value != null && !value.equals(BigDecimal.ZERO) ? BCMUtil.changeDiscountToDecimalAndRound(value.toString()) : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

     public String getOneTimeDiscountedValue() {
         if (isNotNull(oneTimePriceLine) && (!(OrderType.CEASE.getValue().equalsIgnoreCase(lineItemAction)))) {
            return BCMUtil.getPriceInStr(oneTimePriceLine.getChargePrice().getDiscountedPrice());
         }
        return  BCMUtil.getPriceInStr(BigDecimal.ZERO);
    }

    public String getMonthlyDiscountedValue() {
         if (isNotNull(monthlyPriceLine) && (!(OrderType.CEASE.getValue().equalsIgnoreCase(lineItemAction)))) {
            return BCMUtil.getPriceInStr(monthlyPriceLine.getChargePrice().getDiscountedPrice());
         }
        return  BCMUtil.getPriceInStr(BigDecimal.ZERO);
    }

    //Getters for cost lines
    public String getRecurringPrice() {
        if (isNotNull(monthlyPriceLine) && (!(OrderType.CEASE.getValue().equalsIgnoreCase(lineItemAction)))) {
            return BCMUtil.getPriceInStr(monthlyPriceLine.getChargePrice().getPrice());
        }
        return StringUtils.EMPTY;
    }

    public String getNonRecurringPrice() {
        if (isNotNull(oneTimePriceLine) && (!(OrderType.CEASE.getValue().equalsIgnoreCase(lineItemAction)))) {
            return BCMUtil.getPriceInStr(oneTimePriceLine.getChargePrice().getPrice());
        }
        return StringUtils.EMPTY;
    }

    public String getPriceBookVersion() {
        return StringUtils.EMPTY;
    }

    public String getPrimaryTariffZone() {
        return StringUtils.EMPTY;
    }

    public String getLineItemAction() {
        return lineItemAction;
    }

    public String getVendorDiscountRef() {
        if(isNotNull(oneTimePriceLine)){
            return oneTimePriceLine.getVendorDiscountRef();
        }else if(isNotNull(monthlyPriceLine)){
            return monthlyPriceLine.getVendorDiscountRef();
        }

        return StringUtils.EMPTY;
    }

    public String getPmfId() {
        if(isNotNull(oneTimePriceLine)){
            return oneTimePriceLine.getPmfId();
        }else if(isNotNull(monthlyPriceLine)){
            return monthlyPriceLine.getPmfId();
        }

        return StringUtils.EMPTY;
    }
}
