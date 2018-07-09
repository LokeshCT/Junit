package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.order.ItemPrice;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.DefaultProductInstance;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.pricing.fixture.PricingConfigDTOFixture;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.AccessCaveatDescriptionDTO;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.bt.rsqe.domain.product.DefaultProductInstanceFixture.*;
import static com.google.common.collect.Collections2.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class PricingSheetProductModelTest {

    PricingSheetProductModel pricingSheetProductModel;
    PricingSheetProductModel pricingSheetProductModelWithNoAttributes;
    PricingSheetProductModel aSiteAgnosticPricingSheetProductModel;
    PricingSheetProductModel specialBidPricingSheetProductModel;
    private DefaultProductInstance productInstance;
    private DefaultProductInstance productInstanceWithoutCaveats;
    private HashMap<String, String> attributes;
    private PricingSheetProductModel accessProductModel;
     @Mock
    private SiteDTO site;
    @Mock
    private ProductOffering productOffering;
    private QuoteOptionItemDTO quoteOptionIem;
    @Mock
    private CaveatResource caveatResource;
     private String qrefId = "qrefId";
    private String caveats = "123";
    @Mock
    private PricingClient pricingClient;


    @Before
    public void setup() {
        initMocks(this);
        this.pricingSheetProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelWithAChild();
        this.pricingSheetProductModelWithNoAttributes = new PricingSheetTestDataFixture().aPrincgSheetProductModelWithNoAttributes();
        this.aSiteAgnosticPricingSheetProductModel = new PricingSheetTestDataFixture().aSiteAgnosticPricingSheetProductModel();
        this.specialBidPricingSheetProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelWithASpecialBidChild();
         productInstance = aProductInstance().withAttributeValue("QREF", qrefId).withAttributeValue("CAVEATS", caveats).build();
        productInstanceWithoutCaveats = aProductInstance().build();
        attributes = new HashMap<String, String>();
        attributes.put("CAVEATS", caveats);
        attributes.put("CONFIGURATION_CATEGORY", "category");
        attributes.put("QREF", qrefId);
        PriceLine priceline = new PriceLineFixture().withPpsrId(1l).withPmfId("DummyPmfId").build();
        productInstance.addPriceLine(priceline);
        ChangeTracker changeTracker = mock(ChangeTracker.class);;
        MergeResult mergeResult = new MergeResult(Lists.<ProductInstance>newArrayList(productInstance),changeTracker);
        when(changeTracker.changeFor(any(ProductInstance.class))).thenReturn(ChangeType.ADD);
        when(changeTracker.changeFor(any(ItemPrice.class))).thenReturn(ChangeType.ADD);
        quoteOptionIem = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        PricingConfig config = PricingConfigDTOFixture.pricingConfig();
        when(pricingClient.getPricingConfig()).thenReturn(config);
        accessProductModel = new PricingSheetProductModel(site,productInstance,quoteOptionIem,mergeResult,caveatResource, pricingClient, null);
        productInstance.setQref(qrefId);

    }

    @Test
    public void shouldReturnScode() {
        assertThat(pricingSheetProductModel.getSCode(), is("S0308454"));
    }

    @Test
    public void shouldReturnInstanceCharacteristicValue() {
        assertThat(pricingSheetProductModel.getInstanceCharacteristic(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE), is("BUNDLE NAME VALUE"));
        assertThat(pricingSheetProductModelWithNoAttributes.getInstanceCharacteristic("abcd"), is(StringUtils.EMPTY));
    }

    @Test
    public void shouldCheckIfSiteInstallable() {
        assertTrue(pricingSheetProductModel.isSiteInstallable());
        assertFalse(aSiteAgnosticPricingSheetProductModel.isSiteInstallable());
    }

    @Test
    public void shouldReturnEupPriceBook() {
        assertThat(pricingSheetProductModel.getEupPriceBook(), is("eup"));
    }

    @Test
    public void shouldReturnPtpPriceBook() {
        assertThat(pricingSheetProductModel.getPtpPriceBook(), is("ptp"));
    }

    @Test
    public void shouldReturnResilienceType() {
        assertThat(pricingSheetProductModel.getResilienceType(), is(""));
    }

    @Test
    public void shouldReturnChildrenProductModels() {
        List<PricingSheetProductModel> children = pricingSheetProductModel.getChildren();
        assertThat(children.size(), is(1));
    }

    @Test
    public void shouldReturnAllChildren() {
        List<PricingSheetProductModel> children = pricingSheetProductModel.getAllChildren();
        assertThat(children.size(), is(2));
    }

    @Test
    public void shouldReturnSummaryPriceLines() {
        List<PricingSheetPriceModel> allPriceLines = pricingSheetProductModel.getSummaryPriceLines();
        assertThat(allPriceLines.size(), is(1));
    }

    @Test
    public void shouldReturnDetailedPriceLines() {
        List<PricingSheetPriceModel> allPriceLines = pricingSheetProductModel.getDetailedPriceLines("NEW");
        assertThat(allPriceLines.size(), is(2));
    }

    @Test
    public void shouldReturnDummyPriceLineInCaseIfThereAreNoPriceLines() {
        PricingSheetProductModel productWithNoPriceLines = pricingSheetProductModelWithNoAttributes.getChildren().get(0);
        List<PricingSheetPriceModel> priceLines = productWithNoPriceLines.getSummaryPriceLines();
        assertThat(priceLines.size(), is(1));
        assertThat(priceLines.get(0).getPmfId(), is("DummyPmfId"));
    }

    @Test
    public void shouldReturnSummarySheetPriceLineModels() {
        List<PricingSheetPriceModel> allSummarySheetPriceLines = pricingSheetProductModel.getAllSummarySheetPriceLines("NEW");
        assertThat(allSummarySheetPriceLines.size(), is(3));
    }

    @Test
    public void shouldReturnNewNonRecurringPriceLines(){
        BigDecimal totalPrice = pricingSheetProductModel.getNewNonRecurringSummaryTotal();
        assertTrue(totalPrice.compareTo(new BigDecimal(999))==0);
    }

    @Test
    public void shouldReturnNewRecurringPriceLines(){
        BigDecimal totalPrice = pricingSheetProductModel.getNewRecurringSummaryTotal();
        assertTrue(totalPrice.compareTo(new BigDecimal(999))==0);
    }

    @Test
    public void shouldReturnExistingNonRecurringPriceLines(){
        PricingSheetProductModel modifyProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelForModifyJourney();
        BigDecimal totalPrice = modifyProductModel.getExistingNonRecurringSummaryTotal();
        assertTrue(totalPrice.compareTo(new BigDecimal(999))==0);
    }

    @Test
    public void shouldReturnExistingRecurringPriceLines(){
        PricingSheetProductModel modifyProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelForModifyJourney();
        BigDecimal totalPrice = modifyProductModel.getExistingRecurringSummaryTotal();
        assertTrue(totalPrice.compareTo(new BigDecimal(999))==0);
    }

    @Test
    public void shouldReturnDetailSheetPriceLineModels() {
        List<PricingSheetPriceModel> allDetailSheetPriceLines = pricingSheetProductModel.getAllDetailSheetPriceLines();
        assertThat(allDetailSheetPriceLines.size(), is(5));
    }

    @Test
    public void shouldReturnCADetailSheetPriceForModifyJourney() {
        PricingSheetProductModel modifyProductModel = new PricingSheetTestDataFixture().aPricingSheetProductModelForModifyJourney();
        assertThat(modifyProductModel.getAction(), is("MODIFY"));
    }

    @Test
    public void shouldNotReturnDummyPriceLineIfThereArePriceLines() {
        List<PricingSheetPriceModel> beforeRemovingDummayPrices = pricingSheetProductModel.getAllPriceLines(PriceSuppressStrategy.DetailedSheet);
        Collection<PricingSheetPriceModel> afterRemovingDummayPrices = filter(beforeRemovingDummayPrices, PricingSheetPriceModel.notDummyPriceModelPredicate());
        assertThat(beforeRemovingDummayPrices.size(), is(afterRemovingDummayPrices.size()));
    }

    @Test
    public void shouldHaveGetterMethodsForJxls() throws NoSuchMethodException {
        pricingSheetProductModel.getClass().getMethod("getProductInstance");
        pricingSheetProductModel.getClass().getMethod("getQuoteOptionItem");
        pricingSheetProductModel.getClass().getMethod("getSite");
    }


    @Test
    public void shouldReturnNonRecurringTotalPriceForProductInstanceId() {
        double actual2 = pricingSheetProductModel.getDetailedSheetTotalEupPriceForProductInstanceId("rootProductInstanceId1", "one time","NEW");
        assertThat(actual2, is(333.00));

        actual2 = pricingSheetProductModel.getDetailedSheetTotalEupPriceForProductInstanceId("abcd", "one time","NEW");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnRecurringTotalPriceForProductInstanceId() {
        double actual2 = pricingSheetProductModel.getDetailedSheetTotalEupPriceForProductInstanceId("rootProductInstanceId1", "recurring","NEW");
        assertThat(actual2, is(333.00));

        actual2 = pricingSheetProductModel.getDetailedSheetTotalEupPriceForProductInstanceId("abcd", "recurring","New");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnNonRecurringSiteLevelManagementPriceForProductInstanceId() {
        double actual2 = pricingSheetProductModel.getDetailedSheetSiteLevelManagementEupPriceForProductInstanceId("rootProductInstanceId1", "one time", "NEW");
        assertThat(actual2, is(333.00));

        actual2 = pricingSheetProductModel.getDetailedSheetSiteLevelManagementEupPriceForProductInstanceId("abcd", "one time", "NEW");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnRecurringSiteLevelManagementPriceForProductInstanceId() {
        double actual2 = pricingSheetProductModel.getDetailedSheetSiteLevelManagementEupPriceForProductInstanceId("rootProductInstanceId1", "recurring", "NEW");
        assertThat(actual2, is(333.00));

        actual2 = pricingSheetProductModel.getDetailedSheetSiteLevelManagementEupPriceForProductInstanceId("abcd", "recurring", "NEW");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnNonRecurringSpecialBidPriceForProductInstanceId() {
        double actual2 = specialBidPricingSheetProductModel.getDetailedSheetSpecialBidEupPriceForProductInstanceId("productInstanceId", "one time", "NEW");
        assertThat(actual2, is(20.00));

        actual2 = specialBidPricingSheetProductModel.getDetailedSheetSpecialBidEupPriceForProductInstanceId("abcd", "one time", "NEW");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnRecurringSpecialBidPriceForProductInstanceId() {
        double actual2 = specialBidPricingSheetProductModel.getDetailedSheetSpecialBidEupPriceForProductInstanceId("productInstanceId", "recurring", "NEW");
        assertThat(actual2, is(20.00));

        actual2 = specialBidPricingSheetProductModel.getDetailedSheetSpecialBidEupPriceForProductInstanceId("abcd", "recurring", "NEW");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnNonRecurringTotalPriceForProductInstanceIdWithIndirectUser() {
        double actual2 = pricingSheetProductModel.getIndirectUserDetailedSheetTotalPtpPriceForProductInstanceId("rootProductInstanceId1", "one time", "New");
        assertThat(actual2, is(100.00));

        actual2 = pricingSheetProductModel.getIndirectUserDetailedSheetTotalPtpPriceForProductInstanceId("abcd", "one time", "New");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnRecurringTotalPriceForProductInstanceIdWithIndirectUser() {
        double actual2 = pricingSheetProductModel.getIndirectUserDetailedSheetTotalPtpPriceForProductInstanceId("rootProductInstanceId1", "recurring", "New");
        assertThat(actual2, is(100.00));

        actual2 = pricingSheetProductModel.getIndirectUserDetailedSheetTotalPtpPriceForProductInstanceId("abcd", "recurring", "NEW");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnNonRecurringSiteLevelManagementPriceForProductInstanceIdWithIndirectUser() {
        double actual2 = pricingSheetProductModel.getIndirectUserDetailedSheetSiteLevelManagementPtpPriceForProductInstanceId("rootProductInstanceId1", "one time", "NEW");
        assertThat(actual2, is(100.00));

        actual2 = pricingSheetProductModel.getIndirectUserDetailedSheetSiteLevelManagementPtpPriceForProductInstanceId("abcd", "one time", "NEW");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnRecurringSiteLevelManagementPriceForProductInstanceIdWithIndirectUser() {
        double actual2 = pricingSheetProductModel.getIndirectUserDetailedSheetSiteLevelManagementPtpPriceForProductInstanceId("rootProductInstanceId1", "recurring", "NEW");
        assertThat(actual2, is(100.00));

        actual2 = pricingSheetProductModel.getIndirectUserDetailedSheetSiteLevelManagementPtpPriceForProductInstanceId("abcd", "recurring", "NEW");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnNonRecurringSpecialBidPriceForProductInstanceIdWithIndirectUser() {
        double actual2 = specialBidPricingSheetProductModel.getIndirectUserDetailedSheetSpecialBidPtpPriceForProductInstanceId("productInstanceId", "one time");
        assertThat(actual2, is(120.00));

        actual2 = specialBidPricingSheetProductModel.getIndirectUserDetailedSheetSpecialBidPtpPriceForProductInstanceId("abcd", "one time");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldReturnRecurringSpecialBidPriceForProductInstanceIdWithIndirectUser() {
        double actual2 = specialBidPricingSheetProductModel.getIndirectUserDetailedSheetSpecialBidPtpPriceForProductInstanceId("productInstanceId", "recurring");
        assertThat(actual2, is(120.00));

        actual2 = specialBidPricingSheetProductModel.getIndirectUserDetailedSheetSpecialBidPtpPriceForProductInstanceId("abcd", "recurring");
        assertThat(actual2, is(0.0));
    }

    @Test
    public void shouldGetChildForGivenScode() {
        PricingSheetProductModel actual = pricingSheetProductModel.getChild("S0308469");
        assertThat(actual.getSCode(), is("S0308469"));

        actual = pricingSheetProductModel.getChild("abc");
        assertNull(actual);
    }

    @Test
    public void shouldGetPricingStatus(){
        ProductInstance productInstance = mock(ProductInstance.class);
        PricingSheetProductModel productModel = new PricingSheetProductModel(null,productInstance,null,null, caveatResource, pricingClient, null);
        for(PricingStatus status: PricingStatus.values()){
            when(productInstance.getPricingStatus()).thenReturn(status);
            if(status.equals(PricingStatus.BUDGETARY) || status.equals(PricingStatus.ICB_BUDGETARY)){
                assertThat(productModel.getPricingStatusForPricingSheet(),is("Budgetary"));
            }
        }
    }

    @Test
    public void shouldReturnRelationName() {
        assertThat(pricingSheetProductModel.getRelationName("child1ProductInstanceId"), is("relationship"));
    }


    @Test
    public void shouldReturnDetailedPriceLinesForAccess() {
        List<PricingSheetPriceModel> allPriceLines = accessProductModel.getDetailedPriceLines("NEW");
        assertThat(allPriceLines.size(), is(1));
        assertThat(allPriceLines.get(0).getPmfId(), is("DummyPmfIdAdd"));
    }

    @Test
    public void shouldGetCaveatListWhenOneCaveatAssociated() {
        assertThat(accessProductModel.getCaveatsList().size(), is(1));
        assertThat(accessProductModel.getCaveatsList().get(0), is(caveats));
    }

    @Test
    public void shouldGetCaveatDescriptionListWhenOneDescriptionAvailable(){
        String caveatId = "caveatId";
        List<AccessCaveatDescriptionDTO> caveatDescriptionDTOList = newArrayList();
        caveatDescriptionDTOList.add(new AccessCaveatDescriptionDTO(caveatId, "description", null, null));
        when(caveatResource.getCaveatDescriptionFromId(caveatId)).thenReturn(caveatDescriptionDTOList);
        assertThat(accessProductModel.getCaveatDescriptionList(caveatId).size(), is(1));
        assertThat(accessProductModel.getCaveatDescriptionList(caveatId).get(0).toString(), is("description"));
    }

}
