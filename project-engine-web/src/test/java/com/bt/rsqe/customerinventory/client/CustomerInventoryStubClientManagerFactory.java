package com.bt.rsqe.customerinventory.client;

import com.bt.rsqe.customerinventory.AsIsAssets;
import com.bt.rsqe.customerinventory.SpecialPriceBooks;
import com.bt.rsqe.customerinventory.ToBeAssets;
import com.bt.rsqe.pmr.client.PmrClient;

public abstract class CustomerInventoryStubClientManagerFactory {
    public static final ToBeAssets TOBE_ASSET_HOLDER = new ToBeAssets();
    public static final AsIsAssets ASIS_ASSET_HOLDER = new AsIsAssets();
    public static final SpecialPriceBooks SPECIAL_PRICE_BOOKS = new SpecialPriceBooks();

    public static CustomerInventoryClientManager getClientManager(PmrClient pmr){
        return new CustomerInventoryProductInstanceStubClientManager(TOBE_ASSET_HOLDER, ASIS_ASSET_HOLDER, SPECIAL_PRICE_BOOKS, pmr);
    }
}
