package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.model.OneVoiceConfiguration;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMConstants;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.enums.ProductCodes.*;
import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static com.bt.rsqe.security.UserContextBuilder.aDirectUserContext;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@RunWith(JMock.class)
public class QuoteOptionBcmExportBidInfoSheetFactoryTest {
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CURRENCY = "GBP";
    protected static final String CUSTOMER_NAME = "CustomerName";
    protected static final String SALES_REP = "SalesRep";
    protected static final String BID_NUMBER = "BidNumber";
    protected static final String OPPORTUNITY_ID = "opportunityId";
    protected static final String TRADE_LEVEL = "trade level";
    protected static final String SALES_CHANNEL = "SalesChannel";
    protected static final LineItemId ID_1 = new LineItemId("id1");
    protected static final String PTP_VERSION = "ptpVersion";
    protected static final String SHEET_VERSION=BCMConstants.BCM_SHEET_VERSION;
    protected static final String CONTRACT_TERM="contractTerm";
    public static final String TOKEN = "aToken";

    private final Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private QuoteOptionFacade quoteOptionFacade = context.mock(QuoteOptionFacade.class);
    private ExpedioProjectResource expedioProjectResource = context.mock(ExpedioProjectResource.class);
    private CustomerFacade customerFacade = context.mock(CustomerFacade.class);
    private LineItemFacade lineItemFacade = context.mock(LineItemFacade.class);
    private FutureProductInstanceFacade futureProductInstanceFacade = context.mock(FutureProductInstanceFacade.class);
    private FlattenedProductStructure productInstances = context.mock(FlattenedProductStructure.class);

    private QuoteOptionBcmExportBidInfoSheetFactory factory = new QuoteOptionBcmExportBidInfoSheetFactory(quoteOptionFacade, expedioProjectResource, customerFacade, lineItemFacade, futureProductInstanceFacade);

    @Test
    public void shouldGenerateBidInfoRow() throws Exception {
        UserContext userContext = aDirectUserContext().withToken(TOKEN).build();
        UserContextManager.setCurrent(userContext);

        context.checking(new Expectations() {{
            QuoteOptionDTO quoteOptionDTO = new QuoteOptionDTO();
            quoteOptionDTO.currency = CURRENCY;

            allowing(quoteOptionFacade).get(PROJECT_ID, QUOTE_OPTION_ID);
            will(returnValue(quoteOptionDTO));

            allowing(expedioProjectResource).getProject(PROJECT_ID);
            will(returnValue(aProjectDTO()
                                 .withProjectId(PROJECT_ID)
                                 .withSalesRepName(SALES_REP)
                                 .withBidNumber(BID_NUMBER)
                                 .withSiebelId(OPPORTUNITY_ID)
                                 .withTradeLevel(TRADE_LEVEL)
                                 .build()));

            allowing(customerFacade).getByToken(CUSTOMER_ID, TOKEN);
            will(returnValue(new CustomerDTO(CUSTOMER_ID, CUSTOMER_NAME, SALES_CHANNEL)));

            allowing(lineItemFacade).fetchLineItemIds(PROJECT_ID, QUOTE_OPTION_ID, Onevoice.productCode());
            will(returnValue(newArrayList(ID_1, new LineItemId("id2"))));

            allowing(futureProductInstanceFacade).getProductInstances(ID_1);
            will(returnValue(productInstances));

            allowing(productInstances).firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(),
                                                              OneVoiceConfiguration.BasicMPLS.BTPriceLine.CHARGED_PRICEBOOK_VERSION);
            will(returnValue(PTP_VERSION));
        }});

        final List<Map<String, String>> rows = factory.createBidInfoRow(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(rows.size(), is(1));
        assertThat(rows.get(0).get("bid-info.projectId"), is(PROJECT_ID));

        assertThat(rows.get(0).get("bid-info.quoteOptionVersion"), is(QUOTE_OPTION_ID));
        assertThat(rows.get(0).get("bid-info.quoteCurrency"), is(CURRENCY));

        assertThat(rows.get(0).get("bid-info.customerName"), is(CUSTOMER_NAME));
        assertThat(rows.get(0).get("bid-info.opportunityId"), is(OPPORTUNITY_ID));
        assertThat(rows.get(0).get("bid-info.bidNumber"), is(BID_NUMBER));
        assertThat(rows.get(0).get("bid-info.username"), is(SALES_REP));
        assertThat(rows.get(0).get("bid-info.tradeLevel"), is(TRADE_LEVEL));

        assertThat(rows.get(0).get("bid-info.salesChannel"), is(SALES_CHANNEL));
        assertThat(rows.get(0).get("bid-info.priceBookVersion"), is(PTP_VERSION));
    }

    @Test
    public void shouldGenerateBidInfoRowWithEmptyPriceBookVersion() throws Exception {
        UserContext userContext = aDirectUserContext().withToken("aToken").build();
        UserContextManager.setCurrent(userContext);

        context.checking(new Expectations() {{
            QuoteOptionDTO quoteOptionDTO = new QuoteOptionDTO();
            quoteOptionDTO.currency = CURRENCY;

            allowing(quoteOptionFacade).get(PROJECT_ID, QUOTE_OPTION_ID);
            will(returnValue(quoteOptionDTO));

            allowing(expedioProjectResource).getProject(PROJECT_ID);
            will(returnValue(aProjectDTO()
                                 .withProjectId(PROJECT_ID)
                                 .withSalesRepName(SALES_REP)
                                 .withBidNumber(BID_NUMBER)
                                 .withSiebelId(OPPORTUNITY_ID)
                                 .withTradeLevel(TRADE_LEVEL)
                                 .build()));

            allowing(customerFacade).getByToken(CUSTOMER_ID, TOKEN);
            will(returnValue(new CustomerDTO(CUSTOMER_ID, CUSTOMER_NAME, SALES_CHANNEL)));

            allowing(lineItemFacade).fetchLineItemIds(PROJECT_ID, QUOTE_OPTION_ID, Onevoice.productCode());
            will(returnValue(newArrayList()));

            allowing(futureProductInstanceFacade).getProductInstances(ID_1);
            will(returnValue(productInstances));

            allowing(productInstances).firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(),
                                                              OneVoiceConfiguration.BasicMPLS.BTPriceLine.CHARGED_PRICEBOOK_VERSION);
            will(returnValue(PTP_VERSION));
        }});

        final List<Map<String, String>> rows = factory.createBidInfoRow(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(rows.size(), is(1));
        assertThat(rows.get(0).get("bid-info.projectId"), is(PROJECT_ID));

        assertThat(rows.get(0).get("bid-info.quoteOptionVersion"), is(QUOTE_OPTION_ID));
        assertThat(rows.get(0).get("bid-info.quoteCurrency"), is(CURRENCY));

        assertThat(rows.get(0).get("bid-info.customerName"), is(CUSTOMER_NAME));
        assertThat(rows.get(0).get("bid-info.opportunityId"), is(OPPORTUNITY_ID));
        assertThat(rows.get(0).get("bid-info.bidNumber"), is(BID_NUMBER));
        assertThat(rows.get(0).get("bid-info.username"), is(SALES_REP));
        assertThat(rows.get(0).get("bid-info.tradeLevel"), is(TRADE_LEVEL));

        assertThat(rows.get(0).get("bid-info.salesChannel"), is(SALES_CHANNEL));
        assertNull(rows.get(0).get("bid-info.priceBookVersion"));
    }

    @Test
    public void shouldFetchBidInfoRow() throws Exception {
        UserContext userContext = aDirectUserContext().withToken("aToken").build();
        UserContextManager.setCurrent(userContext);

        context.checking(new Expectations() {{
            QuoteOptionDTO quoteOptionDTO = new QuoteOptionDTO();
            quoteOptionDTO.currency = CURRENCY;
            quoteOptionDTO.contractTerm = CONTRACT_TERM;

            allowing(quoteOptionFacade).get(PROJECT_ID, QUOTE_OPTION_ID);
            will(returnValue(quoteOptionDTO));

            allowing(expedioProjectResource).getProject(PROJECT_ID);
            will(returnValue(aProjectDTO()
                                 .withProjectId(PROJECT_ID)
                                 .withSalesRepName(SALES_REP)
                                 .withBidNumber(BID_NUMBER)
                                 .withSiebelId(OPPORTUNITY_ID)
                                 .withTradeLevel(TRADE_LEVEL)
                                 .build()));

            allowing(customerFacade).getByToken(CUSTOMER_ID, TOKEN);
            will(returnValue(new CustomerDTO(CUSTOMER_ID, CUSTOMER_NAME, SALES_CHANNEL)));

            allowing(lineItemFacade).fetchLineItemIds(PROJECT_ID, QUOTE_OPTION_ID, Onevoice.productCode());
            will(returnValue(newArrayList(ID_1, new LineItemId("id2"))));

            allowing(futureProductInstanceFacade).getProductInstances(ID_1);
            will(returnValue(productInstances));

        }});

        final List<Map<String, String>> rows = factory.fetchBidInfoRow(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, "");

        assertThat(rows.size(), is(1));
        assertThat(rows.get(0).get("bid-Info.projectId"), is(PROJECT_ID));

        assertThat(rows.get(0).get("bid-Info.quoteOptionVersion"), is(QUOTE_OPTION_ID));
        assertThat(rows.get(0).get("bid-Info.quoteCurrency"), is(CURRENCY));

        assertThat(rows.get(0).get("bid-Info.customerName"), is(CUSTOMER_NAME));
        assertThat(rows.get(0).get("bid-Info.opportunityId"), is(OPPORTUNITY_ID));
        assertThat(rows.get(0).get("bid-Info.bidNumber"), is(BID_NUMBER));
        assertThat(rows.get(0).get("bid-Info.username"), is(SALES_REP));
        assertThat(rows.get(0).get("bid-Info.tradeLevel"), is(TRADE_LEVEL));

        assertThat(rows.get(0).get("bid-Info.salesChannel"), is(SALES_CHANNEL));
        assertThat(rows.get(0).get("bid-Info.sheetVersion"), is(SHEET_VERSION));
        assertThat(rows.get(0).get("bid-Info.contractTerm"), is(CONTRACT_TERM));
    }


}
