package com.bt.nrm.dto;

import com.bt.pms.dto.ProductCategoryDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserProductDTO {

    private String userId;
    private String createdUser;
    private ProductCategoryDTO product;
    private Date createdDate;

    public UserProductDTO(){
    };

    public UserProductDTO(String userId, String createdUser, ProductCategoryDTO product,Date createdDate) {
        this.userId = userId;
        this.createdUser = createdUser;
        this.product = product;
        this.createdDate = createdDate;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public ProductCategoryDTO getProduct() {
        return product;
    }

    public void setProduct(ProductCategoryDTO product) {
        this.product = product;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

}
