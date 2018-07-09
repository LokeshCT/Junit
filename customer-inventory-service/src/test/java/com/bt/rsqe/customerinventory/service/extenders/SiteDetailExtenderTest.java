package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetSiteDetail;
import com.bt.rsqe.customerinventory.service.client.domain.UnloadedExtensionAccessException;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.project.SiteId;
import com.bt.rsqe.enums.AssetType;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.SiteDetail;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class SiteDetailExtenderTest {
    private static final String SITE_NAME = "SITE";
    private static final String COUNTRY = "COUNTRY";
    private static final String CITY = "CITY";
    private final CustomerResource customerResource = mock(CustomerResource.class);
    private final SiteResource siteResource = mock(SiteResource.class);

    @Test (expected = UnloadedExtensionAccessException.class)
    public void shouldNotLoadSiteDetailsIfNotRequested() {
        CIFAsset cifAsset = aCIFAsset().with((CIFAssetSiteDetail)null).build();

        final SiteDetailExtender siteDetailExtender = new SiteDetailExtender(customerResource);
        siteDetailExtender.extend(new ArrayList<CIFAssetExtension>(), cifAsset);

        cifAsset.getSiteDetail();
    }

    @Test
    public void shouldLoadSiteDetailsIfAvailable() {
        CIFAsset cifAsset = aCIFAsset().build();
        when(customerResource.siteResource(cifAsset.getCustomerId())).thenReturn(siteResource);
        SiteDTO siteDTO = mock(SiteDTO.class);
        when(siteDTO.getSiteName()).thenReturn(SITE_NAME);
        when(siteDTO.getCountryName()).thenReturn(COUNTRY);
        when(siteDTO.getCity()).thenReturn(CITY);
        when(siteDTO.getSiteId()).thenReturn(SiteId.newInstance(114l));
        when(siteResource.get(cifAsset.getSiteId(), cifAsset.getProjectId())).thenReturn(siteDTO);

        final SiteDetailExtender siteDetailExtender = new SiteDetailExtender(customerResource);
        siteDetailExtender.extend(newArrayList(SiteDetail), cifAsset);

        final CIFAssetSiteDetail expectedSiteDetail = new CIFAssetSiteDetail(114, SITE_NAME, CITY, COUNTRY, siteDTO.getCountryISOCode());
        expectedSiteDetail.setAddress("null null CITY null null null");
        assertThat(cifAsset.getSiteDetail(), is(expectedSiteDetail));
    }

    @Test
    public void shouldGetCentralSiteDetailsWhenNullSiteId() {
        CIFAsset cifAsset = aCIFAsset().withSiteId(null).withAssetType(AssetType.REAL).build();

        when(customerResource.siteResource(cifAsset.getCustomerId())).thenReturn(siteResource);
        SiteDTO siteDTO = mock(SiteDTO.class);
        when(siteDTO.getSiteName()).thenReturn(SITE_NAME);
        when(siteDTO.getCountryName()).thenReturn(COUNTRY);
        when(siteDTO.getCity()).thenReturn(CITY);
        when(siteDTO.getSiteId()).thenReturn(SiteId.newInstance(100L));
        when(siteResource.getCentralSite(cifAsset.getProjectId())).thenReturn(siteDTO);
        when(siteResource.get(cifAsset.getSiteId(), cifAsset.getProjectId())).thenReturn(siteDTO);

        final SiteDetailExtender siteDetailExtender = new SiteDetailExtender(customerResource);
        siteDetailExtender.extend(newArrayList(SiteDetail), cifAsset);

        final CIFAssetSiteDetail expected = new CIFAssetSiteDetail(100L, SITE_NAME, CITY, COUNTRY, siteDTO.getCountryISOCode());
        expected.setAddress("null null CITY null null null");
        assertThat(cifAsset.getSiteDetail(), is(expected));
    }

    @Test
    public void shouldNotLoadSiteDetailsUponExceptionOnFetchingCentralSiteDetails() {
        CIFAsset cifAsset = aCIFAsset().withSiteId(null).withAssetType(AssetType.REAL).build();

        when(customerResource.siteResource(cifAsset.getCustomerId())).thenReturn(siteResource);
        SiteDTO siteDTO = mock(SiteDTO.class);
        when(siteDTO.getSiteName()).thenReturn(SITE_NAME);
        when(siteDTO.getCountryName()).thenReturn(COUNTRY);
        when(siteDTO.getCity()).thenReturn(CITY);
        when(siteDTO.getSiteId()).thenReturn(SiteId.newInstance(100L));
        when(siteResource.getCentralSite(cifAsset.getProjectId())).thenThrow(Exception.class);

        final SiteDetailExtender siteDetailExtender = new SiteDetailExtender(customerResource);
        siteDetailExtender.extend(newArrayList(SiteDetail), cifAsset);

        assertThat(cifAsset.getSiteDetail(), nullValue());
    }


    @Test
    public void shouldNotGetCentralSiteDetailsWhenSiteIdOrCustomerIdOrProjectIdAreNull() {
        CIFAsset cifAsset = aCIFAsset().withSiteId(null).withAssetType(AssetType.REAL).withProjectId(null).build();

        final SiteDetailExtender siteDetailExtender = new SiteDetailExtender(customerResource);
        siteDetailExtender.extend(newArrayList(SiteDetail), cifAsset);

        assertThat(cifAsset.getSiteDetail(), nullValue());
    }

    @Test
    public void shouldNotLoadSiteDetailsForStubbedAsset() {
        CIFAsset cifAsset = aCIFAsset().withSiteId(null).withAssetType(AssetType.STUB).build();

        final SiteDetailExtender siteDetailExtender = new SiteDetailExtender(customerResource);
        siteDetailExtender.extend(newArrayList(SiteDetail), cifAsset);

        assertThat(cifAsset.getSiteDetail(), nullValue());
    }
}