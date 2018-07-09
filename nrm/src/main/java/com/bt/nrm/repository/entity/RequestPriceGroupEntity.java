package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestPriceGroupDTO;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "REQUEST_PRICE_GROUP")
public class RequestPriceGroupEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "REQUEST_PG_ID")
    private String requestPriceGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_SITE_ID")
    private RequestSiteEntity requestSiteEntity;

    @Column(name = "SQE_UNIQUE_ID")
    private String sqeUniqueId;

    @Column(name = "PRICE_GROUP_TYPE")
    private String priceGroupType;

    @Column(name = "PRICE_GROUP_DESC")
    private String priceGroupDescription;

    @Column(name = "ONE_OFF_RECOMMENDED_RETAIL")
    private String oneOffRecommendedRetail;

    @Column(name = "RECURRING_RECOMMENDED_RETAIL")
    private String recurringRecommendedRetail;

    @Column(name = "NRC_PRICE_TO_PARTNER")
    private String nrcPriceToPartner;

    @Column(name = "RC_PRICE_TO_PARTNER")
    private String rcPriceToPartner;

    @Column(name = "ONE_OFF_COST")
    private String oneOffCost;

    @Column(name = "RECURRING_COST")
    private String recurringCost;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Timestamp modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    public RequestPriceGroupEntity() {
    }

    public RequestPriceGroupEntity(String requestPriceGroupId, RequestSiteEntity requestSiteEntity, String sqeUniqueId, String priceGroupType, String priceGroupDescription, String oneOffRecommendedRetail, String recurringRecommendedRetail, String nrcPriceToPartner, String rcPriceToPartner, String oneOffCost, String recurringCost, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.requestPriceGroupId = requestPriceGroupId;
        this.requestSiteEntity = requestSiteEntity;
        this.sqeUniqueId = sqeUniqueId;
        this.priceGroupType = priceGroupType;
        this.priceGroupDescription = priceGroupDescription;
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
        this.recurringRecommendedRetail = recurringRecommendedRetail;
        this.nrcPriceToPartner = nrcPriceToPartner;
        this.rcPriceToPartner = rcPriceToPartner;
        this.oneOffCost = oneOffCost;
        this.recurringCost = recurringCost;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    public String getRequestPriceGroupId() {
        return requestPriceGroupId;
    }

    public void setRequestPriceGroupId(String requestPriceGroupId) {
        this.requestPriceGroupId = requestPriceGroupId;
    }

    public RequestSiteEntity getRequestSiteEntity() {
        return requestSiteEntity;
    }

    public void setRequestSiteEntity(RequestSiteEntity requestSiteEntity) {
        this.requestSiteEntity = requestSiteEntity;
    }

    public String getPriceGroupType() {
        return priceGroupType;
    }

    public void setPriceGroupType(String priceGroupType) {
        priceGroupType = priceGroupType;
    }

    public String getPriceGroupDescription() {
        return priceGroupDescription;
    }

    public void setPriceGroupDescription(String priceGroupDescription) {
        priceGroupDescription = priceGroupDescription;
    }

    public String getOneOffRecommendedRetail() {
        return oneOffRecommendedRetail;
    }

    public void setOneOffRecommendedRetail(String oneOffRecommendedRetail) {
        this.oneOffRecommendedRetail = oneOffRecommendedRetail;
    }

    public String getRecurringRecommendedRetail() {
        return recurringRecommendedRetail;
    }

    public void setRecurringRecommendedRetail(String recurringRecommendedRetail) {
        this.recurringRecommendedRetail = recurringRecommendedRetail;
    }

    public String getNrcPriceToPartner() {
        return nrcPriceToPartner;
    }

    public void setNrcPriceToPartner(String nrcPriceToPartner) {
        this.nrcPriceToPartner = nrcPriceToPartner;
    }

    public String getRcPriceToPartner() {
        return rcPriceToPartner;
    }

    public void setRcPriceToPartner(String rcPriceToPartner) {
        this.rcPriceToPartner = rcPriceToPartner;
    }

    public String getOneOffCost() {
        return oneOffCost;
    }

    public void setOneOffCost(String oneOffCost) {
        this.oneOffCost = oneOffCost;
    }

    public String getRecurringCost() {
        return recurringCost;
    }

    public void setRecurringCost(String recurringCost) {
        this.recurringCost = recurringCost;
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

    public String getSqeUniqueId() {
        return sqeUniqueId;
    }

    public void setSqeUniqueId(String siteUniqueId) {
        this.sqeUniqueId = siteUniqueId;
    }

    public RequestPriceGroupDTO toDTO(RequestPriceGroupDTO dto){
        if(dto!=null){
            dto.setRequestPriceGroupId(this.getRequestPriceGroupId());
            dto.setSiteId(this.getRequestSiteEntity().getSiteId());
            dto.setSiteUniqueId(this.getSqeUniqueId());
            dto.setPriceGroupType(this.getPriceGroupType());
            dto.setPriceGroupDescription(this.getPriceGroupDescription());
            dto.setOneOffRecommendedRetail(this.getOneOffRecommendedRetail());
            dto.setRecurringRecommendedRetail(this.getRecurringRecommendedRetail());
            dto.setNrcPriceToPartner(this.getNrcPriceToPartner());
            dto.setRcPriceToPartner(this.getRcPriceToPartner());
            dto.setOneOffCost(this.getOneOffCost());
            dto.setRecurringCost(this.getRecurringCost());
        }
        return dto;
    }

    public RequestPriceGroupDTO toNewDTO(){
        return toDTO(new RequestPriceGroupDTO());
    }
}
