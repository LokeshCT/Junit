package com.bt.rsqe.projectengine.web;


import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilVersion;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductName;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.fixtures.CalendarFixture;
import com.bt.rsqe.integration.PriceLineFixture;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class EndOfLifeValidatorTest {

    private ProductInstanceClient productInstanceClient;
    private Pmr pmr;
    private Pmr.ProductOfferings productOfferings;
    private List<ProductInstance> productInstanceList = new ArrayList<ProductInstance>();
    private ProductInstance asIsProductInstance;
    private EndOfLifeValidator endOfLifeValidator;
    private final static String PRODUCT_CODE = "S00001";
    private final static String PRODUCT_NAME = "PRODUCT NAME";
    private final static String PRODUCT_VERSION = "A.1";
    private final static String SITE_ID = "1";
    public static final String STENCIL_CODE = "S00002";
    public static final String STENCIL_VERSION = "A.2";


    @Before
    public void before() throws Exception {
        pmr = mock(Pmr.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        productOfferings = mock(Pmr.ProductOfferings.class);
        endOfLifeValidator = new EndOfLifeValidator(productInstanceClient, pmr);
    }

    @Test
    public void shouldThrowHardStopMessageDuringEndOfLifeCheckForEffectiveEndDateBeforeSystemDate() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.JUL)
                .year(2013)
                .get()
                .getTime();
        Date systemDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.JUL)
                .year(2014)
                .get()
                .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withEffectiveEndDate(effectiveEndDate);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        Date currentDate = new Date();
        asIsProductInstance = DefaultProductInstanceFixture
                .aProductInstance()
                .withProductIdentifier(new ProductIdentifier(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_VERSION))
                .withProductOffering(offeringFixture)
                .withContractTerm("12")
                .withPriceLines(newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(currentDate).build()))
                .withInitialBillingStartDate(currentDate)
                .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_CODE), new ProductVersion(PRODUCT_VERSION), true)).thenReturn(productInstanceList);
        when(pmr.productOffering(ProductSCode.newInstance(asIsProductInstance.getProductIdentifier().getProductId()))).thenReturn(productOfferings);

        Notification notification = endOfLifeValidator.endOfLifeCheck(SITE_ID, PRODUCT_CODE, PRODUCT_VERSION, systemDate, asIsProductInstance.getLineItemId());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents().get(0).getMessage(), is("Error: The associated CPE bundle is marked as End of Life or will become End of Life during the remaining contract period and needs to be replaced. " +
                "Please return to the Config screens to replace the CPE bundle as part of a new quote"));
    }

    @Test
    public void shouldGetWarningMessageDuringEndOfLifeCheck() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.MAY)
                .year(2015)
                .get()
                .getTime();
        Date systemDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.JUL)
                .year(2014)
                .get()
                .getTime();
        Date billingStartDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.JUL)
                .year(2014)
                .get()
                .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withEffectiveEndDate(effectiveEndDate);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        asIsProductInstance = DefaultProductInstanceFixture
                .aProductInstance()
                .withProductIdentifier(new ProductIdentifier(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_VERSION))
                .withProductOffering(offeringFixture)
                .withContractTerm("12")
                .withPriceLines(newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(billingStartDate).build()))
                .withInitialBillingStartDate(billingStartDate)
                .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_CODE), new ProductVersion(PRODUCT_VERSION), true)).thenReturn(productInstanceList);
        when(pmr.productOffering(ProductSCode.newInstance(asIsProductInstance.getProductIdentifier().getProductId()))).thenReturn(productOfferings);

        Notification notification = endOfLifeValidator.endOfLifeCheck(SITE_ID, PRODUCT_CODE, PRODUCT_VERSION, systemDate, asIsProductInstance.getLineItemId());
        assertThat(notification.getWarningEvents().size(), is(1));
        assertThat(notification.getWarningEvents().get(0).getMessage(), is("Warning: The associated CPE bundle will reach End of Life within the remaining 6 months of the contract end date and it " +
                "is recommended that the CPE bundle is replaced. Please click OK to continue or return to the Config screens to replace the CPE bundle as part of a new quote"));
    }

    @Test
    public void shouldThrowHardWarningMessageForGreaterThanSixMonthDifferenceBetweenEEDandCED() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.MAY)
                .year(2015)
                .get()
                .getTime();
        Date systemDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.JUL)
                .year(2014)
                .get()
                .getTime();
        Date billingStartDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.JUL)
                .year(2014)
                .get()
                .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withEffectiveEndDate(effectiveEndDate);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        asIsProductInstance = DefaultProductInstanceFixture
                .aProductInstance()
                .withProductIdentifier(new ProductIdentifier(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_VERSION))
                .withProductOffering(offeringFixture)
                .withContractTerm("24")
                .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(billingStartDate).build()))
                .withInitialBillingStartDate(billingStartDate)
                .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_CODE), new ProductVersion(PRODUCT_VERSION), true)).thenReturn(productInstanceList);
        when(pmr.productOffering(ProductSCode.newInstance(asIsProductInstance.getProductIdentifier().getProductId()))).thenReturn(productOfferings);

        Notification notification = endOfLifeValidator.endOfLifeCheck(SITE_ID, PRODUCT_CODE, PRODUCT_VERSION, systemDate,asIsProductInstance.getLineItemId());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents().get(0).getMessage(), is("Error: The associated CPE bundle is marked as End of Life or will become End of Life during the remaining contract period and needs to be replaced. " +
                "Please return to the Config screens to replace the CPE bundle as part of a new quote"));
    }

    @Test
    public void shouldNotAllowMoveForStencilProduct() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.JAN)
                .year(2015)
                .get()
                .getTime();
        Date billingStartDate = CalendarFixture.aCalendar()
                .day(10)
                .month(CalendarFixture.Month.FEB)
                .year(2015)
                .get()
                .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withAttribute(ProductOffering.STENCIL_RESERVED_NAME);

        StencilId stencilId = StencilId.versioned(StencilCode.newInstance(STENCIL_CODE),
                                                  StencilVersion.newInstance(STENCIL_VERSION),
                                                  ProductName.newInstance(PRODUCT_NAME));

        ProductOfferingFixture stencilOfferingFixture = ProductOfferingFixture.aStencilableProductOffering()
                .withStencil(stencilId)
                .withEffectiveEndDate(effectiveEndDate);


        when(productOfferings.get()).thenReturn(offeringFixture.build());
        Pmr.ProductOfferings stencilableOfferings = mock(Pmr.ProductOfferings.class);
        when(productOfferings.withStencil(stencilId)).thenReturn(stencilableOfferings);
        when(stencilableOfferings.get()).thenReturn(stencilOfferingFixture.build());
        asIsProductInstance = DefaultProductInstanceFixture
                .aProductInstance()
                .withProductIdentifier(new ProductIdentifier(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_VERSION))
                .withProductOffering(offeringFixture)
                .withContractTerm("12")
                .withStencilId(STENCIL_CODE)
                .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(billingStartDate).build()))
                .withInitialBillingStartDate(billingStartDate)
                .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_CODE), new ProductVersion(PRODUCT_VERSION), true)).thenReturn(productInstanceList);
        when(pmr.productOffering(ProductSCode.newInstance(asIsProductInstance.getProductIdentifier().getProductId()))).thenReturn(productOfferings);

        Notification notification = endOfLifeValidator.endOfLifeCheck(SITE_ID, PRODUCT_CODE, PRODUCT_VERSION, new Date(),asIsProductInstance.getLineItemId());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents().get(0).getMessage(), is("Error: The associated CPE bundle is marked as End of Life or will become End of Life during the remaining contract period and needs to be replaced. " +
                "Please return to the Config screens to replace the CPE bundle as part of a new quote"));
    }
}
