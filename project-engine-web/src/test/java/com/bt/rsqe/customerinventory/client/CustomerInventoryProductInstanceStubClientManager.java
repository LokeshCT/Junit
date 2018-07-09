package com.bt.rsqe.customerinventory.client;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.AsIsAssets;
import com.bt.rsqe.customerinventory.SpecialPriceBooks;
import com.bt.rsqe.customerinventory.ToBeAssets;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManager;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryStubDriverManager;
import com.bt.rsqe.domain.project.ProductInstanceFactory;
import com.bt.rsqe.domain.project.StubCountryResolver;
import com.bt.rsqe.domain.project.ToProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.utils.countries.Countries;

public class CustomerInventoryProductInstanceStubClientManager implements CustomerInventoryClientManager {
    private CustomerInventoryDriverManager driverManager;
    private ProductInstanceFactory productInstanceFactory;
    private Pmr pmr;

    public CustomerInventoryProductInstanceStubClientManager(ToBeAssets tobeAssets,
                                                             AsIsAssets asisAssets,
                                                             SpecialPriceBooks priceBooks,
                                                             PmrClient pmr) {
        this.pmr = pmr;
        this.driverManager = new CustomerInventoryStubDriverManager(tobeAssets, asisAssets, priceBooks);
        try {
            this.productInstanceFactory = ProductInstanceFactory.getProductInstanceFactory(pmr, this, StubCountryResolver.resolveTo(Countries.byIsoStatic("GB")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProductInstanceClient getProductInstanceClient() {
        return new ProductInstanceClient(driverManager,
                                         new ToProductInstance(productInstanceFactory,
                                                               driverManager.getAssetKeyBasedAssetResourceClient()),
                                         productInstanceFactory,
                                         pmr);
    }

    @Override
    public ProductValidationClient getProductValidationClient() {
        return new ProductValidationClient(driverManager);
    }

    @Override
    public SpecialPriceBookClient getSpecialPriceBookClient() {
        return new SpecialPriceBookClient(driverManager);
    }
}
