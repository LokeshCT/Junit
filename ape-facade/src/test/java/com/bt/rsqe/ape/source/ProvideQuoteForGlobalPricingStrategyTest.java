package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.SqeAccessInputDetails;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.ClassPathResource;
import com.bt.rsqe.utils.RsqeCharset;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static com.bt.rsqe.ape.matchers.SqeAccessInputDetailsMatcher.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProvideQuoteForGlobalPricingStrategyTest {
    private APEClient apeClient;

    @Before
    public void setup() {
        apeClient  = mock(APEClient.class);
    }

    @Test
    public void shouldTransformToMBPApeRequest() throws IOException {
        SqeAccessInputDetails sqeAccessInput = new ProvideQuoteForGlobalPricingStrategy(getRequest("com/bt/rsqe/ape/qref-creation-with-mbp-scenario-request.json"), "http://synchUri", null).transform();

        assertThat(sqeAccessInput, aSqeAccessInputDetail()
            .withAutoSelection("1")
            .withSiteDetails("siteName", "city", "country", "BT1 1GJ", "", 1111D, 2222D, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", new String[]{"12345"}, false,1)
            .withPrimaryPortSpeed("2Mbps")
            .withPrimaryAccessTechnology("")
            .withMBPFlag("MBP")
            .withQuoteType("Price")
            .withUserDetails("", "surname", 111111, "sales.user@bt.com", "BT UK")
            .withSynchUri("http://synchUri")
            .withContractTerm("1"));
    }

    @Test
    public void shouldGetMultisiteResponse() throws Exception {
        String syncUri = "http://syncUri";
        ProvideQuoteForGlobalPricingStrategy provideQuoteForGlobalPricingStrategy = new ProvideQuoteForGlobalPricingStrategy(getRequest("com/bt/rsqe/ape/qref-creation-request.json"), syncUri, null);
        SqeAccessInputDetails sqeAccessInput = provideQuoteForGlobalPricingStrategy.transform();
        when(apeClient.provideQuoteForGlobalPricing(sqeAccessInput)).thenReturn(new MultisiteResponse());
        MultisiteResponse response = provideQuoteForGlobalPricingStrategy.getMultiSiteResponse(apeClient);
        assertThat(response, is(new MultisiteResponse()));
    }


    @Test
    public void shouldGetMultisiteResponseForInterimSite() throws Exception {
        String syncUri = "http://syncUri";
        ProvideQuoteForGlobalPricingStrategy provideQuoteForGlobalPricingStrategy = new ProvideQuoteForGlobalPricingStrategy(getRequest("com/bt/rsqe/ape/ape_interim_site_request.json"), syncUri, null);
        SqeAccessInputDetails sqeAccessInput = provideQuoteForGlobalPricingStrategy.transform();
        when(apeClient.provideQuoteForGlobalPricing(sqeAccessInput)).thenReturn(new MultisiteResponse());
        MultisiteResponse response = provideQuoteForGlobalPricingStrategy.getMultiSiteResponse(apeClient);
        assertThat(response, is(new MultisiteResponse()));
    }

    @Test
    public void shouldTransformToApeRequestForAttributeMultiValues() throws IOException {
        SqeAccessInputDetails sqeAccessInput = new ProvideQuoteForGlobalPricingStrategy(getRequest("com/bt/rsqe/ape/qref-creation-with-multi_values-request.json"), "http://synchUri",null).transform();

        assertThat(sqeAccessInput, aSqeAccessInputDetail()
            .withAutoSelection("1")
            .withSiteDetails("siteName", "city", "country", "BT1 1GJ", "", 1111D, 2222D, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", new String[]{"12345","87345"}, false,1)
            .withPrimaryPortSpeed("2Mbps")
            .withPrimaryAccessTechnology("")
            .withMBPFlag("MBP")
            .withQuoteType("Price")
            .withUserDetails("", "surname", 111111, "sales.user@bt.com", "BT UK")
            .withSynchUri("http://synchUri")
            .withContractTerm("1")
            .withAttributeMultiValues("PRIMARY GPOP INCLUDE",new String[]{"Bangor VPN POP","Belfast VPN POP","Birmingham VPN POP","Chelmsford VPN POP"})
        );
    }

    private ApeQrefRequestDTO getRequest(String jsonFilePath) throws IOException {
        String requestJson = new ClassPathResource(jsonFilePath).textContent(RsqeCharset.defaultCharset());
        ApeQrefRequestDTO requestDTO = new Gson().fromJson(requestJson, ApeQrefRequestDTO.class);
        return requestDTO;
    }
}
