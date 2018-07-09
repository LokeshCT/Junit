package com.bt.rsqe.expedio.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MNCCustomersDTO {

    @XmlElement(name = "MNCCustomers")
    private List<MNCCustDTO> customers;

    public List<MNCCustDTO> getCustomers() {
        return customers;
    }

    public void setCustomers(List<MNCCustDTO> customers) {
        this.customers = customers;
    }
}
