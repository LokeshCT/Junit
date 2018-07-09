package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UserDetails;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UserDetailsManager;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.extenders.MandatoryAttributeBuilder;
import com.bt.rsqe.customerinventory.service.extenders.SpecialBidMandatoryAttributeProvider;
import com.bt.rsqe.customerinventory.service.extenders.SpecialBidWellKnownAttributeProvider;
import com.bt.rsqe.customerinventory.service.extenders.TemplateDetailsResponseBuilder;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.tpe.client.TemplateTpeClient;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Request;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class SpecialBidCharacteristicsCreationUpdaterTest {

    private SpecialBidReservedAttributesHelper specialBidReservedAttributesHelper = new SpecialBidReservedAttributesHelper();
    private SpecialBidTemplateAttributeMapper specialBidTemplateAttributeMapper = new SpecialBidTemplateAttributeMapper();
    private SpecialBidWellKnownAttributeMapper wellKnownAttributeMapper = new SpecialBidWellKnownAttributeMapper();
    private SpecialBidWellKnownAttributeProvider wellKnownAttributeProvider = new SpecialBidWellKnownAttributeProvider("templateSelectionGuideUrl", specialBidReservedAttributesHelper);
    private SpecialBidMandatoryAttributeProvider mandatoryAttributeProvider = new SpecialBidMandatoryAttributeProvider();

    @Test
    public void shouldCreateAndSaveSpecialBidCharacteristics() {
        //Given
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        TemplateTpeClient templateTpeClient = mock(TemplateTpeClient.class);
        ExternalAttributesHelper externalAttributesHelper = mock(ExternalAttributesHelper.class);
        UserDetailsManager.set(new UserDetails("aToken", "aLoginName", false));
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CIFAssetQuoteOptionItemDetail optionItemDetail = new CIFAssetQuoteOptionItemDetail(null, 1, false, false, "USD", "12", false, null, null, null, null, false, ProductCategoryCode.NIL, null, false);
        CIFAssetOfferingDetail offeringDetail = new CIFAssetOfferingDetail("BaseProductName", "BaseProductName", "GroupName", "BaseLegacyId", true, true, "Proposition", true, true, null);

        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        SpecialBidAttributesCreationRequest creationRequest = new SpecialBidAttributesCreationRequest(assetKey);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID(assetKey.getAssetId()).withVersion(assetKey.getAssetVersion()).with(optionItemDetail)
                .with(offeringDetail).withContractTerm("12")
                .withCharacteristic(SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes")
                .withCharacteristic(ProductOffering.CONFIGURATION_TYPE_RESERVED_NAME, "aConfigType")
                .withCharacteristic(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME, "aTemplateName").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, SiteDetail, AttributeDetails)))).thenReturn(cifAsset);
        when(externalAttributesHelper.getAttributes(cifAsset)).thenReturn(tpeRequestDTO);

        final String attributeDisplayName = "AttributeDisplayName";
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withCommonMandatoryAttribute(0, 0, 0, new MandatoryAttributeBuilder().withName("aCommonAttribute").build()).withPrimaryMandatoryAttribute(new MandatoryAttributeBuilder().withName("aPrimaryAttribute").withDisplayName(attributeDisplayName).build());
        when(templateTpeClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request("GroupName", "aConfigType", "aTemplateName")))
                .thenReturn(templateDetailsResponseBuilder.build());

        //When
        SpecialBidCharacteristicsCreationUpdater creationUpdater = new SpecialBidCharacteristicsCreationUpdater(cifAssetOrchestrator, templateTpeClient, externalAttributesHelper,
                specialBidReservedAttributesHelper, specialBidTemplateAttributeMapper, wellKnownAttributeMapper, wellKnownAttributeProvider, mandatoryAttributeProvider);
        creationUpdater.performUpdate(creationRequest);

        //verify
        ArgumentCaptor<TpeRequestDTO> argumentCaptor = ArgumentCaptor.forClass(TpeRequestDTO.class);
        verify(externalAttributesHelper, times(1)).saveAttributes(eq(cifAsset), argumentCaptor.capture());
        TpeRequestDTO requestDTO = argumentCaptor.getValue();

        assertThat(requestDTO.contractLength, is(12L));
        assertThat(requestDTO.customerValueCurrency, is("USD"));
//        assertThat(requestDTO.tier, is(""));
//        assertThat(requestDTO.tier, is(SpecialBid));

        List<TpeRequestDTO.TpeMandatoryAttributesDTO> tpeMandatoryAttributesDTOCollection = tpeRequestDTO.tpeMandatoryAttributesDTOCollection;
        TpeRequestDTO.TpeMandatoryAttributesDTO commonAttribute = tpeMandatoryAttributesDTOCollection.get(0);
        assertThat(commonAttribute.getAttributeName(), is("aCommonAttribute"));
        assertThat(commonAttribute.getAttributeClassifier(), is(TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.COMMON));

        TpeRequestDTO.TpeMandatoryAttributesDTO primaryAttribute = tpeMandatoryAttributesDTOCollection.get(1);
        assertThat(primaryAttribute.getAttributeName(), is("aPrimaryAttribute"));
        assertThat(primaryAttribute.getAttributeClassifier(), is(TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.PRIMARY));
    }

    @Test
    public void shouldCreateSpecialBidCharacteristicsAndMapAssetAndUserDetails() {
        //Given
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        TemplateTpeClient templateTpeClient = mock(TemplateTpeClient.class);
        ExternalAttributesHelper externalAttributesHelper = mock(ExternalAttributesHelper.class);
        UserDetailsManager.set(new UserDetails("aToken", "aLoginName", true));
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CIFAssetQuoteOptionItemDetail optionItemDetail = new CIFAssetQuoteOptionItemDetail(null, 1, false, false, "USD", "12", false, null, null, null, null, false, ProductCategoryCode.NIL, null, false);
        CIFAssetOfferingDetail offeringDetail = new CIFAssetOfferingDetail("BaseProductName", "BaseProductName", "GroupName", "BaseLegacyId", true, true, "Proposition", true, true, null);

        TpeRequestDTO tpeRequestDTO = new TpeRequestDTO();
        SpecialBidAttributesCreationRequest creationRequest = new SpecialBidAttributesCreationRequest(assetKey);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID(assetKey.getAssetId()).withVersion(assetKey.getAssetVersion()).with(optionItemDetail)
                .with(offeringDetail).withContractTerm("12")
                .withCharacteristic("Template WIKI", "aTemplateWiki")
                .withCharacteristic("aCommonAttribute", "someCommonAttributeValue")
                .withCharacteristic(SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes")
                .withCharacteristic(SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes")
                .withCharacteristic(ProductOffering.CONFIGURATION_TYPE_RESERVED_NAME, "aConfigType")
                .withCharacteristic(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME, "aTemplateName").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, SiteDetail, AttributeDetails)))).thenReturn(cifAsset);
        when(externalAttributesHelper.getAttributes(cifAsset)).thenReturn(tpeRequestDTO);

        final String attributeDisplayName = "AttributeDisplayName";
        final TemplateDetailsResponseBuilder templateDetailsResponseBuilder = new TemplateDetailsResponseBuilder();
        templateDetailsResponseBuilder.withCommonMandatoryAttribute(0, 0, 0, new MandatoryAttributeBuilder().withName("aCommonAttribute").build())
                .withCommonMandatoryAttribute(1, 0, 0, new MandatoryAttributeBuilder().withName("aCommonAttributeWithDefaultValue").withDefaultValue("defaultValue1").build())
                .withPrimaryMandatoryAttribute(new MandatoryAttributeBuilder().withName("aPrimaryAttribute").withDisplayName(attributeDisplayName).build())
                .withPrimaryMandatoryAttribute(new MandatoryAttributeBuilder().withName("aPrimaryAttWithDefaultValue").withDisplayName(attributeDisplayName).withDefaultValue("defaultValue2").build());
        when(templateTpeClient.SQE_TPE_TemplateDetails(new TPE_TemplateDetails_Request("GroupName", "aConfigType", "aTemplateName")))
                .thenReturn(templateDetailsResponseBuilder.build());

        //When
        SpecialBidCharacteristicsCreationUpdater creationUpdater = new SpecialBidCharacteristicsCreationUpdater(cifAssetOrchestrator, templateTpeClient, externalAttributesHelper,
                specialBidReservedAttributesHelper, specialBidTemplateAttributeMapper, wellKnownAttributeMapper, wellKnownAttributeProvider, mandatoryAttributeProvider);
        creationUpdater.performUpdate(creationRequest);

        //verify
        ArgumentCaptor<TpeRequestDTO> argumentCaptor = ArgumentCaptor.forClass(TpeRequestDTO.class);
        verify(externalAttributesHelper, times(1)).saveAttributes(eq(cifAsset), argumentCaptor.capture());
        TpeRequestDTO requestDTO = argumentCaptor.getValue();

        assertThat(requestDTO.contractLength, is(12L));
        assertThat(requestDTO.customerValueCurrency, is("USD"));
        assertThat(requestDTO.tier, is("Non-Standard"));
        assertThat(requestDTO.templateWiki, is("aTemplateWiki"));

        List<TpeRequestDTO.TpeMandatoryAttributesDTO> tpeMandatoryAttributesDTOCollection = tpeRequestDTO.tpeMandatoryAttributesDTOCollection;
        TpeRequestDTO.TpeMandatoryAttributesDTO commonAttribute = tpeMandatoryAttributesDTOCollection.get(0);
        assertThat(commonAttribute.getAttributeName(), is("aCommonAttribute"));
        assertThat(commonAttribute.getAttributeClassifier(), is(TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.COMMON));
        assertThat(commonAttribute.getAttributeValue(), is("someCommonAttributeValue"));

        TpeRequestDTO.TpeMandatoryAttributesDTO commonAttributeWithDefaultValue = tpeMandatoryAttributesDTOCollection.get(1);
        assertThat(commonAttributeWithDefaultValue.getAttributeName(), is("aCommonAttributeWithDefaultValue"));
        assertThat(commonAttributeWithDefaultValue.getAttributeClassifier(), is(TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.COMMON));
        assertThat(commonAttributeWithDefaultValue.getAttributeValue(), is("defaultValue1"));

        TpeRequestDTO.TpeMandatoryAttributesDTO primaryAttribute = tpeMandatoryAttributesDTOCollection.get(2);
        assertThat(primaryAttribute.getAttributeName(), is("aPrimaryAttribute"));
        assertThat(primaryAttribute.getAttributeClassifier(), is(TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.PRIMARY));
        assertThat(primaryAttribute.getAttributeValue(), is(""));

        TpeRequestDTO.TpeMandatoryAttributesDTO primaryAttributeWithDefaultValue = tpeMandatoryAttributesDTOCollection.get(3);
        assertThat(primaryAttributeWithDefaultValue.getAttributeName(), is("aPrimaryAttWithDefaultValue"));
        assertThat(primaryAttributeWithDefaultValue.getAttributeClassifier(), is(TpeRequestDTO.TpeMandatoryAttributesDTO.AttributeClassifier.PRIMARY));
        assertThat(primaryAttributeWithDefaultValue.getAttributeValue(), is("defaultValue2"));
    }


    @Test
    public void shouldNotCreateAndSaveSpecialBidCharacteristicsWhenAnAssetIsStandard() {
        //Given
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        TemplateTpeClient templateTpeClient = mock(TemplateTpeClient.class);
        ExternalAttributesHelper externalAttributesHelper = mock(ExternalAttributesHelper.class);
        UserDetailsManager.set(new UserDetails("aToken", "aLoginName", false));
        AssetKey assetKey = new AssetKey("anAssetId", 1L);

        SpecialBidAttributesCreationRequest creationRequest = new SpecialBidAttributesCreationRequest(assetKey);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID(assetKey.getAssetId()).withVersion(assetKey.getAssetVersion())
                .withCharacteristic(SPECIAL_BID_ATTRIBUTE_INDICATOR, "No").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, SiteDetail, AttributeDetails)))).thenReturn(cifAsset);

        //When
        SpecialBidCharacteristicsCreationUpdater creationUpdater = new SpecialBidCharacteristicsCreationUpdater(cifAssetOrchestrator, templateTpeClient, externalAttributesHelper,
                specialBidReservedAttributesHelper, specialBidTemplateAttributeMapper, wellKnownAttributeMapper, wellKnownAttributeProvider, mandatoryAttributeProvider);
        creationUpdater.performUpdate(creationRequest);

        //verify
        verify(externalAttributesHelper, times(0)).saveAttributes(any(CIFAsset.class), any(TpeRequestDTO.class));

    }

    @Test
    public void shouldNotCreateAndSaveSpecialBidCharacteristicsWhenAnAssetIsNonStandardButNoTemplateNameSet() {
        //Given
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        TemplateTpeClient templateTpeClient = mock(TemplateTpeClient.class);
        ExternalAttributesHelper externalAttributesHelper = mock(ExternalAttributesHelper.class);
        UserDetailsManager.set(new UserDetails("aToken", "aLoginName", false));
        AssetKey assetKey = new AssetKey("anAssetId", 1L);

        SpecialBidAttributesCreationRequest creationRequest = new SpecialBidAttributesCreationRequest(assetKey);
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID(assetKey.getAssetId()).withVersion(assetKey.getAssetVersion())
                .withCharacteristic(SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").withCharacteristic(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME, "").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, SiteDetail, AttributeDetails)))).thenReturn(cifAsset);

        //When
        SpecialBidCharacteristicsCreationUpdater creationUpdater = new SpecialBidCharacteristicsCreationUpdater(cifAssetOrchestrator, templateTpeClient, externalAttributesHelper,
                specialBidReservedAttributesHelper, specialBidTemplateAttributeMapper, wellKnownAttributeMapper, wellKnownAttributeProvider, mandatoryAttributeProvider);
        creationUpdater.performUpdate(creationRequest);

        //verify
        verify(externalAttributesHelper, times(0)).saveAttributes(any(CIFAsset.class), any(TpeRequestDTO.class));

    }


}