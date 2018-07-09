package com.bt.rsqe.projectengine.web.uri;

import com.bt.rsqe.configuration.UrlConfig;
import com.bt.rsqe.enums.ProductCodes;

public class ConfiguratorProductFactory {
    public static ConfiguratorProduct getProduct(String productCode, UrlConfig[] urls) {
        if (ProductCodes.Onevoice.productCode().equals(productCode)) {
            return new OneVoiceConfiguratorProduct(urls);
        }
        if (ProductCodes.IpConnectGlobal.productCode().equals(productCode)) {
            return new IVPNConfiguratorProduct(urls);
        }
        return new DefaultConfiguratorProduct(urls);
    }
}
