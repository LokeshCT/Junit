package com.bt.rsqe.projectengine.web.model.modelfactory;

import com.bt.rsqe.configuration.SqeAppUrlConfig;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.resource.ProductAgreementResourceClient;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class LineItemModelFactoryImpl implements LineItemModelFactory {

    public LineItemModelFactoryImpl(ExpedioProjectResource projects,
                                    FutureAssetPricesFacade productInstancePricesFacade,
                                    ProductIdentifierFacade productIdentifierFacade,
                                    UriFactory productConfiguratorUriBuilder, PmrClient pmrClient,
                                    ProductInstanceClient productInstanceClient,
                                    ProductAgreementResourceClient productAgreementResourceClient, SqeAppUrlConfig sqeAppUrlConfig) {
        this.projects = projects;
        this.productInstancePricesFacade = productInstancePricesFacade;
        this.productIdentifierFacade = productIdentifierFacade;
        this.productConfiguratorUriBuilder = productConfiguratorUriBuilder;
        this.pmrClient = pmrClient;
        this.productInstanceClient = productInstanceClient;
        this.productAgreementResourceClient=productAgreementResourceClient;
        this.sqeAppUrlConfig = sqeAppUrlConfig;
    }

    private FutureAssetPricesFacade productInstancePricesFacade;
    private ProductIdentifierFacade productIdentifierFacade;
    private UriFactory productConfiguratorUriBuilder;
    private PmrClient pmrClient;
    private ExpedioProjectResource projects;
    private ProductInstanceClient productInstanceClient;
    private ProductAgreementResourceClient productAgreementResourceClient;
    private SqeAppUrlConfig sqeAppUrlConfig;
    @Override
    public LineItemModel create(String projectId, String quoteOptionId, String customerId, String contractId, QuoteOptionItemDTO dto, PriceSuppressStrategy priceSuppressStrategy, QuoteOptionDTO quoteOptionDTO) {
        return new LineItemModel(projectId,
                                 quoteOptionId,
                                 customerId,
                                 contractId,
                                 dto,
                                 projects,
                                 productInstancePricesFacade,
                                 productIdentifierFacade,
                                 productConfiguratorUriBuilder, dto.parent == null ? null : create(projectId, quoteOptionId, customerId, contractId, dto.parent, priceSuppressStrategy, quoteOptionDTO), pmrClient, priceSuppressStrategy, productInstanceClient, quoteOptionDTO,productAgreementResourceClient,sqeAppUrlConfig);
    }

    @Override
    public List<LineItemModel> create(final String projectId, final String quoteOptionId, final String customerId, final String contractId, List<QuoteOptionItemDTO> lineItems) {
        return newArrayList(transform(lineItems, new Function<QuoteOptionItemDTO, LineItemModel>() {
            @Override
            public LineItemModel apply(@Nullable QuoteOptionItemDTO input) {
                return create(projectId, quoteOptionId, customerId, contractId, input, PriceSuppressStrategy.None, null);
            }
        }));
    }
}
