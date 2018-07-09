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
public class ReassignActivityTaskDTO {

    private String activityID;

    private String assignorFullName;

    private String assignorFirstName;

    private String assignorMailID;

    private String assignedToFullName;

    private String assignedToFirstName;

    private String assignedToMailID;

    private String commentsforReAssignment;

    private String task;

    private String  sourceSystem;

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
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

    public String getAssignedToFullName() {
        return assignedToFullName;
    }

    public void setAssignedToFullName(String assignedToFullName) {
        this.assignedToFullName = assignedToFullName;
    }

    public String getAssignedToFirstName() {
        return assignedToFirstName;
    }

    public void setAssignedToFirstName(String assignedToFirstName) {
        this.assignedToFirstName = assignedToFirstName;
    }

    public String getAssignedToMailID() {
        return assignedToMailID;
    }

    public void setAssignedToMailID(String assignedToMailID) {
        this.assignedToMailID = assignedToMailID;
    }

    public String getCommentsforReAssignment() {
        return commentsforReAssignment;
    }

    public void setCommentsforReAssignment(String commentsforReAssignment) {
        this.commentsforReAssignment = commentsforReAssignment;
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
