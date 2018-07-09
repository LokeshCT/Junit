package com.asidua.statsintegration.services.control;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="Product Information")
public class ProductInformation implements Serializable {

    private static final long serialVersionUID = 5729605548683533021L;
    @XmlAttribute
    public String projectId;
    @XmlAttribute
    public String customerId;
    @XmlAttribute
    public String quoteName;
    @XmlElement(name = "Cmc")
    public Cmc cmc = new Cmc();

    public ProductInformation(){

    }

    public ProductInformation(String projectId,String customerId, String quoteName){
        this.projectId = projectId;
        this.customerId = customerId;
        this.quoteName = quoteName;
    }
}
