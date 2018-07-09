package com.bt.rsqe.ape;

import com.bt.rsqe.EmailService;
import com.bt.rsqe.ape.config.ApeMappingConfigLoader;
import com.bt.rsqe.ape.config.LocalIdentifier;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.ape.dto.ApeQrefPrices;
import com.bt.rsqe.ape.dto.ApeQrefProductConfiguration;
import com.bt.rsqe.ape.dto.ApeQrefProjectDetail;
import com.bt.rsqe.ape.dto.ApeQrefSiteDetails;
import com.bt.rsqe.ape.dto.ApeQrefStencilId;
import com.bt.rsqe.ape.dto.ApeQrefUpdate;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.AccessStaffCommentEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.ClassPathResource;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.utils.RsqeCharset;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import com.bt.rsqe.web.rest.exception.InternalServerErrorException;
import com.bt.rsqe.web.rest.exception.PreconditionFailedException;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.mail.MessagingException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

public class ApeEmailHandlerTest {
    private static final ApeQrefStencilId QREF_ID_STENCIL = new ApeQrefStencilId("aQrefIdStencil");
    private static final String QREF_ID = "aQrefId";
    private static final String REQUEST_ID = "aRequestId";
    private static final String USER_LOGIN = "aSalesUser";
    private static final String QUOTE_CURRENCY = "EUR";
    private static final String USER_EMAIL = "aUserEmail@bt.com";
    private static final String USER_FORENAME = "Test";
    private static final String USER_SURNAME = "User";
    private static final String APE_USER_NAME = "APEUser";
    private static final String APE_USER_NAME_2 = "APEUser2";
    private static final ApeQrefProjectDetail APE_QREF_PROJECT_DETAIL = new ApeQrefProjectDetail("aUniqueId","aProjectId","1","ABC","America","1.0",
                                                                                       "Provide","36","GBP","someSiteName","123","BT Americas");
    private APEQrefRepository apeQrefRepository;
    private UserResource expedioUserResource;
    private EmailService emailService;
    private UserDTO userDTO;
    private ApplicationConfig applicationConfig;
    private RestResource resource;
    private ApeQref apeQref;

    AccessStaffCommentEntity commentEntity1 = new AccessStaffCommentEntity(QREF_ID, "comment1", "staffEmail1", APE_USER_NAME, Date.valueOf("2000-01-01"));
    AccessStaffCommentEntity commentEntity2 = new AccessStaffCommentEntity(QREF_ID, "comment2", "staffEmail2", APE_USER_NAME_2, Date.valueOf("2000-01-03"));

    @Before
    public void before() throws Exception {
        apeQrefRepository = mock(APEQrefRepository.class);
        expedioUserResource = mock(UserResource.class);
        emailService = mock(EmailService.class);

        applicationConfig = StubApplicationConfig.defaultTestConfig();
        Application application = new Application(applicationConfig) {
            @Override
            protected ResourceHandlerFactory createResourceHandlerFactory() {
                return new RestResourceHandlerFactory() {
                    {
                        withSingleton(new ApeEmailHandler(emailService, apeQrefRepository, expedioUserResource));
                    }
                };
            }
        };
        application.start();

        userDTO = mock(UserDTO.class);
        when(expedioUserResource.findUser(USER_LOGIN)).thenReturn(userDTO);
        when(userDTO.getEmail()).thenReturn(USER_EMAIL);
        when(userDTO.getForename()).thenReturn(USER_FORENAME);
        when(userDTO.getSurname()).thenReturn(USER_SURNAME);

        apeQref = new ApeQref(REQUEST_ID, QREF_ID_STENCIL.getValue());
        apeQref.getAttributes().add(new ApeQrefAttributeDetail(LocalIdentifier.QREF.name(), QREF_ID));

        when(apeQrefRepository.getApeQref(QREF_ID_STENCIL.getValue())).thenReturn(apeQref);
        when(apeQrefRepository.getAPERequestByRequestId(REQUEST_ID)).thenReturn(new ApeRequestEntity(REQUEST_ID, null, USER_LOGIN, QUOTE_CURRENCY));

        List<AccessStaffCommentEntity> staffComments = newArrayList();
        staffComments.add(commentEntity1);
        staffComments.add(commentEntity2);

        when(apeQrefRepository.getStaffComments(QREF_ID_STENCIL.getValue())).thenReturn(staffComments);

    }

    @Test
    public void shouldSendUpdateEmailWhenQrefHasBeenRejected() throws Exception {
        resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-update");

        String subject = QREF_ID + " - APE Manual Workflow - country - city - speed - Rejected by "
                         + APE_USER_NAME ;

        String expectedBodyMessage = constructBodyMessage("qref-reject-email-body.html", new HashMap<String, String>() {{
            put("#supplier#", "");
        }});

        sendAndVerifyEmail(AccessWorkflowStatus.REJECTED, subject, expectedBodyMessage);
    }

    @Test
    public void shouldSendUpdateEmailWhenQrefHasBeenUpdatedWithNullValuesReplacedByEmptyString() throws Exception {

        resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-update");

        String subject = QREF_ID + " - APE Manual Workflow - country - city - speed - has been assigned to "
            + APE_USER_NAME ;

        sendAndVerifyEmail(AccessWorkflowStatus.ASSIGNED, subject, constructBodyMessage("qref-update-email-body.html", new HashMap<String, String>() {{
            put("#supplier#", "");
            put("#workFlowStatus#", AccessWorkflowStatus.ASSIGNED.getDescription());
        }}));
    }

    @Test
    public void shouldSendUpdateEmailWhenQrefHasBeenCompleted() throws Exception {
        resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-update");

        String subject = QREF_ID + " - APE Manual Workflow - country - city - speed - is completed by "
                         + APE_USER_NAME ;

        sendAndVerifyEmail(AccessWorkflowStatus.COMPLETED, subject, constructBodyMessage("qref-update-email-body.html", new HashMap<String, String>() {{
            put("#supplier#", "");
            put("#workFlowStatus#", AccessWorkflowStatus.COMPLETED.getDescription());
        }}));
    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowInternalServerErrorWhenSendEmailFails() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-update");

        Mockito.doThrow(MessagingException.class).when(emailService).sendEmail(Matchers.<String>any(),
                                                                               Matchers.<String>any(),
                                                                               Matchers.<String>any(),
                                                                               Matchers.<String>any());
        //When
        resource.post(newQrefUpdate(QREF_ID_STENCIL, AccessWorkflowStatus.COMPLETED));
    }

    @Test(expected = PreconditionFailedException.class)
    public void shouldThrowPreconditionFailedWhenQrefIsInInvalidWorkflowState() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-update");

        //When
        resource.post(newQrefUpdate(QREF_ID_STENCIL, AccessWorkflowStatus.SENT_TO_WORKFLOW));
    }

    @Test(expected = PreconditionFailedException.class)
    public void shouldThrowPreconditionFailedWhenQrefIsHasNoWorkflowState() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-update");

        //When
        resource.post(newQrefUpdate(QREF_ID_STENCIL, null));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundWhenExpedioUserDoesNotExist() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-update");
        when(expedioUserResource.findUser(USER_LOGIN)).thenThrow(ResourceNotFoundException.class);

        //When
        resource.post(newQrefUpdate(QREF_ID_STENCIL, AccessWorkflowStatus.COMPLETED));
    }

    @Test
    public void shouldSendInitialSyncUpNotificationEmail() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-initial-syncup-response");

        String expectedSubject = String.format("Access pricing request for [Quote Name: %s ] under [Customer Name: %s ]", "America", "ABC");
        String expectedBodyMessage = constructBodyMessage("qref-initial-syncup-email-body.html",  new HashMap<String, String>() {{
            put("#ErrorMessage#", "");
        }});

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(REQUEST_ID, "aUniqueId", USER_LOGIN, QUOTE_CURRENCY,
                                                      new ApeRequestDetailEntity(REQUEST_ID, ProductOffering.APE_FLAG, "Yes"));
        when(apeQrefRepository.getAPERequestByUniqueId("aUniqueId")).thenReturn(apeRequestEntity);

        //When
        RestResponse response = resource.post(APE_QREF_PROJECT_DETAIL);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

       //Then
        verify(emailService).sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT,
                                       expectedSubject,
                                       expectedBodyMessage,
                                       USER_EMAIL);
    }

    @Test
    public void shouldSendInitialSyncUpNotificationEmailWithErrorMessage() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-initial-syncup-response");

        String expectedSubject = String.format("Access pricing request for [Quote Name: %s ] under [Customer Name: %s ]", "America", "ABC");
        String expectedBodyMessage = constructBodyMessage("qref-initial-syncup-email-body.html",  new HashMap<String, String>() {{
            put("#ErrorMessage#", "someErrorMessage");
        }});


        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(REQUEST_ID, "aUniqueId", USER_LOGIN, QUOTE_CURRENCY,
                                                                 new ApeRequestDetailEntity(REQUEST_ID, ProductOffering.APE_FLAG, "Yes"));
        apeRequestEntity.setErrorMessage("someErrorMessage");
        when(apeQrefRepository.getAPERequestByUniqueId("aUniqueId")).thenReturn(apeRequestEntity);

        //When
        RestResponse response = resource.post(APE_QREF_PROJECT_DETAIL);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        //Then
        verify(emailService).sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT,
                                       expectedSubject,
                                       expectedBodyMessage,
                                       USER_EMAIL);
    }

    @Test
    public void shouldNotSendInitialSyncUpNotificationEmailWhenHandlingSimulatedApeInteraction() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-initial-syncup-response");



        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(REQUEST_ID, "aUniqueId", USER_LOGIN, QUOTE_CURRENCY,
                                                                 new ApeRequestDetailEntity(REQUEST_ID, ProductOffering.APE_FLAG, "No"));
        when(apeQrefRepository.getAPERequestByUniqueId("aUniqueId")).thenReturn(apeRequestEntity);

        //When
        RestResponse response = resource.post(APE_QREF_PROJECT_DETAIL);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        //Then
        verify(emailService, never()).sendEmail(eq(EmailService.DEFAULT_FROM_MAIL_ACCOUNT),
                                       anyString(),
                                       anyString(),
                                       anyString());
    }

    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowInternalServerErrorWhenIntialSyncUpEmailFails() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-initial-syncup-response");

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(REQUEST_ID, "aUniqueId", USER_LOGIN, QUOTE_CURRENCY,
                                                                 new ApeRequestDetailEntity(REQUEST_ID, ProductOffering.APE_FLAG, "Yes"));

        when(apeQrefRepository.getAPERequestByUniqueId("aUniqueId")).thenReturn(apeRequestEntity);


        Mockito.doThrow(MessagingException.class).when(emailService).sendEmail(Matchers.<String>any(),
                                                                               Matchers.<String>any(),
                                                                               Matchers.<String>any(),
                                                                               Matchers.<String>any());
        //When
        resource.post(APE_QREF_PROJECT_DETAIL);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundWhenExpedioUserDoesNotExistWhenSendingIntialiSyncUpEmail() throws Exception {
        //Given
        RestResource resource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "email", "qref-initial-syncup-response");
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(REQUEST_ID, "aUniqueId", USER_LOGIN, QUOTE_CURRENCY,
                                                                 new ApeRequestDetailEntity(REQUEST_ID, ProductOffering.APE_FLAG, "Yes"));

        when(apeQrefRepository.getAPERequestByUniqueId("aUniqueId")).thenReturn(apeRequestEntity);

        when(expedioUserResource.findUser(USER_LOGIN)).thenThrow(ResourceNotFoundException.class);

        //When
        resource.post(APE_QREF_PROJECT_DETAIL);
    }


    private String constructBodyMessage(String templateName, Map<String, String> replaceableValues) throws IOException {
        String message = new ClassPathResource("com/bt/rsqe/ape/" + templateName).textContent(RsqeCharset.defaultCharset());
        for (String key : replaceableValues.keySet()) {
            message = message.replaceAll(key, replaceableValues.get(key));
        }
        return message;
    }

    private void sendAndVerifyEmail(AccessWorkflowStatus workflowStatus, String expectedSubject, String expectedBody) throws Exception {

        ApeQrefUpdate qrefUpdate = newQrefUpdate(QREF_ID_STENCIL, workflowStatus);

        RestResponse response = resource.post(qrefUpdate);
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        verify(emailService).sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT,
                                       expectedSubject,
                                       expectedBody,
                                       USER_EMAIL);
    }

    private ApeQrefUpdate newQrefUpdate(ApeQrefStencilId stencilId, AccessWorkflowStatus workflowStatus) {
        if(null != workflowStatus) {
            String workflowStatusAttributeName = ApeMappingConfigLoader.getLocalIdentifierMappings()
                                                                           .getLocalIdentifierMappingConfig(LocalIdentifier.WORKFLOW_STATUS.name())
                                                                           .getOfferingAttributeConfig()[0].getName();

            apeQref.getAttributes().add(new ApeQrefAttributeDetail(workflowStatusAttributeName, String.valueOf(workflowStatus.getStatus())));
        }

        ApeQrefProductConfiguration apeQrefProductConfiguration = new ApeQrefProductConfiguration("product", "accessTechnology", "speed", null, "suppProduct", workflowStatus);
        ApeQrefSiteDetails apeQrefSiteDetails = new ApeQrefSiteDetails("site", "street", "city", "state", "postCode", "country");
        ApeQrefPrices apeQrefPricingDetails = new ApeQrefPrices("currency", "installPrice", "monthlyPrice", "status");

        return new ApeQrefUpdate(stencilId, apeQrefSiteDetails, apeQrefProductConfiguration, apeQrefPricingDetails);
    }
}
