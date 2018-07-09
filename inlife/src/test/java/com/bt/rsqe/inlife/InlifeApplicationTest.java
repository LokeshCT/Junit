package com.bt.rsqe.inlife;

import com.bt.rsqe.InlifeEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.inlife.config.InlifeConfig;
import com.bt.rsqe.utils.Environment;
import org.junit.Test;

import java.io.IOException;

public class InlifeApplicationTest {

    @Test
    public void shouldStartAndStopApplication() throws IOException {
        InlifeConfig inlifeConfig = ConfigurationProvider.provide(InlifeEnvironmentTestConfig.class, Environment.env()).getInlifeConfig();
        InlifeApplication inlifeApplication = new InlifeApplication(inlifeConfig);
        inlifeApplication.start();
        inlifeApplication.stop();
    }
}
