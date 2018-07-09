package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.UserProductDTO;
import com.bt.pms.dto.ProductCategoryDTO;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;


@Entity
@Table(name="USER_PRODUCT")
public class UserProductEntity implements Serializable {

    @EmbeddedId
    private UserProductConfigID id;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    public UserProductEntity() {
    }

    public UserProductConfigID getId() {
        return id;
    }

    public void setId(UserProductConfigID id) {
        this.id = id;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public UserProductDTO toDTO(UserProductDTO userProductDTO){
        if(userProductDTO != null){
            userProductDTO.setUserId(this.getId().getUserId());
            ProductCategoryDTO productCategoryDTO = new ProductCategoryDTO();
            productCategoryDTO.setProductCategoryCode(this.getId().getProductCategoryCode());
            userProductDTO.setProduct(productCategoryDTO);
            userProductDTO.setCreatedDate(this.getCreatedDate());
            userProductDTO.setCreatedUser(this.getCreatedUser());

        }
        return userProductDTO;
    }

    public UserProductDTO toNewDTO(){
        return toDTO(new UserProductDTO());
    }

}
