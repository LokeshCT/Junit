package com.bt.rsqe.customerinventory.service.updates;


import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.project.PricingStatus;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.bt.rsqe.utils.Lists.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

public class CharacteristicReloadUpdater implements CIFAssetUpdater<CharacteristicReloadRequest, CharacteristicReloadResponse>
{
    private static final List<String> SPECIAL_BID_RELOAD_ATTRIBUTES = newArrayList(SPECIAL_BID_ATTRIBUTE_INDICATOR, SPECIAL_BID_TEMPLATE_RESERVED_NAME);
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private DependentUpdateBuilderFactory dependentUpdateBuilderFactory;
    private PmrHelper pmrHelper;

    public CharacteristicReloadUpdater(CIFAssetOrchestrator cifAssetOrchestrator, DependentUpdateBuilderFactory dependentUpdateBuilderFactory, PmrHelper pmrHelper) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
        this.pmrHelper = pmrHelper;
    }

    public CharacteristicReloadResponse performUpdate(CharacteristicReloadRequest update) {
        Set<CIFAssetUpdateRequest> dependantUpdateRequests = newHashSet();

        String associatedAttribute = update.getAssociatedAttribute();

        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(update.getAssetKey(), newArrayList(CIFAssetExtension.Relationships)));

        CIFAssetCharacteristic cifAssetCharacteristic = cifAsset.getCharacteristic(associatedAttribute);
        if (cifAssetCharacteristic == null) {
            throw new NullPointerException(String.format("Attribute %s not available for reload in asset %s", associatedAttribute, update.getAssetKey()));
        } else if (cifAssetCharacteristic.getName().equals(SPECIAL_BID_ATTRIBUTE_INDICATOR) && cifAsset.getPricingStatus().equals(PricingStatus.PROGRESSING)) {
            cifAsset.setPricingStatus(PricingStatus.NOT_PRICED);
        }
        String valueBeforeReload = cifAssetCharacteristic.getValue();

        //Reload Asset Characteristic values
        cifAsset = cifAssetOrchestrator.forceExtendAsset(cifAsset, cifAssetCharacteristic, newArrayList(CIFAssetExtension.CharacteristicAllowedValues));
        CIFAssetCharacteristic assetCharacteristic = cifAsset.getCharacteristic(associatedAttribute);
        List<CIFAssetCharacteristicValue> allowedValues = assetCharacteristic.getAllowedValues();
        assetCharacteristic.loadAllowedValues(allowedValues);

        //Check if existing value is one of the allowed values, then no need to set it.
        if (isEmpty(valueBeforeReload) && isNullOrEmpty(allowedValues)
                || isEmpty(valueBeforeReload) && !isNullOrEmpty(allowedValues) && allowedValues.size() > 1
                || !isNullOrEmpty(allowedValues) && isValueInList(allowedValues, valueBeforeReload)
                || !isSourceRuleFilterSatisfied(cifAsset, associatedAttribute)) {   //During Reload, we have to check if the allowed values are returned after rule filter satisfaction,
                                                                                    // the current pmr allowed values does not say if its actually executed from source rule, or simply returned empty list as filter not satisfied.

            return new CharacteristicReloadResponse(update, valueBeforeReload, valueBeforeReload, Collections.<CIFAssetUpdateRequest>emptyList());
        }

        if (!isNullOrEmpty(allowedValues) && allowedValues.size() == 1 && !allowedValues.get(0).getValue().equals(valueBeforeReload)) { //Set if values are not same.
            assetCharacteristic.setValue(allowedValues.get(0).getValue());
        } else if (!isEmpty(valueBeforeReload) && (isNullOrEmpty(allowedValues) || !isValueInList(allowedValues, valueBeforeReload))) {    //if allowed values size is zero and set the current value as null;
                assetCharacteristic.setValue(null);
        }

        cifAssetOrchestrator.saveAssetAndClearCaches(cifAsset);
        ContributesToChangeRequestBuilder contributesToChangeRequestBuilder = dependentUpdateBuilderFactory.getContributesToChangeRequestBuilder();
        dependantUpdateRequests.addAll(contributesToChangeRequestBuilder.buildRequests(update.getAssetKey(),
                cifAsset.getProductCode(),
                associatedAttribute,
                update.getExecutionDepth() + 1));

        if (SPECIAL_BID_RELOAD_ATTRIBUTES.contains(update.getAssociatedAttribute())) {
            dependantUpdateRequests.add(new SpecialBidAttributesCreationRequest(update.getAssetKey()));
        }

        return new CharacteristicReloadResponse(update, valueBeforeReload, assetCharacteristic.getValue(), newArrayList(dependantUpdateRequests));
    }

    private boolean isSourceRuleFilterSatisfied(CIFAsset cifAsset, String associatedAttribute) {
        return pmrHelper.isRuleFilterSatisfied(cifAsset, associatedAttribute);
    }

    private boolean isValueInList(List<CIFAssetCharacteristicValue> allowedValues, final String valueBeforeReload) {
        return Iterables.tryFind(allowedValues, new Predicate<CIFAssetCharacteristicValue>() {
            @Override
            public boolean apply(CIFAssetCharacteristicValue input) {
                return input.getValue().equals(valueBeforeReload);
            }
        }).isPresent();
    }
}
