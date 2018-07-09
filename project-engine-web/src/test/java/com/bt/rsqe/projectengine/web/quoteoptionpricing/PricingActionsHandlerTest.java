package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.customerrecord.UserRole;
import com.bt.rsqe.customerrecord.UsersDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.expedio.services.BidManagerApprovalRequestDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalResponseDTO;
import com.bt.rsqe.fixtures.UserDTOFixture;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.RequestDiscountResponseDTO;
import com.bt.rsqe.projectengine.web.facades.BidManagerCommentsFacade;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.UserFacade;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionRevenueDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.security.UserType;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.bt.rsqe.web.rest.exception.InternalServerErrorException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static com.bt.rsqe.projectengine.LineItemDiscountStatus.*;
import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static java.util.UUID.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

public class PricingActionsHandlerTest {

    private static final String PROJECT_ID = "projectId";
    private static final String PROJECT_NAME = "projectName";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";

    private UserFacade userFacade = mock(UserFacade.class);
    private ProjectResource projects = mock(ProjectResource.class);
    private QuoteOptionResource quoteOptions = mock(QuoteOptionResource.class);
    private QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
    private ExpedioServicesFacade expedioServicesFacade = mock(ExpedioServicesFacade.class);
    private QuoteOptionFacade quoteOptionFacade = mock(QuoteOptionFacade.class);
    private FutureAssetPricesFacade productInstancePricesFacade = mock(FutureAssetPricesFacade.class);
    private FutureAssetPricesModel futureAssetPricesModel = mock(FutureAssetPricesModel.class);

    private PricingActionsHandler handler;
    private ProjectDTO projectDto;
    private com.bt.rsqe.expedio.project.ProjectDTO expedioProjectDto;
    private QuoteOptionRevenueOrchestrator revenueOrchestrator = mock(QuoteOptionRevenueOrchestrator.class);
    private QuoteOptionDTO quoteOptionDTO;
    private BidManagerCommentsFacade bidManagerCommentsFacade;


    @Before
    public void before() {
        bidManagerCommentsFacade = mock(BidManagerCommentsFacade.class);
        handler = new PricingActionsHandler(userFacade, projects, expedioServicesFacade, quoteOptionFacade, productInstancePricesFacade, revenueOrchestrator, bidManagerCommentsFacade);
        projectDto = new ProjectDTO(PROJECT_ID, PROJECT_NAME, CUSTOMER_ID, CONTRACT_ID);
        expedioProjectDto = aProjectDTO().withBidNumber("10").withExpedioRef("ECP100").build();
        UserContext userContext = aDirectUserContext().build();
        UserContextManager.setCurrent(userContext);
        quoteOptionDTO = aQuoteOptionDTO().build();
        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(quoteOptionDTO);
    }

    @Test
    public void shouldFindBidManagers() throws Exception {
        final UsersDTO expectedUsers = new UsersDTO(CUSTOMER_ID, "some-group-email@bt.com",
                                                    new ArrayList() {{
                                                        add(new UserDTO("fore name", "surname", "email@email.com", UserType.DIRECT,"02890232333", "loginname","1234"));
                                                    }});

        when(userFacade.findUsers(CUSTOMER_ID, UserRole.BID_MANAGER)).thenReturn(expectedUsers);

        Response response = handler.findBidManagers(CUSTOMER_ID);
        assertThat(response.getEntity(), is(notNullValue()));
        UsersDTO usersDTO = (UsersDTO) response.getEntity();
        assertThat(usersDTO.customerId, is(CUSTOMER_ID));
        assertThat(usersDTO.users.size(), is(1));
        assertThat(usersDTO.groupEmailId, is("some-group-email@bt.com"));
        assertThat(usersDTO.users.get(0).forename, is("fore name"));
        assertThat(usersDTO.users.get(0).surname, is("surname"));
        assertThat(usersDTO.users.get(0).email, is("email@email.com"));
        assertThat(usersDTO.users.get(0).loginName, is("loginname"));
        assertThat(usersDTO.users.get(0).userType, is(UserType.DIRECT));
        assertThat(usersDTO.users.get(0).phoneNumber, is("02890232333"));    }

    @Test
    public void shouldRequestDiscountApproval() throws Exception {
        final QuoteOptionItemDTO firstQuoteOptionItemDTO = aQuoteOptionItemDTO()
            .withDiscountStatus(LineItemDiscountStatus.NOT_APPLICABLE)
            .build();
        final QuoteOptionItemDTO secondQuoteOptionItemDTO = aQuoteOptionItemDTO()
            .withDiscountStatus(LineItemDiscountStatus.NOT_APPLICABLE)
            .build();
        final QuoteOptionItemDTO readOnlyQuoteOptionItemDTO = aQuoteOptionItemDTO()
            .withStatus(QuoteOptionItemStatus.CUSTOMER_APPROVED)
            .withDiscountStatus(LineItemDiscountStatus.NOT_APPLICABLE)
            .build();

        final String activityId = randomUUID().toString();

        ArgumentCaptor<BidManagerApprovalRequestDTO> discountApprovalDTOArgumentCaptor = ArgumentCaptor.forClass(BidManagerApprovalRequestDTO.class);
        //when(futureAssetPricesModel.getPricingStatusOfTree()).thenReturn(PricingStatus.ICB_BUDGETARY);

        mockRequestDiscount(quoteOptionDTO, activityId, newArrayList(firstQuoteOptionItemDTO, secondQuoteOptionItemDTO, readOnlyQuoteOptionItemDTO));

        final Response response = handler.requestDiscountApproval(CUSTOMER_ID, PROJECT_ID,CONTRACT_ID, QUOTE_OPTION_ID, "", "","aComment","");
        assertThat(response.getStatus(), is(OK.getStatusCode()));

        final RequestDiscountResponseDTO responseDTO = (RequestDiscountResponseDTO) response.getEntity();
        assertThat(responseDTO.status, is("success"));
        assertThat(firstQuoteOptionItemDTO.discountStatus, is(APPROVAL_REQUESTED));
        assertThat(firstQuoteOptionItemDTO.discountStatus, is(APPROVAL_REQUESTED));
        assertThat(readOnlyQuoteOptionItemDTO.discountStatus, is(LineItemDiscountStatus.NOT_APPLICABLE));
        assertThat(quoteOptionDTO.activityId, is(activityId));

        //verify Exp Ref is being sent in the request.
        verify(expedioServicesFacade, atLeastOnce()).requestDiscountApproval(discountApprovalDTOArgumentCaptor.capture());
        verify(bidManagerCommentsFacade).saveCommentsAndCaveats(PROJECT_ID, QUOTE_OPTION_ID, "aComment", "");
        BidManagerApprovalRequestDTO discountApprovalDTO = discountApprovalDTOArgumentCaptor.getValue();

        assertThat(discountApprovalDTO, notNullValue());
        assertThat(discountApprovalDTO.expedioReference, is("ECP100"));
        assertThat(discountApprovalDTO.quoteVersion, is("1.0"));
    }

    @Test
    public void shouldRequestDiscountGivenAnyLineItemPricingStatus() throws Exception {
        final PricingStatus[] pricingStatuses = PricingStatus.values();
        final List<QuoteOptionItemDTO> quoteOptionItemsDtos = newArrayList();
        for (PricingStatus pricingStatus : pricingStatuses) {
             quoteOptionItemsDtos.add(aQuoteOptionItemDTO()
                                          .withDiscountStatus(LineItemDiscountStatus.NEEDS_APPROVAL)
                                          .build());
        }

        //when(futureAssetPricesModel.getPricingStatusOfTree()).thenReturn(pricingStatuses[0], Arrays.copyOfRange(pricingStatuses, 1, pricingStatuses.length));

        mockRequestDiscount(quoteOptionDTO, "", quoteOptionItemsDtos);

        handler.requestDiscountApproval(CUSTOMER_ID, PROJECT_ID,CONTRACT_ID, QUOTE_OPTION_ID, "", "","","");

        for(int i = 0; i < pricingStatuses.length; i++) {
            assertThat(quoteOptionItemsDtos.get(i).discountStatus, is(APPROVAL_REQUESTED));
        }

    }

    @Test
    public void shouldReturnFailureMessageForBadRequest() throws Exception {
        when(projects.get(PROJECT_ID)).thenReturn(projectDto);

        when(expedioServicesFacade.requestDiscountApproval(any(BidManagerApprovalRequestDTO.class))).thenThrow(new BadRequestException());

        when(expedioServicesFacade.getExpedioProject(PROJECT_ID)).thenReturn(expedioProjectDto);

        when(expedioServicesFacade.getUserDetails(anyString())).thenReturn(new UserDTOFixture().withLoginName("someLoginName").build());

        when(quoteOptionFacade.get(PROJECT_ID, "")).thenReturn(quoteOptionDTO);
        final Response response = handler.requestDiscountApproval(CUSTOMER_ID, PROJECT_ID,CONTRACT_ID, "", "", "","","");
        assertThat(response.getStatus(), is(OK.getStatusCode()));

        final RequestDiscountResponseDTO responseDTO = (RequestDiscountResponseDTO) response.getEntity();
        assertThat(responseDTO.status, is("fail"));
    }

    @Test
    public void shouldReturnFailureMessageForInternalServerError() throws Exception {
        when(projects.get(PROJECT_ID)).thenReturn(projectDto);

        when(expedioServicesFacade.requestDiscountApproval(any(BidManagerApprovalRequestDTO.class))).thenThrow(new InternalServerErrorException());

        when(expedioServicesFacade.getExpedioProject(PROJECT_ID)).thenReturn(expedioProjectDto);

        when(expedioServicesFacade.getUserDetails(anyString())).thenReturn(new UserDTOFixture().withLoginName("someLoginName").build());

        when(quoteOptionFacade.get(PROJECT_ID, "")).thenReturn(quoteOptionDTO);
        final Response response = handler.requestDiscountApproval(CUSTOMER_ID, PROJECT_ID,CONTRACT_ID, "", "", "","","");
        assertThat(response.getStatus(), is(OK.getStatusCode()));

        final RequestDiscountResponseDTO responseDTO = (RequestDiscountResponseDTO) response.getEntity();
        assertThat(responseDTO.status, is("fail"));
    }

    @Test
    public void shouldPersistRevenueDetails(){
        String jsonString = "{\"itemDTOs\":" +
                            "[" +
                            "{\"existingRevenue\":\"\",\"proposedRevenue\":\"122\",\"triggerMonths\":\"1\",\"productCategoryName\":\"H001\"}," +
                            "{\"existingRevenue\":\"\",\"proposedRevenue\":\"10000\",\"triggerMonths\":\"1\",\"productCategoryName\":\"H002\"}" +
                            "]}";
         when(projects.get(PROJECT_ID)).thenReturn(projectDto);

        when(expedioServicesFacade.requestDiscountApproval(any(BidManagerApprovalRequestDTO.class))).thenThrow(new InternalServerErrorException());

        when(expedioServicesFacade.getExpedioProject(PROJECT_ID)).thenReturn(expedioProjectDto);

        when(expedioServicesFacade.getUserDetails(anyString())).thenReturn(new UserDTOFixture().withLoginName("someLoginName").build());
        handler.requestDiscountApproval(CUSTOMER_ID, PROJECT_ID, CONTRACT_ID, QUOTE_OPTION_ID, "", "", "", jsonString);
        verify(revenueOrchestrator).persistRevenueDetails(eq(PROJECT_ID),eq(QUOTE_OPTION_ID),eq(CUSTOMER_ID),eq(CONTRACT_ID),any(QuoteOptionRevenueDTO.class));
    }

    private void mockRequestDiscount(final QuoteOptionDTO quoteOptionDTO, String activityId, final List<QuoteOptionItemDTO> quoteOptionItemDTOs) {
        final BidManagerApprovalResponseDTO discountApprovalResponse = new BidManagerApprovalResponseDTO(activityId);

        when(projects.get(PROJECT_ID)).thenReturn(projectDto);
        when(projects.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptions);
        when(quoteOptions.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(quoteOptionItemResource);

        when(quoteOptionItemResource.get()).thenReturn(quoteOptionItemDTOs);

        when(expedioServicesFacade.requestDiscountApproval(any(BidManagerApprovalRequestDTO.class))).thenReturn(discountApprovalResponse);

        when(expedioServicesFacade.getExpedioProject(PROJECT_ID)).thenReturn(expedioProjectDto);

        when(expedioServicesFacade.getUserDetails(anyString())).thenReturn(new UserDTOFixture().withLoginName("someLoginName").build());

        for (QuoteOptionItemDTO quoteOptionItemDTO : quoteOptionItemDTOs) {
            when(productInstancePricesFacade.get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, quoteOptionItemDTO.id, PriceSuppressStrategy.None)).thenReturn(futureAssetPricesModel);
        }


        when(quoteOptions.get(QUOTE_OPTION_ID)).thenReturn(quoteOptionDTO);

    }

}
