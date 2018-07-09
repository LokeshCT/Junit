package com.bt.cqm.repository.user;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


public class RagConfigurationId implements Serializable {

    private String mpcCode;

    private String stateCode;

    private String failLevel;

    private String ragCode;

    public RagConfigurationId() {
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
