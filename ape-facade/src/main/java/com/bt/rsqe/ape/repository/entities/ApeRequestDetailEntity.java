package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "APE_REQUEST_DETAIL")
@Entity
public class ApeRequestDetailEntity {

    @EmbeddedId
    private ApeRequestDetailEntityPK key;

    @Column(name = "ATTRIBUTE_VALUE")
    private String attributeValue;

    public ApeRequestDetailEntity() {
    }

    public ApeRequestDetailEntity(String requestId, String attributeName, String attributeValue) {
        this.key = new ApeRequestDetailEntityPK(requestId, attributeName);
        this.attributeValue = attributeValue;
    }

    public String getRequestId() {
        return key.getRequestId();
    }

    public String getAttributeName() {
        return key.getAttributeName();
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
