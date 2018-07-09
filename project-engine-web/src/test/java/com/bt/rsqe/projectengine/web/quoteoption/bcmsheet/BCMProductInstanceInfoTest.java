package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.project.Price;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.integration.PriceLineFixture;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BCMProductInstanceInfoTest {
    private BCMProductInstanceInfo bcmProductInstanceInfo;
    private BCMSiteDetails bcmSiteDetails;
    private Map<BCMPriceLineInfoKey, List<BCMPriceLineInfo>> bcmPriceLineInfoList = new HashMap<BCMPriceLineInfoKey, List<BCMPriceLineInfo>>();
    private String sCode = "S12345678";
    private String displayName = "test Model";
    private String versionNumber = "1.0";
    private String productInstanceId = "98765";
    private Long productInstanceVersion = new Long(2);
    private String action = "provide";
    private String priceStatus = "FIRM";
    private boolean isIndirect = false;
    private Map<String, List<BCMProductInstanceInfo>> relatedInstances = new HashMap<String, List<BCMProductInstanceInfo>>();
    private Map<String, String> bcmInstanceCharacteristicsMap = new HashMap<String, String>();
    private boolean isSpecialBid = false;
    private String currency = "GBP";
    private String quoteOptionItemId = "1234-1234-1234-1234";

    BCMProductInstanceInfo testProductInstance1 = new BCMProductInstanceInfo(bcmSiteDetails,
                                                            bcmPriceLineInfoList,
                                                            sCode,
                                                            "test Instance 1",
                                                            versionNumber,
                                                            productInstanceId,
                                                            productInstanceVersion,
                                                            action,
                                                            priceStatus,
                                                            isIndirect,
                                                            bcmInstanceCharacteristicsMap,
                                                            isSpecialBid,
                                                            currency,
                                                            quoteOptionItemId, null, null, "");

    BCMProductInstanceInfo testProductInstance2 = new BCMProductInstanceInfo(bcmSiteDetails,
                                                            bcmPriceLineInfoList,
                                                            sCode,
                                                            "test Instance 1",
                                                            versionNumber,
                                                            productInstanceId,
                                                            productInstanceVersion,
                                                            action,
                                                            priceStatus,
                                                            isIndirect,
                                                            bcmInstanceCharacteristicsMap,
                                                            isSpecialBid,
                                                            currency,
                                                            quoteOptionItemId, null, null, "");

    BCMPriceLineInfo oneTimeEupPriceLine = new BCMPriceLineInfo("oneTimeEupPriceLine",
                                                                "1",
                                                                PriceType.ONE_TIME,
                                                                new Price("1", new BigDecimal("28"), new BigDecimal("10")), //charge Price
                                                                new Price("1", new BigDecimal("82"), new BigDecimal("10")), //EUP Price
                                                                null,
                                                                "",
                                                                PriceCategory.END_USER_PRICE.getLabel(),
                                                                new BigDecimal("10"));

    BCMPriceLineInfo recurringEupPriceLine = new BCMPriceLineInfo("recurringEupPriceLine",
                                                                "1",
                                                                PriceType.RECURRING,
                                                                new Price("1", new BigDecimal("38"), new BigDecimal("5")), //charge Price
                                                                new Price("1", new BigDecimal("48"), new BigDecimal("5")), //EUP Price
                                                                null,
                                                                "",
                                                                PriceCategory.END_USER_PRICE.getLabel(),
                                                                new BigDecimal("5"));
    BCMPriceLineInfo oneTimePTPPriceLine = new BCMPriceLineInfo("oneTimePTPPriceLine",
                                                                "1",
                                                                PriceType.ONE_TIME,
                                                                new Price("1", new BigDecimal("76"), new BigDecimal("3")), //charge Price
                                                                new Price("1", new BigDecimal("10"), new BigDecimal("3")), //EUP Price
                                                                null,
                                                                "",
                                                                PriceCategory.PRICE_TO_PARTNER.getLabel(),
                                                                new BigDecimal("3"));


    BCMPriceLineInfo recurringPTPPriceLine = new BCMPriceLineInfo("recurringPTPPriceLine",
                                                                "1",
                                                                PriceType.RECURRING,
                                                                new Price("1", new BigDecimal("55"), new BigDecimal("20")), //charge Price
                                                                new Price("1", new BigDecimal("65"), new BigDecimal("20")), //EUP Price
                                                                null,
                                                                "",
                                                                PriceCategory.PRICE_TO_PARTNER.getLabel(),
                                                                new BigDecimal("20"));
    BCMPriceLineInfo oneTimeCostLine = new BCMPriceLineInfo("oneTimeCostLine",
                                                                "1",
                                                                PriceType.ONE_TIME,
                                                                new Price("1", new BigDecimal("21"), new BigDecimal("3")), //charge Price
                                                                new Price("1", new BigDecimal("11"), new BigDecimal("3")), //EUP Price
                                                                null,
                                                                "",
                                                                PriceCategory.COST.getLabel(),
                                                                new BigDecimal("3"));


    BCMPriceLineInfo recurringCostLine= new BCMPriceLineInfo("recurringCostLine",
                                                                "1",
                                                                PriceType.RECURRING,
                                                                new Price("1", new BigDecimal("45"), new BigDecimal("20")), //charge Price
                                                                new Price("1", new BigDecimal("34"), new BigDecimal("20")), //EUP Price
                                                                null,
                                                                "",
                                                                PriceCategory.COST.getLabel(),
                                                                new BigDecimal("20"));


    @Before
    public void setUp() {
        bcmSiteDetails = mock(BCMSiteDetails.class);
        List<BCMProductInstanceInfo> related = new LinkedList<BCMProductInstanceInfo>();
        related.add(testProductInstance1);
        related.add(testProductInstance2);
        relatedInstances.put("Related", related);

        PriceLine priceLine1 = PriceLineFixture.aPriceLine().withUserEntered("Y").withPmfId("aPmfId").withStatus(PricingStatus.NOT_PRICED).build();
        PriceLine priceLine2 = PriceLineFixture.aPriceLine().withPmfId("aPmfId").withStatus(PricingStatus.NOT_PRICED).build();
        PriceLine priceLine3 = PriceLineFixture.aPriceLine().withPmfId("aPmfId1").withStatus(PricingStatus.NOT_PRICED).build();

        bcmProductInstanceInfo = new BCMProductInstanceInfo(bcmSiteDetails,
                                                            bcmPriceLineInfoList,
                                                            sCode,
                                                            displayName,
                                                            versionNumber,
                                                            productInstanceId,
                                                            productInstanceVersion,
                                                            action,
                                                            priceStatus,
                                                            isIndirect,
                                                            bcmInstanceCharacteristicsMap,
                                                            isSpecialBid,
                                                            currency,
                                                            quoteOptionItemId,
                                                            newArrayList(priceLine1,priceLine2,priceLine3), null, "");

        bcmPriceLineInfoList.put(new BCMPriceLineInfoKey("M111", PriceType.ONE_TIME.getValue()), Arrays.asList(oneTimeEupPriceLine));
        bcmPriceLineInfoList.put(new BCMPriceLineInfoKey("M111", PriceType.RECURRING.getValue()), Arrays.asList(recurringEupPriceLine));
        bcmPriceLineInfoList.put(new BCMPriceLineInfoKey("M111", PriceType.ONE_TIME.getValue()), Arrays.asList(oneTimePTPPriceLine));
        bcmPriceLineInfoList.put(new BCMPriceLineInfoKey("M111", PriceType.RECURRING.getValue()), Arrays.asList(recurringPTPPriceLine));
        bcmPriceLineInfoList.put(new BCMPriceLineInfoKey("M111", PriceType.ONE_TIME.getValue()), Arrays.asList(oneTimeCostLine));
        bcmPriceLineInfoList.put(new BCMPriceLineInfoKey("M111", PriceType.RECURRING.getValue()), Arrays.asList(recurringCostLine));

        bcmInstanceCharacteristicsMap.put("TESTCHARACTERISTIC", "testCharacteristicValue");
        bcmInstanceCharacteristicsMap.put("QUANTITY", "1");
    }

    @Test
    public void shouldReturnStringRepresentingSiteIsCentral() {
        when(bcmProductInstanceInfo.isSiteInstallable()).thenReturn(false);
        assertThat(bcmProductInstanceInfo.isBranchOrCentralSite(), is("Central"));
    }

    @Test
    public void shouldReturnStringRepresentingSiteIsBranch() {
        when(bcmProductInstanceInfo.isSiteInstallable()).thenReturn(true);
        assertThat(bcmProductInstanceInfo.isBranchOrCentralSite(), is("Branch"));
    }

    @Test
    public void shouldReturnFalseForSpecialBid() {
        assertThat(bcmProductInstanceInfo.isSpecialBid(), is(false));
    }

    @Test
    public void shouldReturnOneTimeEUPPrice() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimePrice("M111", "EUP"), is(new BigDecimal("28.00")));
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimePrice("M111", 0, "EUP"), is(new BigDecimal("28.00")));
    }

    @Test
    public void shouldReturnOneTimePTPPrice() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimePrice("M111", "PTP"), is(new BigDecimal("10.00")));
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimePrice("M111", 0, "PTP"), is(new BigDecimal("10.00")));
    }

    @Test
    public void shouldReturnMcodeBasedPricingStatus() throws Exception {
        assertThat(bcmProductInstanceInfo.getMcodePriceLineStatus("aPmfId"), is(("User Entered")));
        assertThat(bcmProductInstanceInfo.getMcodePriceLineStatus("aPmfId1"), is("Not Priced"));
        assertThat(bcmProductInstanceInfo.getMcodePriceLineStatus("aPmfId2"), is(""));
    }

    @Test
    public void shouldReturnOneTimeCost() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimeCost("M111"), is(new BigDecimal("21.00")));
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimeCost("M111", 0), is(new BigDecimal("21.00")));
    }

    @Test
    public void shouldReturnBlankOneTimePrice() throws Exception {
        assertThat(bcmProductInstanceInfo.getOneTimePrice("M000", "PTP").toString(), is(""));
        assertThat(bcmProductInstanceInfo.getOneTimePrice("M000", 0, "PTP").toString(), is(""));

        assertThat(bcmProductInstanceInfo.getOneTimePrice("M000", "EUP").toString(), is(""));
        assertThat(bcmProductInstanceInfo.getOneTimePrice("M000", 0, "EUP").toString(), is(""));
    }

    @Test
    public void shouldReturnBlankWhenNoOneTimeCostFound() throws Exception {
        assertThat(bcmProductInstanceInfo.getOneTimeCost("M000").toString(), is(""));
        assertThat(bcmProductInstanceInfo.getOneTimeCost("M000", 0).toString(), is(""));
    }

    @Test
    public void shouldReturnRecurringEUPPrice() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getRecurringPrice("M111", "EUP"), is(new BigDecimal("38.00")));
      assertThat((BigDecimal)bcmProductInstanceInfo.getRecurringPrice("M111", 0, "EUP"), is(new BigDecimal("38.00")));
    }

    @Test
    public void shouldReturnRecurringPTPPrice() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getRecurringPrice("M111", "PTP"), is(new BigDecimal("65.00")));
      assertThat((BigDecimal)bcmProductInstanceInfo.getRecurringPrice("M111", 0, "PTP"), is(new BigDecimal("65.00")));
    }

    @Test
    public void shouldReturnRecurringCost() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getRecurringCost("M111"), is(new BigDecimal("45.00")));
      assertThat((BigDecimal)bcmProductInstanceInfo.getRecurringCost("M111", 0), is(new BigDecimal("45.00")));
    }

    @Test
    public void shouldReturnBlankWhenNoRecurringPriceFound() throws Exception {
        assertThat(bcmProductInstanceInfo.getRecurringPrice("M000", "PTP").toString(), is(""));
        assertThat(bcmProductInstanceInfo.getRecurringPrice("M000", 0, "PTP").toString(), is(""));

        assertThat(bcmProductInstanceInfo.getRecurringPrice("M000", "EUP").toString(), is(""));
        assertThat(bcmProductInstanceInfo.getRecurringPrice("M000", 0, "EUP").toString(), is(""));
    }

    @Test
    public void shouldReturnBlankWhenNoRecurringCostFound() throws Exception {
        assertThat(bcmProductInstanceInfo.getRecurringCost("M000").toString(), is(""));
        assertThat(bcmProductInstanceInfo.getRecurringCost("M000", 0).toString(), is(""));
    }

    @Test
    public void shouldReturnMonthlyDiscount() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getMonthlyDiscount("M111"), is(new BigDecimal("0.05")));
      assertThat((BigDecimal)bcmProductInstanceInfo.getMonthlyDiscount("M111", 0), is(new BigDecimal("0.05")));
    }

    @Test
    public void shouldReturnOneTimeDiscount() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimeDiscount("M111"), is(new BigDecimal("0.10")));
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimeDiscount("M111", 0), is(new BigDecimal("0.10")));
    }

    @Test
    public void shouldReturnBlankStringWhenNoDiscountFound() throws Exception {
      assertThat(bcmProductInstanceInfo.getMonthlyDiscount("M000").toString(), is(""));
      assertThat(bcmProductInstanceInfo.getMonthlyDiscount("M000", 0).toString(), is(""));

      assertThat(bcmProductInstanceInfo.getOneTimeDiscount("M000").toString(), is(""));
      assertThat(bcmProductInstanceInfo.getOneTimeDiscount("M000", 0).toString(), is(""));
    }

    @Test
    public void shouldReturnCharateristicValue() throws Exception {
      assertThat((String) bcmProductInstanceInfo.getValue("testCharacteristic"), is("testCharacteristicValue"));
      assertThat((Integer) bcmProductInstanceInfo.getValue("QUANTITY"), is(1));
    }

    @Test
    public void shouldReturnFirstMatchingPriceLineThatIsOneTimeAndMathcingMCode() throws Exception {
      BCMPriceLineInfo bcmPriceLineInfo = bcmProductInstanceInfo.getPriceLine("M111", "one-time");
      assertThat(bcmPriceLineInfo.getPriceLineName(), is("oneTimePTPPriceLine"));
      assertThat(bcmPriceLineInfo.getTariffType(), is(PriceCategory.PRICE_TO_PARTNER.getLabel()));
    }

    //Tests for Special Bid specific Methods

    @Test
    public void shouldReturnFirstMatchingOneTimePriceRegardlessOfMCode() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimePrice("EUP"), is(new BigDecimal("28.00")));
    }

    @Test
    public void shouldReturnFirstMatchingRecurringPriceRegardlessOfMCode() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getRecurringPrice("EUP"), is(new BigDecimal("38.00")));
    }

    @Test
    public void shouldReturnFirstMatchingOneTimeCost() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getOneTimeCost(), is(new BigDecimal("21.00")));
    }

    @Test
    public void shouldReturnFirstMatchingRecurringCost() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getRecurringCost(), is(new BigDecimal("45.00")));
    }

    @Test
    public void shouldReturnMonthlyDiscountValueForFirstMathcingPriceLineInSpecialBidScenerio() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getSpecialBidMonthlyDiscount("EUP"), is(new BigDecimal("0.05")));
    }

    @Test
    public void shouldReturnOneTimeDiscountForFirstMatchingPriceLineInSpecialBidScenerio() throws Exception {
      assertThat((BigDecimal)bcmProductInstanceInfo.getSpecialBidOneTimeDiscount("EUP"), is(new BigDecimal("0.10")));
    }


}
