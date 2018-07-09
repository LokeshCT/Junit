package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.SqeAccessInput;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.domain.ClassPathResource;
import com.bt.rsqe.utils.RsqeCharset;
import com.google.gson.Gson;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.bt.rsqe.ape.matchers.SqeAccessInputMatcher.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MultipleProvideQuoteStrategyTest {
    private APEClient apeClient;

    @Before
    public void setup() {
        apeClient  = mock(APEClient.class);
    }

    @Test
    public void shouldTransformToApeRequest() throws IOException {
        SqeAccessInput sqeAccessInput = new MultipleProvideQuoteStrategy(getRequest("com/bt/rsqe/ape/qref-creation-request.json"), "http://synchUri").transform();

        assertThat(sqeAccessInput, aSqeAccessInput()
            .withAutoSelection("1")
            .withSiteDetails("siteName", "city", "country", "BT1 1GJ", "", 1111D, 2222D, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "12345", true,1)
            .withPrimaryPortSpeed("1024Kbps")
            .withUserDetails("", "surname", 111111, "sales.user@bt.com", "BT UK")
            .withSynchUri("http://synchUri")
            .withContractTerm("1"));

        assertThat(sqeAccessInput.getQuoteInput()[0].getListOfOnNetBuildingCodes().length, is(1));
    }

    @Test
    public void shouldTransformToApeRequestWithCorrectAccessTechnology() throws IOException {
        SqeAccessInput sqeAccessInput = new MultipleProvideQuoteStrategy(getRequest("com/bt/rsqe/ape/qref-creation-with-access-technology-request.json"), "http://synchUri").transform();

        assertThat(sqeAccessInput, aSqeAccessInput()
            .withAutoSelection("1")
            .withPrimaryPortSpeed("2 (T3)Mbps")
            .withPrimaryAccessTechnology("T3")
            .withSiteDetails("siteName", "city", "country", "BT1 1GJ", "", 1111D, 2222D, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "12345", false,1)
            .withUserDetails("", "surname", 111111, "sales.user@bt.com", "BT UK")
            .withSynchUri("http://synchUri")
            .withContractTerm("1"));
    }

    @Test
    public void shouldTransformStandardQrefCreationRequest() throws IOException {
        SqeAccessInput sqeAccessInput = new MultipleProvideQuoteStrategy(getRequest("com/bt/rsqe/ape/standard-qref-creation-request.json"), "http://synchUri").transform();

        assertThat(sqeAccessInput, aSqeAccessInput()
            .withAutoSelection("1")
            .withSiteDetails("siteName", "city", "country", "BT1 1GJ", "", 1111D, 2222D, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "12345", true,1)
            .withPrimaryPortSpeed("2Mbps")
            .withSecondaryPortSpeed("-1")
            .withPrimaryAccessSpeed("4Mbps")
            .withSecondaryAccessSpeed("-1Kbps")
            .withPrimaryAccessTechSubType("1 Gbps (Ethernet)")
            .withUserDetails("", "surname", 111111, "sales.user@bt.com", "BT UK")
            .withSynchUri("http://synchUri")
            .withContractTerm("1"));
    }

    @Test
    public void shouldTransformToResilientApeRequest() throws IOException {
        SqeAccessInput sqeAccessInput = new MultipleProvideQuoteStrategy(getRequest("com/bt/rsqe/ape/resilient-qref-creation-request.json"), "http://synchUri").transform();

        assertThat(sqeAccessInput.getQuoteInput()[0].getPortSpeed1(), Is.is("2"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getPortSpeed2(), Is.is("4"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getPortSpeedUom1(), Is.is("Mbps"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getPortSpeedUom2(), Is.is("Tbps"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getAccessSpeed1(), Is.is("12"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getAccessSpeed2(), Is.is("14"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getAccessSpeedUom1(), Is.is("Kbps"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getAccessSpeedUom2(), Is.is("Gbps"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getAccess_Type_Name(), Is.is("Primary Leased Line"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getLeg2_Access_Type_Name(), Is.is("Secondary Leased Line"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getResiliencyType(), Is.is("Secure"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getProductSLA(), Is.is("Secure"));
        assertThat(sqeAccessInput.getQuoteInput()[0].isResilient(), Is.is(true));
        assertThat(sqeAccessInput.getQuoteInput()[0].getAccessTechnology(), Is.is("1 Gbps (Ethernet)"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getAccessTechnology2(), Is.is("N x 64"));
        assertThat(sqeAccessInput.getQuoteInput()[0].getFastconvergence(), Is.is(""));
        assertThat(sqeAccessInput.getQuoteInput()[0].getListOfOnNetBuildingCodes().length, is(0));
    }

    @Test
    public void shouldGetMultisiteResponse() throws Exception {
        String syncUri = "http://syncUri";
        MultipleProvideQuoteStrategy provideQuoteStrategy = new MultipleProvideQuoteStrategy(getRequest("com/bt/rsqe/ape/qref-creation-request.json"), syncUri);
        SqeAccessInput sqeAccessInput = provideQuoteStrategy.transform();
        when(apeClient.multipleProvideQuote(sqeAccessInput)).thenReturn(new MultisiteResponse());
        MultisiteResponse response = provideQuoteStrategy.getMultiSiteResponse(apeClient);
        assertThat(response, is(new MultisiteResponse()));
    }

    private ApeQrefRequestDTO getRequest(String jsonFilePath) throws IOException {
        String requestJson = new ClassPathResource(jsonFilePath).textContent(RsqeCharset.defaultCharset());
        ApeQrefRequestDTO requestDTO = new Gson().fromJson(requestJson, ApeQrefRequestDTO.class);
        return requestDTO;
    }
}
