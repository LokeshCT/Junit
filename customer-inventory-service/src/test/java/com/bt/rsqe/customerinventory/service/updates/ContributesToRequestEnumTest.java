package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.LocalAssociation;
import org.junit.Test;

import java.util.Set;

import static com.bt.rsqe.customerinventory.service.updates.ContributesToRequestEnum.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

public class ContributesToRequestEnumTest {

    @Test @SuppressWarnings("unchecked")
    public void shouldConstructContributesToReloadRequestWhenAssociationTypeIsAttributeSource() {
        //Given
        Set<AssetKey> assetKeys = newHashSet(new AssetKey("A", 1l), new AssetKey("B", 1l));
        //When
        Set<? extends CIFAssetUpdateRequest> contributesToRequests = contributesToRequest(new LocalAssociation("anAttribute", Association.AssociationType.ATTRIBUTE_SOURCE), assetKeys, 1);
        //Then
        assertThat((Iterable<CharacteristicReloadRequest>) contributesToRequests, hasItems(new CharacteristicReloadRequest(new AssetKey("A", 1l), "anAttribute", 1),
                                                                                           new CharacteristicReloadRequest(new AssetKey("B", 1l), "anAttribute", 1)));
    }

    @Test @SuppressWarnings("unchecked")
    public void shouldConstructValidationImpactChangeRequestWhenAssociationTypeIsValidation() {
        //Given
        Set<AssetKey> assetKeys = newHashSet(new AssetKey("A", 1l), new AssetKey("B", 1l));
        //When
        Set<? extends CIFAssetUpdateRequest> contributesToRequests = contributesToRequest(new LocalAssociation("anAttribute", Association.AssociationType.VALIDATION), assetKeys, 1);
        //Then
        assertThat((Iterable<ValidationImpactChangeRequest>) contributesToRequests, hasItems(new ValidationImpactChangeRequest(new AssetKey("A", 1l)),
                new ValidationImpactChangeRequest(new AssetKey("B", 1l))));
    }

    @Test @SuppressWarnings("unchecked")
    public void shouldConstructCardinalityImpactChangeRequestWhenAssociationTypeIsCardinality() {
        //Given
        Set<AssetKey> assetKeys = newHashSet(new AssetKey("A", 1l));
        //When
        Set<? extends CIFAssetUpdateRequest> contributesToRequests = contributesToRequest(new LocalAssociation("anAttribute", Association.AssociationType.CARDINALITY), assetKeys, 1);
        //Then
        assertThat((Iterable<CardinalityImpactChangeRequest>) contributesToRequests, hasItems(new CardinalityImpactChangeRequest(new AssetKey("A", 1l))));
    }

    @Test @SuppressWarnings("unchecked")
    public void shouldConstructRuleFilterImpactChangeRequestWhenAssociationTypeIsRuleFilter() {
        //Given
        Set<AssetKey> assetKeys = newHashSet(new AssetKey("A", 1l));
        //When
        Set<? extends CIFAssetUpdateRequest> contributesToRequests = contributesToRequest(new LocalAssociation("anAttribute", Association.AssociationType.RULE_FILTER), assetKeys, 1);
        //Then
        assertThat((Iterable<RuleFilterImpactChangeRequest>) contributesToRequests, hasItems(new RuleFilterImpactChangeRequest(new AssetKey("A", 1l))));
    }

    @Test
    public void shouldNotConstructChangeRequestWhenAssociationTypeIsNotSupported() {
        //Given
        Set<AssetKey> assetKeys = newHashSet(new AssetKey("A", 1l));
        //When
        Set<? extends CIFAssetUpdateRequest> contributesToRequests = contributesToRequest(new LocalAssociation("anAttribute", null), assetKeys, 1);
        //Then
        assertThat(contributesToRequests.isEmpty(), is(true));
    }

}