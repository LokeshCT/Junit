package com.bt.rsqe.customerrecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Customers extends ArrayList<URI> {
    public Customers(){
        super();
    }

    public Customers(List<URI> uris) {
        super(uris);
    }

    @XmlElement(name = "uris")
    public List<URI> getUris(){
        return this;
    }
}
