package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.IdClass(ApeQrefDetailEntityPK.class)
@Table(name = "APE_QREF_DETAIL")
@Entity
public class ApeQrefDetailEntity  implements Serializable {

    public ApeQrefDetailEntity(){

    }

    public ApeQrefDetailEntity(String requestId, String qrefId, String attributeName, String attributeValue, Integer sequence){
        this.attributeValue=attributeValue;
        this.requestId=requestId;
        this.qrefId=qrefId;
        this.attributeName=attributeName;
        this.sequence = sequence;
    }

    public ApeQrefDetailEntity(ApeQrefDetailEntity original){
        this.attributeValue=original.attributeValue;
        this.requestId=original.requestId;
        this.qrefId=original.qrefId;
        this.attributeName=original.attributeName;
    }

    public ApeQrefAttributeDetail dto() {
        return new ApeQrefAttributeDetail(getAttributeName(), getAttributeValue(), getSequence());
    }

    private String requestId;

    @Column(name = "REQUEST_ID")
    @Id
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    private String qrefId;

    @Column(name = "QREF_ID")
    @Id
    public String getQrefId() {
        return qrefId;
    }

    public void setQrefId(String qrefId) {
        this.qrefId = qrefId;
    }

    private String attributeName;

    @Column(name = "ATTRIBUTE_NAME")
    @Id
    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    private String attributeValue;

    @Column(name = "ATTRIBUTE_VALUE")
    @Basic
    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    @Column(name = "SEQUENCE")
    private Integer sequence;

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApeQrefDetailEntity that = (ApeQrefDetailEntity) o;

        if (attributeName != null ? !attributeName.equals(that.attributeName) : that.attributeName != null) {
            return false;
        }
        if (attributeValue != null ? !attributeValue.equals(that.attributeValue) : that.attributeValue != null) {
            return false;
        }
        if (qrefId != null ? !qrefId.equals(that.qrefId) : that.qrefId != null) {
            return false;
        }
        if (requestId != null ? !requestId.equals(that.requestId) : that.requestId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = requestId != null ? requestId.hashCode() : 0;
        result = 31 * result + (qrefId != null ? qrefId.hashCode() : 0);
        result = 31 * result + (attributeName != null ? attributeName.hashCode() : 0);
        result = 31 * result + (attributeValue != null ? attributeValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private ApeRequestEntity apeRequestByRequestId;

    @ManyToOne
    public
    @javax.persistence.JoinColumn(name = "REQUEST_ID", referencedColumnName = "REQUEST_ID", nullable = false, insertable = false, updatable = false)
    ApeRequestEntity getApeRequestByRequestId() {
        return apeRequestByRequestId;
    }

    public void setApeRequestByRequestId(ApeRequestEntity apeRequestByRequestId) {
        this.apeRequestByRequestId = apeRequestByRequestId;
    }
}
