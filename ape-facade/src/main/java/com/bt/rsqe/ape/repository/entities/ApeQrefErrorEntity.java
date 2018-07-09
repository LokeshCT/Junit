package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.ApeQrefError;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Table(name = "APE_QREF_ERROR")
@Entity
public class ApeQrefErrorEntity {
    @Id
    @Column(name = "ERROR_ID")
    private String errorId;

    @Column(name = "QREF_ID")
    private String qrefId;

    @Column(name = "ERROR_CODE")
    private String errorCode;

    @Column(name = "ERROR_MSG")
    private String errorMsg;

    public ApeQrefErrorEntity(){

    }

    public ApeQrefErrorEntity(String qrefId, String errorCode, String errorMsg) {
        this.errorId = UUID.randomUUID().toString();
        this.qrefId = qrefId;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public ApeQrefError toDto() {
        return new ApeQrefError(errorCode, errorMsg);
    }

    public String getErrorId() {
        return errorId;
    }

    public String getQrefId() {
        return qrefId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(qrefId)
            .append(errorCode)
            .append(errorMsg)
            .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApeQrefErrorEntity that = (ApeQrefErrorEntity) o;

        return new EqualsBuilder()
            .append(qrefId, that.qrefId)
            .append(errorCode, that.errorCode)
            .append(errorMsg, that.errorMsg)
            .isEquals();
    }
}
