package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;


import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.filter.AssetFilter;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.AvailableAsset;
import com.bt.rsqe.domain.product.Cardinality;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.isNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.*;
import static org.apache.commons.lang.StringUtils.*;

public class CardinalityValidator {

    private ProductInstanceClient productInstanceClient;
    private SiteFacade siteFacade;

    public CardinalityValidator(ProductInstanceClient productInstanceClient, SiteFacade siteFacade) {
        this.productInstanceClient = productInstanceClient;
        this.siteFacade = siteFacade;
    }


    public CardinalityValidationResult validateContractCardinality(String customerId, String contractId, String quoteOptionId, ProductOffering offering, int noOfProductsBeingAdded, final AssetKey assetToBeIgnored) {
        List<AvailableAsset> contractAssets = productInstanceClient.getContractAssets(new CustomerId(customerId),
                                                                                      new ContractId(contractId),
                                                                                      new ProductCode(offering.getProductIdentifier().getProductId()),
                                                                                      new ProductVersion(offering.getProductIdentifier().getVersionNumber()),
                                                                                      AssetFilter.approvedAssetsFilter(),
                                                                                      AssetFilter.draftAssetsForQuoteOptionFilter(quoteOptionId));


        int totalContractAssets = contractAssets.size();

        if (!isNull(assetToBeIgnored) && !contractAssets.isEmpty()) {
            if (assetToBeIgnoredAvailable(assetToBeIgnored, contractAssets)) {
                totalContractAssets = totalContractAssets - 1;
            }
        }

        Cardinality contractCardinality = offering.getContractCardinality();
        int maximumContractCardinality = contractCardinality.isDynamicCardinality() ? contractCardinality.getMaxCardinalityByExpression(customerId) : contractCardinality.getMax();

        if ((totalContractAssets + noOfProductsBeingAdded) > maximumContractCardinality) {
            return CardinalityValidationResult.failed(format("Contract Cardinality Failed - %s can have only %s instance(s) for the Customer.",
                                                             offering.getProductIdentifier().getProductName(),
                                                             contractCardinality.getMax()));
        }

        return CardinalityValidationResult.success();
    }

    public CardinalityValidationResult validateSiteCardinality(String customerId, String projectId, String quoteOptionId, String siteId, ProductOffering offering, int noOfProductsBeingAdded, AssetKey assetToBeIgnored) {
        if(isEmpty(siteId))  {
            SiteDTO centralSite = siteFacade.getCentralSite(customerId, projectId);
            siteId = centralSite.bfgSiteID;
        }

        ProductCode productCode = new ProductCode(offering.getProductIdentifier().getProductId());
        ProductVersion version = new ProductVersion(offering.getProductIdentifier().getVersionNumber());
        List<AvailableAsset> approvedProducts = productInstanceClient.getApprovedAssets(new SiteId(siteId), productCode, version);
        List<AvailableAsset> draftProducts = productInstanceClient.getDraftAssets(new SiteId(siteId), productCode, version, quoteOptionId);

        List<AvailableAsset> availableAssets = newArrayList();
        availableAssets.addAll(approvedProducts);
        availableAssets.addAll(draftProducts);

        int totalSiteAssets = availableAssets.size();
        if (!isNull(assetToBeIgnored) && !availableAssets.isEmpty()) {
            if(assetToBeIgnoredAvailable(assetToBeIgnored, availableAssets)) {
                totalSiteAssets = totalSiteAssets -1;
            }
        }

        Cardinality siteCardinality = offering.getSiteCardinality();
        int maximumSiteCardinality = siteCardinality.isDynamicCardinality() ? siteCardinality.getMaxCardinalityByExpression(customerId) : siteCardinality.getMax();
        if ((totalSiteAssets + noOfProductsBeingAdded) > maximumSiteCardinality) {
            return CardinalityValidationResult.failed(format("Site Cardinality Failed - %s can have only %s instance(s) for the Customer.",
                                                             offering.getProductIdentifier().getProductName(),
                                                             maximumSiteCardinality));
        }
        return CardinalityValidationResult.success();

    }


    private boolean assetToBeIgnoredAvailable(final AssetKey assetToBeIgnored, List<AvailableAsset> contractAssets) {
        return Iterables.tryFind(contractAssets, new Predicate<AvailableAsset>() {
            @Override
            public boolean apply(AvailableAsset input) {
                return input.equals(new AvailableAsset(assetToBeIgnored.getAssetId(), assetToBeIgnored.getAssetVersion()));
            }
        }).isPresent();
    }
}
