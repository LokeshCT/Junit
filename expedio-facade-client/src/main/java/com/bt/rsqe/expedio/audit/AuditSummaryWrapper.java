package com.bt.rsqe.expedio.audit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/26/15
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AuditSummaryWrapper {

    @XmlElement
    private List<AuditSummaryDTO> auditRecords;

    public List<AuditSummaryDTO> getAuditRecords() {
        return auditRecords;
    }

    public void setAuditRecords(List<AuditSummaryDTO> auditRecords) {
        this.auditRecords = auditRecords;
    }
}
