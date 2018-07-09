package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.logging.LogLevel.*;
import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class QuoteOptionDetailExtender {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private final ProjectResource projectResource;
    private final CIFAssetJPARepository cifAssetRepository;
    private Cache<String, QuoteOptionDTO> quoteOptionCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    private Cache<String, QuoteOptionItemDTO> quoteOptionItemCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    public QuoteOptionDetailExtender(ProjectResource projectResource, CIFAssetJPARepository cifAssetRepository) {
        this.projectResource = projectResource;
        this.cifAssetRepository = cifAssetRepository;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset) {
        if (QuoteOptionItemDetail.isInList(cifAssetExtensions)) {
            CIFAssetQuoteOptionItemDetail quoteOptionItemDetail;
            if (cifAsset.getAssetType() != AssetType.STUB && isNotNull(cifAsset.getLineItemId())) {
                final QuoteOptionDTO quoteOptionDTO = getQuoteOption(cifAsset.getProjectId(), cifAsset.getQuoteOptionId());
                final QuoteOptionItemDTO quoteOptionItemDTO = getQuoteOptionItem(cifAsset.getProjectId(), cifAsset.getQuoteOptionId(), cifAsset.getLineItemId());
                final int lockVersion = cifAssetRepository.getLockVersion(cifAsset.getLineItemId());

                ProductCategoryCode productCategoryCode = ProductCategoryCode.catCodeSet(quoteOptionItemDTO.getProductCategoryCode()) ? quoteOptionItemDTO.getProductCategoryCode() : ProductCategoryCode.NIL;
                quoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(quoteOptionItemDTO.status,
                                                                          lockVersion,
                                                                          quoteOptionDTO.getMigrationQuote(),
                                                                          quoteOptionItemDTO.isIfc,
                                                                          quoteOptionDTO.getCurrency(),
                                                                          quoteOptionDTO.contractTerm,
                                                                          quoteOptionItemDTO.isImportable,
                                                                          quoteOptionItemDTO.getCustomerRequiredDate(),
                                                                          quoteOptionItemDTO.contractDTO.priceBooks,
                                                                          quoteOptionItemDTO.action,
                                                                          quoteOptionDTO.getName(),
                                                                          quoteOptionItemDTO.hasFullAddress,
                                                                          productCategoryCode,
                                                                          quoteOptionItemDTO.getBundleItemId(),
                                                                          quoteOptionItemDTO.isBundleProduct());

            } else {
                quoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(QuoteOptionItemStatus.DRAFT,
                                                                          -1,
                                                                          false,
                                                                          false,
                                                                          "",
                                                                          "",
                                                                          false,
                                                                          JaxbDateTime.NIL,
                                                                          new ArrayList<PriceBookDTO>(),
                                                                          LineItemAction.PROVIDE.getDescription(),
                                                                          "",
                                                                          true, ProductCategoryCode.NIL, null, false);
            }
            cifAsset.loadQuoteOptionItemDetail(quoteOptionItemDetail);

        }
    }

    public QuoteOptionItemDTO getQuoteOptionItem(final String projectId, final String quoteOptionId, final String lineItemId) {
        QuoteOptionItemDTO quoteOptionItemDTO;
        try {
            quoteOptionItemDTO = quoteOptionItemCache.get(lineItemId, new Callable<QuoteOptionItemDTO>() {
                @Override
                public QuoteOptionItemDTO call() throws Exception {
                    return getQuoteOptionItemFromServer(projectId, quoteOptionId, lineItemId);
                }
            });
        } catch (Exception e) {
            logger.fetchQuoteOptionItem(lineItemId);
            e.printStackTrace();
            return getQuoteOptionItemFromServer(projectId, quoteOptionId, lineItemId);
        }
        return quoteOptionItemDTO;
    }

    public QuoteOptionDTO getQuoteOption(final String projectId, final String quoteOptionId) {
        QuoteOptionDTO quoteOptionDTO;
        try {
            quoteOptionDTO = quoteOptionCache.get(quoteOptionId, new Callable<QuoteOptionDTO>() {
                @Override
                public QuoteOptionDTO call() throws Exception {
                    return getQuoteOptionFromServer(projectId, quoteOptionId);
                }
            });
        } catch (Exception e) {
            logger.fetchQuoteOption(quoteOptionId);
            e.printStackTrace();
            return getQuoteOptionFromServer(projectId, quoteOptionId);
        }
        return quoteOptionDTO;
    }

    private QuoteOptionItemDTO getQuoteOptionItemFromServer(String projectId, String quoteOptionId, String lineItemId) {
        QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        return quoteOptionResource.quoteOptionItemResource(quoteOptionId).get(lineItemId);
    }

    private QuoteOptionDTO getQuoteOptionFromServer(String projectId, String quoteOptionId) {
        QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        return quoteOptionResource.getQuoteOptionHeaderDetails(quoteOptionId);
    }

    interface Logger {
        @Log(level = WARN, format = "Fetching quote option item from cache is failed for id - %s , getting it from server")
        void fetchQuoteOptionItem(String lineItemId);

        @Log(level = WARN, format = "Fetching quote option from cache is failed for id - %s , getting it from server")
        void fetchQuoteOption(String quoteOptionId);
    }
}
