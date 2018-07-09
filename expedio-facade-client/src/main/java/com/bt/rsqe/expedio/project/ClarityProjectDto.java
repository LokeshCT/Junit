package com.bt.rsqe.expedio.project;

import com.bt.rsqe.domain.bom.parameters.BFGOrganisationDetails;
import com.bt.rsqe.utils.AssertObject;
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


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ClarityProjectDto {
    private String uniqueId;
    private String projectId;
    private String projectTitle;
    private String projectGfr;
    private String projectDescription;
    private String projectOwnerEin;
    private Date projectStartDate;
    private String projectStartDateAsString;
    private Date projectEndDate;
    private String projectEndDateAsString;
    private String projectCustSac;

    public ClarityProjectDto() {
    }

    public ClarityProjectDto(String uniqueId, String projectId, String projectTitle, String projectGfr, String projectDescription, String projectOwnerEin, Date projectStartDate, Date projectEndDate, String projectCustSac) {
        this.uniqueId = uniqueId;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.projectGfr = projectGfr;
        this.projectDescription = projectDescription;
        this.projectOwnerEin = projectOwnerEin;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
        this.projectCustSac = projectCustSac;
        if(AssertObject.isNotNull(projectStartDate))
        this.projectStartDateAsString=new SimpleDateFormat ("YYYY/MM/dd ':' hh:mm:ss").format(projectStartDate);
        if(AssertObject.isNotNull(projectEndDate));
        this.projectEndDateAsString = new SimpleDateFormat ("YYYY/MM/dd ':' hh:mm:ss").format(projectEndDate);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProjectGfr() {
        return projectGfr;
    }

    public void setProjectGfr(String projectGfr) {
        this.projectGfr = projectGfr;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getProjectOwnerEin() {
        return projectOwnerEin;
    }

    public void setProjectOwnerEin(String projectOwnerEin) {
        this.projectOwnerEin = projectOwnerEin;
    }

    public Date getProjectStartDate() {
        return projectStartDate;
    }

    public String getProjectStartDateAsString() {
        if(projectStartDateAsString == null){
            SimpleDateFormat dateFormat = new SimpleDateFormat ("YYYY/MM/dd ':' hh:mm:ss");
            projectStartDateAsString = dateFormat.format(projectStartDate);
        }
        return projectStartDateAsString;
    }

    public void setProjectStartDate(Date projectStartDate) {
        this.projectStartDate = projectStartDate;

        SimpleDateFormat dateFormat = new SimpleDateFormat ("YYYY/MM/dd ':' hh:mm:ss");
        projectStartDateAsString = dateFormat.format(projectStartDate);
    }



    public Date getProjectEndDate() {
        return projectEndDate;
    }

    public String getProjectEndDateAsString() {
        if(projectEndDateAsString == null){
            SimpleDateFormat dateFormat = new SimpleDateFormat ("YYYY/MM/dd ':' hh:mm:ss");
            projectEndDateAsString = dateFormat.format(projectEndDate);
        }
        return projectEndDateAsString;
    }

    public void setProjectEndDate(Date projectEndDate) {
        this.projectEndDate = projectEndDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat ("YYYY/MM/dd ':' hh:mm:ss");
        projectEndDateAsString = dateFormat.format(projectEndDate);
    }

    public String getProjectCustSac() {
        return projectCustSac;
    }

    public void setProjectCustSac(String projectCustSac) {
        this.projectCustSac = projectCustSac;
    }
}
