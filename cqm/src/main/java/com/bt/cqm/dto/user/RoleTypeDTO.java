package com.bt.cqm.dto.user;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RoleTypeDTO {

    @XmlElement
    private Long roleTypeId;

    @XmlElement
    private String roleTypeName;

    public RoleTypeDTO() {
    }

    public RoleTypeDTO(Long roleTypeId, String roleTypeName) {
        this.roleTypeId = roleTypeId;
        this.roleTypeName = roleTypeName;
    }

    ///CLOVER:OFF

    public Long getRoleTypeId() {
        return roleTypeId;
    }

    public void setRoleTypeId(Long roleTypeId) {
        this.roleTypeId = roleTypeId;
    }

    public String getRoleTypeName() {
        return roleTypeName;
    }

    public void setRoleTypeName(String roleTypeName) {
        this.roleTypeName = roleTypeName;
    }


///CLOVER:ON
}
