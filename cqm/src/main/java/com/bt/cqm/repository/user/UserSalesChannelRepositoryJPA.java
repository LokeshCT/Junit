package com.bt.cqm.repository.user;

import com.bt.cqm.exception.UserConfigNotFoundException;
import com.bt.rsqe.persistence.PersistenceManager;

import java.util.List;

public class UserSalesChannelRepositoryJPA implements UserSalesChannelRepository {

    PersistenceManager persistenceManager;

    public UserSalesChannelRepositoryJPA(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public List<UserSalesChannelEntity> findUserConfigById(String userId) throws UserConfigNotFoundException {

        List<UserSalesChannelEntity> salesChannelConfigEntitiesList = persistenceManager.query(UserSalesChannelEntity.class,
                                                                                               "select distinct usc from UserSalesChannelEntity usc where usc.userId = ?0", userId);

        if (salesChannelConfigEntitiesList.size() == 0) {
            throw new UserConfigNotFoundException("User config not found for userId: " + userId);
        }
        return salesChannelConfigEntitiesList;
    }

    @Override
    public List<UserSalesChannelEntity> getAssociatedSalesChannel(String userId) {
        List<UserSalesChannelEntity> salesChannelList = persistenceManager.query(UserSalesChannelEntity.class,
                                                                                 "select distinct ue from UserSalesChannelEntity ue where ue.userId = ?0", userId);
        return salesChannelList;
    }

}
