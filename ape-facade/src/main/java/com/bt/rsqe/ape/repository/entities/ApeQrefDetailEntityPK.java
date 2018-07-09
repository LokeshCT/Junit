package com.bt.rsqe.ape.repository.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class ApeQrefDetailEntityPK implements Serializable {
    private String requestId;

    public ApeQrefDetailEntityPK() {
    }

    public ApeQrefDetailEntityPK(String requestId, String qrefId, String attributeName) {
        this.requestId=requestId;
        this.qrefId=qrefId;
        this.attributeName=attributeName;
    }

    @Id
    @Column(name = "REQUEST_ID")
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    private String qrefId;

    @Id
    @Column(name = "QREF_ID")
    public String getQrefId() {
        return qrefId;
    }

    public void setQrefId(String qrefId) {
        this.qrefId = qrefId;
    }

    private String attributeName;

    @Id
    @Column(name = "ATTRIBUTE_NAME")
    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApeQrefDetailEntityPK that = (ApeQrefDetailEntityPK) o;

        if (attributeName != null ? !attributeName.equals(that.attributeName) : that.attributeName != null) {
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
        return result;
    }
}
