package com.bt.cqm.dto.user;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRoleMasterDTO {

    private Long roleId;

    private String roleName;

    private boolean isDefault;

    public UserRoleMasterDTO() {
    }

    public UserRoleMasterDTO(Long roleId, String roleName) {
        this(roleId,roleName,false);
    }


    public UserRoleMasterDTO(Long roleId, String roleName,boolean isDefault) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.isDefault=isDefault;
    }

    ///CLOVER:OFF

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    ///CLOVER:ON

}
