package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProductSitesDTOTest {

    private List<SiteDTO> siteDTOs;
    private SiteDTO site1;
    private SiteDTO site2;

    @Before
    public void before() {
        site1 = new SiteDTO("1", "site 1");
        site1.country = "HEARD AND MC DONALD ISLANDS";
        site2 = new SiteDTO("2", "site 2");
        site2.country = "HEARD AND MC DONALD ISLANDS";
        siteDTOs = newArrayList(site1);
    }

    @Test
    public void shouldCreateProductSitesDTOWithOldAndNewSites() throws Exception {
        PaginatedFilterResult<SiteDTO> paginatedFilterResult = new PaginatedFilterResult<SiteDTO>(1, newArrayList(site1), 1, 1);

        ProductSitesDTO productSitesDTO = new ProductSitesDTO(paginatedFilterResult, Lists.<String>newArrayList(), Lists.<String>newArrayList(), false, site2, false, Optional.<Map<AssetDTO,SiteDTO>>absent());
        assertThat(productSitesDTO.sites.get(0).id, is("1"));
        assertThat(productSitesDTO.sites.get(0).newSiteId, is("2"));
    }

    @Test
    public void shouldCreateProductSitesDTOWithNullForOldSite() throws Exception {
        PaginatedFilterResult<SiteDTO> paginatedFilterResult = new PaginatedFilterResult<SiteDTO>(1, newArrayList(site1), 1, 1);
        ProductSitesDTO productSitesDTO = new ProductSitesDTO(paginatedFilterResult, Lists.<String>newArrayList(), Lists.<String>newArrayList(), false, null, false, Optional.<Map<AssetDTO,SiteDTO>>absent());
        assertThat(productSitesDTO.sites.get(0).id, is("1"));
        assertNull(productSitesDTO.sites.get(0).newSiteId);
    }

    @Test
    public void shouldCreateProductSitesDTOWithSameSiteDTOAndAMapWithSummaries() throws Exception {
        PaginatedFilterResult<SiteDTO> paginatedFilterResult = new PaginatedFilterResult<SiteDTO>(1, newArrayList(site1), 1, 1);
        Map<AssetDTO, SiteDTO> siteWithAssetSummaryMap = newHashMap();
        AssetDTO assetDTO = mock(AssetDTO.class);
        when(assetDTO.getDescription()).thenReturn("summaryFromInstance");
        siteWithAssetSummaryMap.put(assetDTO, site1);
        ProductSitesDTO productSitesDTO = new ProductSitesDTO(paginatedFilterResult, Lists.<String>newArrayList(), Lists.<String>newArrayList(), false, null, true, Optional.of(siteWithAssetSummaryMap));
        assertThat(productSitesDTO.sites.get(0).id, is("1"));
        assertThat(productSitesDTO.sites.get(0).newSiteId, is("1"));
        assertThat(productSitesDTO.sites.get(0).summary, is("summaryFromInstance"));
    }
}
