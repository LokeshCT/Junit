package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 28/08/14
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class PricingTaskFactoryTest {

    PricingTaskFactory pricingTaskFactory;
    private String lineItems = "lineItems";
    private String customerId = "customerId";
    private String projectId = "projectId";
    private String quoteOptionId = "quoteOptionId";
    private boolean indirectUser = false;
    private PriceHandlerService priceHandlerService;
    private ProductInstanceClient futureProductInstanceClient;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;

    @Before
    public void before() {
        priceHandlerService = mock(PriceHandlerService.class);
        futureProductInstanceClient = mock(ProductInstanceClient.class);
        migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
        pricingTaskFactory = new PricingTaskFactory(priceHandlerService, futureProductInstanceClient, null, migrationDetailsProvider, PmrMocker.getMockedInstance(true), mock(ApplicationCapabilityProvider.class));
    }



    @Test
    public void shouldReturnScopePricingTask() throws Exception {
        Runnable task = pricingTaskFactory.getScopePricingTask(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken");
        assertThat(task, is(instanceOf(ScopePricingTask.class)));
    }
}
