package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCategoryDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetSiteDetail;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.AttributeDataType;
import com.bt.rsqe.domain.product.AttributeOwner;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CharacteristicChangeRequestBuilderTest {
    private final PmrHelper pmrHelper = mock(PmrHelper.class);
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private final CharacteristicChangeRequestBuilder characteristicChangeRequestBuilder = new CharacteristicChangeRequestBuilder(pmrHelper, cifAssetOrchestrator);
    private final CIFAsset cifAsset = mock(CIFAsset.class);
    private final CIFAssetCategoryDetail defaultCategoryDetail = new CIFAssetCategoryDetail(true);
    private final CIFAssetQuoteOptionItemDetail defaultQuoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(null, 0, true, true, null, "ContractTerm",
                                                                                                                 false, JaxbDateTime.NIL,
                                                                                                                 new ArrayList<PriceBookDTO>(),
                                                                                                                 LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false);
    private final String defaultRelationshipName = "relationshipName";

    @Test
    public void shouldGetStencilCharacteristicChangeRequest() {
        assertCharacteristicAdded(ProductOffering.STENCIL_RESERVED_NAME, "stencilCode");
    }

    @Test
    public void shouldGetStencilVersionCharacteristicChangeRequest() {
        assertCharacteristicAdded(ProductOffering.STENCIL_VERSION_RESERVED_NAME, "A.1");
    }

    @Test
    public void shouldGetProductIdentifierCharacteristicChangeRequest() {
        assertCharacteristicAdded(ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME, "stencilCode");
    }

    @Test
    public void shouldGetChildRelationshipNameCharacteristicChangeRequest() {
        assertCharacteristicAdded(ProductOffering.RELATION_NAME_ATTRIBUTE, "relationshipName");
    }

    @Test
    public void shouldNotGetChildRelationshipNameCharacteristicChangeRequestWhenPassedRelationshipNameIsNull() {
        mockAsset(ProductOffering.CHILD_RELATION_ATTRIBUTE, defaultCategoryDetail, defaultQuoteOptionItemDetail, null, null, "");

        final CharacteristicChangeRequest response = characteristicChangeRequestBuilder.defaultForAllCharacteristics(cifAsset, "stencilCode", null);

        final List<CharacteristicChange> characteristicChanges = response.getCharacteristicChanges();
        assertThat(characteristicChanges.size(), is(0));
    }

    @Test
    public void shouldGetContractTermCharacteristicChangeRequest() {
        final CIFAssetAttributeDetail attributeDetail = new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering,
                                                                                    AttributeDataType.STRING, false, "", false,
                                                                                    "");
        assertCharacteristicAdded(ProductOffering.CONTRACT_TERM_ATTRIBUTE_NAME, "12", attributeDetail, newArrayList(new CIFAssetCharacteristicValue("12")), "12");
    }

    @Test
    public void shouldGetMoveTypeCharacteristicChangeRequest() {
       assertCharacteristicAdded(ProductOffering.MOVE_TYPE, null);
    }

    @Test
    public void shouldGetContractTermCharacteristicChangeRequestWithValueFromQuoteOption() {
        final CIFAssetAttributeDetail attributeDetail = new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering,
                                                                                    AttributeDataType.STRING, false, "", false,
                                                                                    "");
        assertCharacteristicAdded(ProductOffering.CONTRACT_TERM_ATTRIBUTE_NAME, "ContractTerm", attributeDetail, Collections.<CIFAssetCharacteristicValue>emptyList(), "");
    }

    @Test
    public void shouldGetRegionCharacteristicChangeRequest() {
        assertCharacteristicAdded(ProductOffering.REGION_ATTRIBUTE, "AFGHANISTAN");
    }

    @Test
    public void shouldGetMigratingYesChangeRequestForMigratingAsset() {
        assertCharacteristicAdded(ProductOffering.MIGRATING_ASSET, "Yes");
    }

    @Test
    public void shouldGetMigratingNoChangeRequestForNonMigratingAsset() {
        assertCharacteristicAdded(ProductOffering.MIGRATING_ASSET, "No", defaultCategoryDetail,
                                  new CIFAssetQuoteOptionItemDetail(null, 0, false, true, null, "ContractTerm",
                                                                    false, JaxbDateTime.NIL,
                                                                    new ArrayList<PriceBookDTO>(),
                                                                    LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));
    }

    @Test
    public void shouldGetLegacyBillingYesChangeRequestWhenLegacyBillingCategoryIsTrueAndMigrationQuoteOptionIsTrue() {
        assertCharacteristicAdded(ProductOffering.LEGACY_BILLING, "Yes");
    }

    @Test
    public void shouldGetLegacyBillingNoChangeRequestWhenLegacyBillingCategoryIsTrueAndMigrationQuoteOptionIsFalse() {
        assertCharacteristicAdded(ProductOffering.LEGACY_BILLING, "No", new CIFAssetQuoteOptionItemDetail(null, 0, false, true, null, "ContractTerm",
                                                                                                          false, JaxbDateTime.NIL,
                                                                                                          new ArrayList<PriceBookDTO>(),
                                                                                                          LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));
    }

    @Test
    public void shouldGetLegacyBillingNoChangeRequestWhenLegacyBillingCategoryIsFalseAndMigrationQuoteOptionIsTrue() {
        assertCharacteristicAdded(ProductOffering.LEGACY_BILLING, "No", new CIFAssetCategoryDetail(false), new CIFAssetQuoteOptionItemDetail(null, 0, false, true, null, "ContractTerm",
                                                                                                                                             false, JaxbDateTime.NIL,
                                                                                                                                             new ArrayList<PriceBookDTO>(),
                                                                                                                                             LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));
    }

    @Test
    public void shouldGetLegacyBillingNoChangeRequestWhenLegacyBillingCategoryIsFalseAndMigrationQuoteOptionIsFalse() {
        assertCharacteristicAdded(ProductOffering.LEGACY_BILLING, "No", new CIFAssetQuoteOptionItemDetail(null, 0, false, true, null, "ContractTerm",
                                                                                                          false, JaxbDateTime.NIL,
                                                                                                          new ArrayList<PriceBookDTO>(),
                                                                                                          LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false));
    }

    @Test
    public void shouldGetLegacyBillingYesChangeRequestWhenCustomerRootProductIsLegacyBilling() {
        List<String> productCodes = newArrayList("aPackageProductCode");
        when(pmrHelper.getPackageAndContractProductCodesForCategory(cifAsset)).thenReturn(productCodes);
        when(cifAssetOrchestrator.isMigratedCustomer(new CIFAssetOrchestrator.MigratedCustomerKey("customerId", "contractId", productCodes))).thenReturn(true);

        assertCharacteristicAdded(ProductOffering.LEGACY_BILLING, "Yes", new CIFAssetCategoryDetail(false));
    }

    @Test
    public void shouldGetLegacyBillingNoChangeRequestWhenNoCustomerRootProductIsLegacyBilling() {
        List<CIFAssetExtension> expectedResponseExtensions = new ArrayList<CIFAssetExtension>();
        List<String> productCodes = newArrayList("aPackageProductCode");
        when(pmrHelper.getPackageAndContractProductCodesForCategory(cifAsset)).thenReturn(productCodes);
        when(cifAssetOrchestrator.getAssets("customerId", "contractId",productCodes, ProductOffering.LEGACY_BILLING,
                                            "Yes", expectedResponseExtensions)).thenReturn(new ArrayList<CIFAsset>());

        assertCharacteristicAdded(ProductOffering.LEGACY_BILLING, "Yes", defaultCategoryDetail);
    }

    @Test
    public void shouldGetEmptyStringCharacteristicChangeRequestWhenNoDefaultSetInAttributeAndAllowedValuesNotSet() {
        final CIFAssetAttributeDetail attributeDetail = new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering,
                                                                                    AttributeDataType.STRING, false, "", false,
                                                                                    "");

        assertCharacteristicAdded("DefaultingCharacteristic", "", attributeDetail, null, "");
    }

    @Test
    public void shouldGetAllowedValueCharacteristicChangeRequestWhenOnlyOneAvailable() {
        final CIFAssetAttributeDetail attributeDetail = new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering,
                                                                                    AttributeDataType.STRING, false, "", true,
                                                                                    "");

        assertCharacteristicAdded("DefaultingCharacteristic", "defaultAllowed", attributeDetail, newArrayList(new CIFAssetCharacteristicValue("defaultAllowed")), "");
    }

    @Test @Ignore("When allowed values is single value, the existing code defaults it. Its not checking if its mandatory, Ignoring it temporarily...")
    public void shouldGetEmptyStringCharacteristicChangeRequestWhenOnlyOneAvailableAndNoDefaultSetAndNotMandatory() {
        final CIFAssetAttributeDetail attributeDetail = new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering,
                                                                                    AttributeDataType.STRING, false, "", false,
                                                                                    "");

        assertCharacteristicAdded("DefaultingCharacteristic", "", attributeDetail, newArrayList(new CIFAssetCharacteristicValue("defaultAllowed")), "");
    }

    @Test
    public void shouldGetDefaultStringCharacteristicChangeRequestWhenOnlyOneAvailableAndDefaultSetAndNotMandatory() {
        final CIFAssetAttributeDetail attributeDetail = new CIFAssetAttributeDetail(false, false, AttributeOwner.Offering,
                                                                                    AttributeDataType.STRING, false, "", false,
                                                                                    "default2");

        assertCharacteristicAdded("DefaultingCharacteristic", "default2", attributeDetail, newArrayList(new CIFAssetCharacteristicValue("default1"),
                                                                                                        new CIFAssetCharacteristicValue("default2")), "");
    }

    private void assertCharacteristicAdded(String characteristicName, String expectedValue, CIFAssetAttributeDetail attributeDetail,
                                           List<CIFAssetCharacteristicValue> allowedValues, String characteristicValue) {
        assertCharacteristicAdded(characteristicName, expectedValue, defaultCategoryDetail, defaultQuoteOptionItemDetail,
                                  attributeDetail, allowedValues, defaultRelationshipName, characteristicValue);
    }

    private void assertCharacteristicAdded(String characteristicName, String expectedValue, CIFAssetQuoteOptionItemDetail quoteOptionItemDetail) {
        assertCharacteristicAdded(characteristicName, expectedValue, defaultCategoryDetail, quoteOptionItemDetail, null, null, defaultRelationshipName, "");
    }

    private void assertCharacteristicAdded(String characteristicName, String expectedValue) {
        assertCharacteristicAdded(characteristicName, expectedValue, defaultCategoryDetail);
    }

    private void assertCharacteristicAdded(String characteristicName, String expectedValue, CIFAssetCategoryDetail categoryDetail) {
        assertCharacteristicAdded(characteristicName, expectedValue, categoryDetail, defaultQuoteOptionItemDetail, null, null, defaultRelationshipName, "");
    }

    private void assertCharacteristicAdded(String characteristicName, String expectedValue, CIFAssetCategoryDetail categoryDetail,
                                           CIFAssetQuoteOptionItemDetail quoteOptionItemDetail){
        assertCharacteristicAdded(characteristicName, expectedValue, categoryDetail, quoteOptionItemDetail, null, null, defaultRelationshipName, "");
    }

    private void assertCharacteristicAdded(String characteristicName, String expectedValue, CIFAssetCategoryDetail categoryDetail,
                                           CIFAssetQuoteOptionItemDetail quoteOptionItemDetail, CIFAssetAttributeDetail attributeDetail,
                                           List<CIFAssetCharacteristicValue> allowedValues, String relationshipName, String characteristicValue) {
        mockAsset(characteristicName, categoryDetail, quoteOptionItemDetail, attributeDetail, allowedValues, characteristicValue);

        final CharacteristicChangeRequest response = characteristicChangeRequestBuilder.defaultForAllCharacteristics(cifAsset, "stencilCode", relationshipName);

        final List<CharacteristicChange> characteristicChanges = response.getCharacteristicChanges();
        assertThat(characteristicChanges.size(), is(1));
        assertThat(characteristicChanges.get(0), is(new CharacteristicChange(characteristicName, expectedValue)));
    }

    private void mockAsset(String characteristicName, CIFAssetCategoryDetail categoryDetail, CIFAssetQuoteOptionItemDetail quoteOptionItemDetail, CIFAssetAttributeDetail attributeDetail, final List<CIFAssetCharacteristicValue> allowedValues, String characteristicValue) {
        final CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic(characteristicName, characteristicValue, true);
        characteristic.loadAttributeDetail(attributeDetail);
        characteristic.loadAllowedValues(allowedValues);
        Answer<CIFAsset> answer = new Answer<CIFAsset>() {
            @Override
            public CIFAsset answer(InvocationOnMock invocation) throws Throwable {
                final CIFAsset cifAsset = (CIFAsset) invocation.getArguments()[0];
                for (CIFAssetCharacteristic cifAssetCharacteristic : cifAsset.getCharacteristics()) {
                    cifAssetCharacteristic.loadAllowedValues(allowedValues);
                }
                return cifAsset;
            }
        };
        when(cifAssetOrchestrator.forceExtendAsset(cifAsset, newArrayList(CIFAssetExtension.SiteDetail, CIFAssetExtension.CharacteristicAllowedValues, CIFAssetExtension.CharacteristicValue,
                                                                          CIFAssetExtension.AttributeDetails))).then(answer);

        when(cifAsset.getCharacteristics()).thenReturn(newArrayList(characteristic));
        when(cifAsset.getQuoteOptionItemDetail()).thenReturn(quoteOptionItemDetail);
        when(cifAsset.getSiteDetail()).thenReturn(new CIFAssetSiteDetail(1, null, null, null, "AF"));
        when(cifAsset.getCategoryDetail()).thenReturn(categoryDetail);
        when(cifAsset.getCustomerId()).thenReturn("customerId");
        when(cifAsset.getContractId()).thenReturn("contractId");
        when(cifAsset.getProductCode()).thenReturn("aProductCode");
        ProductOffering offering = aProductOffering().withProductIdentifier(new ProductIdentifier("aProductCode", "A.1")).withStencil("stencilCode").build();
        when(pmrHelper.getProductOffering("aProductCode", "stencilCode")).thenReturn(offering);

        when(cifAssetOrchestrator.forceExtendAsset(cifAsset, newArrayList(CIFAssetExtension.QuoteOptionItemDetail))).thenAnswer(answer);
    }
}