package com.bt.cqm.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CustomerModel {

    private String id;
    private String salesChannel;
    private String name;

    public CustomerModel() {

    }

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

   /* public void setName(String name) {
        this.name = name;
    }

    public CustomerModel(String id, String salesChannel, String name) {
        this.id = id;
        this.salesChannel = salesChannel;
        this.name = name;
    }*/
}
