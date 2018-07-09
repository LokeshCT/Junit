package com.bt.rsqe.expedio.audit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/26/15
 * Time: 8:19 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AuditDetailWrapper {

    private List<AuditDetailDTO> auditDetails;

    public List<AuditDetailDTO> getAuditDetails() {
        return auditDetails;
    }

    public void setAuditDetails(List<AuditDetailDTO> auditDetails) {
        this.auditDetails = auditDetails;
    }
}
