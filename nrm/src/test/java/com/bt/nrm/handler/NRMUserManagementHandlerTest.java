package com.bt.nrm.handler;

import com.bt.nrm.dto.UserProductDTO;
import com.bt.nrm.repository.NRMUserManagementRepository;
import com.bt.nrm.repository.ProductTemplateRepository;
import com.bt.pms.dto.ProductCategoryDTO;
import com.bt.pms.resources.PMSResource;
import com.bt.rsqe.utils.RSQEMockery;
import com.bt.usermanagement.resources.UserResource;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

public class NRMUserManagementHandlerTest {

    private NRMUserManagementHandler nrmUserManagementHandler;
    private NRMUserManagementRepository nrmUserManagementRepository;
    private UserResource userResource;
    private ProductTemplateRepository productTemplateRepository;
    private PMSResource pmsResource;
    private String status ="1123";

    private final Mockery context = new RSQEMockery();

    @Before
    public void setUp(){
        try {
            userResource = context.mock(UserResource.class);
            nrmUserManagementRepository = context.mock(NRMUserManagementRepository.class);
            productTemplateRepository = context.mock(ProductTemplateRepository.class);
            nrmUserManagementHandler = new NRMUserManagementHandler(userResource,nrmUserManagementRepository,productTemplateRepository,pmsResource)  ;
        } catch (Throwable ex) {
            System.out.println("Fail to create setup for NRMUserManagementHandlerTest." + ex);
        }
    }


    @Test
    public void shouldAddProductsToUser() throws Exception{
        //ToDo fix below code as per PMS changes
        /*List<ProductCategoryDTO> products = new ArrayList<ProductCategoryDTO>();
        products.add(new ProductCategoryDTO("ProductId","ProductName","Prod Description",null,"User",null,"Mod User",null)) ;
        final UserProductDTO userProductDTO = new UserProductDTO("UserId","MasterEin",products);

        context.checking(new Expectations() {{
            allowing(nrmUserManagementRepository).addProductsToUser(userProductDTO);
            will(returnValue(true));
        }});

        Response res = nrmUserManagementHandler.addProductsToUser(userProductDTO);
        assertEquals(res.getStatus(),200);*/
    }

    @Test
    public void shouldAddProductsToUserWithError() throws Exception{
        //ToDo fix below code as per PMS changes
        /*List<ProductCategoryDTO> products = new ArrayList<ProductCategoryDTO>();
        products.add(new ProductCategoryDTO("ProductId","ProductName","Prod Description",null,"User",null,"Mod User",null)) ;
        final UserProductDTO userProductDTO = new UserProductDTO("UserId","MasterEin",products);

        context.checking(new Expectations() {{
            allowing(nrmUserManagementRepository).addProductsToUser(userProductDTO);
            will(returnValue(false));
        }});

        Response res = nrmUserManagementHandler.addProductsToUser(userProductDTO);
        assertEquals(res.getStatus(),404);*/
    }

    @Test
    public void shouldAddProductsToUserWhenInputIsNull() throws Exception{

        /*final UserProductDTO userProductDTO = null;

        Response res = nrmUserManagementHandler.addProductsToUser(userProductDTO);
        assertEquals(res.getStatus(),400);*/
    }

    @Test
    public void shouldAddProductsToUserThrowsException() throws Exception{
        //ToDo fix below code as per PMS changes
        /*List<ProductCategoryDTO> products = new ArrayList<ProductCategoryDTO>();
        products.add(new ProductCategoryDTO("ProductId","ProductName","Prod Description",null,"User",null,"Mod User",null)) ;
        final UserProductDTO userProductDTO = new UserProductDTO("UserId","MasterEin",products);

        context.checking(new Expectations() {{
            allowing(nrmUserManagementRepository).addProductsToUser(userProductDTO);
            will(returnValue(status));
        }});

        Response res = nrmUserManagementHandler.addProductsToUser(userProductDTO);
        assertEquals(res.getStatus(),417);*/
    }

}