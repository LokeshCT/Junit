package com.bt.nrm.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 608143048 on 10/12/2015.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductDTO {

    private String productCategoryCode;
    private String productCategoryName;
    private String productOfferingCode;
    private String productOfferingName;

    public ProductDTO() {
    }

    public ProductDTO(String productCategoryCode, String productCategoryName, String productOfferingCode, String productOfferingName) {
        this.productCategoryCode = productCategoryCode;
        this.productCategoryName = productCategoryName;
        this.productOfferingCode = productOfferingCode;
        this.productOfferingName = productOfferingName;
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

    public String getProductOfferingCode() {
        return productOfferingCode;
    }

    public void setProductOfferingCode(String productOfferingCode) {
        this.productOfferingCode = productOfferingCode;
    }

    public String getProductOfferingName() {
        return productOfferingName;
    }

    public void setProductOfferingName(String productOfferingName) {
        this.productOfferingName = productOfferingName;
    }
}
