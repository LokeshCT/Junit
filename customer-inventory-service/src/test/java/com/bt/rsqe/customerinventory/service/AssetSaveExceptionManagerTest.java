package com.bt.rsqe.customerinventory.service;

import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.externals.QuoteEngineHelper;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AssetSaveExceptionManagerTest {
    @Test
    public void shouldRemoveCreatedItems() {

        CacheAwareTransaction.set(true);
        //Given
        QuoteEngineHelper quoteEngineHelper = mock(QuoteEngineHelper.class);
        AssetSaveExceptionManager assetSaveExceptionManager = new AssetSaveExceptionManager(quoteEngineHelper);

        QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("createdId").build();
        AssetCacheManager.recordCreatedQuoteOptionItem(quoteOptionItemDTO);

        assetSaveExceptionManager.handleQuoteOptionItems("projectId", "quoteOptionId");

        verify(quoteEngineHelper, times(1)).removeQuoteOptionItem("projectId", "quoteOptionId", "createdId");
    }

    @Test
    public void shouldReinstateRemovedItems() {

        CacheAwareTransaction.set(true);
        //Given
        QuoteEngineHelper quoteEngineHelper = mock(QuoteEngineHelper.class);
        AssetSaveExceptionManager assetSaveExceptionManager = new AssetSaveExceptionManager(quoteEngineHelper);

        QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("removedId").build();
        AssetCacheManager.recordRemovedQuoteOptionItem("createdId", quoteOptionItemDTO);

        assetSaveExceptionManager.handleQuoteOptionItems("projectId", "quoteOptionId");

        verify(quoteEngineHelper, times(1)).associateQuoteOptionItem("projectId", "quoteOptionId", quoteOptionItemDTO);
    }

}