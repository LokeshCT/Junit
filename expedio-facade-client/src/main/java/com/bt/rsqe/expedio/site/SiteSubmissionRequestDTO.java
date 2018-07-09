package com.bt.rsqe.expedio.site;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SiteSubmissionRequestDTO {

    @XmlElement
    public int addressId;
    @XmlElement
    public int locationId;
    @XmlElement
    public int siteId;
    @XmlElement
    public String expedioReferenceId;

    // for jaxb
    public SiteSubmissionRequestDTO() {
    }

    public SiteSubmissionRequestDTO(int addressId, int locationId, int siteId, String expedioReferenceId) {
        this.addressId = addressId;
        this.locationId = locationId;
        this.siteId = siteId;
        this.expedioReferenceId = expedioReferenceId;
    }
}
