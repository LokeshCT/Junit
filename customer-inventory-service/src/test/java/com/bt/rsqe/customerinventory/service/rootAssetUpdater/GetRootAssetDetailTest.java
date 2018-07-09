package com.bt.rsqe.customerinventory.service.rootAssetUpdater;

import com.bt.rsqe.bfgfacade.repository.BfgRepositoryJPA;
import com.bt.rsqe.bfgfacade.write.sp.IStoredProcedureFacade;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;

import com.bt.rsqe.pmr.client.PmrClient;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 605673548
 * Date: 14/07/15
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class GetRootAssetDetailTest {
    private ApplicationConfig applicationConfig;
    protected static ProductInstanceClient productInstanceClient;
    protected static CIFAssetJPARepository cifAssetJPARepository;
    protected static BfgRepositoryJPA bfgReadRepository;
    protected static PmrClient pmr;

    @MockitoAnnotations.Mock
    protected static IStoredProcedureFacade iStoredProcedureFacade;
    public void setUp() throws Exception {
        applicationConfig = StubApplicationConfig.defaultTestConfig();
        productInstanceClient= mock(ProductInstanceClient.class);
        cifAssetJPARepository = mock(CIFAssetJPARepository.class);
        bfgReadRepository = mock(BfgRepositoryJPA.class);
        pmr = mock(PmrClient.class);
        Application application = new Application(applicationConfig) {
            @Override
            protected ResourceHandlerFactory createResourceHandlerFactory() {
                return new RestResourceHandlerFactory() {
                    {
                        withSingleton(new RootAssetOrchestrator(productInstanceClient,cifAssetJPARepository,bfgReadRepository,iStoredProcedureFacade,pmr));
                    }
                };
            }
        };
        application.start();

    }
    @Test
    public void testUpdateCustomerOnBoardDetails() throws Exception {
        RootAssetDto rootAssetDto = new RootAssetDto();
        String packageInstanceId;
        rootAssetDto.setRelElementId(1234L);
        rootAssetDto.setRelElementType("Package");
        rootAssetDto.setElementRelationshipType("Package");


        packageInstanceId = "someProductInstanceId";
        rootAssetDto.setRootElementIdentifier(packageInstanceId);
        rootAssetDto.setRootElementId(0000L);
        rootAssetDto.setRootElementType("Package");

        rootAssetDto.setElementId(111L) ;
        rootAssetDto.setElementType("PI");
        rootAssetDto.setElementSourceSystem(RootAssetConstants.SourceSystem) ;
        rootAssetDto.setElementInsPref("S0308454") ;
        rootAssetDto.setElementCeasedDate(null);
        CifCustomerOnBoardSPParameter cifCustomerOnBoardSPParameter = new CifCustomerOnBoardSPParameter(rootAssetDto);

        assertNotNull("The parameters for onBoard are null", cifCustomerOnBoardSPParameter);
    }

}
