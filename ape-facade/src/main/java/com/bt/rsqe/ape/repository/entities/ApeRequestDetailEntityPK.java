package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ApeRequestDetailEntityPK implements Serializable {

    @Column(name = "REQUEST_ID")
    String requestId;

    @Column(name = "ATTRIBUTE_NAME")
    String attributeName;

    public ApeRequestDetailEntityPK() {
    }

    public ApeRequestDetailEntityPK(String requestId, String attributeName) {
        this.requestId = requestId;
        this.attributeName = attributeName;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
