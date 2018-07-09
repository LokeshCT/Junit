package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;

import org.junit.Before;
import org.junit.Test;
import com.bt.rsqe.projectengine.web.model.LineItemModel;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.matchers.ReflectionEqualsMatcher.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class SiteFacadeTest {

    protected static final String CUSTOMER_ID = "customerId";
    protected static final String QUOTE_OPTION_ID = "quoteOptionId";
    protected static final String CONTRACT_ID = "contractId";
    private static final String PRODUCT_CODE ="productCode";

    private CustomerResource customerResource;
    private SiteResource siteResource;
    private SiteFacade siteFacade;

    private static final String PROJECT_ID = "projectId";
    private LineItemFacade lineItemFacade;

    private List<LineItemModel> lineItemModelList;

    @Before
    public void before() throws Exception {
        customerResource = mock(CustomerResource.class);
        siteResource = mock(SiteResource.class);

        when(customerResource.siteResource(CUSTOMER_ID)).thenReturn(siteResource);

        lineItemFacade = mock(LineItemFacade.class);
        lineItemModelList = new ArrayList<LineItemModel>();

        siteFacade = new SiteFacade(customerResource);


    }

    @Test
    public void shouldFindDistinctCountryForACustomer() throws Exception {
        ArrayList<SiteDTO> sites = new ArrayList<SiteDTO>() {
            {
                SiteDTO uk = aSiteDto();
                uk.country = "UK";

                SiteDTO india = aSiteDto();
                india.country = "India";

                SiteDTO indiaAgain = aSiteDto();
                indiaAgain.country = "India";
                add(uk);
                add(india);
                add(indiaAgain);
            }
        };

        when(siteResource.getBranchSites(PROJECT_ID)).thenReturn(sites);

        List<String> countries = siteFacade.getCountries(CUSTOMER_ID, PROJECT_ID);
        assertThat(countries.size(), is(2));
        assertThat(countries, hasItem("UK"));
        assertThat(countries, hasItem("India"));
    }

    @Test
    public void shouldFindDistinctSortedCountryForACustomer() throws Exception {

        ArrayList<SiteDTO> sites = new ArrayList<SiteDTO>() {
            {
                SiteDTO uk = aSiteDto();
                uk.country = "UK";

                SiteDTO india = aSiteDto();
                india.country = "India";

                add(uk);
                add(india);
            }
        };

        when(siteResource.getBranchSites(PROJECT_ID)).thenReturn(sites);

        List<String> countries = siteFacade.getCountries(CUSTOMER_ID, PROJECT_ID);

        assertThat(countries.size(), is(2));
        assertThat(countries.get(0), is("India"));
        assertThat(countries.get(1), is("UK"));

    }

    @Test
    public void shouldGetASite() throws Exception {
        when(siteResource.get("siteId", PROJECT_ID)).thenReturn(aSiteDto());

        SiteDTO siteDTO = siteFacade.get(CUSTOMER_ID, PROJECT_ID, "siteId");
        assertThat(siteDTO, is(not(nullValue())));
        assertThat(siteDTO, is(reflectionEquals(aSiteDto())));
    }

    @Test
    public void shouldRefreshSite() throws Exception {
        SiteDTO originalSite = new SiteDTO();
        SiteDTO refreshedSite = new SiteDTO();

        when(siteResource.get("siteId", PROJECT_ID)).thenReturn(originalSite);
        when(siteResource.refresh("siteId", PROJECT_ID)).thenReturn(refreshedSite);


        siteFacade.get(CUSTOMER_ID, PROJECT_ID, "siteId");
        siteFacade.refresh(CUSTOMER_ID, PROJECT_ID, "siteId");

        verify(siteResource).refresh("siteId", PROJECT_ID);
        assertThat(siteFacade.get(CUSTOMER_ID, PROJECT_ID, "siteId"), is(refreshedSite));
    }

    @Test
    public void shouldGetAllSites() throws Exception {
        ArrayList<SiteDTO> sites = new ArrayList<SiteDTO>() {{
            add(aSiteDto());
            add(aSiteDto());
        }};
        when(siteResource.getBranchSites(PROJECT_ID)).thenReturn(sites);

        List<SiteDTO> siteDTOs = siteFacade.getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
        assertThat(siteDTOs.size(), is(2));
    }

    @Test
    public void shouldGetAllProductQuoteSites() throws Exception {
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PRODUCT_CODE, true, PriceSuppressStrategy.None)).thenReturn(lineItemModelList);

        List<String> siteDTOs = siteFacade.getAllProductQuoteSites(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PRODUCT_CODE, lineItemFacade);
        assertThat(siteDTOs.size(), is(0));
    }

    @Test
    public void shouldGetCentralSite() throws Exception {
        SiteDTO site = aSiteDto();
        when(siteResource.getCentralSite("proj")).thenReturn(site);

        final SiteDTO siteDTO = siteFacade.getCentralSite(CUSTOMER_ID,"proj");

        assertThat(siteDTO, is(site));
    }

    private SiteDTO aSiteDto() {
        SiteDTO siteDTO = new SiteDTO();
        siteDTO.bfgSiteID = "1";
        siteDTO.city = "IPSWICH";
        siteDTO.addressId = "10";
        siteDTO.locationId = "20";
        return siteDTO;
    }
}
