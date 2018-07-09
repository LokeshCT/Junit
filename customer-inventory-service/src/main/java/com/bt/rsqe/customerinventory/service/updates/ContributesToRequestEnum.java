package com.bt.rsqe.customerinventory.service.updates;


import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.Association;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.Set;

import static com.bt.rsqe.domain.product.Association.AssociationType.*;
import static com.google.common.collect.Sets.*;

public enum ContributesToRequestEnum {

    AttributeSource(ATTRIBUTE_SOURCE) {
        @Override
        Set<? extends CIFAssetUpdateRequest> buildRequests(final Association association, Set<AssetKey> associatedAssetKeys, final int executionDepth) {
            return newLinkedHashSet(Iterables.transform(associatedAssetKeys, new Function<AssetKey, CharacteristicReloadRequest>() {
                @Override
                public CharacteristicReloadRequest apply(AssetKey input) {
                    return new CharacteristicReloadRequest(input, association.getLinkName(), executionDepth);
                }
            }));
        }
    },
    Validation(VALIDATION) {
        @Override
        Set<? extends CIFAssetUpdateRequest> buildRequests(Association association, Set<AssetKey> associatedAssetKeys, int executionDepth) {
            return newLinkedHashSet(Iterables.transform(associatedAssetKeys, new Function<AssetKey, ValidationImpactChangeRequest>() {
                @Override
                public ValidationImpactChangeRequest apply(AssetKey input) {
                    return new ValidationImpactChangeRequest(input);
                }
            }));
        }
    },
    Cardinality(CARDINALITY) {
        @Override
        Set<? extends CIFAssetUpdateRequest> buildRequests(Association association, Set<AssetKey> associatedAssetKeys, int executionDepth) {
            return newLinkedHashSet(Iterables.transform(associatedAssetKeys, new Function<AssetKey, CardinalityImpactChangeRequest>() {
                @Override
                public CardinalityImpactChangeRequest apply(AssetKey input) {
                    return new CardinalityImpactChangeRequest(input);
                }
            }));
        }
    },
    RuleFilter(RULE_FILTER) {
        @Override
        Set<? extends CIFAssetUpdateRequest> buildRequests(Association association, Set<AssetKey> associatedAssetKeys, int executionDepth) {
            return newLinkedHashSet(Iterables.transform(associatedAssetKeys, new Function<AssetKey, RuleFilterImpactChangeRequest>() {
                @Override
                public RuleFilterImpactChangeRequest apply(AssetKey input) {
                    return new RuleFilterImpactChangeRequest(input);
                }
            }));
        }
    };

    private Association.AssociationType associationType;

    ContributesToRequestEnum(Association.AssociationType associationType) {
        this.associationType = associationType;
    }

    abstract Set<? extends CIFAssetUpdateRequest> buildRequests(Association association, Set<AssetKey> associatedAssetKeys, int executionDepth);

    public static Set<? extends CIFAssetUpdateRequest> contributesToRequest(Association association, Set<AssetKey> assetKeys, int executionDepth) {
        for (ContributesToRequestEnum contributesToRequest : ContributesToRequestEnum.values()) {
            if (contributesToRequest.associationType == association.getAssociationType()) {
                return contributesToRequest.buildRequests(association, assetKeys, executionDepth);
            }
        }
        return Collections.emptySet();
    }
}