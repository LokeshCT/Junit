package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;

import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CharacteristicReloadUpdaterTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private PmrHelper pmrHelper = mock(PmrHelper.class);
    private ContributesToChangeRequestBuilder contributesToChangeRequestBuilder = mock(ContributesToChangeRequestBuilder.class);
    private DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(contributesToChangeRequestBuilder).build();
    private CharacteristicReloadUpdater characteristicReloadUpdater = new CharacteristicReloadUpdater(cifAssetOrchestrator, dependentUpdateBuilderFactory, pmrHelper);

    @Before
    public void setUp() {
      when(pmrHelper.isRuleFilterSatisfied(org.mockito.Matchers.any(CIFAsset.class), anyString())).thenReturn(true);
    }

    @Test
    public void shouldReloadCharacteristicAndCalculateDependantRequests() {

        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(assetKey, "A", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic("A", "oldValue").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(asset);
        CIFAsset assetWithReloadedValue = assetWithReloadedValue(asset, "A", "newValue", newArrayList(new CIFAssetCharacteristicValue("newValue")));
        when(cifAssetOrchestrator.forceExtendAsset(asset, asset.getCharacteristic("A"), newArrayList(CIFAssetExtension.CharacteristicAllowedValues))).thenReturn(assetWithReloadedValue);

        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", "A", 2)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());

        //When
        CharacteristicReloadResponse characteristicReloadResponse = characteristicReloadUpdater.performUpdate(characteristicReloadRequest);

        //Then
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(assetWithReloadedValue);
        verify(contributesToChangeRequestBuilder, times(1)).buildRequests(assetKey, "S123", "A", 2);
        assertThat(characteristicReloadResponse.getRequest(), is(characteristicReloadRequest));
        assertThat(characteristicReloadResponse.getCharacteristicOldValue(), is("oldValue"));
        assertThat(characteristicReloadResponse.getCharacteristicNewValue(), is("newValue"));
        assertThat(characteristicReloadResponse.getDependantUpdates().isEmpty(), is(true));
    }

    @Test
    public void shouldReloadCharacteristicWithNullValueWhenNoValueCalculatedDuringAssetCharacteristicExtend() {

        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(assetKey, "A", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic("A", "oldValue").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(asset);
        CIFAsset assetWithReloadedValue = assetWithReloadedValue(asset, "A", null, Collections.<CIFAssetCharacteristicValue>emptyList());
        when(cifAssetOrchestrator.forceExtendAsset(asset, asset.getCharacteristic("A"), newArrayList(CIFAssetExtension.CharacteristicAllowedValues))).thenReturn(assetWithReloadedValue);

        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", "A", 2)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());

        //When
        CharacteristicReloadResponse characteristicReloadResponse = characteristicReloadUpdater.performUpdate(characteristicReloadRequest);

        //Then
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(assetWithReloadedValue);
        verify(contributesToChangeRequestBuilder, times(1)).buildRequests(assetKey, "S123", "A", 2);
        assertThat(characteristicReloadResponse.getRequest(), is(characteristicReloadRequest));
        assertThat(characteristicReloadResponse.getCharacteristicOldValue(), is("oldValue"));
        assertThat(characteristicReloadResponse.getCharacteristicNewValue(), nullValue());
        assertThat(characteristicReloadResponse.getDependantUpdates().isEmpty(), is(true));
    }

    @Test
    public void shouldNotReloadCharacteristicWhenActualValueIsNullAndAllowedValuesAreMoreThanOne() {

        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(assetKey, "A", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic("A", null).build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(asset);
        CIFAsset assetWithReloadedValue = assetWithReloadedValue(asset, "A", null, newArrayList(new CIFAssetCharacteristicValue("newValue1"), new CIFAssetCharacteristicValue("newValue2")));
        when(cifAssetOrchestrator.forceExtendAsset(asset, asset.getCharacteristic("A"), newArrayList(CIFAssetExtension.CharacteristicAllowedValues))).thenReturn(assetWithReloadedValue);

        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", "A", 2)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());

        //When
        CharacteristicReloadResponse characteristicReloadResponse = characteristicReloadUpdater.performUpdate(characteristicReloadRequest);

        //Then
        verify(cifAssetOrchestrator, times(0)).saveAssetAndClearCaches(assetWithReloadedValue);
        verify(contributesToChangeRequestBuilder, times(0)).buildRequests(assetKey, "S123", "A", 2);
        assertThat(characteristicReloadResponse.getRequest(), is(characteristicReloadRequest));
        assertThat(characteristicReloadResponse.getCharacteristicOldValue(), nullValue());
        assertThat(characteristicReloadResponse.getCharacteristicNewValue(), nullValue());
        assertThat(characteristicReloadResponse.getDependantUpdates().isEmpty(), is(true));
    }

    @Test
    public void shouldReloadCharacteristicWithNullValueWhenActualValueIsNullWithNoReloadedAllowedValues() {

        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(assetKey, "A", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic("A", null).build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(asset);
        CIFAsset assetWithReloadedValue = assetWithReloadedValue(asset, "A", null, null);
        when(cifAssetOrchestrator.forceExtendAsset(asset, asset.getCharacteristic("A"), newArrayList(CIFAssetExtension.CharacteristicAllowedValues))).thenReturn(assetWithReloadedValue);

        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", "A", 2)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());

        //When
        CharacteristicReloadResponse characteristicReloadResponse = characteristicReloadUpdater.performUpdate(characteristicReloadRequest);

        //Then
        verify(cifAssetOrchestrator, times(0)).saveAssetAndClearCaches(assetWithReloadedValue);
        verify(contributesToChangeRequestBuilder, times(0)).buildRequests(assetKey, "S123", "A", 2);
        assertThat(characteristicReloadResponse.getRequest(), is(characteristicReloadRequest));
        assertThat(characteristicReloadResponse.getCharacteristicOldValue(), nullValue());
        assertThat(characteristicReloadResponse.getCharacteristicNewValue(), nullValue());
        assertThat(characteristicReloadResponse.getDependantUpdates().isEmpty(), is(true));
    }



    @Test
    public void shouldNotSaveReloadCharacteristicWhenReloadedAllowedValuesContainsPreviousValue() {

        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(assetKey, "A", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic("A", "oldValue").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(asset);
        CIFAsset assetWithReloadedValue = assetWithReloadedValue(asset, "A", null, Collections.<CIFAssetCharacteristicValue>emptyList());
        when(cifAssetOrchestrator.forceExtendAsset(asset, asset.getCharacteristic("A"), newArrayList(CIFAssetExtension.CharacteristicAllowedValues))).thenReturn(assetWithReloadedValue);

        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", "A", 2)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());

        //When
        CharacteristicReloadResponse characteristicReloadResponse = characteristicReloadUpdater.performUpdate(characteristicReloadRequest);

        //Then
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(assetWithReloadedValue);
        verify(contributesToChangeRequestBuilder, times(1)).buildRequests(assetKey, "S123", "A", 2);
        assertThat(characteristicReloadResponse.getRequest(), is(characteristicReloadRequest));
        assertThat(characteristicReloadResponse.getCharacteristicOldValue(), is("oldValue"));
        assertThat(characteristicReloadResponse.getCharacteristicNewValue(), nullValue());
        assertThat(characteristicReloadResponse.getDependantUpdates().isEmpty(), is(true));
    }

    @Test
    public void shouldNotReloadCharacteristicWithNullValueWhenNoAllowedValuesReturnedAndNoSourceRuleFilterSatisfied() {

        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(assetKey, "A", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic("A", "oldValue").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(asset);
        CIFAsset assetWithReloadedValue = assetWithReloadedValue(asset, "A", null, Collections.<CIFAssetCharacteristicValue>emptyList());
        when(cifAssetOrchestrator.forceExtendAsset(asset, asset.getCharacteristic("A"), newArrayList(CIFAssetExtension.CharacteristicAllowedValues))).thenReturn(assetWithReloadedValue);

        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", "A", 2)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());
        when(pmrHelper.isRuleFilterSatisfied(org.mockito.Matchers.any(CIFAsset.class), eq("A"))).thenReturn(false);
        //When
        CharacteristicReloadResponse characteristicReloadResponse = characteristicReloadUpdater.performUpdate(characteristicReloadRequest);

        //Then
        verify(cifAssetOrchestrator, times(0)).saveAssetAndClearCaches(assetWithReloadedValue);
        verify(contributesToChangeRequestBuilder, times(0)).buildRequests(assetKey, "S123", "A", 2);
        assertThat(characteristicReloadResponse.getRequest(), is(characteristicReloadRequest));
        assertThat(characteristicReloadResponse.getCharacteristicOldValue(), is("oldValue"));
        assertThat(characteristicReloadResponse.getCharacteristicNewValue(), is("oldValue"));
        assertThat(characteristicReloadResponse.getDependantUpdates().isEmpty(), is(true));
    }


    @Test
    public void shouldThrowExceptionWhenReloadableCharacteristicIsNotAvailable() {
        //Given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage(containsString("Attribute A not available for reload in asset AssetKey[anAssetId, 1]"));

        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(assetKey, "A", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic("B", "someValue").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(asset);
        //When
        characteristicReloadUpdater.performUpdate(characteristicReloadRequest);
    }

    @Test
    public void shouldReloadCharacteristicAndCalculateSpecialBidCharacteristicsCreationDependantRequests() {

        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(assetKey, SPECIAL_BID_TEMPLATE_RESERVED_NAME, 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME, "oldValue").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(asset);
        CIFAsset assetWithReloadedValue = assetWithReloadedValue(asset, SPECIAL_BID_TEMPLATE_RESERVED_NAME, "newValue", newArrayList(new CIFAssetCharacteristicValue("newValue")));
        when(cifAssetOrchestrator.forceExtendAsset(asset, asset.getCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME), newArrayList(CIFAssetExtension.CharacteristicAllowedValues))).thenReturn(assetWithReloadedValue);

        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", SPECIAL_BID_TEMPLATE_RESERVED_NAME, 2)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());

        //When
        CharacteristicReloadResponse characteristicReloadResponse = characteristicReloadUpdater.performUpdate(characteristicReloadRequest);

        //Then
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(assetWithReloadedValue);
        verify(contributesToChangeRequestBuilder, times(1)).buildRequests(assetKey, "S123", SPECIAL_BID_TEMPLATE_RESERVED_NAME, 2);
        assertThat(characteristicReloadResponse.getRequest(), is(characteristicReloadRequest));
        assertThat(characteristicReloadResponse.getCharacteristicOldValue(), is("oldValue"));
        assertThat(characteristicReloadResponse.getCharacteristicNewValue(), is("newValue"));
        CIFAssetUpdateRequest creationRequest = new SpecialBidAttributesCreationRequest(assetKey);
        assertThat(characteristicReloadResponse.getDependantUpdates(), hasItem(creationRequest));
    }


    private CIFAsset assetWithReloadedValue(CIFAsset asset, String attribute, String newValue, List<CIFAssetCharacteristicValue> allowedValues) {
        return CIFAssetFixture.aCIFAsset().withID(asset.getAssetKey().getAssetId()).withVersion(asset.getAssetKey().getAssetVersion())
                              .withProductIdentifier(asset.getProductCode(), asset.getProductVersion())
                              .withCharacteristic(attribute, newValue, allowedValues).build();
    }

}