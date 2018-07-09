package com.bt.rsqe.expedio.usermanagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 23/03/15
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SubGroup {

    @XmlElement
    private String Message;
    @XmlElement
    private String Listofsubgroups;

    public SubGroup() {
    }

    public SubGroup(String message, String subGroupList) {
        this.Message = message;
        this.Listofsubgroups = subGroupList;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        this.Message = message;
    }

    public String getSubGroupList() {
        return Listofsubgroups;
    }

    public void setSubGroupList(String subGroupList) {
        this.Listofsubgroups = subGroupList;
    }
}
