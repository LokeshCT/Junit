package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;

import java.util.Collection;
import java.util.List;

// TODO: Remove once pricing design implementation is complete.
public class PricingTabView {
    private Collection<String> productNames;
    private String currency;
    private List<String> countries;
    private String exportPricingSheetLink;
    private String requestDiscountActionUrl;
    private final boolean priceLinesLocked;
    private boolean allowRequestDiscount;
    private String isManualModify;
    private String bcmUri;
    private String quoteOptionId;
    private String costAttachmentUrl;
    private boolean discountApprovalRequested;
    private String quoteOptionName;
    private List<String> offerNames;
    private List<BidManagerCommentsDTO> comments;
    private boolean costDiscountApplicable;

    public PricingTabView(Collection<String> productNames,
                          String currency,
                          List<String> countries,
                          String exportPricingSheetLink,
                          String requestDiscountActionUrl,
                          boolean priceLinesLocked,
                          boolean allowRequestDiscount,
                          String isManualModify,
                          String bcmUri,
                          String costAttachmentUrl, QuoteOptionDTO quoteOption,
                          List<BidManagerCommentsDTO> comments,boolean costDiscountApplicable) {
        this.productNames = productNames;
        this.currency = currency;
        this.countries = countries;
        this.exportPricingSheetLink = exportPricingSheetLink;
        this.requestDiscountActionUrl = requestDiscountActionUrl;
        this.priceLinesLocked = priceLinesLocked;
        this.allowRequestDiscount = allowRequestDiscount;
        this.isManualModify = isManualModify;
        this.bcmUri = bcmUri;
        this.comments = comments;
        this.quoteOptionId = quoteOption.id;
        this.quoteOptionName = quoteOption.getName();
        this.offerNames = quoteOption.getOfferNames();
        this.discountApprovalRequested = quoteOption.discountApprovalRequested;
        this.costAttachmentUrl = costAttachmentUrl;
        this.costDiscountApplicable = costDiscountApplicable;
    }

    public Collection<String> getProductNames() {
        return productNames;
    }

    public String getCurrency() {
        return currency;
    }

    public List<String> getCountries() {
        return countries;
    }

    public String getExportPricingSheetLink() {
        return exportPricingSheetLink;
    }

    public String getRequestDiscountActionUrl() {
        return requestDiscountActionUrl;
    }

    public boolean isPriceLinesLocked() {
        return priceLinesLocked;
    }

    public boolean getAllowRequestDiscount() {
        return allowRequestDiscount;
    }

    public String getIsManualModify() {
        return isManualModify;
    }

    public String getBcmUri() {
        return bcmUri;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public String getCostAttachmentUrl() {
        return costAttachmentUrl;
    }

    public boolean getCostDiscountApplicable(){
        return costDiscountApplicable;
    }

    public String getBcmApproveUri() {
        return bcmUri + "/approve-discounts";
    }

    public String getBcmRejectUri() {
        return bcmUri + "/reject-discounts";
    }
    public boolean isDiscountApprovalRequested() {
            return discountApprovalRequested;
        }

    public String getCommentsUri() {
        return bcmUri + "/commentsandcaveats";
    }

    public String getQuoteOptionName() {
        return quoteOptionName;
    }

    public List<String> getOfferNames() {
        return offerNames;
    }

    public  List<BidManagerCommentsDTO> getComments() {
        return comments;
    }
}
