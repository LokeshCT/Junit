package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.BillingTariffRuleSet;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.ProductSalesRelationship;
import com.bt.rsqe.domain.product.parameters.RelatedProductIdentifier;
import com.bt.rsqe.domain.product.parameters.Relationship;
import com.bt.rsqe.domain.product.parameters.RelationshipGroup;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.ResolvesTo;
import com.bt.rsqe.domain.project.DefaultProductInstance;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.PriceBookFacade;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ContractDialogResourceHandlerTest {
    @Mock
    private Presenter presenter;
    @Mock
    private ProjectResource projectResource;
    @Mock
    private JSONSerializer jsonSerializer;
    @Mock
    private PriceBookFacade priceBookFacade;
    @Mock
    private UserContext userContext;
    @Mock
    private ProductInstanceClient productInstanceClient;


    private ContractDialogResourceHandler contractDialogResourceHandler;
    private String projectId;
    private String customerId;
    private String contractId;
    private String quoteOptionId;
    private String quoteOptionItemId;
    private QuoteOptionItemDTO quoteOptionItemDTO;
    private QuoteOptionResource quoteOptionResource;
    private QuoteOptionItemResource quoteOptionItemResource;
    private ProductInstance productInstance;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        projectId = "projectId";
        customerId = "customerId";
        contractId = "contractId";
        quoteOptionId = "quoteOptionId";
        quoteOptionItemId = "quoteOptionItemId";
        quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();

        quoteOptionResource = mock(QuoteOptionResource.class);
        quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        productInstance = getProductInstance();

        when(projectResource.quoteOptionResource(projectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(quoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get(quoteOptionItemId)).thenReturn(quoteOptionItemDTO);
        when(productInstanceClient.get(new LineItemId(quoteOptionItemId))).thenReturn(productInstance);

        contractDialogResourceHandler = new ContractDialogResourceHandler(presenter, projectResource, jsonSerializer, priceBookFacade, productInstanceClient);
    }

    @Test
    public void shouldLoadContractFormWithContractTermAndPriceBookFromAsIsProductContractDTOAndExpedio() {
        UserContextManager.setCurrent(userContext);
        when(priceBookFacade.getLatestPriceBookForIndirectUser(customerId, quoteOptionItemDTO.sCode, ProductCategoryCode.NIL)).thenReturn(new PriceBookDTO("1", "someRequestId", "eup", "ptp", null, null));
        when(presenter.render(Matchers.<View>any())).thenReturn("page");
        PermissionsDTO permissionsDTO = new PermissionsDTO(false,true,true,true, true, false);
        when(userContext.getPermissions()).thenReturn(permissionsDTO);
        ProductInstance productInstance = mock(ProductInstance.class);
        when(productInstance.getProductInstanceId()).thenReturn(new ProductInstanceId("1l"));
        when(productInstanceClient.get(any(LineItemId.class))).thenReturn(productInstance);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class)))
            .thenReturn(Optional.of(productInstance));
        when(productInstance.isCpe()).thenReturn(false);
        List<PriceLine> priceLines = asList(new PriceLineFixture().withChargedBookVersion("assetPTP").withEupPriceBookVersion("assetEUP").withChargingSchemeName("charge scheme name").build());
        List<PriceLine> relPriceLines = asList(new PriceLineFixture().withChargedBookVersion("relAssetPTP").withEupPriceBookVersion("relAssetEUP").withChargingSchemeName("charge scheme name").build());
        when(productInstance.getPriceLines()).thenReturn(priceLines);
        ProductSalesRelationshipInstance salesRelationshipInstance = mock(ProductSalesRelationshipInstance.class);
        when(productInstance.getActiveRelationships()).thenReturn(asList(salesRelationshipInstance));
        ProductInstance relProductInstance = mock(ProductInstance.class);
        when(salesRelationshipInstance.getRelatedProductInstance()).thenReturn(relProductInstance);
        when(relProductInstance.getPriceLines()).thenReturn(relPriceLines);
        Response response = contractDialogResourceHandler.contractForm(customerId, contractId, projectId, quoteOptionId, quoteOptionItemId);
        assertThat(response, is(aResponse().withStatusOK()));
        assertThat(response.getEntity(), is((Object)"page"));
        verify(priceBookFacade).getLatestPriceBookForIndirectUser(customerId, quoteOptionItemDTO.sCode, ProductCategoryCode.NIL);
        verify(productInstance, times(1)).getPriceLines();
        verify(relProductInstance, times(1)).getPriceLines();
        verify(productInstanceClient, times(1)).getSourceAsset(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()));
    }

    @Test
    public void shouldLoadContractFormWithContractTermAndPriceBookFromContractDTOAndExpedioForIndirectUser() {
        UserContextManager.setCurrent(userContext);
        when(priceBookFacade.getLatestPriceBookForIndirectUser(customerId, quoteOptionItemDTO.sCode, ProductCategoryCode.NIL)).thenReturn(new PriceBookDTO("1", "someRequestId", "eup", "ptp", null, null));
        when(presenter.render(Matchers.<View>any())).thenReturn("page");
        PermissionsDTO permissionsDTO = new PermissionsDTO(false,true,true,true, true, false);
        when(userContext.getPermissions()).thenReturn(permissionsDTO);
        ProductInstance instance = mock(ProductInstance.class);
        when(instance.getProductInstanceId()).thenReturn(new ProductInstanceId("1l"));
        when(productInstanceClient.get(any(LineItemId.class))).thenReturn(instance);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class)))
            .thenReturn(Optional.of(instance));
        when(instance.isCpe()).thenReturn(true);
        Response response = contractDialogResourceHandler.contractForm(customerId, contractId, projectId, quoteOptionId, quoteOptionItemId);
        assertThat(response, is(aResponse().withStatusOK()));
        assertThat(response.getEntity(), is((Object)"page"));
        verify(priceBookFacade).getLatestPriceBookForIndirectUser(customerId, quoteOptionItemDTO.sCode, ProductCategoryCode.NIL);
        verify(productInstanceClient, times(1)).get(new LineItemId(quoteOptionItemId));
    }

    @Test
    public void shouldLoadContractFormWithContractTermAndPriceBookFromContractDTOAndExpedioForDirectUser() {
        UserContextManager.setCurrent(userContext);
        when(priceBookFacade.getLatestPriceBookForDirectUser(quoteOptionItemDTO.sCode, ProductCategoryCode.NIL)).thenReturn(new PriceBookDTO("1", "someRequestId", "eup", "ptp", null, null));
        when(presenter.render(Matchers.<View>any())).thenReturn("page");
        PermissionsDTO permissionsDTO = new PermissionsDTO(false,true,true,false, true, false);
        when(userContext.getPermissions()).thenReturn(permissionsDTO);
        ProductInstance instance = mock(ProductInstance.class);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.of(instance));
        when(instance.isCpe()).thenReturn(true);
        Response response = contractDialogResourceHandler.contractForm(customerId, contractId, projectId, quoteOptionId, quoteOptionItemId);
        assertThat(response, is(aResponse().withStatusOK()));
        assertThat(response.getEntity(), is((Object)"page"));
        verify(priceBookFacade).getLatestPriceBookForDirectUser(quoteOptionItemDTO.sCode, ProductCategoryCode.NIL);
        verify(productInstanceClient, times(1)).get(new LineItemId(quoteOptionItemId));
    }

    @Test
    public void shouldUpdatePriceBookWithChangedValue() {
        final String eupPriceBook = "latest-eup";
        final String ptpPriceBook = "latest-ptp";

        when(quoteOptionItemResource.put(quoteOptionItemDTO)).thenReturn(quoteOptionItemDTO);

        Response response = contractDialogResourceHandler.updateContract(projectId, quoteOptionId, quoteOptionItemId, eupPriceBook, ptpPriceBook);

        assertThat(response, is(aResponse().withStatusOK()));
        assertThat(quoteOptionItemDTO.contractDTO.priceBooks.get(0).ptpPriceBook, is(ptpPriceBook));
        assertThat(quoteOptionItemDTO.contractDTO.priceBooks.get(0).eupPriceBook, is(eupPriceBook));
        verify(quoteOptionItemResource).put(quoteOptionItemDTO);
    }

    @Test
    public void shouldRemovePriceLineWithChangeOfPriceBook() {
        final String eupPriceBook = "latest-eup";
        final String ptpPriceBook = "latest-ptp";

        when(quoteOptionItemResource.put(quoteOptionItemDTO)).thenReturn(quoteOptionItemDTO);

        Response response = contractDialogResourceHandler.updateContract(projectId, quoteOptionId, quoteOptionItemId, eupPriceBook, ptpPriceBook);

        assertThat(response, is(aResponse().withStatusOK()));
        assertThat(quoteOptionItemDTO.contractDTO.priceBooks.get(0).ptpPriceBook, is(ptpPriceBook));
        assertThat(quoteOptionItemDTO.contractDTO.priceBooks.get(0).eupPriceBook, is(eupPriceBook));
        verify(quoteOptionItemResource).put(quoteOptionItemDTO);
        assertRemovedPriceLine(productInstance);
    }

    private void assertRemovedPriceLine(ProductInstance defaultProductInstance) {
        assertThat(defaultProductInstance.getPriceLines().size(), is(0));
        assertThat(defaultProductInstance.getPricingStatus(), is(PricingStatus.NOT_PRICED));
        verify(productInstanceClient).put(defaultProductInstance);
        assertRemovedChildProductPriceLine(defaultProductInstance);
    }

    private void assertRemovedChildProductPriceLine(ProductInstance defaultProductInstance) {
        Set<ProductInstance> productInstances = defaultProductInstance.getChildren();
        if(productInstances != null) {
            for(ProductInstance childInstance: productInstances) {
                if(childInstance != null && childInstance.isPriceable()) {
                    assertThat(childInstance.getPriceLines().size(), is(0));
                    assertThat(childInstance.getPricingStatus(), is(PricingStatus.NOT_PRICED));
                    assertRemovedChildProductPriceLine(childInstance);
                }
            }
        }

    }

    @Test
    public void shouldNotUpdatePriceBookIfNoValueIsChanged() {

        when(quoteOptionItemResource.put(quoteOptionItemDTO)).thenReturn(quoteOptionItemDTO);

        Response response = contractDialogResourceHandler.updateContract(projectId, quoteOptionId, quoteOptionItemId, "eup", "ptp");

        assertThat(response, is(aResponse().withStatusOK()));

        verify(quoteOptionItemResource, never()).put(quoteOptionItemDTO);
    }

    @Test
    public void shouldNotRemovePriceLineIfNoValueInPriceBook() {

        when(quoteOptionItemResource.put(quoteOptionItemDTO)).thenReturn(quoteOptionItemDTO);

        Response response = contractDialogResourceHandler.updateContract(projectId, quoteOptionId, quoteOptionItemId, "eup", "ptp");

        assertThat(response, is(aResponse().withStatusOK()));

        verify(quoteOptionItemResource, never()).put(quoteOptionItemDTO);

         assertNotRemovedPriceLine(productInstance);
         verify(productInstanceClient, never()).put((productInstance));
    }

    private void assertNotRemovedPriceLine(ProductInstance defaultProductInstance) {
        assertThat(defaultProductInstance.getPriceLines().size(), is(2));
         assertThat(defaultProductInstance.getPricingStatus(), is(PricingStatus.FIRM));
        assertNotRemovedChildProductPriceLine(defaultProductInstance);
    }

    private void assertNotRemovedChildProductPriceLine(ProductInstance defaultProductInstance) {
        Set<ProductInstance> productInstances = defaultProductInstance.getChildren();
        if(productInstances != null) {
            for(ProductInstance childInstance: productInstances) {
                if(childInstance != null && childInstance.isPriceable()) {
                    assertThat(childInstance.getPriceLines().size(), is(2));
                    assertThat(childInstance.getPricingStatus(), is(PricingStatus.FIRM));
                    assertNotRemovedChildProductPriceLine(childInstance);
                }
            }
        }

    }

    @Test
    public void shouldProceedIfPtpPriceBookIsNullAssumingItIsDirectUser() {
        ContractDTO contractDTO = new ContractDTO("id", "12", newArrayList(new PriceBookDTO("1", "someRequestId", "eup", null, null, null)));
        quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withContract(contractDTO).build();

        quoteOptionResource = mock(QuoteOptionResource.class);
        quoteOptionItemResource = mock(QuoteOptionItemResource.class);

        when(projectResource.quoteOptionResource(projectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(quoteOptionId)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get(quoteOptionItemId)).thenReturn(quoteOptionItemDTO);

        contractDialogResourceHandler = new ContractDialogResourceHandler(presenter, projectResource, jsonSerializer, priceBookFacade, productInstanceClient);
        when(quoteOptionItemResource.put(quoteOptionItemDTO)).thenReturn(quoteOptionItemDTO);

        Response response = contractDialogResourceHandler.updateContract(projectId, quoteOptionId, quoteOptionItemId, "latest-eup", null);

        assertThat(response, is(aResponse().withStatusOK()));
        verify(quoteOptionItemResource).put(quoteOptionItemDTO);
    }

    private ProductInstance getProductInstance() {
        PriceLine existingPriceLine = new PriceLineFixture().withPriceType(PriceType.ONE_TIME).withChargePrice(50.0).withStatus(PricingStatus.FIRM).withChargingSchemeName("charge scheme name").build();
        PriceLine existingPriceLine1 = new PriceLineFixture().withPriceType(PriceType.RECURRING).withChargePrice(50.0).withStatus(PricingStatus.FIRM).withChargingSchemeName("charge scheme name").build();
        PriceLine child2PriceLine1 = new PriceLineFixture().withPriceType(PriceType.ONE_TIME).withChargePrice(100.0).withStatus(PricingStatus.FIRM).withChargingSchemeName("charge scheme name").build();
        PriceLine child2PriceLine2 = new PriceLineFixture().withPriceType(PriceType.RECURRING).withChargePrice(100.0).withStatus(PricingStatus.FIRM).withChargingSchemeName("charge scheme name").build();
        PriceLine child1PriceLine1 = new PriceLineFixture().withPriceType(PriceType.ONE_TIME).withChargePrice(100.0).withStatus(PricingStatus.FIRM).withChargingSchemeName("charge scheme name").build();
        PriceLine child1PriceLine2 = new PriceLineFixture().withPriceType(PriceType.RECURRING).withChargePrice(100.0).withStatus(PricingStatus.FIRM).withChargingSchemeName("charge scheme name").build();
        PriceLine newPriceLine3 = new PriceLineFixture().withPriceType(PriceType.RECURRING).withChargePrice(100.0).withStatus(PricingStatus.FIRM).withChargingSchemeName("charge scheme name").build();

        final ProductChargingScheme chargingScheme1 = new ProductChargingScheme("chargingSchemeName1", PricingStrategy.PricingEngine, "ABC", ProductChargingScheme.PriceVisibility.Sales, "", new ArrayList<BillingTariffRuleSet>(), null);
        final ProductChargingScheme chargingScheme2 = new ProductChargingScheme("chargingSchemeName2", PricingStrategy.PricingEngine, "ABC", ProductChargingScheme.PriceVisibility.Sales, "", new ArrayList<BillingTariffRuleSet>(), null);
        final ProductChargingScheme chargingScheme3 = new ProductChargingScheme("chargingSchemeName3", PricingStrategy.PricingEngine, "", ProductChargingScheme.PriceVisibility.Sales, "", new ArrayList<BillingTariffRuleSet>(), null);
        final ProductChargingScheme aggChargingScheme = new ProductChargingScheme("chargingSchemeName3", PricingStrategy.Aggregation, "", ProductChargingScheme.PriceVisibility.Sales, "ABC", new ArrayList<BillingTariffRuleSet>(), null);

        final ProductInstance childProductInstance1 = createProductInstanceWithRelation(chargingScheme1, newArrayList(child1PriceLine1, child1PriceLine2));
        final ProductInstance childProductInstance2 = createProductInstanceWithRelation(chargingScheme2, newArrayList(child2PriceLine1, child2PriceLine2));
        final ProductInstance relatedProduct = createProductInstanceWithRelation(chargingScheme3, newArrayList(newPriceLine3));
        final ProductInstance defaultProductInstance = createProductInstanceWithRelation(chargingScheme3, newArrayList(existingPriceLine, existingPriceLine1));

        defaultProductInstance.addChildProductInstance(childProductInstance1, RelationshipType.Child);
        childProductInstance1.addChildProductInstance(childProductInstance2, RelationshipType.Child);
        defaultProductInstance.addRelationship(new ProductSalesRelationshipInstance(new ProductSalesRelationship(new Relationship(0, 1, 0, RelationshipName.newInstance("something"), RelationshipType.RelatedTo, ResolvesTo.Any,""), RelatedProductIdentifier.unStencilled(new ProductIdentifier("someCode", "someOther", "1.0")), RelationshipGroup.NIL), relatedProduct));
        childProductInstance1.addRelationship(new ProductSalesRelationshipInstance(new ProductSalesRelationship(new Relationship(0, 1, 0, RelationshipName.newInstance("something"), RelationshipType.RelatedTo, ResolvesTo.Any, ""), RelatedProductIdentifier.unStencilled(new ProductIdentifier("someCode", "someOther", "1.0")), RelationshipGroup.NIL), relatedProduct));
        defaultProductInstance.refreshPricingStatusBasedOnPriceLines();
        childProductInstance1.refreshPricingStatusBasedOnPriceLines();
        childProductInstance2.refreshPricingStatusBasedOnPriceLines();
        return defaultProductInstance;
    }

    private ProductInstance createProductInstanceWithRelation(ProductChargingScheme chargingScheme, List<PriceLine> priceLines) {
        final ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aProductOffering()
                                                                                    .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship().withProductIdentifier(new ProductIdentifier("test", "test", "test")))
                                                                                    .withChargingScheme(chargingScheme);
        DefaultProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                              .withProductOffering(productOfferingFixture)
                                                                              .withPriceLines(priceLines)
                                                                              .build();
        return productInstance;
    }
}
