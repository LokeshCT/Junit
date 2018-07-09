package com.bt.cqm.repository.user;



import com.bt.cqm.model.UserRoleDTO;
import com.bt.rsqe.utils.Lists;
import com.google.common.base.Function;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.sql.Date;
import java.util.List;

import static com.bt.cqm.model.UserRoleDTO.CEASE_OPTIMIZATION_TEAM;
import static com.google.common.collect.Lists.*;

@Entity
@Table(name = "USER_AUTHORIZATION")
public class UserEntity {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EMAIL_ID")
    private String emailId;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    @OneToOne
    @JoinColumn(name = "ROLE_TYPE_ID")
    private RoleTypeEntity userType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id.user", cascade = CascadeType.ALL)
    @OrderBy("defaultRole desc")
    private List<UserRoleConfigEntity> userRoleConfig;


    public UserEntity() {
    }

    public UserEntity(String userId, String userName,String active, Date createdDate, String createdUser, Date modifiedDate, String modifiedUser) {
        this.userId = userId;
        this.userName = userName;
        this.active = active;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserEntity that = (UserEntity) o;

        if (active != null ? !active.equals(that.active) : that.active != null) {
            return false;
        }
        if (createdDate != null ? !createdDate.equals(that.createdDate) : that.createdDate != null) {
            return false;
        }
        if (createdUser != null ? !createdUser.equals(that.createdUser) : that.createdUser != null) {
            return false;
        }
        if (modifiedDate != null ? !modifiedDate.equals(that.modifiedDate) : that.modifiedDate != null) {
            return false;
        }
        if (modifiedUser != null ? !modifiedUser.equals(that.modifiedUser) : that.modifiedUser != null) {
            return false;
        }
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (createdUser != null ? createdUser.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        result = 31 * result + (modifiedUser != null ? modifiedUser.hashCode() : 0);
        result = 31 * result + (userType != null ? userType.hashCode() : 0);
        return result;
    }

    ///CLOVER:OFF

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getActive() {
        return active;
    }

    public boolean isActive() {
        return "Y".equalsIgnoreCase(active);
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public RoleTypeEntity getUserType() {
        return userType;
    }

    public void setUserType(RoleTypeEntity userType) {
        this.userType = userType;
    }

    public List<UserRoleConfigEntity> getUserRoleConfig() {
        return userRoleConfig;
    }

    public void setUserRoleConfig(List<UserRoleConfigEntity> userRoleConfig) {
        this.userRoleConfig = userRoleConfig;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public boolean hasCeaseRole() {
        if (!Lists.isNullOrEmpty(this.getUserRoleConfig())) {

            List<UserRoleDTO> userRoles = newArrayList(transform(this.getUserRoleConfig(), new Function<UserRoleConfigEntity, UserRoleDTO>() {
                @Override
                public UserRoleDTO apply(UserRoleConfigEntity input) {
                    return new UserRoleDTO(input.getId().getRole().getRoleId(), input.getId().getRole().getRoleName());
                }
            }));

            for (UserRoleDTO aRole : userRoles) {
                if (aRole.getRoleName().equalsIgnoreCase(CEASE_OPTIMIZATION_TEAM)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getRole() {
        return Lists.isNullOrEmpty(this.getUserRoleConfig()) ? null : getUserRoleConfig().get(0).getRole().getRoleName();
    }

///CLOVER:ON
}
