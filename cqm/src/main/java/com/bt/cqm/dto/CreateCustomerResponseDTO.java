package com.bt.cqm.dto;


import com.bt.rsqe.customerinventory.dto.contract.ContractDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CreateCustomerResponseDTO {
    @XmlElement
    private Integer responseCode;
    @XmlElement
    private String message;
    @XmlElement
    private String customerName;
    @XmlElement
    private String customerId;
    @XmlElement
    private String customerReference;
    @XmlElement
    private ContractDTO contractDTO;

    @XmlElement
    private Object data;

    @XmlElement
    private Long totalDataCount;

    public CreateCustomerResponseDTO(){}

    public CreateCustomerResponseDTO(String customerId, String customerReference) {
        this.customerId = customerId;
        this.customerReference = customerReference;
    }
    ///CLOVER:OFF

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public ContractDTO getContractDTO() {
        return contractDTO;
    }

    public void setContractDTO(ContractDTO contractDTO) {
        this.contractDTO = contractDTO;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(Long totalDataCount) {
        this.totalDataCount = totalDataCount;
    }
///CLOVER:ON

}
