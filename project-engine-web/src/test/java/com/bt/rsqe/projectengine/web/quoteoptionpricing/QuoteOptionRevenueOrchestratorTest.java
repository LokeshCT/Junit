package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.PriceBookFacade;
import com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionRevenueDTO;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class QuoteOptionRevenueOrchestratorTest {

      QuoteOptionRevenueOrchestrator orchestrator;
    @Mock
    private LineItemFacade lineItemFacade;
    @Mock
    private PriceBookFacade pricebookFacade;
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    @Mock
    private Pagination pagination;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        orchestrator = new QuoteOptionRevenueOrchestrator(lineItemFacade,pricebookFacade);
    }

    @Test
    public void shouldGetRevenueList(){
        List<LineItemModel> lineItemModel = createLineItemModel();
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID,PriceSuppressStrategy.None)).thenReturn(lineItemModel);
        PriceBookDTO priceBookDTO = new PriceBookDTO("id","req","eup","ptp","60000","2");
        when(pricebookFacade.getLatestPriceBookForIndirectUser(CUSTOMER_ID,"S001", ProductCategoryCode.NIL)).thenReturn(priceBookDTO);
        when(pricebookFacade.getLatestPriceBookForIndirectUser(CUSTOMER_ID,"S002", ProductCategoryCode.NIL)).thenReturn(priceBookDTO);
        when(pagination.paginate(anyListOf(QuoteOptionRevenueDTO.ItemRowDTO.class))).thenReturn(Lists.<QuoteOptionRevenueDTO.ItemRowDTO>newArrayList());
        orchestrator.getRevenueFor(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, pagination);
        verify(pricebookFacade).getLatestPriceBookForIndirectUser(CUSTOMER_ID,"S001", ProductCategoryCode.NIL);
        verify(pricebookFacade).getLatestPriceBookForIndirectUser(CUSTOMER_ID, "S002", ProductCategoryCode.NIL);

    }

    @Test
    public void shouldSaveRevenueDetails(){
        QuoteOptionRevenueDTO dto = createDTO();
        List<LineItemModel> lineItemModel = createLineItemModel();
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID,CONTRACT_ID,PROJECT_ID,QUOTE_OPTION_ID,PriceSuppressStrategy.None)).thenReturn(lineItemModel);
        LineItemId lineItemId = new LineItemId("LineItemId");
        orchestrator.persistRevenueDetails(PROJECT_ID,QUOTE_OPTION_ID,CUSTOMER_ID,CONTRACT_ID,dto);
        verify(lineItemFacade).persistMinimumRevenueCommitment(PROJECT_ID,QUOTE_OPTION_ID,lineItemId,"200","1");
        verify(lineItemFacade).persistMinimumRevenueCommitment(PROJECT_ID,QUOTE_OPTION_ID,lineItemId,"10000","5");
    }

    private QuoteOptionRevenueDTO createDTO() {
        QuoteOptionRevenueDTO.ItemRowDTO dto1 = new QuoteOptionRevenueDTO.ItemRowDTO("id1", "", "200", "1","H001");
        QuoteOptionRevenueDTO.ItemRowDTO dto2 = new QuoteOptionRevenueDTO.ItemRowDTO("id2", "", "10000", "5","H002");
        return new QuoteOptionRevenueDTO(newArrayList(dto1,dto2));
    }

    private List<LineItemModel> createLineItemModel() {
        SiteDTO siteOneDTO = new SiteDTO("5431", "Ipswich");
        LineItemModel itemModel1 = LineItemModelFixture.aLineItemModel().withContractId(CONTRACT_ID).withCustomerId(CUSTOMER_ID)
                                                 .with(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("LineItemId")
                                                                                .withContract(createContract("1000", "6")).withSCode("S001"))
            .forSite(siteOneDTO)
                                                 .forProductCategory("H001").build();
        LineItemModel itemModel2 = LineItemModelFixture.aLineItemModel().forProductCategory("H002").withContractId(CONTRACT_ID).withCustomerId(CUSTOMER_ID)
                                                 .with(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("LineItemId")
                                                                                .withContract(createContract("200", "3")).withSCode("S002"))
                                                 .build();
        return newArrayList(itemModel1,itemModel2);
    }

    private ContractDTO createContract(String revenue, String triggerMonths) {
        PriceBookDTO priceBookDTO = new PriceBookDTO("id","req","eup","ptp",revenue,triggerMonths);
        return new ContractDTO("id", "60", newArrayList(priceBookDTO));
    }
}
