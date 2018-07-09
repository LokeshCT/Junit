package com.bt.rsqe.expedio.activity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 5/4/15
 * Time: 7:52 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WithdrawApprovalActivityTaskDTO {

    private String activityID;

    private String bidManagerName;

    private String bidManagerFirstName;

    private String bidManagerMailID;

    private String salesUserName;

    private String salesUserFirstName;

    private String salesUserMailID;

    private String bidManagerCommentsforWithdrawal;

    private String task;

    private String  sourceSystem;

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getBidManagerName() {
        return bidManagerName;
    }

    public void setBidManagerName(String bidManagerName) {
        this.bidManagerName = bidManagerName;
    }

    public String getBidManagerFirstName() {
        return bidManagerFirstName;
    }

    public void setBidManagerFirstName(String bidManagerFirstName) {
        this.bidManagerFirstName = bidManagerFirstName;
    }

    public String getBidManagerMailID() {
        return bidManagerMailID;
    }

    public void setBidManagerMailID(String bidManagerMailID) {
        this.bidManagerMailID = bidManagerMailID;
    }

    public String getSalesUserName() {
        return salesUserName;
    }

    public void setSalesUserName(String salesUserName) {
        this.salesUserName = salesUserName;
    }

    public String getSalesUserFirstName() {
        return salesUserFirstName;
    }

    public void setSalesUserFirstName(String salesUserFirstName) {
        this.salesUserFirstName = salesUserFirstName;
    }

    public String getSalesUserMailID() {
        return salesUserMailID;
    }

    public void setSalesUserMailID(String salesUserMailID) {
        this.salesUserMailID = salesUserMailID;
    }

    public String getBidManagerCommentsforWithdrawal() {
        return bidManagerCommentsforWithdrawal;
    }

    public void setBidManagerCommentsforWithdrawal(String bidManagerCommentsforWithdrawal) {
        this.bidManagerCommentsforWithdrawal = bidManagerCommentsforWithdrawal;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
}
