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
public class RejectActivityTaskDTO {

    private String activityID;

    private String assigneeName;

    private String assigneeFirstName;

    private String assigneeMailID;

    private String assignorName;

    private String assignorFirstName;

    private String assignorMailID;

    private String assignorsCommentsforRejection;

    private String task;

    private String  sourceSystem;

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getAssigneeFirstName() {
        return assigneeFirstName;
    }

    public void setAssigneeFirstName(String assigneeFirstName) {
        this.assigneeFirstName = assigneeFirstName;
    }

    public String getAssigneeMailID() {
        return assigneeMailID;
    }

    public void setAssigneeMailID(String assigneeMailID) {
        this.assigneeMailID = assigneeMailID;
    }

    public String getAssignorName() {
        return assignorName;
    }

    public void setAssignorName(String assignorName) {
        this.assignorName = assignorName;
    }

    public String getAssignorFirstName() {
        return assignorFirstName;
    }

    public void setAssignorFirstName(String assignorFirstName) {
        this.assignorFirstName = assignorFirstName;
    }

    public String getAssignorMailID() {
        return assignorMailID;
    }

    public void setAssignorMailID(String assignorMailID) {
        this.assignorMailID = assignorMailID;
    }

    public String getAssignorsCommentsforRejection() {
        return assignorsCommentsforRejection;
    }

    public void setAssignorsCommentsforRejection(String assignorsCommentsforRejection) {
        this.assignorsCommentsforRejection = assignorsCommentsforRejection;
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
