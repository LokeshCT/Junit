package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.dto.ProjectedUsageDTO;
import com.bt.rsqe.customerinventory.fixtures.ProjectedUsageDTOFixture;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.facades.SpecialPriceBookFacade;
import com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.SpecialPriceBookModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.ProjectedUsageModelFactory;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.utils.countries.Country;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.customerinventory.fixtures.PricePointFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static com.bt.rsqe.expedio.fixtures.SiteDTOFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBcmExportUsageSheetFactoryTest {

    private QuoteOptionBcmExportUsageSheetFactory usageSheetFactory;

    private LineItemModel lineItem;

    @Before
    public void setUp() throws Exception {
        usageSheetFactory = new QuoteOptionBcmExportUsageSheetFactory();

        SiteDTO site = aSiteDTO()
            .withBfgSiteId("siteId1")
            .withName("siteName1")
            .withFloor("floor1")
            .withBuilding("building1")
            .withCity("city1")
            .withCountry(Countries.byIsoStatic("KG").getDisplayName())
            .withPostCode("postcode1")
            .build();

        Countries countries = new Countries();
        Country country = countries.byExpedioName(site.country);
        SpecialPriceBook specialPriceBook = new SpecialPriceBook(
            null,
            country,
            null,
            Lists.newArrayList(
                aPricePoint()
                    .withBasePrice(new BigDecimal("3"))
                    .withDestination(Countries.byIsoStatic("BQ"))
                    .withTerminationType(TerminationType.OFF_NET)
                    .withDiscountValue("1")
                    .build())
        );


        SpecialPriceBookFacade specialPriceBookFacade = mock(SpecialPriceBookFacade.class);
        when(specialPriceBookFacade.get(anyString())).thenReturn(new SpecialPriceBookModel(Lists.<SpecialPriceBook>newArrayList(specialPriceBook)));

        ProjectedUsageModelFactory projectedUsageModelFactory = new ProjectedUsageModelFactory(specialPriceBookFacade);


        SiteFacade siteFacade = mock(SiteFacade.class);
        when(siteFacade.get("customerId", "projectId", site.bfgSiteID)).thenReturn(site);

        List<ProjectedUsageDTO> projectedUsageDTOs = Arrays.asList(
            simpleOffNetProjectedUsagePrice(),
            projectedUsagePriceWithNullValues(),
            simpleOnNetProjectedUsagePrice(),
            projectedUsagePriceWithSpecialPriceBook()
        );


        final PriceLineDTOFixture.Builder priceDTOFixture =
            aPriceLineDTO()
                .with(PriceType.ONE_TIME);

        final FutureAssetPricesDTO futureAssetPricesDTO =
            aFutureAssetPricesDTO()
                .withPriceLine(priceDTOFixture)
                .withProjectedUsage(projectedUsageDTOs)
                .withSiteId(site.bfgSiteID)
                .build();

        final FutureAssetPricesModel futureAssetPricesModel =
            aFutureAssetPricesModel()
                .with(projectedUsageModelFactory)
                .with(futureAssetPricesDTO)
                .with(siteFacade)
                .build();

        lineItem = mock(LineItemModel.class);
        when(lineItem.getSite()).thenReturn(site);
        when(lineItem.getFutureAssetPricesModel()).thenReturn(futureAssetPricesModel);
    }

    @Ignore("Kiran/Kaizer - Fix when BCM is made product agnostic")
    @Test
    public void shouldCreateAUsageRowForIndirectUser() throws Exception {
        UserContextManager.setCurrent(new UserContext(new UserPrincipal("bob"), "AnyToken", new PermissionsDTO(true, true, true, true, true, false)));

        final List<LineItemModel> lineItemModels = newArrayList(lineItem);

        List<Map<String, String>> usageRows = usageSheetFactory.createUsageRows(lineItemModels);
        assertThat(usageRows.size(), is(4));
        Map<String, String> usageRow = usageRows.get(0);
        assertCommonUsageRowValues(usageRow);
        assertThat(usageRow.get("ov-usage.termination-type"), is("OFF NET"));
        assertThat(usageRow.get("ov-usage.outgoing-minutes"), is("100000"));
        assertThat(usageRow.get("ov-usage.incoming-offnet-minutes"), is("40000"));
        assertThat(usageRow.get("ov-usage.tariff-type"), is("Direct"));
        assertIndirectOffNetPricing(usageRow);

        assertIndirectWithNullPricing(usageRows.get(1));

        assertIndirectOnNetPricing(usageRows.get(2));
        assertIndirectWithPriceBook(usageRows.get(3));
    }

    @Ignore("Kiran/Kaizer - Fix when BCM is made product agnostic")
    @Test
    public void shouldCreateAUsageRowForDirectUser() throws Exception {
        UserContextManager.setCurrent(new UserContext(new UserPrincipal("bob"), "AnyToken", new PermissionsDTO(true, true, false, true, true, false)));

        final List<LineItemModel> lineItemModels = newArrayList(lineItem);

        List<Map<String, String>> usageRows = usageSheetFactory.createUsageRows(lineItemModels);
        assertThat(usageRows.size(), is(4));
        Map<String, String> usageRow = usageRows.get(0);
        assertCommonUsageRowValues(usageRow);
        assertThat(usageRow.get("ov-usage.termination-type"), is("OFF NET"));
        assertThat(usageRow.get("ov-usage.outgoing-minutes"), is("100000"));
        assertThat(usageRow.get("ov-usage.incoming-offnet-minutes"), is("40000"));
        assertThat(usageRow.get("ov-usage.tariff-type"), is("Direct"));
        assertDirectOffNetPricing(usageRow);


        assertDirectOnNetPricing(usageRows.get(2));
        assertDirectWithPriceBook(usageRows.get(3));
    }

    private ProjectedUsageDTO projectedUsagePriceWithSpecialPriceBook() {
        return new ProjectedUsageDTOFixture()
            .withDestination(Countries.byIsoStatic("BQ"))
            .withTerminationType(TerminationType.OFF_NET).build();
    }

    private ProjectedUsageDTO projectedUsagePriceWithNullValues() {
        return new ProjectedUsageDTOFixture()
            .withOutgoingUnits(null)
            .withBasePrice(null)
            .withBaseRRP(null)
            .withIncomingUnits(null)
            .withTerminationType(TerminationType.OFF_NET).build();
    }

    private ProjectedUsageDTO simpleOnNetProjectedUsagePrice() {
        return new ProjectedUsageDTOFixture().withTerminationType(TerminationType.ON_NET).build();
    }

    private ProjectedUsageDTO simpleOffNetProjectedUsagePrice() {
        return new ProjectedUsageDTOFixture().withTerminationType(TerminationType.OFF_NET).build();
    }

    private void assertDirectOnNetPricing(Map<String, String> usageRow) {
        assertThat(usageRow.get("ov-usage.eup-minute"), is("10.56"));
        assertThat(usageRow.get("ov-usage.eup-total"), is("1478400.00"));
        assertThat(usageRow.get("ov-usage.ptp-minute"), is(""));
        assertThat(usageRow.get("ov-usage.ptp-total"), is(""));
    }

    private void assertDirectOffNetPricing(Map<String, String> usageRow) {
        assertThat(usageRow.get("ov-usage.eup-minute"), is("10.56"));
        assertThat(usageRow.get("ov-usage.eup-total"), is("1056000.00"));
        assertThat(usageRow.get("ov-usage.ptp-minute"), is(""));
        assertThat(usageRow.get("ov-usage.ptp-total"), is(""));
    }

    private void assertCommonUsageRowValues(Map<String, String> usageRow) {
        assertThat(usageRow.get("ov-usage.originating-country"), is(Countries.byIsoStatic("KG").getDisplayName()));
        assertThat(usageRow.get("ov-usage.city"), is("city1"));
        assertThat(usageRow.get("ov-usage.site-id"), is("siteId1"));
        assertThat(usageRow.get("ov-usage.site-name"), is("siteName1"));
        assertThat(usageRow.get("ov-usage.originating-site-access-type"), is("MPLS"));
        assertThat(usageRow.get("ov-usage.terminating-country"), is("United States"));

    }


    private void assertIndirectOnNetPricing(Map<String, String> usageRow) {
        assertThat(usageRow.get("ov-usage.eup-minute"), is("11.56"));
        assertThat(usageRow.get("ov-usage.eup-total"), is("1618400.00"));
        assertThat(usageRow.get("ov-usage.ptp-minute"), is("10.56"));
        assertThat(usageRow.get("ov-usage.ptp-total"), is("1478400.00"));
    }

    private void assertIndirectWithNullPricing(Map<String, String> usageRow) {
        assertThat(usageRow.get("ov-usage.eup-minute"), is("0.00"));
        assertThat(usageRow.get("ov-usage.eup-total"), is("0.00"));
        assertThat(usageRow.get("ov-usage.ptp-minute"), is("0.00"));
        assertThat(usageRow.get("ov-usage.ptp-total"), is("0.00"));
    }

    private void assertIndirectWithPriceBook(Map<String, String> usageRow) {
        assertThat(usageRow.get("ov-usage.eup-minute"), is("11.56"));
        assertThat(usageRow.get("ov-usage.eup-total"), is("1156000.00"));
        assertThat(usageRow.get("ov-usage.ptp-minute"), is("2.00"));
        assertThat(usageRow.get("ov-usage.ptp-total"), is("200000.00"));
    }

    private void assertDirectWithPriceBook(Map<String, String> usageRow) {
        assertThat(usageRow.get("ov-usage.eup-minute"), is("2.00"));
        assertThat(usageRow.get("ov-usage.eup-total"), is("200000.00"));
        assertThat(usageRow.get("ov-usage.ptp-minute"), is(""));
        assertThat(usageRow.get("ov-usage.ptp-total"), is(""));
    }

    private void assertIndirectOffNetPricing(Map<String, String> usageRow) {
        assertThat(usageRow.get("ov-usage.eup-minute"), is("11.56"));
        assertThat(usageRow.get("ov-usage.eup-total"), is("1156000.00"));
        assertThat(usageRow.get("ov-usage.ptp-minute"), is("10.56"));
        assertThat(usageRow.get("ov-usage.ptp-total"), is("1056000.00"));
    }


}
