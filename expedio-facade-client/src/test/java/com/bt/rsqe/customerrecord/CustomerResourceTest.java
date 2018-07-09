package com.bt.rsqe.customerrecord;

import com.bt.rsqe.utils.RSQEMockery;
import com.bt.rsqe.web.ClientResponseStub;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.GenericEntity;
import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CustomerResourceTest {

    private CustomerResource customerResource;
    private URI lastCall;
    private Mockery mockery;
    private RestResource restResource;

    @Before
    public void setUp() throws Exception {
        mockery = new RSQEMockery();
        restResource = mockery.mock(RestResource.class);
        final URI uri = new URI("uri");
        customerResource = new CustomerResource(new ProxyAwareRestRequestBuilder(uri) {
            @Override
            protected RestResource build(URI uri) {
                lastCall = uri;

                return restResource;
            }
        }, uri, "secret");
    }

    @Test
    public void shouldReturnAPriceBookResourceForAClient() throws Exception {
        final PriceBookResource priceBookResource = customerResource.priceBookResource("customerId");
        assertNotNull(priceBookResource);
    }

    @Test
    public void shouldReturnAccountManagerResourceForAClient() throws Exception {
        final AccountManagerResource accountManagerResource = customerResource.accountManagerResource("customerId", "quoteId");
        assertNotNull(accountManagerResource);
    }

    @Test
    public void shouldReturnBillingAccountResourceForACustomerId() {
        mockery.checking(new Expectations() {{
            allowing(restResource).get();
            will(returnValue(ClientResponseStub.ok(new GenericEntity<List<BillingAccountDTO>>(Lists.<BillingAccountDTO>newArrayList()) {
            })));
        }});
        customerResource.billingAccounts("customerId");
        assertThat(lastCall.toString(), is("uri/customerId/billing-accounts?currency="));
        customerResource.billingAccounts("customerId", "USD");
        assertThat(lastCall.toString(), is("uri/customerId/billing-accounts?currency=USD"));
    }
}
