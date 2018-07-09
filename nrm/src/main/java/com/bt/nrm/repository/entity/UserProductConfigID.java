package com.bt.nrm.repository.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserProductConfigID implements Serializable{

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "PRODUCT_CATEGORY_CODE")
    private String productCategoryCode;

    public UserProductConfigID() {
    }

    public UserProductConfigID(String userId, String productCategoryId) {
        this.userId = userId;
        this.productCategoryCode = productCategoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductCategoryCode() {
        return productCategoryCode;
    }

    public void setProductCategoryCode(String productCategoryCode) {
        this.productCategoryCode = productCategoryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProductConfigID that = (UserProductConfigID) o;

        if (!userId.equals(that.userId)) return false;
        return productCategoryCode.equals(that.productCategoryCode);

    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + productCategoryCode.hashCode();
        return result;
    }
}
