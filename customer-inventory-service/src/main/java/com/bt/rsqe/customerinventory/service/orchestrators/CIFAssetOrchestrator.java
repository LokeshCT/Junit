package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.customerinventory.repository.StaleAssetException;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetLineItemKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.extenders.CIFAssetExtender;
import com.bt.rsqe.customerinventory.service.providers.CIFAssetCreator;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.utils.AssertObject;
import com.google.common.base.Optional;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.perf4j.StopWatch;
import org.perf4j.aop.Profiled;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.logging.LogLevel.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.collections.CollectionUtils.*;

public class CIFAssetOrchestrator {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private CIFAssetExtender cifAssetExtender;
    private CIFAssetCreator assetProvider;
    private final CIFAssetJPARepository cifAssetRepository;

    public CIFAssetOrchestrator(CIFAssetJPARepository cifAssetRepository) {
        this.cifAssetRepository = cifAssetRepository;
    }

    public void setCifAssetProvider(CIFAssetCreator assetProvider) {
        this.assetProvider = assetProvider;
    }

    public void setCifAssetExtender(CIFAssetExtender cifAssetExtender) {
        this.cifAssetExtender = cifAssetExtender;
    }

    @Profiled
    public CIFAsset getAsset(CIFAssetKey cifAssetKey) {
        logger.fetchingAsset(cifAssetKey);
        StopWatch stopwatch = new StopWatch("getAsset");

        CIFAsset asset = AssetCacheManager.getAsset(cifAssetKey, this);
        if (asset != null) {
            logger.fetchingAsset(cifAssetKey, true, stopwatch);
            return asset;
        }

        CIFAsset cifAsset = getCifAsset(cifAssetKey);
        logger.fetchingAsset(cifAssetKey, false, stopwatch);
        return cifAsset;
    }

    public CIFAsset getCifAsset(CIFAssetKey key) {
        CIFAsset cifAsset = cifAssetRepository.getAsset(key.getAssetKey(), eagerRelationships(key.getExtensions()));
        extendAsset(cifAsset, key);
        return cifAsset;
    }

    @Profiled
    public CIFAsset getAsset(CIFAssetLineItemKey cifAssetLineItemKey) {
        logger.fetchingAssetByLineItem(cifAssetLineItemKey);
        StopWatch stopwatch = new StopWatch("getAsset");

        CIFAsset asset = AssetCacheManager.getAsset(cifAssetLineItemKey, this);
        if (asset != null) {
            logger.fetchingAssetByLineItem(cifAssetLineItemKey, true, stopwatch);
            return asset;
        }

        CIFAsset cifAsset = getCifRootAsset(cifAssetLineItemKey);
        logger.fetchingAssetByLineItem(cifAssetLineItemKey, false, stopwatch);
        return cifAsset;

    }

    public CIFAsset getCifRootAsset(CIFAssetLineItemKey cifAssetLineItemKey) {
        CIFAsset cifAsset = cifAssetRepository.getRootAsset(cifAssetLineItemKey.getLineItemId(), eagerRelationships(cifAssetLineItemKey.getExtensions()));
        cifAssetExtender.extend(cifAsset, cifAssetLineItemKey.getUserToken(), cifAssetLineItemKey.getLoginName(), cifAssetLineItemKey.getExtensions());
        return cifAsset;
    }

    @Profiled
    public void saveAssetAndClearCaches(CIFAsset asset) {
        logger.savingAsset(asset.getAssetKey());
        StopWatch stopwatch = new StopWatch("saveAssetAndClearCaches");
        cifAssetRepository.saveAsset(asset);
        logger.savingAsset(asset.getAssetKey(), stopwatch);
        AssetCacheManager.clearAssetCaches();
    }

    @Profiled
    public void saveAsset(CIFAsset asset) {
        logger.savingAsset(asset.getAssetKey());
        StopWatch stopwatch = new StopWatch("saveAsset");
        cifAssetRepository.saveAsset(asset);
        logger.savingAsset(asset.getAssetKey(), stopwatch);
    }

    @Profiled
    public CIFAsset getParentAsset(CIFAssetKey cifAssetKey) {
        logger.gettingParentAsset(cifAssetKey);
        StopWatch stopwatch = new StopWatch("getParentAsset");

        CIFAsset asset = AssetCacheManager.getParentAsset(cifAssetKey, this);
        if (asset != null) {
            logger.gettingParentAsset(cifAssetKey, true, stopwatch);
            return asset;
        }

        CIFAsset cifAsset = getCifParentAsset(cifAssetKey);
        logger.gettingParentAsset(cifAssetKey, false, stopwatch);
        return cifAsset;
    }

    public CIFAsset getCifParentAsset(CIFAssetKey cifAssetKey) {
        CIFAsset cifAsset = cifAssetRepository.getParentAsset(cifAssetKey.getAssetKey(), eagerRelationships(cifAssetKey.getExtensions()));
        extendAsset(cifAsset, cifAssetKey);
        return cifAsset;
    }

    public CIFAsset getCifParentOrOwnerAsset(CIFAssetKey cifAssetKey) {
        CIFAsset cifAsset = cifAssetRepository.getParentOrOwnerAsset(cifAssetKey.getAssetKey(), eagerRelationships(cifAssetKey.getExtensions()));
        extendAsset(cifAsset, cifAssetKey);
        return cifAsset;
    }

    @Profiled
    public boolean isRootAsset(AssetKey cifAssetKey) {
        logger.isRootAsset(cifAssetKey);
        StopWatch stopwatch = new StopWatch("isRootAsset");
        boolean result = cifAssetRepository.isRootAsset(cifAssetKey);
        logger.isRootAsset(cifAssetKey, stopwatch);
        return result;
    }

    public List<CIFAsset> getOwnerAssets(CIFAssetKey cifAssetKey) {
        return getOwnerAssets(cifAssetKey, new ArrayList<AssetVersionStatus>(), Optional.<String>absent(), Optional.<String>absent());
    }

    @Profiled
    public List<CIFAsset> getOwnerAssets(CIFAssetKey cifAssetKey, List<AssetVersionStatus> allowedStatus, Optional<String> productCodeOptional, Optional<String> quoteOptionIdOptional) {

        OwnerAssetKey key = new OwnerAssetKey(cifAssetKey, allowedStatus, productCodeOptional, quoteOptionIdOptional);
        logger.gettingOwnerAssets(key);
        StopWatch stopwatch = new StopWatch("getOwnerAssets");

        List<CIFAsset> cifAssets = AssetCacheManager.getOwnerAssets(key, this);
        if (cifAssets != null) {
            logger.gettingOwnerAssets(key, true, stopwatch);
            return cifAssets;
        }

        List<CIFAsset> owners = getCifOwnerAssets(key);
        logger.gettingOwnerAssets(key, false, stopwatch);
        return owners;
    }

    //TODO: Omm to refactor this method to have a single query with builder based on the inputs
    public List<CIFAsset> getCifOwnerAssets(OwnerAssetKey ownerAssetKey) {
        List<CIFAsset> ownerAssets;
        if (!isEmpty(ownerAssetKey.getAllowedStatus())
                && ownerAssetKey.getProductCodeOptional().isPresent()
                && ownerAssetKey.getQuoteOptionIdOptional().isPresent()) {

            ownerAssets = cifAssetRepository.getOwnerAssets(ownerAssetKey.getCifAssetKey().getAssetKey(),
                    eagerRelationships(ownerAssetKey.getCifAssetKey().getExtensions()),
                    ownerAssetKey.getProductCodeOptional().get(),
                    ownerAssetKey.getQuoteOptionIdOptional().get(),
                    ownerAssetKey.getAllowedStatus());
        } else if (!isEmpty(ownerAssetKey.getAllowedStatus())
                && !ownerAssetKey.getProductCodeOptional().isPresent()
                && ownerAssetKey.getQuoteOptionIdOptional().isPresent()) {

            ownerAssets = cifAssetRepository.getOwnerAssets(ownerAssetKey.getCifAssetKey().getAssetKey(),
                    eagerRelationships(ownerAssetKey.getCifAssetKey().getExtensions()),
                    ownerAssetKey.getQuoteOptionIdOptional().get(),
                    ownerAssetKey.getAllowedStatus());
        } else if (isEmpty(ownerAssetKey.getAllowedStatus())
                && !ownerAssetKey.getProductCodeOptional().isPresent()
                && !ownerAssetKey.getQuoteOptionIdOptional().isPresent()) {

            ownerAssets = cifAssetRepository.getOwnerAssets(ownerAssetKey.getCifAssetKey().getAssetKey(), eagerRelationships(ownerAssetKey.getCifAssetKey().getExtensions()));
        } else {
            ownerAssets = cifAssetRepository.getOwnerAssets(ownerAssetKey.getCifAssetKey().getAssetKey(), eagerRelationships(ownerAssetKey.getCifAssetKey().getExtensions()), ownerAssetKey.getAllowedStatus());
        }

        for (CIFAsset ownerAsset : ownerAssets) {
            extendAsset(ownerAsset, ownerAssetKey.getCifAssetKey());
        }

        return ownerAssets;
    }

    @Profiled
    public Optional<CIFAsset> getInServiceAsset(CIFAssetKey cifAssetKey) {
        logger.gettingInServiceAsset(cifAssetKey);
        StopWatch stopwatch = new StopWatch("getInServiceAsset");

        Optional<CIFAsset> inServiceAsset = AssetCacheManager.getInServiceAsset(cifAssetKey, this);
        if (inServiceAsset.isPresent()) {
            logger.gettingInServiceAsset(cifAssetKey, true, stopwatch);
            return inServiceAsset;
        }

        Optional<CIFAsset> cifAssetOptional = inServiceAsset(cifAssetKey);
        logger.gettingInServiceAsset(cifAssetKey, false, stopwatch);
        return cifAssetOptional;
    }

    public Optional<CIFAsset> inServiceAsset(CIFAssetKey cifAssetKey) {
        final Optional<CIFAsset> inServiceAsset = cifAssetRepository.getInServiceAsset(cifAssetKey.getAssetKey(), eagerRelationships(cifAssetKey.getExtensions()));
        if (inServiceAsset.isPresent()) {
            extendAsset(inServiceAsset.get(), cifAssetKey);
        }
        return inServiceAsset;
    }

    public CIFAsset extendAsset(CIFAsset asset, List<CIFAssetExtension> extensions) {
        asset = cifAssetRepository.getRelationships(asset);
        if (!asset.hasExtensions(extensions)) {
            forceExtendAsset(asset, extensions);
        }
        return asset;
    }

    public CIFAsset forceExtendAsset(CIFAsset asset, List<CIFAssetExtension> extensions) {
        cifAssetExtender.extend(asset, extensions);
        return asset;
    }

    private boolean eagerRelationships(List<CIFAssetExtension> extensions) {
        return CIFAssetExtension.Relationships.isInList(extensions);
    }

    private void extendAsset(CIFAsset cifAsset, CIFAssetKey cifAssetKey) {
        cifAssetExtender.extend(cifAsset, cifAssetKey.getExtensions());
    }

    public boolean hasInServiceAsset(AssetKey assetKey) {
        return cifAssetRepository.hasInServiceAsset(assetKey);
    }

    public boolean hasProvisiongOrInServiceAsset(AssetKey assetKey) {
        return cifAssetRepository.hasProvisiongOrInServiceAsset(assetKey);
    }


    @Profiled
    public List<CIFAsset> getAssets(String customerId, String contractId, List<String> productCodes, String attributeName,
                                    String attributeValue, List<CIFAssetExtension> extensions) {
        logger.getAssets(customerId, contractId, productCodes, attributeName, attributeValue, extensions);
        StopWatch stopwatch = new StopWatch("getAssets");
        List<CIFAsset> assets = cifAssetRepository.getAssets(customerId, contractId, productCodes, attributeName,
                attributeValue, eagerRelationships(extensions));
        for (CIFAsset asset : assets) {
            extendAsset(asset, extensions);
        }
        logger.getAssets(customerId, contractId, productCodes, attributeName, attributeValue, extensions, stopwatch);
        return assets;
    }

    @Profiled
    public CIFAssetRelationship createAndRelateAsset(CIFAsset ownerAsset, String relationshipName,
                                                     String productCode, String stencilCode, String lineItemId, String siteId, String contractTerm,
                                                     String customerId, String contractId, String projectId, String quoteOptionId,
                                                     String alternateCity, ProductCategoryCode productCategoryCode) {
        logger.createAndRelateAsset(ownerAsset.getAssetKey(), relationshipName, productCode, stencilCode, lineItemId, contractTerm, customerId, contractId, projectId, quoteOptionId, alternateCity);
        StopWatch stopwatch = new StopWatch("createAndRelateAsset");
        extendAsset(ownerAsset, newArrayList(Relationships, ProductOfferingRelationshipDetail));

        final CIFAsset newAsset = assetProvider.createAsset(productCode, stencilCode, lineItemId, siteId, contractTerm,
                customerId, contractId, projectId, quoteOptionId, alternateCity, productCategoryCode, ownerAsset.getSlaId(),
                ownerAsset.getMagId(), ownerAsset.getSsvId(),ownerAsset.getContractResignStatus());

        logger.createAndRelateAsset(ownerAsset.getAssetKey(), relationshipName, productCode, stencilCode, lineItemId, contractTerm, customerId, contractId, projectId, quoteOptionId, alternateCity, stopwatch);
        return assetProvider.relateAssets(ownerAsset, newAsset, relationshipName);
    }

    @Profiled
    public CIFAssetRelationship relateAssets(CIFAsset ownerAsset, CIFAsset relatedAsset, String relationshipName) {
        logger.relateAssets(ownerAsset.getAssetKey(), relatedAsset.getAssetKey(), relationshipName);
        StopWatch stopwatch = new StopWatch("relateAssets");
        CIFAssetRelationship relationship = assetProvider.relateAssets(ownerAsset, relatedAsset, relationshipName);
        logger.relateAssets(ownerAsset.getAssetKey(), relatedAsset.getAssetKey(), relationshipName, stopwatch);
        return relationship;
    }

    @Profiled
    public void createLineItemLockVersion(String lineItemId) throws StaleAssetException {
        logger.createLineItemLockVersion(lineItemId);
        StopWatch stopwatch = new StopWatch("createLineItemLockVersion");
        cifAssetRepository.saveLineItemLockVersion(lineItemId, 0);
        logger.createLineItemLockVersion(lineItemId, stopwatch);
    }

    @Profiled
    public List<CIFAsset> getAssets(String customerId, String contractId, String productCode, List<CIFAssetExtension> extensions) {
        logger.getAssets(customerId, contractId, productCode, extensions);
        StopWatch stopwatch = new StopWatch("getAssets");

        StopWatch dbCallStopWatch = new StopWatch("eligibleExistingCandidatesDbCall");
        final List<CIFAsset> assets = cifAssetRepository.getAssets(customerId, contractId, productCode, eagerRelationships(extensions));
        logger.eligibleExistingCandidatesDbCall(customerId, contractId, productCode, extensions, dbCallStopWatch, assets.size());

        for (CIFAsset asset : assets) {
            StopWatch getAssetsAssetExtensionWatch = new StopWatch("eligibleExistingCandidatesAssetExtension");
            extendAsset(asset, extensions);
            logger.eligibleExistingCandidatesAssetExtension(asset.getAssetKey(), extensions, getAssetsAssetExtensionWatch);
        }
        logger.getAssets(customerId, contractId, productCode, extensions, stopwatch, assets.size());
        return assets;
    }

    @Profiled
    public List<CIFAsset> eligibleExistingCandidates(String customerId, String contractId, String productCode, List<CIFAssetExtension> extensions, String quoteOptionId, String siteId, boolean siteMatters) {
        logger.getAssets(customerId, contractId, productCode, extensions);
        StopWatch stopwatch = new StopWatch("eligibleExistingCandidates");

        StopWatch dbCallStopWatch = new StopWatch("eligibleExistingCandidatesDbCall");

        List<CIFAsset> assets;
        boolean loadRelationships = eagerRelationships(extensions);

        if (!AssertObject.isEmpty(siteId) && siteMatters) {
            assets = cifAssetRepository.getEligibleExistingCandidates(customerId, contractId, productCode, loadRelationships, quoteOptionId, siteId);
        } else {
            assets = cifAssetRepository.getEligibleExistingCandidates(customerId, contractId, productCode, loadRelationships, quoteOptionId);
        }

        logger.eligibleExistingCandidatesDbCall(customerId, contractId, productCode, extensions, dbCallStopWatch, assets.size());

        for (CIFAsset asset : assets) {
            StopWatch getAssetsAssetExtensionWatch = new StopWatch("eligibleExistingCandidatesAssetExtension");
            extendAsset(asset, extensions);
            logger.eligibleExistingCandidatesAssetExtension(asset.getAssetKey(), extensions, getAssetsAssetExtensionWatch);
        }
        logger.getAssets(customerId, contractId, productCode, extensions, stopwatch, assets.size());
        return assets;
    }

    @Profiled
    public Set<CancelRelationshipRequest> cancelAssetTree(AssetKey owningAssetKey, String relationshipName, CIFAsset assetToBeCancelled) {
        logger.cancelAssetTree(assetToBeCancelled.getAssetKey());
        StopWatch stopwatch = new StopWatch("cancelAssetTree");
        Set<CancelRelationshipRequest> result = cifAssetRepository.cancelAssetTree(owningAssetKey, relationshipName, assetToBeCancelled);
        logger.cancelAssetTree(assetToBeCancelled.getAssetKey(), stopwatch);
        return result;
    }

    public CIFAsset forceExtendAsset(CIFAsset cifAsset, CIFAssetCharacteristic cifAssetCharacteristic, List<CIFAssetExtension> cifAssetExtensions) {
        cifAssetExtender.extend(cifAsset, cifAssetCharacteristic, cifAssetExtensions);
        return cifAsset;
    }

    public boolean isMigratedCustomer(MigratedCustomerKey migratedCustomerKey) {
        Boolean migratedCustomer = AssetCacheManager.isMigratedCustomer(migratedCustomerKey, this);
        if (migratedCustomer != null) {
            return migratedCustomer;
        }
        return getMigratedCustomer(migratedCustomerKey);
    }

    public boolean getMigratedCustomer(MigratedCustomerKey migratedCustomerKey) {
        return cifAssetRepository.isMigratedCustomer(migratedCustomerKey);
    }

    public List<String> getBEndSiteIds(String ownerAssetId, String relationshipName, String bEndCharName, String ownerStencilId) {
        return cifAssetRepository.getBEndSiteIds(ownerAssetId, relationshipName, bEndCharName, ownerStencilId);
    }

    public List<String> getAendSiteIdByBEndSiteId(String ownerAssetId, String relationshipName, String bEndCharName, String aEndCharName, String ownerStencilId, String bEndSiteId) {
        return cifAssetRepository.getAendSiteIdByBEndSiteId(ownerAssetId, relationshipName, bEndCharName, aEndCharName, ownerStencilId, bEndSiteId);
    }

    public static class MigratedCustomerKey {

        private final String customerId;
        private final String contractId;
        private final List<String> productCodes;

        public MigratedCustomerKey(String customerId, String contractId, List<String> productCodes) {
            this.customerId = customerId;
            this.contractId = contractId;
            this.productCodes = productCodes;
        }

        public String getCustomerId() {
            return customerId;
        }

        public String getContractId() {
            return contractId;
        }

        public List<String> getProductCodes() {
            return productCodes;
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    interface Logger {
        @Log(level = DEBUG, format = "Fetching asset with cifAssetKey=%s")
        void fetchingAsset(CIFAssetKey cifAssetKey);

        @Log(level = DEBUG, format = "Fetching asset with cifAssetKey=%s cacheHit=%s stopwatch=%s")
        void fetchingAsset(CIFAssetKey cifAssetKey, boolean cacheHit, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Fetching asset with cifAssetLineItemKey=%s")
        void fetchingAssetByLineItem(CIFAssetLineItemKey cifAssetLineItemKey);

        @Log(level = DEBUG, format = "Fetching asset with cifAssetLineItemKey=%s cacheHit=%s stopwatch=%s")
        void fetchingAssetByLineItem(CIFAssetLineItemKey cifAssetLineItemKey, boolean cacheHit, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Getting parent asset with cifAssetKey=%s")
        void gettingParentAsset(CIFAssetKey cifAssetKey);

        @Log(level = DEBUG, format = "Getting parent asset with cifAssetKey=%s cacheHit=%s stopwatch=%s")
        void gettingParentAsset(CIFAssetKey cifAssetKey, boolean cacheHit, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Getting owner assets with OwnerAssetKey=%s")
        void gettingOwnerAssets(OwnerAssetKey ownerAssetKey);

        @Log(level = DEBUG, format = "Getting owner assets with cifAssetKey=%s cacheHit=%s stopwatch=%s")
        void gettingOwnerAssets(OwnerAssetKey ownerAssetKey, boolean cacheHit, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Getting in service asset with cifAssetKey=%s")
        void gettingInServiceAsset(CIFAssetKey cifAssetKey);

        @Log(level = DEBUG, format = "Getting in service asset with cifAssetKey=%s cacheHit=%s stopwatch=%s")
        void gettingInServiceAsset(CIFAssetKey cifAssetKey, boolean cacheHit, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Getting root asset with lineItemId=%s extensions=%s")
        void getRootAsset(String lineItemId, List<CIFAssetExtension> extensions);

        @Log(level = DEBUG, format = "Getting root asset with lineItemId=%s extensions=%s cacheHit=%s stopwatch=%s")
        void getRootAsset(String lineItemId, List<CIFAssetExtension> extensions, boolean cacheHit, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Saving asset with assetKey=%s")
        void savingAsset(AssetKey assetKey);

        @Log(level = DEBUG, format = "Saving asset with assetKey=%s stopwatch=%s")
        void savingAsset(AssetKey assetKey, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Checking is root asset with cifAssetKey=%s")
        void isRootAsset(AssetKey cifAssetKey);

        @Log(level = DEBUG, format = "Checking is root asset with cifAssetKey=%s stopwatch=%s")
        void isRootAsset(AssetKey cifAssetKey, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Fetching assets with customerId=%s contractId=%s productCodes=%s attributeName=%s attributeValue=%s extensions=%s")
        void getAssets(String customerId, String contractId, List<String> productCodes, String attributeName, String attributeValue, List<CIFAssetExtension> extensions);

        @Log(level = DEBUG, format = "Fetching assets with customerId=%s contractId=%s productCodes=%s attributeName=%s attributeValue=%s extensions=%s stopwatch=%s")
        void getAssets(String customerId, String contractId, List<String> productCodes, String attributeName, String attributeValue, List<CIFAssetExtension> extensions, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Fetching assets with customerId=%s contractId=%s productCode=%s extensions=%s")
        void getAssets(String customerId, String contractId, String productCode, List<CIFAssetExtension> extensions);

        @Log(level = DEBUG, format = "Fetching assets with customerId=%s contractId=%s productCode=%s extensions=%s stopwatch=%s numberOfAssetsFetched=%s")
        void getAssets(String customerId, String contractId, String productCode, List<CIFAssetExtension> extensions, StopWatch stopwatch, int numberOfAssets);

        @Log(level = DEBUG, format = "Create and relate asset with assetKey=%s relationshipName=%s productCode=%s stencilCode=%s lineItemId=%s contractTerm=%s customerId=%s contractId=%s projectId=%s quoteOptionId=%s alternateCity=%s")
        void createAndRelateAsset(AssetKey assetKey, String relationshipName, String productCode, String stencilCode, String lineItemId, String contractTerm, String customerId, String contractId, String projectId, String quoteOptionId, String alternateCity);

        @Log(level = DEBUG, format = "Create and relate asset with assetKey=%s relationshipName=%s productCode=%s stencilCode=%s lineItemId=%s contractTerm=%s customerId=%s contractId=%s projectId=%s quoteOptionId=%s alternateCity=%s stopwatch=%s")
        void createAndRelateAsset(AssetKey assetKey, String relationshipName, String productCode, String stencilCode, String lineItemId, String contractTerm, String customerId, String contractId, String projectId, String quoteOptionId, String alternateCity, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Relate asset with assetKey=%s relatedAssetKey=%s relationshipName=%s")
        void relateAssets(AssetKey assetKey, AssetKey relatedAssetKey, String relationshipName);

        @Log(level = DEBUG, format = "Relate asset with assetKey=%s relatedAssetKey=%s relationshipName=%s stopwatch=%s")
        void relateAssets(AssetKey assetKey, AssetKey relatedAssetKey, String relationshipName, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Create Line Item Lock Version for lineItemId=%s")
        void createLineItemLockVersion(String lineItemId);

        @Log(level = DEBUG, format = "Create Line Item Lock Version for lineItemId=%s stopwatch=%s")
        void createLineItemLockVersion(String lineItemId, StopWatch stopwatch);

        @Log(level = DEBUG, format = "Cancel asset tree with assetKey=%s")
        void cancelAssetTree(AssetKey assetKey);

        @Log(level = DEBUG, format = "Cancel asset tree with assetKey=%s stopwatch=%s")
        void cancelAssetTree(AssetKey assetKey, StopWatch stopwatch);

        @Log(level = DEBUG, format = "DB call time to fetch assets with customerId=%s contractId=%s productCode=%s extensions=%s stopwatch=%s numberOfAssetsFetched=%s")
        void eligibleExistingCandidatesDbCall(String customerId, String contractId, String productCode, List<CIFAssetExtension> extensions, StopWatch dbCallStopWatch, int assets);

        @Log(level = DEBUG, format = "Asset Extension call time to extend asset for assetKey=%s extensions=%s stopwatch=%s")
        void eligibleExistingCandidatesAssetExtension(AssetKey assetKey, List<CIFAssetExtension> extensions, StopWatch getAssetsAssetExtensionWatch);
    }

    public static class OwnerAssetKey {

        private CIFAssetKey cifAssetKey;
        private List<AssetVersionStatus> allowedStatus;
        private final Optional<String> productCodeOptional;
        private final Optional<String> quoteOptionIdOptional;

        public OwnerAssetKey(CIFAssetKey cifAssetKey, List<AssetVersionStatus> allowedStatus, Optional<String> productCodeOptional, Optional<String> quoteOptionIdOptional) {
            this.cifAssetKey = cifAssetKey;
            this.allowedStatus = allowedStatus;
            this.productCodeOptional = productCodeOptional;
            this.quoteOptionIdOptional = quoteOptionIdOptional;
        }

        public CIFAssetKey getCifAssetKey() {
            return cifAssetKey;
        }

        public List<AssetVersionStatus> getAllowedStatus() {
            return allowedStatus;
        }

        public Optional<String> getProductCodeOptional() {
            return productCodeOptional;
        }

        public Optional<String> getQuoteOptionIdOptional() {
            return quoteOptionIdOptional;
        }

        @Override
        public boolean equals(Object that) {
            return EqualsBuilder.reflectionEquals(this, that);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}