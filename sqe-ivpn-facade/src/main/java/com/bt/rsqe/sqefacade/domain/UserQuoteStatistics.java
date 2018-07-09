package com.bt.rsqe.sqefacade.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserQuoteStatistics {

    private int numberOfCustomers;
    private int numberOfQuotes;
    private int numberOfSites;
    private int numberOfOrders;

    private UserQuoteStatistics() {
    }

    public UserQuoteStatistics(int numberOfCustomers, int numberOfQuotes, int numberOfSites, int numberOfOrders) {
        this.numberOfCustomers = numberOfCustomers;
        this.numberOfQuotes = numberOfQuotes;
        this.numberOfSites = numberOfSites;
        this.numberOfOrders = numberOfOrders;
    }

    public int getNumberOfCustomers() {
        return numberOfCustomers;
    }

    public int getNumberOfQuotes() {
        return numberOfQuotes;
    }

    public int getNumberOfSites() {
        return numberOfSites;
    }

    public int getNumberOfOrders() {
        return numberOfOrders;
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
