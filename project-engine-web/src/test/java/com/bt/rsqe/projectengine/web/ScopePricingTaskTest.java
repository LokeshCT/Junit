package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceAssetValidator;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.ScopePricing;
import com.bt.rsqe.customerinventory.client.ScopePricingItem;
import com.bt.rsqe.customerinventory.client.ScopePricingItemError;
import com.bt.rsqe.customerinventory.client.ScopePricingStatus;
import com.bt.rsqe.customerinventory.dto.AssetCharacteristicDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.customerinventory.parameter.CharacteristicName;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.repository.jpa.entities.definitions.AssetCharacteristic;
import com.bt.rsqe.domain.AbstractNotificationEvent;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.ErrorNotificationEvent;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.AssetProcessType;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.pc.client.ConfiguratorSpecialBidClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.pricing.PriceClientResponse;
import com.bt.rsqe.pricing.PricingErrorDTO;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsConditional;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 28/08/14
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class ScopePricingTaskTest {

    ScopePricingTask scopePricingTask;
    private String lineItems = "lineItems";
    private String customerId = "customerId";
    private String projectId = "projectId";
    private String quoteOptionId = "quoteOptionId";
    private boolean indirectUser = false;
    private PriceHandlerService priceHandlerService;
    private AssetDTO assetDTO;
    private AssetCharacteristicDTO assetCharacteristicDTO;
    private ProductInstanceClient futureProductInstanceClient;
    private ProductInstanceAssetValidator productInstanceValidator;
    private ConfiguratorSpecialBidClient configuratorSpecialBidClient;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private QuoteMigrationDetailsConditional quoteMigrationDetailsConditional;
    private ProductInstance validProductInstance = mock(ProductInstance.class);
    private final static String ICB_ERROR = "ICB Status not supported in move scenario. Please add valid CPE";
    private PmrClient pmr;
    private ApplicationCapabilityProvider applicationCapabilityProvider;

    @Before
    public void before() {
        assetDTO = mock(AssetDTO.class);
        assetCharacteristicDTO = mock(AssetCharacteristicDTO.class);
        priceHandlerService = mock(PriceHandlerService.class);
        futureProductInstanceClient =  mock(ProductInstanceClient.class);
        productInstanceValidator = mock(ProductInstanceAssetValidator.class);
        configuratorSpecialBidClient = mock(ConfiguratorSpecialBidClient.class);
        migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
        quoteMigrationDetailsConditional = mock(QuoteMigrationDetailsConditional.class);
        pmr = PmrMocker.getMockedInstance(true);
        applicationCapabilityProvider = mock(ApplicationCapabilityProvider.class);
        scopePricingTask = new ScopePricingTask(lineItems, customerId, projectId, quoteOptionId, indirectUser, priceHandlerService,
                                                futureProductInstanceClient, productInstanceValidator, configuratorSpecialBidClient,"userToken", migrationDetailsProvider, pmr, applicationCapabilityProvider);
        when(migrationDetailsProvider.conditionalFor(Matchers.<ProductInstance>any())).thenReturn(quoteMigrationDetailsConditional);
        when(quoteMigrationDetailsConditional.isMigrationQuote()).thenReturn(quoteMigrationDetailsConditional);
        when(quoteMigrationDetailsConditional.check()).thenReturn(false);
    }

    @Test
    public void shouldProcessLineItemsForPricing() throws Exception {
        setUpValidProductInstances();
        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItems", PricingStatus.FIRM);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItem", priceClientResponse);
        //When
        when(priceHandlerService.processLineItemsForPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        scopePricing.addItem(new ScopePricingItem(priceClientResponse.getLineItemId(), priceClientResponse.getPriceStatus(),
                                                  Integer.toString(lineItems.hashCode())));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
    }

    @Test
    public void shouldDeleteScopePricingItemIfErrorThrown() throws Exception {
        //When
        setUpValidProductInstances();
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);
        when(priceHandlerService.processLineItemsForPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenThrow(Exception.class);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
    }

    @Test
    public void shouldHandleMultipleLineItems() throws Exception {
        setUpValidProductInstances();
        String multipleLineItems = "lineItem1,lineItem2,lineItem3";
        PriceClientResponse priceClientResponse1 = new PriceClientResponse("productInstanceId1", new Long(1), "lineItem1", PricingStatus.FIRM);
        PriceClientResponse priceClientResponse2 = new PriceClientResponse("productInstanceId2", new Long(1), "lineItem2", PricingStatus.FIRM);
        PriceClientResponse priceClientResponse3 = new PriceClientResponse("productInstanceId3", new Long(1), "lineItem3", PricingStatus.FIRM);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("productInstanceId1", priceClientResponse1);
        priceClientResponseMap.put("productInstanceId2", priceClientResponse2);
        priceClientResponseMap.put("productInstanceId3", priceClientResponse3);
        scopePricingTask = new ScopePricingTask(multipleLineItems, customerId, projectId, quoteOptionId, indirectUser, priceHandlerService,
                                                futureProductInstanceClient, productInstanceValidator, null,"userToken", migrationDetailsProvider, pmr, applicationCapabilityProvider);

        //When
        when(priceHandlerService.processLineItemsForPricing(multipleLineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);
        ScopePricing scopePricing = new ScopePricing(Integer.toString(multipleLineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(multipleLineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(multipleLineItems);
        scopePricing.addItem(new ScopePricingItem(priceClientResponse1.getLineItemId(), priceClientResponse1.getPriceStatus(),
                                                  Integer.toString(multipleLineItems.hashCode())));
        scopePricing.addItem(new ScopePricingItem(priceClientResponse2.getLineItemId(), priceClientResponse2.getPriceStatus(),
                                                  Integer.toString(multipleLineItems.hashCode())));
        scopePricing.addItem(new ScopePricingItem(priceClientResponse3.getLineItemId(), priceClientResponse3.getPriceStatus(),
                                                  Integer.toString(multipleLineItems.hashCode())));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
    }

    @Test
    public void shouldOnlyPriceValidProducts() throws Exception {
        String multipleLineItems = "lineItem1,lineItem2,lineItem3";
        Notification notificationWithErrors = new Notification();
        notificationWithErrors.addEvent(new ErrorNotificationEvent("Test validation error"));
        ProductInstance productInstance1 = mock(ProductInstance.class);
        ProductInstance productInstance2 = mock(ProductInstance.class);
        ProductInstance productInstance3 = mock(ProductInstance.class);

        when(productInstance1.getProductInstanceId()).thenReturn(new ProductInstanceId("prod1"));
        when(productInstance2.getProductInstanceId()).thenReturn(new ProductInstanceId("prod2"));
        when(productInstance3.getProductInstanceId()).thenReturn(new ProductInstanceId("prod3"));

        when(productInstance1.getProductInstanceVersion()).thenReturn(new Long("1"));
        when(productInstance2.getProductInstanceVersion()).thenReturn(new Long("1"));
        when(productInstance3.getProductInstanceVersion()).thenReturn(new Long("2"));

        when(productInstanceValidator.validateAsset(productInstance1, false)).thenReturn(new Notification());
        when(productInstanceValidator.validateAsset(productInstance2, false)).thenReturn(new Notification());
        when(productInstanceValidator.validateAsset(productInstance3, false)).thenReturn(new Notification());

        when(futureProductInstanceClient.get(new LineItemId("lineItem1"))).thenReturn(productInstance1);
        when(futureProductInstanceClient.get(new LineItemId("lineItem2"))).thenReturn(productInstance2);
        when(futureProductInstanceClient.get(new LineItemId("lineItem3"))).thenReturn(productInstance3);
        PriceClientResponse priceClientResponse1 = new PriceClientResponse("productInstanceId1", new Long(1), "lineItem1", PricingStatus.FIRM);
        PriceClientResponse priceClientResponse2 = new PriceClientResponse("productInstanceId2", new Long(1), "lineItem2", PricingStatus.FIRM);
        PriceClientResponse priceClientResponse3 = new PriceClientResponse("productInstanceId3", new Long(1), "lineItem3", PricingStatus.FIRM);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("productInstanceId1", priceClientResponse1);
        priceClientResponseMap.put("productInstanceId2", priceClientResponse2);
        priceClientResponseMap.put("productInstanceId3", priceClientResponse3);
        scopePricingTask = new ScopePricingTask(multipleLineItems, customerId, projectId, quoteOptionId, indirectUser, priceHandlerService,
                                                futureProductInstanceClient, productInstanceValidator, null,"userToken", migrationDetailsProvider, pmr, applicationCapabilityProvider);

        //When
        String multipleValidLineItems = "lineItem1,lineItem3";
        when(priceHandlerService.processLineItemsForPricing(multipleValidLineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);
        ScopePricing scopePricing = new ScopePricing(Integer.toString(multipleLineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(multipleLineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(multipleLineItems);
        scopePricing.addItem(new ScopePricingItem(priceClientResponse1.getLineItemId(), priceClientResponse1.getPriceStatus(),
                                                  Integer.toString(multipleLineItems.hashCode())));
        scopePricing.addItem(new ScopePricingItem(priceClientResponse2.getLineItemId(), priceClientResponse2.getPriceStatus(),
                                                  Integer.toString(multipleLineItems.hashCode()),
                                                  Lists.<ScopePricingItemError>newArrayList(new ScopePricingItemError(priceClientResponse2.getLineItemId(), "Test validation error"))));
        scopePricing.addItem(new ScopePricingItem(priceClientResponse3.getLineItemId(), priceClientResponse3.getPriceStatus(),
                                                  Integer.toString(multipleLineItems.hashCode())));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
    }

    @Test
    public void shouldNotPriceIfNoValidProducts() throws Exception {
        setUpInValidProductInstance();
        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItem1,lineItem2,lineItem3", PricingStatus.FIRM);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItem", priceClientResponse);
        //When
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        scopePricing.addItem(new ScopePricingItem(lineItems, PricingStatus.NOT_PRICED.getDescription(),
                                                  Integer.toString(lineItems.hashCode())));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
    }

    @Test
    public void shouldIgnoreRelationshipValidationErrors() throws Exception {

        final String nonRecurringCharges = "NON RECURRING CHARGES";
        final String recurringCharges = "RECURRING CHARGES";

        when(applicationCapabilityProvider.isFunctionalityEnabled(ApplicationCapabilityProvider.Capability.IGNORE_RELATION_VALIDATIONS_WHEN_PRICING, false, Optional.of("aQuoteOptionId"))).thenReturn(true);

        setUpInValidProductInstance(AbstractNotificationEvent.EventType.RELATIONSHIP);

        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItems", PricingStatus.FIRM);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItem", priceClientResponse);
        //When
        when(priceHandlerService.processLineItemsForPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);

        when(assetDTO.hasCharacteristic(new CharacteristicName(recurringCharges))).thenReturn(true);
        when(assetDTO.hasCharacteristic(new CharacteristicName(nonRecurringCharges))).thenReturn(true);
        when(futureProductInstanceClient.getAssetDtoByAssetKey(new AssetKey("productInstanceId",new Long(1)))).thenReturn(assetDTO);
        when(assetDTO.getCharacteristic(nonRecurringCharges)).thenReturn(assetCharacteristicDTO);
        when(assetDTO.getCharacteristic(recurringCharges)).thenReturn(assetCharacteristicDTO);

        //When
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);

        ArgumentCaptor<ScopePricing> scopePricingArgumentCaptor = ArgumentCaptor.forClass(ScopePricing.class);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricingArgumentCaptor.capture());
        assertThat(scopePricingArgumentCaptor.getValue().getItems().get(0).getErrors().isEmpty(), is(true));
    }

    @Test
    public void shouldNotPriceInvalidSpecialBidProducts() throws Exception {
        setUpInValidSpecialBidProductInstance();
        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItem1,lineItem2,lineItem3", PricingStatus.FIRM);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItem", priceClientResponse);
        //When
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        scopePricing.addItem(new ScopePricingItem(lineItems, PricingStatus.NOT_PRICED.getDescription(),
                                                  Integer.toString(lineItems.hashCode()),
                                                  newArrayList(new ScopePricingItemError("lineItem1", "Special Bid Attributes Invalid"))));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
    }

    @Test
    public void shouldProcessMovesToSpecialBidLineItemsForPricing() throws Exception {
        setUpValidProductInstances();
        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItems", PricingStatus.FIRM);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItem", priceClientResponse);
        //When
        when(priceHandlerService.processLineItemsForPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);
        when(validProductInstance.isSpecialBid()).thenReturn(true);
        when(validProductInstance.getAssetProcessType()).thenReturn(AssetProcessType.MOVE.value());
        when(validProductInstance.getAssetVersionStatus()).thenReturn(AssetVersionStatus.DRAFT);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        scopePricing.addItem(new ScopePricingItem(priceClientResponse.getLineItemId(), priceClientResponse.getPriceStatus(),
                                                  Integer.toString(lineItems.hashCode())));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
    }
    @Test
    public void shouldNotPriceForMoveIcbScenario() throws Exception {
        when(futureProductInstanceClient.getRelatedToLineItemIdsOwnedByLineItemId("lineItems")).thenReturn(Lists.<String>newArrayList("anotherLineItem"));
        when(futureProductInstanceClient.getAssetDTO(new LineItemId("anotherLineItem"))).thenReturn(AssetDTOFixture.anAsset().withProductCode(new ProductCode("P1")).build());

        setUpValidProductInstances();
        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItems", PricingStatus.ICB);
        priceClientResponse.setRequestStatus(newArrayList(new PricingErrorDTO(ICB_ERROR)));
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItems", priceClientResponse);
        //When
        when(priceHandlerService.processLineItemsForPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        scopePricing.addItem(new ScopePricingItem(lineItems, PricingStatus.NOT_PRICED.getDescription(),
                                                  Integer.toString(lineItems.hashCode())));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
        ScopePricingItem scopePricingItem = scopePricing.getItems().get(0);
        assertEquals(ICB_ERROR, scopePricingItem.getErrors().get(0).getError());
    }

    @Test
    public void shouldAddPricingErrorWhenPricingStatusIsICB() throws Exception {
        setUpValidProductInstances();
        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItems", PricingStatus.ICB);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItem", priceClientResponse);
        //When
        when(priceHandlerService.processLineItemsForPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        scopePricing.addItem(new ScopePricingItem(lineItems, PricingStatus.NOT_PRICED.getDescription(),
                                                  Integer.toString(lineItems.hashCode())));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricing);
        ScopePricingItem scopePricingItem = scopePricing.getItems().get(0);
        assertEquals("ICB Asset must be priced on the Bulk Configuration Page using the ICB Pricing Buttons", scopePricingItem.getErrors().get(0).getError());
    }

    @Test
    public void shouldGivePricingStatusOfPartiallyPricedWhenResponseStatusIsNotPricedButThereAreSomeFirmPriceLines() throws Exception {
        when(futureProductInstanceClient.getAssetDTO(new LineItemId("lineItems"))).thenReturn(AssetDTOFixture.anAsset().withPriceLine(new PriceLineDTOFixture().withStatus(PriceLineStatus.FIRM).build()).build());

        setUpValidProductInstances();
        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItems", PricingStatus.NOT_PRICED);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItem", priceClientResponse);
        //When
        when(priceHandlerService.processLineItemsForPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        ArgumentCaptor<ScopePricing> scopePricingResult = ArgumentCaptor.forClass(ScopePricing.class);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricingResult.capture());
        assertThat(scopePricingResult.getValue().getItems().get(0).getStatus(), is("Partially Priced"));
    }

    @Test
    public void shouldGivePricingStatusOfPartiallyPricedWhenResponseStatusIsNotPricedButThereAreSomeFirmPriceLinesOnANonSellableRelatedAsset() throws Exception {
        PmrMocker.returnForProduct(pmr, ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withProductIdentifier("relatedCode").build());
        when(futureProductInstanceClient.getAssetDTO(new LineItemId("lineItems"))).thenReturn(AssetDTOFixture.anAsset()
                                                                                                            .withRelatedToRelation(AssetDTOFixture.anAsset()
                                                                                                                                                  .withPriceLine(new PriceLineDTOFixture().withStatus(PriceLineStatus.FIRM).build())
                                                                                                                                                  .withProductCode(new ProductCode("relatedCode"))
                                                                                                                                                  .build(), RelationshipName.newInstance("relatedRel"))
                                                                                                            .build());

        setUpValidProductInstances();
        PriceClientResponse priceClientResponse = new PriceClientResponse("productInstanceId", new Long(1), "lineItems", PricingStatus.NOT_PRICED);
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap();
        priceClientResponseMap.put("lineItem", priceClientResponse);
        //When
        when(priceHandlerService.processLineItemsForPricing(lineItems, customerId, projectId, quoteOptionId, indirectUser,"userToken")).thenReturn(priceClientResponseMap);
        ScopePricing scopePricing = new ScopePricing(Integer.toString(lineItems.hashCode()), Lists.<ScopePricingItem>newArrayList());
        when(futureProductInstanceClient.getScopePricing(lineItems)).thenReturn(scopePricing);

        //Then
        scopePricingTask.run();
        verify(futureProductInstanceClient, times(1)).getScopePricing(lineItems);
        ArgumentCaptor<ScopePricing> scopePricingResult = ArgumentCaptor.forClass(ScopePricing.class);
        verify(futureProductInstanceClient, times(1)).updateScopePricing(scopePricingResult.capture());
        assertThat(scopePricingResult.getValue().getItems().get(0).getStatus(), is("Partially Priced"));
    }

    private void setUpInValidSpecialBidProductInstance() {
        ProductInstance inValidSpecialBidProductInstance = mock(ProductInstance.class);
        Notification notificationWithErrors = new Notification();
        notificationWithErrors.addEvent(new ErrorNotificationEvent("Test validation error"));
        when(inValidSpecialBidProductInstance.isSpecialBid()).thenReturn(true);
        when(inValidSpecialBidProductInstance.getProductInstanceId()).thenReturn(new ProductInstanceId("prod1"));
        when(inValidSpecialBidProductInstance.getProductInstanceVersion()).thenReturn(new Long("1"));
        when(inValidSpecialBidProductInstance.getLineItemId()).thenReturn("lineItem");
        when(productInstanceValidator.validateAsset(inValidSpecialBidProductInstance, false)).thenReturn(new Notification());
        when(configuratorSpecialBidClient.validateSpecialBidAttributes("lineItem", "prod", 1L)).thenThrow(BadRequestException.class);
        when(productInstanceValidator.validateAsset(inValidSpecialBidProductInstance, false)).thenReturn(new Notification());
        when(futureProductInstanceClient.get(new LineItemId("lineItem1"))).thenReturn(inValidSpecialBidProductInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItem2"))).thenReturn(inValidSpecialBidProductInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItem3"))).thenReturn(inValidSpecialBidProductInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItems"))).thenReturn(inValidSpecialBidProductInstance);
    }

    private void setUpInValidProductInstance() {
        setUpInValidProductInstance(AbstractNotificationEvent.EventType.NONE);
    }

    private void setUpInValidProductInstance(AbstractNotificationEvent.EventType type) {
        Notification notificationWithErrors = new Notification();
        notificationWithErrors.addEvent(new ErrorNotificationEvent("Test validation error"));
        notificationWithErrors.markEventsAsType(type);
        ProductInstance productInstance = mock(ProductInstance.class);
        when(productInstance.getProductInstanceId()).thenReturn(new ProductInstanceId("prod1"));
        when(productInstance.getProductInstanceVersion()).thenReturn(new Long("1"));
        when(productInstance.getQuoteOptionId()).thenReturn("aQuoteOptionId");
        when(productInstanceValidator.validateAsset(productInstance, false)).thenReturn(notificationWithErrors);
        when(futureProductInstanceClient.get(new LineItemId("lineItem1"))).thenReturn(productInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItem2"))).thenReturn(productInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItem3"))).thenReturn(productInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItems"))).thenReturn(productInstance);
    }

    private void setUpValidProductInstances() {
        when(validProductInstance.getProductInstanceId()).thenReturn(new ProductInstanceId("prod1"));

        when(validProductInstance.getProductInstanceVersion()).thenReturn(new Long("1"));

        when(productInstanceValidator.validateAsset(validProductInstance, false)).thenReturn(new Notification());

        when(futureProductInstanceClient.get(new LineItemId("lineItem1"))).thenReturn(validProductInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItem2"))).thenReturn(validProductInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItem3"))).thenReturn(validProductInstance);
        when(futureProductInstanceClient.get(new LineItemId("lineItems"))).thenReturn(validProductInstance);
    }
}
