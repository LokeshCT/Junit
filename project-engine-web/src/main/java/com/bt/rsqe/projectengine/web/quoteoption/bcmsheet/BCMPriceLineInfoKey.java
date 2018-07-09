package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class BCMPriceLineInfoKey {

    protected String tariffType;
    protected String mCode;
    protected String chargeType;

    public BCMPriceLineInfoKey(String mCode, String chargeType) {
        this.tariffType = tariffType;
        this.mCode = mCode;
        this.chargeType = chargeType;
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
