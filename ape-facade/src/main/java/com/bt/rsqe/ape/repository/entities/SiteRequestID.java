package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 08/09/15
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class SiteRequestID implements Serializable {

    private String fileName;

    private Long siteId;

    SiteRequestID(String fileName, Long siteId) {
        this.fileName = fileName;
        this.siteId = siteId;
   }

    public SiteRequestID() {
    }

    public String getFileName() {
        return fileName;
    }


    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "SiteRequestID{" +
               "fileName='" + fileName + '\'' +
               ", siteId=" + siteId +
               '}';
    }
}
