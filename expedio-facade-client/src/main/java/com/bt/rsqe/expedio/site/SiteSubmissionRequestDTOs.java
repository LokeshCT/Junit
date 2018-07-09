package com.bt.rsqe.expedio.site;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 19/12/13
 * Time: 12:34
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SiteSubmissionRequestDTOs {
    private List<SiteSubmissionRequestDTO> siteSubmissionRequestDTOList = newArrayList();

    private SiteSubmissionRequestDTOs(){/*For jaxb*/}

    public SiteSubmissionRequestDTOs(List<SiteSubmissionRequestDTO> siteSubmissionRequestDTOs) {
        this.siteSubmissionRequestDTOList = siteSubmissionRequestDTOs;
    }

    public List<SiteSubmissionRequestDTO> getSiteSubmissionRequestDTOList() {
        return siteSubmissionRequestDTOList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SiteSubmissionRequestDTOs that = (SiteSubmissionRequestDTOs) o;

        return new EqualsBuilder()
                .append(siteSubmissionRequestDTOList, that.siteSubmissionRequestDTOList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(siteSubmissionRequestDTOList)
                .toHashCode();
    }
}
