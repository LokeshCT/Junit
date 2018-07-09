package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestEvaluatorPriceGroupDTO;
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
@Table(name="REQUEST_EVALUATOR_PRICE_GROUP")
public class RequestEvaluatorPriceGroupEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "REQUEST_EVALUATOR_PG_ID")
    private String requestEvaluatorPriceGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_EVALUATOR_SITE_ID")
    private RequestEvaluatorSiteEntity requestEvaluatorSiteEntity;

    @Column(name = "PRICE_GROUP_TYPE")
    private String PriceGroupType;

    @Column(name = "PRICE_GROUP_DESC")
    private String PriceGroupDescription;

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

    public RequestEvaluatorPriceGroupEntity(){
        //default constructor
    }

    public RequestEvaluatorPriceGroupEntity(String requestEvaluatorPriceGroupId, RequestEvaluatorSiteEntity requestEvaluatorSiteEntity, String priceGroupType, String priceGroupDescription, String oneOffRecommendedRetail, String recurringRecommendedRetail, String nrcPriceToPartner, String rcPriceToPartner, String oneOffCost, String recurringCost, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.requestEvaluatorPriceGroupId = requestEvaluatorPriceGroupId;
        this.requestEvaluatorSiteEntity = requestEvaluatorSiteEntity;
        PriceGroupType = priceGroupType;
        PriceGroupDescription = priceGroupDescription;
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

    public String getRequestEvaluatorPriceGroupId() {
        return requestEvaluatorPriceGroupId;
    }

    public void setRequestEvaluatorPriceGroupId(String requestEvaluatorPriceGroupId) {
        this.requestEvaluatorPriceGroupId = requestEvaluatorPriceGroupId;
    }

    public RequestEvaluatorSiteEntity getRequestEvaluatorSiteEntity() {
        return requestEvaluatorSiteEntity;
    }

    public void setRequestEvaluatorSiteEntity(RequestEvaluatorSiteEntity requestEvaluatorSiteEntity) {
        this.requestEvaluatorSiteEntity = requestEvaluatorSiteEntity;
    }

    public String getPriceGroupType() {
        return PriceGroupType;
    }

    public void setPriceGroupType(String priceGroupType) {
        PriceGroupType = priceGroupType;
    }

    public String getPriceGroupDescription() {
        return PriceGroupDescription;
    }

    public void setPriceGroupDescription(String priceGroupDescription) {
        PriceGroupDescription = priceGroupDescription;
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

    public RequestEvaluatorPriceGroupDTO toDTO(RequestEvaluatorPriceGroupDTO dto){
        if(dto!=null){
            dto.setRequestEvaluatorPriceGroupId(this.getRequestEvaluatorPriceGroupId());
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

    public RequestEvaluatorPriceGroupDTO toNewDTO(){
        return toDTO(new RequestEvaluatorPriceGroupDTO());
    }
}
