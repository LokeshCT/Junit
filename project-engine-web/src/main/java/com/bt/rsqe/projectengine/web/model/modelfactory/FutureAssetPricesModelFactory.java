package com.bt.rsqe.projectengine.web.model.modelfactory;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;

public interface FutureAssetPricesModelFactory {
    FutureAssetPricesModel create(String customerId, String projectId, String quoteOptionId, AssetDTO assetDTO, PriceSuppressStrategy priceSuppressStrategy);
    FutureAssetPricesModel create(String customerId, String projectId, String quoteOptionId, FutureAssetPricesDTO futureAssetPricesDTO);
}
