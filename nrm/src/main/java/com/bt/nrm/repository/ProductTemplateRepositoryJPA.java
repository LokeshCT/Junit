package com.bt.nrm.repository;

import com.bt.nrm.repository.entity.UserProductEntity;
import com.bt.pms.dto.ProductCategoryDTO;
import com.bt.rsqe.persistence.PersistenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.utils.AssertObject.*;

public class ProductTemplateRepositoryJPA implements ProductTemplateRepository {

    private final PersistenceManager persistenceManager;

    public ProductTemplateRepositoryJPA(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public Set<String> getProductIdsByUserId(String userId){
        if(isNotNull(userId)){
            List<UserProductEntity> userProductConfigs = persistenceManager.query(UserProductEntity.class,"from UserProductEntity u where u.id.userId=?0",userId);
            Set<String> productIds = new HashSet<String>();
            for(UserProductEntity userProductConfigEntity : userProductConfigs){
                productIds.add(userProductConfigEntity.getId().getProductCategoryCode());
            }
            return productIds;
        }
        return null;
    }

    @Override
    public List<ProductCategoryDTO> getProductsByUserId(String userId){
        if(isNotNull(userId)){
            List<ProductCategoryDTO> productMasterList = new ArrayList<ProductCategoryDTO>();
            Set<String> productIds = getProductIdsByUserId(userId);
            if(productIds.size() > 0) {
                //TODO Call PMS service to get product data
                //productMasterList = getProductsByProductIds(productIds);
            }
            return productMasterList;
        }
        return null;
    }
}
