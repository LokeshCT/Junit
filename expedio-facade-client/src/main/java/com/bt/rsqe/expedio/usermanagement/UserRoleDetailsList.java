package com.bt.rsqe.expedio.usermanagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 25/03/15
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRoleDetailsList {

    private List<UserRoleDetails> userRoleDetails = newArrayList();
    public List<UserRoleDetails> getUserRoleDetails() {
        return userRoleDetails;
    }

    public void setUserRoleDetails(List<UserRoleDetails> userRoleDetails) {
        this.userRoleDetails = userRoleDetails;
    }

    public UserRoleDetailsList() {
    }

    public UserRoleDetailsList(List<UserRoleDetails> userRoleDetails) {
        this.userRoleDetails = userRoleDetails;
    }
}
