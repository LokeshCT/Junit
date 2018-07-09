package com.bt.usermanagement.repository.entitiy;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name="USER_ROLE")
public class UserRoleEntity {

    @EmbeddedId
    private UserRoleID id;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    public UserRoleEntity() {
    }

    public UserRoleEntity(UserRoleID id, Timestamp createdDate, String createdUser) {
        this.id = id;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
    }

    public UserRoleID getId() {
        return id;
    }

    public void setId(UserRoleID id) {
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
}
