package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bt.rsqe.projectengine.LineItemDiscountStatus.*;
import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;

public class LineItemFacadeTest {

    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String SCODE_1 = "scode1";
    private static final String SCODE_2 = "scode2";
    private static final String OFFER_ID = "Offer1";
    private static final String PRODUCT_CODE ="productCode";
    private static final LineItemId LINE_ITEM_ID_1 = new LineItemId("1");
    private static final LineItemId LINE_ITEM_ID_2 = new LineItemId("2");

    private final QuoteOptionItemResource mockQuoteOptionItemResource = mock(QuoteOptionItemResource.class);
    private final LineItemModelFactory mockLineItemModelFactory = mock(LineItemModelFactory.class);
    private final ProjectResource mockProjectResource = mock(ProjectResource.class);
    private final QuoteOptionResource mockQuoteOptionResource = mock(QuoteOptionResource.class);
    private final PmrClient mockPmrClient = mock(PmrClient.class);
    private final ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
    private final Pmr.ProductOfferings mockProductOfferingsOne = mock(Pmr.ProductOfferings.class);
    private final Pmr.ProductOfferings mockProductOfferingsTwo = mock(Pmr.ProductOfferings.class);

    private final QuoteOptionItemDTO item1 = quoteOptionItem("1", SCODE_1);
    private final QuoteOptionItemDTO item2 = quoteOptionItem("2", SCODE_2);
    private LineItemFacade lineItemFacade;

    @Before
    public void before() {
        lineItemFacade = new LineItemFacade(mockProjectResource, mockLineItemModelFactory);
    }

    @Test
    public void shouldFetchLineItems() throws Exception {
        expectationsForLineItems(item1, item2);

        final List<LineItemModel> result = lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None);
        assertThat(result.size(), is(2));
    }

    @Test
    public void shouldFetchVisibleLineItems() throws Exception {
        //Given
        QuoteOptionItemDTO quoteOptionItemDTO_1 = aQuoteOptionItemDTO().withId("1").withSCode("S123").build();
        QuoteOptionItemDTO quoteOptionItemDTO_2 = aQuoteOptionItemDTO().withId("2").withSCode("S321").build();

        LineItemModel lineItemModel_1 = new LineItemModel("","","","", quoteOptionItemDTO_1,null,null,null,null,null,mockPmrClient,null, productInstanceClient, null, null, null);
        LineItemModel lineItemModel_2 = new LineItemModel("","","","", quoteOptionItemDTO_2,null,null,null,null,null,mockPmrClient,null, productInstanceClient, null, null, null);

        expectationsForLineItems(quoteOptionItemDTO_1, quoteOptionItemDTO_2);

        ProductOffering productOffering_1 = ProductOfferingFixture.aProductOffering().withProductIdentifier("S123").withVisibleInOnlineSummary(true).withIsInFrontCatalogue(true).build();
        ProductOffering productOffering_2 = ProductOfferingFixture.aProductOffering().withProductIdentifier("S321").withVisibleInOnlineSummary(false).build();

        when(mockLineItemModelFactory.create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO_1, PriceSuppressStrategy.None, null)).thenReturn(lineItemModel_1);
        when(mockLineItemModelFactory.create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO_2, PriceSuppressStrategy.None, null)).thenReturn(lineItemModel_2);

        when(mockPmrClient.productOffering(ProductSCode.newInstance("S123"))).thenReturn(mockProductOfferingsOne);
        when(mockPmrClient.productOffering(ProductSCode.newInstance("S321"))).thenReturn(mockProductOfferingsTwo);
        when(mockProductOfferingsOne.get()).thenReturn(productOffering_1);
        when(mockProductOfferingsTwo.get()).thenReturn(productOffering_2);

        //when
        final List<LineItemModel> visibleLineItems = lineItemFacade.fetchVisibleLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null, true, PriceSuppressStrategy.None);

        //Then
        assertThat(visibleLineItems.size(), is(1));
        assertThat(visibleLineItems, hasItem(lineItemModel_1));
    }

    @Test
    public void shouldFetchLineItemIds() throws Exception {
        expectationsForLineItems(item1, item2);
        final List<LineItemId> ids = lineItemFacade.fetchLineItemIds(PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(ids.size(), is(2));
        assertThat(ids.get(0).value(), is(item1.id));
        assertThat(ids.get(1).value(), is(item2.id));
    }

    @Test
    public void shouldFetchAllLineItemIdsByProductCode() throws Exception {
        expectationsForLineItems(item1, item1, item2, item2);
        final List<LineItemId> ids = lineItemFacade.fetchLineItemIds(PROJECT_ID, QUOTE_OPTION_ID, SCODE_1);
        assertThat(ids.size(), is(2));
        assertThat(ids.get(0).value(), is(item1.id));
        assertThat(ids.get(1).value(), is(item1.id));
    }

    @Test
    public void shouldApproveDiscounts() throws Exception {

        when(mockProjectResource.quoteOptionResource(PROJECT_ID)).thenReturn(mockQuoteOptionResource);
        when(mockQuoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(mockQuoteOptionItemResource);
        when(mockQuoteOptionItemResource.get(LINE_ITEM_ID_1.toString())).thenReturn(item1);
        when(mockQuoteOptionItemResource.get(LINE_ITEM_ID_2.toString())).thenReturn(item2);

        lineItemFacade.approveDiscounts(PROJECT_ID,
                                        QUOTE_OPTION_ID,
                                        new ArrayList<LineItemId>() {{
                                            add(LINE_ITEM_ID_1);
                                            add(LINE_ITEM_ID_2);
                                        }});

        assertThat(item1.discountStatus, is(APPROVED));
        assertThat(item2.discountStatus, is(APPROVED));

    }

    @Test
    public void shouldRejectDiscounts() throws Exception {

        item1.discountStatus = APPROVAL_REQUESTED;
        item2.discountStatus = APPROVAL_REQUESTED;

        when(mockProjectResource.quoteOptionResource(PROJECT_ID)).thenReturn(mockQuoteOptionResource);
        when(mockQuoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(mockQuoteOptionItemResource);
        when(mockQuoteOptionItemResource.get(LINE_ITEM_ID_1.toString())).thenReturn(item1);
        when(mockQuoteOptionItemResource.get(LINE_ITEM_ID_2.toString())).thenReturn(item2);

        lineItemFacade.rejectDiscounts(PROJECT_ID,
                                       QUOTE_OPTION_ID,
                                       new ArrayList<LineItemId>() {{
                                           add(LINE_ITEM_ID_1);
                                           add(LINE_ITEM_ID_2);
                                       }});

        assertThat(item1.discountStatus, is(REJECTED));
        assertThat(item2.discountStatus, is(REJECTED));

    }

    @Test
    public void shouldReturnFailedLineItems() throws Exception {
        QuoteOptionDTO quoteOptionDTO = QuoteOptionDTOFixture
            .aQuoteOptionDTO()
            .withName("quoteOptionName")
            .withCreationDate("2014-01-01T08:00:00.500+01:00")
            .withCurrency("USD")
            .withContractTerm("12")
            .withCreatedBy("forename surname")
            .build();
        when(mockQuoteOptionResource.get(QUOTE_OPTION_ID)).thenReturn(quoteOptionDTO);
        QuoteOptionItemDTO quoteOptionItemDTO_1 = aQuoteOptionItemDTO().withId("1").withSCode("S123").build();
        QuoteOptionItemDTO quoteOptionItemDTO_2 = aQuoteOptionItemDTO().withId("2").withSCode("S321").build();
        LineItemModel lineItemModel_1 = new LineItemModel("","","","", quoteOptionItemDTO_1,null,null,null,null,null,mockPmrClient,null, productInstanceClient, quoteOptionDTO, null, null);
        LineItemModel lineItemModel_2 = new LineItemModel("","","","", quoteOptionItemDTO_2,null,null,null,null,null,mockPmrClient,null, productInstanceClient, quoteOptionDTO, null, null);
        expectationsForLineItems(quoteOptionItemDTO_1, quoteOptionItemDTO_2);

        when(mockLineItemModelFactory.create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO_1, PriceSuppressStrategy.None, quoteOptionDTO)).thenReturn(lineItemModel_1);
        when(mockLineItemModelFactory.create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO_2, PriceSuppressStrategy.None, quoteOptionDTO)).thenReturn(lineItemModel_2);

        item1.status = QuoteOptionItemStatus.FAILED;
        expectationsForLineItems(item1, item2);

        final List<LineItemModel> lineItemModels = lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null, true, PriceSuppressStrategy.None);
        assertThat(lineItemModels.size(), is(2));
        assertThat(lineItemModels.get(0).getQuoteOptionDTO().getCurrency(), is("USD"));
    }

    @Test
    public void shouldNotReturnFailedLineItems() throws Exception {
        item1.status = QuoteOptionItemStatus.FAILED;
        expectationsForLineItems(item1, item2);
        final List<LineItemModel> lineItemModels = lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null, false, PriceSuppressStrategy.None);
        assertThat(lineItemModels.size(), is(1));
    }

    @Test
    public void shouldReturnEmptyLineItemList() throws Exception {
        item1.status = QuoteOptionItemStatus.FAILED;
        expectationsForLineItems(item1, item2);
        final List<LineItemModel> lineItemModels = lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PRODUCT_CODE, true, PriceSuppressStrategy.None);
        assertThat(lineItemModels.size(), is(0));
    }

    @Test
    public void shouldPersistCommitmentValue(){
        expectationsForLineItems(item1);
        lineItemFacade.persistMinimumRevenueCommitment(PROJECT_ID, QUOTE_OPTION_ID, new LineItemId("1"), "1000", null);
        verify(mockQuoteOptionItemResource).put(item1);
    }

    @Test
    public void shouldNotPersistCommitmentValue(){
        expectationsForLineItems(item1);
        lineItemFacade.persistMinimumRevenueCommitment(PROJECT_ID,QUOTE_OPTION_ID,new LineItemId("1"),null, null);
        verify(mockQuoteOptionItemResource,never()).put(item1);
    }

    private QuoteOptionItemDTO quoteOptionItem(String lineItemId, String productCode) {
        PriceBookDTO priceBook = new PriceBookDTO("bookId","req1","eup","ptp","10","3") ;
        return aQuoteOptionItemDTO().withId(lineItemId)
            .withStatus(QuoteOptionItemStatus.CUSTOMER_APPROVED)
            .withSCode(productCode)
            .withContract(new ContractDTO("contractId","60",newArrayList(priceBook)))
            .withContractTerm("12")
            .withOfferId(OFFER_ID).build();
    }

    private void expectationsForLineItems(final QuoteOptionItemDTO... quoteOptionItemDTOs) {
        final List<QuoteOptionItemDTO> dtos = Arrays.asList(quoteOptionItemDTOs);

        when(mockProjectResource.quoteOptionResource(PROJECT_ID)).thenReturn(mockQuoteOptionResource);
        when(mockQuoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(mockQuoteOptionItemResource);
        when(mockQuoteOptionItemResource.get()).thenReturn(dtos);
        when(mockQuoteOptionItemResource.get("1")).thenReturn(item1);
    }

}
