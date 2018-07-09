package com.bt.nrm.repository.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class UserGroupConfigID implements Serializable{

    @Column(name = "PRODUCT_CATEGORY_CODE")
    private String productCategoryCode;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "EVALUATOR_GROUP_ID")
    private String evaluatorGroupId;

    public UserGroupConfigID() {
    }

    public UserGroupConfigID(String productCategoryCode, String userId, String evaluatorGroupId) {
        this.productCategoryCode = productCategoryCode;
        this.userId = userId;
        this.evaluatorGroupId = evaluatorGroupId;
    }

    public String getProductCategoryCode() {
        return productCategoryCode;
    }

    public void setProductCategoryCode(String productCategoryCode) {
        this.productCategoryCode = productCategoryCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEvaluatorGroupId() {
        return evaluatorGroupId;
    }

    public void setEvaluatorGroupId(String evaluatorGroupId) {
        this.evaluatorGroupId = evaluatorGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserGroupConfigID that = (UserGroupConfigID) o;

        if (!productCategoryCode.equals(that.productCategoryCode)) return false;
        if (!userId.equals(that.userId)) return false;
        return evaluatorGroupId.equals(that.evaluatorGroupId);

    }

    @Override
    public int hashCode() {
        int result = productCategoryCode.hashCode();
        result = 31 * result + userId.hashCode();
        result = 31 * result + evaluatorGroupId.hashCode();
        return result;
    }
}
