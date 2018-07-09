package com.bt.rsqe.ape.config;

import com.bt.commons.configuration.ConfigurationException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

import static com.bt.rsqe.ape.config.ApeMappingConfigLoader.*;
import static com.bt.rsqe.ape.matchers.AccessTechnologyMappingMatcher.*;
import static com.bt.rsqe.ape.matchers.AttributeMappingMatcher.*;
import static com.bt.rsqe.ape.matchers.LocalIdentifierMappingMatcher.*;
import static com.bt.rsqe.ape.matchers.ProductOfferingMappingMatcher.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ApeMappingConfigLoaderTest {
    @Test
    public void shouldGetAccessTechnologyLocator(){
        String accessTechnology = getAccessTechnologyLocator();
        assertThat("access.name", is(accessTechnology));
    }

    @Test
    public void shouldGetAccessTechnologyMappings(){
        assertThat(newArrayList(getAccessTechnologyMappings()), hasItems(anAccessTechnologyMapping().withId("hVPN-PLC").withBaseAccessTechnology("hVPN"),
                                                                   anAccessTechnologyMapping().withId("hVPN-DSL").withBaseAccessTechnology("hVPN"),
                                                                   anAccessTechnologyMapping().withId("hVPN-Cable").withBaseAccessTechnology("hVPN"),
                                                                   anAccessTechnologyMapping().withId("hVPN-Satellite").withBaseAccessTechnology("hVPN"),
                                                                   anAccessTechnologyMapping().withId("hVPN-Wireless").withBaseAccessTechnology("hVPN"),
                                                                   anAccessTechnologyMapping().withId("hVPN-Mobile").withBaseAccessTechnology("hVPN")));
    }

    @Test
    public void shouldGetSpecifiedAccessTechnologyMapping(){
        AccessTechnologyMapping accessTechnologyMapping = getAccessTechnologyMapping("hVPN-DSL");
        assertThat(accessTechnologyMapping.getBaseAccessTechnology(), is("hVPN"));
    }

    @Test
    public void shouldGetApeQuoteMappings(){
        List<AttributeMapping> apeProductPricingMappings = getApeQuoteMappings("hVPN");
        assertThat(apeProductPricingMappings, hasItems(anAttributeMapping()
                                                                       .withUserVisible("false")
                                                                       .withName("Request Id")
                                                                       .withMapsToOffering("false")
                                                                       .withMapping("requestId")));
    }

    @Test
    public void shouldGetApeProductPricingMappings(){
        List<AttributeMapping> apeProductPricingMappings = getApeProductPricingMappings("hVPN");
        assertThat(newArrayList(apeProductPricingMappings), hasItems(anAttributeMapping()
                                                                       .withUserVisible("true")
                                                                       .withName("INTERFACE TYPE")
                                                                       .withMapsToOffering("true")
                                                                       .withMapping("theInterface.name"),
                                                                   anAttributeMapping()
                                                                       .withUserVisible("false")
                                                                       .withName("MINIMUM GUARANTEED SPEEDS")
                                                                       .withMapsToOffering("true")
                                                                       .withMapping("hvpnCaveats.HVPNGUARANTEEDSPEED")));
    }

    @Test
    public void shouldGetApeMappings(){
        List<AttributeMapping> apeProductPricingMappings = getApeMappings("hVPN");
        assertThat(newArrayList(apeProductPricingMappings), hasItems(anAttributeMapping()
                                                                       .withUserVisible("true")
                                                                       .withName("INTERFACE TYPE")
                                                                       .withMapsToOffering("true")
                                                                       .withMapping("theInterface.name"),
                                                                  anAttributeMapping()
                                                                       .withUserVisible("false")
                                                                       .withName("Site Name")
                                                                       .withMapsToOffering("true")
                                                                       .withMapping("siteName"),
                                                                  anAttributeMapping()
                                                                       .withUserVisible("false")
                                                                       .withName("Request Id")
                                                                       .withMapsToOffering("false")
                                                                       .withMapping("requestId")));
    }

    @Test
    public void shouldGetApeSiteQueryMappings(){
        List<AttributeMapping> apeProductPricingMappings = getApeSiteQueryMappings("hVPN");
        assertThat(newArrayList(apeProductPricingMappings), hasItems(anAttributeMapping()
                                                                       .withUserVisible("false")
                                                                       .withName("Site Name")
                                                                       .withMapsToOffering("true")
                                                                       .withMapping("siteName"),
                                                                   anAttributeMapping()
                                                                       .withUserVisible("false")
                                                                       .withName("SitePost/ZipCode")
                                                                       .withMapsToOffering("true")
                                                                       .withMapping("siteAddress.postCode")));
    }

    @Test
    public void shouldGetApeResponseTypeQueryMappings(){
        List<AttributeMapping> apeResponseTypeMappings = getApeResponseTypeMappings("MarketBasedPrice");
        assertThat(newArrayList(apeResponseTypeMappings), hasItems(anAttributeMapping()
                                                                         .withUserVisible("true")
                                                                         .withName("BT Install Price")
                                                                         .withMapsToOffering("false")
                                                                         .withMapping("BTInstallprice"),
                                                                     anAttributeMapping()
                                                                         .withUserVisible("true")
                                                                         .withName("STDEV Install")
                                                                         .withMapsToOffering("false")
                                                                         .withMapping("STDEVInstall")));

    }

    @Test
    public void shouldNotGetApeResponseTypeQueryMappingsIfQrefResponseTypeIsNull(){
        List<AttributeMapping> apeResponseTypeMappings = getApeResponseTypeMappings(null);
        assertThat(apeResponseTypeMappings.size(),is(0));
    }

    @Test
    public void shouldGetSpecifiedApeProductPricingMappings(){
        assertThat(getApeProductPricingMappings("Not hVPN!"), hasItems(anAttributeMapping()
                                                                       .withUserVisible("true")
                                                                       .withName("INTERFACE TYPE")
                                                                       .withMapsToOffering("true")
                                                                       .withMapping("theInterface.name")));
    }

    @Test
    public void shouldGetLocalIdentifierMappings() throws Exception {
      LocalIdentifierMappings localIdentifierMappings = ApeMappingConfigLoader.getLocalIdentifierMappings();
      assertThat(newArrayList(localIdentifierMappings.getLocalIdentifierMappingConfigs()), hasItems(aLocalIdentifierMapping()
                                                                                                        .withId("PAIR_ID")
                                                                                                        .withOfferingAttribute("Pair")));
    }

    @Test
    public void shouldGetLocalSpecifiedLocalIdentifierMapping() throws Exception {
      LocalIdentifierMapping localIdentifierMapping = ApeMappingConfigLoader.getLocalIdentifierMappings().getLocalIdentifierMappingConfig("PAIR_ID");
      assertThat(localIdentifierMapping, is(aLocalIdentifierMapping()
                                                .withId("PAIR_ID")
                                                .withOfferingAttribute("Pair")));
    }

    @Test
    public void shouldGetNameSpecificAttributeMapping() throws Exception {
        AttributeMapping apeProductPricingMapping = getApeMapping("hVPN", "INTERFACE TYPE");
        assertThat(apeProductPricingMapping, is(anAttributeMapping()
                                                   .withUserVisible("true")
                                                   .withName("INTERFACE TYPE")
                                                   .withMapsToOffering("true")
                                                   .withMapping("theInterface.name")));
    }

    @Test
    public void shouldGetNameSpecificAttributeMappingForIf() throws Exception {
        AttributeMapping apeProductPricingMapping = getApeMapping("*", "ACCESS SUPPLIER NAME (TELCO NAME)");
        assertThat(apeProductPricingMapping, is(anAttributeMapping()
                                                    .withUserVisible("true")
                                                    .withName("ACCESS SUPPLIER NAME (TELCO NAME)")
                                                    .withMapsToOffering("true")
                                                    .withMapping("if( userAttributes.pricingType = 'MBP', supplierName, supplier.name)")));
    }

    @Test
    public void shouldGetTypeAndNameSpecificAttributeMapping() throws Exception {
        AttributeMapping apeProductPricingMapping = getApeMapping("hVPN", "COPPER DETAILS");
        assertThat(apeProductPricingMapping, is(anAttributeMapping()
                                                   .withUserVisible("false")
                                                   .withName("COPPER DETAILS")
                                                   .withMapsToOffering("true")
                                                   .withMapping("hvpnCaveats.copperDetails")));
    }

    @Test
    public void shouldNotGetSpecificAttributeMappingWithWrongType() throws Exception {
        AttributeMapping apeProductPricingMapping = getApeMapping("not hVPN", "METHOD");
        assertThat(apeProductPricingMapping, nullValue());
    }

    @Test(expected = ConfigurationException.class)
    public void shouldGetAccessTechnologyMapping() throws Exception {
        ApeMappingConfigLoader.getAccessTechnologyMapping("not hVPN");
    }

    @Test
    public void shouldGetAccessExtensionProductOfferingMappings() throws Exception {
        ProductOfferingMapping[] accessExtensionProductOfferingMappings = getAccessExtensionProductOfferingMappings();
        assertThat(newArrayList(accessExtensionProductOfferingMappings), hasItems(aProductOfferingMapping().withId("hVPN").withRelationName("HVPNAccess"),
                                                                                  aProductOfferingMapping().withId("Ethernet").withRelationName("EthernetAccess"),
                                                                                  aProductOfferingMapping().withId("Leasedline").withRelationName("LeasedLineAccess")));
        assertThat(accessExtensionProductOfferingMappings.length, is(3));


    }


    @Test
    public void shouldOverlayAccessSpecificPricingMappings() throws Exception {
        List<AttributeMapping> mappings = getApeProductPricingMappings("hVPN");

        assertFalse("Expected Default Access Upstream Speed mapping not to be present",
                    Iterables.tryFind(mappings, new Predicate<AttributeMapping>() {
                        @Override
                        public boolean apply(@Nullable AttributeMapping input) {
                            return anAttributeMapping()
                                .withName("ACCESS UPSTREAM SPEED DISPLAY VALUE")
                                .withUserVisible("true")
                                .withMapsToOffering("true")
                                .withMapping("\"accessSpeedValue+accessSpeedUom\"").matchesSafely(input);
                        }
                    }).isPresent());

        assertThat(mappings, hasItem(anAttributeMapping()
                                          .withName("ACCESS UPSTREAM SPEED DISPLAY VALUE")
                                          .withUserVisible("true")
                                          .withMapsToOffering("true")
                                          .withMapping("\"hvpnCaveats.hvpnAccessSpeedValue@SlashRangeUpper:String\"")));
    }

    @Test
    public void shouldOnlyReturnDefaultPricingMappingsForNonMappedAccessTechnology() throws Exception {
        List<AttributeMapping> mappings = getApeProductPricingMappings("doesNotExist");

        assertFalse("Expected HVPN Access Upstream Speed mapping not to be present",
                    Iterables.tryFind(mappings, new Predicate<AttributeMapping>() {
                        @Override
                        public boolean apply(@Nullable AttributeMapping input) {
                            return anAttributeMapping()
                                          .withName("ACCESS UPSTREAM SPEED DISPLAY VALUE")
                                          .withUserVisible("true")
                                          .withMapsToOffering("true")
                                          .withMapping("hvpnCaveats.hvpnAccessSpeedValue").matchesSafely(input);
                        }
                    }).isPresent());

        assertThat(mappings, hasItem(anAttributeMapping()
                                .withName("ACCESS UPSTREAM SPEED DISPLAY VALUE")
                                .withUserVisible("true")
                                .withMapsToOffering("true")
                                .withMapping("\"accessSpeedValue+accessSpeedUom\"")));
    }

    @Test
    public void shouldGetBaseAccessTechnology() throws Exception {
        assertThat(ApeMappingConfigLoader.getBaseAccessTechnology("hVPN-PLC"), Is.is("hVPN"));
    }

    @Test
    public void shouldGetDefaultBaseAccessTechnologyWhenGivenAccessTechnologyIsNotMapped() throws Exception {
        assertThat(ApeMappingConfigLoader.getBaseAccessTechnology("anUnmappedAccessTechnology"), Is.is("*"));
    }

    @Test
    public void shouldGetApeStarsResponseMappings(){
        List<AttributeMapping> apeStarsResponseMappings = getApeStarsResponseMappings("RenewalPrice");
        assertThat(newArrayList(apeStarsResponseMappings), hasItems(anAttributeMapping()
                                        .withUserVisible("true")
                                        .withName("Leg Identifier")
                                        .withMapsToOffering("false")
                                        .withMapping("legIdentifier"),
                                        anAttributeMapping()
                                        .withUserVisible("true")
                                        .withName("Renewable Status")
                                        .withMapsToOffering("false")
                                        .withMapping("renewalStatus")));

    }
}
