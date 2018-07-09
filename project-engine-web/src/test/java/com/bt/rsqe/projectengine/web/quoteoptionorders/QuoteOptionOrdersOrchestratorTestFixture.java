package com.bt.rsqe.projectengine.web.quoteoptionorders;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.OfferStatus;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.projectengine.IfcAction;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemIcbApprovalStatus;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderStatus;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.RfoValidDTO;
import com.bt.rsqe.projectengine.web.InVisibleCreatableLineItemRetriever;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOfferFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.projectengine.web.model.modelfactory.OrderModelFactory;
import com.bt.rsqe.projectengine.web.resourcestubs.ProjectResourceStub;
import com.bt.rsqe.projectengine.web.resourcestubs.QuoteOptionResourceStub;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.Mockery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static java.util.Arrays.*;

public class QuoteOptionOrdersOrchestratorTestFixture {
    public static final String CUSTOMER_ID = "";
    public static final String CONTRACT_ID = "";
    public static final String PROJECT_ID = "";
    public static final String QUOTE_OPTION_ID = "";
    public static final String OFFER_ID = "OfferId";
    public static final String QUOTE_OPTION_ITEM_ID = "QuoteOptionItemId";
    public static final String ITEM_SCODE = "sCode1";
    public static final String OFFER_NAME = "OfferName";
    public static final String ORDER_ID = "orderId";
    public static final String USER_TOKEN = "DIRECT_USER";

    private final Mockery context = new RSQEMockery();

    private final ProjectResourceStub projects = new ProjectResourceStub();
    private QuoteOptionOrderFacade quoteOptionOrderFacade = new QuoteOptionOrderFacade(projects, new OrderModelFactory(null));
    private final QuoteOptionOfferFacade quoteOptionOfferFacade = context.mock(QuoteOptionOfferFacade.class);
    private ProjectResource projectResource;
    private QuoteOptionResourceStub resourceStub;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private ProductInstanceClient productInstanceClient;
    private InVisibleCreatableLineItemRetriever inVisibleCreatableLineItemRetriever;
    private Pmr pmr = context.mock(Pmr.class);
    private LineItemFacade lineItemFacade = context.mock(LineItemFacade.class);
    private com.bt.rsqe.customerrecord.CustomerResource customerResource;
    private ApplicationPropertyResourceClient applicationPropertyResourceClient;

    public QuoteOptionOrdersOrchestrator build() {
        return new QuoteOptionOrdersOrchestrator(quoteOptionOrderFacade, quoteOptionOfferFacade, projectResource, migrationDetailsProvider, productInstanceClient, inVisibleCreatableLineItemRetriever, pmr, lineItemFacade,customerResource,applicationPropertyResourceClient);
    }

    public QuoteOptionOrdersOrchestratorTestFixture withOrderFacade(QuoteOptionOrderFacade orderFacade) {
        this.quoteOptionOrderFacade = orderFacade;
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withProjectResource(ProjectResource projectResource) {
        this.projectResource = projectResource;
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withProductInstanceClient(ProductInstanceClient productInstanceClient) {
        this.productInstanceClient = productInstanceClient;
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withQuoteMigrationDetailsProvider(QuoteMigrationDetailsProvider migrationDetailsProvider) {
        this.migrationDetailsProvider = migrationDetailsProvider;
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withPMR(Pmr pmr) {
        this.pmr = pmr;
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withinVisibleCreatableLineItemResolver(InVisibleCreatableLineItemRetriever inVisibleCreatableLineItemRetriever) {
        this.inVisibleCreatableLineItemRetriever = inVisibleCreatableLineItemRetriever;
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withOneOrder() {
        createOrder("OrderId", "OrderName", "2011-01-01T12:30:55.000+00:00", OrderStatus.CREATED.getValue());
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withRfoValidDto(boolean isRfoValid) {
        createRfoValidDto("OrderId", isRfoValid);
        return this;
    }

    private QuoteOptionItemDTO createOffer(String orderId, final String creationDate) {
        final QuoteOptionItemDTO item = new QuoteOptionItemDTO(QUOTE_OPTION_ITEM_ID, ITEM_SCODE, "name1", null, OFFER_ID, null, "", QuoteOptionItemStatus.ORDER_CREATED, LineItemDiscountStatus.NOT_APPLICABLE,
                                                               LineItemIcbApprovalStatus.NOT_APPLICABLE, orderId,
                                                               new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.PENDING), LineItemOrderStatus.NOT_APPLICABLE, IfcAction.NOT_APPLICABLE, null, null,
                                                               new ContractDTO(UUID.randomUUID().toString(), "",asList(new PriceBookDTO(UUID.randomUUID().toString(), "someRequestId", "eup", "ptp", null, null))), false, false, false, null, null, true, new ProductCategoryCode("H123"), null, false);

        resourceStub = projects.with(new ProjectDTO("", "", "", ""))
                               .quoteOptionResource(QUOTE_OPTION_ID).with(QuoteOptionDTO.newInstance("", "", "", "24", ""));

        resourceStub.quoteOptionItemResource(QUOTE_OPTION_ID).with(item);

        context.checking(new Expectations(){{
            allowing(quoteOptionOfferFacade).getOffer(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
            will(returnValue(createAnOffer(creationDate, item)));
            ignoring(quoteOptionOfferFacade);
        }});

        return item;
    }

    private void createOrder(String orderId, String orderName, String creationDate, String status) {
        final QuoteOptionItemDTO item = createOffer(orderId, creationDate);
        resourceStub.quoteOptionOrderResource(QUOTE_OPTION_ID).with(createAnOrder(orderId, orderName, creationDate, status, item));
    }

    private void createRfoValidDto(String orderId, boolean isRfoValid) {
        resourceStub.quoteOptionOrderResource(QUOTE_OPTION_ID).with(orderId, createAnRfoValidDto(isRfoValid));
    }

    private OrderDTO createAnOrder(String orderId, String orderName, String creationDate, String status, QuoteOptionItemDTO item) {
        List<QuoteOptionItemDTO> orderItems = new ArrayList<QuoteOptionItemDTO>();
        orderItems.add(item);
        return OrderDTO.newInstance(orderId, orderName, creationDate, status, orderItems);
    }

    private RfoValidDTO createAnRfoValidDto(boolean isRfoValid) {
        RfoValidDTO rfoValidDTO = new RfoValidDTO();
        rfoValidDTO.setValue(isRfoValid);
        return rfoValidDTO;
    }

    private OfferDTO createAnOffer(String creationDate, QuoteOptionItemDTO item) {
        List<QuoteOptionItemDTO> offerItems = new ArrayList<QuoteOptionItemDTO>();
        offerItems.add(aQuoteOptionItemDTO().withId(item.id).withSCode(item.sCode).withStatus(item.status).withDiscountStatus(item.discountStatus).withContractTerm(item.contractTerm).build());
        return OfferDTO.create(OFFER_ID, OFFER_NAME, creationDate, OfferStatus.ACTIVE.toString(), offerItems,OFFER_NAME+"-COR");
    }

    public OrderDTO getSingleOrder() {
        return projects.quoteOptionResource("").quoteOptionOrderResource("").storedOrderList().get(0);
    }

    public QuoteOptionOrdersOrchestratorTestFixture withOrder(String id, String name, String created, String status) {
        createOrder(id, name, created, status);
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withRfoValidDto(String id, boolean isRfoValid) {
        createRfoValidDto(id, isRfoValid);
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withQuoteOption() {
        createOffer(null, "2011-01-01T12:30:55.000+00:00");
        return this;
    }

    public QuoteOptionOrdersOrchestratorTestFixture withLineItemFacade(LineItemFacade lineItemFacade) {
        this.lineItemFacade = lineItemFacade;
        return this;
    }
}
