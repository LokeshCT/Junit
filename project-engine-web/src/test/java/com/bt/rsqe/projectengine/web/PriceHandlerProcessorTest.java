package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceAssetValidator;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 28/08/14
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */
public class PriceHandlerProcessorTest {

    private PriceHandlerProcessor priceHandlerProcessor;
    private ExecutorService executorService;
    private PricingTaskFactory pricingTaskFactory;
    private String lineItems = "lineItems";
    private String customerId = "customerId";
    private String projectId = "projectId";
    private String quoteOptionId = "quoteOptionItemId";
    private boolean indirectUser = false;
    private PriceHandlerService priceHandlerService;
    private ProductInstanceClient futureProductInstanceClient;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private PmrClient pmrClient;

    @Before
    public void before() {
        priceHandlerService = mock(PriceHandlerService.class);
        futureProductInstanceClient = mock(ProductInstanceClient.class);
        executorService = mock(ExecutorService.class);
        pricingTaskFactory = mock(PricingTaskFactory.class);
        priceHandlerProcessor = new PriceHandlerProcessor(executorService, pricingTaskFactory);
        migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
        pmrClient = PmrMocker.getMockedInstance(true);
    }

    @Test
    public void shouldSubmitPricingTask() throws Exception {
        ScopePricingTask scopePricingTask = new ScopePricingTask(lineItems, customerId, projectId, quoteOptionId, indirectUser,
                                                                 priceHandlerService, futureProductInstanceClient, new ProductInstanceAssetValidator(futureProductInstanceClient),
                                                                 null,"userToken", migrationDetailsProvider, pmrClient, mock(ApplicationCapabilityProvider.class));
        //When
        when(pricingTaskFactory.getScopePricingTask(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(scopePricingTask);
        //Then
        priceHandlerProcessor.startPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken");
        verify(executorService, times(1)).submit(scopePricingTask);
    }
}
