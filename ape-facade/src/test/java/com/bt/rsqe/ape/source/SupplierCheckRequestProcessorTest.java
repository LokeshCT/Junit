package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.ApeOnnetBuildingResourceHandlerClient;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.source.processor.RequestBuilder;
import com.bt.rsqe.ape.source.processor.SupplierCheckRequestProcessor;
import com.bt.rsqe.customerrecord.CustomerResource;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class SupplierCheckRequestProcessorTest {

    private APEQrefJPARepository apeRepository;
    private CustomerResource customerResource;
    private SupplierCheckConfig config;
    private SupplierCheckRequestProcessor processor;
    private RequestBuilder requestBuilder;
    private ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient;
    private String status;

    @Before
    public void setup() {
        apeRepository = mock(APEQrefJPARepository.class);
        customerResource = mock(CustomerResource.class);
        config = mock(SupplierCheckConfig.class);
        requestBuilder = mock(RequestBuilder.class);
        apeOnnetBuildingResourceHandlerClient =mock(ApeOnnetBuildingResourceHandlerClient.class) ;
        processor = new SupplierCheckRequestProcessor(config, customerResource, requestBuilder, apeOnnetBuildingResourceHandlerClient, null);
    }

    @Test
    public void shouldReturnFailureIfFailedFromApe() throws Exception {
        String response = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <s:Body>\n" +
                "      <GetSupplierProductListResponse xmlns=\"http://tempuri.org/\">\n" +
                "         <GetSupplierProductListResult xmlns:a=\"http://schemas.datacontract.org/2004/07/DSLService1\"\n" +
                "                                       xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "            <a:status>[Failure] :: Request for ARQTest619 has not been queued</a:status>\n" +
                "            <a:description>Failure in processing the request</a:description>\n" +
                "         </GetSupplierProductListResult>\n" +
                "      </GetSupplierProductListResponse>\n" +
                "   </s:Body>\n" +
                "</s:Envelope>";
        status = processor.validateAcknowledgementResponse(response, null, "GetSupplierProductListResult");
        assertThat(status, either(containsString("Failure")));
    }

    @Test
    public void shouldReturnSuccessIfSuccessFromApe() throws Exception {
        String response = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <s:Body>\n" +
                "      <GetAvailabiltyResponse xmlns=\"http://tempuri.org/\">\n" +
                "         <GetAvailabiltyResult xmlns:a=\"http://schemas.datacontract.org/2004/07/DSLService1\"\n" +
                "                               xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "            <a:status>[Success]:: Request for ARQTest616 has been queued succussfully</a:status>\n" +
                "            <a:description/>\n" +
                "         </GetAvailabiltyResult>\n" +
                "      </GetAvailabiltyResponse>\n" +
                "   </s:Body>\n" +
                "</s:Envelope>";
        status = processor.validateAcknowledgementResponse(response, null, "GetAvailabiltyResult");
        assertThat(status, either(containsString("Success")));
    }

    @Test
    public void shouldFailfFailedFromApe() throws Exception {
        String response = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <s:Body>\n" +
                "      <GetSupplierProductListResponse xmlns=\"http://tempuri.org/\">\n" +
                "         <GetSupplierProductListResult xmlns:a=\"http://schemas.datacontract.org/2004/07/DSLService1\"\n" +
                "                                       xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "            <a:status>[Failure] :: Request for ARTest8 has not been queued</a:status>\n" +
                "            <a:description>Failure in processing the request</a:description>\n" +
                "         </GetSupplierProductListResult>\n" +
                "      </GetSupplierProductListResponse>\n" +
                "   </s:Body>\n" +
                "</s:Envelope>";
        status = processor.validateAcknowledgementResponse(response, null, "GetSupplierProductListResult");
        assertThat(status, either(containsString("Failure")));
    }
}