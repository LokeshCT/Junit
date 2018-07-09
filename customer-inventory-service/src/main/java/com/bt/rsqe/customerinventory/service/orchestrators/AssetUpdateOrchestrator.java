package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetUpdateRequestFilter;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetUpdateResponseFilter;
import com.bt.rsqe.customerinventory.service.updates.AutoDefaultRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.CancelRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.CharacteristicReloadUpdater;
import com.bt.rsqe.customerinventory.service.updates.CharacteristicsUpdater;
import com.bt.rsqe.customerinventory.service.updates.ChooseRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.CreateRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.InvalidatePriceUpdater;
import com.bt.rsqe.customerinventory.service.updates.ReprovideAssetUpdater;
import com.bt.rsqe.customerinventory.service.updates.RestoreAssetUpdater;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidCharacteristicsCreationUpdater;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidCharacteristicsReloadUpdater;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidCharacteristicsUpdater;
import com.bt.rsqe.customerinventory.service.updates.UpdateDelegator;
import com.bt.rsqe.customerinventory.service.updates.UpdateRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.UpdateStencilUpdater;
import com.bt.rsqe.customerinventory.service.updates.UpdationIgnoreRequestUpdater;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.bt.rsqe.logging.LogLevel.*;

public class AssetUpdateOrchestrator {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private UpdateDelegator updateDelegator;
    private CIFAssetUpdateRequestFilter cifAssetUpdateRequestFilter;
    private CIFAssetUpdateResponseFilter cifAssetUpdateResponseFilter;

    public AssetUpdateOrchestrator(CreateRelationshipUpdater createRelationshipUpdater,
                                   CharacteristicsUpdater characteristicsUpdater,
                                   UpdateStencilUpdater stencilUpdater,
                                   ChooseRelationshipUpdater chooseRelationshipUpdater,
                                   AutoDefaultRelationshipUpdater autoDefaultRelationshipUpdater,
                                   SpecialBidCharacteristicsUpdater specialBidCharacteristicsUpdater,
                                   CancelRelationshipUpdater cancelRelationshipUpdater,
                                   ReprovideAssetUpdater reprovideAssetUpdater,
                                   UpdateRelationshipUpdater updateRelationshipUpdater,
                                   UpdationIgnoreRequestUpdater updationIgnoreRequestUpdater,
                                   CharacteristicReloadUpdater characteristicReloadUpdater,
                                   SpecialBidCharacteristicsReloadUpdater specialBidCharacteristicsReloadUpdater,
                                   InvalidatePriceUpdater invalidatePriceUpdater,
                                   CIFAssetUpdateRequestFilter cifAssetUpdateRequestFilter,
                                   CIFAssetUpdateResponseFilter cifAssetUpdateResponseFilter,
                                   SpecialBidCharacteristicsCreationUpdater specialBidCharacteristicsCreationUpdater,
                                   RestoreAssetUpdater restoreAssetUpdater) {
        this.cifAssetUpdateRequestFilter = cifAssetUpdateRequestFilter;
        this.cifAssetUpdateResponseFilter = cifAssetUpdateResponseFilter;
        updateDelegator = new UpdateDelegator(createRelationshipUpdater,
                                              characteristicsUpdater,
                                              stencilUpdater,
                                              chooseRelationshipUpdater,
                                              autoDefaultRelationshipUpdater,
                                              specialBidCharacteristicsUpdater,
                                              cancelRelationshipUpdater,
                                              reprovideAssetUpdater,
                                              updateRelationshipUpdater,
                                              updationIgnoreRequestUpdater,
                                              characteristicReloadUpdater,
                                              specialBidCharacteristicsReloadUpdater,
                                              invalidatePriceUpdater,
                                              specialBidCharacteristicsCreationUpdater,
                                              restoreAssetUpdater);
    }

    public List<CIFAssetUpdateResponse> update(List<CIFAssetUpdateRequest> updateRequests) {
        List<CIFAssetUpdateResponse> updateResponses = new ArrayList<CIFAssetUpdateResponse>();
        LinkedList<CIFAssetUpdateRequest> updatesList = new LinkedList<CIFAssetUpdateRequest>();
        updatesList.addAll(updateRequests);

        for(int requestIndex=0; requestIndex<updatesList.size();requestIndex++) {
            CIFAssetUpdateRequest update = updatesList.get(requestIndex);
            logger.performUpdate(update);
            long start = System.currentTimeMillis();
            final CIFAssetUpdateResponse updateResponse = updateDelegator.performUpdate(update);
            long duration = System.currentTimeMillis() - start ;
            logger.updateComplete(update.getClass(), duration);

            if (updateResponse != null) {
                @SuppressWarnings("unchecked") // I have no idea why we get a warning below.
                List<CIFAssetUpdateRequest> dependantUpdates = updateResponse.getDependantUpdates();
                List<CIFAssetUpdateRequest> filteredRequests = cifAssetUpdateRequestFilter.filter(updatesList, dependantUpdates);

                // Sort the list so that special bid appears at the end, this ensures rules are fired first
                Collections.sort(filteredRequests, new Comparator<CIFAssetUpdateRequest>() {
                    @Override
                    public int compare(CIFAssetUpdateRequest o1, CIFAssetUpdateRequest o2) {
                        if(o1 instanceof SpecialBidAttributesCreationRequest) {
                            return 1;
                        }
                        return 0;
                    }
                });

                for (CIFAssetUpdateRequest cifAssetUpdateRequest : filteredRequests) {
                    logger.dependantUpdateAdded(cifAssetUpdateRequest);
                    updatesList.addLast(cifAssetUpdateRequest);
                }


                updateResponses.add(updateResponse);
            }

            // Clear the cache of assets
            AssetCacheManager.clearCaches(updateResponse);
        }

        return cifAssetUpdateResponseFilter.filter(updateResponses);
    }

    interface Logger {
        @Log(level = DEBUG, format = "update=%s")
        void performUpdate(CIFAssetUpdateRequest update);

        @Log(level = INFO, format = "updateClass=%s duration=%d")
        void updateComplete (Class updateClass, long duration);

        @Log(level = DEBUG, format = "dependantUpdate=%s")
        void dependantUpdateAdded (CIFAssetUpdateRequest dependantUpdate);
    }

}
