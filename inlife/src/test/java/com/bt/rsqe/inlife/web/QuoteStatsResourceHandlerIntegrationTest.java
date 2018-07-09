package com.bt.rsqe.inlife.web;

import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.mis.client.DateFilter;
import com.bt.rsqe.mis.client.QuoteItemStatsResource;
import com.bt.rsqe.mis.client.QuoteStatsResource;
import com.bt.rsqe.mis.client.TimeRange;
import com.bt.rsqe.mis.client.dto.QuoteItemStatsDTO;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import static org.mockito.Mockito.*;

public class QuoteStatsResourceHandlerIntegrationTest {

    private static final String PRODUCT_GROUP = "productGroup";
    private ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();

    private QuoteStatsResource quoteStatsResource = mock(QuoteStatsResource.class);
    private QuoteItemStatsResource quoteItemStatsResource = mock(QuoteItemStatsResource.class);
    private Application application;
    private DateFilter dateFilter;


    @Before
    public void setup() throws IOException {
        dateFilter = mock(DateFilter.class);
        when(quoteItemStatsResource.quoteItemStatus(PRODUCT_GROUP)).thenReturn(dateFilter);
        when(dateFilter.forPeriod(Matchers.<TimeRange>any())).thenReturn(dateFilter);
        when(dateFilter.get()).thenReturn(Lists.<QuoteItemStatsDTO>newArrayList());

        application = ContainerUtils.startContainer(applicationConfig, new QuoteStatsResourceHandler(quoteStatsResource, quoteItemStatsResource));
    }

    @Test
    public void shouldReceiveEnumTypeAsQueryParameter() {
        URI uri = UriBuilder.buildUri(applicationConfig, "rsqe", "inlife", "stats", "quote-item-stats");
        RestResource restResource = new RestRequestBuilder(uri).build(new HashMap<String, String>() {{
            put("product", PRODUCT_GROUP);
            put("dateRange", "Yesterday");
        }});

        restResource.get();

        verify(quoteItemStatsResource, times(1)).quoteItemStatus(PRODUCT_GROUP);
        verify(dateFilter, times(1)).forPeriod(TimeRange.Yesterday);
        verify(dateFilter, times(1)).get();
    }


    @After
    public void tearDown() throws IOException {
        application.stop();
    }

}
