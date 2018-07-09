package com.bt.nrm.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by 608143048 on 10/12/2015.
 * This DTO will be used as an input parameter to the interfaces(SQE/rSQE) method to create non-standard request.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NonStandardRequestDTO {

    private Long requestId;
    private String requestName;
    private String customerRequestedDate;
    private String templateCode;
    private String templateVersion;
    private String renewalFlag;
    private String volumeForFeature;
    private String bidManagerName;
    /** Quote **/
    private QuoteDTO quote;
    /** Product **/
    private ProductDTO product;
    /** User **/
    private UserDTO user;
    /** Common Info **/
    private List<NonStandardRequestAttributeDTO> commonDetails;
    /** Site List **/
    private List<NonStandardRequestSiteDTO> sites;

    public NonStandardRequestDTO() {
    }

    public NonStandardRequestDTO(Long requestId, String requestName, String customerRequestedDate, String templateCode, String templateVersion, String renewalFlag, String volumeForFeature, String bidManagerName, QuoteDTO quote, ProductDTO product, UserDTO user, List<NonStandardRequestAttributeDTO> commonDetails, List<NonStandardRequestSiteDTO> sites) {
        this.requestId = requestId;
        this.requestName = requestName;
        this.customerRequestedDate = customerRequestedDate;
        this.templateCode = templateCode;
        this.templateVersion = templateVersion;
        this.renewalFlag = renewalFlag;
        this.volumeForFeature = volumeForFeature;
        this.bidManagerName = bidManagerName;
        this.quote = quote;
        this.product = product;
        this.user = user;
        this.commonDetails = commonDetails;
        this.sites = sites;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getCustomerRequestedDate() {
        return customerRequestedDate;
    }

    public void setCustomerRequestedDate(String customerRequestedDate) {
        this.customerRequestedDate = customerRequestedDate;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public String getRenewalFlag() {
        return renewalFlag;
    }

    public void setRenewalFlag(String renewalFlag) {
        this.renewalFlag = renewalFlag;
    }

    public String getVolumeForFeature() {
        return volumeForFeature;
    }

    public void setVolumeForFeature(String volumeForFeature) {
        this.volumeForFeature = volumeForFeature;
    }

    public String getBidManagerName() {
        return bidManagerName;
    }

    public void setBidManagerName(String bidManagerName) {
        this.bidManagerName = bidManagerName;
    }

    public QuoteDTO getQuote() {
        return quote;
    }

    public void setQuote(QuoteDTO quote) {
        this.quote = quote;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public List<NonStandardRequestAttributeDTO> getCommonDetails() {
        return commonDetails;
    }

    public void setCommonDetails(List<NonStandardRequestAttributeDTO> commonDetails) {
        this.commonDetails = commonDetails;
    }

    public List<NonStandardRequestSiteDTO> getSites() {
        return sites;
    }

    public void setSites(List<NonStandardRequestSiteDTO> sites) {
        this.sites = sites;
    }
}
