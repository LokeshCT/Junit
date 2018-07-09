package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.customerinventory.filter.AssetFilter;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.AvailableAsset;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilInfo;
import com.bt.rsqe.domain.bom.fixtures.AttributeFixture;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.bom.parameters.ProductName;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Cardinality;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.extensions.CardinalityExpression;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.projectengine.DeliveryAddressDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.AssetKeyContainer;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.domain.product.fixtures.RuleCalculatedAttributeSourceFixture.*;
import static com.google.common.collect.Lists.*;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ProductBasedImporterTest extends ECRFImporterTest {

    private ProductInstance productInstance;
    private ProductOffering rootOffering;

    @Before
    public void setUp() throws IOException {
        before();
    }

    @Test
    public void shouldImportAttributesAndCreateProductInstance() throws Exception {
        buildRootProductInstance();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = productBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(),
                                                                                 productInstance.getContractTerm(), productInstance.getProjectId(),
                                                                                 productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(),
                                                                                 productInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode(""));

        verify(quoteOptionClient).createQuoteOptionItem(eq(productInstance.getProjectId()), eq(productInstance.getQuoteOptionId()), anyString(), anyString(), eq(ROOT_PRODUCT_CODE_IMPORTABLE), eq(contractDTO), anyString(), eq(new ProductCategoryCode("")));
        verify(productInstanceClient, times(1)).createProductInstance(eq(productInstance.getProductIdentifier().getProductId()),
                                                                      eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                                      anyString(),
                                                                      eq(ROOT_SITE_ID),
                                                                      eq(productInstance.getCustomerId()),
                                                                      eq(productInstance.getContractId()),
                                                                      eq(productInstance.getQuoteOptionId()),
                                                                      eq(StencilId.NIL),
                                                                      eq(productInstance.getProjectId()),
                                                                      eq(productInstance.getContractTerm()),
                                                                      eq(quoteOptionClient), any(ProductCategoryCode.class));


        verify(productInstanceClient).put(productInstance);
        verify(productInstanceClient).refreshAttributesOfProductInstance(productInstance);
        assertFalse(importResults.hasErrors());
        assertThat(lineItemsImpacted.size(), is(1));
    }

    @Test
    public void shouldImportAttributesAndCreateProductInstanceUsingSiteName() throws Exception {
        buildRootProductInstance();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_NAME),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, "myValue")))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = productBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(),
                                                                                 productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(),
                                                                                 Optional.<LineItemId>absent(), false, new ProductCategoryCode(""));

        verify(quoteOptionClient).createQuoteOptionItem(eq(productInstance.getProjectId()), eq(productInstance.getQuoteOptionId()), anyString(), anyString(), eq(ROOT_PRODUCT_CODE_IMPORTABLE), eq(contractDTO), anyString(), eq(new ProductCategoryCode("")));
        verify(productInstanceClient, times(1)).createProductInstance(eq(productInstance.getProductIdentifier().getProductId()),
                                                                      eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                                      anyString(),
                                                                      eq(ROOT_SITE_ID),
                                                                      eq(productInstance.getCustomerId()),
                                                                      eq(productInstance.getContractId()),
                                                                      eq(productInstance.getQuoteOptionId()),
                                                                      eq(StencilId.NIL),
                                                                      eq(productInstance.getProjectId()),
                                                                      eq(productInstance.getContractTerm()),
                                                                      eq(quoteOptionClient), any(ProductCategoryCode.class));


        verify(productInstanceClient).put(productInstance);
        verify(productInstanceClient).refreshAttributesOfProductInstance(productInstance);
        assertFalse(importResults.hasErrors());
        assertThat(lineItemsImpacted.size(), is(1));
        assertThat(productInstance.getInstanceCharacteristicValue(ATTRIBUTE_WITH_SOURCE_RULE),is("myValue"));
    }

    @Test
    public void shouldImportAttributesAndCreateProductInstanceForDifferentSites() throws Exception {
        String secondRowSite = "23456";
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();

        ProductOffering rootOffering = productOfferingFixture.build();
        ProductInstance newProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("lineItemId").withProductInstanceId("new")
                                                                          .withSiteId(ROOT_SITE_ID)
                                                                          .withProductOffering(productOfferingFixture.withAttribute(ATTRIBUTE_NAME).withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                                                  .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                                                                                  .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                                                                                  .build())
                                                                                                                     .withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                    .called(ATTRIBUTE_WITH_SOURCE_RULE)
                                                                                                                                                    .withMaxLength(5)
                                                                                                                                                    .withMinLength(5)
                                                                                                                                                    .withAttributeSourceRule(aCalculatedAttributeSourceRule().forAttribute(ATTRIBUTE_WITH_SOURCE_RULE).withExpression("'A'").build())
                                                                                                                                                    .build()))
                                                                          .build();

        ProductInstance newProductInstance1 = DefaultProductInstanceFixture.aProductInstance().withLineItemId("anotherLineItemId").withProductInstanceId("new1")
                                                                           .withSiteId(secondRowSite)
                                                                           .withProductOffering(productOfferingFixture.withAttribute(ATTRIBUTE_NAME).withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                                                   .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                                                                                   .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                                                                                   .build())
                                                                                                                      .withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                     .called(ATTRIBUTE_WITH_SOURCE_RULE)
                                                                                                                                                     .withMaxLength(5)
                                                                                                                                                     .withMinLength(5)
                                                                                                                                                     .withAttributeSourceRule(aCalculatedAttributeSourceRule().forAttribute(ATTRIBUTE_WITH_SOURCE_RULE).withExpression("'A'").build())
                                                                                                                                                     .build()))
                                                                           .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(newProductInstance.getCustomerId())).thenReturn(siteResource);
        siteDtos.add(SiteDTOFixture.aSiteDTO().withBfgSiteId(secondRowSite).withName("Site 2").build());
        when(siteResource.get(newProductInstance.getProjectId(), SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(configuratorClient.createContractDto(newProductInstance.getContractTerm(), newProductInstance.getCustomerId(), ROOT_PRODUCT_CODE_IMPORTABLE, newProductInstance.getContractId(), "token")).thenReturn(contractDTO);
        when(productInstanceClient.getByAssetKey(newProductInstance.getKey())).thenReturn(newProductInstance);
        when(productInstanceClient.getByAssetKey(newProductInstance1.getKey())).thenReturn(newProductInstance1);


        when(productInstanceClient.createProductInstance(eq(newProductInstance.getProductIdentifier().getProductId()),
                                                         eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(ROOT_SITE_ID),
                                                         eq(newProductInstance.getCustomerId()),
                                                         eq(newProductInstance.getContractId()),
                                                         eq(newProductInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(newProductInstance.getProjectId()),
                                                         eq(newProductInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class))).thenReturn(newProductInstance);

        when(productInstanceClient.createProductInstance(eq(newProductInstance1.getProductIdentifier().getProductId()),
                                                         eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(secondRowSite),
                                                         eq(newProductInstance1.getCustomerId()),
                                                         eq(newProductInstance1.getContractId()),
                                                         eq(newProductInstance1.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(newProductInstance1.getProjectId()),
                                                         eq(newProductInstance1.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class))).thenReturn(newProductInstance1);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, secondRowSite),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = productBasedImporter.importFromSheet(newProductInstance.getCustomerId(), newProductInstance.getContractId(), newProductInstance.getContractTerm(), newProductInstance.getProjectId(), newProductInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), newProductInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode("123"));

        verify(quoteOptionClient, times(2)).createQuoteOptionItem(eq(newProductInstance.getProjectId()), eq(newProductInstance.getQuoteOptionId()), anyString(), anyString(), eq(ROOT_PRODUCT_CODE_IMPORTABLE), eq(contractDTO), anyString(), any(ProductCategoryCode.class));
        verify(productInstanceClient, times(1)).createProductInstance(eq(newProductInstance1.getProductIdentifier().getProductId()),
                                                                      eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                                      anyString(),
                                                                      eq(ROOT_SITE_ID),
                                                                      eq(newProductInstance1.getCustomerId()),
                                                                      eq(newProductInstance1.getContractId()),
                                                                      eq(newProductInstance1.getQuoteOptionId()),
                                                                      eq(StencilId.NIL),
                                                                      eq(newProductInstance1.getProjectId()),
                                                                      eq(newProductInstance1.getContractTerm()),
                                                                      eq(quoteOptionClient), any(ProductCategoryCode.class));

        verify(productInstanceClient, times(1)).createProductInstance(eq(newProductInstance.getProductIdentifier().getProductId()),
                                                                      eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                                      anyString(),
                                                                      eq(secondRowSite),
                                                                      eq(newProductInstance.getCustomerId()),
                                                                      eq(newProductInstance.getContractId()),
                                                                      eq(newProductInstance.getQuoteOptionId()),
                                                                      eq(StencilId.NIL),
                                                                      eq(newProductInstance.getProjectId()),
                                                                      eq(newProductInstance.getContractTerm()),
                                                                      eq(quoteOptionClient), any(ProductCategoryCode.class));


        verify(productInstanceClient, times(1)).put(newProductInstance);
        verify(productInstanceClient, times(1)).put(newProductInstance1);

        verify(productInstanceClient).refreshAttributesOfProductInstance(newProductInstance);
        verify(productInstanceClient).refreshAttributesOfProductInstance(newProductInstance1);

        assertFalse(importResults.hasErrors());
        assertThat(lineItemsImpacted.size(), is(2));
    }

    @Test
    public void shouldThrowErrorIfSiteIdNotPresentInExpedio() throws Exception {
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();
        SiteResource siteResource = mock(SiteResource.class);

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        siteDtos = newArrayList(SiteDTOFixture.aSiteDTO().withBfgSiteId("3").withName("Site 3").build());
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        productBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), ROOT_PRODUCT_CODE_IMPORTABLE, Optional.<LineItemId>absent(), false, new ProductCategoryCode("123"));

        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.siteIdNotFoundForCustomer, ROOT_SITE_ID));
    }

    @Test
    public void shouldThrowErrorIfSiteNameNotPresentInExpedio() throws Exception {
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();
        SiteResource siteResource = mock(SiteResource.class);

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        siteDtos = newArrayList(SiteDTOFixture.aSiteDTO().withBfgSiteId("3").withName("Site 3").build());
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_NAME),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        productBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), ROOT_PRODUCT_CODE_IMPORTABLE, Optional.<LineItemId>absent(), false, new ProductCategoryCode("123"));

        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.siteIdNotFoundForCustomer, ROOT_SITE_NAME));
    }

    @Test
    public void shouldThrowErrorIfSiteIdColumnNotPresentInSheetInBulkImport() throws Exception {
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();
        SiteResource siteResource = mock(SiteResource.class);

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withSheetName(ROOT_SHEET_NAME)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();

        ImportResults importResults = new ImportResults();
        productBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), ROOT_PRODUCT_CODE_IMPORTABLE, Optional.<LineItemId>absent(), false, new ProductCategoryCode("123"));

        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.attributeNotFoundInWorkSheet, SITE_ID, ROOT_SHEET_NAME));
    }

    @Test
    public void shouldThrowErrorIfSiteIdColumnPresentAndValueIsEmptyInBulkImport() throws Exception {
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();
        SiteResource siteResource = mock(SiteResource.class);

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withSheetName(ROOT_SHEET_NAME)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ""),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        productBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), ROOT_PRODUCT_CODE_IMPORTABLE, Optional.<LineItemId>absent(), false, new ProductCategoryCode("123"));

        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.siteIdNotFoundInWorkSheet, ROOT_SHEET_NAME));
    }

    @Test
    public void shouldThrowErrorIfSiteIdColumnPresentAndValueIsNullInBulkImport() throws Exception {
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();
        SiteResource siteResource = mock(SiteResource.class);

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
            .withSheetIndex(1)
            .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withSheetName(ROOT_SHEET_NAME)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, "null"),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        productBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), ROOT_PRODUCT_CODE_IMPORTABLE, Optional.<LineItemId>absent(), false, new ProductCategoryCode("H123"));

        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.siteIdNotFoundInWorkSheet, ROOT_SHEET_NAME));
    }

    @Test
    public void shouldFailTheImportIfSiteCardinalityFailed() throws Exception {

        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withSiteCardinality(new Cardinality(0, 1, null)).withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();

        SiteResource siteResource = mock(SiteResource.class);
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);


        when(productInstanceClient.getApprovedAssets(eq(new SiteId(ROOT_SITE_ID)), eq(new ProductCode(ROOT_PRODUCT_CODE_IMPORTABLE)), eq(new ProductVersion("1")))).thenReturn(newArrayList(new AvailableAsset("assetId", 1L)));
        when(productInstanceClient.getDraftAssets(eq(new SiteId(ROOT_SITE_ID)), eq(new ProductCode(ROOT_PRODUCT_CODE_IMPORTABLE)), eq(new ProductVersion("1")), eq(quoteOptionId))).thenReturn(newArrayList(new AvailableAsset("assetId", 1L)));


        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        productBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), ROOT_PRODUCT_CODE_IMPORTABLE, Optional.<LineItemId>absent(), false, new ProductCategoryCode("H123"));

        assertImportResultContainsErrorMessage(importResults, String.format("Site Cardinality Failed - %s can have only %s instance(s) for the Customer.", "aProductName", "1"));
    }

    @Test
    public void shouldFailTheImportIfContractCardinalityFailed1() throws Exception {
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withContractCardinality(new Cardinality(0, 2, null)).withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProduct", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.getCentralSite(projectId)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());

        when(productInstanceClient.getContractAssets(any(CustomerId.class), any(ContractId.class), any(ProductCode.class), any(ProductVersion.class), any(AssetFilter.class),
                                                     any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset("anAssetId", 1L)));


        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        productBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), ROOT_PRODUCT_CODE_IMPORTABLE, Optional.<LineItemId>absent(), false, new ProductCategoryCode("H123"));

        assertImportResultContainsErrorMessage(importResults, "Contract Cardinality Failed - aProduct can have only 2 instance(s) for the Customer.");
    }

    //R37

    @Test
    public void shouldAlwaysStartImportWithSecondSheetInWorkBook() throws InstanceCharacteristicNotFound {

        //When
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(RELATED_PRODUCT_CODE_IMPORTABLE)
                                                                                                                                                     .withRelationType(RelationshipType.RelatedTo)
                                                                                                                                                     .withCardinalityExpression(CardinalityExpression.NIL))
                                                                       )
                                                                       .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.getCentralSite("DEFAULT-TEST-PROJECT-ID")).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);


        ProductInstance relatedProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("relatedLineItem")
                                                                              .withSiteId(null).withContractTerm(CONTRACT_TERM)
                                                                              .withProductOffering(ProductOfferingFixture
                                                                                                       .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                       .withAttribute("BULKUPLOADER")
                                                                                                       .withProductIdentifier(
                                                                                                           RELATED_PRODUCT_CODE_IMPORTABLE))
                                                                              .build();
        when(productOfferings2.get()).thenReturn(relatedProductInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(RELATED_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings2);
        when(productInstanceClient.get(Optional.of(new LineItemId("relatedLineItem")).get())).thenReturn(relatedProductInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID)))
                                                                                    .build())
                                                   .build();

        ECRFSheet ecrfSheetModelForRelatedProduct = ECRFModelFixture.aECRFModel()
                                                                    .withScode(RELATED_PRODUCT_CODE_IMPORTABLE)
                                                                    .withSheetName(RELATED_PRODUCT_SHEET_NAME)
                                                                    .withSheetIndex(2)
                                                                    .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                                    .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                     .withRowId(RELATED_PRODUCT_ROW_ID)
                                                                                                     .withAttributes(new ArrayList<ECRFSheetModelAttribute>())
                                                                                                     .build())
                                                                    .build();

        ECRFSheet ecrfSheetModelForRelatedMapping = ECRFModelFixture.aECRFModel()
                                                                    .withScode(RELATED_MAPPING)
                                                                    .withSheetName(RELATED_PRODUCT_MAPPING_SHEET)
                                                                    .withSheetIndex(3)
                                                                    .withSheetTypeStrategy(SheetTypeStrategy.Related)
                                                                    .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                     .withOwnerProductId(ROOT_PRODUCT_ROW_ID)
                                                                                                     .withRelatedToId(RELATED_PRODUCT_ROW_ID)
                                                                                                     .withRelationShipName("related1")
                                                                                                     .build())
                                                                    .build();

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForRelatedProduct, ecrfSheetModelForRelatedMapping)).withControlSheet().build();
        when(productInstanceClient.createProductInstance(eq(productInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(productInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(productInstance.getSiteId()),
                                                         eq(productInstance.getCustomerId()),
                                                         eq(productInstance.getContractId()),
                                                         eq(productInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(productInstance.getProjectId()),
                                                         eq(productInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class)))
            .thenReturn(productInstance);
        when(productInstanceClient.createProductInstance(eq(relatedProductInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(relatedProductInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(relatedProductInstance.getSiteId()),
                                                         eq(relatedProductInstance.getCustomerId()),
                                                         eq(relatedProductInstance.getContractId()),
                                                         eq(relatedProductInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(relatedProductInstance.getProjectId()),
                                                         eq(relatedProductInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class)))
            .thenReturn(relatedProductInstance);

        ImportResults importResults = new ImportResults();

        //When
        Set<LineItemId> lineItemIds = productBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), relatedProductInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode("H123"));

        //verify
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(productInstanceClient, times(1)).put(relatedProductInstance);
        assertFalse(importResults.hasErrors());
        Assert.assertThat(lineItemIds.size(), is(2));
    }

    @Test
    public void shouldCreateRelationShipWhenRelatedToMappingAvailableInWorkBook() throws InstanceCharacteristicNotFound {

        //When
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(RELATED_PRODUCT_CODE_IMPORTABLE)
                                                                                                                                                     .withRelationType(RelationshipType.RelatedTo)
                                                                                                                                                     .withCardinalityExpression(CardinalityExpression.NIL))
                                                                       )
                                                                       .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.getCentralSite("DEFAULT-TEST-PROJECT-ID")).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        doNothing().when(productRelationshipService).createRelations(any(ECRFSheet.class), any(AssetKeyContainer.class), any(ImportResults.class));


        ProductInstance relatedProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("relatedLineItem")
                                                                              .withSiteId(null).withContractTerm(CONTRACT_TERM)
                                                                              .withProductOffering(ProductOfferingFixture
                                                                                                       .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                       .withAttribute("BULKUPLOADER")
                                                                                                       .withProductIdentifier(
                                                                                                           RELATED_PRODUCT_CODE_IMPORTABLE))
                                                                              .build();
        when(productOfferings2.get()).thenReturn(relatedProductInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(RELATED_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings2);
        when(productInstanceClient.get(Optional.of(new LineItemId("relatedLineItem")).get())).thenReturn(relatedProductInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID)))
                                                                                    .build())
                                                   .build();

        ECRFSheet ecrfSheetModelForRelatedProduct = ECRFModelFixture.aECRFModel()
                                                                    .withScode(RELATED_PRODUCT_CODE_IMPORTABLE)
                                                                    .withSheetName(RELATED_PRODUCT_SHEET_NAME)
                                                                    .withSheetIndex(2)
                                                                    .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                                    .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                     .withRowId(RELATED_PRODUCT_ROW_ID)
                                                                                                     .withAttributes(new ArrayList<ECRFSheetModelAttribute>())
                                                                                                     .build())
                                                                    .build();

        ECRFSheet ecrfSheetModelForRelatedMapping = ECRFModelFixture.aECRFModel()
                                                                    .withScode(RELATED_MAPPING)
                                                                    .withSheetName(RELATED_PRODUCT_MAPPING_SHEET)
                                                                    .withSheetIndex(3)
                                                                    .withSheetTypeStrategy(SheetTypeStrategy.Related)
                                                                    .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                     .withOwnerProductId(ROOT_PRODUCT_ROW_ID)
                                                                                                     .withRelatedToId(RELATED_PRODUCT_ROW_ID)
                                                                                                     .withRelationShipName("related1")
                                                                                                     .build())
                                                                    .build();

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForRelatedProduct, ecrfSheetModelForRelatedMapping))
                                                       .withIsRelatedToSheet()
                                                       .withControlSheet()
                                                       .build();
        when(productInstanceClient.createProductInstance(eq(productInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(productInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(productInstance.getSiteId()),
                                                         eq(productInstance.getCustomerId()),
                                                         eq(productInstance.getContractId()),
                                                         eq(productInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(productInstance.getProjectId()),
                                                         eq(productInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class)))
            .thenReturn(productInstance);
        when(productInstanceClient.createProductInstance(eq(relatedProductInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(relatedProductInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(relatedProductInstance.getSiteId()),
                                                         eq(relatedProductInstance.getCustomerId()),
                                                         eq(relatedProductInstance.getContractId()),
                                                         eq(relatedProductInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(relatedProductInstance.getProjectId()),
                                                         eq(relatedProductInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class)))
            .thenReturn(relatedProductInstance);

        ImportResults importResults = new ImportResults();

        //When
        Set<LineItemId> lineItemIds = productBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), relatedProductInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode("H123"));

        //verify
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(productInstanceClient, times(1)).put(relatedProductInstance);
        assertFalse(importResults.hasErrors());
        Assert.assertThat(lineItemIds.size(), is(2));
    }

    @Test
    public void shouldBreakWithFirstExceptionWhenAnyMandatoryValidationFails() throws InstanceCharacteristicNotFound {

        //When
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withContractCardinality(new Cardinality(0, 0, null))
                                                                                                .withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProduct", "1"))
                                                                       )
                                                                       .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.getCentralSite("DEFAULT-TEST-PROJECT-ID")).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        doNothing().when(productRelationshipService).createRelations(any(ECRFSheet.class), any(AssetKeyContainer.class), any(ImportResults.class));

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE))).build())
                                                   .build();

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel))
                                                       .withIsRelatedToSheet()
                                                       .withControlSheet()
                                                       .build();

        ImportResults importResults = new ImportResults();

        //When
        Set<LineItemId> impactedLineItemId = productBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode("H123"));

        //verify
        assertTrue(importResults.hasErrors());
        assertImportResultContainsErrorMessage(importResults, "Contract Cardinality Failed - aProduct can have only 0 instance(s) for the Customer.");
        assertTrue(impactedLineItemId.size() == 0);
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldAddTheExceptionToListAndContinueImportingNextProductWhenProductLevelExceptionsReported() throws InstanceCharacteristicNotFound {
        final String SITE_2 = "22334";
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId").withSiteId(ROOT_SITE_ID)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withAttribute(AttributeFixture.anAttribute()
                                                                                                                               .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                               .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                               .build())
                                                                                                .withAttribute(AttributeFixture.anAttribute()
                                                                                                                               .called(ATTRIBUTE_WITH_SOURCE_RULE)
                                                                                                                               .withMaxLength(5)
                                                                                                                               .withMinLength(5)
                                                                                                                               .withAttributeSourceRule(aCalculatedAttributeSourceRule().forAttribute(ATTRIBUTE_WITH_SOURCE_RULE).withExpression("'A'").build())
                                                                                                                               .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        ProductInstance secondInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("secondInstance").withSiteId(SITE_2)
                                                                      .withProductOffering(ProductOfferingFixture
                                                                                               .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                               .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                               .withSiteSpecific()
                                                                                               .withAttribute(AttributeFixture.anAttribute()
                                                                                                                              .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                              .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                              .build())
                                                                                               .withAttribute(AttributeFixture.anAttribute()
                                                                                                                              .called(ATTRIBUTE_WITH_SOURCE_RULE)
                                                                                                                              .withMaxLength(5)
                                                                                                                              .withMinLength(5)
                                                                                                                              .withAttributeSourceRule(aCalculatedAttributeSourceRule().forAttribute(ATTRIBUTE_WITH_SOURCE_RULE).withExpression("'A'").build())
                                                                                                                              .build())
                                                                                               .withProductIdentifier(
                                                                                                   ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                      .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(productInstance.getCustomerId())).thenReturn(siteResource);
        siteDtos.add(SiteDTOFixture.aSiteDTO().withBfgSiteId(SITE_2).withName("Site 2").build());
        when(siteResource.get(productInstance.getProjectId(), SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(ROOT_SHEET_INDEX)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID_2)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, "wrongSiteId"),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID_2)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, SITE_2),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();

        when(productInstanceClient.createProductInstance(eq(productInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(productInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(productInstance.getSiteId()),
                                                         eq(productInstance.getCustomerId()),
                                                         eq(productInstance.getContractId()),
                                                         eq(productInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(productInstance.getProjectId()),
                                                         eq(productInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class)))
            .thenReturn(productInstance);
        when(productInstanceClient.createProductInstance(eq(secondInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(secondInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(secondInstance.getSiteId()),
                                                         eq(secondInstance.getCustomerId()),
                                                         eq(secondInstance.getContractId()),
                                                         eq(secondInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(secondInstance.getProjectId()),
                                                         eq(secondInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class)))
            .thenReturn(secondInstance);


        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = productBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode("H123"));

        //verify
        assertImportResultContainsErrorMessage(importResults, "Site Id : \"wrongSiteId\" for this Customer cannot be found");
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(productInstanceClient, times(1)).put(secondInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
        Assert.assertThat(lineItemsImpacted.size(), is(2));
        assertTrue(lineItemsImpacted.contains(new LineItemId(productInstance.getLineItemId())));
        assertTrue(lineItemsImpacted.contains(new LineItemId(secondInstance.getLineItemId())));
    }

    @Test
    public void shouldHaveAssetKeyForEachRowIdInWorkBookAddedToAssetKeyMap() throws InstanceCharacteristicNotFound {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                                                                     .withRelatedProductIdentifier("S0000001", "Child1",
                                                                                                                                                                                   StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID),
                                                                                                                                                                                                              ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                                                                     .withCardinalityExpression(CardinalityExpression.NIL))
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier("Another Relationship").withCardinalityExpression(CardinalityExpression.NIL))
                                                                       )
                                                                       .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.getCentralSite("DEFAULT-TEST-PROJECT-ID")).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);


        ProductInstance childProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                            .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
                                                                            .withProductOffering(ProductOfferingFixture
                                                                                                     .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                     .withAttribute(STENCIL_ATTRIBUTE)
                                                                                                     .withStencil(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                     .withAttribute("Contract Term")
                                                                                                     .withProductIdentifier(
                                                                                                         CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                     .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE).withCardinalityExpression(CardinalityExpression.NIL)))
                                                                            .build();

        childProductInstance.setStencilId(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID)));
        childProductInstance.getProductOffering().setAvailableStencils(newArrayList(StencilInfo.newInstance(StencilCode.newInstance(STENCIL_ID), STENCIL_ATTRIBUTE)));

        ProductInstance childOfChildProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                                   .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
                                                                                   .withProductOffering(ProductOfferingFixture
                                                                                                            .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                            .withAttribute("Contract Term")
                                                                                                            .withProductIdentifier(
                                                                                                                SECOND_CHILD_PRODUCT_CODE_IMPORTABLE))
                                                                                   .build();

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID)))
                                                                                    .build())
                                                   .build();

        ECRFSheet ecrfSheetModelForChild = ECRFModelFixture.aECRFModel()
                                                           .withScode(CHILD_PRODUCT_CODE_IMPORTABLE)
                                                           .withSheetName(CHILD_SHEET_NAME)
                                                           .withSheetIndex(2)
                                                           .withSheetTypeStrategy(SheetTypeStrategy.Child)
                                                           .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                            .withRowId(CHILD_PRODUCT_ROW_ID)
                                                                                            .withParentId(ROOT_PRODUCT_ROW_ID)
                                                                                            .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                         new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                         new ECRFSheetModelAttribute("Contract Term", "Term"))
                                                                                            )
                                                                                            .build())
                                                           .build();

        ECRFSheet ecrfSheetModelForSecondChild = ECRFModelFixture.aECRFModel()
                                                                 .withScode(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                 .withSheetName(SECOND_CHILD_SHEET_NAME)
                                                                 .withSheetIndex(3)
                                                                 .withSheetTypeStrategy(SheetTypeStrategy.Child)
                                                                 .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                  .withRowId(SECOND_CHILD_PRODUCT_ROW_ID)
                                                                                                  .withParentId(CHILD_PRODUCT_ROW_ID)
                                                                                                  .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                               new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                               new ECRFSheetModelAttribute("Contract Term", "Term")))
                                                                                                  .build())
                                                                 .build();

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForChild, ecrfSheetModelForSecondChild)).withControlSheet().build();
        when(productInstanceClient.createProductInstance(CHILD_PRODUCT_CODE_IMPORTABLE, null, productInstance.getLineItemId(), ROOT_SITE_ID, childProductInstance.getCustomerId(), childProductInstance.getContractId(),
                                                         childProductInstance.getQuoteOptionId(), StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)),
                                                         childProductInstance.getProjectId(), childProductInstance.getContractTerm(), quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childProductInstance);
        when(productInstanceClient.createProductInstance(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE, null, productInstance.getLineItemId(), ROOT_SITE_ID, childOfChildProductInstance.getCustomerId(), childOfChildProductInstance.getContractId(),
                                                         childOfChildProductInstance.getQuoteOptionId(), StencilId.NIL, childOfChildProductInstance.getProjectId(), childOfChildProductInstance.getContractTerm(), quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childOfChildProductInstance);
        when(productInstanceClient.createProductInstance(eq(productInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(productInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(productInstance.getSiteId()),
                                                         eq(productInstance.getCustomerId()),
                                                         eq(productInstance.getContractId()),
                                                         eq(productInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(productInstance.getProjectId()),
                                                         eq(productInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class))).thenReturn(productInstance);


        ImportResults importResults = new ImportResults();
        AssetKeyContainer assetKeyContainer = new AssetKeyContainer();
        Set<LineItemId> lineItemsImpacted = productBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, assetKeyContainer, productInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode("H123"));

        //verify
        checkAssetKeyPresentInContainerForEachRowID(ecrfWorkBook, assetKeyContainer);
        assertFalse(importResults.hasErrors());
        verify(productInstanceClient).put(productInstance);
        Assert.assertThat(lineItemsImpacted.size(), is(1));
    }

    @Test
    public void shouldSaveFirstRootInstanceWhenChildHasNoErrorsAndRoolbackSecondInstanceWhenItsChildHasErrors() throws InstanceCharacteristicNotFound {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                                                                     .withRelatedProductIdentifier("S0000001", "Child1",
                                                                                                                                                                                   StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID),
                                                                                                                                                                                                              ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                                                                     .withCardinalityExpression(CardinalityExpression.NIL))
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier("Another Relationship").withCardinalityExpression(CardinalityExpression.NIL))
                                                                       )
                                                                       .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.getCentralSite("DEFAULT-TEST-PROJECT-ID")).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").withName("Site Name").build());
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);


        ProductInstance childProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                            .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
                                                                            .withProductOffering(ProductOfferingFixture
                                                                                                     .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                     .withAttribute(STENCIL_ATTRIBUTE)
                                                                                                     .withStencil(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                     .withAttribute("Contract Term")
                                                                                                     .withProductIdentifier(
                                                                                                         CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                     .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE).withCardinalityExpression(CardinalityExpression.NIL)))
                                                                            .build();

        childProductInstance.setStencilId(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID)));
        childProductInstance.getProductOffering().setAvailableStencils(newArrayList(StencilInfo.newInstance(StencilCode.newInstance(STENCIL_ID), STENCIL_ATTRIBUTE)));

        ProductInstance childOfChildProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                                   .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
                                                                                   .withProductOffering(ProductOfferingFixture
                                                                                                            .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                            .withProductIdentifier(
                                                                                                                SECOND_CHILD_PRODUCT_CODE_IMPORTABLE))
                                                                                   .build();

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID)))
                                                                                    .build())
                                                   .build();

        ECRFSheet ecrfSheetModelForChild = ECRFModelFixture.aECRFModel()
                                                           .withScode(CHILD_PRODUCT_CODE_IMPORTABLE)
                                                           .withSheetName(CHILD_SHEET_NAME)
                                                           .withSheetIndex(2)
                                                           .withSheetTypeStrategy(SheetTypeStrategy.Child)
                                                           .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                            .withRowId(CHILD_PRODUCT_ROW_ID)
                                                                                            .withParentId(ROOT_PRODUCT_ROW_ID)
                                                                                            .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                         new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                         new ECRFSheetModelAttribute("Contract Term", "Term"))
                                                                                            )
                                                                                            .build())
                                                           .build();

        ECRFSheet ecrfSheetModelForSecondChild = ECRFModelFixture.aECRFModel()
                                                                 .withScode(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                 .withSheetName(SECOND_CHILD_SHEET_NAME)
                                                                 .withSheetIndex(3)
                                                                 .withSheetTypeStrategy(SheetTypeStrategy.Child)
                                                                 .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                  .withRowId(SECOND_CHILD_PRODUCT_ROW_ID)
                                                                                                  .withParentId(CHILD_PRODUCT_ROW_ID)
                                                                                                  .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                               new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                               new ECRFSheetModelAttribute("Contract Term", "Term")))
                                                                                                  .build())
                                                                 .build();

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForChild, ecrfSheetModelForSecondChild)).withControlSheet().build();
        when(productInstanceClient.createProductInstance(CHILD_PRODUCT_CODE_IMPORTABLE, null, productInstance.getLineItemId(), ROOT_SITE_ID, childProductInstance.getCustomerId(), childProductInstance.getContractId(),
                                                         childProductInstance.getQuoteOptionId(), StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)),
                                                         childProductInstance.getProjectId(), childProductInstance.getContractTerm(), quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childProductInstance);
        when(productInstanceClient.createProductInstance(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE, null, productInstance.getLineItemId(), ROOT_SITE_ID, childOfChildProductInstance.getCustomerId(), childOfChildProductInstance.getContractId(),
                                                         childOfChildProductInstance.getQuoteOptionId(), StencilId.NIL, childOfChildProductInstance.getProjectId(), childOfChildProductInstance.getContractTerm(), quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childOfChildProductInstance);
        when(productInstanceClient.createProductInstance(eq(productInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(productInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(productInstance.getSiteId()),
                                                         eq(productInstance.getCustomerId()),
                                                         eq(productInstance.getContractId()),
                                                         eq(productInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(productInstance.getProjectId()),
                                                         eq(productInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class))).thenReturn(productInstance);


        ImportResults importResults = new ImportResults();
        AssetKeyContainer assetKeyContainer = new AssetKeyContainer();
        Set<LineItemId> lineItemsImpacted = productBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, assetKeyContainer, productInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode(""));

        //verify
        assertTrue(importResults.hasErrors());
        assertImportResultContainsErrorMessage(importResults, "The attribute Contract Term is not configured for this product");
        verify(productInstanceClient, never()).put(productInstance);
        verify(quoteOptionClient, times(1)).deleteQuoteOptionItem(eq(productInstance.getProjectId()), eq(productInstance.getQuoteOptionId()), anyString());
        Assert.assertThat(lineItemsImpacted.size(), is(0));
    }

    @Test
    public void shouldExtractDeliveryAddressDetailsFromDeliveryAddressSheet(){
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();
        ProductInstance newProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("anotherLineItemId").withProductInstanceId("new")
                                                                          .withSiteId(ROOT_SITE_ID)
                                                                          .withProductOffering(productOfferingFixture.withAttribute(ATTRIBUTE_NAME).withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                                                  .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                                                                                  .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                                                                                  .build())
                                                                                                                     .withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                    .called(ATTRIBUTE_WITH_SOURCE_RULE)
                                                                                                                                                    .withMaxLength(5)
                                                                                                                                                    .withMinLength(5)
                                                                                                                                                    .withAttributeSourceRule(aCalculatedAttributeSourceRule().forAttribute(ATTRIBUTE_WITH_SOURCE_RULE).withExpression("'A'").build())
                                                                                                                                                    .build()))
                                                                          .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(newProductInstance.getCustomerId())).thenReturn(siteResource);
        when(siteResource.get(newProductInstance.getProjectId(), SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(configuratorClient.createContractDto(newProductInstance.getContractTerm(), newProductInstance.getCustomerId(), ROOT_PRODUCT_CODE_IMPORTABLE, newProductInstance.getContractId(), "token")).thenReturn(contractDTO);
        when(productInstanceClient.getByAssetKey(newProductInstance.getKey())).thenReturn(newProductInstance);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(newProductInstance.getProjectId())).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(newProductInstance.getQuoteOptionId())).thenReturn(quoteOptionItemResource);

        when(productInstanceClient.createProductInstance(eq(newProductInstance.getProductIdentifier().getProductId()),
                                                         eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(ROOT_SITE_ID),
                                                         eq(newProductInstance.getCustomerId()),
                                                         eq(newProductInstance.getContractId()),
                                                         eq(newProductInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(newProductInstance.getProjectId()),
                                                         eq(newProductInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class))).thenReturn(newProductInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        List<ECRFSheet> nonProductSheetModel = newArrayList(ECRFModelFixture.aECRFModel()
                                                                            .withSheetIndex(2)
                                                                            .withSheetName(DELIVERY_ADDRESS)
                                                                            .withSheetTypeStrategy(SheetTypeStrategy.NonProduct)
                                                                            .withRow(ECRFSheetModelRowFixture.aDeliveryAddressModelRow(ROOT_PRODUCT_ROW_ID)).build());

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withNonProductECRFSheets(nonProductSheetModel).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = productBasedImporter.importFromSheet(newProductInstance.getCustomerId(), newProductInstance.getContractId(), newProductInstance.getContractTerm(), newProductInstance.getProjectId(), newProductInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), newProductInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode(""));

        verify(quoteOptionClient).createQuoteOptionItem(eq(newProductInstance.getProjectId()), eq(newProductInstance.getQuoteOptionId()), anyString(), anyString(), eq(ROOT_PRODUCT_CODE_IMPORTABLE), eq(contractDTO), anyString(), eq(new ProductCategoryCode("")));
        verify(productInstanceClient, times(1)).createProductInstance(eq(newProductInstance.getProductIdentifier().getProductId()),
                                                                      eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                                      anyString(),
                                                                      eq(ROOT_SITE_ID),
                                                                      eq(newProductInstance.getCustomerId()),
                                                                      eq(newProductInstance.getContractId()),
                                                                      eq(newProductInstance.getQuoteOptionId()),
                                                                      eq(StencilId.NIL),
                                                                      eq(newProductInstance.getProjectId()),
                                                                      eq(newProductInstance.getContractTerm()),
                                                                      eq(quoteOptionClient), any(ProductCategoryCode.class));


        verify(productInstanceClient).put(newProductInstance);
        verify(productInstanceClient).refreshAttributesOfProductInstance(newProductInstance);
        verify(quoteOptionItemResource, atLeastOnce()).createDeliveryAddressForLineItem(anyString(), any(DeliveryAddressDTO.class));
        assertFalse(importResults.hasErrors());
        assertThat(lineItemsImpacted.size(), is(1));
    }

    @Test
    public void shouldNotExtractDeliveryAddressDetailsFromDeliveryAddressSheetWhenNoMatchingAddressIdFound(){
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        ProductOffering rootOffering = productOfferingFixture.build();
        ProductInstance newProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("anotherLineItemId").withProductInstanceId("new")
                                                                          .withSiteId(ROOT_SITE_ID)
                                                                          .withProductOffering(productOfferingFixture.withAttribute(ATTRIBUTE_NAME).withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                                                  .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                                                                                  .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                                                                                  .build())
                                                                                                                     .withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                    .called(ATTRIBUTE_WITH_SOURCE_RULE)
                                                                                                                                                    .withMaxLength(5)
                                                                                                                                                    .withMinLength(5)
                                                                                                                                                    .withAttributeSourceRule(aCalculatedAttributeSourceRule().forAttribute(ATTRIBUTE_WITH_SOURCE_RULE).withExpression("'A'").build())
                                                                                                                                                    .build()))
                                                                          .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(newProductInstance.getCustomerId())).thenReturn(siteResource);
        when(siteResource.get(newProductInstance.getProjectId(), SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(configuratorClient.createContractDto(newProductInstance.getContractTerm(), newProductInstance.getCustomerId(), ROOT_PRODUCT_CODE_IMPORTABLE, newProductInstance.getContractId(), "token")).thenReturn(contractDTO);
        when(productInstanceClient.getByAssetKey(newProductInstance.getKey())).thenReturn(newProductInstance);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(newProductInstance.getProjectId())).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(quoteOptionId)).thenReturn(quoteOptionItemResource);

        when(productInstanceClient.createProductInstance(eq(newProductInstance.getProductIdentifier().getProductId()),
                                                         eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(ROOT_SITE_ID),
                                                         eq(newProductInstance.getCustomerId()),
                                                         eq(newProductInstance.getContractId()),
                                                         eq(newProductInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(newProductInstance.getProjectId()),
                                                         eq(newProductInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class))).thenReturn(newProductInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        List<ECRFSheet> nonProductSheetModel = newArrayList(ECRFModelFixture.aECRFModel()
                                                                            .withSheetIndex(2)
                                                                            .withSheetName(DELIVERY_ADDRESS)
                                                                            .withSheetTypeStrategy(SheetTypeStrategy.NonProduct)
                                                                            .withRow(ECRFSheetModelRowFixture.aDeliveryAddressModelRow("someRowId")).build());

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withNonProductECRFSheets(nonProductSheetModel).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = productBasedImporter.importFromSheet(newProductInstance.getCustomerId(), newProductInstance.getContractId(), newProductInstance.getContractTerm(), newProductInstance.getProjectId(), newProductInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), newProductInstance.getProductIdentifier().getProductId(), Optional.<LineItemId>absent(), false, new ProductCategoryCode(""));

        verify(quoteOptionClient).createQuoteOptionItem(eq(newProductInstance.getProjectId()), eq(newProductInstance.getQuoteOptionId()), anyString(), anyString(), eq(ROOT_PRODUCT_CODE_IMPORTABLE), eq(contractDTO), anyString(), eq(new ProductCategoryCode("")));
        verify(productInstanceClient, times(1)).createProductInstance(eq(newProductInstance.getProductIdentifier().getProductId()),
                                                                      eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                                      anyString(),
                                                                      eq(ROOT_SITE_ID),
                                                                      eq(newProductInstance.getCustomerId()),
                                                                      eq(newProductInstance.getContractId()),
                                                                      eq(newProductInstance.getQuoteOptionId()),
                                                                      eq(StencilId.NIL),
                                                                      eq(newProductInstance.getProjectId()),
                                                                      eq(newProductInstance.getContractTerm()),
                                                                      eq(quoteOptionClient), any(ProductCategoryCode.class));


        verify(productInstanceClient).put(newProductInstance);
        verify(productInstanceClient).refreshAttributesOfProductInstance(newProductInstance);
        verify(quoteOptionItemResource, never()).createDeliveryAddressForLineItem(eq(newProductInstance.getQuoteOptionId()), any(DeliveryAddressDTO.class));
        assertFalse(importResults.hasErrors());
        assertThat(lineItemsImpacted.size(), is(1));
    }

    private void buildRootProductInstance() {
        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProductName", "aVersion")).withSiteSpecific();
        rootOffering = productOfferingFixture.build();
        productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("anotherLineItemId").withProductInstanceId("new")
                                                                          .withSiteId(ROOT_SITE_ID)
                                                                          .withProductOffering(productOfferingFixture.withAttribute(ATTRIBUTE_NAME).withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                                                  .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                                                                                  .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                                                                                  .build())
                                                                                                                     .withAttribute(AttributeFixture.anAttribute()
                                                                                                                                                    .called(ATTRIBUTE_WITH_SOURCE_RULE)
                                                                                                                                                    .withMaxLength(5)
                                                                                                                                                    .withMinLength(5)
                                                                                                                                                    .withAttributeSourceRule(aCalculatedAttributeSourceRule().forAttribute(ATTRIBUTE_WITH_SOURCE_RULE).withExpression("'A'").build())
                                                                                                                                                    .build()))
                                                                          .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(productOfferings.get()).thenReturn(rootOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(customerResource.siteResource(productInstance.getCustomerId())).thenReturn(siteResource);
        when(siteResource.get(productInstance.getProjectId(), SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(configuratorClient.createContractDto(productInstance.getContractTerm(), productInstance.getCustomerId(), ROOT_PRODUCT_CODE_IMPORTABLE, productInstance.getContractId(), "token")).thenReturn(contractDTO);
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);

        when(productInstanceClient.createProductInstance(eq(productInstance.getProductIdentifier().getProductId()),
                                                         eq(rootOffering.getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(ROOT_SITE_ID),
                                                         eq(productInstance.getCustomerId()),
                                                         eq(productInstance.getContractId()),
                                                         eq(productInstance.getQuoteOptionId()),
                                                         eq(StencilId.NIL),
                                                         eq(productInstance.getProjectId()),
                                                         eq(productInstance.getContractTerm()),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class))).thenReturn(productInstance);
    }

    private boolean checkAssetKeyPresentInContainerForEachRowID(ECRFWorkBook ecrfWorkBook, AssetKeyContainer assetKeyContainer) {
        for (ECRFSheet ecrfSheet : ecrfWorkBook.getSheets()) {
            for (ECRFSheetModelRow ecrfSheetModelRow : ecrfSheet.getRows()) {
                if (!assetKeyContainer.getAssetKey(ecrfSheetModelRow.getRowId()).isPresent()) {
                    return false;
                }
            }
        }
        return true;
    }
}
