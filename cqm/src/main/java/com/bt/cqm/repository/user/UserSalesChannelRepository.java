package com.bt.cqm.repository.user;

import com.bt.cqm.exception.UserConfigNotFoundException;

import java.util.List;

public interface UserSalesChannelRepository {

    List<UserSalesChannelEntity> findUserConfigById(String userId) throws UserConfigNotFoundException;

    List<UserSalesChannelEntity> getAssociatedSalesChannel(String userId);
}
