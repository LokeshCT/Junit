package com.bt.rsqe.nad.config;

import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.ApplicationConfig;

public interface NadFacadeClientConfig extends RestClientConfig{
       ApplicationConfig getApplicationConfig();

}
