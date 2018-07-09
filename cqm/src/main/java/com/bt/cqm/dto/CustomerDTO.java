package com.bt.cqm.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CustomerDTO {

    @XmlElement
    private String id;
    @XmlElement
    private String salesChannel;
    @XmlElement
    private String name;
    @XmlElement
    private String status;

    public CustomerDTO() {
        // required by jaxb
    }

    public CustomerDTO(String id, String name, String salesChannel) {
        this.id = id;
        this.name = name;
        this.salesChannel = salesChannel;
    }

///CLOVER:OFF

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


///CLOVER:ON
}
