package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidCharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidCharacteristicChangeResponse;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.projectengine.TpeRequestDTO;

import java.util.Collections;

import static com.bt.rsqe.domain.SpecialBidWellKnownAttribute.*;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.bt.rsqe.projectengine.TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.*;
import static com.bt.rsqe.utils.AssertObject.*;

public class SpecialBidCharacteristicsUpdater implements CIFAssetUpdater<SpecialBidCharacteristicChangeRequest, SpecialBidCharacteristicChangeResponse> {

    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private SpecialBidWellKnownAttributeMapper wellKnownAttributeMapper;
    private SpecialBidTemplateAttributeMapper templateAttributeMapper;
    private ExternalAttributesHelper externalAttributesHelper;
    private DependentUpdateBuilderFactory dependentUpdateBuilderFactory;

    public SpecialBidCharacteristicsUpdater (CIFAssetOrchestrator cifAssetOrchestrator,
                                             SpecialBidWellKnownAttributeMapper wellKnownAttributeMapper,
                                             SpecialBidTemplateAttributeMapper templateAttributeMapper,
                                             ExternalAttributesHelper externalAttributesHelper,
                                             DependentUpdateBuilderFactory dependentUpdateBuilderFactory) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.wellKnownAttributeMapper = wellKnownAttributeMapper;
        this.templateAttributeMapper = templateAttributeMapper;
        this.externalAttributesHelper = externalAttributesHelper;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
    }

    public SpecialBidCharacteristicChangeResponse performUpdate(SpecialBidCharacteristicChangeRequest update) {
        DependantUpdatesBuilder dependantUpdatesBuilder = dependentUpdateBuilderFactory.getDependentUpdateBuilderFactory() ;
        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(update.getAssetKey(), Collections.<CIFAssetExtension>emptyList()));
        TpeRequestDTO tpeRequest = externalAttributesHelper.getAttributes(cifAsset);

        wellKnownAttributeMapper.map(update.getCharacteristicChanges(), tpeRequest);
        wellKnownAttributeMapper.syncUpTemplateName(tpeRequest, RequestName.getAttributeName(), getValue(cifAsset));
        templateAttributeMapper.map(update.getCharacteristicChanges(), tpeRequest, PRIMARY);

        // Do any of the characteristic changes need to trigger Price Invalidation?
        dependantUpdatesBuilder.withOptional(dependentUpdateBuilderFactory.getInvalidatePriceRequestBuilder()
                                                             .invalidatePriceForCharacteristicChanges(cifAsset, update.getCharacteristicNames())) ;

        externalAttributesHelper.saveAttributes(cifAsset, tpeRequest);

        return new SpecialBidCharacteristicChangeResponse(update, update.getCharacteristicChanges(), dependantUpdatesBuilder.dependantRequests());
    }

    private String getValue(CIFAsset cifAsset) {
        CIFAssetCharacteristic characteristic = cifAsset.getCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME);
        return isNull(characteristic) ? "" : characteristic.getValue();
    }

}
