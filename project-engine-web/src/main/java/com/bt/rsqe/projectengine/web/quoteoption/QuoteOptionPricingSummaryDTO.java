package com.bt.rsqe.projectengine.web.quoteoption;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteOptionPricingSummaryDTO {

    public QuoteOptionPricingSummaryDTO() {
        /*Needed for JAXB*/
    }

    @XmlElement
    public BigDecimal totalOneTimeGross;

    @XmlElement
    public BigDecimal totalOneTimeDiscount;

    @XmlElement
    public BigDecimal totalOneTimeNet;

    @XmlElement
    public BigDecimal totalRecurringGross;

    @XmlElement
    public BigDecimal totalRecurringDiscount;

    @XmlElement
    public BigDecimal totalRecurringNet;

    @XmlElement
    public BigDecimal totalOffNetUsage;

    @XmlElement
    public BigDecimal totalOnNetUsage;

    @XmlElement
    public BigDecimal totalUsage;
}
