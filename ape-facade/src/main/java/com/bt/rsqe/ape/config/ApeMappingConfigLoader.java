package com.bt.rsqe.ape.config;

import com.bt.rsqe.configuration.ConfigurationProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class ApeMappingConfigLoader{

    public static ApeMappingConfig apeMappingConfig = ConfigurationProvider.provide(ApeMappingConfig.class, "ApeMappingConfig");

    public static String getAccessTechnologyLocator(){
        return apeMappingConfig.getAccessTechnologyLocator();
    }

    public static AccessTechnologyMapping[] getAccessTechnologyMappings(){
        return apeMappingConfig.getAccessTechnologyMappingsConfig().getAccessTechnologyMappingConfigs();
    }

    public static AccessTechnologyMapping getAccessTechnologyMapping(String accessTechnology){
        return apeMappingConfig.getAccessTechnologyMappingsConfig().getAccessTechnologyMappingConfig(accessTechnology);
    }

    public static List<AttributeMapping> getApeQuoteMappings(String baseAccessTechnology){
        ApeQuoteMappings[] apeMappingses = apeMappingConfig.getApeQuoteMappingsConfig();
        List<AttributeMapping> filteredMappings=newArrayList();

        for(ApeQuoteMappings apeMapping : apeMappingses){
            if("*".equals(apeMapping.getBaseAccessTechnology()) ||
                    baseAccessTechnology.equals(apeMapping.getBaseAccessTechnology())){
                AttributeMapping[] allMappings = apeMapping.getAttributeMappingConfigs();

                Collections.addAll(filteredMappings, allMappings);
            }
        }
        return filteredMappings;
    }

    public static List<AttributeMapping> getApeSiteQueryMappings(String baseAccessTechnology){
        ApeSiteQueryMappings[] apeMappingses = apeMappingConfig.getApeSiteQueryMappingsConfig();
        List<AttributeMapping> filteredMappings=newArrayList();

        for(ApeSiteQueryMappings apeMapping : apeMappingses){
            if("*".equals(apeMapping.getBaseAccessTechnology()) ||
                    baseAccessTechnology.equals(apeMapping.getBaseAccessTechnology())){
                AttributeMapping[] allMappings = apeMapping.getAttributeMappingConfigs();

                Collections.addAll(filteredMappings, allMappings);
            }
        }
        return filteredMappings;
    }

    public static List<AttributeMapping> getApeResponseTypeMappings(String responseType){
        ApeResponseTypeMappings[] apeMappings = apeMappingConfig.getApeResponseTypeMappingsConfig();
        List<AttributeMapping> filteredMappings=newArrayList();

        for(ApeResponseTypeMappings apeMapping : apeMappings){
            if(isNotNull(responseType) && responseType.equals(apeMapping.getResponseType())){
                AttributeMapping[] allMappings = apeMapping.getAttributeMappingConfigs();

                Collections.addAll(filteredMappings, allMappings);
            }
        }
        return filteredMappings;
    }

    public static List<AttributeMapping> getApeStarsResponseMappings(String responseType){
        ApeStarsResponseMappings[] apeMappings = apeMappingConfig.getApeStarsResponseMappingConfig();
        List<AttributeMapping> filteredMappings=newArrayList();

        for(ApeStarsResponseMappings apeMapping : apeMappings){
            if(isNotNull(responseType) && responseType.equals(apeMapping.getResponseType())){
                AttributeMapping[] allMappings = apeMapping.getAttributeMappingConfigs();

                Collections.addAll(filteredMappings, allMappings);
            }
        }
        return filteredMappings;
    }

    public static List<AttributeMapping> getApeProductPricingMappings(String baseAccessTechnology){
        ApeProductPricingMappings[] apeMappingses = apeMappingConfig.getApeProductPricingMappingsConfig();
        Map<String, AttributeMapping> filteredMappings = new HashMap<String, AttributeMapping>();

        for(ApeProductPricingMappings apeMapping : apeMappingses){
            if("*".equals(apeMapping.getBaseAccessTechnology()) ||
                    baseAccessTechnology.equals(apeMapping.getBaseAccessTechnology())){
                AttributeMapping[] allMappings = apeMapping.getAttributeMappingConfigs();

                for(AttributeMapping attributeMapping : allMappings){
                    String mappingName = attributeMapping.getName();

                    if(filteredMappings.containsKey(mappingName)) {
                        // only update existing mapping if base access type is specific i.e. not '*'
                        if(!"*".equals(apeMapping.getBaseAccessTechnology())) {
                            filteredMappings.put(attributeMapping.getName(), attributeMapping);
                        }
                    } else {
                        filteredMappings.put(attributeMapping.getName(), attributeMapping);
                    }
                }
            }
        }

        return newArrayList(filteredMappings.values());
    }

    public static List<AttributeMapping> getApeMappings(String baseAccessTechnology){
        List<AttributeMapping> mappings=newArrayList();
        mappings.addAll(getApeQuoteMappings(baseAccessTechnology));
        mappings.addAll(getApeSiteQueryMappings(baseAccessTechnology));
        mappings.addAll(getApeProductPricingMappings(baseAccessTechnology));
        return mappings;
    }

    public static AttributeMapping getApeMapping(String baseAccessTechnology, String attributeName){
        List<AttributeMapping> mappings=newArrayList();
        mappings.addAll(getApeQuoteMappings(baseAccessTechnology));
        mappings.addAll(getApeSiteQueryMappings(baseAccessTechnology));
        mappings.addAll(getApeProductPricingMappings(baseAccessTechnology));

        for(AttributeMapping attributeMapping : mappings){
            if (attributeMapping.getName().equals(attributeName)) {
                return attributeMapping;
            }
        }
        return null;
    }

    public static LocalIdentifierMappings getLocalIdentifierMappings() {
        return apeMappingConfig.getLocalIdentifierMappingsConfig();
    }

    public static ProductOfferingMapping[] getAccessExtensionProductOfferingMappings() {
        return apeMappingConfig.getProductOfferingMappings().getProductOfferingMappings();
    }

    public static String getBaseAccessTechnology(String accessTechnology) {
        try {
            AccessTechnologyMapping baseAccessTechnologyMappings = getAccessTechnologyMapping(accessTechnology);
            return baseAccessTechnologyMappings.getBaseAccessTechnology();
        } catch (com.bt.commons.configuration.ConfigurationException e) {
            // this is ok! Access specific mappings may not exist for the given access type
            return "*";
        }
    }

    public static List<AttributeMapping> getCopyRules() {
        return Arrays.asList(apeMappingConfig.getAttributeCopyRulesConfig().getAttributeMappingConfigs());
    }
}
