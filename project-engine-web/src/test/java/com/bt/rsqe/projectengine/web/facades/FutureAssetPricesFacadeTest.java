package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerinventory.client.resource.tobe.OptionBasedFutureAssetResourceClient;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManager;
import com.bt.rsqe.customerinventory.driver.FutureAssetPriceReportDriver;
import com.bt.rsqe.customerinventory.driver.OptionBasedFutureAssetDriver;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPriceReportDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.FutureAssetPricesModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class FutureAssetPricesFacadeTest {

    private static final String LINE_ITEM_ID = "lineItemId";
    private static final String LINE_ITEM_ID_1 = "lineItemId1";
    private static final String LINE_ITEM_ID_2 = "lineItemId2";
    private static final String CUSTOMER_ID = "customerId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";

    private JUnit4Mockery context;
    private OptionBasedFutureAssetDriver optionBasedFutureAssetDriver;
    private CustomerInventoryDriverManager customerInventoryRestfulDriverManager;
    private FutureAssetPricesModelFactory futureAssetPricesModelFactory;
    private FutureAssetPricesFacade productInstancePricesFacade;
    private FutureAssetPriceReportDriver futureAssetPriceReportDriver;
    private OptionBasedFutureAssetResourceClient optionBasedFutureAssetResourceClient;

    @Before
    public void before() {
        context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        customerInventoryRestfulDriverManager = context.mock(CustomerInventoryDriverManager.class);
        optionBasedFutureAssetDriver = context.mock(OptionBasedFutureAssetDriver.class);
        futureAssetPricesModelFactory = context.mock(FutureAssetPricesModelFactory.class);
        futureAssetPriceReportDriver = context.mock(FutureAssetPriceReportDriver.class);
        optionBasedFutureAssetResourceClient = context.mock(OptionBasedFutureAssetResourceClient.class);
        productInstancePricesFacade = new FutureAssetPricesFacade(customerInventoryRestfulDriverManager, futureAssetPricesModelFactory);
    }

    @Test
    public void shouldGetIndividualProductInstancePricesModel() throws Exception {
        final FutureAssetPricesModel futureAssetPricesModel = aFutureAssetPricesModel().build();
        context.checking(new Expectations() {{
            oneOf(customerInventoryRestfulDriverManager).getOptionBasedFutureAssetDriver(new LineItemId("lineItemId"));
            will(returnValue(optionBasedFutureAssetDriver));
            oneOf(optionBasedFutureAssetDriver).get();
            final AssetDTO returnedDto = new AssetDTO();
            will(returnValue(returnedDto));
            oneOf(futureAssetPricesModelFactory).create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, returnedDto, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        final FutureAssetPricesModel result = productInstancePricesFacade.get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID, PriceSuppressStrategy.None);
        assertThat(result, is(futureAssetPricesModel));
    }

    @Test
    public void shouldGetMultipleProductInstancePricesModel() throws Exception {

        final ArrayList<LineItemId> lineItemIds = new ArrayList<LineItemId>() {{
            add(new LineItemId(LINE_ITEM_ID_1));
            add(new LineItemId(LINE_ITEM_ID_2));
        }};

        context.checking(new Expectations() {{
            oneOf(customerInventoryRestfulDriverManager).getFutureAssetPriceReportDriver();
            will(returnValue(futureAssetPriceReportDriver));

            oneOf(futureAssetPriceReportDriver).post(lineItemIds);
            final FutureAssetPriceReportDTO report = buildReportFor(lineItemIds);
            will(returnValue(report));
            oneOf(futureAssetPricesModelFactory).create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, report.getPriceList().get(0));
            oneOf(futureAssetPricesModelFactory).create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, report.getPriceList().get(1));
        }});

        final List<FutureAssetPricesModel> priceModels = productInstancePricesFacade.getForLineItems(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, lineItemIds);
        assertThat(priceModels.size(), is(2));
    }

    @Test
    public void shouldUpdatePricingStatusOfLineItem() throws Exception {
        final AssetDTO asset = AssetDTOFixture.anAsset().withLineItemId("aLineItemId").withId("anAssetId").build();

        context.checking(new Expectations() {{
            oneOf(customerInventoryRestfulDriverManager).getOptionBasedFutureAssetResourceClient();
            will(returnValue(optionBasedFutureAssetResourceClient));

            oneOf(optionBasedFutureAssetResourceClient).getByOption("aLineItemId");
            will(returnValue(asset));

            oneOf(optionBasedFutureAssetResourceClient).put("aLineItemId", "anAssetId", asset);
        }});

        final FutureAssetPricesModel model = FutureAssetPricesModelFixture.aFutureAssetPricesModel().with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO().withLineItemId("aLineItemId")).build();
        productInstancePricesFacade.updatePricingStatus(model, PricingStatus.REPRICING);

        assertThat(asset.getPricingStatus(), is(PricingStatus.REPRICING));
    }

    private FutureAssetPriceReportDTO buildReportFor(ArrayList<LineItemId> lineItemIds) {
        List<FutureAssetPricesDTO> prices = new ArrayList<FutureAssetPricesDTO>();
        for (LineItemId lineItemId : lineItemIds) {
            final FutureAssetPricesDTO futureAssetPrices = aFutureAssetPricesDTO()
                .withLineItemId(lineItemId.value())
                .withPriceLine(aPriceLineDTO().withId(lineItemId.value() + "_priceLine1"))
                .withPriceLine(aPriceLineDTO().withId(lineItemId.value() + "_priceLine2")).build();
            prices.add(futureAssetPrices);
        }
        return new FutureAssetPriceReportDTO(prices);
    }
}
