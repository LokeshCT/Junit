package com.bt.rsqe.projectengine.web.model.modelfactory;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.PricingStrategyDecider;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.model.DiscountUpdater;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Optional;

public class FutureAssetPricesModelFactoryImpl implements FutureAssetPricesModelFactory{
    private ProductInstanceClient productInstanceClient;
    private SiteFacade siteFacade;
    private ProductIdentifierFacade productIdentifierFacade;
    private ProjectedUsageModelFactory projectedUsageModelFactory;
    private DiscountUpdater discountUpdater;
    private PricingStrategyDecider pricingStrategyDecider;
    private PricingClient pricingClient;
    private ProjectResource projects;

    public FutureAssetPricesModelFactoryImpl(ProductInstanceClient productInstanceClient, SiteFacade siteFacade, ProductIdentifierFacade productIdentifierFacade, ProjectedUsageModelFactory projectedUsageModelFactory, DiscountUpdater discountUpdater, PricingStrategyDecider pricingStrategyDecider, PricingClient pricingClient, ProjectResource projects) {
        this.productInstanceClient = productInstanceClient;
        this.siteFacade = siteFacade;
        this.productIdentifierFacade = productIdentifierFacade;
        this.projectedUsageModelFactory = projectedUsageModelFactory;
        this.discountUpdater = discountUpdater;
        this.pricingStrategyDecider = pricingStrategyDecider;
        this.pricingClient = pricingClient;
        this.projects = projects;
    }

    @Override
    public FutureAssetPricesModel create(String customerId, String projectId, String quoteOptionId, AssetDTO assetDTO, PriceSuppressStrategy priceSuppressStrategy) {
        return new FutureAssetPricesModel(customerId, projectId, quoteOptionId, new FutureAssetPricesDTO(assetDTO), siteFacade, productIdentifierFacade, projectedUsageModelFactory, priceSuppressStrategy, discountUpdater, Optional.fromNullable(pricingStrategyDecider), pricingClient, projects, productInstanceClient);
    }

    @Override
    public FutureAssetPricesModel create(String customerId, String projectId, String quoteOptionId, FutureAssetPricesDTO futureAssetPricesDTO) {
        return new FutureAssetPricesModel(customerId, projectId, quoteOptionId, futureAssetPricesDTO, siteFacade, productIdentifierFacade, projectedUsageModelFactory, PriceSuppressStrategy.None, discountUpdater, Optional.fromNullable(pricingStrategyDecider), pricingClient, projects, productInstanceClient);
    }

}
