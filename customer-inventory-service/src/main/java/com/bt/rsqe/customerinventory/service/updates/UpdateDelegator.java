package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RestoreAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidCharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.google.common.collect.ImmutableMap;

import java.util.Map;


public class UpdateDelegator {
    private final Map<Class,CIFAssetUpdater> updaterMap ;

    public UpdateDelegator(CreateRelationshipUpdater createRelationshipUpdater,
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
                           SpecialBidCharacteristicsCreationUpdater specialBidCharacteristicsCreationUpdater,
                           RestoreAssetUpdater restoreAssetUpdater) {

        updaterMap = ImmutableMap.<Class,CIFAssetUpdater>builder()
                .put(CreateRelationshipRequest.class, createRelationshipUpdater)
                .put(CharacteristicChangeRequest.class, characteristicsUpdater)
                .put(UpdateStencilRequest.class, stencilUpdater)
                .put(ChooseRelationshipRequest.class, chooseRelationshipUpdater)
                .put(AutoDefaultRelationshipsRequest.class, autoDefaultRelationshipUpdater)
                .put(CancelRelationshipRequest.class, cancelRelationshipUpdater)
                .put(SpecialBidCharacteristicChangeRequest.class, specialBidCharacteristicsUpdater)
                .put(ReprovideAssetRequest.class, reprovideAssetUpdater)
                .put(InvalidatePriceRequest.class, invalidatePriceUpdater)
                .put(UpdateRelationshipRequest.class, updateRelationshipUpdater)
                .put(ValidationImpactChangeRequest.class, updationIgnoreRequestUpdater)
                .put(CardinalityImpactChangeRequest.class, updationIgnoreRequestUpdater)
                .put(RuleFilterImpactChangeRequest.class, updationIgnoreRequestUpdater)
                .put(CharacteristicReloadRequest.class, characteristicReloadUpdater)
                .put(SpecialBidAttributesReloadRequest.class, specialBidCharacteristicsReloadUpdater)
                .put(SpecialBidAttributesCreationRequest.class, specialBidCharacteristicsCreationUpdater)
                .put(RestoreAssetRequest.class, restoreAssetUpdater)
                .build();
    }

    public CIFAssetUpdateResponse performUpdate(CIFAssetUpdateRequest update)
    {
        final CIFAssetUpdater cifAssetUpdater = updaterMap.get(update.getClass());
        if (cifAssetUpdater == null)
        {
            throw new IllegalArgumentException("Cannot build an updater for update with type " + update.getClass().getCanonicalName());
        }

        return cifAssetUpdater.performUpdate(update);
    }

}
