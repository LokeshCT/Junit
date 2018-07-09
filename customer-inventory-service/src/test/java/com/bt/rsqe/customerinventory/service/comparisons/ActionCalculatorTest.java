package com.bt.rsqe.customerinventory.service.comparisons;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.productinstancemerge.ChangeType;
import org.junit.Test;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.enums.AssetVersionStatus.*;
import static com.bt.rsqe.productinstancemerge.ChangeType.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ActionCalculatorTest {
    @Test
    public void shouldGetDeleteChangeTypeWhenBaseAssetIsCeasing() {
        final CIFAsset compareTo = aCIFAsset().build();
        final CIFAsset cifAsset = aCIFAsset().withAssetVersionStatus(CEASED).build();

        final ActionCalculator actionCalculator = new ActionCalculator();
        final ChangeType action = actionCalculator.getAction(cifAsset, compareTo);

        assertThat(action, is(DELETE));
    }

    @Test
    public void shouldGetAddChangeTypeWhenNullCompareToAsset() {
        final CIFAsset cifAsset = aCIFAsset().build();

        final ActionCalculator actionCalculator = new ActionCalculator();
        final ChangeType action = actionCalculator.getAction(cifAsset, null);

        assertThat(action, is(ADD));
    }

    @Test
    public void shouldGetUpdateChangeTypeWhenCharacteristicValueHasChanged() {
        final CIFAsset compareTo = aCIFAsset().withCharacteristic("CHAR1", "VALUE1").build();
        final CIFAsset cifAsset = aCIFAsset().withCharacteristic("CHAR1", "VALUE2").build();

        final ActionCalculator actionCalculator = new ActionCalculator();
        final ChangeType action = actionCalculator.getAction(cifAsset, compareTo);

        assertThat(action, is(UPDATE));
    }

    @Test
    public void shouldGetNoneChangeTypeWhenCharacteristicValuesHaveNotChnaged() {
        final CIFAsset compareTo = aCIFAsset().withCharacteristic("CHAR1", "VALUE1").build();
        final CIFAsset cifAsset = aCIFAsset().withCharacteristic("CHAR1", "VALUE1").build();

        final ActionCalculator actionCalculator = new ActionCalculator();
        final ChangeType action = actionCalculator.getAction(cifAsset, compareTo);

        assertThat(action, is(NONE));
    }

    @Test
    public void shouldGetNoneChangeTypeWhenCharacteristicValuesAreNull() {
        final CIFAsset compareTo = aCIFAsset().withCharacteristic("CHAR1", null).build();
        final CIFAsset cifAsset = aCIFAsset().withCharacteristic("CHAR1", null).build();

        final ActionCalculator actionCalculator = new ActionCalculator();
        final ChangeType action = actionCalculator.getAction(cifAsset, compareTo);

        assertThat(action, is(NONE));
    }

    @Test
    // This expected behaviour is to cover the scenario where MIGRATING ASSET characteristic is added inconsistently to
    // the assets.
    public void shouldGetNoneChangeTypeWhenNewCharacteristicValueAreNull() {
        final CIFAsset compareTo = aCIFAsset().withCharacteristic("MIGRATING ASSET", "N").build();
        final CIFAsset cifAsset = aCIFAsset().build();

        final ActionCalculator actionCalculator = new ActionCalculator();
        final ChangeType action = actionCalculator.getAction(cifAsset, compareTo);

        assertThat(action, is(NONE));
    }

}