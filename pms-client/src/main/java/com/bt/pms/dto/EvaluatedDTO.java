package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EvaluatedDTO {
    @XmlAttribute
    private ModeDTO mode;

    public EvaluatedDTO() {
    }

    public EvaluatedDTO(ModeDTO mode) {
        this.mode = mode;
    }

    public ModeDTO getMode() {
        return mode;
    }

    public void setMode(ModeDTO mode) {
        this.mode = mode;
    }
}
