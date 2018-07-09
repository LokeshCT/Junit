package com.bt.cqm.repository.user;


import com.bt.cqm.dto.user.UserRoleMasterDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "RAG_CONFIGURATION")
@IdClass(RagConfigurationId.class)
public class RagConfigurationEntity{

    @Id
    @Column(name = "MPC_CODE")
    private String mpcCode;

    @Id
    @Column(name = "STATE_CODE")
    private String stateCode;

    @Id
    @Column(name = "FAIL_LEVEL")
    private String failLevel;

    @Id
    @Column(name = "RAG_CODE")
    private String ragCode;



    public RagConfigurationEntity() {
    }

    public RagConfigurationEntity(String mpcCode, String stateCode, String failLevel, String ragCode) {
        this.mpcCode = mpcCode;
        this.stateCode = stateCode;
        this.failLevel = failLevel;
        this.ragCode = ragCode;
    }



    public String getMpcCode() {
        return mpcCode;
    }

    public void setMpcCode(String mpcCode) {
        this.mpcCode = mpcCode;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getFailLevel() {
        return failLevel;
    }

    public void setFailLevel(String failLevel) {
        this.failLevel = failLevel;
    }

    public String getRagCode() {
        return ragCode;
    }

    public void setRagCode(String ragCode) {
        this.ragCode = ragCode;
    }

    ///CLOVER:ON
}
