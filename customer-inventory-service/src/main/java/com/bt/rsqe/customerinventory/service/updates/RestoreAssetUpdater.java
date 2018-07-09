package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RestoreAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RestoreAssetResponse;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.google.common.base.Optional;

import java.util.List;
import java.util.Objects;

import static com.bt.rsqe.logging.LogLevel.*;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by 802998369 on 05/12/2015.
 */
public class RestoreAssetUpdater implements CIFAssetUpdater<RestoreAssetRequest, RestoreAssetResponse>
{
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);

    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final DependentUpdateBuilderFactory dependentUpdateBuilderFactory;

    public RestoreAssetUpdater(CIFAssetOrchestrator cifAssetOrchestrator, DependentUpdateBuilderFactory dependentUpdateBuilderFactory)
    {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
    }

    String characteristicValue (CIFAssetCharacteristic characteristic)
    {
        String value = null ;
        if (characteristic != null)
        {
            value = characteristic.getValue() ;
        }
        return value ;
    }

    @Override
    public RestoreAssetResponse performUpdate(RestoreAssetRequest update)
    {
        logger.unableToHandleRequest(update);

        // TODO what extension do we really need to use here?
        CIFAssetKey cifAssetKey = new CIFAssetKey(update.getAssetKey(),
                newArrayList (CIFAssetExtension.AsIsAsset, CIFAssetExtension.CharacteristicValue, CIFAssetExtension.Relationships))  ;

        DependantUpdatesBuilder dependantUpdatesBuilder = dependentUpdateBuilderFactory.getDependentUpdateBuilderFactory() ;

        // TODO do we need to check for null on either of these here?
        CIFAsset tobeAsset = cifAssetOrchestrator.getCifAsset(cifAssetKey) ;
        CIFAsset asisAsset = tobeAsset.getAsIsAsset();

        List<CharacteristicChange> characteristicChanges = newArrayList() ;

        // Check across all the characteristics to see which need to be changed back to something else
        for (CIFAssetCharacteristic cifAssetCharacteristic : tobeAsset.getCharacteristics())
        {
            String name = cifAssetCharacteristic.getName();
            String tobeValue = cifAssetCharacteristic.getValue();
            String asisValue = characteristicValue(asisAsset.getCharacteristic(name)) ;
            logger.characteristicValues (name, tobeValue, asisValue) ;
            if (!Objects.equals(asisValue, tobeValue))
            {
                characteristicChanges.add(new CharacteristicChange(name, asisValue));
            }

        }

        // If there were any changes necessary then generate one single characteristic change request for all of them
        if (!characteristicChanges.isEmpty())
        {
            CharacteristicChangeRequest characteristicChangeRequest = new CharacteristicChangeRequest(
                    cifAssetKey.getAssetKey(),
                    CIFAssetExtension.noExtensions(),
                    characteristicChanges,
                    tobeAsset.getLineItemId(),
                    tobeAsset.getQuoteOptionItemDetail().getLockVersion()
            )  ;
            logger.addCharacteristicChangeRequest (characteristicChangeRequest) ;
            dependantUpdatesBuilder.with(characteristicChangeRequest);
        }

        // TODO should we try and figure out all the relationship changes now?
        // or trigger some other request to check again after all the characteristic updates
        // and any relationship changes that they trigger

        throw new UpdateException("RestoreAssetUpdater not implemented yet");

        // return new RestoreAssetResponse(update, dependantUpdatesBuilder.dependantRequests());
    }

    interface Logger
    {
        @Log(level = ERROR, format = "characteristicChange=%s")
        void unableToHandleRequest(RestoreAssetRequest update);

        @Log(level = ERROR, format = "cifAssetKey=%s")
        void currentAssetNotFound(CIFAssetKey cifAssetKey);

        @Log(level = DEBUG, format = "name=%s tobeValue=%s asisValue=%s")
        void characteristicValues(String name, String tobeValue, String asisValue);

        @Log(level = DEBUG, format = "characteristicChangeRequest=%s")
        void addCharacteristicChangeRequest(CharacteristicChangeRequest characteristicChangeRequest);
    }
}
