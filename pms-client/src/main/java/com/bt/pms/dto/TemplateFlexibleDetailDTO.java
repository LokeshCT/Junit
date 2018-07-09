package com.bt.pms.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TemplateFlexibleDetailDTO {

    @XmlAttribute
    private List<AttributeDTO> attributes;
    @XmlAttribute
    private List<PriceGroupDTO> priceGroups;

    public TemplateFlexibleDetailDTO() {
    }

    public TemplateFlexibleDetailDTO(List<AttributeDTO> attributes, List<PriceGroupDTO> priceGroups) {
        this.attributes = attributes;
        this.priceGroups = priceGroups;
    }

    public List<AttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public List<PriceGroupDTO> getPriceGroups() {
        return priceGroups;
    }

    public void setPriceGroups(List<PriceGroupDTO> priceGroups) {
        this.priceGroups = priceGroups;
    }
}
