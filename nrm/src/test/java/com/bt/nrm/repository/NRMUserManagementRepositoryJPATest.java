package com.bt.nrm.repository;

import com.bt.nrm.repository.entity.UserProductConfigID;
import com.bt.nrm.repository.entity.UserProductEntity;
import com.bt.rsqe.persistence.PersistenceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: 607866849
 * Date: 16/10/15
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public class NRMUserManagementRepositoryJPATest {

    @Mock
    private ProductTemplateRepository productTemplateRepository;
    @Mock
    private PersistenceManager persistenceManager;

    NRMUserManagementRepositoryJPA repositoryJPA;


    @Before
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
        repositoryJPA = new NRMUserManagementRepositoryJPA(productTemplateRepository,persistenceManager);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldAddProductToUser() throws Exception{
        //ToDo fix below code as per PMS changes
        /*String userId = "userId";
        String productId = "productId";

        UserProductConfigID userProductConfigID = new UserProductConfigID();
        userProductConfigID.setUserId(userId);
        userProductConfigID.setProductId(productId);
        UserProductEntity userProductEntity = new UserProductEntity();
        userProductEntity.setId(userProductConfigID);

        persistenceManager.save(userProductEntity);

        List<ProductMasterEntity> productMasterEntityList = new ArrayList<ProductMasterEntity>(1);
        ProductMasterEntity productMasterEntity = new ProductMasterEntity(productId,"productName","Description",null,null,"creator",null,"modifier");
        productMasterEntityList.add(productMasterEntity);
        when(productTemplateRepository.getProductsByUserId(userId)).thenReturn(productMasterEntityList);
        assertEquals(productMasterEntityList.get(0).getProductId(), productId);
        assertEquals(productMasterEntityList.size(),1);*/

    }
}
