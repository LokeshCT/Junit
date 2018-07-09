package com.bt.rsqe.ape;

import com.bt.cqm.client.SACAvailabilityCheckerClient;
import com.bt.rsqe.ape.config.SupplierCheckConfig;
import com.bt.rsqe.ape.config.TimeoutConfig;
import com.bt.rsqe.ape.dto.Supplier;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.source.processor.RequestBuilder;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.utils.GsonUtil;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

public class SupplierProductResourceHandlerTest {

    private SupplierCheckConfig config;
    private SupplierProductResourceHandler supplierProductResourceHandler;
    private APEQrefJPARepository apeQrefRepository;
    private CustomerResource customerResource;
    private RequestBuilder requestBuilder;
    private TimeoutConfig apeServiceTimeoutConfig;
    private SACAvailabilityCheckerClient sacAvailabilityCheckerClient;
    private SupplierProductResourceClient supplierProductResourceClient;
    private ApeOnnetBuildingResourceHandlerClient apeOnnetBuildingResourceHandlerClient;
    private ExecutorService executor;
    @Before
    public void setUp() {
        config = mock(SupplierCheckConfig.class);
        apeQrefRepository = mock(APEQrefJPARepository.class);
        customerResource = mock(CustomerResource.class);
        requestBuilder = mock(RequestBuilder.class);
        apeServiceTimeoutConfig = mock(TimeoutConfig.class);
        supplierProductResourceClient = mock(SupplierProductResourceClient.class);
        apeOnnetBuildingResourceHandlerClient =mock(ApeOnnetBuildingResourceHandlerClient.class);
        executor = mock(ExecutorService.class);
        supplierProductResourceHandler = new SupplierProductResourceHandler(config,executor, supplierProductResourceClient,customerResource,sacAvailabilityCheckerClient, requestBuilder, apeOnnetBuildingResourceHandlerClient, null);

    }

    @Ignore("This can be used as client to hit rest service and see actual result")
    @Test
    public void shouldInvokeGetSupplierProductSitesService() {
        URI uri = UriBuilder.fromUri("http://172.31.209.144:9987/rsqe/ape-facade/supplier-check-service/get-supplier-product-sites-for-customer/customer/").build();
        RestResource restResource = new RestRequestBuilder(uri).withSecret("rsqe-secret").build(new HashMap<String, String>() {{
            put("customer", "1234");
        }});
        RestResponse restResponse = restResource.post();
    }

    @Test
    public void shouldReturnSupplierProductSitesForCustomer() {
        String customerId = "11234";
        Response response = supplierProductResourceHandler.getSupplierProductSitesForCustomer(customerId);
        //List<SupplierSite> responseSites = (List<SupplierSite>) response.getEntity();
       // assertThat(responseSites.size(), is(not(0)));
    }

    @Test
    public void shouldReturnSupplierProductSites() {
        List<SupplierSite> supplierSiteList = Lists.newArrayList();
        supplierSiteList.add(new SupplierSite(1L, "siteName", "countryISOCode","city", "countryName", new Date(), 1,"9998338849","errorDescription", "status", new ArrayList<Supplier>()));
        JsonObject obj=GsonUtil.toJson(buildRequestObject());
        Response response = supplierProductResourceHandler.getSupplierProductSites(buildRequestObject());
        //assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
       // List<SupplierSite> responseSites = (List<SupplierSite>) response.getEntity();
        //assertThat(responseSites.size(), is(not(0)));
    }

    private SupplierCheckRequest buildRequestObject() {
        List<SupplierSite> supplierSiteList = Lists.newArrayList();
        supplierSiteList.add(new SupplierSite(1L, "siteName", "countryISOCode","city", "countryName", new Date(), 1,"9998338849","errorDescription", "status", new ArrayList<Supplier>()));
        supplierSiteList.add(new SupplierSite(1L, "siteName", "countryISOCode","city", "countryName", new Date(), 1,"9998338849","errorDescription", "status", new ArrayList<Supplier>()));
        return new SupplierCheckRequest("requestId","parentRequestId","customerId","user", "syncUri", "clientCallbackUri", "status","level", "autoTrigger", "triggerType", "sourceSystemName","errorDescription", supplierSiteList, null);
    }

}