package com.bt.rsqe.ape.config;

public interface ApeMappingConfig {
    String getAccessTechnologyLocator();
    AccessTechnologyMappings getAccessTechnologyMappingsConfig();
    ApeQuoteMappings[] getApeQuoteMappingsConfig();
    ApeSiteQueryMappings[] getApeSiteQueryMappingsConfig();
    ApeProductPricingMappings[] getApeProductPricingMappingsConfig();
    LocalIdentifierMappings getLocalIdentifierMappingsConfig();
    ProductOfferingMappings getProductOfferingMappings();
    AttributeCopyRulesConfig getAttributeCopyRulesConfig();
    ApeResponseTypeMappings[] getApeResponseTypeMappingsConfig();
    ApeStarsResponseMappings[] getApeStarsResponseMappingConfig();
}
