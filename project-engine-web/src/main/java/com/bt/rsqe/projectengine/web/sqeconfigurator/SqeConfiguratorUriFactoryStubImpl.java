package com.bt.rsqe.projectengine.web.sqeconfigurator;


import com.bt.rsqe.projectengine.server.SqeConfiguratorStubConfig;

public class SqeConfiguratorUriFactoryStubImpl implements SqeConfiguratorUriStubFactory {

    private enum UriContext {
        CREATE;
    }

    private SqeConfiguratorStubConfig sqeConfiguratorStubConfig;

    public SqeConfiguratorUriFactoryStubImpl(SqeConfiguratorStubConfig sqeConfiguratorStubConfig) {
        this.sqeConfiguratorStubConfig = sqeConfiguratorStubConfig;
    }

    @Override
    public String getSqeLineItemCreationStubUri() {
        return getUri(UriContext.CREATE);
    }

    private String getUri(UriContext context) {
        SqeConfiguratorStubConfig.Url[] urls = sqeConfiguratorStubConfig.getUrls();

        for (SqeConfiguratorStubConfig.Url url : urls) {
            if (UriContext.valueOf(url.getContext()) == context) {
                return url.getUrl();
            }
        }
        return "";
    }
}
