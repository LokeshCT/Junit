package com.bt.nrm.repository;

import com.bt.nrm.dto.UserProductDTO;
import com.bt.nrm.repository.entity.UserGroupEntity;
import com.bt.nrm.repository.entity.UserProductEntity;
import com.bt.nrm.repository.entity.UserProductConfigID;
import com.bt.rsqe.persistence.PersistenceManager;
import java.util.List;
import java.util.ArrayList;
import com.bt.nrm.util.GeneralUtil;


import static com.bt.rsqe.utils.AssertObject.*;

public class NRMUserManagementRepositoryJPA implements NRMUserManagementRepository {
    private final PersistenceManager persistenceManager;
    private final ProductTemplateRepository productTemplateRepository;

    public NRMUserManagementRepositoryJPA(ProductTemplateRepository productTemplateRepository, PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.productTemplateRepository = productTemplateRepository;
    }

    @Override
    public List<UserGroupEntity> getGroupsFromUserId(String userId){
        if(isNotNull(userId)){
            return persistenceManager.query(UserGroupEntity.class,"from UserGroupEntity u where u.id.userId=?0",userId);
        }
        return null;
    }

    @Override
    public List<UserProductEntity> getNRMUserProductDTOs(String userId){
        if(isNotNull(userId)){
            return persistenceManager.query(UserProductEntity.class,"from UserProductEntity u where u.id.userId=?0",userId);
        }
        return null;
    }

    @Override
    public void addGroupToUser(UserGroupEntity userGroupEntity){
        persistenceManager.save(userGroupEntity);
    }

    @Override
    public void deleteGroupFromUser(UserGroupEntity userGroupEntity){
        persistenceManager.entityManager().remove(persistenceManager.entityManager().contains(userGroupEntity)
                                                      ? userGroupEntity : persistenceManager.entityManager().merge(userGroupEntity));
    }

    @Override
    public boolean addProductsToUser(List<UserProductDTO> userProductList,String userId){
        try{
            //fetching all the user product rows for this userId
            List<UserProductEntity> existingProductsListForUserId = getNRMUserProductDTOs(userId);

            if(userProductList.size() == 0){
                for(UserProductEntity userProductEntity:existingProductsListForUserId){
                    persistenceManager.entityManager().remove(persistenceManager.entityManager().contains(userProductEntity)
                                                                  ? userProductEntity : persistenceManager.entityManager().merge(userProductEntity));
                }
                return true;
            }
            else{
            if(isNull(existingProductsListForUserId)){
                for (UserProductDTO userProductDTO:userProductList){
                    UserProductConfigID userProductConfigID = new UserProductConfigID();
                    userProductConfigID.setUserId(userProductDTO.getUserId());
                    userProductConfigID.setProductCategoryCode(userProductDTO.getProduct().getProductCategoryCode());
                    UserProductEntity userProductEntity = new UserProductEntity();
                    userProductEntity.setId(userProductConfigID);
                    userProductEntity.setCreatedDate(GeneralUtil.getCurrentTimeStamp());
                    userProductEntity.setCreatedUser(userProductDTO.getCreatedUser());
                    persistenceManager.save(userProductEntity);
                }
                return true;
            } else{
                //only removing the user product association as removed by user on UI
                for (UserProductEntity userProductEntity:userProductAssociationToBeRemoved(userProductList,existingProductsListForUserId)){
                    persistenceManager.entityManager().remove(persistenceManager.entityManager().contains(userProductEntity)
                                                                  ? userProductEntity : persistenceManager.entityManager().merge(userProductEntity));
                }

                //inserting only the new values for user product association from the UI
                for (UserProductEntity userProductEntity:userProductAssociationToBeInserted(userProductList,existingProductsListForUserId)){
                    persistenceManager.save(userProductEntity);
                }
                return true;
            }
            }
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("Exception while associating user to Product(s)"+ex.getMessage());
            return false;
        }

    }

    private List<UserProductEntity> userProductAssociationToBeRemoved(List<UserProductDTO> userProductList,List<UserProductEntity> existingProductsListForUserId){

        List<UserProductEntity> userProductsToBeDeletedList = new ArrayList<UserProductEntity>();

        for(UserProductEntity userProductEntity:existingProductsListForUserId){
            if(!foundInNewUserProducts(userProductEntity,userProductList)){
                userProductsToBeDeletedList.add(userProductEntity);
            }
            }
        return userProductsToBeDeletedList;
    }

    private boolean foundInNewUserProducts(UserProductEntity userProductEntity,List<UserProductDTO> userProductList){
        boolean foundUserEntity = false;
        for(UserProductDTO userProductDTO:userProductList){
            if(userProductDTO.getUserId().equals(userProductEntity.getId().getUserId()) && userProductDTO.getProduct().getProductCategoryCode().equals(userProductEntity.getId().getProductCategoryCode())){
                foundUserEntity = true;
                break;
            }else{
                foundUserEntity = false;
            }
        }
        return foundUserEntity;
    }

    private List<UserProductEntity> userProductAssociationToBeInserted(List<UserProductDTO> userProductList,List<UserProductEntity> existingProductsListForUserId){

        List<UserProductEntity> userProductsToBeInsertedList = new ArrayList<UserProductEntity>();

        for(UserProductDTO userProductDTO:userProductList){
            if(!foundInExistingUserProducts(userProductDTO, existingProductsListForUserId)){
                userProductsToBeInsertedList.add(convertUserProductDTOtoEntity(userProductDTO));
            }
        }
        return userProductsToBeInsertedList;
    }

    private boolean foundInExistingUserProducts(UserProductDTO userProductDTO,List<UserProductEntity> existingProductsListForUserId ){
        boolean foundInExistingUserProducts = false;
        for(UserProductEntity userProductEntity:existingProductsListForUserId){
            if(userProductDTO.getUserId().equals(userProductEntity.getId().getUserId()) && userProductDTO.getProduct().getProductCategoryCode().equals(userProductEntity.getId().getProductCategoryCode())){
                foundInExistingUserProducts = true;
                break;
            }else{
                foundInExistingUserProducts = false;
            }
        }
        return foundInExistingUserProducts;
    }

    private UserProductEntity convertUserProductDTOtoEntity(UserProductDTO userProductDTO){
        UserProductEntity userProductEntity = new UserProductEntity();
        UserProductConfigID id = new UserProductConfigID();
        id.setUserId(userProductDTO.getUserId());
        id.setProductCategoryCode(userProductDTO.getProduct().getProductCategoryCode());
        userProductEntity.setId(id);
        userProductEntity.setCreatedUser(userProductDTO.getCreatedUser());
        userProductEntity.setCreatedDate(GeneralUtil.getCurrentTimeStamp());

        return userProductEntity;
    }



}
