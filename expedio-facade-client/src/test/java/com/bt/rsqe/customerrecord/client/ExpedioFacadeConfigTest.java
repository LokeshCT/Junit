package com.bt.rsqe.customerrecord.client;

import com.bt.rsqe.ExpedioFacadeClientEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.utils.Environment;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.text.StringEndsWith.endsWith;
import static org.junit.Assert.*;

public class ExpedioFacadeConfigTest {
    private ExpedioFacadeClientEnvironmentTestConfig rsqeConfig;

    @Before
    public void before() {
        rsqeConfig = ConfigurationProvider.provide(ExpedioFacadeClientEnvironmentTestConfig.class, Environment.env());
    }

    @Test
    public void shouldLoadApplicationConfig() throws Exception {
        final ApplicationConfig config = rsqeConfig
            .getExpedioFacadeConfig().getApplicationConfig();
        assertThat(config.getHost(), is("127.0.0.1"));
        assertThat(String.valueOf(config.getPort()), endsWith("9997"));
        assertThat(config.getScheme(), is("http"));
    }
}
