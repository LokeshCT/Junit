package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class UISiteAddressStrategyTest {
    @Test
    public void shouldGetSiteAddress() {
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withBfgSiteId("100").withSubBuilding("someSubBuilding").withBuilding("someBuilding")
                                        .withBuildingNumber("someBuildingNumber").withSubStreet("someSubStreet").withStreet("someStreet")
                                        .withSubLocality("someSubLocality").withLocality("someLocality").withCity("someCity")
                                        .withSubStateCountyProvince("someSubStateCountyProvince").withStateCountyProvince("someStateCountyProvince")
                                        .withCountry("someCountry").withPostCode("614704").withPostBox("somePOBox").build();

        String siteAddress = UISiteAddressStrategy.siteAddress(siteDTO);
        Assert.assertThat(siteAddress, is("someSubBuilding, someBuilding, someBuildingNumber, someSubStreet, someStreet, someSubLocality, someLocality, someCity, someSubStateCountyProvince, someStateCountyProvince, someCountry, 614704, somePOBox"));
    }

    @Test
    public void shouldRemoveSeparatorWhenAddressMemberIsEmptyOrNull() {
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withBfgSiteId("100").withSubBuilding("someSubBuilding").withSubStreet("someSubStreet").withStreet("someStreet")
                                        .withSubLocality("someSubLocality").withLocality("someLocality").withCity("someCity")
                                        .withStateCountyProvince("someStateCountyProvince")
                                        .withCountry("").withPostCode("614704").build();

        String siteAddress = UISiteAddressStrategy.siteAddress(siteDTO);
        Assert.assertThat(siteAddress, is("someSubBuilding, someSubStreet, someStreet, someSubLocality, someLocality, someCity, someStateCountyProvince, 614704"));
    }

    @Test
    public void shouldReturnEmptyWhenAddressMembersAreNull() {
        SiteDTO siteDTO = SiteDTOFixture.aSiteDTO().withBfgSiteId("100").build();

        String siteAddress = UISiteAddressStrategy.siteAddress(siteDTO);
        Assert.assertThat(siteAddress, is(""));
    }
}
