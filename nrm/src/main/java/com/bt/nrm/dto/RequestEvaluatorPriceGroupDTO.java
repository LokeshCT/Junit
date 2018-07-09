package com.bt.nrm.dto;

import java.util.List;

public class RequestEvaluatorPriceGroupDTO {

    private String requestEvaluatorPriceGroupId;
    private String PriceGroupType;
    private String PriceGroupDescription;
    private String oneOffRecommendedRetail;
    private String recurringRecommendedRetail;
    private String nrcPriceToPartner;
    private String rcPriceToPartner;
    private String oneOffCost;
    private String recurringCost;

    public RequestEvaluatorPriceGroupDTO() {
    }

    public RequestEvaluatorPriceGroupDTO(String requestEvaluatorPriceGroupId, String priceGroupType, String priceGroupDescription, String oneOffRecommendedRetail, String recurringRecommendedRetail, String nrcPriceToPartner, String rcPriceToPartner, String oneOffCost, String recurringCost) {
        this.requestEvaluatorPriceGroupId = requestEvaluatorPriceGroupId;
        PriceGroupType = priceGroupType;
        PriceGroupDescription = priceGroupDescription;
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
        this.recurringRecommendedRetail = recurringRecommendedRetail;
        this.nrcPriceToPartner = nrcPriceToPartner;
        this.rcPriceToPartner = rcPriceToPartner;
        this.oneOffCost = oneOffCost;
        this.recurringCost = recurringCost;
    }


    public String getRequestEvaluatorPriceGroupId() {
        return requestEvaluatorPriceGroupId;
    }

    public void setRequestEvaluatorPriceGroupId(String requestEvaluatorPriceGroupId) {
        this.requestEvaluatorPriceGroupId = requestEvaluatorPriceGroupId;
    }

    public String getPriceGroupType() {
        return PriceGroupType;
    }

    public void setPriceGroupType(String priceGroupType) {
        PriceGroupType = priceGroupType;
    }

    public String getPriceGroupDescription() {
        return PriceGroupDescription;
    }

    public void setPriceGroupDescription(String priceGroupDescription) {
        PriceGroupDescription = priceGroupDescription;
    }

    public String getOneOffRecommendedRetail() {
        return oneOffRecommendedRetail;
    }

    public void setOneOffRecommendedRetail(String oneOffRecommendedRetail) {
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
    }

    public String getRecurringRecommendedRetail() {
        return recurringRecommendedRetail;
    }

    public void setRecurringRecommendedRetail(String recurringRecommendedRetail) {
        this.recurringRecommendedRetail = recurringRecommendedRetail;
    }

    public String getNrcPriceToPartner() {
        return nrcPriceToPartner;
    }

    public void setNrcPriceToPartner(String nrcPriceToPartner) {
        this.nrcPriceToPartner = nrcPriceToPartner;
    }

    public String getRcPriceToPartner() {
        return rcPriceToPartner;
    }

    public void setRcPriceToPartner(String rcPriceToPartner) {
        this.rcPriceToPartner = rcPriceToPartner;
    }

    public String getOneOffCost() {
        return oneOffCost;
    }

    public void setOneOffCost(String oneOffCost) {
        this.oneOffCost = oneOffCost;
    }

    public String getRecurringCost() {
        return recurringCost;
    }

    public void setRecurringCost(String recurringCost) {
        this.recurringCost = recurringCost;
    }

}
