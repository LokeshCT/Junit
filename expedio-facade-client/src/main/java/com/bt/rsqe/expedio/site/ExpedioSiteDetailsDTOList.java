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
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ExpedioSiteDetailsDTOList {
    private List<ExpedioSiteDetailsDTO> expedioSiteDetailsDTOList = newArrayList();

    private ExpedioSiteDetailsDTOList(){};

    public ExpedioSiteDetailsDTOList(List<ExpedioSiteDetailsDTO> expedioSiteDetailsDTOs) {
        this.expedioSiteDetailsDTOList = expedioSiteDetailsDTOs;
    }

    public List<ExpedioSiteDetailsDTO> getExpedioSiteDetailsDTOList() {
        return expedioSiteDetailsDTOList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExpedioSiteDetailsDTOList that = (ExpedioSiteDetailsDTOList) o;

        return new EqualsBuilder()
                .append(expedioSiteDetailsDTOList, that.expedioSiteDetailsDTOList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(expedioSiteDetailsDTOList)
                .toHashCode();
    }
}
