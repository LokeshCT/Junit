package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerinventory.client.resource.tobe.OptionBasedFutureAssetResourceClient;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManager;
import com.bt.rsqe.customerinventory.driver.FutureAssetPriceDriver;
import com.bt.rsqe.customerinventory.driver.OptionBasedFutureAssetDriver;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPriceReportDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.FutureAssetPricesModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class FutureAssetPricesFacade {

    private CustomerInventoryDriverManager customerInventoryDriverManager;
    private FutureAssetPricesModelFactory futureAssetPricesModelFactory;

    public FutureAssetPricesFacade(CustomerInventoryDriverManager customerInventoryDriverManager,
                                   FutureAssetPricesModelFactory futureAssetPricesModelFactory) {
        this.customerInventoryDriverManager = customerInventoryDriverManager;
        this.futureAssetPricesModelFactory = futureAssetPricesModelFactory;
    }

    public List<FutureAssetPricesModel> getForLineItems(String customerId, String projectId, String quoteOptionId, List<LineItemId> lineItemIds) {
        final FutureAssetPriceReportDTO priceReportDTO = customerInventoryDriverManager.getFutureAssetPriceReportDriver().post(lineItemIds);

        ArrayList<FutureAssetPricesModel> futureAssetPricesModels = new ArrayList<FutureAssetPricesModel>();
        for (FutureAssetPricesDTO futureAssetPricesDTO : priceReportDTO.getPriceList()) {
            futureAssetPricesModels.add(futureAssetPricesModelFactory.create(customerId, projectId, quoteOptionId, futureAssetPricesDTO));
        }
        return futureAssetPricesModels;
    }

    public FutureAssetPricesModel get(String customerId, String projectId, String quoteOptionId, AssetDTO asset, PriceSuppressStrategy priceSuppressStrategy) {
        return futureAssetPricesModelFactory.create(customerId, projectId, quoteOptionId, asset, priceSuppressStrategy);
    }

    public FutureAssetPricesModel get(String customerId, String projectId, String quoteOptionId, String lineItemId, PriceSuppressStrategy priceSuppressStrategy) {
        final OptionBasedFutureAssetDriver optionBasedFutureAssetDriver = customerInventoryDriverManager.getOptionBasedFutureAssetDriver(
            new LineItemId(lineItemId));
        return get(customerId, projectId, quoteOptionId, optionBasedFutureAssetDriver.get(), priceSuppressStrategy);
    }

    public void save(FutureAssetPricesModel futureAssetPricesModel) {
        final FutureAssetPriceDriver futureAssetPriceDriver = customerInventoryDriverManager.getFutureAssetPriceDriver(
            new LineItemId(futureAssetPricesModel.getLineItemId()),
            new LengthConstrainingProductInstanceId(futureAssetPricesModel.getId()));
        futureAssetPriceDriver.put(futureAssetPricesModel.getPricesDTO());
    }

    public void updatePricingStatus(FutureAssetPricesModel futureAssetPricesModel, PricingStatus status) {
        final OptionBasedFutureAssetResourceClient optionBasedFutureAssetResourceClient = customerInventoryDriverManager.getOptionBasedFutureAssetResourceClient();
        final AssetDTO asset = optionBasedFutureAssetResourceClient.getByOption(futureAssetPricesModel.getLineItemId());
        asset.setPricingStatus(status);
        optionBasedFutureAssetResourceClient.put(asset.getLineItemId(), asset.getId(), asset);
    }
    public List<String> getOwnerAssetForLineItem(String assetId, Long version, String relationshipName) {

        List<String> ownerLineItemIdsList = new ArrayList();
        RelationshipName relationName = RelationshipName.newInstance(relationshipName);
        List<AssetDTO> assetDtoList = customerInventoryDriverManager.getAssetDriver().getOwnerAssets(assetId,version,relationName);
        if(isNotNull(assetDtoList) && assetDtoList.size() > 0 ){
            for(AssetDTO assetDTO : assetDtoList){
                if(isNotNull(assetDTO)){
                    ownerLineItemIdsList.add(assetDTO.getLineItemId());
                }
            }
        }
        return ownerLineItemIdsList;
    }
}
