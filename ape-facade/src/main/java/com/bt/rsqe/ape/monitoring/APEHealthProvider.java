package com.bt.rsqe.ape.monitoring;

import com.bt.rsqe.ape.config.ApeFacadeConfig;
import com.bt.rsqe.monitoring.HealthStatus;
import com.bt.rsqe.monitoring.HttpGetHealthProvider;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.*;

public class APEHealthProvider extends HttpGetHealthProvider {

    private final ApeFacadeConfig apeFacadeConfig;

    public APEHealthProvider(ApeFacadeConfig apeFacadeConfig) {
        this.apeFacadeConfig = apeFacadeConfig;
    }

    @Override
    public HealthStatus getStatus() {
        String serviceEndpoint1 = apeFacadeConfig.getApeServiceEndPointConfig("ape").getValue();
        String serviceEndpoint2 = apeFacadeConfig.getApeServiceEndPointConfig("onNetCheck").getValue();
        String serviceEndpoint3 = apeFacadeConfig.getApeServiceEndPointConfig("supplier-check").getValue();
        try {
            return isOk(new URI(serviceEndpoint1)) && isOk(new URI(serviceEndpoint2)) && isOk(new URI(serviceEndpoint3))? HealthStatus.green("APE is up and running") : HealthStatus.red("APE is unavailable");
        } catch (URISyntaxException e) {
            return  HealthStatus.red(format("Invalid end point '%s'", serviceEndpoint1));
        }
    }
}
