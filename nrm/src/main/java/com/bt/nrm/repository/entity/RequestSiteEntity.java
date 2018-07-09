package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestAttributeDTO;
import com.bt.nrm.dto.RequestPriceGroupDTO;
import com.bt.nrm.dto.RequestSiteDTO;
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

@Entity
@Table(name = "REQUEST_SITE")
public class RequestSiteEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "REQUEST_SITE_ID")
    private String requestSiteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID")
    private RequestEntity requestEntity;

    @Column(name = "SITE_ID")
    private String siteId;

    @Column(name = "SITE_NAME")
    private String siteName;

    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @Column(name = "COUNTRY_ISO_ALPHA2_CODE")
    private String countryISOAlpha2Code;

    @OneToMany(mappedBy = "requestSiteEntity",cascade = CascadeType.ALL)
    private List<RequestAttributeEntity> siteAttributes;

    @OneToMany(mappedBy = "requestSiteEntity",cascade = CascadeType.ALL)
    private List<RequestPriceGroupEntity> priceGroups;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Timestamp modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    public RequestSiteEntity() {
    }

    public RequestSiteEntity(String requestSiteId, RequestEntity requestEntity, String siteId, String siteName, String countryName, String countryISOAlpha2Code, List<RequestAttributeEntity> siteAttributes, List<RequestPriceGroupEntity> priceGroups, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.requestSiteId = requestSiteId;
        this.requestEntity = requestEntity;
        this.siteId = siteId;
        this.siteName = siteName;
        this.countryName = countryName;
        this.countryISOAlpha2Code = countryISOAlpha2Code;
        this.siteAttributes = siteAttributes;
        this.priceGroups = priceGroups;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    public String getRequestSiteId() {
        return requestSiteId;
    }

    public void setRequestSiteId(String requestSiteId) {
        this.requestSiteId = requestSiteId;
    }

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(RequestEntity requestEntity) {
        this.requestEntity = requestEntity;
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

    public List<RequestAttributeEntity> getSiteAttributes() {
        return siteAttributes;
    }

    public void setSiteAttributes(List<RequestAttributeEntity> siteAttributes) {
        this.siteAttributes = siteAttributes;
    }

    public List<RequestPriceGroupEntity> getPriceGroups() {
        return priceGroups;
    }

    public void setPriceGroups(List<RequestPriceGroupEntity> priceGroups) {
        this.priceGroups = priceGroups;
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

    public RequestSiteDTO toDTO(RequestSiteDTO dto){
        if(dto!=null){
            dto.setRequestSiteId(this.getRequestSiteId());
            dto.setSiteId(this.getSiteId());
            dto.setSiteName(this.getSiteName());
            dto.setCountryName(this.getCountryName());
            dto.setCountryISOAlpha2Code(this.getCountryISOAlpha2Code());

            if((isNull(this.getSiteAttributes())) || (this.getSiteAttributes().size() == 0)){
                dto.setSiteAttributes(new ArrayList<RequestAttributeDTO>());
            }else{
                if(isNull(dto.getSiteAttributes())){
                    dto.setSiteAttributes(new ArrayList<RequestAttributeDTO>());
                }
                for(RequestAttributeEntity requestAttributeEntity : this.getSiteAttributes()){
                    dto.getSiteAttributes().add(requestAttributeEntity.toDTO(new RequestAttributeDTO()));
                }
            }
            if((isNull(this.getPriceGroups())) || (this.getPriceGroups().size() == 0)){
                dto.setPriceGroups(new ArrayList<RequestPriceGroupDTO>());
            }else{
                if(isNull(dto.getPriceGroups())){
                    dto.setPriceGroups(new ArrayList<RequestPriceGroupDTO>());
                }
                for(RequestPriceGroupEntity requestPriceGroupEntity : this.getPriceGroups()){
                    dto.getPriceGroups().add(requestPriceGroupEntity.toDTO(new RequestPriceGroupDTO()));
                }
            }
            dto.setCreatedUser(this.getCreatedUser());
            dto.setCreatedDate(this.getCreatedDate());
            dto.setModifiedDate(this.getModifiedDate());
            dto.setModifiedUser(this.getModifiedUser());
        }
        return dto;
    }

    public RequestSiteDTO toNewDTO(){
        return toDTO(new RequestSiteDTO());
    }
}
