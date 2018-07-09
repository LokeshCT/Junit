package com.bt.rsqe.projectengine.web.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OfferDetailsDTO {
    @XmlElement
    public String name;
    @XmlElement
    public String created;
    @XmlElement
    public List<ItemRowDTO> itemDTOs;
    @XmlElement(name = "sEcho")
    public int pageNumber;
    @XmlElement(name = "iTotalDisplayRecords")
    public int totalDisplayRecords;
    @XmlElement(name = "iTotalRecords")
    public int totalRecords;

    public OfferDetailsDTO(String name, String created, int pageNumber, int totalDisplayRecords, int totalRecords, List<ItemRowDTO> itemDTOs) {
        this.name = name;
        this.created = created;
        this.pageNumber = pageNumber;
        this.totalDisplayRecords = totalDisplayRecords;
        this.totalRecords = totalRecords;
        this.itemDTOs = itemDTOs;
    }

    private OfferDetailsDTO() {
        /*Needed for JAXB*/
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ItemRowDTO {

        public String id;
        public String product;
        public String site;
        public String miniAddress;
        public String status;
        public String discountStatus;
        public String pricingStatus;
        public String validity;
        public String errorMessage;
        public boolean forIfc;
        public boolean isManualModify;
        public boolean isQuoteOnlyLeadToCashPhase;
        public String summary;

        private ItemRowDTO() {/*JAXB*/
        }

        public ItemRowDTO(String id, String product,String miniAddress, SiteView site, String status, String discountStatus,
                          String pricingStatus, String validity, String errorMessage,
                          boolean forIfc, String summary) {
            this.id = id;
            this.product = product;
            this.miniAddress = miniAddress;
            this.validity = validity;
            this.errorMessage = errorMessage;
            this.forIfc = forIfc;
            this.site = site.getName();
            this.status = status;
            this.discountStatus = discountStatus;
            this.pricingStatus = pricingStatus;
            this.isManualModify = false;
            this.summary = summary;
        }

        public ItemRowDTO(String id, String product, String miniAddress,SiteView site, String status, String discountStatus,
                          String pricingStatus, String validity, String errorMessage, String summary,
                          boolean forIfc, boolean isManualModify, boolean isQuoteOnlyLeadToCashPhase) {
            this.id = id;
            this.product = product;
            this.miniAddress = miniAddress;
            this.site = site.getName();
            this.status = status;
            this.discountStatus = discountStatus;
            this.pricingStatus = pricingStatus;
            this.validity = validity;
            this.errorMessage = errorMessage;
            this.forIfc = forIfc;
            this.isManualModify = isManualModify;
            this.isQuoteOnlyLeadToCashPhase = isQuoteOnlyLeadToCashPhase;
            this.summary = summary;
        }

    }
}

