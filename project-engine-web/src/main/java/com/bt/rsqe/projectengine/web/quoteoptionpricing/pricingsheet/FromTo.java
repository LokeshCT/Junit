package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class FromTo {
    int from;
    int to;

    public FromTo(int from, int to){
        this.from = from;
        this.to = to;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public void to(int to){
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FromTo fromTo = (FromTo) o;

        return new EqualsBuilder()
                .append(from, fromTo.from)
                .append(to, fromTo.to)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(from)
                .append(to)
                .toHashCode();
    }
}
