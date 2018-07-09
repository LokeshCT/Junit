package com.bt.rsqe.expedio.project;

import com.bt.rsqe.domain.bom.parameters.BFGOrganisationDetails;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 01/12/15
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */


import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ClarityProjectRequestDto {
    private String projectId;
    private String projectName;
    private String sacId;

    public ClarityProjectRequestDto() {
    }

    public ClarityProjectRequestDto(String projectId, String projectName, String sacId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.sacId = sacId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSacId() {
        return sacId;
    }

    public void setSacId(String sacId) {
        this.sacId = sacId;
    }
}
