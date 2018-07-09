package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.google.common.base.Optional;
import org.mockito.Matchers;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.UUID;

import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static org.mockito.Mockito.*;

public class LineItemModelFixture {
    public static Builder aLineItemModel() {
        return new Builder();
    }

    public static class Builder {
        private QuoteOptionItemDTOFixture.Builder dtoBuilder = aQuoteOptionItemDTO();
        private FutureAssetPricesFacade productInstancePricesFacade = mock(FutureAssetPricesFacade.class);
        private ProductIdentifierFacade productIdentifierFacade;
        private UriFactory productConfiguratorUriBuilder;
        private ExpedioProjectResource projectResource;
        private ProductInstanceClient productInstanceClient = getMockedInstanceClient();
        private String customerId;
        private String contractId;
        private PriceSuppressStrategy priceSuppressStrategy = PriceSuppressStrategy.None;
        private LineItemModel parent;
        private String projectId = "projectId";
        private String quoteOptionId = "quoteOptionId";
        private FutureAssetPricesModel futureAssetPricesModel = mock(FutureAssetPricesModel.class);
        private PmrClient pmrClient = PmrMocker.getMockedInstance(true);
        final QuoteOptionDTO quoteOptionDTO = QuoteOptionDTOFixture.aQuoteOptionDTO()
            .withName("quoteOptionName")
            .withCreationDate("2014-01-01T08:00:00.500+01:00")
            .withCurrency("USD")
            .withContractTerm("12")
            .withCreatedBy("forename surname")
            .build();

        public Builder with(QuoteOptionItemDTOFixture.Builder quoteOptionDtoBuilder) {
            dtoBuilder = quoteOptionDtoBuilder;
            return this;
        }

        public Builder withPmr(PmrClient pmr) {
            this.pmrClient = pmr;
            return this;
        }

        public Builder with(FutureAssetPricesFacade productInstancePricesFacade) {
            this.productInstancePricesFacade = productInstancePricesFacade;
            return this;
        }

        public Builder with(ProductIdentifierFacade productIdentifierFacade) {
            this.productIdentifierFacade = productIdentifierFacade;
            return this;
        }

        public Builder with(ExpedioProjectResource projectResource) {
            this.projectResource = projectResource;
            return this;
        }

        public Builder with(ProductInstanceClient productInstanceClient) {
            this.productInstanceClient = productInstanceClient;
            return this;
        }

        public Builder with(UriFactory productConfiguratorUriBuilder) {
            this.productConfiguratorUriBuilder = productConfiguratorUriBuilder;
            return this;
        }

        public Builder thatIsSiteAgnostic(){
            return forSite(null);
        }

        public Builder forSite(SiteDTO value) {
            if(!new MockUtil().isMock(productInstancePricesFacade)){
                throw new RuntimeException("productInstancePricesFacade should be a mocked to support makeSiteAgnostic");
            }
            when(productInstanceClient.get(any(LineItemId.class))).thenReturn(DefaultProductInstanceFixture.aProductInstance().build());
            when(productInstancePricesFacade.get(eq(customerId), eq(projectId), eq(quoteOptionId), Matchers.<AssetDTO>any(), eq(PriceSuppressStrategy.None))).thenReturn(futureAssetPricesModel);
            when(futureAssetPricesModel.getSite()).thenReturn(value);
            return this;
        }

        public Builder forProductCategory(String productCategoryName) {
            Optional<ProductIdentifier> productCategoryIdentifier = Optional.of(new ProductIdentifier("productId", productCategoryName, "1.0"));
            when(pmrClient.getProductHCode(anyString())).thenReturn(productCategoryIdentifier);
            return this;
        }

        public Builder isVisibleOnOfferDetailsPage(boolean isVisible){
            Pmr.ProductOfferings productOfferings = mock(Pmr.ProductOfferings.class);
            ProductOffering offering = mock(ProductOffering.class);
            when(productOfferings.get()).thenReturn(offering);
            when(pmrClient.productOffering(ProductSCode.newInstance(anyString()))).thenReturn(productOfferings);
            when(offering.isVisibleInOnlineSummary()).thenReturn(isVisible);
            when(offering.isInFrontCatalogue()).thenReturn(isVisible);
            return this;
        }

        //public Builder forPricingStatus(PricingStatus pricingStatus){
        //    when(parent.getPricingStatusDescription()).thenReturn(pricingStatus.getDescription());
        //    return this;
        //}

        public LineItemModel build() {
            return new LineItemModel(projectId,
                                     quoteOptionId,
                                     customerId,
                                     contractId, dtoBuilder.build(),
                                     projectResource,
                                     productInstancePricesFacade,
                                     productIdentifierFacade,
                                     productConfiguratorUriBuilder, parent, pmrClient, priceSuppressStrategy, productInstanceClient, quoteOptionDTO, null, null);
        }

        public Builder withCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder withPriceSuppressStrategy(PriceSuppressStrategy priceSuppressStrategy) {
            this.priceSuppressStrategy = priceSuppressStrategy;
            return this;
        }

        public Builder withContractId(String contractId) {
            this.contractId = contractId;
            return this;
        }

        public Builder withParent(LineItemModel parent) {
            this.parent = parent;
            return this;
        }

        public Builder withProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder withQuoteOptionId(String quoteOptionId) {
            this.quoteOptionId = quoteOptionId;
            return this;
        }

        public Builder withQuoteOptionItemDTOId(String quoteOptionItemDTOId) {
            this.dtoBuilder = aQuoteOptionItemDTO().withId(quoteOptionItemDTOId);
            return this;
        }

        public Builder withQuoteOptionItemDTOOrderId(String orderId) {
            this.dtoBuilder = aQuoteOptionItemDTO().withOrderId(orderId);
            return this;
        }

        private static ProductInstanceClient getMockedInstanceClient() {
            ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
            doAnswer(new Answer<AssetDTO>() {
                @Override
                public AssetDTO answer(InvocationOnMock invocation) throws Throwable {
                    LineItemId lineItemId = (LineItemId)invocation.getArguments()[0];
                    return AssetDTOFixture.anAsset().withLineItemId(null != lineItemId ? lineItemId.value() : UUID.randomUUID().toString()).build();
                }
            }).when(productInstanceClient).getAssetDTO(any(LineItemId.class));
            return productInstanceClient;
        }
    }
}
