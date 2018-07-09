package com.bt.rsqe.inlife.web;

import com.bt.rsqe.inlife.geo.locator.GeoLocator;
import com.bt.rsqe.mis.client.WebMetricsResource;
import com.bt.rsqe.mis.client.dto.NavigationName;
import com.bt.rsqe.monitoring.WebMetricsDTO;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.Date;

import static com.bt.rsqe.mis.client.fixtures.NavigationMetricsDTOFixture.*;
import static com.bt.rsqe.mis.client.fixtures.WebClientFixture.*;
import static com.bt.rsqe.mis.client.fixtures.WebMetricsFixture.*;
import static com.google.common.collect.Lists.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WebMetricsResourceHandlerTest {
    private WebMetricsResource webMetricsResource;
    private WebMetricsResourceHandler webMetricsResourceHandler;
    private GeoLocator geoLocator;

    @Before
    public void setup() {
        webMetricsResource = mock(WebMetricsResource.class);
        geoLocator = mock(GeoLocator.class);
        webMetricsResourceHandler = new WebMetricsResourceHandler(webMetricsResource, null, geoLocator);
    }

    @Test
    public void shouldHandleCreateRequest() {
        WebMetricsDTO webMetricsDTO = aWebMetrics().build();
        when(webMetricsResource.post(webMetricsDTO)).thenReturn("123");

        Response response = webMetricsResourceHandler.create(webMetricsDTO);

        verify(webMetricsResource, only()).post(webMetricsDTO);
        assertThat(response.getStatus(), Is.is(CREATED.getStatusCode()));
        assertThat(response.getEntity().toString(), Is.is("123"));
    }

    @Test
    public void shouldReturnErrorResponseIfThereIsAnyExceptionWhileCreatingMetrics() {
        WebMetricsDTO webMetricsDTO = aWebMetrics().build();
        doThrow(new RuntimeException("exception msg")).when(webMetricsResource).post(webMetricsDTO);

        Response response = webMetricsResourceHandler.create(webMetricsDTO);

        assertThat(response.getStatus(), Is.is(INTERNAL_SERVER_ERROR.getStatusCode()));
        assertThat(response.getEntity().toString(), Is.is("exception msg"));
    }

    @Test
    public void shouldHandleGetLocationBasedMetricsRequest() {
        when(webMetricsResource.getLocationBasedMetrics(eq("navName"), any(Date.class), any(Date.class))).thenReturn(aDefaultNavigationMetricsDTO());
        Response response = webMetricsResourceHandler.getLocationBasedMetrics("navName", "12-2-2014", "12-12-2014");
        assertThat(response.getStatus(), Is.is(OK.getStatusCode()));
        verify(webMetricsResource, times(1)).getLocationBasedMetrics(eq("navName"), any(Date.class), any(Date.class));
    }

    @Test
    public void shouldLoadClientLocationBasedOnEIN() {
        WebMetricsDTO webMetricsDTO = aWebMetrics().withEin("1234").build();
        when(geoLocator.locateCountry("1234")).thenReturn("Ind");
        when(webMetricsResource.post(aWebMetrics().withEin("1234").withWebClientFixture(aWebClient().withLocation("Ind")).build())).thenReturn("123");

        Response response = webMetricsResourceHandler.create(webMetricsDTO);

        assertThat(response.getEntity().toString(), Is.is("123"));
        assertThat(response.getStatus(), Is.is(CREATED.getStatusCode()));
        verify(geoLocator, times(1)).locateCountry("1234");
    }

    @Test
    public void shouldNotLoadClientLocationIfEinIsEmpty() {
        WebMetricsDTO webMetricsDTO = aWebMetrics().build();
        when(webMetricsResource.post(webMetricsDTO)).thenReturn("123");

        Response response = webMetricsResourceHandler.create(webMetricsDTO);

        assertThat(response.getEntity().toString(), Is.is("123"));
        assertThat(response.getStatus(), Is.is(CREATED.getStatusCode()));
        verify(geoLocator, never()).locateCountry(anyString());
    }

    @Test
    public void shouldUseClientLocationBeingPassedByClient() {
        WebMetricsDTO webMetricsDTO = aWebMetrics().withEin("1234").withWebClientFixture(aWebClient().withLocation("Ind")).build();
        when(webMetricsResource.post(webMetricsDTO)).thenReturn("123");

        Response response = webMetricsResourceHandler.create(webMetricsDTO);

        assertThat(response.getEntity().toString(), Is.is("123"));
        assertThat(response.getStatus(), Is.is(CREATED.getStatusCode()));
        verify(geoLocator, never()).locateCountry(anyString());
    }

    @Test
    public void shouldReturnErrorCodeForInvalidDate() throws ParseException {
        Response response = webMetricsResourceHandler.getLocationBasedMetrics("navName", "12-12w-23", "12-12w-23");
        assertThat(response.getStatus(), Is.is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    @Test
    public void shouldReturnBadRequestIfNavNameIsEmpty() throws ParseException {
        Response response = webMetricsResourceHandler.getLocationBasedMetrics("", "12-12-2014", "12-12-2014");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnBadRequestIfDateIsEmpty() throws ParseException {
        Response response = webMetricsResourceHandler.getLocationBasedMetrics("NavName", "", "12-12-2014");
        assertThat(response.getStatus(), Is.is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldHandleGetNavigationsRequest() {
        when(webMetricsResource.getNavigationList()).thenReturn(newArrayList(new NavigationName("site management")));
        Response response = webMetricsResourceHandler.getNavigations();
        assertThat(response.getStatus(), Is.is(OK.getStatusCode()));
        verify(webMetricsResource, times(1)).getNavigationList();
        assertThat(response.getEntity().toString(), Is.is("[{\"name\":\"site management\"}]"));
    }
}
