package com.bt.rsqe.customerrecord;

import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import javax.ws.rs.core.GenericType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class CustomerResourceCacheTest {

    private static final String CUSTOMER_ID = "customerId";
    private static final String EXPEDIO_QUOTE_ID = "expedioQuoteId";
    private static final String SITE_ID = "123";
    private static final String SITE_ID1 = "124";
    private static final String SITE_ID2 = "125";
    private static final URI EXPEDIO_URI = URI.create("http://expedioURI");
    private RestResource mockResource;
    private RestRequestBuilder stubRequestBuilder;
    private CustomerResource customerResource;
    private RestResponse mockResponse;
    private SiteDTO ipSwitchSite;

    @Before
    public void setUp() {
        mockResource = mock(RestResource.class);
        mockResponse = mock(RestResponse.class);
        when(mockResource.get()).thenReturn(mockResponse);

        stubRequestBuilder = new RestRequestBuilder(EXPEDIO_URI) {
            @Override
            public RestResource build(String[] segments, Map<String, String> queryParams) {
                return mockResource;
            }
        };

        customerResource = new CustomerResource(stubRequestBuilder, EXPEDIO_URI, "secret");

        ipSwitchSite = new SiteDTO(SITE_ID, "Ipswich");
        when(mockResponse.getEntity(Matchers.<GenericType>anyObject())).thenReturn(ipSwitchSite);
        customerResource.findSiteById(SITE_ID, EXPEDIO_QUOTE_ID, CUSTOMER_ID);
    }

    @Test
    public void shouldFetchASiteForACustomerAndQuoteOption() throws Exception {

        final SiteDTO site = customerResource.findSiteById(SITE_ID, EXPEDIO_QUOTE_ID, CUSTOMER_ID);
        assertThat(site, is(ipSwitchSite));

        verify(mockResource).get();
    }

    @Test
    public void shouldFetchSiteFromCacheIfSameParametersPassedInRequest() throws Exception {
        //Given I have fetched a site twice with the same parameters...
        final SiteDTO response1 = customerResource.findSiteById(SITE_ID, EXPEDIO_QUOTE_ID, CUSTOMER_ID);
        final SiteDTO response2 = customerResource.findSiteById(SITE_ID, EXPEDIO_QUOTE_ID, CUSTOMER_ID);

        assertThat(response1, is(response2));

        //..I expect the result to come from a cache, and not fetch from the external resource
        verify(mockResource, times(0)).get();
    }

    @Test
    public void shouldFetchFromExternalResourceForDifferentSiteId() throws Exception {

        final String someSiteId = "1234";
        final String someOtherSiteId = "567";

        final SiteDTO bangaloreSite = new SiteDTO(someOtherSiteId, "Bangalore");

        when(mockResponse.getEntity(Matchers.<GenericType>anyObject()))
            .thenReturn(ipSwitchSite)
            .thenReturn(bangaloreSite);

        //Given I have fetched a site twice but for different sites
        final SiteDTO response1 = customerResource.findSiteById(someSiteId, EXPEDIO_QUOTE_ID, CUSTOMER_ID);
        final SiteDTO response2 = customerResource.findSiteById(someOtherSiteId, EXPEDIO_QUOTE_ID, CUSTOMER_ID);

        assertThat(response1, is(ipSwitchSite));
        assertThat(response2, is(bangaloreSite));

        //..I expect two requests to be made
        verify(mockResource, times(2)).get();
    }

    @Test
    public void shouldFetchFromExternalResourceOneTimesEvenRequestedSiteForDifferentCustomerId() throws Exception {

        final SiteDTO ipswichSite = new SiteDTO(SITE_ID1, "Ipswich");
        final SiteDTO bangaloreSite = new SiteDTO(SITE_ID1, "Bangalore");

        when(mockResponse.getEntity(Matchers.<GenericType>anyObject()))
            .thenReturn(ipswichSite)
            .thenReturn(bangaloreSite);

        //Given I have fetched a site twice but for different customers
        final SiteDTO response1 = customerResource.findSiteById(SITE_ID1, EXPEDIO_QUOTE_ID, "otherCustomerId");
        final SiteDTO response2 = customerResource.findSiteById(SITE_ID1, EXPEDIO_QUOTE_ID, "anotherCustomerId");

        assertThat(response1, is(ipswichSite));
        assertThat(response2, is(bangaloreSite));

        //..I expect one requests to be made as site is cached based on siteId
        verify(mockResource, times(1)).get();
    }

    @Test
    public void shouldFetchFromExternalResourceOneTimesEvenRequestedSiteForDifferentQuoteOptionId() throws Exception {

        final SiteDTO ipswichSite = new SiteDTO(SITE_ID2, "Ipswich");
        final SiteDTO bangaloreSite = new SiteDTO(SITE_ID2, "Bangalore");

        when(mockResponse.getEntity(Matchers.<GenericType>anyObject()))
            .thenReturn(ipswichSite)
            .thenReturn(bangaloreSite);

        //Given I have fetched a site twice but for different quote option ids
        final SiteDTO response1 = customerResource.findSiteById(SITE_ID2, "someQuoteOptionId", CUSTOMER_ID);
        final SiteDTO response2 = customerResource.findSiteById(SITE_ID2, "differentQuoteOptionId", CUSTOMER_ID);

        assertThat(response1, is(ipswichSite));
        assertThat(response2, is(bangaloreSite));

        //..I expect one requests to be made as site is cached based on siteId
        verify(mockResource, times(1)).get();
    }

}
