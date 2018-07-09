package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.customerrecord.UserRole;
import com.bt.rsqe.customerrecord.UsersDTO;
import com.bt.rsqe.security.UserContextManager;

public class UserFacade {

    private final UserResource userResource;

    public UserFacade(UserResource userResource) {
        this.userResource = userResource;
    }

    public UsersDTO findUsers(String customerId, UserRole userRole) {
        UserContext userContext = UserContextManager.getCurrent();
        return userResource.find(customerId,
                                 userRole.value(),
                                 userContext.getLoginName(),
                                 userContext.getRsqeToken());
    }

    public UserDTO findUser(String loginName) {
        return userResource.findUser(loginName);
    }
}
