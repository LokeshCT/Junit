package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseQuoteOptionPricingDTO {
    @XmlElement(name = "sEcho")
    public int pageNumber;
    @XmlElement(name = "iTotalDisplayRecords")
    public int totalDisplayRecords;
    @XmlElement(name = "iTotalRecords")
    public int totalRecords;

    public BaseQuoteOptionPricingDTO() {/*JAXB*/}

    public BaseQuoteOptionPricingDTO(PaginatedFilterResult filterResult) {
        this.pageNumber = filterResult.getPageNumber();
        this.totalRecords = filterResult.getTotalRecords();
        this.totalDisplayRecords = filterResult.getFilteredSize();
    }

    public static class PriceLineDTO {
        @XmlElement
        public String id;
        @XmlElement
        public String rrp;
        @XmlElement
        public String value;
        @XmlElement
        public String discount;
        @XmlElement
        public String netTotal;
        @XmlElement
        public boolean discountEnabled;
        @XmlElement
        public String vendorDiscountRef;

        public PriceLineDTO() {
            this("", "", "", "", "", false, "");
        }

        public PriceLineDTO(String id, String rrp, String value, String discount, String netTotal, boolean isDiscountEnabled, String vendorDiscountRef) {
            this.id = id;
            this.rrp = rrp;
            this.value = value;
            this.discount = discount;
            this.netTotal = netTotal;
            this.discountEnabled = isDiscountEnabled;
            this.vendorDiscountRef = vendorDiscountRef == null ? "" : vendorDiscountRef;
        }
    }
}
