package com.bt.rsqe.ape;

import com.bt.rsqe.ape.config.AccessTechnologyMapping;
import com.bt.rsqe.ape.config.AccessTechnologyMappings;
import com.bt.rsqe.ape.config.ApeMappingConfig;
import com.bt.rsqe.ape.dto.AccessAttributeMappingDetail;
import com.bt.rsqe.ape.dto.AccessTechnologyMappingConfig;
import com.bt.rsqe.ape.dto.ApeAccessExtensionDetail;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.google.common.base.Strings;
import com.bt.rsqe.web.rest.RestResponse;
import javax.ws.rs.core.GenericType;
import com.bt.rsqe.utils.UriBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApeConfigHandlerTest {
    private static final String QREF_ID_STENCIL = "Q1234";
    private APEQrefRepository apeQrefRepository;
    private ApplicationConfig applicationConfig;
    private ApeMappingConfig apeMappingConfig;
    private ApeFacade apeFacade;

    @Before
    public void setup() throws Exception {
        apeQrefRepository = mock(APEQrefRepository.class);
        apeMappingConfig = mock(ApeMappingConfig.class);
        ApeQref qref = mock(ApeQref.class);
        when(apeQrefRepository.getApeQref(QREF_ID_STENCIL)).thenReturn(qref);
        when(qref.getAccessTechnology()).thenReturn("hVPN-PLC");
        when(qref.getResponseType()).thenReturn("Access Price");

        applicationConfig = StubApplicationConfig.defaultTestConfig();
        Application application = new Application(applicationConfig) {
            @Override
            protected ResourceHandlerFactory createResourceHandlerFactory() {
                return new RestResourceHandlerFactory() {
                    {
                        withSingleton(new ApeConfigHandler(apeQrefRepository));
                    }
                };
            }
        };
        application.start();

        URI uri = new UriBuilder().scheme(applicationConfig.getScheme()).host(applicationConfig.getHost())
                                      .port(applicationConfig.getPort()).segment("rsqe", "ape-facade").build();
        RestRequestBuilder restRequestBuilder = new RestRequestBuilder(uri);
        apeFacade = new ApeFacade(restRequestBuilder);
    }

    @Test
    public void shouldGetAttributeMappings(){
        String[] segments = new String[]{"rsqe", "ape-facade", "config", "qref", QREF_ID_STENCIL};
        RestRequestBuilder restRequestBuilder = new RestRequestBuilder(applicationConfig);
        RestResponse response = restRequestBuilder.build(segments).get();

        List<AccessAttributeMappingDetail> mappings = response.getEntity(new GenericType<List<AccessAttributeMappingDetail>>() {});

        for(AccessAttributeMappingDetail mapping : mappings) {
            assertTrue(!Strings.isNullOrEmpty(mapping.getName()));
            assertTrue(null != mapping.getMapsToOffering());
            assertTrue(null != mapping.getUserVisible());
            assertTrue(mapping.getPriority() > 0);
        }
    }

    @Test
    public void shouldGetAccessExtensionDetails(){

        //When
        ApeConfigHandler apeConfigHandler = new ApeConfigHandler(null);
        Response accessExtensionDetails = apeConfigHandler.getAccessExtensionDetails();

        //Then
        Assert.assertThat(accessExtensionDetails.getStatus(), is(Response.Status.OK.getStatusCode()));
        List<ApeAccessExtensionDetail> entity = (List<ApeAccessExtensionDetail>) accessExtensionDetails.getEntity();
        assertThat(entity, hasItems(new ApeAccessExtensionDetail("hVPN", "HVPNAccess"),
                                    new ApeAccessExtensionDetail("Ethernet", "EthernetAccess"),
                                    new ApeAccessExtensionDetail("Leasedline", "LeasedLineAccess")));

    }

    @Test
    public void shouldGetAccessTechnologyDetail(){

        //When
        ApeConfigHandler apeConfigHandler = new ApeConfigHandler(null);
        when(apeMappingConfig.getAccessTechnologyMappingsConfig()).thenReturn(new AccessTechnologyMappings() {
            @Override
            public AccessTechnologyMapping[] getAccessTechnologyMappingConfigs() {
                return new AccessTechnologyMapping[] {new AccessTechnologyMapping() {
                    @Override
                    public String getId() {
                        return "hVPN-DSL";
                    }

                    @Override
                    public String getBaseAccessTechnology() {
                        return "hVPN";
                    }
                }};
            }

            @Override
            public AccessTechnologyMapping getAccessTechnologyMappingConfig(String accessTechnology) {
                return null;
            }
        });
        Response accessTechnologyDetail = apeConfigHandler.getAccessTechnologyDetail();

        //Then
        Assert.assertThat(accessTechnologyDetail.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat((List<AccessTechnologyMappingConfig>) accessTechnologyDetail.getEntity(), hasItem(new AccessTechnologyMappingConfig("hVPN-DSL", "hVPN")));

    }

    @Test
    public void shouldGetAttributeCopyRules() {

        List<AccessAttributeMappingDetail> copyRules = apeFacade.getCopyRulesConfig();

        AccessAttributeMappingDetail nonStandardAttribute = copyRules.get(0);
        AccessAttributeMappingDetail transitCopyRule = copyRules.get(1);
        AccessAttributeMappingDetail customerProvidedCopyRule = copyRules.get(2);
        AccessAttributeMappingDetail legacyBillingCopyRule = copyRules.get(3);
        AccessAttributeMappingDetail customAccessSolutionPrimaryBEndSiteName = copyRules.get(4);
        AccessAttributeMappingDetail primaryBEndSiteCountry = copyRules.get(5);
        AccessAttributeMappingDetail primaryBEndSiteCity = copyRules.get(6);


        assertThat(copyRules.size(), is(7));
        assertThat(nonStandardAttribute.getName(), is("NON STANDARD"));
        assertThat(transitCopyRule.getName(), is("TRANSIT"));
        assertThat(customerProvidedCopyRule.getName(), is("CUSTOMER PROVIDED"));
        assertThat(legacyBillingCopyRule.getName(), is("LEGACY BILLING"));
        assertThat(customAccessSolutionPrimaryBEndSiteName.getName(), is("CUSTOM ACCESS SOLUTION PRIMARY B-END SITE NAME"));
        assertThat(primaryBEndSiteCountry.getName(), is("PRIMARY B-END SITE COUNTRY"));
        assertThat(primaryBEndSiteCity.getName(), is("PRIMARY B-END SITE CITY"));

        assertThat(nonStandardAttribute.getDefaultValue(), nullValue());
        assertThat(transitCopyRule.getDefaultValue(), nullValue());
        assertThat(customerProvidedCopyRule.getDefaultValue(), nullValue());
        assertThat(legacyBillingCopyRule.getDefaultValue(), nullValue());
        assertThat(customAccessSolutionPrimaryBEndSiteName.getDefaultValue(), nullValue());
        assertThat(primaryBEndSiteCountry.getDefaultValue(), nullValue());
        assertThat(primaryBEndSiteCity.getDefaultValue(), nullValue());

    }

}
