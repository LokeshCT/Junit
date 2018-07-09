package com.bt.rsqe.customerrecord;

import com.google.common.base.Strings;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BillingAccountDTO {
    public String billingId;
    private String billingAccountName;
    private String currencyCode;

    private BillingAccountDTO() {/*JAXB*/
    }

    public BillingAccountDTO(String billingId, String billingAccountName, String currencyCode) {
        this.billingId = billingId;
        this.billingAccountName = billingAccountName;
        this.currencyCode = currencyCode;
    }

    public String getFriendlyName() {
        if(Strings.isNullOrEmpty(billingAccountName)) {
            return billingId;
        } else {
            return billingId + " - " + billingAccountName;
        }
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
