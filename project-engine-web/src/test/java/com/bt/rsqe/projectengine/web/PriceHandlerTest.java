package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.ScopePricing;
import com.bt.rsqe.customerinventory.client.ScopePricingItem;
import com.bt.rsqe.customerinventory.client.ScopePricingItemError;
import com.bt.rsqe.customerinventory.client.ScopePricingStatus;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pricing.PriceClientResponse;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.PricingFacadeService;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PriceHandlerTest {

    private PricingClient pricingClient = mock(PricingClient.class);
    private ProductInstanceClient futureProductInstanceClient;
    private ExecutorService executorService;
    private PriceHandler priceHandler;
    private static final String LINEITEMS = "lineItems";
    private static final String CUSTOMER_ID = "customerId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private SiteFacade siteFacade;
    private PricingFacadeService pricingFacadeService;
    private PmrClient pmrClient;
    private ProjectResource projectResource;
    private UserResource expedioUserResource;
    private CustomerResource customerResource;
    private PriceHandlerService priceHandlerService;
    private PriceHandlerProcessor priceHandlerProcessor;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        siteFacade = mock(SiteFacade.class);
        pricingFacadeService = mock(PricingFacadeService.class);
        pmrClient = mock(PmrClient.class);
        projectResource = mock(ProjectResource.class);
        expedioUserResource = mock(UserResource.class);
        customerResource = mock(CustomerResource.class);
        futureProductInstanceClient = mock(ProductInstanceClient.class);
        priceHandlerService = mock(PriceHandlerService.class);
        priceHandlerProcessor = mock(PriceHandlerProcessor.class);
        UserContext userContext = new UserContext("login", "userToken", "channel");
        userContext.getPermissions().indirectUser = false;
        UserContextManager.setCurrent(userContext);
        priceHandler = new PriceHandler(futureProductInstanceClient, priceHandlerProcessor);
    }

    @Test
    public void shouldTriggerPriceProcessing() throws Exception {
        //When
        ScopePricing scopePricing = new ScopePricing(LINEITEMS, Lists.<ScopePricingItem>newArrayList());
        scopePricing.setStatus(ScopePricingStatus.PROCESSING);
        when(futureProductInstanceClient.getScopePricing(LINEITEMS)).thenThrow(ResourceNotFoundException.class)
            .thenReturn(scopePricing);
        //Then
        Response response = priceHandler.getPrices(CUSTOMER_ID, null, PROJECT_ID, QUOTE_OPTION_ID, LINEITEMS);
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());
        assertFalse(jsonObject.getBoolean("done"));
        verify(futureProductInstanceClient, times(2)).getScopePricing(LINEITEMS);
        verify(futureProductInstanceClient, times(1)).createScopePricing(LINEITEMS);
        verify(priceHandlerProcessor, times(1)).startPricing(LINEITEMS, CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID,
                                                             UserContextManager.getCurrent().getPermissions().indirectUser, "userToken");
    }

    @Test
    public void shouldReturnDoneForProcessingItems() throws Exception {
        //When
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap<String, PriceClientResponse>();
        String priceClientResourceMapJson = "[{\"errors\":[],\"status\":\"Firm\",\"lineItemId\":\"item\"}]";
        ScopePricing scopePricingProcessing = new ScopePricing(LINEITEMS, Lists.<ScopePricingItem>newArrayList());
        scopePricingProcessing.setStatus(ScopePricingStatus.PROCESSING);
        ScopePricing scopePricing = new ScopePricing(LINEITEMS, newArrayList(new ScopePricingItem("item", "Firm", LINEITEMS)));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        when(futureProductInstanceClient.getScopePricing(LINEITEMS)).thenThrow(ResourceNotFoundException.class)
            .thenReturn(scopePricingProcessing).thenReturn(scopePricing);
        when(priceHandlerService.processLineItemsForPricing(LINEITEMS, CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, true,"userToken")).thenReturn(priceClientResponseMap);
        //Then
        Response response = priceHandler.getPrices(CUSTOMER_ID, null, PROJECT_ID, QUOTE_OPTION_ID, LINEITEMS);
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());
        assertFalse(jsonObject.getBoolean("done"));
        verify(futureProductInstanceClient, times(1)).createScopePricing(LINEITEMS);

        //Then
        response = priceHandler.getPrices(CUSTOMER_ID, null, PROJECT_ID, QUOTE_OPTION_ID, LINEITEMS);
        jsonObject = new JSONObject(response.getEntity().toString());
        assertTrue(jsonObject.getBoolean("done"));
        assertEquals(jsonObject.getJSONArray("response").toString(), priceClientResourceMapJson);
        verify(futureProductInstanceClient, times(1)).deleteScopePricing(LINEITEMS);
    }

    @Test
    public void shouldReturnDoneForProcessingItemsWithLineItemErrors() throws Exception {
        //When
        Map<String, PriceClientResponse> priceClientResponseMap = new HashMap<String, PriceClientResponse>();
        String priceClientResourceMapJson = "[{\"errors\":[{\"error\":\"error\",\"lineItemId\":\"item\"}],\"status\":\"Firm\",\"lineItemId\":\"item\"}]";
        ScopePricing scopePricingProcessing = new ScopePricing(LINEITEMS, Lists.<ScopePricingItem>newArrayList());
        scopePricingProcessing.setStatus(ScopePricingStatus.PROCESSING);
        ScopePricing scopePricing = new ScopePricing(LINEITEMS, newArrayList(new ScopePricingItem("item", "Firm", LINEITEMS, Lists.<ScopePricingItemError>newArrayList(new ScopePricingItemError("item", "error")))));
        scopePricing.setStatus(ScopePricingStatus.COMPLETE);
        when(futureProductInstanceClient.getScopePricing(LINEITEMS)).thenThrow(ResourceNotFoundException.class)
            .thenReturn(scopePricingProcessing).thenReturn(scopePricing);
        when(priceHandlerService.processLineItemsForPricing(LINEITEMS, CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, true,"userToken")).thenReturn(priceClientResponseMap);
        //Then
        Response response = priceHandler.getPrices(CUSTOMER_ID, null, PROJECT_ID, QUOTE_OPTION_ID, LINEITEMS);
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());
        assertFalse(jsonObject.getBoolean("done"));
        verify(futureProductInstanceClient, times(1)).createScopePricing(LINEITEMS);

        //Then
        response = priceHandler.getPrices(CUSTOMER_ID, null, PROJECT_ID, QUOTE_OPTION_ID, LINEITEMS);
        jsonObject = new JSONObject(response.getEntity().toString());
        assertTrue(jsonObject.getBoolean("done"));
        assertEquals(jsonObject.getJSONArray("response").toString(), priceClientResourceMapJson);
        verify(futureProductInstanceClient, times(1)).deleteScopePricing(LINEITEMS);
    }

    @Test
    public void shouldHandlePricingErrors() throws Exception {
        //When
        ScopePricing scopePricing = new ScopePricing(LINEITEMS, newArrayList(new ScopePricingItem("item", "Firm", LINEITEMS)));
        scopePricing.setStatus(ScopePricingStatus.ERROR);
        scopePricing.setError("Error text");
        when(futureProductInstanceClient.getScopePricing(LINEITEMS)).thenReturn(scopePricing);

        //Then
        Response response = priceHandler.getPrices(CUSTOMER_ID, null, PROJECT_ID, QUOTE_OPTION_ID, LINEITEMS);
        assertEquals(response.getEntity(), "Error text");
        verify(futureProductInstanceClient, times(1)).deleteScopePricing(LINEITEMS);
    }

}
