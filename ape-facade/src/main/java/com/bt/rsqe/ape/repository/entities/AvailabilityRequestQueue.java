package com.bt.rsqe.ape.repository.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by 605783162 on 29/09/2015.
 */

@Entity
@Table(name = "AVAILABILITY_REQUEST_QUEUE")
public class AvailabilityRequestQueue {
    @Id
    @SequenceGenerator(name = "ARQ_ID", sequenceName = "ARQ_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ARQ_ID")
    @Column(name="ARQ_ID")
    private Long id;

    @Column(name = "CLIENT_REQUEST")
    private String clientRequestId;

    @Column(name = "APE_REQUEST")
    private String apeRequestId;

    @Column(name = "SITE_ID")
    private String siteId;

    @Column(name = "TASK")
    private String task;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_ON")
    private Date createdOn;

    public AvailabilityRequestQueue() {
    }

    public AvailabilityRequestQueue(String clientRequestId, String apeRequestId, String siteId, String task, String status, Date createdOn) {
        this.clientRequestId = clientRequestId;
        this.apeRequestId = apeRequestId;
        this.siteId = siteId;
        this.task = task;
        this.status = status;
        this.createdOn = createdOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    public String getApeRequestId() {
        return apeRequestId;
    }

    public void setApeRequestId(String apeRequestId) {
        this.apeRequestId = apeRequestId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}
