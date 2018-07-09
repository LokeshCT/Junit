package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeResponse;
import com.bt.rsqe.domain.AssetKey;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
public class UpdationIgnoreRequestUpdaterTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private UpdationIgnoreRequestUpdater requestUpdater = new UpdationIgnoreRequestUpdater();

    @Test
    public void shouldConstructResponseFromValidationImpactChangeRequest() {
        //Given
        ValidationImpactChangeRequest validationImpactChangeRequest = new ValidationImpactChangeRequest(new AssetKey("anAsset", 1L));

        //When
        CIFAssetUpdateResponse cifAssetUpdateResponse = requestUpdater.performUpdate(validationImpactChangeRequest);

        //Then
        assertThat((ValidationImpactChangeResponse) cifAssetUpdateResponse, is(new ValidationImpactChangeResponse(validationImpactChangeRequest)));

    }

    @Test
    public void shouldConstructResponseFromCardinalityImpactChangeRequest() {
        //Given
        CardinalityImpactChangeRequest cardinalityImpactChangeRequest = new CardinalityImpactChangeRequest(new AssetKey("anAsset", 1L));

        //When
        CIFAssetUpdateResponse cifAssetUpdateResponse = requestUpdater.performUpdate(cardinalityImpactChangeRequest);

        //Then
        assertThat((CardinalityImpactChangeResponse) cifAssetUpdateResponse, is(new CardinalityImpactChangeResponse(cardinalityImpactChangeRequest)));
    }

    @Test
    public void shouldConstructResponseFromRuleFilterImpactChangeRequest() {
        //Given
        RuleFilterImpactChangeRequest ruleFilterImpactChangeRequest = new RuleFilterImpactChangeRequest(new AssetKey("anAsset", 1L));

        //When
        CIFAssetUpdateResponse cifAssetUpdateResponse = requestUpdater.performUpdate(ruleFilterImpactChangeRequest);

        //Then
        assertThat((RuleFilterImpactChangeResponse) cifAssetUpdateResponse, is(new RuleFilterImpactChangeResponse(ruleFilterImpactChangeRequest)));
    }

    @Test
    public void shouldThrowExceptionWhenUpdationIgnoreRequestIsNotSupported() {
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage(containsString(String.format("Cannot build for request %s, as its not currently supported", CharacteristicChangeRequest.class.getName())));

        //Given
        CIFAssetUpdateRequest cifAssetUpdateRequest = new CharacteristicChangeRequest();

        //When
        requestUpdater.performUpdate(cifAssetUpdateRequest);
    }

}