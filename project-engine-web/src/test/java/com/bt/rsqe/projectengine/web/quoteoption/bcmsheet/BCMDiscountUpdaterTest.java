package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.order.OrderItemItemPrice;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.pricing.AutoPriceAggregator;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BCMDiscountUpdaterTest {

    @Mock
    ProductInstanceClient productInstanceClient;
    BCMDiscountUpdater updater;
    private static final String productInstanceId = "aProductInstanceId";
    private static final Long version= 1l;
    @Mock
    private ProductInstance productInstance;
    @Mock
    private LineItemFacade lineItemFacade;
    @Mock
    private ProjectResource projectResource;
    @Mock
    private LineItemModelFactory modelFactory;
    final String projectID = "ProjectID";
    final String quoteOptionId = "quoteOptionId";
    final String lineItemId = "lineItemId";
    @Mock
    private QuoteOptionResource quoteOptionResource;
    @Mock
    private QuoteOptionItemResource quoteOptionItemResource;
    @Mock
    private AutoPriceAggregator autoPriceAggregator;
    final PriceBookDTO priceBookDTO = new PriceBookDTO("priceBookId", "requestId", "eup", "ptp", "", "");

    @Before
    public void setUp(){
        initMocks(this);
        lineItemFacade = new LineItemFacade(projectResource,modelFactory);
        when(projectResource.quoteOptionResource(projectID)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(quoteOptionId)).thenReturn(quoteOptionItemResource);
        QuoteOptionItemDTO quoteOptionDTO = new QuoteOptionItemDTO();
        final ArrayList<PriceBookDTO> pricebookDTO = newArrayList(priceBookDTO);
        quoteOptionDTO.contractDTO = new ContractDTO("id","30", pricebookDTO);
        when(quoteOptionItemResource.get(lineItemId)).thenReturn(quoteOptionDTO);
        updater = new BCMDiscountUpdater(productInstanceClient, autoPriceAggregator, lineItemFacade);
    }

    @Test
    public void shouldUpdatePriceLinesWithDiscount(){
        Map<String, Double> priceLineToDiscountMap = newHashMap();
        priceLineToDiscountMap.put("id1",.18);
        priceLineToDiscountMap.put("id2",.18);
        priceLineToDiscountMap.put("id3",.18);
        ImportDiscounts discounts = createImportDiscounts(productInstanceId, version, priceLineToDiscountMap);
        List<PriceLine> pricelines = newArrayList();
        pricelines.add(createPriceLineWith("id1", "priceline Name1"));
        pricelines.add(createPriceLineWith("id2", "priceline Name1"));
        pricelines.add(createPriceLineWith("id3", "priceline Name1"));
        setExpectations(pricelines);
        productInstance = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId(productInstanceId)
                                                                          .withProductInstanceVersion(version)
            .withProjectId(projectID).withLineItemId(lineItemId).withQuoteOptionId(quoteOptionId)
                                                                          .withPriceLines(pricelines).build();
        when(productInstanceClient.getByAssetKey(new LengthConstrainingProductInstanceId(productInstanceId),
                                                 new ProductInstanceVersion(version))).thenReturn(productInstance);
        ChangeTracker tracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        //when(productInstanceClient.getAssetsDiff(any(String.class), any(Long.class), any(Long.class), eq(InstanceTreeScenario.PROVIDE))).thenReturn(mergeResult);
        when(productInstanceClient.getAssetsDiff(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()),
                                            new ProductInstanceVersion(productInstance.getProductInstanceVersion()),
                                            isNotNull(productInstance.getAssetSourceVersion()) ? new ProductInstanceVersion(productInstance.getAssetSourceVersion()) : null,
                                            InstanceTreeScenario.PROVIDE)).thenReturn(mergeResult);
        when(mergeResult.changeFor(any(OrderItemItemPrice.class))).thenReturn(ChangeType.ADD);
        updater.updateDiscountsFrom(newArrayList(discounts));
        verify(productInstanceClient, times(1)).put(productInstance);
        verify(autoPriceAggregator,times(1)).aggregatePricesOf(priceBookDTO,new LineItemId(lineItemId));
        assertThat(pricelines.get(0).getChargePrice().getDiscountPercentage().doubleValue(), is(18.0));
    }

    @Test
    public void shouldNotUpdateIfNoPricelinesUpdated(){
        Map<String, Double> priceLineToDiscountMap = newHashMap();
        priceLineToDiscountMap.put("id1",10.98);
        priceLineToDiscountMap.put("id2",10.98);
        priceLineToDiscountMap.put("id3",10.98);
        ImportDiscounts discounts = createImportDiscounts(productInstanceId, version, priceLineToDiscountMap);
        List<PriceLine> pricelines = newArrayList();
        setExpectations(pricelines);
        productInstance = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId(productInstanceId)
                                                       .withProductInstanceVersion(version)
                                                       .withPriceLines(pricelines).build();
        when(productInstanceClient.getByAssetKey(new LengthConstrainingProductInstanceId(productInstanceId),
                                                 new ProductInstanceVersion(version))).thenReturn(productInstance);
        ChangeTracker tracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        //when(productInstanceClient.getAssetsDiff(any(String.class), any(Long.class), any(Long.class), eq(InstanceTreeScenario.PROVIDE))).thenReturn(mergeResult);
        when(productInstanceClient.getAssetsDiff(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()),
                                                 new ProductInstanceVersion(productInstance.getProductInstanceVersion()),
                                                 isNotNull(productInstance.getAssetSourceVersion()) ? new ProductInstanceVersion(productInstance.getAssetSourceVersion()) : null,
                                                 InstanceTreeScenario.PROVIDE)).thenReturn(mergeResult);
        when(mergeResult.changeFor(any(OrderItemItemPrice.class))).thenReturn(ChangeType.ADD);
        updater.updateDiscountsFrom(newArrayList(discounts));
        verify(productInstanceClient, times(0)).put(productInstance);
    }

    @Test
    public void shouldUpdateOnlyForMatchingPricelines(){
        Map<String, Double> priceLineToDiscountMap = newHashMap();
        priceLineToDiscountMap.put("id1",.28);
        priceLineToDiscountMap.put("id2",.28);
        priceLineToDiscountMap.put("id3",.28);
        ImportDiscounts discounts = createImportDiscounts(productInstanceId, version, priceLineToDiscountMap);
        List<PriceLine> pricelines = newArrayList();
        pricelines.add(createPriceLineWith("id1","priceline Name1"));
        pricelines.add(createPriceLineWith("id2","priceline Name1"));
        //setExpectations(pricelines);
        productInstance = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId(productInstanceId)
                                                       .withProjectId(projectID).withLineItemId(lineItemId).withQuoteOptionId(quoteOptionId)
                                                       .withProductInstanceVersion(version)
                                                       .withPriceLines(pricelines).build();
        when(productInstanceClient.getByAssetKey(new LengthConstrainingProductInstanceId(productInstanceId),
                                                 new ProductInstanceVersion(version))).thenReturn(productInstance);
        ChangeTracker tracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        //when(productInstanceClient.getAssetsDiff(any(String.class), any(Long.class), any(Long.class), eq(InstanceTreeScenario.PROVIDE))).thenReturn(mergeResult);
        when(productInstanceClient.getAssetsDiff(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()),
                                                 new ProductInstanceVersion(productInstance.getProductInstanceVersion()),
                                                 isNotNull(productInstance.getAssetSourceVersion()) ? new ProductInstanceVersion(productInstance.getAssetSourceVersion()) : null,
                                                 InstanceTreeScenario.PROVIDE)).thenReturn(mergeResult);
        when(mergeResult.changeFor(any(OrderItemItemPrice.class))).thenReturn(ChangeType.ADD);
        updater.updateDiscountsFrom(newArrayList(discounts));
        verify(productInstanceClient, times(1)).put(productInstance);
        assertThat(pricelines.get(0).getChargePrice().getDiscountPercentage().doubleValue(),is(28.0));
        assertThat(pricelines.get(1).getChargePrice().getDiscountPercentage().doubleValue(),is(0.0));
    }

    private PriceLine createPriceLineWith(String id, String name) {
        return new PriceLineFixture().withId(id).withPriceLineName(name).withChargePrice(12.01).build();
    }

    private void setExpectations(List<PriceLine> prices) {
        when(productInstance.getPriceLines()).thenReturn(prices);
        when(productInstanceClient.getByAssetKey(new LengthConstrainingProductInstanceId(productInstanceId), new ProductInstanceVersion(version)))
            .thenReturn(productInstance);
    }

    private ImportDiscounts createImportDiscounts(String productInstanceId, Long version, Map<String, Double> priceLineToDiscountMap) {
        ImportDiscounts importDiscounts = new ImportDiscounts();
        importDiscounts.setProductInstanceId(productInstanceId);
        importDiscounts.setProductInstanceVersion(version);
        importDiscounts.setPriceLineToDiscountMap(priceLineToDiscountMap);
        return importDiscounts;
    }

}
