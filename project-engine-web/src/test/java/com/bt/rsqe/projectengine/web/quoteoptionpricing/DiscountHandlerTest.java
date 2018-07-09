package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.pricing.AutoPriceAggregator;
import com.bt.rsqe.projectengine.IfcAction;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemIcbApprovalStatus;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.PriceHandlerService;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture;
import com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.projectengine.web.model.DiscountDelta;
import com.bt.rsqe.projectengine.web.model.DiscountUpdater;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDeltas;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class DiscountHandlerTest {
    private DiscountHandler discountHandler;
    private FutureAssetPricesFacade futureAssetPricesFacade;
    private QuoteOptionFacade quoteOptionFacade;
    ProjectResource projectResource;
    AutoPriceAggregator autoPriceAggregator;
    QuoteOptionResource quoteOptionResource;
    QuoteOptionItemResource quoteOptionitemResource;
    PriceHandlerService priceHandlerService;
    ProductInstanceClient instanceClient;

    @Before
    public void before() {
        UserContext userContext = aDirectUserContext().build();
        UserContextManager.setCurrent(userContext);
        futureAssetPricesFacade = mock(FutureAssetPricesFacade.class);
        quoteOptionFacade = mock(QuoteOptionFacade.class);
        projectResource = mock(ProjectResource.class);
        autoPriceAggregator = mock(AutoPriceAggregator.class);
        quoteOptionResource = mock(QuoteOptionResource.class);
        quoteOptionitemResource = mock(QuoteOptionItemResource.class);
        priceHandlerService = mock(PriceHandlerService.class);
        instanceClient = mock(ProductInstanceClient.class);
        discountHandler = new DiscountHandler(futureAssetPricesFacade, quoteOptionFacade, autoPriceAggregator, projectResource, priceHandlerService, instanceClient);
    }

    @Test
    public void shouldReturn400GivenBadJSON() throws Exception {
        final Response response = discountHandler.addDiscounts("customerId", "", "", "blah");
        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void shouldSaveExpectedLineItems() throws Exception {
        final FutureAssetPricesModel futureAssetPricesModel1 = mock(FutureAssetPricesModel.class);
        final FutureAssetPricesModel futureAssetPricesModel2 = mock(FutureAssetPricesModel.class);

        ContractDTO contractDTO = new ContractDTO("contractId", "aTeram", newArrayList(new PriceBookDTO("id", "aREquestID",
                                                                                                        "ICG", "", "", "")));

        QuoteOptionItemDTO quoteOptionItemDTO = new QuoteOptionItemDTO(
            "aId", "aCode", "aAction", null, "aOfferID", "aOfferName", "aContractTerm",
            QuoteOptionItemStatus.DRAFT,
            LineItemDiscountStatus.APPROVED,
            LineItemIcbApprovalStatus.NOT_APPLICABLE,
            "aOrderId",
            new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.VALID),
            LineItemOrderStatus.NOT_APPLICABLE,
            IfcAction.NOT_APPLICABLE,
            "aBillingId", null, contractDTO, false, false, false, null, null, true, new ProductCategoryCode("H123"), null, false);


        when(futureAssetPricesFacade.getForLineItems(eq("customerId"), eq("projectId"), eq("quoteOptionId"), any(List.class))).thenReturn(asList(futureAssetPricesModel1, futureAssetPricesModel2));
        when(futureAssetPricesModel1.getLineItemId()).thenReturn("b07a0524-24a2-4be6-818e-eebfcfb75962");
        when(futureAssetPricesModel2.getLineItemId()).thenReturn("lineItemId2");
        when(futureAssetPricesModel1.getPricesDTO()).thenReturn(new FutureAssetPricesDTO(AssetDTOFixture.anAsset()
                                                                                                        .withLineItemId("L1")
                                                                                                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P1").build())
                                                                                                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P2").build())
                                                                                                        .build()));
        when(futureAssetPricesModel2.getPricesDTO()).thenReturn(new FutureAssetPricesDTO(AssetDTOFixture.anAsset()
                                                                                                        .withLineItemId("L1")
                                                                                                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P1").build())
                                                                                                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P2").build())
                                                                                                        .build()));
        when(projectResource.quoteOptionResource("projectId")).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource("quoteOptionId")).thenReturn(quoteOptionitemResource);
        when(quoteOptionitemResource.get(anyString())).thenReturn(quoteOptionItemDTO);


        final Response response = discountHandler.addDiscounts("customerId", "projectId", "quoteOptionId",
                                                               "{\"discount\": {\"b07a0524-24a2-4be6-818e-eebfcfb75962\": {\"id_f4e9b54804835ce9cfbca0c4cb\": \"0.00000\" }},\"gross\": [{\"lineItemId\":\"b07a0524-24a2-4be6-818e-eebfcfb75962\",\"id\":\"100\",\"gross\":\"34\",\"type\":\"oneTime\",\"productDescription\":\"Site Management Price\"},{\"lineItemId\":\"b07a0524-24a2-4be6-818e-eebfcfb75962\",\"id\":\"200\",\"gross\":\"34\",\"type\":\"recurring\",\"productDescription\":\"Site Management Price\"}]}}");


        assertThat(response.getStatus(), is(204));
        Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
        map.put("f4e9b54804835ce9cfbca0c4cb", BigDecimal.ZERO.setScale(5));
        verify(futureAssetPricesModel1).applyDiscount(map);
        //verify(futureAssetPricesModel2).applyDiscount(argThat(aHashMap("recurring", -75)));
        verify(futureAssetPricesFacade).save(futureAssetPricesModel1);
        verify(futureAssetPricesFacade).save(futureAssetPricesModel2);

    }

    @Test
    public void shouldRequestDiscountOnQuoteOption() throws Exception {
        discountHandler.addDiscounts("customerId", "projectId", "quoteOptionId",
         "{\"discount\": {\"b07a0524-24a2-4be6-818e-eebfcfb75962\": {\"id_f4e9b54804835ce9cfbca0c4cb\": \"0.00000\" }},\"gross\": [{\"lineItemId\":\"b07a0524-24a2-4be6-818e-eebfcfb75962\",\"id\":\"100\",\"gross\":\"34\",\"type\":\"oneTime\",\"productDescription\":\"Site Management Price\"},{\"lineItemId\":\"b07a0524-24a2-4be6-818e-eebfcfb75962\",\"id\":\"200\",\"gross\":\"34\",\"type\":\"recurring\",\"productDescription\":\"Site Management Price\"}]}}");
        verify(quoteOptionFacade).putDiscountRequest("projectId", "quoteOptionId");
    }

    @Test
    public void shouldSaveManualPricingForCost() throws Exception {
        String costDeltasJson = "{\"quoteOptionCostDeltas\": [{\"lineItemId\": \"L1\",\"description\": \"aDescription1\",\"vendorDiscountRef\": \"aVendorDiscountRef1\",\"oneTimeDiscount\": {\"priceLineId\": \"P1\",\"discount\": \"10\",\"discountUpdated\": true, \"grossValue\" : \"20\"},\"recurringDiscount\": {\"priceLineId\": \"P2\",\"discount\": \"50\", \"discountUpdated\": true, \"grossValue\" : \"20\"},\"isManualPricing\" : true , \"isGrossAdded\" : true}]}";

        ProductIdentifierFacade productIdentifierFacade = mock(ProductIdentifierFacade.class);
        DiscountUpdater discountUpdater = mock(DiscountUpdater.class);

        final FutureAssetPricesModel futureAssetPricesModel = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                .with(productIdentifierFacade)
                .with(discountUpdater)
                .with(new FutureAssetPricesDTO(AssetDTOFixture.anAsset()
                        .withLineItemId("L1")
                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P1").build())
                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P2").build())
                        .build()))
                .build();
        when(futureAssetPricesFacade.getForLineItems("aCustomerId", "aProjectId", "aQuoteOptionId", newArrayList(new LineItemId("L1"))))
                .thenReturn(newArrayList(futureAssetPricesModel));

        Response response = discountHandler.applyCostDiscounts("aCustomerId", "aProjectId", "aQuoteOptionId", costDeltasJson);

        Map<String, DiscountDelta> expectedDiscounts = newHashMap();
        expectedDiscounts.put("P1", new DiscountDelta(Optional.of(new BigDecimal("10")), Optional.of("aVendorDiscountRef1"), Optional.of(PriceLineStatus.REPRICING)));
        expectedDiscounts.put("P2", new DiscountDelta(Optional.of(new BigDecimal("50")), Optional.of("aVendorDiscountRef1"), Optional.of(PriceLineStatus.REPRICING)));
        verify(futureAssetPricesFacade).save(futureAssetPricesModel);
        verify(futureAssetPricesFacade).updatePricingStatus(futureAssetPricesModel, PricingStatus.FIRM);

        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void shouldSaveUpdatedCostDiscounts() throws Exception {
        String costDeltasJson = "{" +
                            "\"quoteOptionCostDeltas\": [{" +
                            "\"lineItemId\": \"L1\"," +
                            "\"description\": \"aDescription1\"," +
                            "\"vendorDiscountRef\": \"aVendorDiscountRef1\"," +
                            "\"oneTimeDiscount\": {" +
                            "\"priceLineId\": \"P1\"," +
                            "\"discount\": \"10\"," +
                            "\"discountUpdated\": true" +
                            "}," +
                            "\"recurringDiscount\": {" +
                            "\"priceLineId\": \"P2\"," +
                            "\"discount\": \"50\", " +
                            "\"discountUpdated\": true" +
                            "}" +
                            "}]" +
                            "}";

        ProductIdentifierFacade productIdentifierFacade = mock(ProductIdentifierFacade.class);
        DiscountUpdater discountUpdater = mock(DiscountUpdater.class);

        final FutureAssetPricesModel futureAssetPricesModel = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                          .with(productIdentifierFacade)
                                                                          .with(discountUpdater)
                                                                          .with(new FutureAssetPricesDTO(AssetDTOFixture.anAsset()
                                                                                                                        .withLineItemId("L1")
                                                                                                                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P1").build())
                                                                                                                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P2").build())
                                                                                                                        .build()))
                                                                          .build();
        when(futureAssetPricesFacade.getForLineItems("aCustomerId", "aProjectId", "aQuoteOptionId", newArrayList(new LineItemId("L1"))))
            .thenReturn(newArrayList(futureAssetPricesModel));

        Response response = discountHandler.applyCostDiscounts("aCustomerId", "aProjectId", "aQuoteOptionId", costDeltasJson);

        Map<String, DiscountDelta> expectedDiscounts = newHashMap();
        expectedDiscounts.put("P1", new DiscountDelta(Optional.of(new BigDecimal("10")), Optional.of("aVendorDiscountRef1"), Optional.of(PriceLineStatus.REPRICING)));
        expectedDiscounts.put("P2", new DiscountDelta(Optional.of(new BigDecimal("50")), Optional.of("aVendorDiscountRef1"), Optional.of(PriceLineStatus.REPRICING)));
        verify(discountUpdater).applyDiscount(eq(expectedDiscounts), Matchers.<FutureAssetPricesDTO>any(), Matchers.<List<PriceLineModel>>any());
        verify(futureAssetPricesFacade).save(futureAssetPricesModel);
        verify(futureAssetPricesFacade).updatePricingStatus(futureAssetPricesModel, PricingStatus.REPRICING);

        ArgumentCaptor<Set> lineItemCaptor = ArgumentCaptor.forClass(Set.class);
        verify(priceHandlerService).processLineItemsForPricing(lineItemCaptor.capture(),
                                                               eq("aCustomerId"),
                                                               eq("aProjectId"),
                                                               eq("aQuoteOptionId"),
                                                               eq(false),
                                                               Matchers.<String>any());
        assertThat(((Set<LineItemId>)lineItemCaptor.getValue()).toArray(new LineItemId[]{})[0].value(), is("L1"));
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void shouldNotSaveCostDiscountsWhenNoChangeHasBeenMadeInDelta() throws Exception {
        String costDeltasJson = "{" +
                            "\"quoteOptionCostDeltas\": [{" +
                            "\"lineItemId\": \"L1\"," +
                            "\"description\": \"aDescription1\"," +
                            "\"vendorDiscountRef\": \"\"," +
                            "\"oneTimeDiscount\": {" +
                            "\"priceLineId\": \"P1\"," +
                            "\"discount\": \"10\"," +
                            "\"discountUpdated\": false" +
                            "}," +
                            "\"recurringDiscount\": {" +
                            "\"priceLineId\": \"P2\"," +
                            "\"discount\": \"50\", " +
                            "\"discountUpdated\": false" +
                            "}" +
                            "}]" +
                            "}";

        ProductIdentifierFacade productIdentifierFacade = mock(ProductIdentifierFacade.class);
        DiscountUpdater discountUpdater = mock(DiscountUpdater.class);

        final FutureAssetPricesModel futureAssetPricesModel = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                          .with(productIdentifierFacade)
                                                                          .with(discountUpdater)
                                                                          .with(new FutureAssetPricesDTO(AssetDTOFixture.anAsset()
                                                                                                                        .withLineItemId("L1")
                                                                                                                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P1").build())
                                                                                                                        .withPriceLine(PriceLineDTOFixture.aPriceLineDTO().withId("P2").build())
                                                                                                                        .build()))
                                                                          .build();
        when(futureAssetPricesFacade.getForLineItems("aCustomerId", "aProjectId", "aQuoteOptionId", newArrayList(new LineItemId("L1"))))
            .thenReturn(newArrayList(futureAssetPricesModel));

        Response response = discountHandler.applyCostDiscounts("aCustomerId", "aProjectId", "aQuoteOptionId", costDeltasJson);

        Map<String, DiscountDelta> expectedDiscounts = newHashMap();
        expectedDiscounts.put("P1", new DiscountDelta(Optional.<BigDecimal>absent(), Optional.<String>absent(), Optional.<PriceLineStatus>absent()));
        expectedDiscounts.put("P2", new DiscountDelta(Optional.<BigDecimal>absent(), Optional.<String>absent(), Optional.<PriceLineStatus>absent()));
        verify(discountUpdater).applyDiscount(eq(expectedDiscounts), Matchers.<FutureAssetPricesDTO>any(), Matchers.<List<PriceLineModel>>any());
        verify(futureAssetPricesFacade).save(futureAssetPricesModel);
        verify(futureAssetPricesFacade, never()).updatePricingStatus(futureAssetPricesModel, PricingStatus.REPRICING);
        verify(priceHandlerService, never()).processLineItemsForPricing(eq("L1"),
                                                               eq("customerId"),
                                                               eq("projectId"),
                                                               eq("quoteOptionId"),
                                                               eq(false),
                                                               Matchers.<String>any());

        assertThat(response.getStatus(), is(200));
    }


    @Test
    public void shouldSaveUpdatedUsageDiscounts() throws Exception {
        QuoteOptionPricingDeltas.QuoteOptionPricingDelta delta1 = new QuoteOptionPricingDeltas.QuoteOptionPricingDelta("L1", "P1", "C1", "1", "2", null);
        QuoteOptionPricingDeltas.QuoteOptionPricingDelta delta2 = new QuoteOptionPricingDeltas.QuoteOptionPricingDelta("L2", "P2", "C2", null, null, "3");
        QuoteOptionPricingDeltas deltas = new QuoteOptionPricingDeltas();
        deltas.getQuoteOptionPricingDeltas().add(delta1);
        deltas.getQuoteOptionPricingDeltas().add(delta2);

        FutureAssetPricesModel model1 = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                     .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                                                                      .withLineItemId("L1")
                                                                                                      .withPriceLine(PriceLineDTOFixture.aPriceLineDTO()
                                                                                                                                        .withId("P1")
                                                                                                                                        .withPrice(PriceCategory.MIN_CHARGE, 5, "C1")
                                                                                                                                        .withPrice(PriceCategory.FIXED_CHARGE, 5, "C1")))
                                                                     .build();
        FutureAssetPricesModel model2 = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                     .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                                                                      .withLineItemId("L2")
                                                                                                      .withPriceLine(PriceLineDTOFixture.aPriceLineDTO()
                                                                                                                                        .withId("P2")
                                                                                                                                        .withPrice(PriceCategory.CHARGE_RATE, 5, "C2")))
                                                                     .build();

        when(futureAssetPricesFacade.getForLineItems("aCustomerId", "aProjectId", "aQuoteOptionId", newArrayList(new LineItemId("L1"), new LineItemId("L2"))))
            .thenReturn(Lists.<FutureAssetPricesModel>newArrayList(model1, model2));

        Response response = discountHandler.applyUsageDiscounts("aCustomerId", "aProjectId", "aQuoteOptionId", new Gson().toJson(deltas));
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        ArgumentCaptor<FutureAssetPricesModel> lineItemCaptor = ArgumentCaptor.forClass(FutureAssetPricesModel.class);
        verify(futureAssetPricesFacade, times(2)).save(lineItemCaptor.capture());

        assertThat(lineItemCaptor.getAllValues().get(0).getPricesDTO().getPriceLines().get(0).getPrices(PriceCategory.MIN_CHARGE).get(0).discountPercentage, is(new BigDecimal("1")));
        assertThat(lineItemCaptor.getAllValues().get(0).getPricesDTO().getPriceLines().get(0).getPrices(PriceCategory.FIXED_CHARGE).get(0).discountPercentage, is(new BigDecimal("2")));
        assertThat(lineItemCaptor.getAllValues().get(1).getPricesDTO().getPriceLines().get(0).getPrices(PriceCategory.CHARGE_RATE).get(0).discountPercentage, is(new BigDecimal("3")));
    }

    public static class CorrectHashMap extends TypeSafeMatcher<Map<String, BigDecimal>> {

        private String key;
        private double value;

        private CorrectHashMap(String key, double value) {
            this.key = key;
            this.value = value;
        }

        public static CorrectHashMap aHashMap(String key, double value) {
            return new CorrectHashMap(key, value);
        }

        @Override
        public boolean matchesSafely(Map<String, BigDecimal> item) {
            assertThat(item.get(key), is(new BigDecimal(value)));
            return true;
        }

        @Override
        public void describeTo(Description description) {
        }
    }
}
