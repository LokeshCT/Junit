package com.bt.nrm.dto;

import com.bt.pms.dto.EvaluatorGroupDTO;
import com.bt.pms.dto.ProductCategoryDTO;

import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserGroupDTO {

    private String userId;
    private ProductCategoryDTO product;
    private EvaluatorGroupDTO group;
    private String createdUser;
    private Date createdDate;

    public UserGroupDTO() {
    }

    public UserGroupDTO(String userId, ProductCategoryDTO product, EvaluatorGroupDTO group, String createdUser, Date createdDate) {
        this.userId = userId;
        this.product = product;
        this.group = group;
        this.createdUser = createdUser;
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProductCategoryDTO getProduct() {
        return product;
    }

    public void setProduct(ProductCategoryDTO product) {
        this.product = product;
    }

    public EvaluatorGroupDTO getGroup() {
        return group;
    }

    public void setGroup(EvaluatorGroupDTO group) {
        this.group = group;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
