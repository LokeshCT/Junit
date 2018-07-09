package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.tpe.client.TemplateTpeClient;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Request;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.updates.CIFAssetMockHelper.mockCharacteristics;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpecialBidTemplateAttributeProviderTest {
    private static final int GROUP_ZERO = 0, GROUP_ONE = 1;
    private static final int ROW_ZERO = 0, ROW_ONE = 1;
    private static final int ATTRIBUTE_ZERO = 0, ATTRIBUTE_ONE = 1;
    private final TemplateTpeClient templateClient = mock(TemplateTpeClient.class);
    private final SpecialBidReservedAttributesHelper attributeHelper = new SpecialBidReservedAttributesHelper();
    private final String GROUP_NAME = "groupName";
    private final String TEMPLATE_NAME = "templateName";
    private final String CONFIG_TYPE = "configType";
    private final SpecialBidTemplateAttributeProvider templateAttributeProvider = new SpecialBidTemplateAttributeProvider(templateClient, attributeHelper);
    private final CIFAsset cifAsset = mock(CIFAsset.class);
    private TpeRequestDTO tpeRequest = new TpeRequestDTO();

    @Before
    public void setUp() throws Exception {
        // Mock the asset
        CIFAssetCharacteristic templateNameCharacteristic = new CIFAssetCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME, TEMPLATE_NAME, true);
        CIFAssetCharacteristic configTypeCharacteristic = new CIFAssetCharacteristic(CONFIGURATION_TYPE_RESERVED_NAME, CONFIG_TYPE, false);
        mockCharacteristics (cifAsset, templateNameCharacteristic, configTypeCharacteristic) ;
        when(cifAsset.getOfferingDetail()).thenReturn(new CIFAssetOfferingDetail("", "", GROUP_NAME, "", false, false, "", false, true, null));
    }

    private void clearMockedCharacteristics()
    {
        when(cifAsset.getCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME)).thenReturn(null) ;
        when(cifAsset.getCharacteristic(CONFIGURATION_TYPE_RESERVED_NAME)).thenReturn(null) ;
    }

    @Test
    public void shouldReturnAllAttributeFromTemplate() {
        // Build the response this test tests against
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withCommonMandatoryAttribute(GROUP_ZERO, ROW_ZERO, ATTRIBUTE_ZERO)
                                      .withCommonMandatoryAttribute(GROUP_ZERO, ROW_ZERO, ATTRIBUTE_ONE)
                                      .withCommonMandatoryAttribute(GROUP_ZERO, ROW_ONE, ATTRIBUTE_ZERO)
                                      .withCommonMandatoryAttribute(GROUP_ONE, ROW_ZERO, ATTRIBUTE_ZERO)
                                      .withPrimaryMandatoryAttribute()
                                      .withPrimaryMandatoryAttribute();
        when(templateClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request(GROUP_NAME, CONFIG_TYPE, TEMPLATE_NAME)))
            .thenReturn(templateDetailsResponseBuilder.build());

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        // Check responses
        assertThat(specialBidCharacteristics.size(), is(6));
    }

    @Test
    public void shouldReturnNoAttributesWhenTemplateNameNotFound() {
        // TODO need to unmock what is setup
        // when(cifAsset.getCharacteristics()).thenReturn(new ArrayList<CIFAssetCharacteristic>());
        clearMockedCharacteristics () ;

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        // Check responses
        assertThat(specialBidCharacteristics.size(), is(0));
    }

    @Test
    public void shouldGetCharacteristicNameFromTemplateResponse() {
        // Build the response this test tests against
        final String attributeName = "AttributeName";
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withPrimaryMandatoryAttribute(new MandatoryAttributeBuilder().withName(attributeName).build());
        when(templateClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request(GROUP_NAME, CONFIG_TYPE, TEMPLATE_NAME)))
            .thenReturn(templateDetailsResponseBuilder.build());

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        // Check responses
        assertThat(specialBidCharacteristics.get(0).getName(), is(attributeName));
    }

    @Test
    public void shouldNotGetCharacteristicNameFromTemplateResponseIfPrimaryAndCommonInfoIsNull() {
        // Build the response this test tests against
        final String attributeName = "AttributeName";
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        when(templateClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request(GROUP_NAME, CONFIG_TYPE, TEMPLATE_NAME)))
            .thenReturn(templateDetailsResponseBuilder.build());

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        // Check responses
        assertThat(specialBidCharacteristics.size(), is(0));
    }

    @Test
    public void shouldGetCharacteristicDisplayNameFromTemplateResponse() {
        // Build the response this test tests against
        final String attributeDisplayName = "AttributeDisplayName";
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withPrimaryMandatoryAttribute(new MandatoryAttributeBuilder().withDisplayName(attributeDisplayName).build());
        when(templateClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request(GROUP_NAME, CONFIG_TYPE, TEMPLATE_NAME)))
            .thenReturn(templateDetailsResponseBuilder.build());

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        // Check responses
        assertThat(specialBidCharacteristics.get(0).getAttributeDetail().getDisplayName(), is(attributeDisplayName));
    }

    @Test
    public void shouldGetDefaultValueTemplateResponse() {
        // Build the response this test tests against
        final String defaultValue = "AttribbuteDefaultValue";
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withPrimaryMandatoryAttribute(new MandatoryAttributeBuilder().withDefaultValue(defaultValue).build());
        when(templateClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request(GROUP_NAME, CONFIG_TYPE, TEMPLATE_NAME)))
            .thenReturn(templateDetailsResponseBuilder.build());

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        // Check responses
        assertThat(specialBidCharacteristics.get(0).getValue(), is(defaultValue));
    }

    @Test
    public void shouldGetTemplateAttributeValueFromTPERequest() {
        final String attributeName = "name";
        final String attributeValue = "attributeValue";
        TpeRequestDTO.TpeMandatoryAttributesDTO anAttribute = new TpeRequestDTO.TpeMandatoryAttributesDTO("id",
                                                                                                          attributeName,
                                                                                                          "type",
                                                                                                          attributeValue,
                                                                                                          ROW_ZERO,
                                                                                                          TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.PRIMARY);
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withPrimaryMandatoryAttribute(new MandatoryAttributeBuilder().withName(attributeName).build());
        when(templateClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request(GROUP_NAME, CONFIG_TYPE, TEMPLATE_NAME)))
            .thenReturn(templateDetailsResponseBuilder.build());
        tpeRequest.tpeMandatoryAttributesDTOCollection = newArrayList(anAttribute);

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);


        // Assert the expected characteristic with the expected value has been added
        assertThat(specialBidCharacteristics.get(0).getValue(), is(attributeValue));
    }

    @Test
    public void shouldNotGetTemplateAttributeValueFromTPERequestIfNamesDoNotMatch() {
        final String attributeName = "name1";
        final String notTheAttributeName = "name2";
        final String attributeValue = "attributeValue";
        TpeRequestDTO.TpeMandatoryAttributesDTO anAttribute = new TpeRequestDTO.TpeMandatoryAttributesDTO("id",
                                                                                                          attributeName,
                                                                                                          "type",
                                                                                                          attributeValue,
                                                                                                          ROW_ZERO,
                                                                                                          TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.PRIMARY);
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withPrimaryMandatoryAttribute(new MandatoryAttributeBuilder().withName(notTheAttributeName).build());
        when(templateClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request(GROUP_NAME, CONFIG_TYPE, TEMPLATE_NAME)))
            .thenReturn(templateDetailsResponseBuilder.build());
        tpeRequest.tpeMandatoryAttributesDTOCollection = newArrayList(anAttribute);

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        // Assert the expected characteristic with null value has been added
        assertThat(specialBidCharacteristics.get(0).getValue(), nullValue());
    }

    @Test
    public void shouldNotGetTemplateAttributeValueFromTPERequestIfClassifierDoesNotMatch() {
        final String attributeName = "name1";
        final String attributeValue = "attributeValue";
        TpeRequestDTO.TpeMandatoryAttributesDTO anAttribute = new TpeRequestDTO.TpeMandatoryAttributesDTO("id",
                                                                                                          attributeName,
                                                                                                          "type",
                                                                                                          attributeValue,
                                                                                                          ROW_ZERO,
                                                                                                          TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.PRIMARY);
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withCommonMandatoryAttribute(GROUP_ZERO, ROW_ZERO, ATTRIBUTE_ZERO, new MandatoryAttributeBuilder().withName(attributeName).build());
        when(templateClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request(GROUP_NAME, CONFIG_TYPE, TEMPLATE_NAME)))
            .thenReturn(templateDetailsResponseBuilder.build());
        tpeRequest.tpeMandatoryAttributesDTOCollection = newArrayList(anAttribute);

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        // Assert the expected characteristic with null value has been added
        assertThat(specialBidCharacteristics.get(0).getValue(), nullValue());
    }

    @Test
    public void shouldNotLoadSpecialBidCharacteristicsIfTemplateNameNotSelected() {
        CIFAssetCharacteristic templateNameCharacteristic= new CIFAssetCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME, null, true);
        mockCharacteristics(cifAsset, templateNameCharacteristic);

        // Run the method under test
        final List<CIFAssetCharacteristic> specialBidCharacteristics = templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest);

        assertThat(specialBidCharacteristics.size(), is(0));
    }
}