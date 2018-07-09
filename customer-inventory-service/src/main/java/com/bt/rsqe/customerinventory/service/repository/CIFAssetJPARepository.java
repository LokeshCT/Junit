package com.bt.rsqe.customerinventory.service.repository;

import com.bt.rsqe.bfgfacade.constants.BfgConstants;
import com.bt.rsqe.customerinventory.parameter.LineItemLockVersion;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.repository.StaleAssetException;
import com.bt.rsqe.customerinventory.repository.jpa.entities.AbstractAssetEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetRelationshipEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.LineItemLockVersionEntity;
import com.bt.rsqe.customerinventory.repository.jpa.keys.FutureAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.entities.CIFAssetTransformer;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.AssetProcessType;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.bfgfacade.constants.BfgConstants.*;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository.QueryParameter.*;
import static com.bt.rsqe.enums.AssetVersionStatus.*;
import static com.bt.rsqe.logging.LogLevel.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

public class CIFAssetJPARepository {
    private static final String ALLOWED_STATUS = "allowedStatus";
    private static final String PRODUCT_CODE = "productCode";
    private static final String CONTRACT_ID = "contractId";
    public static final ArrayList<AssetVersionStatus> PROVISIONED_ASSET_STATUS = newArrayList(AssetVersionStatus.IN_SERVICE, AssetVersionStatus.PROVISIONING);
    public static final String ASSET_VERSION_STATUS = "assetVersionStatus";
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private JPAPersistenceManager persistence;
    private ExternalAssetReader externalAssetReader;

    private static final String ASSET_ID = "assetId";
    private static final String ASSET_VERSION = "assetVersion";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CUSTOMER_ID = "customerId";
    private static final String SITE_ID = "siteId";



    public CIFAssetJPARepository(JPAPersistenceManager customerInventoryModelPersistence, ExternalAssetReader externalAssetReader) {
        this.persistence = customerInventoryModelPersistence;
        this.externalAssetReader = externalAssetReader;
    }

    public void saveAsset(CIFAsset cifAsset) {
        AbstractAssetEntity entity = CIFAssetTransformer.toAssetEntity(cifAsset);
        persistence.saveAndFlush(entity);
        if (cifAsset.hasExtension(Relationships)) {
            for (CIFAsset childAsset : cifAsset.getChildren()) {
                saveAsset(childAsset);
            }
        }
    }

    public CIFAsset getRootAsset(String lineItemId, boolean loadRelationships) {
        return getAsset(FUTURE_ROOT_ASSET_BY_LINE_ITEM_ID, loadRelationships, p("lineItemId", lineItemId));
    }

    public CIFAsset getAsset(AssetKey assetKey, boolean loadRelationships) {
        return getAsset(FUTURE_ASSET_BY_ASSET_ID_VERSION, assetKey, loadRelationships);
    }

    public CIFAsset getParentAsset(AssetKey assetKey, boolean loadRelationships) {
        return getAsset(PARENT_FUTURE_ASSET, assetKey, loadRelationships);
    }

    public CIFAsset getParentOrOwnerAsset(AssetKey assetKey, boolean loadRelationships) {
        return getAsset(PARENT_OR_OWNER_FUTURE_ASSET, assetKey, loadRelationships);
    }

    public List<CIFAsset> getOwnerAssets(AssetKey assetKey, Boolean loadRelationships) {
        return getAssets(DEPENDANT_ASSET, assetKey, loadRelationships);
    }

    public List<CIFAsset> getOwnerAssets(AssetKey assetKey, boolean loadRelationships, List<AssetVersionStatus> allowedStatus) {
        return getAssets(DEPENDANT_ASSET_WITH_STATUS, loadRelationships,
                new QueryParameter(ASSET_ID, assetKey.getAssetId()),
                new QueryParameter(ASSET_VERSION, assetKey.getAssetVersion()),
                new QueryParameter(ALLOWED_STATUS, allowedStatus));
    }

    public List<CIFAsset> getOwnerAssets(AssetKey assetKey, boolean loadRelationships, String productCode, String quoteOptionId, List<AssetVersionStatus> allowedStatus) {
        return getAssets(DEPENDANT_ASSET_WITH_STATUS_QUOTEOPTIONID_PRODUCTCODE, loadRelationships,
                new QueryParameter(ASSET_ID, assetKey.getAssetId()),
                new QueryParameter(ASSET_VERSION, assetKey.getAssetVersion()),
                new QueryParameter(QUOTE_OPTION_ID, quoteOptionId),
                new QueryParameter(PRODUCT_CODE, productCode),
                new QueryParameter(ALLOWED_STATUS, allowedStatus));
    }

    public List<CIFAsset> getOwnerAssets(AssetKey assetKey, boolean loadRelationships, String quoteOptionId, List<AssetVersionStatus> allowedStatus) {
        return getAssets(DEPENDANT_ASSET_WITH_STATUS_QUOTEOPTIONID, loadRelationships,
                new QueryParameter(ASSET_ID, assetKey.getAssetId()),
                new QueryParameter(ASSET_VERSION, assetKey.getAssetVersion()),
                new QueryParameter(QUOTE_OPTION_ID, quoteOptionId),
                new QueryParameter(ALLOWED_STATUS, allowedStatus));
    }

    public List<CIFAsset> getAssets(String customerId, String contractId, List<String> productCodes,
                                    String attributeName, String attributeValue, boolean loadRelationships) {
        return getAssets(ASSETS_BY_S_CODE_AND_CHARACTERISTIC,
                loadRelationships,
                new QueryParameter(CUSTOMER_ID , customerId),
                new QueryParameter(CONTRACT_ID, contractId),
                new QueryParameter("attributeName", attributeName),
                new QueryParameter("productCodes", productCodes),
                new QueryParameter("attributeValue", attributeValue));
    }

    public List<CIFAsset> getAssets(String customerId, String contractId, String productCode, boolean loadRelationships) {
        return getAssets(ASSETS_BY_S_CODE,
                loadRelationships,
                new QueryParameter(CUSTOMER_ID , customerId),
                new QueryParameter(CONTRACT_ID, contractId),
                new QueryParameter(PRODUCT_CODE, productCode));
    }


    public List<CIFAsset> getEligibleExistingCandidates(String customerId, String contractId, String productCode, boolean loadRelationships, String quoteOptionId) {
        return getAssets(ELIGIBLE_EXISTING_ASSETS,
                loadRelationships,
                new QueryParameter(CUSTOMER_ID , customerId),
                new QueryParameter(CONTRACT_ID, contractId),
                new QueryParameter(PRODUCT_CODE, productCode),
                new QueryParameter(QUOTE_OPTION_ID, quoteOptionId),
                new QueryParameter(ASSET_VERSION_STATUS, newArrayList(PROVISIONING, IN_SERVICE)));
    }

    public List<CIFAsset> getEligibleExistingCandidates(String customerId, String contractId, String productCode, boolean loadRelationships, String quoteOptionId, String siteId) {
        return getAssets(ELIGIBLE_EXISTING_ASSETS_BY_SITE,
                loadRelationships,
                new QueryParameter(CUSTOMER_ID, customerId),
                new QueryParameter(CONTRACT_ID, contractId),
                new QueryParameter(PRODUCT_CODE, productCode),
                new QueryParameter(QUOTE_OPTION_ID, quoteOptionId),
                new QueryParameter(SITE_ID, siteId),
                new QueryParameter("assetVersionStatus", newArrayList(PROVISIONING, IN_SERVICE)));
    }

    private CIFAsset getAsset(String queryStr, AssetKey assetKey, Boolean loadRelationships) {
        return first(getAssets(queryStr, assetKey, loadRelationships));
    }

    public Optional<CIFAsset> getAsset(AssetKey assetKey) {
        final List<CIFAsset> assets = getAssets(FUTURE_ASSET_BY_ASSET_ID_VERSION, assetKey, true);
        if (assets.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(assets.get(0));
    }

    private CIFAsset getAsset(String queryStr, Boolean loadRelationships, QueryParameter... parameters) {
        return first(getAssets(queryStr, loadRelationships, parameters));
    }

    private List<CIFAsset> getAssets(String queryStr, AssetKey assetKey, Boolean loadRelationships) {
        return getAssets(queryStr,
                loadRelationships,
                p(ASSET_ID, assetKey.getAssetId()),
                p(ASSET_VERSION, assetKey.getAssetVersion()));
    }

    private List<CIFAsset> getAssets(String queryStr, boolean loadRelationships, QueryParameter... parameters) {
        TypedQuery<AbstractAssetEntity> query = persistence.entityManager()
                                                           .createQuery(queryStr, AbstractAssetEntity.class);

        for (QueryParameter parameter : parameters) {
            query.setParameter(parameter.name, parameter.value);
        }

        List<CIFAsset> cifAssets = newArrayList();

        for (AbstractAssetEntity abstractAssetEntity : query.getResultList()) {
            cifAssets.add(CIFAssetTransformer.fromAssetEntity(abstractAssetEntity, loadRelationships, externalAssetReader));
        }

        return cifAssets;
    }

    public Optional<CIFAsset> getInServiceAsset(AssetKey assetKey, boolean loadRelationships) {
        TypedQuery<AbstractAssetEntity> query = persistence.entityManager()
                                                           .createQuery(IN_SERVICE_FUTURE_ASSET_BY_ASSET_ID,
                                                                   AbstractAssetEntity.class)
                                                           .setParameter(ASSET_ID, assetKey.getAssetId());

        final List<AbstractAssetEntity> resultList = query.getResultList();
        if (resultList.size() > 0) {
            return Optional.of(CIFAssetTransformer.fromAssetEntity(resultList.get(0), loadRelationships, externalAssetReader));
        } else {
            return Optional.absent();
        }
    }

    public boolean hasInServiceAsset(AssetKey assetKey) {
        TypedQuery<AbstractAssetEntity> query = persistence.entityManager()
                                                           .createQuery(IN_SERVICE_FUTURE_ASSET_BY_ASSET_ID,
                                                                        AbstractAssetEntity.class)
                                                           .setParameter(ASSET_ID, assetKey.getAssetId());

        final List<AbstractAssetEntity> resultList = query.getResultList();
        return !resultList.isEmpty();
    }

    public boolean hasProvisiongOrInServiceAsset(AssetKey assetKey) {
        TypedQuery<AbstractAssetEntity> query = persistence.entityManager()
                .createQuery(PROVISIONING_IN_SERVICE_FUTURE_ASSET_BY_ASSET_ID,
                        AbstractAssetEntity.class)
                .setParameter(ASSET_ID, assetKey.getAssetId());

        final List<AbstractAssetEntity> resultList = query.getResultList();
        return !resultList.isEmpty();
    }

    public List<CIFAsset> getAssetDetails(String customerId, boolean loadRelationships) {
        return getAssets(FUTURE_ROOT_ASSET_BY_CUSTOMER_ITEM_ID, loadRelationships, p(CUSTOMER_ID , customerId));
    }

    private static final String FUTURE_ROOT_ASSET_BY_CUSTOMER_ITEM_ID =
        "SELECT futureAsset " +
        "FROM AbstractAssetEntity futureAsset " +
        "WHERE futureAsset.details.customerId=:customerId and futureAsset.details.assetVersionStatus='" + AssetVersionStatus.IN_SERVICE + "' and futureAsset.details.assetType='" + AssetType.REAL + "' ";



    public CIFAsset getRelationships(CIFAsset asset) {
        if (!asset.hasExtension(Relationships)) {
            final CIFAsset assetWithRelationships = getAsset(asset.getAssetKey(), true);
            asset.loadRelationships(assetWithRelationships.getRelationships());
        }
        return asset;
    }

    public int getLockVersion(String lineItemId) {
        LineItemLockVersionEntity lockVersion = persistence.get(LineItemLockVersionEntity.class, lineItemId);
        if (lockVersion != null) {
            return lockVersion.getLockVersion();
        }
        return LineItemLockVersion.IMMUTABLE.value();
    }


    // This is protected for now to facilitate test but should be made private once the asset save is interacting with
    // it (asset save should be the only thing interacting with it).
    public void saveLineItemLockVersion(String lineItemId, int lockVersion) throws StaleAssetException {
        try {
            persistence.saveAndFlush(new LineItemLockVersionEntity(lineItemId, lockVersion));
        } catch (OptimisticLockException ole) {
            throw new StaleAssetException(lineItemId, lockVersion, getLockVersion(lineItemId));
        }
    }

    private static final String FUTURE_ASSET_BY_ASSET_ID_VERSION =
        "SELECT futureAsset " +
        "FROM AbstractAssetEntity futureAsset " +
        "WHERE futureAsset.key.key.assetId=:assetId " +
        "AND futureAsset.key.key.assetVersion=:assetVersion ";

    private static final String IN_SERVICE_FUTURE_ASSET_BY_ASSET_ID =
        "SELECT futureAsset " +
        "FROM AbstractAssetEntity futureAsset " +
        "WHERE futureAsset.key.key.assetId=:assetId " +
        "AND futureAsset.details.assetVersionStatus='" + AssetVersionStatus.IN_SERVICE + "'";

    private static final String PARENT_FUTURE_ASSET =
        "SELECT relationship.key.owner " +
        "FROM FutureAssetRelationshipEntity relationship " +
        "WHERE relationship.key.related.key.key.assetId=:assetId AND relationship.key.related.key.key.assetVersion=:assetVersion AND relationship.relationshipType = 'Child'";

    private static final String PARENT_OR_OWNER_FUTURE_ASSET =
            "SELECT relationship.key.owner " +
            "FROM FutureAssetRelationshipEntity relationship " +
            "WHERE relationship.key.related.key.key.assetId=:assetId AND relationship.key.related.key.key.assetVersion=:assetVersion AND relationship.relationshipType in ('Child', 'RelatedTo')";

    private static final String ASSETS_BY_S_CODE_AND_CHARACTERISTIC =
        "SELECT futureAsset " +
        "FROM AbstractAssetEntity futureAsset " +
        "WHERE futureAsset.details.customerId=:customerId " +
        "AND futureAsset.details.contractId=:contractId " +
        "AND futureAsset.details.productCode in (:productCodes)" +
        "AND futureAsset.key.key.assetId in " +
        "(SELECT futureAssetCharacteristic.key.key.key.assetId " +
        "FROM FutureAssetCharacteristicEntity futureAssetCharacteristic " +
        "WHERE name=:attributeName AND value=:attributeValue)";

    private static final String ASSETS_BY_S_CODE =
        "SELECT futureAsset " +
        "FROM AbstractAssetEntity futureAsset " +
        "WHERE futureAsset.details.customerId=:customerId " +
        "AND futureAsset.details.contractId=:contractId " +
        "AND futureAsset.details.productCode=:productCode";

    private static final String ELIGIBLE_EXISTING_ASSETS =
            "SELECT futureAsset " +
            "FROM AbstractAssetEntity futureAsset " +
            "WHERE futureAsset.details.customerId=:customerId " +
            "AND futureAsset.details.contractId=:contractId " +
            "AND futureAsset.details.productCode=:productCode " +
            "AND futureAsset.details.assetType != 'STUB' " +
            "AND (futureAsset.details.quoteOptionId=:quoteOptionId OR futureAsset.details.assetVersionStatus in (:assetVersionStatus))";

    private static final String ELIGIBLE_EXISTING_ASSETS_BY_SITE =
            "SELECT futureAsset " +
            "FROM AbstractAssetEntity futureAsset " +
            "WHERE futureAsset.details.customerId=:customerId " +
            "AND futureAsset.details.contractId=:contractId " +
            "AND futureAsset.details.productCode=:productCode " +
            "AND futureAsset.details.assetType != 'STUB' " +
            "AND futureAsset.details.siteId=:siteId " +
            "AND (futureAsset.details.quoteOptionId=:quoteOptionId OR futureAsset.details.assetVersionStatus in (:assetVersionStatus))";


    private static final String DEPENDANT_ASSET =
        "SELECT relationship.key.owner " +
        "FROM FutureAssetRelationshipEntity relationship " +
        "WHERE relationship.key.related.key.key.assetId=:assetId AND relationship.key.related.key.key.assetVersion=:assetVersion ";

    private static final String DEPENDANT_ASSET_WITH_STATUS =
        "SELECT relationship.key.owner " +
        "FROM FutureAssetRelationshipEntity relationship " +
        "WHERE relationship.key.related.key.key.assetId=:assetId AND relationship.key.related.key.key.assetVersion=:assetVersion " +
        "AND relationship.key.owner.details.assetVersionStatus in (:allowedStatus) ";

    private static final String DEPENDANT_ASSET_WITH_STATUS_QUOTEOPTIONID =
            "SELECT relationship.key.owner " +
                    "FROM FutureAssetRelationshipEntity relationship " +
                    "WHERE relationship.key.related.key.key.assetId=:assetId AND relationship.key.related.key.key.assetVersion=:assetVersion " +
                    "AND (relationship.key.owner.details.quoteOptionId=:quoteOptionId OR relationship.key.owner.details.assetVersionStatus in (:allowedStatus))";

    private static final String DEPENDANT_ASSET_WITH_STATUS_QUOTEOPTIONID_PRODUCTCODE =
            "SELECT relationship.key.owner " +
                    "FROM FutureAssetRelationshipEntity relationship " +
                    "WHERE relationship.key.related.key.key.assetId=:assetId AND relationship.key.related.key.key.assetVersion=:assetVersion " +
                    "AND relationship.key.owner.details.productCode=:productCode " +
                    "AND (relationship.key.owner.details.quoteOptionId=:quoteOptionId OR relationship.key.owner.details.assetVersionStatus in (:allowedStatus))";

    private static final String FUTURE_ROOT_ASSET_BY_LINE_ITEM_ID =
        "SELECT futureAsset " +
        "FROM AbstractAssetEntity futureAsset " +
        "WHERE futureAsset.details.lineItemId=:lineItemId " +
        "AND futureAsset.details.status<>'REMOVED' " +
        "AND futureAsset.key.key.assetId not in " +
        "(" +
        "SELECT relationship.key.related.key.key.assetId " +
        "FROM FutureAssetRelationshipEntity relationship " +
        "WHERE relationship.key.owner.details.lineItemId=:lineItemId ) ";

    private static final String RELATIONSHIPS =
        "SELECT relationship " +
        "FROM FutureAssetRelationshipEntity relationship " +
        "WHERE relationship.key.related.key.key.assetId=:relatedAssetId AND relationship.key.related.key.key.assetVersion=:relatedAssetVersion ";

    private static final String LIVE_RELATED_TO_RELATIONSHIPS =
        "SELECT futureAsset " +
        "FROM FutureAssetRelationshipEntity relationship,  AbstractAssetEntity futureAsset " +
        "WHERE relationship.key.related.key.key.assetId=:relatedAssetId AND relationship.key.related.key.key.assetVersion=:relatedAssetVersion " +
        "AND relationship.relationshipType = 'RelatedTo' AND relationship.key.owner.key.key.assetId = futureAsset.key.key.assetId " +
        "AND relationship.key.owner.key.key.assetVersion = futureAsset.key.key.assetVersion AND relationship.key.owner.details.assetVersionStatus <> 'OBSOLETE' " +
        "AND relationship.key.owner.details.status = 'LIVE'";

    private static final String BEND_SITEIDS_IN_USE = "select value from future_asset_characteristic where name =:bEndSiteCharName " +
            "and asset_id in (select a.related_asset_id from future_asset_relationship a inner join future_asset_detail b " +
            "on a.owner_asset_id = b.asset_id and a.owner_line_item_id = b.line_item_id and a.relationship_name=:relationShipName and b.quote_option_id in " +
            "(select quote_option_id from future_asset_detail where asset_id=:ownerAssetId) " +
            "and a.owner_asset_id in (select asset_id from future_asset_characteristic where name='STENCIL' and value=:ownerStencilId) )";

    private static final String AEND_SITEID_BY_BEND_SITE_ID = "select value from future_asset_characteristic where name =:aEndSiteCharName and asset_id in " +
            "(select asset_id from future_asset_characteristic where name =:bEndSiteCharName and value=:bEndSiteId " +
            "and asset_id in (select a.related_asset_id from future_asset_relationship a inner join future_asset_detail b " +
            "on a.owner_asset_id = b.asset_id and a.owner_line_item_id = b.line_item_id and a.relationship_name=:relationShipName and b.quote_option_id in " +
            "(select quote_option_id from future_asset_detail where asset_id=:ownerAssetId) " +
            "and a.owner_asset_id in (select asset_id from future_asset_characteristic where name='STENCIL' and value=:ownerStencilId)))";

    private CIFAsset first(List<CIFAsset> assets) {
        if (assets.isEmpty()) {
            throw new NoResultException();
        }
        return assets.get(0);
    }

    public boolean isRootAsset(AssetKey assetKey) {
        Query query = persistence.entityManager().createNativeQuery("select owner_asset_id from future_asset_relationship " +
                "where related_asset_id =:assetId and related_asset_version =:asset_version " +
                "and relationship_type = 'Child'")
                                 .setParameter(ASSET_ID, assetKey.getAssetId())
                                 .setParameter("asset_version", assetKey.getAssetVersion());

        return query.getResultList().isEmpty();
    }

    public List<String> getBEndSiteIds(String ownerAssetId, String relationshipName, String bEndSiteCharName, String ownerStencilId) {
        Query query = persistence.entityManager()
                .createNativeQuery(BEND_SITEIDS_IN_USE)
                .setParameter("ownerAssetId", ownerAssetId)
                .setParameter("relationShipName", relationshipName)
                .setParameter("bEndSiteCharName", bEndSiteCharName)
                .setParameter("ownerStencilId", ownerStencilId);
        return query.getResultList();
    }

    public List<String> getAendSiteIdByBEndSiteId(String ownerAssetId, String relationshipName, String bEndSiteCharName, String aEndSiteCharName, String ownerStencilId, String bEndSiteId) {
        Query query = persistence.entityManager()
                .createNativeQuery(AEND_SITEID_BY_BEND_SITE_ID)
                .setParameter("ownerAssetId", ownerAssetId)
                .setParameter("relationShipName", relationshipName)
                .setParameter("bEndSiteCharName", bEndSiteCharName)
                .setParameter("aEndSiteCharName", aEndSiteCharName)
                .setParameter("ownerStencilId", ownerStencilId)
                .setParameter("bEndSiteId", bEndSiteId);
        return query.getResultList();
    }

    public Set<CancelRelationshipRequest> cancelAssetTree(AssetKey owningAssetKey, String relationshipName, CIFAsset cifAsset) {
        logger.cancelAssetTree(cifAsset.getAssetKey()) ;
        Set<CancelRelationshipRequest> cancelRelationshipRequests = newHashSet();
        for (CIFAssetRelationship child : cifAsset.getChildRelationships()) {
            cancelRelationshipRequests.addAll(cancelAssetTree(cifAsset.getAssetKey(), child.getRelationshipName(), child.getRelated()));
        }
        cancelRelationshipRequests.addAll(cancelAsset(owningAssetKey, relationshipName, cifAsset));
        return cancelRelationshipRequests;
    }

    private Set<CancelRelationshipRequest> cancelAsset(AssetKey owningAssetKey, String relationshipName, CIFAsset cifAsset) {
        AssetKey assetKey = cifAsset.getAssetKey() ;
        logger.cancelAsset(assetKey) ;
        AbstractAssetEntity assetEntity = getAssetEntity(assetKey);

        if(PROVISIONED_ASSET_STATUS.contains(assetEntity.getAssetVersionStatus())) {
            AbstractAssetEntity ownerEntity = getAssetEntity(owningAssetKey);
            if(ownerEntity != null) {
                ownerEntity.removeRelationshipTo(assetEntity, RelationshipName.newInstance(relationshipName));
                persistence.saveAndFlush(ownerEntity);
            }
            return Collections.emptySet();
        }

        Set<CancelRelationshipRequest> cancelRelationshipRequests = constructCancelRequestForRelatedToAssets(assetEntity);
        cancelAllRelationshipsPointsTo(cifAsset);

        // Couple of options here
        // remove the asset completely if this is a provide order
        // Or just mark it's status as Ceased if this is a Modify or Cease order
        // or Cancelled if it is a cancel order

        // TODO how to work out we are a cancel at this point
        // cifAsset.setStatus(ProductInstanceState.CANCELLED);

        LineItemAction action = getLineItemAction(cifAsset);
        boolean ifc = isIfc(cifAsset);
        logger.typeOfCancelAssetOperation(action, ifc) ;
        switch (action) {
            case PROVIDE:
                assetEntity = getAssetEntity(assetKey);
                persistence.remove(assetEntity);
                break;
            case MODIFY:
            case CEASE:
                cifAsset.setStatus(ProductInstanceState.CEASED);
                cifAsset.setAssetProcessType(AssetProcessType.NOT_APPLICABLE) ;
                cifAsset.setAssetSubProcessType(AssetProcessType.NOT_APPLICABLE) ;
                cifAsset.setContractResignStatus(Constants.NO);
                saveAsset(cifAsset);
                break;
        }

        return cancelRelationshipRequests;
    }

    private boolean isIfc(CIFAsset cifAsset) {
        return cifAsset.getQuoteOptionItemDetail().isIfc();
    }

    private LineItemAction getLineItemAction(CIFAsset cifAsset) {
        if(hasInServiceAsset(cifAsset.getAssetKey())) {
            return LineItemAction.fromDescription(cifAsset.getQuoteOptionItemDetail().getLineItemAction());
        }
        return LineItemAction.PROVIDE;
    }

    private Set<CancelRelationshipRequest> constructCancelRequestForRelatedToAssets(AbstractAssetEntity assetEntity) {
        Set<CancelRelationshipRequest> cancelRelationshipRequests = newHashSet();
        final List<FutureAssetRelationshipEntity> relatedToRelations = assetEntity.getRelatedToRelations();
        for (FutureAssetRelationshipEntity relatedToRelation : relatedToRelations) {
            AbstractAssetEntity related = relatedToRelation.getRelated();
            String relatedAssetId = related.getKey().getAssetId();
            final Long relatedAssetVersion = related.getKey().getAssetVersion();

            if (!hasInServiceAsset(AssetKey.newInstance(relatedAssetId, relatedAssetVersion))) {
                List<AbstractAssetEntity> ownersWithRelatedTo = getValidOwnersWithRelatedTo(assetEntity, related.getKey());
                if (ownersWithRelatedTo.isEmpty()) {
                    cancelRelationshipRequests.add(new CancelRelationshipRequest(new AssetKey(assetEntity.getKey().getAssetId(), assetEntity.getKey().getAssetVersion()),
                                                                                 related.getDetails().getLineItemId(),
                                                                                 0,
                                                                                 new AssetKey(relatedAssetId, relatedAssetVersion),
                                                                                 relatedToRelation.getRelationshipName(),
                                                                                 related.getDetails().getProductCode(), true));

                }
            }
        }
        return cancelRelationshipRequests;
    }

    private AbstractAssetEntity getAssetEntity(AssetKey assetKey) {
        TypedQuery<AbstractAssetEntity> query = persistence.entityManager()
                                                           .createQuery(FUTURE_ASSET_BY_ASSET_ID_VERSION, AbstractAssetEntity.class)
                                                           .setParameter(ASSET_ID, assetKey.getAssetId())
                                                           .setParameter(ASSET_VERSION, assetKey.getAssetVersion());

        return query.getSingleResult();
    }

    private void cancelAllRelationshipsPointsTo(CIFAsset cifAsset) {
        AssetKey assetEntityKey = cifAsset.getAssetKey();
        logger.cancelAllRelationshipsPointsTo(assetEntityKey) ;
        List<FutureAssetRelationshipEntity> resultList = getOwnersOf(assetEntityKey.getAssetId(), assetEntityKey.getAssetVersion());
        if (!resultList.isEmpty()) {

            List<AbstractAssetEntity> saveList = newArrayList () ;
            for (FutureAssetRelationshipEntity relationshipEntity : resultList)
            {
                logger.removeRelationshipTo(relationshipEntity) ;

                LineItemAction action = getLineItemAction(cifAsset) ;
                boolean ifc = isIfc(cifAsset);
                logger.typeOfCancelAllRelationshipsPointsTo(action, ifc);

                switch (action)
                {
                    case PROVIDE:
                        relationshipEntity.getOwner().removeRelationshipTo(relationshipEntity.getRelated(), relationshipEntity.getRelationshipType());
                        break;
                    case MODIFY:
                    case CEASE:
                        relationshipEntity.getOwner().setRelationshipToStatus(relationshipEntity.getRelated(), relationshipEntity.getRelationshipType(), ProductInstanceState.CEASED.name());
                        break;
                }
                saveList.add(relationshipEntity.getOwner()) ;
            }

            for (AbstractAssetEntity asset : saveList)
            {
                persistence.saveAndFlush(asset);
            }
        }
    }

    private List<FutureAssetRelationshipEntity> getOwnersOf(String assetId, Long assetVersion) {
        TypedQuery<FutureAssetRelationshipEntity> query = persistence.entityManager()
                                                                     .createQuery(RELATIONSHIPS, FutureAssetRelationshipEntity.class)
                                                                     .setParameter("relatedAssetId", assetId)
                                                                     .setParameter("relatedAssetVersion", assetVersion);
        return query.getResultList();
    }

    private List<AbstractAssetEntity> getValidOwnersWithRelatedTo(final AbstractAssetEntity cancelingAsset, FutureAssetKey relatedAssetKey) {
        final List<AbstractAssetEntity> ownersWithRelatedTo = getOwnersWithRelatedTo(relatedAssetKey.getAssetId(), relatedAssetKey.getAssetVersion());
        return newArrayList(Iterables.filter(ownersWithRelatedTo, new Predicate<AbstractAssetEntity>() {
            @Override
            public boolean apply(AbstractAssetEntity input) {
                boolean isOwnerADraftAsset = input.getDetails().getAssetVersionStatus().equals(DRAFT);
                final boolean isCancelingAndOwnerInSameOption = cancelingAsset.getDetails().getQuoteOptionId().equals(input.getDetails().getQuoteOptionId());
                return !input.getKey().equals(cancelingAsset.getKey()) && (!isOwnerADraftAsset || isCancelingAndOwnerInSameOption);
            }
        }));
    }

    private List<AbstractAssetEntity> getOwnersWithRelatedTo(String assetId, Long assetVersion) {
        TypedQuery<AbstractAssetEntity> query = persistence.entityManager()
                                                           .createQuery(LIVE_RELATED_TO_RELATIONSHIPS, AbstractAssetEntity.class)
                                                           .setParameter("relatedAssetId", assetId)
                                                           .setParameter("relatedAssetVersion", assetVersion);
        return query.getResultList();
    }

    public boolean isMigratedCustomer(CIFAssetOrchestrator.MigratedCustomerKey migratedCustomerKey) {

        String sqlString = String.format("select fad.asset_id from future_asset_detail fad, future_asset_characteristic fac " +
                "where fad.asset_id = fac.asset_id and fad.asset_version = fac.asset_version and fad.customer_id =:%s " +
                "and fad.contract_id =:%s and fad.product_code in (:%s) and fac.name =:%s and fac.value = 'Yes'", CUSTOMER_ID, BfgConstants.CONTRACT_ID, BfgConstants.PRODUCT_CODE, LEGACY_BILLING);

        List resultList = persistence.entityManager().createNativeQuery(sqlString)
                .setParameter(CUSTOMER_ID, migratedCustomerKey.getCustomerId())
                .setParameter(BfgConstants.CONTRACT_ID, migratedCustomerKey.getContractId())
                .setParameter(BfgConstants.PRODUCT_CODE, migratedCustomerKey.getProductCodes())
                .setParameter(LEGACY_BILLING, ProductOffering.LEGACY_BILLING)
                .getResultList();

        return !resultList.isEmpty();
    }

    public static class QueryParameter {
        public final String name;
        public final Object value;

        private QueryParameter(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public static QueryParameter p(String name, Object value) {
            return new QueryParameter(name, value);
        }
    }


    private static final String ASSETS_BY_QUOTE_OPTION_ID =
        "SELECT futureAsset " +
        "FROM AbstractAssetEntity futureAsset " +
        "WHERE futureAsset.details.quoteOptionId=:quoteOptionId ";

    public String getAssetByQuoteOptionId(String quoteOptionId) {
        TypedQuery<AbstractAssetEntity> query = persistence.entityManager()
                                                           .createQuery(ASSETS_BY_QUOTE_OPTION_ID, AbstractAssetEntity.class)
                                                           .setParameter(QUOTE_OPTION_ID, quoteOptionId);
        if(query.getResultList().size()!=0){
            return query.getResultList().get(0).getDetails().getCustomerId();
        }
        return null;
    }

    public List<FutureAssetRelationshipEntity> getRelationDetails(String assetId, String lineItemId) {
        TypedQuery<FutureAssetRelationshipEntity> query = persistence.entityManager()
                                                                     .createQuery(RELATIONSHIP_TYPE, FutureAssetRelationshipEntity.class)
                                                                     .setParameter("relatedAssetId", assetId)
                                                                     .setParameter("relatedLineItemId", lineItemId);
        /*List<FutureAssetRelationshipEntity> result =   query.getResultList();
        if(isNull(result)){
                return null;
        }   else{*/
                return query.getResultList() ;
        //}

    }


    private static final String RELATIONSHIP_TYPE =
        "SELECT relationship " +
        "FROM FutureAssetRelationshipEntity relationship " +
        "WHERE relationship.key.related.key.key.assetId=:relatedAssetId AND relationship.relatedLineItemId=:relatedLineItemId " ;

    private static final String PROVISIONING_IN_SERVICE_FUTURE_ASSET_BY_ASSET_ID =
            "SELECT futureAsset " +
                    "FROM AbstractAssetEntity futureAsset " +
                    "WHERE futureAsset.key.key.assetId=:assetId " +
                    "AND futureAsset.details.assetVersionStatus in ('PROVISIONING', 'IN_SERVICE')";


    interface Logger {
        @Log(level = DEBUG, format = "assetKey=%s")
        void cancelAssetTree (AssetKey cifAssetKey);

        @Log(level = DEBUG, format = "assetKey=%s")
        void cancelAsset (AssetKey assetKey);

        @Log(level = DEBUG, format = "key=%s")
        void cancelAllRelationshipsPointsTo (AssetKey key);

        @Log(level = DEBUG, format = "relationshipEntity=%s")
        void removeRelationshipTo (FutureAssetRelationshipEntity relationshipEntity);

        @Log(level = DEBUG, format = "action=%s ifc=%s")
        void typeOfCancelAssetOperation(LineItemAction action, boolean ifc);

        @Log(level = DEBUG, format = "action=%s ifc=%s")
        void typeOfCancelAllRelationshipsPointsTo(LineItemAction action, boolean ifc);
    }


}
