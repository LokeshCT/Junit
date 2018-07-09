package com.bt.rsqe.ape.repository.entities;


import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.domain.product.ProductOffering;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.*;

@Table(name = "APE_REQUEST")
@Entity
public class ApeRequestEntity implements Serializable{

    @Id
    @Column(name = "REQUEST_ID")
    private String requestId;

    @Column(name = "UNIQUE_ID")
    private String uniqueId;

    @Column(name = "USER_LOGIN")
    private String userLogin;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private QrefRequestStatus.Status status = QrefRequestStatus.Status.WAITING;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "PSTN_TEL_LINE")
    private String pstnTelLine;

    @Column(name = "SITE_TEL_NUMBER")
    private String siteTelNumber;

    @OneToMany(mappedBy = "apeRequestByRequestId", fetch = FetchType.LAZY)
    @OrderBy(value = "sequence")
    private List<ApeQrefDetailEntity> apeQrefDetailsByRequestId = new ArrayList<ApeQrefDetailEntity>();

    @OneToMany(mappedBy = "key.requestId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ApeRequestDetailEntity> apeRequestAttributes = new ArrayList<ApeRequestDetailEntity>();

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "UPDATED_DATE")
    private Date updatedDate;

    @Column(name = "EXPECTED_RESPONSE_TIME")
    private String expectedResponseTime;

    @Column(name = "ACCESS_METHOD_TYPE")
    private String accessMethodType;


    public ApeRequestEntity() {
    }

    public ApeRequestEntity(String requestId, String uniqueId, String userLogin, String currency, ApeRequestDetailEntity... requestDetailEntities) {
        this.requestId=requestId;
        this.uniqueId=uniqueId;
        this.userLogin = userLogin;
        this.currency = currency;
        for (ApeRequestDetailEntity requestDetailEntity : requestDetailEntities) {
            apeRequestAttributes.add(requestDetailEntity);
        }
    }


    public String getRequestId() {
        return requestId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getPstnTelLine() {
        return pstnTelLine;
    }

    public void setPstnTelLine(String pstnTelLine) {
         this.pstnTelLine = pstnTelLine;
    }

    public String getSiteTelNumber() {
        return siteTelNumber;
    }

    public void setSiteTelNumber(String siteTelNumber) {
        this.siteTelNumber = siteTelNumber;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setStatus(QrefRequestStatus.Status status) {
        this.status = status;
    }

    public QrefRequestStatus.Status getStatus() {
        return status;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public List<ApeQrefDetailEntity> getApeQrefDetailsByRequestId() {
        return apeQrefDetailsByRequestId;
    }

    public List<ApeRequestDetailEntity> getApeRequestAttributes() {
        return apeRequestAttributes;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = this.updatedDate =  new DateTime().toDate();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new DateTime().toDate();
    }

    public QrefRequestStatus toQrefRequestStatusDto() {
        return new QrefRequestStatus(uniqueId, requestId, status, errorMessage);
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isSimulatedRequest() {
        Optional<ApeRequestDetailEntity> apeRequestDetailEntityOptional = Iterables.tryFind(apeRequestAttributes, new Predicate<ApeRequestDetailEntity>() {
            @Override
            public boolean apply(ApeRequestDetailEntity input) {
                return input.getAttributeName().equals(ProductOffering.APE_FLAG);
            }
        });

        return apeRequestDetailEntityOptional.isPresent() && "No".equals(apeRequestDetailEntityOptional.get().getAttributeValue());
    }

    public static ApeRequestEntity toEntity(String requestId, String uniqueId, ApeQrefRequestDTO requestDTO) {
        List<ApeRequestDetailEntity> apeRequestAttributes = newArrayList();

        for (ApeQrefRequestDTO.AssetAttribute assetAttribute : requestDTO.attributes()) {
            apeRequestAttributes.add( new ApeRequestDetailEntity(requestId, assetAttribute.getAttributeName(), assetAttribute.getAttributeValue()));
        }

        final ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, requestDTO.user().getLoginName(), requestDTO.currency(), apeRequestAttributes.toArray(new ApeRequestDetailEntity[]{}));
        requestEntity.setSiteTelNumber(null != requestDTO.siteDetail() ? requestDTO.siteDetail().getTelephoneNumber() : null);
        requestEntity.setAccessMethodType(requestDTO.getAccessMethodType());
        return requestEntity;
    }

    public List<ApeQrefAttributeDetail> requestAttributes() {
       return newArrayList(transform(apeRequestAttributes, new Function<ApeRequestDetailEntity, ApeQrefAttributeDetail>() {
           @Override
           public ApeQrefAttributeDetail apply(ApeRequestDetailEntity input) {
               return new ApeQrefAttributeDetail(input.getAttributeName(), input.getAttributeValue());
           }
       }));
    }

    public void setExpectedResponseTime(String expectedResponseTime) {
        this.expectedResponseTime = expectedResponseTime;
    }

    public String getExpectedResponseTime() {
        return expectedResponseTime;
    }

    public String getAccessMethodType() {
        return accessMethodType;
    }

    public void setAccessMethodType(String accessMethodType) {
        this.accessMethodType = accessMethodType;
    }
}
