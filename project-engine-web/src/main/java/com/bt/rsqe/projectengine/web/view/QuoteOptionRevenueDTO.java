package com.bt.rsqe.projectengine.web.view;


import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteOptionRevenueDTO {
    @XmlElement
    public List<ItemRowDTO> itemDTOs;
    @XmlElement(name = "sEcho")
    public int pageNumber;
    @XmlElement(name = "iTotalDisplayRecords")
    public int totalDisplayRecords;
    @XmlElement(name = "iTotalRecords")
    public int totalRecords;

    public QuoteOptionRevenueDTO() {
        //for JAXB
    }

    public QuoteOptionRevenueDTO(List<ItemRowDTO> itemDTOs) {
        this(itemDTOs,1,itemDTOs.size(),itemDTOs.size());
    }

    public QuoteOptionRevenueDTO(List<ItemRowDTO> itemDTOs, int pageNumber, int totalDisplayRecords, int totalRecords) {
        this.itemDTOs = itemDTOs;
        this.pageNumber = pageNumber;
        this.totalDisplayRecords = totalDisplayRecords;
        this.totalRecords = totalRecords;
    }

    public List<ItemRowDTO> getItemDTOs() {
        return itemDTOs;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ItemRowDTO {

        public int existingRevenue;
        public int proposedRevenue;
        public int triggerMonths;
        public String productCategoryName;
        public String id;

        public ItemRowDTO() {
            //for jaxb
        }

        public ItemRowDTO(String id, String existingRevenue, String proposedRevenue, String triggerMonths, String productCategoryName) {
            this.existingRevenue = StringUtils.isEmpty(existingRevenue)? 0: Integer.parseInt(existingRevenue);
            this.proposedRevenue = StringUtils.isEmpty(proposedRevenue)? 0 : Integer.parseInt(proposedRevenue);
            this.triggerMonths = StringUtils.isEmpty(proposedRevenue)? 0 :Integer.parseInt(triggerMonths);
            this.productCategoryName = productCategoryName;
            this.id = id;
        }

    }

}
