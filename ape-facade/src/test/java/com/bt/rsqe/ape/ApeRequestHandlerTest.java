package com.bt.rsqe.ape;

import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.config.TimeoutConfig;
import com.bt.rsqe.ape.config.CallbackEndpointConfig;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.dto.AsIsAsset;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.utils.Uuid;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.bt.rsqe.web.rest.RestResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApeRequestHandlerTest {
    private static final String USER_LOGIN = "aSalesUser";
    private static final String QUOTE_CURRENCY = "GBP";
    private static final String REQUEST_ID = "aRequestId";
    private APEQrefRepository apeQrefRepository = mock(APEQrefRepository.class);
    private APEClient apeClient = mock(APEClient.class);
    private String uniqueId;
    private CallbackEndpointConfig callbackEndpointConfig = mock(CallbackEndpointConfig.class);
    private TimeoutConfig timeoutConfig = mock(TimeoutConfig.class);
    private RestResource requestStatusResource, requestStatusRequestIdResource, createRequestResource, cancelRequestResource;
    private ApeQrefRequestDTO draftRequest, inServiceRequest, mbpRequest;

    @Before
    public void setup() throws Exception {
        uniqueId = Uuid.randomUuid();

        ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();
        Application application = new Application(applicationConfig) {
            @Override
            protected ResourceHandlerFactory createResourceHandlerFactory() {
                return new RestResourceHandlerFactory() {
                    {
                        withSingleton(new ApeRequestHandler(apeQrefRepository, apeClient, callbackEndpointConfig,null));
                    }
                };
            }
        };
        application.start();

        requestStatusResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "access", "request", uniqueId, "status");
        requestStatusRequestIdResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "access", "request", uniqueId, "status", REQUEST_ID);
        cancelRequestResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "access", "request", uniqueId, "cancel");

        Map<String, String> queryParams = newHashMap();
        createRequestResource = new RestRequestBuilder(applicationConfig).build(new String[]{"rsqe", "ape-facade", "access", "request", uniqueId}, queryParams);

        UserDTO user = new UserDTO();
        user.loginName = USER_LOGIN;

        draftRequest = new ApeQrefRequestDTO(uniqueId,
                                             new CustomerDTO(),
                                             new SiteDTO("1","siteName"),
                                             user,
                                             "GBP",
                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute("ASSET VERSION STATUS", "DRAFT")),
                                             new ProductIdentifier(), ApeQrefRequestDTO.ProcessType.PROVIDE, ApeQrefRequestDTO.SubProcessType.SAME_SITE, null, null, AsIsAsset.NIL, new SiteDTO(), ChangeType.ADD, null,"1234", "5678");

        inServiceRequest = new ApeQrefRequestDTO(uniqueId,
                                                 new CustomerDTO(),
                                                 new SiteDTO("1","siteName"),
                                                 user,
                                                 "GBP",
                                                 newArrayList(new ApeQrefRequestDTO.AssetAttribute("MIN REQUIRED SPEED", "64 Mbps")),
                                                 new ProductIdentifier(),
                                                 ApeQrefRequestDTO.ProcessType.MODIFY, null, new ApeQrefRequestDTO.SupplierDetails("supplierProduct", "circuitId"), null, AsIsAsset.NIL, new SiteDTO(), ChangeType.ADD, null,"1234","5678");

        mbpRequest = new ApeQrefRequestDTO(uniqueId,
                                             new CustomerDTO(),
                                             new SiteDTO("1","siteName"),
                                             user,
                                             "GBP",
                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute("ASSET VERSION STATUS", "DRAFT")),
                                             new ProductIdentifier(), ApeQrefRequestDTO.ProcessType.PROVIDE, ApeQrefRequestDTO.SubProcessType.SAME_SITE, null, null, AsIsAsset.NIL, new SiteDTO(), ChangeType.ADD, "MBP Access","1234","5678");


    }

    @Test
    public void shouldReturnApeRequestStatus() {
        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(anApeRequestEntityFor(uniqueId));

        RestResponse response = requestStatusResource.get();
        QrefRequestStatus qrefRequestStatus = response.getEntity(QrefRequestStatus.class);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(qrefRequestStatus.getUniqueId(), is(uniqueId));
        assertThat(qrefRequestStatus.getStatus(), is(QrefRequestStatus.Status.WAITING));
    }

    @Test
    public void shouldReturnApeRequestStatusBasedOnRequestId() throws Exception {
        when(apeQrefRepository.getAPERequestByRequestId(REQUEST_ID)).thenReturn(anApeRequestEntityFor(uniqueId));

        RestResponse response = requestStatusRequestIdResource.get();
        QrefRequestStatus qrefRequestStatus = response.getEntity(QrefRequestStatus.class);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(qrefRequestStatus.getUniqueId(), is(uniqueId));
        assertThat(qrefRequestStatus.getStatus(), is(QrefRequestStatus.Status.WAITING));

        verify(apeQrefRepository).getAPERequestByRequestId(REQUEST_ID);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldReturnNotFoundWhenNoRequestExistsForRequestId() throws Exception {
        when(apeQrefRepository.getAPERequestByRequestId(REQUEST_ID)).thenThrow(ResourceNotFoundException.class);
        requestStatusRequestIdResource.get();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldReturnNotFoundWhenRequestHasBeenCancelledForRequestId() throws Exception {
        final ApeRequestEntity request = anApeRequestEntityFor(uniqueId);
        request.setStatus(QrefRequestStatus.Status.CANCELLED);
        when(apeQrefRepository.getAPERequestByRequestId(REQUEST_ID)).thenReturn(request);
        requestStatusRequestIdResource.get();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldReturnNotFound() {
        doThrow(ResourceNotFoundException.class).when(apeQrefRepository).getAPERequestByUniqueId(uniqueId);
        requestStatusResource.get();
    }

    @Test
    public void shouldHandleCreateRequestForNonMBPScenario() {
        MultisiteResponse apeResponse = new MultisiteResponse();
        apeResponse.setRequestId(REQUEST_ID);
        apeResponse.setComments("some comments");

        when(apeClient.multipleProvideQuote(any(SqeAccessInput.class))).thenReturn(apeResponse);
        when(callbackEndpointConfig.getUri()).thenReturn("http://test/%s");

        RestResponse response = createRequestResource.post(draftRequest);

        assertEquals(draftRequest.getSubProcessType().getType(), "SameSite");
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat((response.getEntity(QrefRequestStatus.class)).getUniqueId(), is(uniqueId));

        verify(apeQrefRepository).save(new ApeRequestEntity(REQUEST_ID, uniqueId, USER_LOGIN, QUOTE_CURRENCY, new ApeRequestDetailEntity(REQUEST_ID, "ASSET VERSION STATUS", "DRAFT")));
    }

    @Test
    public void shouldHandleCreateRequestForMBPScenario() {
        MultisiteResponse apeResponse = new MultisiteResponse();
        apeResponse.setRequestId(REQUEST_ID);
        apeResponse.setComments("some comments");

        when(apeClient.provideQuoteForGlobalPricing(any(SqeAccessInputDetails.class))).thenReturn(apeResponse);
        when(callbackEndpointConfig.getUri()).thenReturn("http://test/%s");

        RestResponse response = createRequestResource.post(mbpRequest);

        assertEquals(draftRequest.getSubProcessType().getType(), "SameSite");
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat((response.getEntity(QrefRequestStatus.class)).getUniqueId(), is(uniqueId));

        verify(apeQrefRepository).save(new ApeRequestEntity(REQUEST_ID, uniqueId, USER_LOGIN, QUOTE_CURRENCY, new ApeRequestDetailEntity(REQUEST_ID, "ASSET VERSION STATUS", "DRAFT")));
    }

    @Test
    public void shouldUpdateRequestStatus() throws Exception {
        final ApeRequestEntity currentApeRequestEntity = new ApeRequestEntity(REQUEST_ID, uniqueId, USER_LOGIN, QUOTE_CURRENCY);
        currentApeRequestEntity.setStatus(QrefRequestStatus.Status.WAITING);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(currentApeRequestEntity);

        QrefRequestStatus requestStatus = new QrefRequestStatus(uniqueId, REQUEST_ID, QrefRequestStatus.Status.ERROR, "anError");
        createRequestResource.put(requestStatus);

        currentApeRequestEntity.setStatus(QrefRequestStatus.Status.ERROR);
        currentApeRequestEntity.setErrorMessage("anError");
        verify(apeQrefRepository).save(currentApeRequestEntity);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldReturnNotFoundWhenTryingToUpdateARequestWIthADifferentRequestId() throws Exception {
        final ApeRequestEntity currentApeRequestEntity = new ApeRequestEntity("aDifferentRequest", uniqueId, USER_LOGIN, QUOTE_CURRENCY);
        currentApeRequestEntity.setStatus(QrefRequestStatus.Status.WAITING);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(currentApeRequestEntity);

        QrefRequestStatus requestStatus = new QrefRequestStatus(uniqueId, REQUEST_ID, QrefRequestStatus.Status.ERROR, "anError");
        createRequestResource.put(requestStatus);
    }

    @Test
    public void shouldHandleCreateRequestInModifyJourney() {
        MultisiteResponse apeResponse = new MultisiteResponse();
        apeResponse.setRequestId(REQUEST_ID);

        when(apeClient.bulkModifyQuote(any(SQEBulkModifyInput.class))).thenReturn(apeResponse);
        when(callbackEndpointConfig.getUri()).thenReturn("http://test/%s");

        RestResponse response = createRequestResource.post(inServiceRequest);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat((response.getEntity(QrefRequestStatus.class)).getUniqueId(), is(uniqueId));

        verify(apeQrefRepository).save(new ApeRequestEntity(REQUEST_ID, uniqueId, USER_LOGIN, QUOTE_CURRENCY, new ApeRequestDetailEntity(REQUEST_ID, "MIN REQUIRED SPEED", "64 Mbps")));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldReturnNotFoundExceptionIfWeTryToCancelANonExistentRequestEntity() {
        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(null);
        cancelRequestResource.post();
    }

    @Test
    public void shouldUpdateTheStatusOfQrefDetailEntityToCancelledIfExists() throws Exception {

        final ApeRequestEntity currentApeRequestEntity = new ApeRequestEntity();
        currentApeRequestEntity.setStatus(QrefRequestStatus.Status.WAITING);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(currentApeRequestEntity);
        RestResponse response = cancelRequestResource.post();

        verify(apeQrefRepository, times(1)).save(Matchers.<ApeRequestEntity>anyObject());

        ArgumentCaptor<ApeRequestEntity> argument = ArgumentCaptor.forClass(ApeRequestEntity.class);
        verify(apeQrefRepository).save(argument.capture());
        assertEquals(QrefRequestStatus.Status.CANCELLED, argument.getValue().getStatus());

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    @Ignore("Marcus - 2013/07/31 - A test similar to this needs writing/this test should be un-ignored when strategy for deleting QREFS implemented" +
            "in R28")
    public void shouldDeleteAnyQREFSAssociatedWithTheQREFRequest() throws Exception {
        final ApeRequestEntity currentApeRequestEntity = new ApeRequestEntity();
        currentApeRequestEntity.setStatus(QrefRequestStatus.Status.WAITING);

        final ApeQrefDetailEntity qref = new ApeQrefDetailEntity();
        qref.setQrefId("qrefId123");

        List<ApeQrefDetailEntity> qrefs = newArrayList();
        qrefs.add(qref);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(currentApeRequestEntity);
        when(apeQrefRepository.getAPEQrefsByUniqueId(uniqueId)).thenReturn(qrefs);

        cancelRequestResource.post();

        verify(apeQrefRepository, times(1)).deleteApeQref(qref.getQrefId());
    }

    @Test
    public void shouldSaveErrorRequestEntityWhenCreateRequestFailsNonMBPScenario() throws Exception {
        MultisiteResponse apeResponse = new MultisiteResponse();
        apeResponse.setRequestId(REQUEST_ID);
        apeResponse.setComments("[Failure] : anErrorOccurred");

        when(apeClient.multipleProvideQuote(any(SqeAccessInput.class))).thenReturn(apeResponse);
        when(callbackEndpointConfig.getUri()).thenReturn("http://test/%s");

        RestResponse response = createRequestResource.post(draftRequest);
        QrefRequestStatus qrefRequestStatus = response.getEntity(QrefRequestStatus.class);
        assertThat(qrefRequestStatus.getStatus(), is(QrefRequestStatus.Status.ERROR));
        assertThat(qrefRequestStatus.getErrorMessage(), is("APE Request failed: [Failure] : anErrorOccurred"));

        ApeRequestEntity requestEntity = new ApeRequestEntity(REQUEST_ID, uniqueId, USER_LOGIN, QUOTE_CURRENCY, new ApeRequestDetailEntity(REQUEST_ID, "ASSET VERSION STATUS", "DRAFT"));
        requestEntity.setStatus(QrefRequestStatus.Status.ERROR);
        requestEntity.setErrorMessage("APE Request failed: [Failure] : anErrorOccurred");

        verify(apeQrefRepository).save(requestEntity);
    }

    @Test
    public void shouldSaveErrorRequestEntityWhenCreateRequestFailsMBPScenario() throws Exception {
        MultisiteResponse apeResponse = new MultisiteResponse();
        apeResponse.setRequestId(REQUEST_ID);
        apeResponse.setComments("[Failure] : anErrorOccurred");

        when(apeClient.provideQuoteForGlobalPricing(any(SqeAccessInputDetails.class))).thenReturn(apeResponse);
        when(callbackEndpointConfig.getUri()).thenReturn("http://test/%s");

        RestResponse response = createRequestResource.post(mbpRequest);
        QrefRequestStatus qrefRequestStatus = response.getEntity(QrefRequestStatus.class);
        assertThat(qrefRequestStatus.getStatus(), is(QrefRequestStatus.Status.ERROR));
        assertThat(qrefRequestStatus.getErrorMessage(), is("APE Request failed: [Failure] : anErrorOccurred"));

        ApeRequestEntity requestEntity = new ApeRequestEntity(REQUEST_ID, uniqueId, USER_LOGIN, QUOTE_CURRENCY, new ApeRequestDetailEntity(REQUEST_ID, "ASSET VERSION STATUS", "DRAFT"));
        requestEntity.setStatus(QrefRequestStatus.Status.ERROR);
        requestEntity.setErrorMessage("APE Request failed: [Failure] : anErrorOccurred");

        verify(apeQrefRepository).save(requestEntity);
    }

    private ApeRequestEntity anApeRequestEntityFor(String uniqueId) {
        return new ApeRequestEntity(Uuid.randomUuid(), uniqueId, null, QUOTE_CURRENCY);
    }
}
