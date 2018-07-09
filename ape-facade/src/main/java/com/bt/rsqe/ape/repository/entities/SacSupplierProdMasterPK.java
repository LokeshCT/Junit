package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 17/09/15
 * Time: 21:02
 * To change this template use File | Settings | File Templates.
 */
public class SacSupplierProdMasterPK implements Serializable {

    private Long siteId;

    /*private String supplierId;*/

    private String spacId;

    public SacSupplierProdMasterPK(Long siteId, /*String supplierId,*/ String spacId) {
        this.siteId = siteId;
        /*this.supplierId = supplierId;*/
        this.spacId = spacId;
    }

    public SacSupplierProdMasterPK() {
    }

    public Long getSiteId() {
        return siteId;
    }

    /*   public String getSupplierId() {
        return supplierId;
    }*/
    public String getSpacId() {
        return spacId;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
