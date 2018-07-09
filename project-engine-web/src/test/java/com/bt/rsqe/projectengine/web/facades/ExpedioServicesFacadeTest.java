package com.bt.rsqe.projectengine.web.facades;


import com.bt.rsqe.customerrecord.ExpedioServicesResource;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.fixtures.UserDTOFixture;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.expedio.services.CloseBidManagerActivityDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalRequestDTO;
import com.bt.rsqe.expedio.services.BidManagerApprovalResponseDTO;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;

public class ExpedioServicesFacadeTest {

    private static final String PROJECT_ID = "project id";
    private static final String ACTIVITY_ID = "1234";
    private final Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    ExpedioServicesFacade expedioServicesFacade;
    private ExpedioServicesResource expedioServicesResource;
    private ExpedioProjectResource expedioProjectsResource;
    private UserFacade userFacade;

    @Before
    public void before() {
        expedioServicesResource = context.mock(ExpedioServicesResource.class);
        expedioProjectsResource = context.mock(ExpedioProjectResource.class);
        userFacade = context.mock(UserFacade.class);
        expedioServicesFacade = new ExpedioServicesFacade(expedioServicesResource, expedioProjectsResource, userFacade);
    }

    @Test
    public void shouldSubmitRequestDiscountApproval() throws Exception {
        final BidManagerApprovalRequestDTO discountApprovalRequest = new BidManagerApprovalRequestDTO("", "", "", "", "", "", "", "user@bt.com", null);
        final BidManagerApprovalResponseDTO discountApprovalResponseDto = new BidManagerApprovalResponseDTO(ACTIVITY_ID);
        context.checking(new Expectations() {{
            oneOf(expedioServicesResource).postBidManagerDiscountApprovalRequest(discountApprovalRequest);
            will(returnValue(discountApprovalResponseDto));

        }});
        final BidManagerApprovalResponseDTO responseDTO = expedioServicesFacade.requestDiscountApproval(discountApprovalRequest);
        assertThat(responseDTO, is(notNullValue()));
        assertThat(responseDTO.activityId, is(ACTIVITY_ID));
    }

    @Test
    public void shouldSubmitRequestBidManagerIcbApproval() throws Exception {
        final BidManagerApprovalRequestDTO icbApprovalRequest = new BidManagerApprovalRequestDTO("", "", "", "", "", "", "", "user@bt.com", null);
        final BidManagerApprovalResponseDTO icbApprovalResponseDto = new BidManagerApprovalResponseDTO(ACTIVITY_ID);
        context.checking(new Expectations() {{
            oneOf(expedioServicesResource).postBidManagerIcbApprovalRequest(icbApprovalRequest);
            will(returnValue(icbApprovalResponseDto));

        }});
        final BidManagerApprovalResponseDTO responseDTO = expedioServicesFacade.requestIcbApproval(icbApprovalRequest);
        assertThat(responseDTO, is(notNullValue()));
        assertThat(responseDTO.activityId, is(ACTIVITY_ID));
    }

    @Test
    public void shouldGetExpedioProject() throws Exception {
        final ProjectDTO projectDto = aProjectDTO().withProjectId(PROJECT_ID).build();
        context.checking(new Expectations() {{
            oneOf(expedioProjectsResource).getProject(PROJECT_ID);
            will(returnValue(projectDto));
        }});

        final ProjectDTO expedioProject = expedioServicesFacade.getExpedioProject(PROJECT_ID);
        assertThat(expedioProject, is(notNullValue()));
        assertThat(expedioProject.projectId, is(PROJECT_ID));
    }

    @Test
    public void shouldPutExpedioProject() throws Exception {
        final ProjectDTO projectDto = aProjectDTO().withProjectId(PROJECT_ID).build();
        context.checking(new Expectations() {{
            oneOf(expedioProjectsResource).put(PROJECT_ID, projectDto);
        }});
        expedioServicesFacade.putExpedioProject(PROJECT_ID, projectDto);
    }

    @Test
    public void shouldPostExpedioProject() throws Exception {
        final ProjectDTO projectDto = aProjectDTO().withProjectId(PROJECT_ID).build();
        context.checking(new Expectations() {{
            oneOf(expedioProjectsResource).post(PROJECT_ID, projectDto);
        }});
        expedioServicesFacade.postExpedioProject(PROJECT_ID, projectDto);
    }

    @Test
    public void shouldGetUserDetails() throws Exception {
        //Given
        final String loginName = "someLoginName";
        context.checking(new Expectations() {{
            oneOf(userFacade).findUser(loginName);
            will(returnValue(new UserDTOFixture().withLoginName(loginName).withForeName("someForeName").withSurName("someSurName")
                                                 .withEIN("612345").withEmailId("abc@bt.com").build()));
        }});

        //When
        final UserDTO userDTO = expedioServicesFacade.getUserDetails(loginName);

        //Then
        assertThat(userDTO, is(notNullValue()));
        assertThat(userDTO.loginName, is(loginName));
        assertThat(userDTO.forename, is("someForeName"));
        assertThat(userDTO.surname, is("someSurName"));
        assertThat(userDTO.email, is("abc@bt.com"));
        assertThat(userDTO.ein, is("612345"));
    }

    @Test
    public void shouldCloseRequestDiscountActivity() throws Exception {

        final CloseBidManagerActivityDTO closeRequest = new CloseBidManagerActivityDTO("someReason", ACTIVITY_ID, PROJECT_ID, null);
        context.checking(new Expectations() {{

            oneOf(expedioServicesResource).postBidManagerDiscountApprovalCloseRequest(closeRequest);
        }});

        expedioServicesFacade.closeBidManagerDiscountApprovalRequestActivity(closeRequest);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldCloseRequestBidManagerIcbApprovalActivity() throws Exception {

        final CloseBidManagerActivityDTO closeRequest = new CloseBidManagerActivityDTO("someReason", ACTIVITY_ID, PROJECT_ID, null);
        context.checking(new Expectations() {{

            oneOf(expedioServicesResource).postBidManagerIcbApprovalCloseRequest(closeRequest);
        }});

        expedioServicesFacade.closeBidManagerIcbApprovalRequestActivity(closeRequest);
        context.assertIsSatisfied();
    }
}

