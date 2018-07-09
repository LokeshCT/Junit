package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * This is the object used by the {@see QuoteOptionPricingTabNew.ftl} FreeMarker template to populate the Pricing Tab view.
 *
 * This is an eventual replacement of the {@link PricingTabView} class.
 */
public class PricingTabViewNew
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PricingTabViewNew.class);

    private static final String URI_SEPARATOR = "/";
    private static final String BCM_APPROVAL_DISCOUNTS_URI = "approve-discounts";
    private static final String BCM_REJECT_DISCOUNTS_URI = "reject-discounts";
    private static final String BCM_COMMENTS_CAVEATS_URI = "commentsandcaveats";

    private final Collection<String> productNames;
    private final String currency;
    private final Collection<String> countries;
    private final String exportPricingSheetLink;
    private final String requestDiscountActionUrl;
    private final boolean priceLinesLocked;
    private final boolean allowRequestDiscount;
    private final String isManualModify;
    private final String bcmUri;
    private final String quoteOptionId;
    private final String costAttachmentUrl;
    private final boolean discountApprovalRequested;
    private final String quoteOptionName;
    private final List<String> offerNames;
    private List<BidManagerCommentsDTO> comments;
    private final boolean costDiscountApplicable;

    public PricingTabViewNew(Collection<String> productNames,
                             String currency,
                             Collection<String> countries,
                             String exportPricingSheetLink,
                             String requestDiscountActionUrl,
                             boolean priceLinesLocked,
                             boolean allowRequestDiscount,
                             String isManualModify,
                             String bcmUri,
                             String costAttachmentUrl,
                             QuoteOptionDTO quoteOption,
                             List<BidManagerCommentsDTO> comments,
                             boolean costDiscountApplicable)
    {
        this.productNames = productNames;
        this.currency = currency;
        this.countries = countries;
        this.exportPricingSheetLink = exportPricingSheetLink;
        this.requestDiscountActionUrl = requestDiscountActionUrl;
        this.priceLinesLocked = priceLinesLocked;
        this.allowRequestDiscount = allowRequestDiscount;
        this.isManualModify = isManualModify;
        this.bcmUri = bcmUri;
        this.quoteOptionId = quoteOption.id;
        this.quoteOptionName = quoteOption.getName();
        this.offerNames = quoteOption.getOfferNames();
        this.discountApprovalRequested = quoteOption.discountApprovalRequested;
        this.costAttachmentUrl = costAttachmentUrl;
        this.comments = comments;
        this.costDiscountApplicable = costDiscountApplicable;
    }

    public Collection<String> getProductNames()
    {
        return productNames;
    }

    public String getCurrency()
    {
        return currency;
    }

    public Collection<String> getCountries() {
        return countries;
    }

    public String getExportPricingSheetLink()
    {
        return exportPricingSheetLink;
    }

    public String getRequestDiscountActionUrl()
    {
        return requestDiscountActionUrl;
    }

    public boolean isPriceLinesLocked()
    {
        return priceLinesLocked;
    }

    public boolean getAllowRequestDiscount()
    {
        return allowRequestDiscount;
    }

    public String getIsManualModify()
    {
        return isManualModify;
    }

    public String getBcmUri()
    {
        return bcmUri;
    }

    public String getQuoteOptionId()
    {
        return quoteOptionId;
    }

    public String getCostAttachmentUrl()
    {
        return costAttachmentUrl;
    }

    public boolean getCostDiscountApplicable()
    {
        return costDiscountApplicable;
    }

    public String getBcmApproveUri()
    {
        return bcmUri + URI_SEPARATOR + BCM_APPROVAL_DISCOUNTS_URI;
    }

    public String getBcmRejectUri()
    {
        return bcmUri + URI_SEPARATOR + BCM_REJECT_DISCOUNTS_URI;
    }
    public boolean isDiscountApprovalRequested()
    {
            return discountApprovalRequested;
    }

    public String getCommentsUri()
    {
        return bcmUri + URI_SEPARATOR + BCM_COMMENTS_CAVEATS_URI;
    }

    public String getQuoteOptionName()
    {
        return quoteOptionName;
    }

    public List<String> getOfferNames()
    {
        return offerNames;
    }

    public List<BidManagerCommentsDTO> getComments()
    {
        return comments;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object.
     */
    @Override
    public String toString()
    {
        String result = ToStringBuilder.reflectionToString(this);
        LOGGER.trace("Returning toString of this object = {}", result);
        return result;
    }
}