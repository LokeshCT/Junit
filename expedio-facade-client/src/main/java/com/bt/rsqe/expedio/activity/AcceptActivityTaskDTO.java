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
public class AcceptActivityTaskDTO {

    private String activityID;

    private String assigneeFirstName;

    private String assigneeFullName;

    private String assigneeMailID;

    private String assignorFullName;

    private String assignorFirstName;

    private String assignorMailID;

    private String assigneeComments;

    private String task;

    private String  sourceSystem;

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getAssigneeFirstName() {
        return assigneeFirstName;
    }

    public void setAssigneeFirstName(String assigneeFirstName) {
        this.assigneeFirstName = assigneeFirstName;
    }

    public String getAssigneeFullName() {
        return assigneeFullName;
    }

    public void setAssigneeFullName(String assigneeFullName) {
        this.assigneeFullName = assigneeFullName;
    }

    public String getAssigneeMailID() {
        return assigneeMailID;
    }

    public void setAssigneeMailID(String assigneeMailID) {
        this.assigneeMailID = assigneeMailID;
    }

    public String getAssignorFullName() {
        return assignorFullName;
    }

    public void setAssignorFullName(String assignorFullName) {
        this.assignorFullName = assignorFullName;
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

    public String getAssigneeComments() {
        return assigneeComments;
    }

    public void setAssigneeComments(String assigneeComments) {
        this.assigneeComments = assigneeComments;
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