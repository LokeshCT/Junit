package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.project.PricingCaveat;
import com.bt.rsqe.domain.project.PricingCaveatDoc;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.domain.product.DefaultProductInstanceFixture.aProductInstance;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModelFixture.aPricingSheetModel;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModelFixture.aPricingSheetProductModel;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PricingSheetDataModelTest {

    private PricingSheetDataModel pricingSheetDataModel;
    final HashSet<PriceBookDTO> priceBooks = newHashSet();
        final HashSet<String> productNames = newHashSet();

    @Before
    public void setup() {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestData();
    }

    @Test
    public void shouldReturnCurrentDate() {
        String expected = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        assertThat(pricingSheetDataModel.getCurrentDate(), is(expected));
    }

    @Test
    public void shouldReturnPricingSheetModelMap() {
        Map<String, Object> map = pricingSheetDataModel.map();
        assertThat(map.size(), is(20));
        assertTrue(map.containsKey("project"));
        assertTrue(map.containsKey("customer"));
        assertTrue(map.containsKey("accountManager"));
        assertTrue(map.containsKey("quoteOption"));
        assertTrue(map.containsKey("centralSite"));
        assertTrue(map.containsKey("model"));
        assertTrue(map.containsKey("products"));
        assertTrue(map.containsKey("specialBidProducts"));
        assertTrue(map.containsKey("contractProducts"));
        assertTrue(map.containsKey("priceBooks"));
        assertTrue(map.containsKey("PTP"));
        assertTrue(map.containsKey("RRP"));
        assertTrue(map.containsKey("EUP"));
        assertTrue(map.containsKey("BUDGETARY"));
        assertTrue(map.containsKey("FIRM"));
        assertTrue(map.containsKey("ONE_TIME"));
        assertTrue(map.containsKey("accessProducts"));
    }

    @Test
    public void shouldReturnAllSummaryPrices() {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataForModifyJourney();
        List<PricingSheetPriceModel> allPrices = pricingSheetDataModel.getAllSummaryPrices("EXISTING",false);
        assertThat(allPrices.size(), is(8));
    }

    @Test
    public void shouldReturnAllDetailedPrices() {
        List<PricingSheetPriceModel> allPrices = pricingSheetDataModel.getAllDetailedPrices();
        assertThat(allPrices.size(), is(11));
    }

    @Test
    public void shouldHaveGetterMethodsForJxls() throws NoSuchMethodException {
        pricingSheetDataModel.getClass().getMethod("getCustomer");
        pricingSheetDataModel.getClass().getMethod("getProject");
        pricingSheetDataModel.getClass().getMethod("getQuoteOption");
        pricingSheetDataModel.getClass().getMethod("getCentralSite");
        pricingSheetDataModel.getClass().getMethod("getAccountManager");
        pricingSheetDataModel.getClass().getMethod("getModel");
    }

    @Test
    public void shouldAggregatePrices(){
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataBothSpecialBidAndStandardForModifyJourneyWithSite();
        assertThat(pricingSheetDataModel.getAggregatedPrice("one time",true,"EXISTING",true,"ptp"),is(240.00));
        assertThat(pricingSheetDataModel.getAggregatedPrice("recurring",true,"EXISTING",true,"eup"), is(40.0));
        //without site specific
        //assertThat(pricingSheetDataModel.getAggregatedPrice("one time",false,"EXISTING",true,"PTP"), is(40.0));
        //assertThat(pricingSheetDataModel.getAggregatedPrice("recurring",false,"EXISTING",true,"EUP"), is(40.0));

    }

    @Test
    public void shouldGetStandardProductAggregatedSiteLevelManagementCharges(){
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestData();
        assertThat(pricingSheetDataModel.getAggregatedSiteLevelManagementCharges("one time",false,false,"NEW","eup"),is(333.00));
        assertThat(pricingSheetDataModel.getAggregatedSiteLevelManagementCharges("recurring",false,false,"NEW","eup"),is(333.00));
        assertThat(pricingSheetDataModel.getAggregatedSiteLevelManagementCharges("one time",false,false,"EXISTING","eup"),is(0.0));
        assertThat(pricingSheetDataModel.getAggregatedSiteLevelManagementCharges("one time",false,false,"NEW","ptp"),is(100.00));
    }

    @Test
    public void shouldGetAggregatedSiteLevelTotalManagementCharges() {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetAccessCaveatsTestData();
        assertThat(pricingSheetDataModel.getAggregatedSiteLevelTotalManagementCharges("one time",false,"NEW","eup"),is(100.00));
        assertThat(pricingSheetDataModel.getAggregatedSiteLevelTotalManagementCharges("recurring",false,"NEW","eup"),is(100.00));
    }

    @Test
    public void shouldAggregateTotalSiteLevelManagementChargesToZeroForSpecialBid(){
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetSpecialBidTestData();
        assertThat(pricingSheetDataModel.getAggregatedSiteLevelTotalManagementCharges("one time",true,"NEW","eup"),is(0.00));
        assertThat(pricingSheetDataModel.getAggregatedSiteLevelTotalManagementCharges("recurring",true,"NEW","eup"), is(0.00));
    }

    @Test
    public void shouldReturnAllSummaryPricesForModify() {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataForModifyJourney();
        List<PricingSheetPriceModel> allPrices = pricingSheetDataModel.getAllSummaryPrices("EXISTING",false);
        assertThat(allPrices.size(), is(8));
    }

    @Test
    public void shouldReturnPriceStatus() {
        PricingSheetSpecialBidProduct specialBidProduct = mock(PricingSheetSpecialBidProduct.class);
        PricingSheetProductModel productModel = mock(PricingSheetProductModel.class);
        PricingSheetContractProduct contractProduct = mock(PricingSheetContractProduct.class);
        PricingSheetProductModel accessProduct = mock(PricingSheetProductModel.class);
        when(productModel.getProductInstance()).thenReturn(aProductInstance().build());
        pricingSheetDataModel = new PricingSheetDataModel(null,null,null,null,null,newArrayList(productModel),newArrayList(specialBidProduct), newArrayList(contractProduct), priceBooks, productNames, newArrayList(accessProduct), null);
        when(specialBidProduct.getPricingStatusForPricingSheet()).thenReturn("Budgetary");
        when(productModel.getPricingStatusForPricingSheet()).thenReturn("Firm");
        assertThat(pricingSheetDataModel.fetchPricingStatus(),is("Budgetary"));
        when(specialBidProduct.getPricingStatusForPricingSheet()).thenReturn("Firm");
        when(accessProduct.getPricingStatusForPricingSheet()).thenReturn("Firm");
        assertThat(pricingSheetDataModel.fetchPricingStatus(),is("Firm"));
    }

    @Test
    public void shouldReturnPricingCaveats(){
        PricingSheetSpecialBidProduct specialBidProduct = mock(PricingSheetSpecialBidProduct.class);
        PricingSheetProductModel productModel = mock(PricingSheetProductModel.class);
        PricingSheetContractProduct contractProduct = mock(PricingSheetContractProduct.class);
        when(productModel.getProductInstance()).thenReturn(aProductInstance().build());
        pricingSheetDataModel = new PricingSheetDataModel(null,null,null,null,null,newArrayList(productModel),newArrayList(specialBidProduct), newArrayList(contractProduct), priceBooks, productNames, null, null);
        List<PricingCaveat> pricingCaveats = newArrayList();
        List<PricingCaveatDoc> pricingCaveatDocs = newArrayList();
        pricingCaveatDocs.add(new PricingCaveatDoc("testDocName","testDocLink"));
        pricingCaveats.add(new PricingCaveat("testCaveatId","testCaveatType","testCaveatDescription",pricingCaveatDocs));
        when(productModel.getPricingCaveats()).thenReturn(pricingCaveats);
        assertThat(pricingSheetDataModel.getPricingCaveats().size(),is(1));
        assertThat(pricingSheetDataModel.getPricingCaveats().get(0).caveatId,is("testCaveatId"));
        assertThat(pricingSheetDataModel.getPricingCaveats().get(0).getPricingCaveatDocs().get(0).getDocLink(),is("testDocLink"));
    }

    @Test
    public void shouldGetContractSummaryTotals(){
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetContractTestData();
        assertTrue(new BigDecimal(666).compareTo(pricingSheetDataModel.getContractNewNonRecurringSummaryTotal())==0);
        assertTrue(new BigDecimal(666).compareTo(pricingSheetDataModel.getContractNewRecurringSummaryTotal())==0);

        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetContractTestDataForModify();
        assertTrue(new BigDecimal(666).compareTo(pricingSheetDataModel.getContractExistingNonRecurringSummaryTotal())==0);
        assertTrue(new BigDecimal(666).compareTo(pricingSheetDataModel.getContractExistingRecurringSummaryTotal())==0);
    }

    @Test
    public void shouldOrderProducts() {

        ProductInstance related1 = aProductInstance().withAssetKey("related1", 1L).build();
        ProductInstance related2 = aProductInstance().withAssetKey("related2", 1L).build();
        ProductInstance related3 = aProductInstance().withAssetKey("related3", 1L).build();

        ProductInstance sellableProduct1 = aProductInstance().withAssetKey("sellableProduct1", 1L).withRelatedToProductInstance(related1).build();
        ProductInstance sellableProduct2 = aProductInstance().withAssetKey("sellableProduct2", 1L).withRelatedToProductInstance(related2).build();
        ProductInstance sellableProduct3 = aProductInstance().withAssetKey("sellableProduct3", 1L).withRelatedToProductInstance(related3).build();

        PricingSheetProductModel sellableProduct1Model = aPricingSheetProductModel().withProductInstance(sellableProduct1).build();
        PricingSheetProductModel sellableProduct2Model = aPricingSheetProductModel().withProductInstance(sellableProduct2).build();
        PricingSheetProductModel sellableProduct3Model = aPricingSheetProductModel().withProductInstance(sellableProduct3).build();
        PricingSheetProductModel relatedProudct1Model = aPricingSheetProductModel().withProductInstance(related1).build();
        PricingSheetProductModel relatedProduct2Model = aPricingSheetProductModel().withProductInstance(related2).build();
        PricingSheetProductModel relatedProduct3Model = aPricingSheetProductModel().withProductInstance(related3).build();

        PricingSheetDataModel pricingSheetDataModel = aPricingSheetModel()
            .withPricingSheetProductModel(relatedProudct1Model, relatedProduct2Model, sellableProduct1Model, sellableProduct2Model, sellableProduct3Model, relatedProduct3Model).build();

        List<PricingSheetProductModel> products = pricingSheetDataModel.getProducts();

        assertThat(products.size(), Is.is(6));
        assertThat((ArrayList<PricingSheetProductModel>) products, is(newArrayList(sellableProduct1Model, relatedProudct1Model,
                                                                                   sellableProduct2Model, relatedProduct2Model,
                                                                                   sellableProduct3Model, relatedProduct3Model)));

    }
    @Test
    public void shouldReturnTotalOfNewNonRecurringPriceLinesForContractProduct() {

        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetContractTestData();
        BigDecimal totalPrice = pricingSheetDataModel.getContractSummaryTotal("NEW", false, "nonRecurringEupPrice");
        assertTrue(totalPrice.compareTo(new BigDecimal(666)) == 0);
    }

    @Test
    public void shouldReturnTotalOfNewRecurringPriceLinesForContractProduct() {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetContractTestData();
        BigDecimal totalPrice = pricingSheetDataModel.getContractSummaryTotal("NEW", false, "recurringEupPrice");
        assertTrue(totalPrice.compareTo(new BigDecimal(666)) == 0);
    }

    @Test
    public void shouldReturnTotalOfExistingNonRecurringPriceLinesForContractProduct() {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetContractTestDataForModify();
        BigDecimal totalPrice = pricingSheetDataModel.getContractSummaryTotal("EXISTING", false, "nonRecurringEupPrice");
        assertTrue(totalPrice.compareTo(new BigDecimal(666)) == 0);
    }

    @Test
    public void shouldReturnTotalOfExistingRecurringPriceLinesForContractProduct() {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetContractTestDataForModify();
        BigDecimal totalPrice = pricingSheetDataModel.getContractSummaryTotal("EXISTING", false, "recurringEupPrice");
        assertTrue(totalPrice.compareTo(new BigDecimal(666)) == 0);
    }
    @Test
    public void shouldReturnBidDetails() {

    }
}
