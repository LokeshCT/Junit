package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.client.domain.updates.QuoteOptionContext;
import com.bt.rsqe.customerinventory.service.updates.ContributesToChangeRequestBuilder;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.DetailedAssetKey;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.DirectAssociation;
import com.bt.rsqe.domain.product.LocalAssociation;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import org.junit.After;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Set;

import static com.bt.rsqe.domain.product.Association.AssociationType.*;
import static com.bt.rsqe.domain.product.extensions.ExpressionExpectedResultType.Boolean;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AssociatedAssetKeyProviderTest {

    private JPAPersistenceManager persistenceManager = mock(JPAPersistenceManager.class);
    private AssociatedAssetKeyProvider associatedAssetKeyProvider = new AssociatedAssetKeyProvider(persistenceManager);

    @Test
    public void shouldReturnAssociatedAssetKeysForGivenAssetAttributeDirectAssociation() {

        //Given
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);

        Association association = new DirectAssociation("anAssociatedAttribute", ATTRIBUTE_SOURCE, new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Child"), new Expression("1.0", Boolean));

        when(persistenceManager.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(newArrayList(new Object[]{new Object[]{"anAssociatedAssetId", new BigDecimal("1"), "anAssociatedQuoteOptionId"}}));

        //When
        Set<DetailedAssetKey> assetKeys = associatedAssetKeyProvider.getKeys(new ContributesToChangeRequestBuilder.AssociatedAssetKey(new AssetKey("anAssetId", 1L), association));

        //Then

        assertThat(assetKeys.size(), is(1));
        assertThat(assetKeys, hasItem(new DetailedAssetKey("anAssociatedAssetId", 1L, "anAssociatedQuoteOptionId")));
    }

    @Test
    public void shouldReturnCurrentAssetKeyForGivenAssetAttributeLocalAssociation() {

        //Given
        QuoteOptionContext.set("anAssociatedQuoteOptionId");
        Association association = new LocalAssociation("anAssociatedAttribute", ATTRIBUTE_SOURCE);

        //When
        Set<DetailedAssetKey> assetKeys = associatedAssetKeyProvider.getKeys(new ContributesToChangeRequestBuilder.AssociatedAssetKey(new AssetKey("anAssetId", 1L), association));

        //Then
        assertThat(assetKeys.size(), is(1));
        assertThat(assetKeys, hasItem(new DetailedAssetKey("anAssetId", 1L, "anAssociatedQuoteOptionId")));
    }

    @Test
    public void shouldReturnAssociatedAssetKeysForGivenAssetAttributeDirectAssociationFromCache() {

        //Given
        CacheAwareTransaction.set(true);
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);

        Association association = new DirectAssociation("anAssociatedAttribute", ATTRIBUTE_SOURCE, new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Child"), new Expression("1.0", Boolean));

        when(persistenceManager.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(newArrayList(new Object[]{new Object[]{"anAssociatedAssetId", new BigDecimal("1"), "aQuoteOptionId"}}));

        //When
        Set<DetailedAssetKey> assetKeys = associatedAssetKeyProvider.getKeys(new ContributesToChangeRequestBuilder.AssociatedAssetKey(new AssetKey("anAssetId", 1L), association));
        assertThat(assetKeys.size(), is(1));
        assertThat(assetKeys, hasItem(new DetailedAssetKey("anAssociatedAssetId", 1L, "aQuoteOptionId")));

        assetKeys = associatedAssetKeyProvider.getKeys(new ContributesToChangeRequestBuilder.AssociatedAssetKey(new AssetKey("anAssetId", 1L), association));
        assertThat(assetKeys.size(), is(1));
        assertThat(assetKeys, hasItem(new DetailedAssetKey("anAssociatedAssetId", 1L, "aQuoteOptionId")));

        verify(persistenceManager, times(1)).entityManager();
    }

    @After
    public void after() {
        AssetCacheManager.clearAllCaches();
        QuoteOptionContext.remove();
    }

}