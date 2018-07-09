package com.bt.rsqe.projectengine.server;

public interface SqeConfiguratorStubConfig {
     Url[] getUrls();

     public interface Url {
            String getContext();
            String getUrl();
        }
}
