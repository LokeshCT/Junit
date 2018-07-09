package com.asidua.statsintegration.services.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "purge")
public class PurgeResponse implements Serializable {


    private static final long serialVersionUID = 766086847091415987L;
    @XmlElement
    private String responseMessage;


    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }



    public PurgeResponse(String message) {
        System.out.println("###### Constructed purge response");
        setResponseMessage(message);

    }


}
