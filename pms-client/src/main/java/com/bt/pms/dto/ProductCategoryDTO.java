package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProductCategoryDTO {
    @XmlAttribute
    private String productCategoryId;
    @XmlAttribute
    private String productCategoryCode;
    @XmlAttribute
    private String productCategoryName;
    @XmlAttribute
    private String productCategoryDescription;
    @XmlAttribute
    private List<TemplateDTO> templates;
    @XmlAttribute
    private String version;
    @XmlAttribute
    private String startDate;
    @XmlAttribute
    private String endDate;
    @XmlAttribute
    private String exclude;
    @XmlAttribute
    private String status;


    public ProductCategoryDTO() {
    }

    public ProductCategoryDTO(String productCategoryId, String productCategoryCode, String productCategoryName, String productCategoryDescription, List<TemplateDTO> templates, String version, String startDate, String endDate, String exclude, String status) {
        this.productCategoryId = productCategoryId;
        this.productCategoryCode = productCategoryCode;
        this.productCategoryName = productCategoryName;
        this.productCategoryDescription = productCategoryDescription;
        this.templates = templates;
        this.version = version;
        this.startDate = startDate;
        this.endDate = endDate;
        this.exclude = exclude;
        this.status = status;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getProductCategoryCode() {
        return productCategoryCode;
    }

    public void setProductCategoryCode(String productCategoryCode) {
        this.productCategoryCode = productCategoryCode;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public void setProductCategoryName(String productCategoryName) {
        this.productCategoryName = productCategoryName;
    }

    public String getProductCategoryDescription() {
        return productCategoryDescription;
    }

    public void setProductCategoryDescription(String productCategoryDescription) {
        this.productCategoryDescription = productCategoryDescription;
    }

    public List<TemplateDTO> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateDTO> templates) {
        this.templates = templates;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean equals(Object o){
        if(o instanceof ProductCategoryDTO && this.getProductCategoryCode().equals(((ProductCategoryDTO) o).getProductCategoryCode())){
            return true;
        } else{
            return false;
        }
    }
}
