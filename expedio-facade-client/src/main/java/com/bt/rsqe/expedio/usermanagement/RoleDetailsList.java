package com.bt.rsqe.expedio.usermanagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 23/03/15
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RoleDetailsList {

    private List<RoleDetails> roleDetails = newArrayList();

    public RoleDetailsList() {
    }

    public List<RoleDetails> getRoleDetails() {
        return roleDetails;
    }

    public void setRoleDetails(List<RoleDetails> roleDetails) {
        this.roleDetails = roleDetails;
    }
}

