package com.asidua.statsintegration.services.control;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="Cmc")
public class Cmc implements Serializable{

    private static final long serialVersionUID = -7397639310152209785L;
    @XmlAttribute
    public String numberOfUsers;
    @XmlAttribute
    public String einNumber;

    public Cmc(){

    }

}