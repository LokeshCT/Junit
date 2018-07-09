package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceAssetValidator;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.pc.client.ConfiguratorSpecialBidClient;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 28/08/14
 * Time: 16:11
 * To change this template use File | Settings | File Templates.
 */
public class PricingTaskFactory {

    private PriceHandlerService priceHandlerService;
    private ProductInstanceClient futureProductInstanceClient;
    private ConfiguratorSpecialBidClient configuratorSpecialBidClient;
    private QuoteMigrationDetailsProvider quoteMigrationDetailsProvider;
    private Pmr pmr;
    private ApplicationCapabilityProvider applicationCapabilityProvider;

    public PricingTaskFactory(PriceHandlerService priceHandlerService, ProductInstanceClient futureProductInstanceClient, ConfiguratorSpecialBidClient configuratorSpecialBidClient,
                              QuoteMigrationDetailsProvider quoteMigrationDetailsProvider, Pmr pmr, ApplicationCapabilityProvider applicationCapabilityProvider) {
        this.priceHandlerService = priceHandlerService;
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.configuratorSpecialBidClient = configuratorSpecialBidClient;
        this.quoteMigrationDetailsProvider = quoteMigrationDetailsProvider;
        this.pmr = pmr;
        this.applicationCapabilityProvider = applicationCapabilityProvider;
    }

    public ScopePricingTask getScopePricingTask(String lineItems, String customerId, String projectId, String quoteOptionId, boolean indirectUser,
                                                String userToken) {
        return new ScopePricingTask(lineItems, customerId, projectId, quoteOptionId, indirectUser, priceHandlerService, futureProductInstanceClient,
                                    new ProductInstanceAssetValidator(futureProductInstanceClient), configuratorSpecialBidClient, userToken, quoteMigrationDetailsProvider, pmr, applicationCapabilityProvider);
    }
}
