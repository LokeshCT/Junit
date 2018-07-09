package com.bt.cqm.repository.user;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 7/24/14
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
@Embeddable
public class UserRoleConfigID implements Serializable {

    @ManyToOne
    private UserEntity user;

    @ManyToOne
    private UserRoleMasterEntity role;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public UserRoleMasterEntity getRole() {
        return role;
    }

    public void setRole(UserRoleMasterEntity role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserRoleConfigID)) {
            return false;
        }

        UserRoleConfigID that = (UserRoleConfigID) o;

        if (role != null ? !role.equals(that.role) : that.role != null) {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (role != null ? role.hashCode() : 0);
        return result;
    }
}
