package com.asidua.statsintegration.services;

import com.google.common.base.Strings;

public class ParameterMap {
    String testId = null;
    String testName = null;
    String projectId = null;
    String customerId = null;
    String quoteName = null;
    String triggerEin = null;
    String URI = null;

    public ParameterMap(){
    }

    public Errors validate(){
        boolean ret = true;
        Errors errors = new Errors("Post Parameter Map");

        if (Strings.isNullOrEmpty(testId)){
           ret = false;
            errors.add("testId","is missing");
        }

        if (Strings.isNullOrEmpty(testName)){
           ret = false;
            errors.add("testName","is missing");
        }

        if (Strings.isNullOrEmpty(projectId)){
           ret = false;
            errors.add("projectId","is missing");
        }

        if (Strings.isNullOrEmpty(customerId)){
           ret = false;
            errors.add("customerId","is missing");
        }

        if (Strings.isNullOrEmpty(quoteName)){
           ret = false;
            errors.add("quoteName","is missing");
        }

        if (Strings.isNullOrEmpty(triggerEin)){
           ret = false;
            errors.add("triggerEin","is missing");
        }
        if (Strings.isNullOrEmpty(URI)){
           ret = false;
            errors.add("URI","is missing");
        }


        return errors;
    }


    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }

    public String getTriggerEin() {
        return triggerEin;
    }

    public void setTriggerEin(String triggerEin) {
        this.triggerEin = triggerEin;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }
}
