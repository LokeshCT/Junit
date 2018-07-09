package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.PricingStrategyDecider;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.model.DiscountUpdater;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.ProjectedUsageModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Optional;
import org.mockito.Matchers;

import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture.*;
import static org.mockito.Mockito.*;

public class FutureAssetPricesModelFixture {

    public static FutureAssetPricesModelFixture aFutureAssetPricesModel() {
        return new FutureAssetPricesModelFixture();
    }

    private ProductInstanceClient productInstanceClient = aReturnAllInstanceClient();
    private SiteFacade siteFacade;
    private ProductIdentifierFacade productIdentifierFacade;
    private FutureAssetPricesDTOFixture.Builder builder = aFutureAssetPricesDTO();
    private FutureAssetPricesDTO dto;
    private ProjectedUsageModelFactory projectedUsageModelFactory;
    private PriceSuppressStrategy priceSuppressStrategy = PriceSuppressStrategy.None;
    private DiscountUpdater discountUpdater;
    private PricingStrategyDecider pricingStrategyDecider;
    private PricingConfig pricingConfig;
    private ProjectResource projectResource;
    private static final String CUSTOMER_ID = "customerId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String LINE_ITEM_ID = "lineItemId";

    public FutureAssetPricesModelFixture with(SiteFacade siteFacade) {
        this.siteFacade = siteFacade;
        return this;
    }

    public FutureAssetPricesModelFixture with(ProductIdentifierFacade productIdentifierFacade) {
        this.productIdentifierFacade = productIdentifierFacade;
        return this;
    }

    public FutureAssetPricesModel build() {
        final FutureAssetPricesDTO futureAssetPricesDTO = dto == null ? builder.build() : dto;

        PricingClient pricingClient = mock(PricingClient.class);
        if(pricingConfig == null) {
            pricingConfig = new PricingConfig();
        }
        doReturn(pricingConfig).when(pricingClient).getPricingConfig();

        if(null == projectResource) {
            projectResource = mock(ProjectResource.class);
            QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
            when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
            QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
            when(quoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(quoteOptionItemResource);
            QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId(LINE_ITEM_ID)
                                                                            .withAction("Provide")
                                                                            .build();
            when(quoteOptionItemResource.get(Matchers.<String>any(String.class))).thenReturn(quoteOptionItemDTO);
        }

        return new FutureAssetPricesModel(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, futureAssetPricesDTO, siteFacade, productIdentifierFacade, projectedUsageModelFactory, priceSuppressStrategy, discountUpdater, Optional.fromNullable(pricingStrategyDecider), pricingClient, projectResource, productInstanceClient);
    }

    public FutureAssetPricesModelFixture with(FutureAssetPricesDTOFixture.Builder builder) {
        this.builder = builder;
        return this;
    }

    public FutureAssetPricesModelFixture with(ProductInstanceClient productInstanceClient) {
        this.productInstanceClient = productInstanceClient;
        return this;
    }

    public FutureAssetPricesModelFixture with(FutureAssetPricesDTO dto) {
        this.dto = dto;
        return this;
    }

    public FutureAssetPricesModelFixture with(ProjectedUsageModelFactory projectedUsageModelFactory) {
        this.projectedUsageModelFactory = projectedUsageModelFactory;
        return this;
    }

    public FutureAssetPricesModelFixture with(PriceSuppressStrategy priceSuppressStrategy) {
        this.priceSuppressStrategy = priceSuppressStrategy;
        return this;
    }

    public FutureAssetPricesModelFixture with(DiscountUpdater discountUpdater) {
        this.discountUpdater = discountUpdater;
        return this;
    }

    public FutureAssetPricesModelFixture with(PricingStrategyDecider pricingStrategyDecider) {
        this.pricingStrategyDecider = pricingStrategyDecider;
        return this;
    }

    public FutureAssetPricesModelFixture with(PricingConfig pricingConfig) {
        this.pricingConfig = pricingConfig;
        return this;
    }

    public FutureAssetPricesModelFixture with(ProjectResource projectResource) {
        this.projectResource = projectResource;
        return this;
    }

    private static ProductInstanceClient aReturnAllInstanceClient() {
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        when(productInstanceClient.getByAssetKey(Matchers.<LengthConstrainingProductInstanceId>any(), Matchers.<ProductInstanceVersion>any())).thenReturn(DefaultProductInstanceFixture.aProductInstance().build());
        when(productInstanceClient.getAction(Matchers.<ProductInstance>any())).thenReturn(ChangeType.ADD);
        return productInstanceClient;
    }
}
