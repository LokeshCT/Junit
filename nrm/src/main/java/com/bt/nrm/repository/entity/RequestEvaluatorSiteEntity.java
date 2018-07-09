package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestEvaluatorPriceGroupDTO;
import com.bt.nrm.dto.RequestEvaluatorSiteDTO;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNull;

/**
 * Created by 608143048 on 02/02/2016.
 */
@Entity
@Table(name = "REQUEST_EVALUATOR_SITE")
public class RequestEvaluatorSiteEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "REQUEST_EVALUATOR_SITE_ID")
    private String requestEvaluatorSiteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_EVALUATOR_ID")
    private RequestEvaluatorEntity requestEvaluatorEntity;

    @Column(name = "SITE_ID")
    private String siteId;

    @Column(name = "SITE_NAME")
    private String siteName;

    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @Column(name = "COUNTRY_ISO_ALPHA2_CODE")
    private String countryISOAlpha2Code;

    @OneToMany(mappedBy = "requestEvaluatorSiteEntity",cascade = CascadeType.ALL)
    private List<RequestEvaluatorPriceGroupEntity> requestEvaluatorPriceGroups;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Timestamp modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    public RequestEvaluatorSiteEntity() {
    }

    public RequestEvaluatorSiteEntity(String requestEvaluatorSiteId, RequestEvaluatorEntity requestEvaluatorEntity, String siteId, String siteName, String countryName, String countryISOAlpha2Code, List<RequestEvaluatorPriceGroupEntity> requestEvaluatorPriceGroups, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.requestEvaluatorSiteId = requestEvaluatorSiteId;
        this.requestEvaluatorEntity = requestEvaluatorEntity;
        this.siteId = siteId;
        this.siteName = siteName;
        this.countryName = countryName;
        this.countryISOAlpha2Code = countryISOAlpha2Code;
        this.requestEvaluatorPriceGroups = requestEvaluatorPriceGroups;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    public String getRequestEvaluatorSiteId() {
        return requestEvaluatorSiteId;
    }

    public void setRequestEvaluatorSiteId(String requestEvaluatorSiteId) {
        this.requestEvaluatorSiteId = requestEvaluatorSiteId;
    }

    public RequestEvaluatorEntity getRequestEvaluatorEntity() {
        return requestEvaluatorEntity;
    }

    public void setRequestEvaluatorEntity(RequestEvaluatorEntity requestEvaluatorEntity) {
        this.requestEvaluatorEntity = requestEvaluatorEntity;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryISOAlpha2Code() {
        return countryISOAlpha2Code;
    }

    public void setCountryISOAlpha2Code(String countryISOAlpha2Code) {
        this.countryISOAlpha2Code = countryISOAlpha2Code;
    }

    public List<RequestEvaluatorPriceGroupEntity> getRequestEvaluatorPriceGroups() {
        return requestEvaluatorPriceGroups;
    }

    public void setRequestEvaluatorPriceGroups(List<RequestEvaluatorPriceGroupEntity> requestEvaluatorPriceGroups) {
        this.requestEvaluatorPriceGroups = requestEvaluatorPriceGroups;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public RequestEvaluatorSiteDTO toDTO(RequestEvaluatorSiteDTO dto){
        if(dto!=null){
            dto.setRequestEvaluatorSiteId(this.getRequestEvaluatorSiteId());
            dto.setSiteId(this.getSiteId());
            dto.setSiteName(this.getSiteName());
            dto.setCountryName(this.getCountryName());
            dto.setCountryISOAlpha2Code(this.getCountryISOAlpha2Code());

            if((isNull(this.getRequestEvaluatorPriceGroups())) || (this.getRequestEvaluatorPriceGroups().size() == 0)){
                dto.setRequestEvaluatorPriceGroups(new ArrayList<RequestEvaluatorPriceGroupDTO>());
            }else{
                if(isNull(dto.getRequestEvaluatorPriceGroups())){
                    dto.setRequestEvaluatorPriceGroups(new ArrayList<RequestEvaluatorPriceGroupDTO>());
                }
                for(RequestEvaluatorPriceGroupEntity requestEvaluatorPriceGroupEntity : this.getRequestEvaluatorPriceGroups()){
                    dto.getRequestEvaluatorPriceGroups().add(requestEvaluatorPriceGroupEntity.toDTO(new RequestEvaluatorPriceGroupDTO()));
                }
            }
            dto.setCreatedUser(this.getCreatedUser());
            dto.setCreatedDate(this.getCreatedDate());
            dto.setModifiedDate(this.getModifiedDate());
            dto.setModifiedUser(this.getModifiedUser());
        }
        return dto;
    }

    public RequestEvaluatorSiteDTO toNewDTO(){
        return toDTO(new RequestEvaluatorSiteDTO());
    }

}
