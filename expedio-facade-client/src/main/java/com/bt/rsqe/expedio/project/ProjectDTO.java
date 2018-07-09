package com.bt.rsqe.expedio.project;

import com.bt.rsqe.domain.bom.parameters.BFGOrganisationDetails;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProjectDTO {

    @XmlElement
    public String projectId;
    @XmlElement
    public String customerId;
    @XmlElement
    public String customerName;
    @XmlElement
    public String quoteVersion;
    @XmlElement
    public String orderType;
    @XmlElement
    public BFGOrganisationDetails organisation;
    @XmlElement
    public String quoteStatus;
    @XmlElement
    public String currency;
    @XmlElement
    public String salesRepName;
    @XmlElement
    public String salesRepLoginId;
    @XmlElement
    public String expRef;
    @XmlElement
    public String contractId;
    @XmlElement
    public String contractTerm;
    @XmlElement
    public String orderId;
    @XmlElement
    public String bidNumber;
    @XmlElement
    public String siebelId;
    @XmlElement
    public String tradeLevel;
    @XmlElement
    public String quoteName;
    @XmlElement
    public String orderStatus;
    @XmlElement
    public String modifiedBy;
    @XmlElement
    public String isMNCContractUpdated;
    @XmlElement
    public Date modifiedDate;
    @XmlElement
    public String quoteOptionName;
    @XmlElement
    public Date quoteOptionExpiryDate;
    @XmlElement
    public Date createdDate;
    @XmlElement
    public String quoteIndicativeFlag;
    @XmlElement
    public String subOrderType;

    public ProjectDTO() {
    }

    public ProjectDTO(String projectId,
                      String customerId,
                      String customerName,
                      String quoteVersion,
                      String orderType,
                      BFGOrganisationDetails organisation,
                      String quoteStatus,
                      String quoteName,
                      String currency,
                      String salesRepName,
                      String salesRepLoginId,
                      String expRef,
                      String contractId,
                      String contractTerm,
                      String orderId,
                      String bidNumber,
                      String siebelId,
                      String tradeLevel,
                      String orderStatus,
                      String modifiedBy,
                      String isMNCContractUpdated,
                      Date modifiedDate,
                      String quoteOptionName,
                      Date quoteOptionExpiryDate,
                      Date createdDate,
                      String quoteIndicativeFlag,
                      String subOrderType) {

        this.projectId = projectId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.quoteVersion = quoteVersion;
        this.orderType = orderType;
        this.organisation = organisation;
        this.quoteStatus = quoteStatus;
        this.quoteName = quoteName;
        this.currency = currency;
        this.salesRepName = salesRepName;
        this.salesRepLoginId = salesRepLoginId;
        this.expRef = expRef;
        this.contractId = contractId;
        this.contractTerm = contractTerm;
        this.orderId = orderId;
        this.bidNumber = bidNumber;
        this.siebelId = siebelId;
        this.tradeLevel = tradeLevel;
        this.orderStatus = orderStatus;
        this.modifiedBy = modifiedBy;
        this.isMNCContractUpdated = isMNCContractUpdated;
        this.modifiedDate = modifiedDate;
        this.quoteOptionName = quoteOptionName;
        this.quoteOptionExpiryDate = quoteOptionExpiryDate;
        this.createdDate = createdDate;
        this.quoteIndicativeFlag = quoteIndicativeFlag;
        this.subOrderType = subOrderType;
    }

    public BFGOrganisationDetails getOrganisation() {
        return organisation;
    }

    public boolean isMNCContractUpdated() {
        return "Yes".equalsIgnoreCase(this.isMNCContractUpdated);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setQuoteOptionName(String quoteOptionName) {
        this.quoteOptionName = quoteOptionName;
    }

    public void setQuoteOptionExpiryDate(Date quoteOptionExpiryDate) {
        this.quoteOptionExpiryDate = quoteOptionExpiryDate;
    }

    public void setContractTerm(String contractTerm) {
        this.contractTerm = contractTerm;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setQuoteIndicativeFlag(String quoteIndicativeFlag) {
        this.quoteIndicativeFlag = quoteIndicativeFlag;
    }

    public void setSalesRepName(String salesRepName) {
        this.salesRepName = salesRepName;
    }
}