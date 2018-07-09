package com.bt.cqm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 7/25/14
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserRoleDTO {

    public static final String CEASE_OPTIMIZATION_TEAM = "Cease Optimization Team";


    @XmlElement
    private Long roleId;

    @XmlElement
    private String roleName;

    public UserRoleDTO(Long roleId, String roleName){
        this.roleId=roleId;
        this.roleName=roleName;
    }

    public UserRoleDTO(){}

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
}
