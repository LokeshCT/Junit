package com.bt.rsqe.projectengine.web.productconfigurator;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.configuration.ProductConfiguratorConfig;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.utils.ProductConfiguratorConfigUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class ProductConfiguratorUriFactoryImplTest {

    private UriFactoryImpl configuratorConfigTest;

ProductConfiguratorConfigUtils pcUtils;
    @Before
    public void setUp() throws Exception {
        configuratorConfigTest = new UriFactoryImpl(
            ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig());


        ProductConfiguratorConfig productConfiguratorConfig = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig().getProductConfiguratorConfig();
        pcUtils  = new ProductConfiguratorConfigUtils(productConfiguratorConfig.getProducts());
    }

    @Test
    public void shouldReturnPCConfigUrlForDefaultProduct() throws Exception {
        final String lineItemId = "123";
        final String customerId = "customerId";
        final String contractId = "contractId";
        final String projectId = "projectId";
        final String quoteOptionId = "quoteOptionId";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("status", "status");
        parameters.put("readOnly", "readOnly");
        String match  = pcUtils.getDefaultUriForContext("CONFIG");
        assertThat(configuratorConfigTest.getConfigurationUri("S1234", customerId, contractId, projectId, quoteOptionId, lineItemId, parameters),
                   is(String.format(match + "?readOnly=readOnly&amp;status=status",
                                    customerId, contractId, projectId, quoteOptionId, lineItemId, lineItemId)));
    }

    @Test
    public void shouldReturnPCUrlForDefaultProduct() throws Exception {
        final String result = configuratorConfigTest.getLineItemCreationUri("S1234", "customerId", "contractId", "projectId");
        String def = pcUtils.getDefaultUriForContext("CREATE");
        assertThat(result, is(        String.format(def,"customerId","contractId","projectId")        ));
    }

    @Test
    public void shouldReturnBulkViewUris() {
        String def = pcUtils.getDefaultUriForContext("BULK_VIEW");

        assertThat(configuratorConfigTest.getBulkViewUri("customer", "contract", "project", "quoteOption"), is(String.format(def,"customer","contract","project","quoteOption")));
    }


    @Test
    public void shouldReturnLocateOnGoogleMapsUris() {
        String def = pcUtils.getDefaultUriForContext("LOCATE_ON_GOOGLE_MAPS");

        assertThat(configuratorConfigTest.getLocateOnGoogleMapsViewUri("customer", "contract", "project", "quoteOption"),
                   is(String.format(def,"customer","contract","project","quoteOption")));
    }

    @Test
    public void shouldReturnQuoteLaunchUri() {
        String def = "/rsqe/customers/%s/contracts/%s/projects/%s";

        assertThat(configuratorConfigTest.getQuoteLaunchUri("customer", "contract", "project"),
                   is(String.format(def,"customer","contract","project")));
    }

}
