package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.google.common.base.Optional;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.math.BigDecimal;

public class DiscountDelta {
    private Optional<BigDecimal> discount;
    private Optional<PriceLineStatus> status = Optional.absent();
    private Optional<String> vendorDiscountRef = Optional.absent();

    public DiscountDelta(BigDecimal discount) {
        this.discount = Optional.of(discount);
    }

    public DiscountDelta(Optional<BigDecimal> discount, Optional<String> vendorDiscountRef, Optional<PriceLineStatus> status) {
        this.discount = discount;
        this.vendorDiscountRef = vendorDiscountRef;
        this.status = status;
    }

    public Optional<BigDecimal> getDiscount() {
        return discount;
    }

    public Optional<String> getVendorDiscountRef() {
        return vendorDiscountRef;
    }

    public Optional<PriceLineStatus> getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }
}
