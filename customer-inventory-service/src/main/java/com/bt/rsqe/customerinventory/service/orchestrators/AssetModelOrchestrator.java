package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.LazyValue;
import com.bt.rsqe.bfgfacade.exception.BfgReadException;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.bfg.readers.AssetReader;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.CharacteristicName;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.repository.ImmutableAssetException;
import com.bt.rsqe.customerinventory.repository.ProductInstanceRepository;
import com.bt.rsqe.customerinventory.repository.StaleAssetException;
import com.bt.rsqe.customerinventory.repository.jpa.ExternalAssetRepository;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class AssetModelOrchestrator {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);

    private ProductInstanceRepository assetRepository;
    private ExternalAssetRepository externalAssetRepository;
    private AssetReader assetReader;
    private Pmr pmr;
    private ProjectResource projectResource;

    public AssetModelOrchestrator(ProductInstanceRepository assetRepository,
                                  ExternalAssetRepository externalAssetRepository,
                                  AssetReader assetReader,
                                  Pmr pmr,
                                  ProjectResource projectResource) {
        this.assetRepository = assetRepository;
        this.externalAssetRepository = externalAssetRepository;
        this.assetReader = assetReader;
        this.pmr = pmr;
        this.projectResource = projectResource;
    }

    public AssetDTO fetchAsset(AssetKey assetKey) {
        return assetRepository.getFutureAsset(new com.bt.rsqe.customerinventory.repository.jpa.keys.AssetKey(assetKey.getAssetId(), assetKey.getAssetVersion()));
    }

    public List<AssetDTO> fetchAssets(CustomerId customerId, ContractId contractId, ProductIdentifier productIdentifier) {
        return assetRepository.getFutureAssets(customerId,
                contractId,
                new ProductCode(productIdentifier.getProductId()),
                new ProductVersion(productIdentifier.getVersionNumber()));
    }

    public ProductOffering fetchBaseOffering(AssetDTO asset) {
        return pmr.productOffering(ProductSCode.newInstance(asset.getProductCode()))
                  .get();
    }

    public ProductOffering fetchOffering(AssetDTO asset) {
        return pmr.productOffering(ProductSCode.newInstance(asset.getProductCode()))
                  .withStencil(asset.getStencilId())
                  .get();
    }

    public QuoteOptionDTO fetchQuoteOption(AssetDTO asset) {
        return projectResource.quoteOptionResource(asset.getProjectId()).get(asset.getQuoteOptionId());
    }

    public boolean isExternalRelationship(ProductIdentifier linkedIdentifier) {
        ProductOffering productOffering = pmr.productOffering(com.bt.rsqe.domain.bom.parameters.ProductSCode.newInstance(linkedIdentifier.getProductId())).get();
        return SimpleProductOfferingType.isExternalType(productOffering.getSimpleProductOfferingType());
    }

    public List<AssetDTO> fetchExternalAssets(AssetDTO owner,
                                              ProductIdentifier linkedIdentifier,
                                              LazyValue<String> quoteOptionName,
                                              boolean siteMatters,
                                              final RelationshipName relationshipName, boolean isBundledProduct) {
        List<AssetDTO> assets = newArrayList();

        CustomerId customerId = new CustomerId(owner.getCustomerId());
        ContractId contractId = new ContractId(owner.getContractId());
        boolean isExternalRelationship = isExternalRelationship(linkedIdentifier);

        try {
            final ProductCode productCode = new ProductCode(linkedIdentifier.getProductId());
            List<AssetDTO> externalAssets = newArrayList();

            if (siteMatters) {
                if(!Strings.isNullOrEmpty(owner.getSiteId())) {
                    Optional<String> cpeServiceInstanceId = new ServiceInstanceIdResolver(owner).get(linkedIdentifier, relationshipName);
                    externalAssets = externalAssetRepository.getAssets(productCode, new SiteId(owner.getSiteId()), quoteOptionName, isBundledProduct, cpeServiceInstanceId);
                }
            } else {
                externalAssets = externalAssetRepository.getAssets(contractId, productCode, quoteOptionName);

                if (externalAssets.isEmpty() && isExternalRelationship) {
                    try {
                        if (owner.hasCharacteristic(new CharacteristicName(VPN_ID)) && !Strings.isNullOrEmpty(owner.getCharacteristic(VPN_ID).getValue())) {
                            externalAssets = assetReader.readExternalAssets(customerId, productCode, relationshipName, owner.getCharacteristic(VPN_ID).getValue());
                        } else {
                            externalAssets = assetReader.readExternalAssets(customerId, productCode, relationshipName, null);
                        }
                    } catch (BfgReadException e) {
                        logger.errorReadingExternalAssets(e);
                    }
                }
            }

            assets.addAll(externalAssets);
        } catch (ResourceNotFoundException exception) {
            //ignore
        }

        return assets;
    }

    public void put(AssetDTO assetDTO) throws ImmutableAssetException, StaleAssetException {
        assetRepository.saveStubAsset(assetDTO);
    }

    interface Logger {
        @Log(level = LogLevel.ERROR, format = "Error occurred while reading external assets. %s")
        void errorReadingExternalAssets(BfgReadException e);
    }
}