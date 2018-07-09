package com.bt.cqm.dto.user;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SubGroupDTO {

    @XmlElement
    private Long subGroupId;

    @XmlElement
    private String subGroupName;

    public SubGroupDTO() {
    }

    public SubGroupDTO(Long subGroupId, String subGroupName) {
        this.subGroupId = subGroupId;
        this.subGroupName = subGroupName;
    }
///CLOVER:OFF

    public Long getSubGroupId() {
        return subGroupId;
    }

    public void setSubGroupId(Long subGroupId) {
        this.subGroupId = subGroupId;
    }

    public String getSubGroupName() {
        return subGroupName;
    }

    public void setSubGroupName(String subGroupName) {
        this.subGroupName = subGroupName;
    }


///CLOVER:ON
}
