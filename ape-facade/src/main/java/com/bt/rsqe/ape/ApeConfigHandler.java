package com.bt.rsqe.ape;

import com.bt.commons.configuration.ConfigurationException;
import com.bt.rsqe.ape.config.AccessTechnologyMapping;
import com.bt.rsqe.ape.config.ApeMappingConfigLoader;
import com.bt.rsqe.ape.config.AttributeMapping;
import com.bt.rsqe.ape.config.ProductOfferingMapping;
import com.bt.rsqe.ape.dto.AccessAttributeMappingDetail;
import com.bt.rsqe.ape.dto.AccessTechnologyMappingConfig;
import com.bt.rsqe.ape.dto.ApeAccessExtensionDetail;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.rest.ResponseBuilder;
import com.google.common.base.Function;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;

@Path("/rsqe/ape-facade/config")
@Produces({MediaType.APPLICATION_JSON})
public class ApeConfigHandler {
    private static final String TRUE = "true";
    private static final String GET_TRANSFORMER = "getTransformer";
    private final APEQrefRepository apeQrefRepository;

    public ApeConfigHandler(APEQrefRepository apeQrefRepository) {
        this.apeQrefRepository = apeQrefRepository;
    }

    @GET
    public Response get(@QueryParam("accessTechnology") String accessTechnology,
                        @QueryParam("mappingType") String mappingType) {
        String baseAccessTechnology = ApeMappingConfigLoader.getBaseAccessTechnology(accessTechnology);
        final List<AttributeMapping> apeMappings = ApeMappingConfigLoader.getApeMappings(baseAccessTechnology);
        final List<AttributeMapping> responseTypeMappings = ApeMappingConfigLoader.getApeResponseTypeMappings(mappingType);
        apeMappings.addAll(responseTypeMappings);
        final List<AttributeMapping> starsResponseMappings = ApeMappingConfigLoader.getApeStarsResponseMappings(mappingType);
        apeMappings.addAll(starsResponseMappings);
        List<AccessAttributeMappingDetail> mappingConfig = newArrayList();
        for (AttributeMapping apeProductPricingMapping : apeMappings) {
            mappingConfig.add(new AccessAttributeMappingDetail(apeProductPricingMapping.getName(),
                                                               TRUE.equals(apeProductPricingMapping.getMapsToOffering()),
                                                               TRUE.equals(apeProductPricingMapping.getUserVisible()),
                                                               makeOptional(apeProductPricingMapping, GET_TRANSFORMER),
                                                               Integer.parseInt(makeOptional(apeProductPricingMapping, "getPriority", "1000")),
                                                               makeOptional(apeProductPricingMapping, "getMapping")));
        }

        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<AccessAttributeMappingDetail>>(mappingConfig) {
        }).build();
    }


    @Path("/qref/{qrefId}")
    @GET
    public Response getAttributeMappingDetail(@PathParam("qrefId") String qrefId) {
        ApeQref qref = apeQrefRepository.getApeQref(qrefId);
        return get(qref.getAccessTechnology(), qref.getResponseType());
    }

    @Path("/copy-rules")
    @GET
    public Response getAttributeCopyRules() {
        final List<AttributeMapping> apeMappings = ApeMappingConfigLoader.getCopyRules();

        List<AccessAttributeMappingDetail> mappingConfig = newArrayList();
        for (AttributeMapping apeProductPricingMapping : apeMappings) {
            mappingConfig.add(new AccessAttributeMappingDetail(apeProductPricingMapping.getName(),
                                                               TRUE.equals(apeProductPricingMapping.getMapsToOffering()),
                                                               TRUE.equals(apeProductPricingMapping.getUserVisible()),
                                                               makeOptional(apeProductPricingMapping, GET_TRANSFORMER),
                                                               Integer.parseInt(makeOptional(apeProductPricingMapping, "getPriority", "1000")),
                                                               apeProductPricingMapping.getMapping(),
                                                               makeOptional(apeProductPricingMapping,"getDefaultValue")));
        }

        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<AccessAttributeMappingDetail>>(mappingConfig) {
        }).build();
    }

    @Path("/access-extension-details")
    @GET
    public Response getAccessExtensionDetails() {
        try {
            final List<ProductOfferingMapping> productOfferingMappings = newArrayList(ApeMappingConfigLoader.getAccessExtensionProductOfferingMappings());
            final ArrayList<ApeAccessExtensionDetail> apeAccessExtensionDetails = newArrayList(transform(productOfferingMappings, new Function<ProductOfferingMapping, ApeAccessExtensionDetail>() {
                @Override
                public ApeAccessExtensionDetail apply(ProductOfferingMapping input) {
                    return new ApeAccessExtensionDetail(input.getId(), input.getRelationshipName());
                }
            }));
            return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<ApeAccessExtensionDetail>>(apeAccessExtensionDetails) {
            }).build();
        } catch (ConfigurationException e) {
            return ResponseBuilder.notFound().build();
        }
    }

    @Path("/access-technology-details")
    @GET
    public Response getAccessTechnologyDetail() {
        try {
            List<AccessTechnologyMappingConfig> accessTechnologyMappingConfigs = newArrayList();
            AccessTechnologyMapping[] accessTechnologyMappings = ApeMappingConfigLoader.getAccessTechnologyMappings();
            for (AccessTechnologyMapping accessTechnologyMapping : accessTechnologyMappings) {
                accessTechnologyMappingConfigs.add( new AccessTechnologyMappingConfig(accessTechnologyMapping.getId(),
                                                          accessTechnologyMapping.getBaseAccessTechnology()) );
            }
            return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<AccessTechnologyMappingConfig>>(accessTechnologyMappingConfigs) {
            }).build();
        } catch (ConfigurationException e) {
            return ResponseBuilder.notFound().build();
        }
    }

    private String makeOptional(AttributeMapping config, String method) {
        return makeOptional(config, method, null);
    }

    private String makeOptional(AttributeMapping config, String method, String defaultValue) {
        try {
            return (String)AttributeMapping.class.getMethod(method).invoke(config);
        } catch (Exception e) {
            // This is ok.  The parameter might not be in the XML file (as its optional)
        }

        return defaultValue;
    }
}
