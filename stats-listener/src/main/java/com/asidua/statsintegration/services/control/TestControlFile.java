package com.asidua.statsintegration.services.control;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "StatsIntegration")
public class TestControlFile implements Serializable {

    private static final long serialVersionUID = 1182896394279747438L;
    @XmlAttribute(name = "name")
    public String name;

    @XmlAttribute(name = "version")
    public String version;

    @XmlElement(name="ProductInformation")
    public ProductInformation product = new ProductInformation();


    public TestControlFile(){
        name="Unknown";
        version="0.0";
    }
    public TestControlFile(String name, String version) {
        this.name = name;
        this.version = version;

    }


}
