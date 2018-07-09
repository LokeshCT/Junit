package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.sac.SacBulkInputDTO;
import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 03/09/15
 * Time: 18:29
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "SAC_BULK_UPLOAD")
public class SacBulkUploadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FileNameGenerator")
    @GenericGenerator(name = "FileNameGenerator",
                      strategy = "com.bt.rsqe.ape.repository.entities.SacBulkUploadFileNameGenerator",
                      parameters = {
                          @Parameter(name = "sequence", value = "createUser")
                      })
    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_DESCRIPTION")
    private String fileDesc;

    @OneToMany(mappedBy = "fileName", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<SacRequestEntity> siteRequests;

    @Column(name = "AVAILABILITY_STATUS")
    private String availabilityStatus;

    @Column(name = "VALIDATION_STATUS")
    private String validationStatus;

    @Column(name = "SALES_CHANNEL")
    private String salesChannel;

    @Column(name = "SHARE_POINT_ORG_DOC_ID")
    private String sharePointOrgDocId;

    @Column(name = "SHARE_POINT_FAIL_DOC_ID")
    private String sharePointFailDocId;

    @Column(name = "SHARE_POINT_RESULT_DOC_ID")
    private String sharePointResultDocId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "HOST_NAME")
    private String hostName;

    @Column(name ="ITERATION_COUNT")
    private Long iterationCount;

    @Column(name = "CREATE_DATETIME")
    private Timestamp createDate;

    @Column(name = "CREATED_USER")
    private String createUser;

    @Column(name = "UPDATE_DATETIME")
    private Timestamp updateDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDesc() {
        return fileDesc;
    }

    public void setFileDesc(String fileDesc) {
        this.fileDesc = fileDesc;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getSharePointOrgDocId() {
        return sharePointOrgDocId;
    }

    public void setSharePointOrgDocId(String sharePointOrgDocId) {
        this.sharePointOrgDocId = sharePointOrgDocId;
    }

    public String getSharePointFailDocId() {
        return sharePointFailDocId;
    }

    public void setSharePointFailDocId(String sharePointFailDocId) {
        this.sharePointFailDocId = sharePointFailDocId;
    }

    public String getSharePointResultDocId() {
        return sharePointResultDocId;
    }

    public void setSharePointResultDocId(String sharePointResultDocId) {
        this.sharePointResultDocId = sharePointResultDocId;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public List<SacRequestEntity> getSiteRequests() {
        return siteRequests;
    }

    public void setSiteRequests(List<SacRequestEntity> siteRequests) {
        this.siteRequests = siteRequests;
    }

    public Long getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(Long iterationCount) {
        this.iterationCount = iterationCount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public SacBulkInputDTO toDto() {
        SacBulkInputDTO sacBulkInputDTO = new SacBulkInputDTO();
        sacBulkInputDTO.setFileName(fileName);
        sacBulkInputDTO.setFileDesc(fileDesc);
        sacBulkInputDTO.setSharePointFailDocId(sharePointFailDocId);
        sacBulkInputDTO.setSharePointOrgDocId(sharePointOrgDocId);
        sacBulkInputDTO.setSharePointResultDocId(sharePointResultDocId);
        sacBulkInputDTO.setAvailabilityStatus(availabilityStatus);
        sacBulkInputDTO.setUserId(createUser);
        sacBulkInputDTO.setTimeStamp(createDate);
        sacBulkInputDTO.setSalesChannel(salesChannel);
        sacBulkInputDTO.setValidationStatus(validationStatus);
        sacBulkInputDTO.setHostName(hostName);
        sacBulkInputDTO.setUserName(userName);
        if (this.getSiteRequests() != null && this.getSiteRequests().size() > 0) {
            List<SacSiteDTO> sites = new ArrayList<SacSiteDTO>();
            for (SacRequestEntity siteEntity : getSiteRequests()) {
                sites.add(siteEntity.toDto());

            }
            sacBulkInputDTO.setSites(sites);
        }
        return sacBulkInputDTO;
    }

    public SacBulkInputDTO toShallowDto() {
        SacBulkInputDTO sacBulkInputDTO = new SacBulkInputDTO();
        sacBulkInputDTO.setFileName(fileName);
        sacBulkInputDTO.setFileDesc(fileDesc);
        sacBulkInputDTO.setSharePointFailDocId(sharePointFailDocId);
        sacBulkInputDTO.setSharePointOrgDocId(sharePointOrgDocId);
        sacBulkInputDTO.setSharePointResultDocId(sharePointResultDocId);
        sacBulkInputDTO.setAvailabilityStatus(availabilityStatus);
        sacBulkInputDTO.setUserId(createUser);
        sacBulkInputDTO.setTimeStamp(createDate);
        sacBulkInputDTO.setSalesChannel(salesChannel);
        sacBulkInputDTO.setValidationStatus(validationStatus);
        sacBulkInputDTO.setUserName(userName);
        sacBulkInputDTO.setHostName(hostName);
        sacBulkInputDTO.setItrCount(iterationCount);
        return sacBulkInputDTO;
    }
}
