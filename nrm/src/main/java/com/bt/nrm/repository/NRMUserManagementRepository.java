package com.bt.nrm.repository;

import com.bt.nrm.dto.UserProductDTO;
import com.bt.nrm.repository.entity.UserGroupEntity;
import com.bt.nrm.repository.entity.UserProductEntity;

import java.util.List;

public interface NRMUserManagementRepository {
    List<UserGroupEntity> getGroupsFromUserId(String userId);
    void addGroupToUser(UserGroupEntity userGroupEntity);
    void deleteGroupFromUser(UserGroupEntity userGroupEntity);
    boolean addProductsToUser(List<UserProductDTO> userProductList,String userId);
    List<UserProductEntity> getNRMUserProductDTOs(String userId);
}
