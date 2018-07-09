package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.bt.rsqe.logging.LogLevel.*;
import static com.google.common.collect.Lists.*;

public class CharacteristicsUpdater implements CIFAssetUpdater<CharacteristicChangeRequest, CharacteristicChangeResponse> {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final DependentUpdateBuilderFactory dependentUpdateBuilderFactory;
    private CIFCharacteristicValue cifCharacteristicValue;
    private SpecialBidAttributesReloadRequestBuilder specialBidAttributesReloadRequestBuilder;

    public CharacteristicsUpdater(CIFAssetOrchestrator cifAssetOrchestrator, DependentUpdateBuilderFactory dependentUpdateBuilderFactory) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
        this.cifCharacteristicValue = new CIFCharacteristicValue();
        this.specialBidAttributesReloadRequestBuilder = new SpecialBidAttributesReloadRequestBuilder();
    }

    public CharacteristicChangeResponse performUpdate(CharacteristicChangeRequest update) {
        List<CharacteristicChange> characteristicChangeResponses = newArrayList();
        DependantUpdatesBuilder dependantUpdatesBuilder = DependantUpdatesBuilder.dependantUpdatesBuilder();

        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(update.getAssetKey(), newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.AttributeDetails)));
        for (CharacteristicChange request : update.getCharacteristicChanges()) {

            logger.processCharacteristicChange(request);
            CIFAssetCharacteristic characteristic = cifAsset.getCharacteristic(request.getName());

            String newValue = cifCharacteristicValue.valueOf(characteristic.getAttributeDetail().getDataType(),
                    request.getNewValue());

            String oldValue = characteristic.getValue();
            characteristic.setValue(newValue);

            characteristicChangeResponses.add(new CharacteristicChange(request.getName(), request.getNewValue(), oldValue));
            int contributesToExecutionDepth = 1;
            dependantUpdatesBuilder.withSet(dependentUpdateBuilderFactory.getContributesToChangeRequestBuilder().buildRequests(cifAsset.getAssetKey(),
                    cifAsset.getProductCode(),
                    characteristic.getName(),
                    contributesToExecutionDepth));

        }

        dependantUpdatesBuilder.withOptional(dependentUpdateBuilderFactory.getInvalidatePriceRequestBuilder()
                                                                     .invalidatePriceForCharacteristicChanges(cifAsset, update.getCharacteristicNames()));

        cifAssetOrchestrator.saveAsset(cifAsset);
        AssetCacheManager.clearAssetCaches();


        if (specialBidAttributesRequired(characteristicChangeResponses)) {
            dependantUpdatesBuilder.with(new SpecialBidAttributesCreationRequest(update.getAssetKey()));
        }

        dependantUpdatesBuilder.withList(specialBidAttributesReloadRequestBuilder.buildRequests(update.getAssetKey(), update.getCharacteristicChanges()));

        return new CharacteristicChangeResponse(update, characteristicChangeResponses, dependantUpdatesBuilder.dependantRequests());
    }

    //TODO: Move this logic to the dependants updates builder
    private boolean specialBidAttributesRequired(List<CharacteristicChange> characteristicChangeResponses) {
        return Iterables.tryFind(characteristicChangeResponses, new Predicate<CharacteristicChange>() {
            @Override
            public boolean apply(CharacteristicChange input) {
                return (SPECIAL_BID_ATTRIBUTE_INDICATOR.equals(input.getName()) || SPECIAL_BID_TEMPLATE_RESERVED_NAME.equals(input.getName()));
            }
        }).isPresent();
    }

    interface Logger {
        @Log(level = DEBUG, format = "characteristicChange=%s")
        void processCharacteristicChange(CharacteristicChange characteristicChange);
    }


}
