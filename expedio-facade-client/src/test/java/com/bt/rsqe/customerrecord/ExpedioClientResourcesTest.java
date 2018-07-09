package com.bt.rsqe.customerrecord;

import com.bt.rsqe.ExpedioFacadeClientEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.utils.Environment;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ExpedioClientResourcesTest {

    private ExpedioClientResources expedioClientResources;

    @Before
    public void before() {
        expedioClientResources = new ExpedioClientResources(ConfigurationProvider.provide(ExpedioFacadeClientEnvironmentTestConfig.class, Environment.env()).getExpedioFacadeConfig());
    }

    @Test
    public void shouldReturnOrderResource() {
        assertThat(expedioClientResources.orderResource(), is(notNullValue()));
    }

    @Test
    public void shouldReturnProjectResource() throws Exception {
        assertThat(expedioClientResources.projectResource(), is(notNullValue()));
    }
}
