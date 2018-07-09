package com.bt.cqm.repository.user;

import com.bt.cqm.dto.UserCreateDTO;
import com.bt.cqm.dto.user.UserDTO;
import com.bt.cqm.dto.user.UserRoleMasterDTO;
import com.bt.cqm.exception.SalesChannelNotFoundException;
import com.bt.cqm.exception.UserConfigNotFoundException;

import java.util.List;

/**
 * The Interface UserManagementRepository.
 */
public interface UserManagementRepository {

    /**
     * Find user by user id.
     *
     * @param userId the user id
     * @return the user entity
     * @throws com.bt.cqm.exception.SalesChannelNotFoundException
     *          the sales channel not found exception
     */
    UserEntity findUserByUserId(String userId);

    /**
     * Gets the user role master list.
     *
     * @return the user role master list
     * @throws UserConfigNotFoundException the user config not found exception
     */
    List<UserRoleMasterEntity> getUserRoleMasterList() throws UserConfigNotFoundException;

    /**
     * Gets the sales channels associated with user.
     *
     * @param userId the user id
     * @return the sales channels associated with user
     * @throws com.bt.cqm.exception.SalesChannelNotFoundException
     *          the sales channel not found exception
     */
    List<UserSalesChannelEntity> getSalesChannelsAssociatedWithUser(String userId) throws SalesChannelNotFoundException;

    /**
     * Gets the available sales channels.
     *
     * @return the available sales channels
     * @throws com.bt.cqm.exception.SalesChannelNotFoundException the sales channel not found exception
     */
    /*List<SalesChannelEntity> getAvailableSalesChannels() throws SalesChannelNotFoundException;
    */
    /* void updateUserInfo(UserEntity userEntity) throws UserConfigNotFoundException; */

    /**
     * Update user info.
     *
     * @param userDto           the user dto
     * @param userRoleMasterDto the user role master dto
     * @throws UserConfigNotFoundException the user config not found exception
     */
    void updateUserInfo(UserDTO userDto, UserRoleMasterDTO userRoleMasterDto) throws UserConfigNotFoundException;

    /**
     * Update user sales channels.
     *
     * @param userId               the user id
     * @param createdBy            the created by
     * @param userSalesChannelList the user sales channel list
     * @throws com.bt.cqm.exception.SalesChannelNotFoundException
     *          the sales channel not found exception
     */
    void updateUserSalesChannels(String userId, String createdBy, List<String> userSalesChannelList) throws SalesChannelNotFoundException;

    /**
     * Find user config by id.
     *
     * @param userId the user id
     * @return the list
     * @throws UserConfigNotFoundException the user config not found exception
     */
    List<UserSalesChannelEntity> findUserConfigById(String userId) throws UserConfigNotFoundException;

    /**
     * Gets the associated sales channel.
     *
     * @param userId the user id
     * @return the associated sales channel
     */
    List<UserSalesChannelEntity> getAssociatedSalesChannel(String userId);

    List<UserRoleConfigEntity> getUserRoleConfig(String userId);

    List<UserRoleMasterEntity> getRoles(Integer roleTypeId);

    List<SalesChannelEntity> getSalesChannels(Integer roleTypeId);

    void updateUserInfo(UserDTO userDto) throws UserConfigNotFoundException;

    String getSalesChannelGfrCode(String salesChannelName);

    String getSalesChannelFromGFRCode(String gfrCode);

    public List<RagConfigurationEntity> getColorCodeDetails();

    void createUser(UserCreateDTO user) throws Exception;

    void createUserRoleConfig(String userId, List<Long> roleIds) throws Exception;

    int insertSalesChannels(String userId, String createdBy, List<String> userSalesChannelList);

    CountryVatMapEntity getCountryVatPrefix(String countryName);

}
