package com.bt.rsqe.customerinventory.service.rootAssetUpdater;

import com.bt.rsqe.bfgfacade.repository.BfgRepositoryJPA;
import com.bt.rsqe.bfgfacade.write.sp.IStoredProcedureFacade;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.service.handlers.AssetCandidateHandler;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class RootAssetOrchestratorTest {



    private ApplicationConfig applicationConfig;
    protected static ProductInstanceClient productInstanceClient;
    protected static CIFAssetJPARepository cifAssetJPARepository;
    protected static BfgRepositoryJPA bfgReadRepository;
    protected static PmrClient pmr;

    @MockitoAnnotations.Mock
    protected static IStoredProcedureFacade iStoredProcedureFacade;
    @Before
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
    public void testRootAssetUpdater() throws Exception {
        String[] segments = new String[]{"rsqe", "customer-inventory-service", "rootAssetService", "rootAssetUpdater","7789"};
        RestRequestBuilder restRequestBuilder = new RestRequestBuilder(applicationConfig);
        RestResource response = restRequestBuilder.build(segments);
        assertNotNull("Expected RootAssetOrchestrator instance to be registered", response);
    }


}
