package com.bt.rsqe.inlife;

import com.bt.rsqe.InlifeEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.inlife.config.InlifeConfig;
import com.bt.rsqe.utils.Environment;
import org.junit.Test;

import java.io.IOException;

public class StatsApplicationTest {

    @Test
    public void shouldStartAndStopApplication() throws IOException {
        InlifeConfig inlifeConfig = ConfigurationProvider.provide(InlifeEnvironmentTestConfig.class, Environment.env()).getInlifeConfig();
        StatsApplication statsApplication = new StatsApplication(inlifeConfig);
        statsApplication.start();
        statsApplication.stop();
    }
}
