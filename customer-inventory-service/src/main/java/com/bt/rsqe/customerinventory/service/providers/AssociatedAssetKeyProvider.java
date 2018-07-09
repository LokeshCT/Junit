package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.repository.jpa.AssociatedAssetQueryBuilder;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.client.domain.updates.QuoteOptionContext;
import com.bt.rsqe.customerinventory.service.updates.ContributesToChangeRequestBuilder;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.DetailedAssetKey;
import com.bt.rsqe.domain.product.DirectAssociation;
import com.bt.rsqe.domain.product.constraints.AssociatedRootAsset;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Set;

import static com.google.common.collect.Sets.*;
import static org.apache.commons.lang.StringUtils.*;

public class AssociatedAssetKeyProvider {
    private JPAPersistenceManager jpaPersistenceManager;

    public AssociatedAssetKeyProvider(JPAPersistenceManager jpaPersistenceManager) {
        this.jpaPersistenceManager = jpaPersistenceManager;
    }

    public Set<DetailedAssetKey> getKeys(ContributesToChangeRequestBuilder.AssociatedAssetKey associatedAssetKey) {

        Set<DetailedAssetKey> associatedAssetKeys = AssetCacheManager.getAssociatedAssetKeys(associatedAssetKey, this);
        if(associatedAssetKeys != null) {
            return associatedAssetKeys;
        }

        return getAssetKeys(associatedAssetKey);
    }

    public Set<DetailedAssetKey> getAssetKeys(ContributesToChangeRequestBuilder.AssociatedAssetKey associatedAssetKey) {
        if (associatedAssetKey.getAssociation() instanceof DirectAssociation) {
            DirectAssociation directAssociation = (DirectAssociation) associatedAssetKey.getAssociation();
            final String associatedLink = directAssociation.getContributedToOffering().getProductId() + "." + join(directAssociation.getRelationshipFromContributedToOffering(), ".");
            AssociatedRootAsset associatedRootAsset = new AssociatedRootAsset(associatedLink, EMPTY);
            return associatedAssetKeys(associatedAssetKey.getAssetKey(), associatedRootAsset);
        } else {
            return newHashSet(new DetailedAssetKey(associatedAssetKey.getAssetKey(), QuoteOptionContext.get()));
        }
    }

    @SuppressWarnings("unchecked")
    private Set<DetailedAssetKey> associatedAssetKeys(AssetKey sourceKey, AssociatedRootAsset associatedRootAsset) {

        String queryString = new AssociatedAssetQueryBuilder()
                .withAssociatedProduct(associatedRootAsset.getAssociatedSCode())
                .withAssociatedRelationships(associatedRootAsset.getAssociatedPath())
                .withAssociatedLocalRelationships(associatedRootAsset.getLocalLinkList())
                .buildForAssetKeys();

        Query valueQuery = jpaPersistenceManager.entityManager().createNativeQuery(queryString).setParameter("rootAssetId", sourceKey.getAssetId())
                .setParameter("rootAssetVersion", sourceKey.getAssetVersion());

        return newHashSet(Iterables.transform(valueQuery.getResultList(), new Function<Object, AssetKey>() {
            @Override
            public AssetKey apply(Object input) {
                Object[] columns = (Object[]) input;
                String assetId = (String) columns[0];
                BigDecimal assetVersion = (BigDecimal) (columns[1]);
                String quoteOptionId = (String) columns[2];
                return new DetailedAssetKey(assetId, Long.parseLong(assetVersion.toString()), quoteOptionId);
            }
        }));

    }
}
