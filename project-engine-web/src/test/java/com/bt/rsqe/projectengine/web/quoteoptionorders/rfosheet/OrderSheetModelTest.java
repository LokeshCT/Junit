package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.fixtures.CalendarFixture;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.google.common.collect.Maps;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture.*;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderSheetModelTest {

    private ProductIdentifierFacade productIdentifierFacade;
    private ProductInstanceClient productInstanceClient;
    private PmrClient pmr;

    @Before
    public void setUp() throws Exception {
        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        productInstanceClient = mock(ProductInstanceClient.class);

        when(productInstanceClient.getAssetDTO(new LineItemId("id"))).thenReturn(AssetDTOFixture.anAsset().build());
        when(productInstanceClient.getAssetDTO(new LineItemId("line-item1"))).thenReturn(AssetDTOFixture.anAsset().build());
        when(productInstanceClient.getAssetDTO(new LineItemId("line-item2"))).thenReturn(AssetDTOFixture.anAsset().build());

        pmr = PmrMocker.getMockedInstance(true);
    }

    @Test
    public void shouldCreateOrderSheetWithSiteAgnosticProducts() {

        final OrderSheetModel orderSheetModel = orderSheetModel(aLineItemModel()
                                                                    .with(productIdentifierFacade)
                                                                    .with(productInstanceClient)
                                                                    .withPmr(pmr)
                                                                    .with(aQuoteOptionItemDTO()
                                                                              .withId("line-item1")
                                                                              .withCustomerRequiredDate(new DateTime(2012, 9, 9, 0, 0, 0, 0)))
                                                                    .thatIsSiteAgnostic()
                                                                    .build(),
                                                                aLineItemModel()
                                                                    .with(productIdentifierFacade)
                                                                    .with(productInstanceClient)
                                                                    .withPmr(pmr)
                                                                    .with(aQuoteOptionItemDTO()
                                                                              .withId("line-item2")
                                                                              .withCustomerRequiredDate(new DateTime(2012, 9, 9, 0, 0, 0, 0)))
                                                                    .forSite(new SiteDTO("12", "blahSite"))
                                                                    .build());
        assertThat(orderSheetModel.rows(), Matchers.hasItems(OrderSheetRowMatcher.anOrderRow().withId("line-item1").withSiteName(SiteDTO.CUSTOMER_OWNED.name)
                                                                                 .withCustomerRequiredDate(DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime("9/9/2012").toDate()),
                                                             OrderSheetRowMatcher.anOrderRow().withId("line-item2").withSiteName("blahSite")
                                                                                 .withCustomerRequiredDate(DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime("9/9/2012").toDate())));
    }

    @Test
    public void shouldIncludeProductNames() {
        final ProductOffering product1 = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier("scode1", "product1", "1.0", "Display1")).build();
        PmrMocker.returnForProduct(pmr, product1);
        final ProductOffering product2 = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier("scode2", "product2", "1.0", "Display2")).build();
        PmrMocker.returnForProduct(pmr, product2);

        when(productIdentifierFacade.getDisplayName(product1)).thenReturn("Display1");
        when(productIdentifierFacade.getDisplayName(product2)).thenReturn("Display2");
        final OrderSheetModel orderSheetModel = orderSheetModel(aLineItemModel().with(productIdentifierFacade)
                                                                    .with(productInstanceClient)
                                                                    .withPmr(pmr)
                                                                    .with(aQuoteOptionItemDTO()
                                                                              .withId("line-item1")
                                                                              .withSCode("scode1")
                                                                              .withCustomerRequiredDate(new DateTime(2012, 8, 9, 0, 0, 0, 0)))
                                                                    .thatIsSiteAgnostic()
                                                                    .build(),
                                                                aLineItemModel().with(productIdentifierFacade)
                                                                    .with(productInstanceClient)
                                                                    .withPmr(pmr)
                                                                    .with(aQuoteOptionItemDTO()
                                                                              .withId("line-item2")
                                                                              .withSCode("scode2")
                                                                              .withCustomerRequiredDate(new DateTime(2012, 9, 9, 0, 0, 0, 0)))
                                                                    .forSite(new SiteDTO("12", "blahSite")).build());

        assertThat(orderSheetModel.rows(), Matchers.hasItems(OrderSheetRowMatcher.anOrderRow().withId("line-item1").withProduct("Display1")
                                                                                 .withCustomerRequiredDate(DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime("9/8/2012").toDate()),
                                                             OrderSheetRowMatcher.anOrderRow().withId("line-item2").withProduct("Display2")
                                                                                 .withCustomerRequiredDate(DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime("9/9/2012").toDate())));


    }

    @Test
    public void shouldIncludeInitialBillingStartDateWhenConvertingLineItemModelToOrderSheetModel() throws Exception {
        Calendar cal = CalendarFixture.aCalendar().day(30).month(CalendarFixture.Month.JAN).year(2014).get();

        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItem")))
            .thenReturn(AssetDTOFixture.anAsset().withInitialBillingStartDate(cal.getTime()).build());

        LineItemModel lineItemModel = aLineItemModel().forSite(new SiteDTO("1", "siteName"))
                                                      .with(productInstanceClient)
                                                      .with(productIdentifierFacade)
                                                      .withPmr(pmr)
                                                      .with(aQuoteOptionItemDTO().withId("aLineItem").withSCode("aProductCode"))
                                                      .build();

        final OrderSheetModel orderSheetModel = orderSheetModel(lineItemModel);

        assertThat(DateUtils.isSameDay(orderSheetModel.rows().get(0).initialBillingStartDate().get(), cal.getTime()), is(true));
    }

    private OrderSheetModel orderSheetModel(LineItemModel... lineItems) {
        return new OrderSheetModel(asList(lineItems), asList(new BillingAccountDTO("1", "A1", "USD"), new BillingAccountDTO("2", "A2", "USD")), new DateTime(),null, Maps.<String, String>newHashMap(), "");
    }
}

