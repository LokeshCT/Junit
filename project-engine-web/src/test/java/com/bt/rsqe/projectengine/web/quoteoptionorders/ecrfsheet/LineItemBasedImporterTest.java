package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.client.QuoteOptionClient;
import com.bt.rsqe.customerinventory.filter.AssetFilter;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
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
import com.bt.rsqe.domain.product.AttributeDataType;
import com.bt.rsqe.domain.product.Cardinality;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.extensions.CardinalityExpression;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.extensions.ExpressionExpectedResultType;
import com.bt.rsqe.domain.product.extensions.RuleCalculatedAttributeSource;
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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.domain.bom.fixtures.AttributeFixture.*;
import static com.bt.rsqe.domain.product.extensions.Expression.*;
import static com.bt.rsqe.domain.product.fixtures.RuleCalculatedAttributeSourceFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class LineItemBasedImporterTest extends ECRFImporterTest {

    @Before
    public void setUp() throws IOException {
        before();
    }

    @Test
    public void shouldImportAttributesIntoRootProductInstance() throws Exception {
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

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

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
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals(ATTRIBUTE_DEFAULT_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME_DEFAULT_VALUE).getStringValue());

        assertFalse(importResults.hasErrors());
        verify(productInstanceClient).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
        assertThat(lineItemsImpacted.size(), is(1));
        assertTrue(lineItemsImpacted.contains(new LineItemId(productInstance.getLineItemId())));
    }

    @Test
    public void shouldNotImportAttributesIntoRootProductInstanceIfSiteIdImportedNotMatchWithRootProductSiteId() throws Exception {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId").withSiteId("aSiteId")
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
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(siteResource.getCentralSite(projectId)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").withName("siteName").build());
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

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
                                                                                        new ECRFSheetModelAttribute(SITE_ID, "anotherSiteID"),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.siteIdNotFoundForCustomer, "anotherSiteID"));
        verify(productInstanceClient, times(0)).put(productInstance);
        verify(productInstanceClient, times(0)).refreshAttributesOfProductInstance(productInstance);
    }

    @Test
    public void shouldImportAttributesIntoRootProductInstanceIfSiteIdNullOrEmptyInWorkSheetAndRootProductIsSiteSpecific() throws Exception {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId").withSiteId("aSiteId")
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
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(siteResource.getCentralSite(projectId)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

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
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ""),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
    }

    @Test
    public void shouldNotImportAttributesIntoRootProductInstanceIfSiteIdNotPresentInWorkSheetAndRootProductIsSiteSpecific() throws Exception {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId").withSiteId("aSiteId")
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
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(siteResource.getCentralSite(projectId)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(ROOT_SHEET_INDEX)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withSheetName(ROOT_SHEET_NAME)
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.attributeNotFoundInWorkSheet, "SITE ID", ROOT_SHEET_NAME));
        verify(productInstanceClient, times(0)).put(productInstance);
        verify(productInstanceClient, times(0)).refreshAttributesOfProductInstance(productInstance);
    }

    @Test
    public void shouldImportAttributesIntoChildProductInstanceAndCreateRelationshipAndIgnoreSiteCheckForChild() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(ATTRIBUTE_NAME)
                                                                                                .withAttribute("REGION")
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture()
                                                                                                                           .withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                                           .withRelatedProductIdentifier("S0000001", "Child1",
                                                                                                                                                         StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID),
                                                                                                                                                                                    ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                                           .withCardinalityExpression(CardinalityExpression.NIL)))

                                                                       .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(siteResource.getCentralSite(projectId)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);

        RuleCalculatedAttributeSource rule1 = new RuleCalculatedAttributeSource("rule1", alwaysPassesExpression(), "REGION", 1, new Expression("Parent.REGION", ExpressionExpectedResultType.String), false, null);
        ProductInstance childProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                            .withProductOffering(ProductOfferingFixture
                                                                                                     .aProductOffering()
                                                                                                     .withAttribute(ATTRIBUTE_NAME)
                                                                                                     .withSiteSpecific()
                                                                                                     .withAttribute(anAttribute().called("REGION").withAttributeSourceRule(rule1).build())
                                                                                                     .withAttribute(STENCIL_ATTRIBUTE)
                                                                                                     .withStencil(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                     .withAttribute("Contract Term")
                                                                                                     .withProductIdentifier(
                                                                                                         CHILD_PRODUCT_CODE_IMPORTABLE))
                                                                            .build();

        childProductInstance.setStencilId(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID)));
        childProductInstance.getProductOffering().setAvailableStencils(newArrayList(StencilInfo.newInstance(StencilCode.newInstance(STENCIL_ID), STENCIL_ATTRIBUTE)));

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
                                                                                        new ECRFSheetModelAttribute("REGION", "UK")))
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
                                                                                            .withSheetName(CHILD_SHEET_NAME)
                                                                                            .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                         new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                         new ECRFSheetModelAttribute("Contract Term", "Term"))
                                                                                            ).build())
                                                           .build();

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForChild)).withControlSheet().build();
        when(productInstanceClient.createProductInstance(CHILD_PRODUCT_CODE_IMPORTABLE, null, "LineItemId", ROOT_SITE_ID, customerId, contractId,
                                                         quoteOptionId, StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)),
                                                         projectId, CONTRACT_TERM, quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childProductInstance);

        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals(ATTRIBUTE_VALUE, childProductInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals("UK", childProductInstance.getInstanceCharacteristic("REGION").getStringValue());
        assertTrue(productInstance.getActiveChildren().contains(childProductInstance));
        assertFalse(importResults.hasErrors());
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
    }

    @Test
    public void shouldCreateRelationShipAForChildWhenRelateToAndChildMappingAvailableInWorkBook() throws Exception {
        final String RELATION_NAME = "RELATION NAME";
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(ATTRIBUTE_NAME)
                                                                                                .withAttribute("REGION")
                                                                                                .withAttribute(RELATION_NAME)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture()
                                                                                                                           .withRelationName("reletedToRelation")
                                                                                                                           .withProductIdentifier(RELATED_PRODUCT_CODE_IMPORTABLE)
                                                                                                                           .withRelatedProductIdentifier("S0000005", "RelatedTo",
                                                                                                                                                         StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID),
                                                                                                                                                                                    ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                                           .withCardinalityExpression(CardinalityExpression.NIL))
                                                                                                .withSalesRelationship(new SalesRelationshipFixture()
                                                                                                                           .withRelationName("childRelation")
                                                                                                                           .withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                                           .withRelatedProductIdentifier("S0000001", "Child1",
                                                                                                                                                         StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID),
                                                                                                                                                                                    ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                                           .withCardinalityExpression(CardinalityExpression.NIL)))


                                                                       .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(siteResource.getCentralSite(projectId)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(pmrClient.productOffering(ProductSCode.newInstance(CHILD_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);

        RuleCalculatedAttributeSource rule1 = new RuleCalculatedAttributeSource("rule1", alwaysPassesExpression(), "REGION", 1, new Expression("Parent.REGION", ExpressionExpectedResultType.String), false, null);
        ProductInstance childProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                            .withProductOffering(ProductOfferingFixture
                                                                                                     .aProductOffering()
                                                                                                     .withAttribute(ATTRIBUTE_NAME)
                                                                                                     .withAttribute(RELATION_NAME)
                                                                                                     .withSiteSpecific()
                                                                                                     .withAttribute(anAttribute().called("REGION").withAttributeSourceRule(rule1).build())
                                                                                                     .withAttribute(STENCIL_ATTRIBUTE)
                                                                                                     .withStencil(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                     .withAttribute("Contract Term")
                                                                                                     .withProductIdentifier(
                                                                                                         CHILD_PRODUCT_CODE_IMPORTABLE))
                                                                            .build();

        childProductInstance.setStencilId(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID)));
        childProductInstance.getProductOffering().setAvailableStencils(newArrayList(StencilInfo.newInstance(StencilCode.newInstance(STENCIL_ID), STENCIL_ATTRIBUTE)));

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
                                                                                        new ECRFSheetModelAttribute("REGION", "UK")))
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
                                                                                            .withSheetName(CHILD_SHEET_NAME)
                                                                                            .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                         new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                         new ECRFSheetModelAttribute("RELATION NAME", "childRelation"),
                                                                                                                         new ECRFSheetModelAttribute("Contract Term", "Term"))
                                                                                            ).build())
                                                           .build();

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForChild)).withControlSheet().build();
        when(productInstanceClient.createProductInstance(CHILD_PRODUCT_CODE_IMPORTABLE, null, "LineItemId", ROOT_SITE_ID, customerId, contractId,
                                                         quoteOptionId, StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)),
                                                         projectId, CONTRACT_TERM, quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childProductInstance);

        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());

        //Verify instance only created for Child
        verify(productInstanceClient, times(1)).createProductInstance(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(StencilId.class), anyString(), anyString(), any(QuoteOptionClient.class), any(ProductCategoryCode.class));

        assertFalse(importResults.hasErrors());
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
    }

    @Test
    public void shouldImportAttributesIntoRootProductInstanceAndCreateRootInstancesIfGiven() throws Exception {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("lineItemId").withProductInstanceId("marker")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withAttribute(AttributeFixture.anAttribute()
                                                                                                                               .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                               .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                               .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();

        ProductInstance newProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("anotherLineItemId").withProductInstanceId("new")
                                                                          .withSiteId(ROOT_SITE_ID)
                                                                          .withProductOffering(ProductOfferingFixture
                                                                                                   .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                   .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                   .withSiteSpecific()
                                                                                                   .withAttribute(AttributeFixture.anAttribute()
                                                                                                                                  .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                                  .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                                  .build())
                                                                                                   .withProductIdentifier(
                                                                                                       ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                          .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);

        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(siteResource.getCentralSite(projectId)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("SiteId").build());
        when(productInstanceClient.get(Optional.of(new LineItemId("lineItemId")).get())).thenReturn(productInstance);
        when(productInstanceClient.get(Optional.of(new LineItemId("anotherLineItemId")).get())).thenReturn(newProductInstance);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);

        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productInstanceClient.getByAssetKey(newProductInstance.getKey())).thenReturn(newProductInstance);
        when(productInstanceClient.createProductInstance(eq(newProductInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                         eq(newProductInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                         anyString(),
                                                         eq(newProductInstance.getSiteId()),
                                                         eq(customerId),
                                                         eq(contractId),
                                                         eq(quoteOptionId),
                                                         eq(StencilId.NIL),
                                                         eq(projectId),
                                                         eq(contractTrem),
                                                         eq(quoteOptionClient), any(ProductCategoryCode.class))).thenReturn(newProductInstance);

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
                                                                                    .withRowId(ANOTHER_ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("lineItemId")), false, new ProductCategoryCode(""));

        //Verify Marker Root Product Instance
        verify(productInstanceClient).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);

        //Verify for Newly created Root Product Instance
        verify(quoteOptionClient, times(1)).createQuoteOptionItem(eq(projectId), eq(quoteOptionId), eq(productInstance.getLineItemId()), anyString(), eq(productInstance.getProductOffering().getProductIdentifier().getProductId()), anyString(), any(ProductCategoryCode.class), anyString(), anyBoolean());
        verify(productInstanceClient, times(1)).createProductInstance(eq(productInstance.getProductOffering().getProductIdentifier().getProductId()),
                                                                      eq(productInstance.getProductOffering().getProductIdentifier().getVersionNumber()),
                                                                      anyString(),
                                                                      eq(productInstance.getSiteId()),
                                                                      eq(customerId),
                                                                      eq(contractId),
                                                                      eq(quoteOptionId),
                                                                      eq(StencilId.NIL),
                                                                      eq(projectId),
                                                                      eq(contractTrem),
                                                                      eq(quoteOptionClient), any(ProductCategoryCode.class));
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(newProductInstance);
        assertThat(lineItemsImpacted.size(), is(2));
        assertFalse(importResults.hasErrors());
        assertTrue(lineItemsImpacted.contains(new LineItemId(productInstance.getLineItemId())));
        assertTrue(lineItemsImpacted.contains(new LineItemId(newProductInstance.getLineItemId())));
    }

    @Test
    public void shouldImportAttributesAndBuildRelationshipsForChildrenWithChildren() throws Exception {
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


        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, ProductCategoryCode.NIL);
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals(ATTRIBUTE_VALUE, childProductInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals(ATTRIBUTE_VALUE, childOfChildProductInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertFalse(importResults.hasErrors());

        assertTrue(productInstance.getActiveChildren().contains(childProductInstance));
        assertTrue(childProductInstance.getActiveChildren().contains(childOfChildProductInstance));
        verify(productInstanceClient).put(productInstance);
    }

    @Test
    public void shouldParseDataTypesForOfferingCorrectly() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(ATTRIBUTE_NAME)
                                                                                                                   .withDataType(AttributeDataType.STRING)
                                                                                                                   .build())
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(NUMBER_ATTRIBUTE1)
                                                                                                                   .withDataType(AttributeDataType.NUMBER)
                                                                                                                   .build())
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(NUMBER_ATTRIBUTE2)
                                                                                                                   .withDataType(AttributeDataType.NUMBER)
                                                                                                                   .build())
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(NUMBER_ATTRIBUTE3)
                                                                                                                   .withDataType(AttributeDataType.NUMBER)
                                                                                                                   .build())
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(DATE_ATTRIBUTE1)
                                                                                                                   .withDataType(AttributeDataType.DATE)
                                                                                                                   .build())
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(DATE_ATTRIBUTE2)
                                                                                                                   .withDataType(AttributeDataType.DATE)
                                                                                                                   .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific())
                                                                       .build();

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute(NUMBER_ATTRIBUTE1, "123456"),
                                                                                        new ECRFSheetModelAttribute(NUMBER_ATTRIBUTE2, "49357240570123456"),
                                                                                        new ECRFSheetModelAttribute(NUMBER_ATTRIBUTE3, ""),
                                                                                        new ECRFSheetModelAttribute(DATE_ATTRIBUTE1, "2014/11/11"),
                                                                                        new ECRFSheetModelAttribute(DATE_ATTRIBUTE2, "")))
                                                                                    .build())

                                                   .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals("123456", productInstance.getInstanceCharacteristic(NUMBER_ATTRIBUTE1).getStringValue());
        assertEquals("49357240570123456", productInstance.getInstanceCharacteristic(NUMBER_ATTRIBUTE2).getStringValue());
        assertEquals("", productInstance.getInstanceCharacteristic(NUMBER_ATTRIBUTE3).getStringValue());
        assertEquals("2014/11/11", productInstance.getInstanceCharacteristic(DATE_ATTRIBUTE1).getStringValue());
        assertEquals("", productInstance.getInstanceCharacteristic(DATE_ATTRIBUTE2).getStringValue());
        assertFalse(importResults.hasErrors());
        verify(productInstanceClient).put(productInstance);
    }

    @Test
    public void shouldFailToParseInvalidNumberDataTypes() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(NUMBER_ATTRIBUTE1)
                                                                                                                   .withDataType(AttributeDataType.NUMBER)
                                                                                                                   .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(NUMBER_ATTRIBUTE1, "not a number")))
                                                                                    .build())

                                                   .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.attributeDataTypeMisMatch, "not a number", NUMBER_ATTRIBUTE1, ROOT_SHEET_NAME, ROOT_PRODUCT_ROW_ID, AttributeDataType.NUMBER.toString().toLowerCase()));
        verify(productInstanceClient, times(0)).put(productInstance);
        verify(productInstanceClient, times(0)).refreshAttributesOfProductInstance(productInstance);
    }

    @Test
    public void shouldFailIfMinCardinalityViolatedForAnAttribute() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called("anAttribute")
                                                                                                                   .withDataType(AttributeDataType.NUMBER)
                                                                                                                   .withMaxLength(10)
                                                                                                                   .withMinLength(8)
                                                                                                                   .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute("anAttribute", "123")))
                                                                                    .build())

                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.minimumLengthExceeded, "anAttribute", 8, "123", ROOT_SHEET_NAME, ROOT_PRODUCT_ROW_ID));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldFailIfMaxCardinalityViolatedForAnAttribute() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called("anAttribute")
                                                                                                                   .withDataType(AttributeDataType.NUMBER)
                                                                                                                   .withMaxLength(10)
                                                                                                                   .withMinLength(8)
                                                                                                                   .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute("anAttribute", "123456789123")))
                                                                                    .build())

                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.maximumLengthExceeded, "anAttribute", 10, "123456789123", ROOT_SHEET_NAME, ROOT_PRODUCT_ROW_ID));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldFailTheImportIfContractCardinalityFailed() throws Exception {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withContractCardinality(new Cardinality(0, 2, null))
                                                                                                .withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProduct", "1"))
                                                                       )
                                                                       .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        when(productInstanceClient.getContractAssets(any(CustomerId.class), any(ContractId.class), any(ProductCode.class), any(ProductVersion.class), any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset("anAssetId", 1L)));


        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
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
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, "Contract Cardinality Failed - aProduct can have only 2 instance(s) for the Customer.");
        verify(productInstanceClient, times(0)).put(productInstance);
    }


    @Test
    public void shouldFailToParseInvalidDateDataTypes() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(DATE_ATTRIBUTE1)
                                                                                                                   .withDataType(AttributeDataType.DATE)
                                                                                                                   .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(DATE_ATTRIBUTE1, "not a date")))
                                                                                    .build())

                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.attributeDataTypeMisMatch, "not a date", DATE_ATTRIBUTE1, ROOT_SHEET_NAME, ROOT_PRODUCT_ROW_ID, AttributeDataType.DATE.toString().toLowerCase()));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldAllowNullPointerExceptionIfCellValueIsMissing() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(DATE_ATTRIBUTE1)
                                                                                                                   .withDataType(AttributeDataType.STRING)
                                                                                                                   .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific())
                                                                       .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(DATE_ATTRIBUTE1, null)))
                                                                                    .build())

                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertFalse(importResults.hasErrors());
        assertEquals("null", productInstance.getInstanceCharacteristic(DATE_ATTRIBUTE1).getValue());
        verify(productInstanceClient).put(productInstance);
    }

    @Test
    public void shouldFailIfValueIsNotInAttributeAllowedValues() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering()
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(ATTRIBUTE_NAME)
                                                                                                                   .withDataType(AttributeDataType.STRING)
                                                                                                                   .withAllowedValues(AttributeValue.newInstance("yes"), AttributeValue.newInstance("no"))
                                                                                                                   .build())
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(ATTRIBUTE_NAME1)
                                                                                                                   .withDataType(AttributeDataType.STRING)
                                                                                                                   .withAllowedValues(AttributeValue.newInstance("yes"), AttributeValue.newInstance("no"))
                                                                                                                   .build())
                                                                                                .withAttribute(AttributeFixture
                                                                                                                   .anAttribute()
                                                                                                                   .called(ATTRIBUTE_NAME2)
                                                                                                                   .withDataType(AttributeDataType.STRING)
                                                                                                                   .withAllowedValues(AttributeValue.newInstance("yes"), AttributeValue.newInstance("no"))
                                                                                                                   .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, "yes"),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME1, "no"),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME2, "not valid")))
                                                                                    .build())

                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.valueNotAllowedInAllowedValues, "not valid", ATTRIBUTE_NAME2, ROOT_SHEET_NAME, ROOT_PRODUCT_ROW_ID, "yes, no"));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldThrowExceptionIfParentIdIsNotFound() throws InstanceCharacteristicNotFound {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE)))
                                                                       .build();

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .build();

        ECRFSheet ecrfSheetModelForChild = ECRFModelFixture.aECRFModel()
                                                           .withScode(CHILD_PRODUCT_CODE_IMPORTABLE)
                                                           .withSheetName(CHILD_SHEET_NAME)
                                                           .withSheetTypeStrategy(SheetTypeStrategy.Child)
                                                           .withSheetIndex(2)
                                                           .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                            .withRowId(CHILD_PRODUCT_ROW_ID)
                                                                                            .withSheetName(CHILD_SHEET_NAME)
                                                                                            .withParentId(NOT_IN_WORK_BOOK)
                                                                                            .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                         new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                         new ECRFSheetModelAttribute("Contract Term", "Term"))
                                                                                            ).build())
                                                           .build();

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForChild)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.parentIdNotFound, NOT_IN_WORK_BOOK, CHILD_SHEET_NAME));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldThrowExceptionIfParentIdIsNotFoundNestedChildren() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE))
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier("Another Relationship"))
                                                                       )
                                                                       .build();

        ProductInstance childProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                            .withSiteId(ROOT_SITE_ID)
                                                                            .withContractTerm(CONTRACT_TERM)
                                                                            .withProductOffering(ProductOfferingFixture
                                                                                                     .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                     .withAttribute(STENCIL_ATTRIBUTE)
                                                                                                     .withStencil(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                     .withAttribute("Contract Term")
                                                                                                     .withProductIdentifier(
                                                                                                         CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                     .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE)))
                                                                            .build();

        childProductInstance.setStencilId(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID)));
        childProductInstance.getProductOffering().setAvailableStencils(newArrayList(StencilInfo.newInstance(StencilCode.newInstance(STENCIL_ID), STENCIL_ATTRIBUTE)));

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
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
                                                                                            .withSheetName(CHILD_SHEET_NAME)
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
                                                                                                  .withSheetName(SECOND_CHILD_SHEET_NAME)
                                                                                                  .withParentId(NOT_IN_WORK_BOOK)
                                                                                                  .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                               new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                               new ECRFSheetModelAttribute("Contract Term", "Term")))
                                                                                                  .build())
                                                                 .build();

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForChild, ecrfSheetModelForSecondChild)).withControlSheet().build();
        when(productInstanceClient.createProductInstance(CHILD_PRODUCT_CODE_IMPORTABLE, null, productInstance.getLineItemId(), ROOT_SITE_ID, childProductInstance.getCustomerId(), childProductInstance.getContractId(),
                                                         childProductInstance.getQuoteOptionId(), StencilId.NIL, childProductInstance.getProjectId(), childProductInstance.getContractTerm(), quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childProductInstance);
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, ProductCategoryCode.NIL);
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.parentIdNotFound, NOT_IN_WORK_BOOK, SECOND_CHILD_SHEET_NAME));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldFailIfProductOfferingIsStencilableButRowIsMissingStencilAttribute() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aStencilableProductOffering(AttributeValue.newInstance("stencil1"),
                                                                                                                             AttributeValue.newInstance("stencil2"),
                                                                                                                             AttributeValue.newInstance("stencil3"))
                                                                                                .withAttribute(ATTRIBUTE_NAME)
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withSheetName(ROOT_SHEET_NAME)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .build();

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.stencilMissingForStencilProduct, ROOT_SHEET_NAME, ROOT_PRODUCT_ROW_ID));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldFailIfProductOfferingIsStencilableButStencilValueIsNotInOffering() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aStencilableProductOffering(AttributeValue.newInstance("stencil1"),
                                                                                                                             AttributeValue.newInstance("stencil2"),
                                                                                                                             AttributeValue.newInstance("stencil3"))
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, NOT_IN_WORK_BOOK)))
                                                                                    .build())
                                                   .build();

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.stencilValueNotInProductOffering, NOT_IN_WORK_BOOK, ROOT_SHEET_NAME, ROOT_PRODUCT_ROW_ID));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldFailIfProductOfferingIsStencilableButStencilColumnIsNotInSheet() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aStencilableProductOffering(AttributeValue.newInstance("stencil1"),
                                                                                                                             AttributeValue.newInstance("stencil2"),
                                                                                                                             AttributeValue.newInstance("stencil3"))
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(new ArrayList<ECRFSheetModelAttribute>())
                                                                                    .build())
                                                   .build();

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.stencilMissingForStencilProduct, ROOT_SHEET_NAME, ROOT_PRODUCT_ROW_ID));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldApplyStencilToProduct() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aStencilableProductOffering(AttributeValue.newInstance("stencil1"),
                                                                                                                             AttributeValue.newInstance("stencil2"),
                                                                                                                             AttributeValue.newInstance("stencil3"))
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific())

                                                                       .build();
        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);


        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, "stencil2")))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertEquals("stencil2", productInstance.getStencilName());
        assertFalse(importResults.hasErrors());
        verify(productInstanceClient).put(productInstance);
    }

    @Test
    public void shouldPriceAfterImportingBCM() throws Exception {

        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withAttribute(AttributeFixture.anAttribute()
                                                                                                                               .called(ATTRIBUTE_NAME_DEFAULT_VALUE)
                                                                                                                               .withDefaultValue(ATTRIBUTE_DEFAULT_VALUE)
                                                                                                                               .build())
                                                                                                .withAttribute(AttributeFixture.anAttribute()
                                                                                                                               .called("NON RECURRING CHARGES")
                                                                                                                               .withDefaultValue("0")
                                                                                                                               .build())
                                                                                                .withAttribute(AttributeFixture.anAttribute()
                                                                                                                               .called("RECURRING CHARGES")
                                                                                                                               .withDefaultValue("0")
                                                                                                                               .build())
                                                                                                .withChargingScheme(new ProductChargingScheme("scheme", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Sales))
                                                                                                .withSiteSpecific()
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();
        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);


        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withSheetIndex(1)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                        new ECRFSheetModelAttribute("NON RECURRING CHARGES", "200"),
                                                                                        new ECRFSheetModelAttribute("RECURRING CHARGES", "85")))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals(ATTRIBUTE_DEFAULT_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME_DEFAULT_VALUE).getStringValue());
        assertFalse(importResults.hasErrors());
        verify(productInstanceClient).put(productInstance);
    }

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
                                                                              .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
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

        ImportResults importResults = new ImportResults();

        //When
        Set<LineItemId> lineItemIds = lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), relatedProductInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("relatedLineItem")), false, ProductCategoryCode.NIL);

        //verify
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(productInstanceClient, times(1)).put(relatedProductInstance);
        assertFalse(importResults.hasErrors());
        assertThat(lineItemIds.size(), is(2));
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
                                                                              .withSiteId(ROOT_SITE_ID).withContractTerm(CONTRACT_TERM)
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

        ImportResults importResults = new ImportResults();

        //When
        Set<LineItemId> lineItemIds = lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), relatedProductInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("relatedLineItem")), false, new ProductCategoryCode(""));

        //verify
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(productInstanceClient, times(1)).put(relatedProductInstance);
        assertFalse(importResults.hasErrors());
        assertThat(lineItemIds.size(), is(2));
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
        Set<LineItemId> impactedLineItemId = lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));

        //verify
        assertTrue(importResults.hasErrors());
        assertImportResultContainsErrorMessage(importResults, "Contract Cardinality Failed - aProduct can have only 0 instance(s) for the Customer.");
        assertTrue(impactedLineItemId.size() == 0);
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldAddTheExceptionToListAndContinueImportingNextProductWhenProductLevelExceptionsReported() throws InstanceCharacteristicNotFound {
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
        ProductInstance secondInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("secondInstance").withSiteId(ROOT_SITE_ID)
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
                                                                                        new ECRFSheetModelAttribute(SITE_ID, ROOT_SITE_ID),
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_WITH_SOURCE_RULE, null)))
                                                                                    .build())
                                                   .build();

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
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));

        //verify
        assertImportResultContainsErrorMessage(importResults, "Site Id : \"wrongSiteId\" for this Customer cannot be found");
        verify(productInstanceClient).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
        assertThat(lineItemsImpacted.size(), is(2));
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


        ImportResults importResults = new ImportResults();
        AssetKeyContainer assetKeyContainer = new AssetKeyContainer();
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, assetKeyContainer, productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, ProductCategoryCode.NIL);

        //verify
        checkAssetKeyPresentInContainerForEachRowID(ecrfWorkBook, assetKeyContainer);
        verify(productInstanceClient).put(productInstance);
        assertFalse(importResults.hasErrors());
        assertThat(lineItemsImpacted.size(), is(1));
    }

    @Test
    public void shouldSaveFirstRootInstanceWhenChildHasNoErrorsAndRoolbackSecondInstanceWhenItsChildHasErrors() throws InstanceCharacteristicNotFound {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE))
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier("Another Relationship"))
                                                                       )
                                                                       .build();

        ProductInstance secondProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("secondLineItemId")
                                                                             .withSiteId(ROOT_SITE_ID)
                                                                             .withContractTerm(CONTRACT_TERM)
                                                                             .withProductOffering(ProductOfferingFixture
                                                                                                      .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                      .withProductIdentifier(
                                                                                                          ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                      .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE))
                                                                                                      .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier("Another Relationship"))
                                                                             )
                                                                             .build();

        ProductInstance childProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                            .withSiteId(ROOT_SITE_ID)
                                                                            .withContractTerm(CONTRACT_TERM)
                                                                            .withProductOffering(ProductOfferingFixture
                                                                                                     .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                     .withAttribute(STENCIL_ATTRIBUTE)
                                                                                                     .withStencil(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                     .withAttribute(AttributeFixture
                                                                                                                        .anAttribute()
                                                                                                                        .called("anAttribute")
                                                                                                                        .withDataType(AttributeDataType.NUMBER)
                                                                                                                        .withMaxLength(10)
                                                                                                                        .withMinLength(8).build())
                                                                                                     .withProductIdentifier(
                                                                                                         CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                     .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE)))
                                                                            .build();

        childProductInstance.setStencilId(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID)));
        childProductInstance.getProductOffering().setAvailableStencils(newArrayList(StencilInfo.newInstance(StencilCode.newInstance(STENCIL_ID), STENCIL_ATTRIBUTE)));

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID_2)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .build();

        ECRFSheet ecrfSheetModelForChild = ECRFModelFixture.aECRFModel()
                                                           .withScode(CHILD_PRODUCT_CODE_IMPORTABLE)
                                                           .withSheetName(CHILD_SHEET_NAME)
                                                           .withSheetIndex(2)
                                                           .withSheetTypeStrategy(SheetTypeStrategy.Child)
                                                           .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                            .withRowId(CHILD_PRODUCT_ROW_ID)
                                                                                            .withParentId(ROOT_PRODUCT_ROW_ID_2)
                                                                                            .withSheetName(CHILD_SHEET_NAME)
                                                                                            .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                         new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                         new ECRFSheetModelAttribute("Contract Term", "Term"))
                                                                                            )
                                                                                            .build())
                                                           .build();

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        when(productInstanceClient.get(new LineItemId("secondLineItemId"))).thenReturn(secondProductInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForChild)).withControlSheet().build();
        when(productInstanceClient.createProductInstance(CHILD_PRODUCT_CODE_IMPORTABLE, null, productInstance.getLineItemId(), ROOT_SITE_ID, childProductInstance.getCustomerId(), childProductInstance.getContractId(),
                                                         childProductInstance.getQuoteOptionId(), StencilId.NIL, childProductInstance.getProjectId(), childProductInstance.getContractTerm(), quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childProductInstance);
        when(productInstanceClient.createProductInstance(eq(ROOT_PRODUCT_CODE_IMPORTABLE), anyString(), anyString(), anyString(), eq(secondProductInstance.getCustomerId()), eq(secondProductInstance.getContractId()),
                                                         eq(secondProductInstance.getQuoteOptionId()), eq(StencilId.NIL), eq(secondProductInstance.getProjectId()), eq(secondProductInstance.getContractTerm()), eq(quoteOptionClient), eq(ProductCategoryCode.NIL)))
            .thenReturn(childProductInstance);
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, ProductCategoryCode.NIL);

        //verify
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(quoteOptionClient, times(1)).deleteQuoteOptionItem(eq(secondProductInstance.getProjectId()), eq(secondProductInstance.getQuoteOptionId()), anyString());
        assertTrue(importResults.hasErrors());
        assertImportResultContainsErrorMessage(importResults, String.format(ECRFImportException.stencilMissingForStencilProduct, CHILD_SHEET_NAME, ROOT_PRODUCT_ROW_ID_2));
        assertThat(lineItemsImpacted.size(), is(1));
    }

    @Test
    public void shouldNotRollBackRootLineItemInstancesForTheImportingLineItemEvenInCaseOfFailure() throws InstanceCharacteristicNotFound {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withSiteId(ROOT_SITE_ID)
                                                                       .withContractTerm(CONTRACT_TERM)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE)
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(CHILD_PRODUCT_CODE_IMPORTABLE))
                                                                                                .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier("Another Relationship"))
                                                                       )
                                                                       .build();

        ProductInstance childProductInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("childLineItemId")
                                                                            .withSiteId(ROOT_SITE_ID)
                                                                            .withContractTerm(CONTRACT_TERM)
                                                                            .withProductOffering(ProductOfferingFixture
                                                                                                     .aProductOffering().withAttribute(ATTRIBUTE_NAME)
                                                                                                     .withAttribute(STENCIL_ATTRIBUTE)
                                                                                                     .withStencil(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID), ProductName.newInstance(STENCIL_ATTRIBUTE)))
                                                                                                     .withAttribute(AttributeFixture
                                                                                                                        .anAttribute()
                                                                                                                        .called("anAttribute")
                                                                                                                        .withDataType(AttributeDataType.NUMBER)
                                                                                                                        .withMaxLength(10)
                                                                                                                        .withMinLength(8).build())
                                                                                                     .withProductIdentifier(
                                                                                                         CHILD_PRODUCT_CODE_IMPORTABLE)
                                                                                                     .withSalesRelationship(new SalesRelationshipFixture().withProductIdentifier(SECOND_CHILD_PRODUCT_CODE_IMPORTABLE)))
                                                                            .build();

        childProductInstance.setStencilId(StencilId.latestVersionFor(StencilCode.newInstance(STENCIL_ID)));
        childProductInstance.getProductOffering().setAvailableStencils(newArrayList(StencilInfo.newInstance(StencilCode.newInstance(STENCIL_ID), STENCIL_ATTRIBUTE)));

        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
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
                                                                                            .withSheetName(CHILD_SHEET_NAME)
                                                                                            .withAttributes(newArrayList(new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE),
                                                                                                                         new ECRFSheetModelAttribute(STENCIL_ATTRIBUTE, STENCIL_ATTRIBUTE),
                                                                                                                         new ECRFSheetModelAttribute("Contract Term", "Term"))
                                                                                            )
                                                                                            .build())
                                                           .build();

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel, ecrfSheetModelForChild)).withControlSheet().build();
        when(productInstanceClient.createProductInstance(CHILD_PRODUCT_CODE_IMPORTABLE, null, productInstance.getLineItemId(), ROOT_SITE_ID, childProductInstance.getCustomerId(), childProductInstance.getContractId(),
                                                         childProductInstance.getQuoteOptionId(), StencilId.NIL, childProductInstance.getProjectId(), childProductInstance.getContractTerm(), quoteOptionClient, ProductCategoryCode.NIL))
            .thenReturn(childProductInstance);
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, ProductCategoryCode.NIL);

        //verify
        verify(productInstanceClient, times(0)).put(productInstance);
        verify(quoteOptionClient, never()).deleteQuoteOptionItem(eq(productInstance.getProjectId()), eq(productInstance.getQuoteOptionId()), anyString());
        assertImportResultContainsErrorMessage(importResults, "The attribute Contract Term is not configured for this product");
        assertThat(lineItemsImpacted.size(), is(0));
    }

    @Test
    public void shouldImportInstanceAndExtractDeliveryAddressDetailFromWorkBook() throws InstanceCharacteristicNotFound {
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

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(projectId)).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(quoteOptionId)).thenReturn(quoteOptionItemResource);

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
                                                   .build();
        List<ECRFSheet> nonProductSheetModel = newArrayList(ECRFModelFixture.aECRFModel()
                                                                            .withSheetIndex(2)
                                                                            .withSheetName(DELIVERY_ADDRESS)
                                                                            .withSheetTypeStrategy(SheetTypeStrategy.NonProduct)
                                                                            .withRow(ECRFSheetModelRowFixture.aDeliveryAddressModelRow(ROOT_PRODUCT_ROW_ID)).build());

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withNonProductECRFSheets(nonProductSheetModel).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals(ATTRIBUTE_DEFAULT_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME_DEFAULT_VALUE).getStringValue());

        assertFalse(importResults.hasErrors());
        verify(productInstanceClient).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
        verify(quoteOptionItemResource, atLeastOnce()).createDeliveryAddressForLineItem(anyString(), any(DeliveryAddressDTO.class));
        assertThat(lineItemsImpacted.size(), is(1));
        assertTrue(lineItemsImpacted.contains(new LineItemId(productInstance.getLineItemId())));
    }

    @Test
    public void shouldNeverTryToExtractDeliveryAddressWhenTheRowNotMappedToAnyDeliveryAddress() throws InstanceCharacteristicNotFound {
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

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(projectId)).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(quoteOptionId)).thenReturn(quoteOptionItemResource);

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
                                                   .build();
        List<ECRFSheet> nonProductSheetModel = newArrayList(ECRFModelFixture.aECRFModel()
                                                                            .withSheetIndex(2)
                                                                            .withSheetName(DELIVERY_ADDRESS)
                                                                            .withSheetTypeStrategy(SheetTypeStrategy.NonProduct)
                                                                            .withRow(ECRFSheetModelRowFixture.aDeliveryAddressModelRow("someRowId")).build());

        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withNonProductECRFSheets(nonProductSheetModel).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals(ATTRIBUTE_DEFAULT_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME_DEFAULT_VALUE).getStringValue());

        assertFalse(importResults.hasErrors());
        verify(productInstanceClient).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
        verify(quoteOptionItemResource, never()).createDeliveryAddressForLineItem(eq(quoteOptionId), any(DeliveryAddressDTO.class));
        assertThat(lineItemsImpacted.size(), is(1));
        assertTrue(lineItemsImpacted.contains(new LineItemId(productInstance.getLineItemId())));
    }

    @Test
    public void shouldImportProductWhenConractCardinalityIsOneAndOnlyOneProductImported() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withLineItemId("LineItemId")
                                                                       .withAssetKey("anAssetId", 1L)
                                                                       .withProductOffering(ProductOfferingFixture
                                                                                                .aProductOffering().withContractCardinality(new Cardinality(0, 1, null)).withAttribute(ATTRIBUTE_NAME)
                                                                                                .withProductIdentifier(new ProductIdentifier(ROOT_PRODUCT_CODE_IMPORTABLE, "aProduct", "1"))
                                                                       )
                                                                       .build();

        when(futureProductInstanceClient.get(new LineItemId(lineItemId))).thenReturn(productInstance);
        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("DEFAULT-TEST-CUSTOMER-ID")).thenReturn(siteResource);
        when(siteResource.get("DEFAULT-TEST-PROJECT-ID", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productOfferings.get()).thenReturn(productInstance.getProductOffering());
        when(pmrClient.productOffering(ProductSCode.newInstance(ROOT_PRODUCT_CODE_IMPORTABLE))).thenReturn(productOfferings);
        when(productInstanceClient.get(Optional.of(new LineItemId("LineItemId")).get())).thenReturn(productInstance);

        when(productInstanceClient.getContractAssets(any(CustomerId.class), any(ContractId.class), any(ProductCode.class), any(ProductVersion.class), any(AssetFilter.class), any(AssetFilter.class))).thenReturn(newArrayList(new AvailableAsset("anAssetId", 1L)));


        ECRFSheet ecrfSheetModel = ECRFModelFixture.aECRFModel()
                                                   .withScode(ROOT_PRODUCT_CODE_IMPORTABLE)
                                                   .withSheetName(ROOT_SHEET_NAME)
                                                   .withSheetIndex(1)
                                                   .withSheetTypeStrategy(SheetTypeStrategy.Parent)
                                                   .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                    .withRowId(ROOT_PRODUCT_ROW_ID)
                                                                                    .withAttributes(newArrayList(
                                                                                        new ECRFSheetModelAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE)))
                                                                                    .build())
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        lineItemBasedImporter.importFromSheet(productInstance.getCustomerId(), productInstance.getContractId(), productInstance.getContractTerm(), productInstance.getProjectId(), productInstance.getQuoteOptionId(), ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), false, new ProductCategoryCode(""));
        assertFalse(importResults.hasErrors());
        verify(productInstanceClient, times(1)).put(productInstance);
    }

    @Test
    public void shouldAddMigrationAttributeIntoProductInstanceIfMigration() throws Exception {
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
                                                                                                .withAttribute(AttributeFixture.anAttribute()
                                                                                                                               .called(ProductOffering.MIGRATING_ASSET)
                                                                                                                               .build())
                                                                                                .withProductIdentifier(
                                                                                                    ROOT_PRODUCT_CODE_IMPORTABLE))
                                                                       .build();

        when(pmrClient.getProductHCode(ROOT_PRODUCT_CODE_IMPORTABLE)).thenReturn(Optional.of(new ProductIdentifier("H012345", "product category", "versionNumber")));
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(customerId)).thenReturn(siteResource);
        when(siteResource.get(projectId, SiteResource.SiteFilterType.All)).thenReturn(siteDtos);

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
                                                   .build();
        ECRFWorkBook ecrfWorkBook = ECRFWorkBookFixture.aECRFWorkBook().withECRFSheets(newArrayList(ecrfSheetModel)).withControlSheet().build();
        ImportResults importResults = new ImportResults();
        Set<LineItemId> lineItemsImpacted = lineItemBasedImporter.importFromSheet(customerId, contractId, contractTrem, projectId, quoteOptionId, ecrfWorkBook, importResults, new AssetKeyContainer(), productInstance.getProductIdentifier().getProductId(), Optional.of(new LineItemId("LineItemId")), true, new ProductCategoryCode(""));
        assertEquals(ATTRIBUTE_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME).getStringValue());
        assertEquals(ATTRIBUTE_DEFAULT_VALUE, productInstance.getInstanceCharacteristic(ATTRIBUTE_NAME_DEFAULT_VALUE).getStringValue());
        assertEquals("Yes", productInstance.getInstanceCharacteristic(ProductOffering.MIGRATING_ASSET).getStringValue());

        assertFalse(importResults.hasErrors());
        verify(productInstanceClient).put(productInstance);
        verify(productInstanceClient, times(1)).refreshAttributesOfProductInstance(productInstance);
        assertThat(lineItemsImpacted.size(), is(1));
        assertTrue(lineItemsImpacted.contains(new LineItemId(productInstance.getLineItemId())));
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
