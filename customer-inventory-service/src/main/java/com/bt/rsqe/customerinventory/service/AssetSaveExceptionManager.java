package com.bt.rsqe.customerinventory.service;

import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.externals.QuoteEngineHelper;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;

import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.logging.LogLevel.*;

//This class is added to handle line items being created and removed during asset saves. As RSQE is not handling distributed transactions across REST components
//Line items being created and removed are orphaned with/without assets, The logic available in this class will handle till a way is found out to handle global/distributed transactions.
public class AssetSaveExceptionManager {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private QuoteEngineHelper quoteEngineHelper;

    public AssetSaveExceptionManager(QuoteEngineHelper quoteEngineHelper) {
        this.quoteEngineHelper = quoteEngineHelper;
    }

    public void handleQuoteOptionItems(String projectId, String quoteOptionId) {
        //Remove created line items
        Set<QuoteOptionItemDTO> createdQuoteOptionItems = AssetCacheManager.getCreatedQuoteOptionItems();
        logger.createdQuoteOptionItemsGettingRemoved(createdQuoteOptionItems);
        for (QuoteOptionItemDTO createdQuoteOptionItem : createdQuoteOptionItems) {
            try {
                quoteEngineHelper.removeQuoteOptionItem(projectId, quoteOptionId, createdQuoteOptionItem.getId());
            } catch (Exception e) {
                //Carry on
            }
        }

        //Restore Removed Line items
        Map<String, QuoteOptionItemDTO> removedQuoteOptionItems = AssetCacheManager.getRemovedQuoteOptionItems();
        logger.removedQuoteOptionItemsGettingRestored(removedQuoteOptionItems);
        for (Map.Entry<String, QuoteOptionItemDTO> itemDTOEntry : removedQuoteOptionItems.entrySet()) {
            try {
                quoteEngineHelper.associateQuoteOptionItem(projectId, quoteOptionId, itemDTOEntry.getValue());
            } catch (Exception e) {
                //Carry on
            }
        }
    }

    interface Logger {
        @Log(level = ERROR, format = "*** [%s]***")
        void createdQuoteOptionItemsGettingRemoved(Set<QuoteOptionItemDTO> createdQuoteOptionItems);

        @Log(level = ERROR, format = "*** [%s]***")
        void removedQuoteOptionItemsGettingRestored(Map<String, QuoteOptionItemDTO> removedQuoteOptionItems);
    }
}
