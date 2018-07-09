package com.bt.rsqe.inlife.web;

import com.bt.rsqe.domain.ClassPathResource;
import com.bt.rsqe.inlife.geo.locator.btdirectory.BTDirectoryBasedGeoLocator;
import com.bt.rsqe.inlife.geo.locator.GeoLocator;
import com.bt.rsqe.utils.RsqeCharset;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilderFixture;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BTDirectoryBasedGeoLocatorTest {

    @Test
    public void shouldLocateCountryForValidEin() throws URISyntaxException, IOException {
        RestRequestBuilder restRequestBuilder = RestRequestBuilderFixture.aRequest().withEntity(validResponse()).withMethod(HttpMethod.GET).build();
        String country = new BTDirectoryBasedGeoLocator(restRequestBuilder).locateCountry("608308027");
        assertThat(country, Is.is("India"));
    }

    @Test
    public void shouldLocateCountryForValidWFHEin() throws URISyntaxException, IOException {
        RestRequestBuilder restRequestBuilder = RestRequestBuilderFixture.aRequest().withEntity(validWFHResponse()).withMethod(HttpMethod.GET).build();
        String country = new BTDirectoryBasedGeoLocator(restRequestBuilder).locateCountry("608308027");
        assertThat(country, Is.is("US"));
    }

    @Test
    public void shouldReturnUnknownForInvalidEin() throws URISyntaxException, IOException {
        RestRequestBuilder restRequestBuilder = RestRequestBuilderFixture.aRequest().withEntity(inValidResponse()).withMethod(HttpMethod.GET).build();
        String country = new BTDirectoryBasedGeoLocator(restRequestBuilder).locateCountry("60830802712121");
        assertThat(country, Is.is(GeoLocator.UNKNOWN_COUNTRY));
    }

    @Test
    public void shouldReturnUnknownIfAnyExceptionWhileInvokingBtDirectoryService() throws URISyntaxException, IOException {
        RestRequestBuilder restRequestBuilder = RestRequestBuilderFixture.aRequest().thatThrows(new RuntimeException()).build();
        String country = new BTDirectoryBasedGeoLocator(restRequestBuilder).locateCountry("60830802712121");
        assertThat(country, Is.is(GeoLocator.UNKNOWN_COUNTRY));
    }

    @Test
    public void shouldReturnUnknownIfMalformedResponseReturnedFromBtDirectory() throws URISyntaxException, IOException {
        RestRequestBuilder restRequestBuilder = RestRequestBuilderFixture.aRequest().withEntity(malformedResponse()).withMethod(HttpMethod.GET).build();
        String country = new BTDirectoryBasedGeoLocator(restRequestBuilder).locateCountry("60830802712121");
        assertThat(country, Is.is(GeoLocator.UNKNOWN_COUNTRY));
    }

    @Test
    public void shouldCacheCountry() throws IOException {
        RestRequestBuilder restRequestBuilder = RestRequestBuilderFixture.aRequest().withEntity(validResponse()).withMethod(HttpMethod.GET).build();
        BTDirectoryBasedGeoLocator btDirectoryBasedGeoLocator = new BTDirectoryBasedGeoLocator(restRequestBuilder);
        btDirectoryBasedGeoLocator.locateCountry("608308027");
        btDirectoryBasedGeoLocator.locateCountry("608308027");
        btDirectoryBasedGeoLocator.locateCountry("608308027");

       verify(restRequestBuilder, times(1)).build(new HashMap<String, String>(){{put("ein", "608308027");}});
    }

    private String validResponse() throws IOException {
        return new ClassPathResource("com/bt/rsqe/inlife/web/geo/locator/btdirectory/people.xml").textContent(RsqeCharset.defaultCharset());
    }

    private String validWFHResponse() throws IOException {
        return new ClassPathResource("com/bt/rsqe/inlife/web/geo/locator/btdirectory/wfh-address-response.xml").textContent(RsqeCharset.defaultCharset());
    }

    private String inValidResponse() throws IOException {
        return "<people></people>";
    }

    private String malformedResponse() throws IOException {
        return "dkfjdflkdjfd";
    }
}
