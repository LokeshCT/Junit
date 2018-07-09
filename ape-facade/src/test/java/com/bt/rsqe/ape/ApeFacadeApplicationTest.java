package com.bt.rsqe.ape;

import com.bt.rsqe.ApeFacadeEnvironmentTestConfig;
import com.bt.rsqe.ape.config.ApeFacadeConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.container.Cluster;
import com.bt.rsqe.utils.Environment;
import org.junit.Test;
import static com.bt.rsqe.projectengine.web.fixtures.SessionServiceClientResourcesFixture.aFakeSessionService;

public class ApeFacadeApplicationTest {
    @Test
    public void shouldSuccessfullyStartApplicationWithPortFromConfigFile() throws Exception {
        final ApeFacadeConfig apeFacadeConfig = ConfigurationProvider.provide(ApeFacadeEnvironmentTestConfig.class, Environment.env()).getApeFacadeConfig();
        final ApeFacadeApplication application = new ApeFacadeApplication(apeFacadeConfig, aFakeSessionService().build());
        application.becomeMemberOf(new Cluster());
        application.start();
        application.stop();
    }
}
