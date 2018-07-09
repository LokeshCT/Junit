package com.bt.rsqe.ape;

import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.config.LocalIdentifier;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.ape.dto.ManualQuote;
import com.bt.rsqe.ape.dto.ManualQuoteItem;
import com.bt.rsqe.ape.dto.QrefRecallDTO;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.AccessUserCommentsEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.AccessUserCommentsDTO;
import com.bt.rsqe.domain.bom.fixtures.BfgContactFixture;
import com.bt.rsqe.domain.bom.parameters.BfgContact;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.security.ExpedioUserContextResolver;
import com.bt.rsqe.security.RestAuthenticationFilterConfig;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.security.UserPrincipal;
import com.bt.rsqe.security.UserType;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.bt.rsqe.web.rest.exception.InternalServerErrorException;
import com.bt.rsqe.web.rest.exception.PreconditionFailedException;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import org.glassfish.jersey.server.ContainerRequest;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.sql.Date;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class APEInteractionsHandlerTest {
    private static final String COMMENTS = "here are my comments";
    private static final String COMMENTS2 = "here are some more comments";
    private static final String QREF_ID = "a qref";
    private static final String QREF_ID2 = "a second qref";
    private static final String QREF_ID3 = "a third qref";
    private static final String QREF_STENCIL = QREF_ID + "Stencil";
    private static final String QREF_STENCIL2 = QREF_ID2 + "Stencil";
    private static final String QREF_STENCIL3 = QREF_ID3 + "Stencil";
    private static final String QREF_STENCIL4 = "ACCESS_a_second_qref";
    private static final String QREF_STENCIL5 = "ACCESS_a_third_qref";
    private static final String ERROR_MSG = "Something went wrong";
    private static final String PAIR_ID = "aPairId";
    private static final String WORKFLOW_STATUS_NAME = "Workflow Status";
    private static final String SENT_TO_WORKFLOW_STATUS = String.valueOf(AccessWorkflowStatus.SENT_TO_WORKFLOW.getStatus());
    private static final String COMPLETED_WORKFLOW_STATUS = String.valueOf(AccessWorkflowStatus.COMPLETED.getStatus());
    private static final String REQUEST_ID = "requestId";
    private static final String UNIQUE_ID = "uniqueId";
    private static final String PSTN_TEL_LINE = "123456";
    private static final String SITE_TEL_NUMBER = "654321";
    private static final String USER_LOGIN = "aSalesUser";
    private static final String QUOTE_CURRENCY = "GBP";
    private static final String ACCESS_USER_QREF_ID = "8548C32C-893F-4BF4-97B4-F2CF6667B5ED";

    private APEClient apeClient;
    private ApplicationConfig applicationConfig;
    private RestResource apeCommentsResource, apeManualQuoteResource, apeSaveCommentsResource, apeLoadCommentsResource, apeQrefRecallResource, apeSaveManualQuoteResource;
    private QrefPairCommentsInput[] commentsInputs;
    private APEQrefRepository apeQrefRepository;
    private ExpedioUserContextResolver expedioUserContextResolver;
    private UserResource expedioUserResource;
    private UserContext userContext;
    private CustomerResource customerResource;    private ApeQref apeQref, apeQref2, apeQref3;
    private ManualQuoteItem manualQuoteItem;
    Date currentDate = new java.sql.Date(System.currentTimeMillis());

    @Before
    public void before() throws Exception {
        commentsInputs = new QrefPairCommentsInput[1];

        QrefPairCommentsInput commentsInput = new QrefPairCommentsInput();
        commentsInput.setUserComments(COMMENTS);
        commentsInput.setQref(QREF_ID);

        commentsInputs[0] = commentsInput;

        apeClient = mock(APEClient.class);
        apeQrefRepository = mock(APEQrefRepository.class);

        expedioUserContextResolver = mock(ExpedioUserContextResolver.class);
        expedioUserResource = mock(UserResource.class);
        userContext = mock(UserContext.class);
        customerResource = mock(CustomerResource.class);

        manualQuoteItem = mock(ManualQuoteItem.class);
        when(userContext.getLoginName()).thenReturn("aLoginName");
        when(expedioUserContextResolver.resolve(any(String.class), any(String.class))).thenReturn(userContext);
        when(expedioUserResource.findUser(any(String.class))).thenReturn(new UserDTO("fName", "sName", "email", UserType.DIRECT, "123456", "loginName", "654321"));
        when(customerResource.getByToken(any(String.class), any(String.class))).thenReturn(new CustomerDTO("1", "aName", "salesChannel"));

        applicationConfig = StubApplicationConfig.defaultTestConfig();
        Application application = new Application(applicationConfig) {
            @Override
            protected ResourceHandlerFactory createResourceHandlerFactory() {
                return new RestResourceHandlerFactory() {
                    {
                        withSingleton(new APEInteractionsHandler(apeClient, apeQrefRepository, expedioUserContextResolver, expedioUserResource, customerResource));
                    }
                };
            }
        };
        application.start();

        apeCommentsResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "comments", "qref", QREF_STENCIL);
        apeManualQuoteResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "manual-quote", "customerId", "aCustomerId", "projectId", "aProjectId");
        apeSaveCommentsResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "saveComments", "qref", QREF_STENCIL, "userName", "userName");
        apeLoadCommentsResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "loadComments", "qref", QREF_STENCIL);
        apeQrefRecallResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "recall-qrefs");
        apeSaveManualQuoteResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "saveManualQuote");

        apeQref = new ApeQref();
        apeQref.setRequestId(REQUEST_ID);
        apeQref.setQrefId(QREF_STENCIL);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS),
                                           new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID)));
        apeQref2 = new ApeQref();
        apeQref2.setQrefId(QREF_STENCIL2);
        apeQref2.setRequestId(REQUEST_ID);
        apeQref2.setAttributes(newArrayList(new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID2)));

        apeQref3 = new ApeQref();
        apeQref3.setQrefId(QREF_STENCIL3);
        apeQref3.setRequestId(REQUEST_ID);
        apeQref3.setAttributes(newArrayList(new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID3)));

        when(apeQrefRepository.getApeQref(QREF_STENCIL)).thenReturn(apeQref);
        when(apeQrefRepository.getApeQref(QREF_STENCIL2)).thenReturn(apeQref2);
        when(apeQrefRepository.getApeQref(QREF_STENCIL3)).thenReturn(apeQref3);

        when(apeQrefRepository.getApeQref(QREF_STENCIL4)).thenReturn(apeQref2);
        when(apeQrefRepository.getApeQref(QREF_STENCIL5)).thenReturn(apeQref3);

        when(apeQrefRepository.getUserCommentsForQrefId(QREF_STENCIL)).thenReturn(newArrayList(new AccessUserCommentsEntity(ACCESS_USER_QREF_ID, QREF_ID, USER_LOGIN, COMMENTS,currentDate)));
        when(apeQrefRepository.getUserCommentsForQrefId(QREF_STENCIL2)).thenReturn(newArrayList(new AccessUserCommentsEntity(ACCESS_USER_QREF_ID,QREF_ID2,USER_LOGIN, COMMENTS ,currentDate)));
        when(apeQrefRepository.getUserCommentsForQrefId(QREF_STENCIL3)).thenReturn(newArrayList(new AccessUserCommentsEntity(ACCESS_USER_QREF_ID,QREF_ID3, USER_LOGIN, COMMENTS2,currentDate)));

        when(apeQrefRepository.getUserCommentsForQrefId(QREF_STENCIL4)).thenReturn(newArrayList(new AccessUserCommentsEntity(ACCESS_USER_QREF_ID,QREF_ID2, USER_LOGIN, COMMENTS,currentDate)));
        when(apeQrefRepository.getUserCommentsForQrefId(QREF_STENCIL5)).thenReturn(newArrayList(new AccessUserCommentsEntity(ACCESS_USER_QREF_ID,QREF_ID3, USER_LOGIN, COMMENTS2,currentDate)));

        when(apeQrefRepository.getAPERequestByRequestId(REQUEST_ID)).thenReturn(new ApeRequestEntity(REQUEST_ID, UNIQUE_ID, USER_LOGIN, QUOTE_CURRENCY));
    }

    @Test
    public void shouldSendCommentsToAPEAndReturnOK() throws Exception {
        QrefPairCommentsInputResponse expectedCommentsInputResponse = new QrefPairCommentsInputResponse();
        expectedCommentsInputResponse.setSuccess(true);
        expectedCommentsInputResponse.setQref(QREF_ID);

        when(apeClient.sendComment(commentsInputs)).thenReturn(new QrefPairCommentsInputResponse[]{expectedCommentsInputResponse});

        RestResponse response = apeCommentsResource.post(COMMENTS);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeClient).sendComment(commentsInputs);
    }

    @Test
    public void shouldSaveCommentsToDBAndReturnOK() throws Exception {
        RestResponse response = apeSaveCommentsResource.post();
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeQrefRepository).save(any(AccessUserCommentsEntity.class));
    }

    @Test
    public void shouldLoadCommentsFromDBAndReturnOK() throws Exception {
        RestResponse response = apeLoadCommentsResource.get();
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeQrefRepository).getUserCommentsForQrefId(QREF_STENCIL);
    }

    @Test
    public void shouldLoadCommentsAndReturnOKWhenNothingReturnedFromDB() throws Exception {
        when(apeQrefRepository.getUserCommentsForQrefId(QREF_STENCIL)).thenReturn((newArrayList(new AccessUserCommentsEntity(ACCESS_USER_QREF_ID,QREF_ID2, USER_LOGIN, COMMENTS,currentDate))));
        RestResponse response = apeLoadCommentsResource.get();
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeQrefRepository).getUserCommentsForQrefId(QREF_STENCIL);
    }

    @Test
    public void shouldSendPairIdAlongWithCommentsIfItExistsInQref() throws Exception {
        commentsInputs[0].setPairId(PAIR_ID);

        QrefPairCommentsInputResponse expectedCommentsInputResponse = new QrefPairCommentsInputResponse();
        expectedCommentsInputResponse.setSuccess(true);
        expectedCommentsInputResponse.setQref(QREF_ID);
        expectedCommentsInputResponse.setPairId(PAIR_ID);

        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("Pair", PAIR_ID),
                                           new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS),
                                           new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID)));

        when(apeQrefRepository.getApeQref(QREF_STENCIL)).thenReturn(apeQref);
        when(apeClient.sendComment(commentsInputs)).thenReturn(new QrefPairCommentsInputResponse[]{expectedCommentsInputResponse});

        RestResponse response = apeCommentsResource.post(COMMENTS);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeClient).sendComment(commentsInputs);
        verify(apeQrefRepository).getApeQref(QREF_STENCIL);
    }

    @Test
    public void shouldThrowPreconditionFailedWhenQrefWorkflowIsNotAssignedOrSentToWorkflow() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, COMPLETED_WORKFLOW_STATUS)));

        when(apeQrefRepository.getApeQref(QREF_STENCIL)).thenReturn(apeQref);

        try {
            apeCommentsResource.post(COMMENTS);
            fail();
        } catch(PreconditionFailedException e) {
            assertThat(e.errorDto().description, Is.is("Workflow is in an invalid state"));
        }
    }

    @Test
    public void shouldThrowPreConditionFailedWhenQrefWorkflowHasNotBeenSet() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, null)));

        when(apeQrefRepository.getApeQref(QREF_STENCIL)).thenReturn(apeQref);

        try {
            apeCommentsResource.post(COMMENTS);
            fail();
        } catch(PreconditionFailedException e) {
            assertThat(e.errorDto().description, Is.is("Workflow is in an invalid state"));
        }
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenSendCommentsToAPEFails() throws Exception {
        QrefPairCommentsInputResponse expectedCommentsInputResponse = new QrefPairCommentsInputResponse();
        expectedCommentsInputResponse.setSuccess(false);
        expectedCommentsInputResponse.setQref(QREF_ID);
        expectedCommentsInputResponse.setErrorDesc(ERROR_MSG);

        when(apeClient.sendComment(commentsInputs)).thenReturn(new QrefPairCommentsInputResponse[]{expectedCommentsInputResponse});

        try {
            apeCommentsResource.post(COMMENTS);
            fail();
        } catch(BadRequestException e) {
            assertThat(e.errorDto().description, Is.is(ERROR_MSG));
        }
    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowInternalServerErrorWhenAPECallFailsWhileSendingComments() throws Exception {
        when(apeClient.sendComment(Matchers.<QrefPairCommentsInput[]>any())).thenThrow(RemoteException.class);
        apeCommentsResource.post(COMMENTS);
    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowInternalServerErrorWhenQrefResourceNotFoundWhileSendingComments() throws Exception {
        when(apeQrefRepository.getApeQref(Matchers.<String>any())).thenThrow(ResourceNotFoundException.class);
        apeCommentsResource.post(COMMENTS);
    }

    @Test
    public void shouldSaveManualQuoteToDBAndReturnOK() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        apeQref.setRequestId(REQUEST_ID);
        ManualQuote manualQuote = new ManualQuote(newArrayList(new ManualQuoteItem(QREF_ID)));
        manualQuote.setPstnTelephoneLine("123456");
        manualQuote.setSiteTelephoneNumber("654321");
        when(manualQuoteItem.getQrefId()).thenReturn(QREF_ID);
        when(apeQrefRepository.getApeQref(QREF_ID)).thenReturn(apeQref);

        RestResponse response = apeSaveManualQuoteResource.post(manualQuote);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeQrefRepository).save(any(ApeRequestEntity.class));
    }

    @Test
    public void shouldSendQrefRecallRequestToAPEAndReturnOK() throws Exception {
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS),
                                           new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID)));

        apeQref2.setAttributes(newArrayList(new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS),
                                            new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID2)));

        QrefPairInput[] pairInputRequest = new QrefPairInput[2];
        pairInputRequest[0] = new QrefPairInput(QREF_ID, null,"");
        pairInputRequest[1] = new QrefPairInput(QREF_ID2, null,"");

        QrefRecallDTO qrefRecall = new QrefRecallDTO(newArrayList(QREF_STENCIL, QREF_STENCIL2));

        QrefPairInputResponse[] qrefPairInputResponse = new QrefPairInputResponse[2];
        qrefPairInputResponse[0] = new QrefPairInputResponse(QREF_ID, null,"", true, null);
        qrefPairInputResponse[1] = new QrefPairInputResponse(QREF_ID2, null,"", true, null);

        when(apeClient.recallQrefs(pairInputRequest)).thenReturn(qrefPairInputResponse);

        RestResponse response = apeQrefRecallResource.post(qrefRecall);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        verify(apeClient).recallQrefs(pairInputRequest);
    }

    @Test(expected = PreconditionFailedException.class)
    public void shouldThrowPreConditionFailedWhenQrefWorkflowIsInvalidDuringQrefRecall() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_STENCIL);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("QREF", QREF_ID),
                                           new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, COMPLETED_WORKFLOW_STATUS)));

        when(apeQrefRepository.getApeQref(QREF_STENCIL)).thenReturn(apeQref);
        apeQrefRecallResource.post(new QrefRecallDTO(newArrayList(QREF_STENCIL)));
    }

    @Test
    public void shouldSetWorkflowStatusToEmptyOnSuccessfulQrefRecall() throws Exception {
        QrefPairInput[] pairInputRequest = new QrefPairInput[1];
        pairInputRequest[0] = new QrefPairInput(QREF_ID, null,"");

        QrefRecallDTO qrefRecall = new QrefRecallDTO(newArrayList(QREF_STENCIL));

        QrefPairInputResponse[] qrefPairInputResponse = new QrefPairInputResponse[1];
        qrefPairInputResponse[0] = new QrefPairInputResponse(QREF_ID, null,"", true, null);

        when(apeClient.recallQrefs(pairInputRequest)).thenReturn(qrefPairInputResponse);

        RestResponse response = apeQrefRecallResource.post(qrefRecall);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        ApeQrefDetailEntity apeQrefDetailEntity = new ApeQrefDetailEntity(REQUEST_ID, QREF_STENCIL, "Workflow Status", "2", null);
        verify(apeQrefRepository).save(apeQrefDetailEntity);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenNoQrefStencilIdsSupplied() throws Exception {
        apeQrefRecallResource.post(new QrefRecallDTO());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenQrefRecallFails() throws Exception {
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS),
                                           new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID)));

        apeQref2.setAttributes(newArrayList(new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS),
                                            new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID2)));

        QrefPairInput[] pairInputRequest = new QrefPairInput[2];
        pairInputRequest[0] = new QrefPairInput(QREF_ID, null,"");
        pairInputRequest[1] = new QrefPairInput(QREF_ID2, null,"");

        QrefRecallDTO qrefRecall = new QrefRecallDTO(newArrayList(QREF_STENCIL, QREF_STENCIL2));

        QrefPairInputResponse[] qrefPairInputResponse = new QrefPairInputResponse[2];
        qrefPairInputResponse[0] = new QrefPairInputResponse(QREF_ID, null,"", false, ERROR_MSG);
        qrefPairInputResponse[1] = new QrefPairInputResponse(QREF_ID2, null,"", false, ERROR_MSG);

        when(apeClient.recallQrefs(pairInputRequest)).thenReturn(qrefPairInputResponse);

        try {
            apeQrefRecallResource.post(qrefRecall);
            fail();
        } catch(BadRequestException e) {
            assertThat(e.errorDto().description, containsString("Recall QREF Failed for all QREFs."));
            assertThat(e.errorDto().description, containsString("["+ QREF_ID + "] "+ ERROR_MSG));
            assertThat(e.errorDto().description, containsString("["+QREF_ID2 + "] "+ ERROR_MSG));
        }
    }

    @Test
    public void shouldSendManualQuoteRequestToAPEAndReturnOK() throws Exception {
        SqeQrefPairCommentsInput[] manualQuoteRequest = buildManualQuoteRequest();
        when(apeClient.manualQuote(manualQuoteRequest)).thenReturn(buildManualQuoteResponse(true));

        RestResponse response = apeManualQuoteResource.post(buildManualQuoteWithAddressDetails());
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeClient).manualQuote(manualQuoteRequest);
    }

    @Test
    public void shouldSendManualQuoteRequestToAPEAndReturnErrorResponseIfAddressValidationFails() throws Exception {
        SqeQrefPairCommentsInput[] manualQuoteRequest = buildManualQuoteRequest();
        when(apeClient.manualQuote(manualQuoteRequest)).thenReturn(buildManualQuoteResponse(true));

        try {
            apeManualQuoteResource.post(buildManualQuoteWithAddressDetailsAndMandatoryValueWithQrefIdFormat());
            fail();
        } catch (BadRequestException e) {
            assertThat(e.errorDto().description, Is.is("Access pricing Manual workflow request has failed for  for all QREFs. [a second qref] as the following site details (Country) are missing and need to be updated before the workflow request can be submitted.Please update the site details and re-submit the request., [a third qref] as the following site details (Country) are missing and need to be updated before the workflow request can be submitted.Please update the site details and re-submit the request."));
        }
    }

    @Test
    public void shouldThrowBadRequestExceptionForSomeInvalidWorkflowStates() throws Exception {
        SqeQrefPairCommentsInput[] manualQuoteRequest = buildManualQuoteRequest();

        ApeQref apeQref = new ApeQref();
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("QREF", QREF_ID2),
                                           new ApeQrefAttributeDetail("Pair", PAIR_ID),
                                           new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS)));

        when(apeQrefRepository.getApeQref(QREF_STENCIL2)).thenReturn(apeQref);
        when(apeClient.manualQuote(manualQuoteRequest)).thenReturn(buildManualQuoteResponse(true));

        try {
            apeManualQuoteResource.post(buildManualQuoteWithAddressDetails());
            fail();
        } catch(BadRequestException e) {
            assertThat(e.errorDto().description, Is.is("Access pricing Manual workflow request has failed for  for some QREFs. [" + QREF_ID2 + "] Workflow is in an invalid state"));
        }
    }

    @Test
    public void shouldThrowBadRequestExceptionForAllInvalidWorkflowStates() throws Exception {
        SqeQrefPairCommentsInput[] manualQuoteRequest = buildManualQuoteRequest();

        ApeQref apeQref = new ApeQref();
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("QREF", QREF_ID2),
                                           new ApeQrefAttributeDetail("Pair", PAIR_ID),
                                           new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS)));

        ApeQref apeQref2 = new ApeQref();
        apeQref2.setAttributes(newArrayList(new ApeQrefAttributeDetail("QREF", QREF_ID3),
                                           new ApeQrefAttributeDetail("Pair", PAIR_ID),
                                           new ApeQrefAttributeDetail(WORKFLOW_STATUS_NAME, SENT_TO_WORKFLOW_STATUS)));

        when(apeQrefRepository.getApeQref(QREF_STENCIL2)).thenReturn(apeQref);
        when(apeQrefRepository.getApeQref(QREF_STENCIL3)).thenReturn(apeQref2);
        when(apeClient.manualQuote(manualQuoteRequest)).thenReturn(buildManualQuoteResponse(true));

        try {
            apeManualQuoteResource.post(buildManualQuote());
            fail();
        } catch(BadRequestException e) {
            assertThat(e.errorDto().description, Is.is("Access pricing Manual workflow request has failed for  for all QREFs. [" + QREF_ID2 + "] Workflow is in an invalid state, [" + QREF_ID3 + "] Workflow is in an invalid state"));
        }
    }

    @Test
    public void shouldSendPairIdAlongWithManualQuoteIfItExistsInQref() throws Exception {
        SqeQrefPairCommentsInput[] manualQuoteRequest = buildManualQuoteRequest();
        manualQuoteRequest[0].setPairId(PAIR_ID);

        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID2);
        apeQref.setRequestId(REQUEST_ID);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("Pair", PAIR_ID),
                                           new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID2)));

        when(apeQrefRepository.getApeQref(QREF_ID2)).thenReturn(apeQref);
        ApeQref apeQref3 = new ApeQref();
        apeQref3.setQrefId(QREF_ID3);
                apeQref3.setRequestId(REQUEST_ID);
                apeQref3.setAttributes(newArrayList(new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID3)));
        when(apeQrefRepository.getApeQref(QREF_ID3)).thenReturn(apeQref3);
        AccessUserCommentsEntity accessUserCommentsEntity4 = new AccessUserCommentsEntity(ACCESS_USER_QREF_ID,QREF_ID2, USER_LOGIN, COMMENTS,currentDate);
        AccessUserCommentsEntity accessUserCommentsEntity5 = new AccessUserCommentsEntity(ACCESS_USER_QREF_ID,QREF_ID3, USER_LOGIN, COMMENTS2,currentDate);

        when(apeQrefRepository.getUserCommentsForQrefId(QREF_ID2)).thenReturn(newArrayList(accessUserCommentsEntity4));
        when(apeQrefRepository.getUserCommentsForQrefId(QREF_ID3)).thenReturn(newArrayList(accessUserCommentsEntity5));


        when(apeClient.manualQuote(manualQuoteRequest)).thenReturn(buildManualQuoteResponse(true));

        RestResponse response = apeManualQuoteResource.post(buildQuote());
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeClient).manualQuote(manualQuoteRequest);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(apeQrefRepository, times(3)).getApeQref(captor.capture());
        assertEquals(QREF_ID2, captor.getAllValues().get(0));
        assertEquals(QREF_ID3, captor.getAllValues().get(1));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenManualQuoteToAPEFailsForAllQrefs() throws Exception {
        when(apeClient.manualQuote(buildManualQuoteRequest())).thenReturn(buildManualQuoteResponse(false));

        try {
            apeManualQuoteResource.post(buildManualQuoteWithAddressDetails());
            fail();
        } catch(BadRequestException e) {
            assertThat(e.errorDto().description, Is.is("Access pricing Manual workflow request has failed for  for all QREFs. ["+QREF_ID2+"] "+ERROR_MSG+", [" + QREF_ID3+"] "+ERROR_MSG));
        }
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenManualQuoteToAPEFailsForSomeQrefs() throws Exception {
        SqeQrefPairInputResponse[] manualQuoteResponse = buildManualQuoteResponse(false);
        manualQuoteResponse[1].setSuccess(true);

        when(apeClient.manualQuote(buildManualQuoteRequest())).thenReturn(manualQuoteResponse);

        try {
            apeManualQuoteResource.post(buildManualQuoteWithAddressDetails());
            fail();
        } catch(BadRequestException e) {
            assertThat(e.errorDto().description, Is.is("Access pricing Manual workflow request has failed for  for some QREFs. ["+QREF_ID2+"] "+ERROR_MSG));
        }
    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowInternalServerErrorWhenAPECallFailsForManualQuote() throws Exception {
        when(apeClient.manualQuote(Matchers.<SqeQrefPairCommentsInput[]>any())).thenThrow(RemoteException.class);
        apeManualQuoteResource.post(buildManualQuote());
    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowInternalServerErrorWhenQrefResourceNotFoundForManualQuote() throws Exception {
        when(apeQrefRepository.getApeQref(Matchers.<String>any())).thenThrow(ResourceNotFoundException.class);
        apeManualQuoteResource.post(buildManualQuote());
    }

    @Test
    public void shouldUpdateWorkflowStatusForEachSuccessfullySentQrefAfterManualQuote() throws Exception {
        SqeQrefPairCommentsInput[] manualQuoteRequest = buildManualQuoteRequest();
        when(apeClient.manualQuote(manualQuoteRequest)).thenReturn(buildManualQuoteResponse(true));

        RestResponse response = apeManualQuoteResource.post(buildManualQuoteWithAddressDetails());
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        ApeQrefDetailEntity status1 = new ApeQrefDetailEntity(REQUEST_ID, QREF_STENCIL2, "Workflow Status", "-1", null);
        ApeQrefDetailEntity status2 = new ApeQrefDetailEntity(REQUEST_ID, QREF_STENCIL3, "Workflow Status", "-1", null);
        ApeQrefDetailEntity status3 = new ApeQrefDetailEntity(REQUEST_ID, QREF_STENCIL3, "WORKFLOW STATUS", "-1", null);
        verify(apeQrefRepository).save(status1);
        verify(apeQrefRepository).save(status2);
        verify(apeQrefRepository).save(status3);
    }

    @Test
    public void shouldOnlySendCommentsWithManualQuoteIfTheyExist() throws Exception {
        when(apeQrefRepository.getUserCommentsForQrefId(QREF_STENCIL2)).thenReturn((newArrayList(new AccessUserCommentsEntity(ACCESS_USER_QREF_ID,QREF_ID2, USER_LOGIN, COMMENTS,currentDate))));

        SqeQrefPairCommentsInput manualQuotePiece = new SqeQrefPairCommentsInput();
        manualQuotePiece.setQref(QREF_ID2);
        manualQuotePiece.setUserComments("");
        manualQuotePiece.setSalesUserFirstName("fName");
        manualQuotePiece.setSalesUserLastName("sName");
        manualQuotePiece.setSalesUserEMailId("email");
        manualQuotePiece.setSalesUserPhoneNo("123456");
        manualQuotePiece.setIEin(654321);
        manualQuotePiece.setSalesChannel("salesChannel");
        manualQuotePiece.setPocPSTNNo(PSTN_TEL_LINE);
        manualQuotePiece.setPocPhoneNo(SITE_TEL_NUMBER);
        manualQuotePiece.setPocEmail("avbc@mac.cm");
        manualQuotePiece.setPocFirstName("mani");
        manualQuotePiece.setPocLastName("ashish");

        SqeQrefPairCommentsInput[] manualQuoteRequest = new SqeQrefPairCommentsInput[]{manualQuotePiece};

        SqeQrefPairInputResponse[] manualQuoteResponse = new SqeQrefPairInputResponse[]{new SqeQrefPairInputResponse()};

        manualQuoteResponse[0].setQref(QREF_ID2);
        manualQuoteResponse[0].setSuccess(true);

        when(apeClient.manualQuote(any(SqeQrefPairCommentsInput[].class))).thenReturn(manualQuoteResponse);


        RestResponse response = apeManualQuoteResource.post(buildSingleManualQuoteItemWithAddressDetails());
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        verify(apeClient).manualQuote(any(SqeQrefPairCommentsInput[].class));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenManualQuotePstnOrSiteTelNumberAreMissing() throws Exception {
        SqeQrefPairInputResponse[] manualQuoteResponse = buildManualQuoteResponse(true);
        when(apeClient.manualQuote(buildManualQuoteRequest())).thenReturn(manualQuoteResponse);
        try {
            apeManualQuoteResource.post(new ManualQuote(newArrayList(new ManualQuoteItem(QREF_ID2), new ManualQuoteItem(QREF_ID3))));
            fail();
        } catch(BadRequestException e) {
            assertThat(e.errorDto().description, Is.is("Missing values: Site Telephone Number"));
        }
    }

    private ManualQuote buildQuote() {
        ManualQuote manualQuote = new ManualQuote(newArrayList(new ManualQuoteItem(QREF_ID2,"", "", "", "", "", "", "", "", "", "", "", "", "", ""), new ManualQuoteItem(QREF_ID3, "", "", "", "", "", "", "", "", "", "", "", "","", "")));
        manualQuote.setPstnTelephoneLine(PSTN_TEL_LINE);
        manualQuote.setSiteTelephoneNumber(SITE_TEL_NUMBER);
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withCountry("aCountry").withCity("City").withBuilding("aBuilding").withSubBuilding("subBuilding").withStreet("aStreet").withPostBox("aPostBox").withBuildingNumber("buildingNumber")
                                        .withStateCountyProvince("aProvince").withLocality("locality").withSubLocality("aSubLocality").withPhoneNumber("aPhoneNumber").withSubStreet("subStreet").withPostCode("postCode").build();
        BfgContact bfgContact = BfgContactFixture.aBfgContact().withPhoneNumber("aPhoneNumber").build();
        manualQuote.setBfgContact(bfgContact);
        manualQuote.setSiteDTO(siteDTO);
        return manualQuote;
    }

    private ManualQuote buildManualQuote() {
        ManualQuote manualQuote = new ManualQuote(newArrayList(new ManualQuoteItem(QREF_STENCIL2), new ManualQuoteItem(QREF_STENCIL3)));
        manualQuote.setPstnTelephoneLine(PSTN_TEL_LINE);
        manualQuote.setSiteTelephoneNumber(SITE_TEL_NUMBER);
        BfgContact bfgContact = BfgContactFixture.aBfgContact().withPhoneNumber("aPhoneNumber").build();
        manualQuote.setBfgContact(bfgContact);
        return manualQuote;
    }

    private ManualQuote buildManualQuoteWithAddressDetails() {
        ManualQuote manualQuote = new ManualQuote(newArrayList(new ManualQuoteItem(QREF_STENCIL2, "", "", "", "", "", "", "", "", "", "", "", "", "", ""), new ManualQuoteItem(QREF_STENCIL3, "", "", "", "", "", "", "", "", "", "", "", "", "", "")));
        manualQuote.setPstnTelephoneLine(PSTN_TEL_LINE);
        manualQuote.setSiteTelephoneNumber(SITE_TEL_NUMBER);
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withCountry("aCountry").withCity("City").withBuilding("aBuilding").withSubBuilding("subBuilding").withStreet("aStreet").withPostBox("aPostBox").withBuildingNumber("buildingNumber")
                                        .withStateCountyProvince("aProvince").withLocality("locality").withSubLocality("aSubLocality").withPhoneNumber("aPhoneNumber").withSubStreet("subStreet").withPostCode("postCode").build();
        BfgContact bfgContact = BfgContactFixture.aBfgContact().withPhoneNumber("aPhoneNumber").build();
        manualQuote.setBfgContact(bfgContact);
        manualQuote.setSiteDTO(siteDTO);
        return manualQuote;
    }

    private ManualQuote buildSingleManualQuoteItemWithAddressDetails() {
        ManualQuote manualQuote = new ManualQuote(newArrayList(new ManualQuoteItem(QREF_STENCIL2, "", "", "", "", "", "", "", "", "", "", "", "", "", "")));
        manualQuote.setPstnTelephoneLine(PSTN_TEL_LINE);
        manualQuote.setSiteTelephoneNumber(SITE_TEL_NUMBER);
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withCountry("aCountry").withCity("City").withBuilding("aBuilding").withSubBuilding("subBuilding").withStreet("aStreet").withPostBox("aPostBox").withBuildingNumber("buildingNumber")
                                        .withStateCountyProvince("aProvince").withLocality("locality").withSubLocality("aSubLocality").withPhoneNumber("aPhoneNumber").withSubStreet("subStreet").withPostCode("postCode").build();
        BfgContact bfgContact = BfgContactFixture.aBfgContact().withPhoneNumber("aPhoneNumber").build();
        manualQuote.setBfgContact(bfgContact);
        manualQuote.setSiteDTO(siteDTO);
        return manualQuote;
    }

    private ManualQuote buildManualQuoteWithAddressDetailsAndMandatoryValueWithQrefIdFormat() {
        ManualQuote manualQuote = new ManualQuote(newArrayList(new ManualQuoteItem(QREF_STENCIL4, "Y","", "", "", "", "", "", "", "", "", "", "", "", ""), new ManualQuoteItem(QREF_STENCIL5, "Y","", "", "", "", "", "", "", "", "", "", "", "", "")));
        manualQuote.setPstnTelephoneLine(PSTN_TEL_LINE);
        manualQuote.setSiteTelephoneNumber(SITE_TEL_NUMBER);
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withCity("City").withBuilding("aBuilding").withSubBuilding("subBuilding").withStreet("aStreet").withPostBox("aPostBox").withBuildingNumber("buildingNumber")
                                        .withStateCountyProvince("aProvince").withLocality("locality").withSubLocality("aSubLocality").withPhoneNumber("aPhoneNumber").withSubStreet("subStreet").withPostCode("postCode").withPostBox("aPostBox").build();
        BfgContact bfgContact = BfgContactFixture.aBfgContact().withPhoneNumber("aPhoneNumber").build();
        manualQuote.setBfgContact(bfgContact);
        manualQuote.setSiteDTO(siteDTO);
        return manualQuote;
    }

    private SqeQrefPairInputResponse[] buildManualQuoteResponse(boolean success) {
        SqeQrefPairInputResponse[] manualQuoteResponse = new SqeQrefPairInputResponse[]{new SqeQrefPairInputResponse(),
                                                                                        new SqeQrefPairInputResponse()};

        manualQuoteResponse[0].setQref(QREF_ID2);
        manualQuoteResponse[0].setSuccess(success);
        manualQuoteResponse[0].setErrorDesc(success ? null : ERROR_MSG);

        manualQuoteResponse[1].setQref(QREF_ID3);
        manualQuoteResponse[1].setSuccess(success);
        manualQuoteResponse[1].setErrorDesc(success ? null : ERROR_MSG);

        return manualQuoteResponse;
    }

    private SqeQrefPairCommentsInput[] buildManualQuoteRequest() {
        SqeQrefPairCommentsInput manualQuoteRequest = new SqeQrefPairCommentsInput();
        manualQuoteRequest.setQref(QREF_ID2);
        manualQuoteRequest.setUserComments(COMMENTS);
        manualQuoteRequest.setSalesUserFirstName("fName");
        manualQuoteRequest.setSalesUserLastName("sName");
        manualQuoteRequest.setSalesUserEMailId("email");
        manualQuoteRequest.setSalesUserPhoneNo("123456");
        manualQuoteRequest.setIEin(654321);
        manualQuoteRequest.setSalesChannel("salesChannel");
        manualQuoteRequest.setPocPSTNNo(PSTN_TEL_LINE);
        manualQuoteRequest.setPocPhoneNo(SITE_TEL_NUMBER);
        manualQuoteRequest.setPocEmail("avbc@mac.cm");
        manualQuoteRequest.setPocFirstName("mani");
        manualQuoteRequest.setPocLastName("ashish");

        SqeQrefPairCommentsInput manualQuoteRequest2 = new SqeQrefPairCommentsInput();
        manualQuoteRequest2.setQref(QREF_ID3);
        manualQuoteRequest2.setUserComments(COMMENTS2);
        manualQuoteRequest2.setSalesUserFirstName("fName");
        manualQuoteRequest2.setSalesUserLastName("sName");
        manualQuoteRequest2.setSalesUserEMailId("email");
        manualQuoteRequest2.setSalesUserPhoneNo("123456");
        manualQuoteRequest2.setIEin(654321);
        manualQuoteRequest2.setSalesChannel("salesChannel");
        manualQuoteRequest2.setPocPSTNNo(PSTN_TEL_LINE);
        manualQuoteRequest2.setPocPhoneNo(SITE_TEL_NUMBER);
        manualQuoteRequest2.setPocEmail("avbc@mac.cm");
        manualQuoteRequest2.setPocFirstName("mani");
        manualQuoteRequest2.setPocLastName("ashish");

        return new SqeQrefPairCommentsInput[]{manualQuoteRequest, manualQuoteRequest2};
    }
}
