package com.bt.pms.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EvaluatorGroupDTO {
    @XmlAttribute
    private String evaluatorGroupId;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String description;
    @XmlAttribute
    private EvaluatorGroupDTO masterEvaluatorGroup;
    @XmlAttribute
    private String isEvaluatorGroupMaster;
    @XmlAttribute
    private String email;
    @XmlAttribute
    private String sitePlannerNotification;
    @XmlAttribute
    private String directPricingAllowed;
    @XmlAttribute
    private String status;

    public EvaluatorGroupDTO() {
    }

    public EvaluatorGroupDTO(String evaluatorGroupId, String name, String description, EvaluatorGroupDTO masterEvaluatorGroup, String isEvaluatorGroupMaster, String email, String sitePlannerNotification, String directPricingAllowed, String status) {
        this.evaluatorGroupId = evaluatorGroupId;
        this.name = name;
        this.description = description;
        this.masterEvaluatorGroup = masterEvaluatorGroup;
        this.isEvaluatorGroupMaster = isEvaluatorGroupMaster;
        this.email = email;
        this.sitePlannerNotification = sitePlannerNotification;
        this.directPricingAllowed = directPricingAllowed;
        this.status = status;
    }

    public String getEvaluatorGroupId() {
        return evaluatorGroupId;
    }

    public void setEvaluatorGroupId(String evaluatorGroupId) {
        this.evaluatorGroupId = evaluatorGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EvaluatorGroupDTO getMasterEvaluatorGroup() {
        return masterEvaluatorGroup;
    }

    public void setMasterEvaluatorGroup(EvaluatorGroupDTO masterEvaluatorGroup) {
        this.masterEvaluatorGroup = masterEvaluatorGroup;
    }

    public String getIsEvaluatorGroupMaster() {
        return isEvaluatorGroupMaster;
    }

    public void setIsEvaluatorGroupMaster(String isEvaluatorGroupMaster) {
        this.isEvaluatorGroupMaster = isEvaluatorGroupMaster;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSitePlannerNotification() {
        return sitePlannerNotification;
    }

    public void setSitePlannerNotification(String sitePlannerNotification) {
        this.sitePlannerNotification = sitePlannerNotification;
    }

    public String getDirectPricingAllowed() {
        return directPricingAllowed;
    }

    public void setDirectPricingAllowed(String directPricingAllowed) {
        this.directPricingAllowed = directPricingAllowed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
