package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.UserGroupDTO;
import com.bt.pms.dto.EvaluatorGroupDTO;
import com.bt.pms.dto.ProductCategoryDTO;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

import static com.bt.rsqe.utils.AssertObject.*;


@Entity
@Table(name="USER_GROUP")
public class UserGroupEntity implements Serializable {

    @EmbeddedId
    private UserGroupConfigID id;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    public UserGroupEntity() {
    }

    public UserGroupEntity(UserGroupConfigID id, Timestamp createdDate, String createdUser) {
        this.id = id;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
    }

    public UserGroupConfigID getId() {
        return id;
    }

    public void setId(UserGroupConfigID id) {
        this.id = id;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public UserGroupDTO toDTO(UserGroupDTO dto){
        if(isNotNull(dto) && isNotNull(this.getId())){
            dto.setUserId(this.getId().getUserId());
            if(isNull(dto.getGroup())){
                dto.setGroup(new EvaluatorGroupDTO());
                dto.getGroup().setEvaluatorGroupId(this.getId().getEvaluatorGroupId());
            }else{
                dto.getGroup().setEvaluatorGroupId(this.getId().getEvaluatorGroupId());
            }
            if(isNull(dto.getProduct())){
                dto.setProduct(new ProductCategoryDTO());
                dto.getProduct().setProductCategoryCode(this.getId().getProductCategoryCode());
            }else{
                dto.getProduct().setProductCategoryCode(this.getId().getProductCategoryCode());
            }
            dto.setCreatedUser(this.getCreatedUser());
            dto.setCreatedDate(this.getCreatedDate());
        }
        return dto;
    }

    public UserGroupDTO toNewDTO(){
        return toDTO(new UserGroupDTO());
    }


}
