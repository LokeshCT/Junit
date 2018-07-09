package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.fixtures.CustomerDTOFixture;
import com.bt.rsqe.expedio.fixtures.ProjectDTOFixture;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BCMInformerTest {
    private static final String CUSTOMER_ID = "aCustomerId";
    private static final String QUOTE_OPTION_ID = "aQuoteOptionId";
    private static final String CONTRACT_ID = "aContractId";
    private static final String PROJECT_ID = "aProjectId";

    private QuoteOptionFacade quoteOptionFacade;
    private ExpedioProjectResource expedioProjectsResource;
    private CustomerFacade customerFacade;
    private ExpedioClientResources expedioClientResources;
    private ProjectResource projectResource;
    private ProductInstanceClient futureProductInstanceClient;
    private BCMInformer informer;

    @Before
    public void setup() {
        quoteOptionFacade = mock(QuoteOptionFacade.class);
        expedioProjectsResource = mock(ExpedioProjectResource.class);
        customerFacade = mock(CustomerFacade.class);
        expedioClientResources = mock(ExpedioClientResources.class);
        projectResource = mock(ProjectResource.class);
        futureProductInstanceClient = mock(ProductInstanceClient.class);

        BCMInformerFactory factory = new BCMInformerFactory(quoteOptionFacade,
                                                            expedioProjectsResource,
                                                            customerFacade,
                                                            expedioClientResources,
                                                            projectResource,
                                                            futureProductInstanceClient);

        informer = factory.informerFor(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "");
    }

    @Test
    public void shouldCacheProjectDTO() throws Exception {
        final ProjectDTO project = ProjectDTOFixture.aProjectDTO().build();
        when(expedioProjectsResource.getProject(PROJECT_ID)).thenReturn(project);

        assertThat(informer.getProject(), is(project));
        assertThat(informer.getProject(), is(project));
        verify(expedioProjectsResource, times(1)).getProject(any(String.class));
    }

    @Test
    public void shouldCacheCustomerDTO() throws Exception {
        final CustomerDTO customer = CustomerDTOFixture.aCustomerDTO().build();
        when(customerFacade.get(CUSTOMER_ID, CONTRACT_ID)).thenReturn(customer);

        assertThat(informer.getCustomer(), is(customer));
        assertThat(informer.getCustomer(), is(customer));
        verify(customerFacade, times(1)).get(any(String.class), any(String.class));
    }

    @Test
    public void shouldCacheQuoteOptionDTO() throws Exception {
        final QuoteOptionDTO quoteOption = QuoteOptionDTOFixture.aQuoteOptionDTO().build();
        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(quoteOption);

        assertThat(informer.getQuoteOption(), is(quoteOption));
        assertThat(informer.getQuoteOption(), is(quoteOption));
        verify(quoteOptionFacade, times(1)).get(any(String.class), any(String.class));
    }

    @Test
    public void shouldCacheQuoteOptionItems() throws Exception {
        final QuoteOptionItemDTO item1 = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        final QuoteOptionItemDTO item2 = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();

        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(Lists.<QuoteOptionItemDTO>newArrayList(item1, item2));

        assertThat(informer.getQuoteOptionItems(), hasItems(item1, item2));
        assertThat(informer.getQuoteOptionItems(), hasItems(item1, item2));
        verify(projectResource, times(1)).quoteOptionResource(any(String.class));
    }

    @Test
    public void shouldCacheProductInstance() throws Exception {
        final ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().build();
        when(futureProductInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(productInstance);

        assertThat(informer.getProductInstance("aLineItemId"), is(productInstance));
        assertThat(informer.getProductInstance("aLineItemId"), is(productInstance));
        verify(futureProductInstanceClient, times(1)).get(any(LineItemId.class));
    }

    @Test
    public void shouldCacheSiteDTO() throws Exception {
        com.bt.rsqe.customerrecord.SiteDTO site = SiteDTOFixture.aSiteDTO().build();
        CustomerResource customerResource = mock(CustomerResource.class);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(CUSTOMER_ID)).thenReturn(siteResource);
        when(siteResource.get("1", PROJECT_ID)).thenReturn(site);

        assertThat(informer.getSite("1"), is(site));
        assertThat(informer.getSite("1"), is(site));
        verify(expedioClientResources, times(1)).getCustomerResource();
    }

    @Test
    public void shouldCacheCentralSite() throws Exception {
        com.bt.rsqe.customerrecord.SiteDTO site = SiteDTOFixture.aSiteDTO().build();
        CustomerResource customerResource = mock(CustomerResource.class);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(CUSTOMER_ID)).thenReturn(siteResource);
        when(siteResource.getCentralSite(PROJECT_ID)).thenReturn(site);

        assertThat(informer.getCentralSite(), is(site));
        assertThat(informer.getCentralSite(), is(site));
        verify(expedioClientResources, times(1)).getCustomerResource();
    }

    @Test
    public void shouldGetProjectId() throws Exception {
        assertThat(informer.getProjectId(), is(PROJECT_ID));
    }

    @Test
    public void shouldGetQuoteOptionId() throws Exception {
        assertThat(informer.getQuoteOptionId(), is(QUOTE_OPTION_ID));
    }

    @Test
    public void shouldGetCustomerId() throws Exception {
        assertThat(informer.getCustomerId(), is(CUSTOMER_ID));
    }
}
