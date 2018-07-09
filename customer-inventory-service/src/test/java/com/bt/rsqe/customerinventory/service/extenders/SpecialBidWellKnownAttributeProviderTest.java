package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.domain.DateFormats;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.SpecialBidWellKnownAttribute;
import com.bt.rsqe.domain.product.AttributeDataType;
import com.bt.rsqe.domain.product.AttributeOwner;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.domain.CIFPermission.DirectOnly;
import static com.bt.rsqe.domain.SpecialBidWellKnownAttribute.*;
import static com.bt.rsqe.domain.product.ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;

public class SpecialBidWellKnownAttributeProviderTest {
    private final String templateUrl = "templateUrlValue";
    private final TpeRequestDTO tpeRequest = new TpeRequestDTO();
    private final CIFAsset cifAsset = mock(CIFAsset.class);
    private final String requestName = "requestName";
    private final SpecialBidReservedAttributesHelper attributeHelper = new SpecialBidReservedAttributesHelper();

    @Before
    public void setUp() throws Exception {
        when(cifAsset.getCharacteristics()).thenReturn(newArrayList(new CIFAssetCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME, requestName, true)));
        when(cifAsset.getQuoteOptionItemDetail()).thenReturn(new CIFAssetQuoteOptionItemDetail(QuoteOptionItemStatus.DRAFT, 1, false, false,
                "GBP", "12", false, JaxbDateTime.NIL,
                new ArrayList<PriceBookDTO>(),
                LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));
    }

    @Test
    public void shouldGetBasicWellKnownAttributeInformation() {
        tpeRequest.additionalInformation = "addInfo";
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(AdditionalInformation.getAttributeName(), tpeRequest.additionalInformation, true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.TEXT,
                false, AdditionalInformation.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetTemplateUrlValueFromConstructor() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(TemplateSelectionGuide.getAttributeName(), templateUrl, true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, AttributeOwner.Offering, AttributeDataType.URL,
                false, TemplateSelectionGuide.getAttributeName(), false, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetEmptyTemplateUrlValueWhenGuideIsNull() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(null, attributeHelper);
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(TemplateSelectionGuide.getAttributeName(), "", true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, AttributeOwner.Offering, AttributeDataType.URL,
                false, TemplateSelectionGuide.getAttributeName(), false, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetTemplateWikiUrlValueFromTPERequest() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.templateWiki = "templateWiki";
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(TemplateWiki.getAttributeName(), tpeRequest.templateWiki, true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, AttributeOwner.Offering, AttributeDataType.URL,
                false, TemplateWiki.getAttributeName(), false, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetRequestNameFromCIFAssetCharacteristic() {
        //Given
        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(RequestName.getAttributeName(), requestName, true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.STRING,
                false, RequestName.getAttributeName(), true, ""));

        //When
        when(cifAsset.getCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME)).thenReturn(new CIFAssetCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME, requestName, true));

        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        // Then
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetEmptyRequestNameWhenNoTPETemplateNameCharacteristic() {
        when(cifAsset.getCharacteristics()).thenReturn(new ArrayList<CIFAssetCharacteristic>());
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(RequestName.getAttributeName(), "", true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.STRING,
                false, RequestName.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetCustomerValueCurrency() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.customerValueCurrency = "GBP";
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(CustomerValueCurrency.getAttributeName(), tpeRequest.customerValueCurrency, true);
        expected.loadAllowedValues(newArrayList(new CIFAssetCharacteristicValue("EUR", "EUR", ""),
                new CIFAssetCharacteristicValue("GBP", "GBP", ""),
                new CIFAssetCharacteristicValue("USD", "USD", "")));
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.STRING,
                false, CustomerValueCurrency.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetTier() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.tier = "Stamdard";
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(Tier.getAttributeName(), tpeRequest.tier, true);
        expected.loadAllowedValues(newArrayList(new CIFAssetCharacteristicValue("Standard", "Standard", ""),
                new CIFAssetCharacteristicValue("Non-Standard", "Non-Standard", ""),
                new CIFAssetCharacteristicValue("Complex", "Complex", "", DirectOnly)));
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.STRING,
                false, Tier.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetDefaultTier() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);

        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(Tier.getAttributeName(), SpecialBidWellKnownAttribute.Tier.getDefaultValue(), true);
        expected.loadAllowedValues(newArrayList(new CIFAssetCharacteristicValue("Standard", "Standard", ""),
                new CIFAssetCharacteristicValue("Non-Standard", "Non-Standard", ""),
                new CIFAssetCharacteristicValue("Complex", "Complex", "", DirectOnly)));
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.STRING,
                false, Tier.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetBidManagerName() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.bidManagerName = "bidMgr";
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(BidManagerName.getAttributeName(), tpeRequest.bidManagerName, true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.STRING,
                false, BidManagerName.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetDefaultBidManagerName() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);

        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(BidManagerName.getAttributeName(), SpecialBidWellKnownAttribute.BidManagerName.getDefaultValue(), true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.STRING,
                false, BidManagerName.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetContractLengthMonths() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.contractLength = 12L;
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(ContractLength.getAttributeName(), "12", true);
        expected.loadAllowedValues(newArrayList(new CIFAssetCharacteristicValue("12", "12", ""),
                new CIFAssetCharacteristicValue("24", "24", ""),
                new CIFAssetCharacteristicValue("36", "36", ""),
                new CIFAssetCharacteristicValue("60", "60", "")));
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.NUMBER,
                false, ContractLength.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetWinChance() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.winChance = 5L;
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(WinChance.getAttributeName(), "5", true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.NUMBER,
                false, WinChance.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetDefaultWinChance() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);

        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(WinChance.getAttributeName(), SpecialBidWellKnownAttribute.WinChance.getDefaultValue(), true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.NUMBER,
                false, WinChance.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetVolumeForFeature() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.volumeForFeature = 120L;
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(VolumeForFeature.getAttributeName(), "120", true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.NUMBER,
                false, VolumeForFeature.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetDefaultVolumeForFeature() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);

        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(VolumeForFeature.getAttributeName(), SpecialBidWellKnownAttribute.VolumeForFeature.getDefaultValue(), true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.NUMBER,
                false, VolumeForFeature.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetCustomerValue() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.customerValue = 1000000L;
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(CustomerValue.getAttributeName(), "1000000", true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.NUMBER,
                false, CustomerValue.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetCustomerRequestedDate() throws ParseException {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.customerRequestedDate = new SimpleDateFormat(DateFormats.DATE_FORMAT).parse("2015-Dec-12");
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(CustomerRequestedDate.getAttributeName(), "2015-Dec-12", true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering, AttributeDataType.DATE,
                false, CustomerRequestedDate.getAttributeName(), true, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetDetailedResponse() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.detailedResponse = "detailedResponse";
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(DetailedResponse.getAttributeName(), tpeRequest.detailedResponse, true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, AttributeOwner.Offering, AttributeDataType.TEXT,
                false, DetailedResponse.getAttributeName(), false, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }

    @Test
    public void shouldGetBidState() {
        final SpecialBidWellKnownAttributeProvider attributeProvider = new SpecialBidWellKnownAttributeProvider(templateUrl, attributeHelper);
        tpeRequest.bidState = "bidState";
        final List<CIFAssetCharacteristic> specialBidCharacteristics = attributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);

        final CIFAssetCharacteristic expected = new CIFAssetCharacteristic(BidState.getAttributeName(), tpeRequest.bidState, true);
        expected.loadAllowedValues(null);
        expected.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, AttributeOwner.Offering, AttributeDataType.STRING,
                false, BidState.getAttributeName(), false, ""));
        assertThat(specialBidCharacteristics, hasItem(expected));
    }
}
