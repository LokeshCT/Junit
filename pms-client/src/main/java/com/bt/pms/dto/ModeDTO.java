package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ModeDTO {
    @XmlAttribute
    private String modeName;
    @XmlAttribute
    private String modeDescription;
    @XmlAttribute
    private List<EvaluatorGroupDTO> evaluatorGroups;

    public ModeDTO() {
    }

    public ModeDTO(String modeName, String modeDescription, List<EvaluatorGroupDTO> evaluatorGroups) {
        this.modeName = modeName;
        this.modeDescription = modeDescription;
        this.evaluatorGroups = evaluatorGroups;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    public String getModeDescription() {
        return modeDescription;
    }

    public void setModeDescription(String modeDescription) {
        this.modeDescription = modeDescription;
    }

    public List<EvaluatorGroupDTO> getEvaluatorGroups() {
        return evaluatorGroups;
    }

    public void setEvaluatorGroups(List<EvaluatorGroupDTO> evaluatorGroups) {
        this.evaluatorGroups = evaluatorGroups;
    }
}
