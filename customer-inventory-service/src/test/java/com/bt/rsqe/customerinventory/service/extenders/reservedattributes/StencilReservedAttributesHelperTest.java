package com.bt.rsqe.customerinventory.service.extenders.reservedattributes;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.domain.QrefIdFormat;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.updates.CIFAssetMockHelper.*;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StencilReservedAttributesHelperTest {
    private final CIFAsset baseAsset = mock(CIFAsset.class);
    private final StencilReservedAttributesHelper stencilHelper = new StencilReservedAttributesHelper();
    private final CIFAssetCharacteristic stencilCharacteristic = new CIFAssetCharacteristic(STENCIL_RESERVED_NAME, "S1", true);
    private final CIFAssetCharacteristic stencilCharacteristicForAccess = new CIFAssetCharacteristic(STENCIL_RESERVED_NAME, QrefIdFormat.convert("S1"), true);
    private final CIFAssetCharacteristic stencilCharacteristicWhichDoesNotMatchAnyAllowedValue = new CIFAssetCharacteristic(STENCIL_RESERVED_NAME, "S3", true);
    private final CIFAssetCharacteristic stencilCharacteristicWithNoAllowedValues = new CIFAssetCharacteristic(STENCIL_RESERVED_NAME, "S1", true);
    private final CIFAssetCharacteristic stencilCharacteristicWithUnloadedAllowedValues = new CIFAssetCharacteristic(STENCIL_RESERVED_NAME, "S1", true);
    private final CIFAssetCharacteristic stencilVersionCharacteristic = new CIFAssetCharacteristic(STENCIL_VERSION_RESERVED_NAME, "V1", true);
    private final CIFAssetCharacteristic productIdentifierCharacteristic = new CIFAssetCharacteristic(PRODUCT_IDENTIFIER_RESERVED_NAME, "S2", true);
    private final CIFAssetCharacteristicValue allowedValue1 = new CIFAssetCharacteristicValue("S1", "CaptionS1", "Group");
    private final CIFAssetCharacteristicValue allowedValue2 = new CIFAssetCharacteristicValue("S2", "CaptionS2", "Group");

    @Before
    public void setUp() throws Exception {
        List<CIFAssetCharacteristicValue> allowedValues = newArrayList(allowedValue1, allowedValue2);
        stencilCharacteristic.loadAllowedValues(allowedValues);
        stencilCharacteristicWhichDoesNotMatchAnyAllowedValue.loadAllowedValues(allowedValues);
        stencilCharacteristicWithNoAllowedValues.loadAllowedValues(null);
        productIdentifierCharacteristic.loadAllowedValues(allowedValues);
    }

    @Test
    public void shouldGetNullFromNoStencilAttributes() {
        assertThat(stencilHelper.getStencilDetail(baseAsset), nullValue());
    }

    @Test
    public void shouldGetNullFromStencilVersionAttribute() {
        mockCharacteristic(baseAsset, stencilVersionCharacteristic);
        assertThat(stencilHelper.getStencilDetail(baseAsset), nullValue());
    }

    @Test
    public void shouldGetStencilDetailFromStencilAndVersionAttributes() {
        mockCharacteristics(baseAsset, stencilCharacteristic, stencilVersionCharacteristic);

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        assertThat(stencilDetail.getStencilCode(), is(stencilCharacteristic.getValue()));
        assertThat(stencilDetail.getStencilVersion(), is(stencilVersionCharacteristic.getValue()));
        assertThat(stencilDetail.getProductName(), is(allowedValue1.getCaption()));
    }

    @Test
    public void shouldGetStencilDetailFromProductIdentifierAndVersionAttributes() {
        mockCharacteristics (baseAsset, productIdentifierCharacteristic, stencilVersionCharacteristic) ;

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        assertThat(stencilDetail.getStencilCode(), is(productIdentifierCharacteristic.getValue()));
        assertThat(stencilDetail.getStencilVersion(), is(stencilVersionCharacteristic.getValue()));
        assertThat(stencilDetail.getProductName(), is(allowedValue2.getCaption()));
    }

    @Test
    public void shouldGetStencilDetailFromOnlyStencilAttribute() {
        mockCharacteristic(baseAsset, stencilCharacteristic);

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        assertThat(stencilDetail.getStencilCode(), is(stencilCharacteristic.getValue()));
        assertThat(stencilDetail.getStencilVersion(), nullValue());
        assertThat(stencilDetail.getProductName(), is(allowedValue1.getCaption()));
    }

    @Test
    public void shouldGetStencilIdFromOnlyProductIdentifierAttribute() {
        mockCharacteristic(baseAsset, productIdentifierCharacteristic);

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        assertThat(stencilDetail.getStencilCode(), is(productIdentifierCharacteristic.getValue()));
        assertThat(stencilDetail.getStencilVersion(), nullValue());
        assertThat(stencilDetail.getProductName(), is(allowedValue2.getCaption()));
    }

    @Test
    public void shouldGetAvailableStencilsFromStencilAttributeAllowedValues() {
        mockCharacteristic(baseAsset, stencilCharacteristic);

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        final List<CIFAssetStencilDetail> expectedAllowedStencils = newArrayList(new CIFAssetStencilDetail(allowedValue1.getValue(), null,
                                                                                                           allowedValue1.getCaption(), null),
                                                                                 new CIFAssetStencilDetail(allowedValue2.getValue(), null,
                                                                                                           allowedValue2.getCaption(), null));
        assertThat(stencilDetail.getAllowedStencils(), is(expectedAllowedStencils));
    }

    @Test
    public void shouldNotGetAvailableStencilsWhenNoStencilAttributeAllowedValues() {
        mockCharacteristic(baseAsset, stencilCharacteristicWithNoAllowedValues);

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        assertThat(stencilDetail.getAllowedStencils(), nullValue());
    }

    @Test
    public void shouldNotGetAvailableStencilsWhenStencilAttributeAllowedValuesNotLoaded() {
        mockCharacteristic(baseAsset, stencilCharacteristicWithUnloadedAllowedValues);

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        assertThat(stencilDetail.getAllowedStencils(), nullValue());
    }

    @Test
    public void shouldGetEmptyStencilNameValueWhenSelectedStencilDoesNotMatchAnyAllowedValue() {
        mockCharacteristics(baseAsset, stencilCharacteristicWhichDoesNotMatchAnyAllowedValue, stencilVersionCharacteristic);

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        assertThat(stencilDetail.getStencilCode(), is(stencilCharacteristicWhichDoesNotMatchAnyAllowedValue.getValue()));
        assertThat(stencilDetail.getStencilVersion(), is(stencilVersionCharacteristic.getValue()));
        assertThat(stencilDetail.getProductName(), is(""));
    }

    @Test
    public void shouldGetStencilNameFromStencilCodeForAccessStencilCode() {
        mockCharacteristics(baseAsset, stencilCharacteristicForAccess,stencilVersionCharacteristic);

        final CIFAssetStencilDetail stencilDetail = stencilHelper.getStencilDetail(baseAsset);

        assertThat(stencilDetail.getStencilCode(), is(stencilCharacteristicForAccess.getValue()));
        assertThat(stencilDetail.getStencilVersion(), is(stencilVersionCharacteristic.getValue()));
        assertThat(stencilDetail.getProductName(), is("S1"));
    }
}