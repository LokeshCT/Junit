package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.client.QuoteOptionClient;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.ChangeAssetDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.extensions.CardinalityExpression;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.DefaultProductInstance;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.pc.client.ConfiguratorClient;
import com.bt.rsqe.pc.client.ConfiguratorContractClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.AssetKeyContainer;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

public class ECRFImporterDefaultedRelationshipsTest {
    public static final String AUTO_DEFAULTED_RELATIONSHIP = "AutoDefaultedRelationship";
    private ProductInstanceClient productInstanceClient;
    private SiteFacade siteFacade;
    private QuoteOptionClient quoteOptionClient;
    private ProductInstance autoDefaultedChildAsset;
    private ProductInstance autoDefaultedGrandChildAsset;
    private ProductInstance rootProductInstance;
    private DefaultProductInstance createdChildInstance1;
    private DefaultProductInstance createdChildInstance2;
    private DefaultProductInstance createdGrandChildInstance;
    private ProductIdentifier childIdentifier = new ProductIdentifier("ChildCode", "1");
    private ProductIdentifier grandChildIdentifier = new ProductIdentifier("GrandChildCode", "1");
    private PmrClient pmr;
    private CustomerResource customerResource;
    private static final String CHILD="CHILD_AUTO";
    private static final String GRAND_CHILD="GRAND_CHILD";
    private ConfiguratorContractClient configuratorClient;
    private ProjectResource projectResource;
    private ProductRelationshipService productRelationshipService;


    @Before
    public void before() throws IOException {
        projectResource = mock(ProjectResource.class);
        productRelationshipService = mock(ProductRelationshipService.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        siteFacade = mock(SiteFacade.class);
        quoteOptionClient = mock(QuoteOptionClient.class);
        customerResource = mock(CustomerResource.class);
        configuratorClient = mock(ConfiguratorClient.class);

        pmr = mock(PmrClient.class);
        UserContext userContext = new UserContext("login", "token", "channel");
        userContext.getPermissions().indirectUser = false;
        UserContextManager.setCurrent(userContext);

    }

    @Test
    public void shouldRemoveChildRelationshipsWhichHaveBeenAutoDefaultedFromRootAndCreatedAssets() {
        ProductBasedImporter productBasedImporter = new ProductBasedImporter(productInstanceClient, quoteOptionClient, pmr, new CardinalityValidator(productInstanceClient, siteFacade), customerResource,configuratorClient, projectResource, productRelationshipService);

        createRootInstance();
        ECRFWorkBook ecrfWorkbook = createECRFWorkbook();
        when(productInstanceClient.get(new LineItemId(rootProductInstance.getLineItemId()))).thenReturn(rootProductInstance);
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(rootProductInstance.getCustomerId())).thenReturn(siteResource);
        ArrayList<SiteDTO> siteDtos = newArrayList(SiteDTOFixture.aSiteDTO().withBfgSiteId("1234").build());
        when(siteResource.get(rootProductInstance.getProjectId(), SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productInstanceClient.createProductInstance(eq(rootProductInstance.getProductOffering().getProductIdentifier().getProductId()), eq(rootProductInstance.getProductOffering().getProductIdentifier().getVersionNumber()), anyString(), eq(rootProductInstance.getSiteId()),
                                                         eq(rootProductInstance.getCustomerId()), eq(rootProductInstance.getContractId()), eq(rootProductInstance.getQuoteOptionId()), any(StencilId.class), eq(rootProductInstance.getProjectId()),
                                                         eq(rootProductInstance.getContractTerm()), any(QuoteOptionClient.class), any(ProductCategoryCode.class))).thenReturn(rootProductInstance);

        Pmr.ProductOfferings productOfferings = mock(Pmr.ProductOfferings.class);
        when(productOfferings.get()).thenReturn(rootProductInstance.getProductOffering());
        when(pmr.productOffering(ProductSCode.newInstance("aScode"))).thenReturn(productOfferings);

        ImportResults results = new ImportResults();
        productBasedImporter.importFromSheet(rootProductInstance.getCustomerId(), rootProductInstance.getContractId(), rootProductInstance.getContractTerm(), rootProductInstance.getProjectId(), rootProductInstance.getQuoteOptionId(), ecrfWorkbook,
                                             results, new AssetKeyContainer(), "aScode", Optional.<LineItemId>absent(), false, new ProductCategoryCode(""));

        verify(productInstanceClient, times(1)).deleteAsset(eq(ChangeAssetDTO.delete(autoDefaultedChildAsset)));
        verify(productInstanceClient, times(1)).deleteAsset(eq(ChangeAssetDTO.delete(autoDefaultedGrandChildAsset)));
        verify(productInstanceClient, never()).deleteAsset(not(or(eq(ChangeAssetDTO.delete(autoDefaultedChildAsset)), eq(ChangeAssetDTO.delete(autoDefaultedGrandChildAsset)))));

        verify(productInstanceClient, times(2)).createProductInstance(eq(childIdentifier.getProductId()), anyString(), anyString(), anyString(),
                                                                      anyString(), anyString(), anyString(), any(StencilId.class), anyString(),
                                                                      anyString(), any(QuoteOptionClient.class), any(ProductCategoryCode.class));
        verify(productInstanceClient, times(1)).createProductInstance(eq(grandChildIdentifier.getProductId()), anyString(), anyString(), anyString(),
                                                                      anyString(), anyString(), anyString(), any(StencilId.class), anyString(),
                                                                      anyString(), any(QuoteOptionClient.class), any(ProductCategoryCode.class));
        ArgumentCaptor<DefaultProductInstance> argumentCaptor = ArgumentCaptor.forClass(DefaultProductInstance.class);
        verify(productInstanceClient).put(argumentCaptor.capture());
        DefaultProductInstance savedAsset = argumentCaptor.getValue();
        assertThat(savedAsset.getRelationships().size(), is(2));
        assertThat(savedAsset.getRelationships().get(0).getRelatedProductInstance().getRelationships().size(), is(1));
        assertThat(savedAsset.getRelationships().get(1).getRelatedProductInstance().getRelationships().size(), is(0));
    }

    @Test
    public void shouldAutoAddRelatedInstanceBasedOnCardinality() {
        ProductBasedImporter productBasedImporter = new ProductBasedImporter(productInstanceClient, quoteOptionClient, pmr, new CardinalityValidator(productInstanceClient, siteFacade), customerResource,configuratorClient, projectResource, productRelationshipService);

        createRootInstance();
        createCardinalRelations();
        ECRFWorkBook ecrfWorkbook = createECRFWorkbook();
        when(productInstanceClient.get(new LineItemId("lineItemId"))).thenReturn(rootProductInstance);
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource("custId")).thenReturn(siteResource);
        ArrayList<SiteDTO> siteDtos = newArrayList(SiteDTOFixture.aSiteDTO().withBfgSiteId("1234").build());
        when(siteResource.get("projId", SiteResource.SiteFilterType.All)).thenReturn(siteDtos);
        when(productInstanceClient.createProductInstance(anyString(), anyString(), anyString(), anyString(),
                                                                      anyString(), anyString(), anyString(), any(StencilId.class), anyString(),
                                                                      anyString(), any(QuoteOptionClient.class), any(ProductCategoryCode.class))).thenReturn(rootProductInstance);


        ImportResults results = new ImportResults();
        productBasedImporter.importFromSheet("custId", "contId", "contractTerm", "projId", "quoteOptionId",
                                             ecrfWorkbook, results, new AssetKeyContainer(), "aScode", Optional.of(new LineItemId("lineItemId")), false, ProductCategoryCode.NIL);

        ArgumentCaptor<DefaultProductInstance> argumentCaptor = ArgumentCaptor.forClass(DefaultProductInstance.class);
        verify(productInstanceClient).put(argumentCaptor.capture());
        DefaultProductInstance savedAsset = argumentCaptor.getValue();
        assertThat(savedAsset.getProductSalesRelationshipInstances(CHILD).size(), is(1));

        ProductInstance childProductInstance = savedAsset.getProductSalesRelationshipInstances(CHILD).get(0).getRelatedProductInstance();
        assertThat(childProductInstance.getProductSalesRelationshipInstances(GRAND_CHILD).size(), is(1));
    }

    private void createCardinalRelations() {
        ProductIdentifier newChildIdentifier = new ProductIdentifier(CHILD, "1");
        ProductIdentifier grandChildProductIdentifier = new ProductIdentifier(GRAND_CHILD, "1");
        Expression minimumExpression = mock(Expression.class);
        when(minimumExpression.getExpressionText()).thenReturn("if(1>0, 1, 0)");
        Expression defaultExpression = mock(Expression.class);
        when(defaultExpression.getExpressionText()).thenReturn("if(1>0, 1, 0)");
        Expression maximumExpression = mock(Expression.class);
        when(maximumExpression.getExpressionText()).thenReturn("if(1>0, 1, 0)");
        CardinalityExpression cardinalityExpression = new CardinalityExpression(minimumExpression, defaultExpression, maximumExpression);

        SalesRelationshipFixture salesRelationship = SalesRelationshipFixture.aSalesRelationship()
                                                                              .withRelationName(GRAND_CHILD)
                                                                              .withProductIdentifier(GRAND_CHILD)
                                                                              .withCardinalityExpression(cardinalityExpression);

        final ProductOfferingFixture childProductOffering = ProductOfferingFixture.aProductOffering()
                                                                                  .withSalesRelationship(salesRelationship)
                                                                                  .withProductIdentifier(newChildIdentifier);

        final ProductOfferingFixture grandChildProductOffering = ProductOfferingFixture.aProductOffering()
                                                                                       .withProductIdentifier(grandChildProductIdentifier);

        SalesRelationshipFixture childSalesRelationship = SalesRelationshipFixture.aSalesRelationship()
                                                                              .withRelationName(CHILD)
                                                                              .withProductIdentifier(CHILD)
                                                                              .withCardinalityExpression(CardinalityExpression.NIL);

        ProductInstance createdChildInstance3 = DefaultProductInstanceFixture.aProductInstance()
                                                             .withProductIdentifier(newChildIdentifier)
                                                             .withProductOffering(childProductOffering).build();

        ProductInstance grandChildInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                 .withProductIdentifier(grandChildProductIdentifier)
                                                                 .withProductOffering(grandChildProductOffering).build();

        when(productInstanceClient.createProductInstance(eq(grandChildProductIdentifier.getProductId()), anyString(), anyString(),anyString(),
                                                      anyString(), anyString(), anyString(), any(StencilId.class), anyString(),
                                                      anyString(), anyString(), anyString(), any(QuoteOptionClient.class),
                                                      any(Optional.class), any(Optional.class),
                eq(ProductCategoryCode.NIL))).thenReturn(grandChildInstance);

        Pmr.ProductOfferings productOfferings = mock(Pmr.ProductOfferings.class);
        when(pmr.productOffering(any(ProductSCode.class))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(childProductOffering.build());

        ProductSalesRelationshipInstance newRelationshipInstance = new ProductSalesRelationshipInstance(childSalesRelationship.build(), createdChildInstance3, rootProductInstance);
        rootProductInstance.addRelationship(newRelationshipInstance);
    }


    private ECRFWorkBook createECRFWorkbook() {
        ECRFWorkBook workBook = mock(ECRFWorkBook.class);

        ECRFSheet rootSheet = mock(ECRFSheet.class);
        ECRFSheet childSheet = mock(ECRFSheet.class);
        ECRFSheet grandChildSheet = mock(ECRFSheet.class);
        ECRFSheetModelRow rootSheetRowZero = mock(ECRFSheetModelRow.class);
        ECRFSheetModelRow childSheetRowZero = mock(ECRFSheetModelRow.class);
        ECRFSheetModelRow childSheetRowOne = mock(ECRFSheetModelRow.class);
        ECRFSheetModelRow grandChildSheetRowZero = mock(ECRFSheetModelRow.class);

        when(workBook.getSheetBySheetIndex(1)).thenReturn(rootSheet);
        when(workBook.getSheetBySheetIndex(2)).thenReturn(childSheet);
        when(workBook.getSheetBySheetIndex(3)).thenReturn(grandChildSheet);
        Map<String,String> controlSheet = new HashMap<String, String>();
        controlSheet.put("rootSheet", "aProductCode1");
        controlSheet.put("childSheet", "aProductCode2");
        controlSheet.put("grandChildSheet", "aProductCode3");
        when(rootSheet.getRow(0)).thenReturn(rootSheetRowZero);
        when(rootSheet.getRows()).thenReturn(newArrayList(rootSheetRowZero));
        when(rootSheet.getSheetIndex()).thenReturn(1);
        when(rootSheet.isParentSheet()).thenReturn(true);
        when(rootSheet.isChildSheet()).thenReturn(false);
        when(rootSheet.getProductCode()).thenReturn("aScode");
        when(childSheet.getRow(0)).thenReturn(childSheetRowZero);
        when(childSheet.getRow(0)).thenReturn(childSheetRowOne);
        when(childSheet.getRows()).thenReturn(newArrayList(childSheetRowZero, childSheetRowOne));
        when(childSheet.getProductCode()).thenReturn(childIdentifier.getProductId());
        when(childSheet.isChildSheet()).thenReturn(true);
        when(childSheet.getSheetIndex()).thenReturn(2);
        when(grandChildSheet.getRow(0)).thenReturn(grandChildSheetRowZero);
        when(grandChildSheet.getRows()).thenReturn(newArrayList(grandChildSheetRowZero));
        when(grandChildSheet.getProductCode()).thenReturn(grandChildIdentifier.getProductId());
        when(grandChildSheet.getSheetIndex()).thenReturn(3);
        when(grandChildSheet.isChildSheet()).thenReturn(true);

        when(rootSheetRowZero.getRowId()).thenReturn("RootId");
        when(childSheetRowZero.getParentId()).thenReturn("RootId");
        when(childSheetRowZero.getRowId()).thenReturn("Child1Id");
        when(childSheetRowOne.getParentId()).thenReturn("RootId");
        when(childSheetRowOne.getRowId()).thenReturn("Child2Id");
        when(grandChildSheetRowZero.getParentId()).thenReturn("Child1Id");
        when(rootSheetRowZero.getStencil()).thenReturn(Optional.<String>absent());
        when(childSheetRowZero.getStencil()).thenReturn(Optional.<String>absent());
        when(childSheetRowOne.getStencil()).thenReturn(Optional.<String>absent());
        when(grandChildSheetRowZero.getStencil()).thenReturn(Optional.<String>absent());

        when(workBook.getSheetByProductCode(rootProductInstance.getProductOffering().getProductIdentifier().getProductId()))
            .thenReturn(rootSheet);
        when(workBook.getSheets()).thenReturn(newArrayList(rootSheet, childSheet, grandChildSheet));
        when(workBook.getControlSheet()).thenReturn(controlSheet);

        return workBook;
    }

    private void createRootInstance() {
        SalesRelationshipFixture salesRelationship1 = SalesRelationshipFixture.aSalesRelationship()
                                                                              .withRelationName(AUTO_DEFAULTED_RELATIONSHIP)
                                                                              .withProductIdentifier(childIdentifier)
                                                                              .withCardinalityExpression(CardinalityExpression.NIL);
        SalesRelationshipFixture salesRelationship2 = SalesRelationshipFixture.aSalesRelationship()
                                                                              .withRelationName(AUTO_DEFAULTED_RELATIONSHIP)
                                                                              .withProductIdentifier(grandChildIdentifier)
                                                                              .withCardinalityExpression(CardinalityExpression.NIL);
        final ProductOfferingFixture rootProductOffering = ProductOfferingFixture.aProductOffering()
                                                                                 .withSalesRelationship(salesRelationship1);
        final ProductOfferingFixture childProductOffering = ProductOfferingFixture.aProductOffering()
                                                                                  .withSalesRelationship(salesRelationship2)
                                                                                  .withProductIdentifier(childIdentifier);
        final ProductOfferingFixture grandChildProductOffering = ProductOfferingFixture.aProductOffering()
                                                                                       .withProductIdentifier(grandChildIdentifier);
        autoDefaultedChildAsset = DefaultProductInstanceFixture.aProductInstance()
                                                               .withProductIdentifier(childIdentifier)
                                                               .withProductOffering(childProductOffering).build();
        autoDefaultedGrandChildAsset = DefaultProductInstanceFixture.aProductInstance()
                                                                    .withProductIdentifier(grandChildIdentifier)
                                                                    .withProductOffering(grandChildProductOffering).build();
        createdChildInstance1 = DefaultProductInstanceFixture.aProductInstance()
                                                             .withProductIdentifier(childIdentifier)
                                                             .withChildProductInstance(autoDefaultedGrandChildAsset, AUTO_DEFAULTED_RELATIONSHIP)
                                                             .withProductOffering(childProductOffering).build();
        createdChildInstance2 = DefaultProductInstanceFixture.aProductInstance()
                                                             .withProductIdentifier(childIdentifier)
                                                             .withProductOffering(childProductOffering).build();
        createdGrandChildInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                 .withProductIdentifier(grandChildIdentifier)
                                                                 .withProductOffering(grandChildProductOffering).build();
        rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                           .withProductOffering(rootProductOffering)
                                                           .withChildProductInstance(autoDefaultedChildAsset, AUTO_DEFAULTED_RELATIONSHIP).build();
        when(productInstanceClient.createProductInstance(eq(childIdentifier.getProductId()), anyString(), anyString(), anyString(),
                                                         anyString(), anyString(), anyString(), any(StencilId.class), anyString(),
                                                         anyString(), any(QuoteOptionClient.class), any(ProductCategoryCode.class)))
            .thenReturn(createdChildInstance1)
            .thenReturn(createdChildInstance2);
        when(productInstanceClient.createProductInstance(eq(grandChildIdentifier.getProductId()), anyString(), anyString(), anyString(),
                                                         anyString(), anyString(), anyString(), any(StencilId.class), anyString(),
                                                         anyString(), any(QuoteOptionClient.class), any(ProductCategoryCode.class)))
            .thenReturn(createdGrandChildInstance);

        doThrow(ResourceNotFoundException.class).when(productInstanceClient).deleteAsset(eq(ChangeAssetDTO.delete(autoDefaultedGrandChildAsset)));

        when(siteFacade.getCentralSite(anyString(), anyString())).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("1").build());
    }
}
