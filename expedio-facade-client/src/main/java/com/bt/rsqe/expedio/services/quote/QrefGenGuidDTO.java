package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 07/07/15
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QrefGenGuidDTO {

    private String qrefReportType;

    private String salesChannel;

    private String salesChannelType;

    private String userFirstName;

    private String userLastName;

    private String userRole;

    private String userEmailID;

    private String submitterSystem;

    public String getQrefReportType() {
        return qrefReportType;
    }

    public void setQrefReportType(String qrefReportType) {
        this.qrefReportType = qrefReportType;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getSalesChannelType() {
        return salesChannelType;
    }

    public void setSalesChannelType(String salesChannelType) {
        this.salesChannelType = salesChannelType;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserEmailID() {
        return userEmailID;
    }

    public void setUserEmailID(String userEmailID) {
        this.userEmailID = userEmailID;
    }

    public String getSubmitterSystem() {
        return submitterSystem;
    }

    public void setSubmitterSystem(String submitterSystem) {
        this.submitterSystem = submitterSystem;
    }
}
