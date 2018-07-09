package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class WorkflowTypeDTO {

    @XmlAttribute
    private String workflowName;
    @XmlAttribute
    private String workflowDescription;
    @XmlAttribute
    private EvaluatedDTO evaluated;
    @XmlAttribute
    private FastTrackDTO fastTrack;
    @XmlAttribute
    private ZeroTouchDTO zeroTouch;

    public WorkflowTypeDTO() {
    }

    public WorkflowTypeDTO(String workflowType, EvaluatedDTO evaluated, FastTrackDTO fastTrack, ZeroTouchDTO zeroTouch) {
        this.workflowName = workflowType;
        this.evaluated = evaluated;
        this.fastTrack = fastTrack;
        this.zeroTouch = zeroTouch;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowType) {
        this.workflowName = workflowType;
    }

    public EvaluatedDTO getEvaluated() {
        return evaluated;
    }

    public void setEvaluated(EvaluatedDTO evaluated) {
        this.evaluated = evaluated;
    }

    public FastTrackDTO getFastTrack() {
        return fastTrack;
    }

    public void setFastTrack(FastTrackDTO fastTrack) {
        this.fastTrack = fastTrack;
    }

    public ZeroTouchDTO getZeroTouch() {
        return zeroTouch;
    }

    public void setZeroTouch(ZeroTouchDTO zeroTouch) {
        this.zeroTouch = zeroTouch;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public void setWorkflowDescription(String workflowDescription) {
        this.workflowDescription = workflowDescription;
    }
}
