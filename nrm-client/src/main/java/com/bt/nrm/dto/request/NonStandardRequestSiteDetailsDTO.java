package com.bt.nrm.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by 608143048 on 10/12/2015.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NonStandardRequestSiteDetailsDTO {

    private String uniqueIdentifier;
    private List<NonStandardRequestAttributeDTO> attributes;

    public NonStandardRequestSiteDetailsDTO() {
    }

    public NonStandardRequestSiteDetailsDTO(String uniqueIdentifier, List<NonStandardRequestAttributeDTO> attributes) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.attributes = attributes;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public List<NonStandardRequestAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<NonStandardRequestAttributeDTO> attributes) {
        this.attributes = attributes;
    }
}
