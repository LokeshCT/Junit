package com.bt.rsqe.customerinventory.service;

import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.service.handlers.AssetCandidateHandler;
import com.bt.rsqe.customerinventory.service.handlers.CIFAssetHandler;
import com.bt.rsqe.customerinventory.service.handlers.ValidationHandler;
import com.bt.rsqe.utils.Environment;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;

public class CustomerInventoryServiceApplicationTest {
    public static interface CustomerInventoryServiceTestConfig {
        CustomerInventoryServiceConfig getCustomerInventoryServiceConfig();
    }

    @Test
    public void shouldContainCorrectResources() throws Exception {
        CustomerInventoryServiceConfig config = ConfigurationProvider.provide(CustomerInventoryServiceTestConfig.class, Environment.env()).getCustomerInventoryServiceConfig();
        CustomerInventoryServiceApplication application = new CustomerInventoryServiceApplication(config);

        ApplicationConfig applicationConfig = config.getApplicationConfig();
        String baseUri = applicationConfig.getScheme() + "://" +
                         applicationConfig.getHost() + ":" +
                         applicationConfig.getPort();

        assertThat(application.getBaseUri(), is(baseUri));
        assertNotNull("Expected AssetCandidateHandler instance to be registered", application.getResource(AssetCandidateHandler.class));
        assertNotNull("Expected AssetHandler instance to be registered", application.getResource(CIFAssetHandler.class));
        assertNotNull("Expected ValidationHandler instance to be registered", application.getResource(ValidationHandler.class));
    }
}
