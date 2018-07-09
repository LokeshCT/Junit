package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeDataType;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.AttributeOwner;
import com.bt.rsqe.domain.product.DefaultValue;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.bt.rsqe.domain.product.AttributeDataType.*;
import static com.bt.rsqe.domain.product.AttributeOwner.*;
import static com.bt.rsqe.domain.product.ProductOffering.STENCIL_RESERVED_NAME;
import static com.google.common.collect.Lists.*;
import static org.mockito.Mockito.*;

public class CIFAssetCharacteristicExtenderTest {
    public static final String CHARACTERISTIC_ONE_NAME = "CharacteristicOne";
    public static final String CHARACTERISTIC_STENCIL_NAME = STENCIL_RESERVED_NAME;
    public static final String CHARACTERISTIC_READ_ONLY_NAME = "CharacteristicThree";
    public static final String CHARACTERISTIC_QREF_NAME = "CharacteristicFour";
    public static final String CHARACTERISTIC_DATE_NAME = "CharacteristicFive";
    private final PmrHelper pmrHelper = mock(PmrHelper.class);
    private final CharacteristicExtender extender = new CharacteristicExtender(pmrHelper);
    private final CIFAsset baseAsset = mock(CIFAsset.class);
    private final ProductOffering baseOffering = mock(ProductOffering.class);
    private final CIFAssetCharacteristic baseCharacteristic1 = mock(CIFAssetCharacteristic.class);
    private final CIFAssetCharacteristic stencilCharacteristic = mock(CIFAssetCharacteristic.class);
    private final CIFAssetCharacteristic readOnlyCharacteristic = mock(CIFAssetCharacteristic.class);
    private final CIFAssetCharacteristic qrefCharacteristic = mock(CIFAssetCharacteristic.class);
    private final CIFAssetCharacteristic dateCharacteristic = mock(CIFAssetCharacteristic.class);
    private final Attribute baseAttribute1 = mock(Attribute.class);
    private final Attribute stencilAttribute = mock(Attribute.class);
    private final Attribute readOnlyAttribute = mock(Attribute.class);
    private final Attribute qrefAttribute = mock(Attribute.class);
    private final Attribute dateAttribute = mock(Attribute.class);

    @Before
    public void setUp() throws Exception {
        when(pmrHelper.getProductOffering(baseAsset)).thenReturn(baseOffering);
        when(baseAsset.getCharacteristics()).thenReturn(newArrayList(baseCharacteristic1, stencilCharacteristic,
                                                                     readOnlyCharacteristic, qrefCharacteristic,
                                                                     dateCharacteristic));
        when(baseAsset.getQuoteOptionItemDetail()).thenReturn(new CIFAssetQuoteOptionItemDetail(DRAFT, 1, false, false, "USD", "12", false,
                                                                                                JaxbDateTime.NIL,
                                                                                                new ArrayList<PriceBookDTO>(),
                                                                                                LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));
        mockAttribute(baseCharacteristic1, baseOffering, CHARACTERISTIC_ONE_NAME, baseAsset, baseAttribute1,
                      false, false, Offering, STRING, DefaultValue.instanceOf(""));
        mockAttribute(stencilCharacteristic, baseOffering, CHARACTERISTIC_STENCIL_NAME, baseAsset, stencilAttribute,
                      true, false, Offering, STRING, DefaultValue.NOT_SET);
        mockAttribute(readOnlyCharacteristic, baseOffering, CHARACTERISTIC_READ_ONLY_NAME, baseAsset, readOnlyAttribute,
                      false, true, Offering, STRING, DefaultValue.NOT_SET);
        mockAttribute(qrefCharacteristic, baseOffering, CHARACTERISTIC_QREF_NAME, baseAsset, qrefAttribute,
                      false, false, Qref, STRING, DefaultValue.NOT_SET);
        mockAttribute(dateCharacteristic, baseOffering, CHARACTERISTIC_DATE_NAME, baseAsset, dateAttribute,
                      false, false, Offering, DATE, DefaultValue.NOT_SET);
    }

    private void mockAttribute(CIFAssetCharacteristic characteristic, ProductOffering offering, String name, CIFAsset asset,
                               Attribute attribute, boolean isStencil, boolean isReadOnly, AttributeOwner owner, AttributeDataType type, DefaultValue defaultValue) {
        when(attribute.isStencil()).thenReturn(isStencil);
        when(attribute.isReadOnly()).thenReturn(isReadOnly);
        when(attribute.getAttributeOwner()).thenReturn(owner);
        when(attribute.dataType()).thenReturn(type);
        when(attribute.isVisibleInSummary()).thenReturn(false);
        when(attribute.getDisplayName()).thenReturn("");
        when(pmrHelper.getAllowedValues(asset, attribute)).thenReturn(Optional.<List<CIFAssetCharacteristicValue>>absent());
        when(offering.getAttribute(new AttributeName(name))).thenReturn(attribute);
        when(characteristic.getName()).thenReturn(name);
        when(attribute.getDefaultValue()).thenReturn(defaultValue);
        when(attribute.isOptional()).thenReturn(true);
        when(characteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(isStencil, isReadOnly, owner, type, false, "", false, defaultValue));
    }

    @Test
    public void shouldExtendCharacteristicsWithAllowedValuesWhenRequested(){
        List<CIFAssetCharacteristicValue> expectedBaseAllowedValues = newArrayList(new CIFAssetCharacteristicValue("value1"),
                                                                                   new CIFAssetCharacteristicValue("value2"));
        when(pmrHelper.getAllowedValues(baseAsset, baseAttribute1)).thenReturn(Optional.of(expectedBaseAllowedValues));
        when(pmrHelper.getAllowedValues(baseAsset, stencilAttribute)).thenReturn(Optional.<List<CIFAssetCharacteristicValue>>absent());

        extender.extend(newArrayList(CharacteristicAllowedValues), baseAsset, baseOffering, baseAsset.getCharacteristics());

    }

    @Test
    public void shouldExtendCharacteristicsWithAllowedValuesAndCalculateValueWhenRequested(){
        List<CIFAssetCharacteristicValue> characteristicValues = newArrayList(new CIFAssetCharacteristicValue("value1"));
        when(pmrHelper.getAllowedValues(baseAsset, baseAttribute1)).thenReturn(Optional.of(characteristicValues));
        when(baseCharacteristic1.getAllowedValues()).thenReturn(characteristicValues);

        extender.extend(newArrayList(CharacteristicAllowedValues, CharacteristicValue), baseAsset, baseOffering, newArrayList(baseCharacteristic1));

        verify(baseCharacteristic1, times(1)).loadAllowedValues(characteristicValues);
        verify(baseCharacteristic1, times(1)).setValue("value1");
    }

    @Test
    public void shouldExtendCharacteristicsWithNullAllowedValuesWhenNoneReturned(){
        when(pmrHelper.getAllowedValues(baseAsset, baseAttribute1)).thenReturn(Optional.<List<CIFAssetCharacteristicValue>>absent());
        when(pmrHelper.getAllowedValues(baseAsset, stencilAttribute)).thenReturn(Optional.<List<CIFAssetCharacteristicValue>>absent());

        extender.extend(newArrayList(CharacteristicAllowedValues), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verify(baseCharacteristic1, times(1)).loadAllowedValues(anyListOf(CIFAssetCharacteristicValue.class));
    }

    @Test
    public void shouldNotLoadAllowedValuesOnACharacteristicWhichAlreadyHasThem() {
        CIFAssetCharacteristic baseCharacteristic1WithAllowedValues = mock(CIFAssetCharacteristic.class);
        when(baseCharacteristic1WithAllowedValues.hasExtension(CIFAssetExtension.CharacteristicAllowedValues)).thenReturn(true);
        when(baseAsset.getCharacteristics()).thenReturn(newArrayList(baseCharacteristic1WithAllowedValues));

        extender.extend(newArrayList(CharacteristicAllowedValues), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verifyNoMoreInteractions(baseCharacteristic1);
    }

    @Test
    public void shouldNotLoadCharacteristicsIfNotRequested() {
        extender.extend(newArrayList(QuoteOptionItemDetail), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verify(baseCharacteristic1, times(0)).loadAllowedValues(anyListOf(CIFAssetCharacteristicValue.class));
    }

    @Test
    public void shouldLoadAllowedValuesForStencilCharacteristics() {
        List<CIFAssetCharacteristicValue> expectedStencilAllowedValues = newArrayList(new CIFAssetCharacteristicValue("value1"),
                                                                                   new CIFAssetCharacteristicValue("value2"));
        when(pmrHelper.getAllowedValues(baseAsset, stencilAttribute)).thenReturn(Optional.of(expectedStencilAllowedValues));

        extender.extend(newArrayList(StencilDetails), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verify(stencilCharacteristic, times(1)).loadAllowedValues(expectedStencilAllowedValues);
    }

    @Test
    public void shouldLoadAttributeDetail() {
        extender.extend(newArrayList(AttributeDetails), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verify(baseCharacteristic1, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, ""));
        verify(stencilCharacteristic, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(true, false, Offering, STRING, false, "", false, ""));
        verify(readOnlyCharacteristic, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, STRING, false, "", false, ""));
        verify(qrefCharacteristic, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Qref, STRING, false, "", false, ""));
        verify(dateCharacteristic, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, DATE, false, "", false, ""));
    }

    @Test
    public void shouldLoadReadOnlyAttributeDetailWhenQuoteOptionIsLocked() {
        when(baseAsset.getQuoteOptionItemDetail()).thenReturn(new CIFAssetQuoteOptionItemDetail(CUSTOMER_APPROVED, 1, false, false, "USD",
                                                                                                "12", false, JaxbDateTime.NIL,
                                                                                                new ArrayList<PriceBookDTO>(),
                                                                                                LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));

        extender.extend(newArrayList(AttributeDetails), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verify(baseCharacteristic1, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, STRING, false, "", false, ""));
        verify(stencilCharacteristic, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(true, true, Offering, STRING, false, "", false, ""));
        verify(readOnlyCharacteristic, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, STRING, false, "", false, ""));
        verify(qrefCharacteristic, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Qref, STRING, false, "", false, ""));
        verify(dateCharacteristic, times(1)).loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, DATE, false, "", false, ""));
    }

    @Test
    public void shouldNotSetDescriptionWhenNotRequested() {
        extender.extend(newArrayList(AttributeDetails), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verify(baseAsset, times(0)).loadDescription(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldSetDescriptionToBlankIfNoVisibleInSummaryAttributes() {
        when(baseCharacteristic1.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        when(stencilCharacteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        when(readOnlyCharacteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        when(qrefCharacteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        when(dateCharacteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));

        extender.extend(newArrayList(Description), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verify(baseAsset, times(1)).loadDescription("", "", "");
    }

    @Test
    public void shouldSetDescriptionWhenVisibleInSummaryAttributesAvailable() {
        when(baseCharacteristic1.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, true, "Attr1", false, "defaultValue"));
        when(stencilCharacteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, true, "Attr2", false, "defaultValue"));
        when(readOnlyCharacteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        when(qrefCharacteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        when(dateCharacteristic.getAttributeDetail()).thenReturn(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        when(baseCharacteristic1.getValue()).thenReturn("val1");
        when(stencilCharacteristic.getValue()).thenReturn("val2");

        extender.extend(newArrayList(Description), baseAsset, baseOffering, baseAsset.getCharacteristics());

        verify(baseAsset, times(1)).loadDescription("Attr1:val1 Attr2:val2 ", "val1 val2 ", "val1,val2");
    }
}
