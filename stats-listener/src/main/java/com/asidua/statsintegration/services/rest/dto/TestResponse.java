package com.asidua.statsintegration.services.rest.dto;

import com.asidua.statsintegration.Constants;
import com.asidua.statsintegration.services.TestInvocationException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "testresponse")
public class TestResponse implements Serializable {


    private static final long serialVersionUID = -9077642943289682506L;

    public enum ResponseStatus {PASSED, FAILED, INPROGRESS, INTERRUPTED,UNKNOWN, UNDEFINED};

    private String testName;
    private String testId;
    private List<String> responseMessages;
    private ResponseStatus responseSummary;
    private Map<String, Object> params;


    public TestResponse(String testName,String testId,TestInvocationException e){
        this(testName,testId,"Test Invocation Exception error :"+e);
        responseSummary = ResponseStatus.FAILED;
    }

    public TestResponse(String testName,String testId,Exception e){
        this(testName,testId,"Server error :"+e);
        responseSummary = ResponseStatus.FAILED;
    }

    public TestResponse(String testName, String responseMessage) {
        this(testName, Arrays.asList(responseMessage));
    }

    public TestResponse(String testName, List<String> responseMessages) {
        this();
        this.testName = testName;
        this.responseMessages.addAll(responseMessages);
    }

    public TestResponse(String testId, String testName, String responseMessage) {
        this(testId, testName, Arrays.asList(responseMessage));
    }

    public TestResponse(String testId, String testName, List<String> responseMessages) {
        this();
        this.testName = testName;
        this.testId = testId;
        this.responseMessages.addAll(responseMessages);
    }


    @XmlElement(name="params", nillable = true)
    public String[] getParams() {

        if (null!=params && params.size()>0){
        List<String> responses = new ArrayList<String>(params.size());
        for (String key : params.keySet()) {
            responses.add(String.format("%s = %s",key,params.get(key)));
        }
        return responses.toArray(new String[responses.size()]);
        } else {
            return null;
        }

    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public TestResponse() {
        params = new HashMap<String, Object>();
        responseMessages = new ArrayList<String>();
        responseSummary = ResponseStatus.UNDEFINED;

    }


    @XmlElement
    public String getApiVersion(){
        return Constants.version;
    }
    @XmlElement(name="testName")

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    @XmlElement(name="testId")
    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    @XmlElement(name = "responseMessages")
    public String[] getResponseMessages() {
        return responseMessages.toArray(new String[responseMessages.size()]);
    }

    public void setSingleResponseMessage(String responseMessage) {
        setResponseMessages(Arrays.asList(responseMessage));
    }

    public void addResponseMessage(String responseMessage) {
        responseMessages.add(responseMessage);
    }

    public void setResponseMessages(List<String> responseMessages) {
        this.responseMessages = responseMessages;
    }

    @XmlElement(name="responseSummary")
    public ResponseStatus getResponseSummary() {
        return responseSummary;
    }

    public void setResponseSummary(ResponseStatus responseSummary) {
        this.responseSummary = responseSummary;
    }
}
