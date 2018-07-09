package com.bt.cqm.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SiteMinDataDTO  {

    @XmlElement
    private Long siteId;
    @XmlElement
    private String name;


    public SiteMinDataDTO() {
        // required by jaxb
    }


///CLOVER:OFF


    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return name;
    }

    public void setSiteName(String siteName) {
        this.name = siteName;
    }


///CLOVER:ON
}
