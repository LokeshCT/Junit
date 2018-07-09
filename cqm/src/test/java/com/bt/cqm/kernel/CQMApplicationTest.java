package com.bt.cqm.kernel;

import com.bt.rsqe.CqmEnvironmentConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.utils.Environment;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class CQMApplicationTest {

    @Test
    @Ignore
    public void shouldStartAndStopCqmApplication() throws IOException {
        CqmEnvironmentConfig environmentConfig = ConfigurationProvider.provide(CqmEnvironmentConfig.class, Environment.env());
        CQMApplication cqmApplication = new CQMApplication(environmentConfig.getCqmConfig());
        cqmApplication.start();
        cqmApplication.stop();
    }
}
