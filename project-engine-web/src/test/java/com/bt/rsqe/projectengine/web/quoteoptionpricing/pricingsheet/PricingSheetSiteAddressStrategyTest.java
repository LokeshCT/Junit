package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PricingSheetSiteAddressStrategyTest {

    @Test
    public void shouldReturnSiteAddressDetails() {
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withBfgSiteId("100").withSubBuilding("someSubBuilding").withBuilding("someBuilding")
                                        .withBuildingNumber("someBuildingNumber").withSubStreet("someSubStreet").withStreet("someStreet")
                                        .withSubLocality("someSubLocality").withLocality("someLocality").withCity("someCity")
                                        .withSubStateCountyProvince("someSubStateCountyProvince").withStateCountyProvince("someStateCountyProvince")
                                        .withCountry("someCountry").withPostCode("614704").withPostBox("somePOBox").build();

        PricingSheetSiteAddressStrategy addressStrategy = new PricingSheetSiteAddressStrategy(siteDTO);

        assertThat(addressStrategy.getBuilding(), is("someSubBuilding, someBuilding"));
        assertThat(addressStrategy.getAddressLine1(), is("someBuildingNumber, someSubStreet, someStreet, somePOBox"));
        assertThat(addressStrategy.getAddressLine2(), is("someSubLocality, someLocality"));
        assertThat(addressStrategy.getCity(), is("someCity"));
        assertThat(addressStrategy.getState(), is("someSubStateCountyProvince, someStateCountyProvince"));
        assertThat(addressStrategy.getPostCode(), is("614704"));

    }

    @Test
    public void shouldRemoveSeparatorsWhenAAddressMemberIsEmptyOrNull() {
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withBfgSiteId("100").withBuilding("someBuilding")
                                        .withBuildingNumber("someBuildingNumber").withStreet("someStreet")
                                        .withSubLocality("someSubLocality").withCity("someCity")
                                        .withSubStateCountyProvince("someSubStateCountyProvince").withStateCountyProvince("someStateCountyProvince")
                                        .withCountry("someCountry").withPostBox("somePOBox").build();

        PricingSheetSiteAddressStrategy addressStrategy = new PricingSheetSiteAddressStrategy(siteDTO);

        assertThat(addressStrategy.getBuilding(), is("someBuilding"));
        assertThat(addressStrategy.getAddressLine1(), is("someBuildingNumber, someStreet, somePOBox"));
        assertThat(addressStrategy.getAddressLine2(), is("someSubLocality"));
        assertThat(addressStrategy.getCity(), is("someCity"));
        assertThat(addressStrategy.getState(), is("someSubStateCountyProvince, someStateCountyProvince"));
        assertThat(addressStrategy.getPostCode(), is(""));

    }
}
