package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteOptionPricingDTO extends BaseQuoteOptionPricingDTO {
    @XmlElement
    public List<ItemRowDTO> itemDTOs;

    public QuoteOptionPricingDTO() {
        super();
    }

    public QuoteOptionPricingDTO(List<QuoteOptionPricingDTO.ItemRowDTO> items, PaginatedFilterResult filterResult) {
        super(filterResult);
        this.itemDTOs = items;
    }

    public static class ItemRowDTO {
        @XmlElement
        public boolean aggregateRow;
        @XmlElement
        public int groupingLevel;
        @XmlElement
        public String site;
        @XmlElement
        public String miniAddress;
        @XmlElement
        public String product;
        @XmlElement
        public String lineItemId;
        @XmlElement
        public String description;
        @XmlElement
        public String status;
        @XmlElement
        public String discountStatus;
        @XmlElement
        public String offerName;
        @XmlElement
        public String summary;
        @XmlElement
        public PriceLineDTO oneTime;
        @XmlElement
        public PriceLineDTO recurring;
        @XmlElement
        public boolean readOnly;
        @XmlElement
        public boolean forIfc;
        @XmlElement
        public String userEntered;
        @XmlElement
        public boolean isManualPricing;
    }
}
