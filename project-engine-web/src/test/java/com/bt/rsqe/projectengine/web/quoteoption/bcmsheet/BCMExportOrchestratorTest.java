package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductGroupName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.SiteId;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BCMExportOrchestratorTest {

    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CURRENCY = "GBP";
    protected static final String CUSTOMER_NAME = "CustomerName";
    protected static final String SALES_REP = "SalesRep";
    protected static final String BID_NUMBER = "BidNumber";
    protected static final String TRADE_LEVEL = "trade level";
    protected static final String SALES_CHANNEL = "SalesChannel";
    protected static final String SIEBEL_ID = "SiebelId";
    protected static final int CONTRACT_TERM = 12;

    private QuoteOptionFacade quoteOptionFacade;
    private QuoteOptionDTO quoteOptionDTO;
    private ExpedioProjectResource expedioProjectResource;
    private ProjectDTO projectDTO;
    private CustomerFacade customerFacade;
    private CustomerDTO customerDTO;
    private ProductInstanceClient productInstanceClient;
    private ProjectResource projectResource;
    private ExpedioClientResources expedioClientResources;
    private PmrClient pmrClient;

    private BCMBidInfoFactory bcmBidInfoFactory;
    private BCMExportOrchestrator bcmExportOrchestrator;
    private BCMProductPerSiteFactory bcmProductPerSiteFactory;
    private BCMProductInstanceInfoFactory bcmProductInstanceInfoFactory;
    private BCMPriceLineInfoFactory bcmPriceLineInfoFactory;
    private BCMSiteDetailsFactory bcmSiteDetailsFactory;
    private BCMInformerFactory bcmInformerFactory;

    @Before
    public void setUp() {
        quoteOptionFacade = mock(QuoteOptionFacade.class);
        expedioProjectResource = mock(ExpedioProjectResource.class);
        customerFacade = mock(CustomerFacade.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        projectResource = mock(ProjectResource.class);
        expedioClientResources = mock(ExpedioClientResources.class);
        pmrClient = mock(PmrClient.class);


        bcmInformerFactory = new BCMInformerFactory(quoteOptionFacade, expedioProjectResource, customerFacade, expedioClientResources, projectResource, productInstanceClient);
        bcmBidInfoFactory = new BCMBidInfoFactory();
        bcmProductPerSiteFactory = new BCMProductPerSiteFactory(pmrClient);
        bcmProductInstanceInfoFactory = new BCMProductInstanceInfoFactory(productInstanceClient, bcmPriceLineInfoFactory, bcmSiteDetailsFactory);
        bcmExportOrchestrator = new BCMExportOrchestrator(bcmInformerFactory, bcmBidInfoFactory, null, bcmProductPerSiteFactory);

        quoteOptionDTO = mock(QuoteOptionDTO.class);
        quoteOptionDTO.contractTerm = String.valueOf(CONTRACT_TERM);
        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(quoteOptionDTO);
        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID).getCurrency()).thenReturn(CURRENCY);

        projectDTO = mock(ProjectDTO.class);
        when(expedioProjectResource.getProject(PROJECT_ID)).thenReturn(projectDTO);
        projectDTO.siebelId = SIEBEL_ID;
        projectDTO.bidNumber = BID_NUMBER;
        projectDTO.salesRepName = SALES_REP;
        projectDTO.tradeLevel = TRADE_LEVEL;

        customerDTO = mock(CustomerDTO.class);
        when(customerFacade.get(CUSTOMER_ID, "")).thenReturn(customerDTO);
        when(customerFacade.get(CUSTOMER_ID, "").getName()).thenReturn(CUSTOMER_NAME);
        when(customerFacade.get(CUSTOMER_ID, "").getSalesChannel()).thenReturn(SALES_CHANNEL);
        when(customerFacade.get(CUSTOMER_ID, "").getSalesChannelType()).thenReturn(SALES_CHANNEL);
    }

    @Test
    public void shouldReturnBCMBidInfo() throws Exception {
        BCMBidInfo bcmBidInfo = bcmExportOrchestrator.buildBCMBidInfo(bcmInformerFactory.informerFor(CUSTOMER_ID, "", PROJECT_ID, QUOTE_OPTION_ID, ""));

        assertThat((String)bcmBidInfo.getQuoteId(), is(PROJECT_ID));
        assertThat(bcmBidInfo.getQuoteOptionId(), is(QUOTE_OPTION_ID));
        assertThat(bcmBidInfo.getQuoteCurrency(), is(CURRENCY));
        assertThat(bcmBidInfo.getOpportunityId(), is(SIEBEL_ID));
        assertThat((String)bcmBidInfo.getBidNumber(), is(BID_NUMBER));
        assertThat(bcmBidInfo.getUsername(), is(SALES_REP));
        assertThat(bcmBidInfo.getTradeLevel(), is(TRADE_LEVEL));
        assertThat(bcmBidInfo.getCustomerName(), is(CUSTOMER_NAME));
        assertThat(bcmBidInfo.getSalesChannel(), is(SALES_CHANNEL));
        assertThat(bcmBidInfo.getContractTerm(), is(CONTRACT_TERM));
    }

    @Test
    public void shouldReturnBCMProductPerSiteList() throws Exception {
        String siteName = "testSite";
        String countryName = "testCountry";
        String cityName = "TestCity";

        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(quoteOptionItemResource);
        QuoteOptionItemDTO quoteOptionItemDTO = mock(QuoteOptionItemDTO.class);
        quoteOptionItemDTO.id = "1234";
        List<QuoteOptionItemDTO> quoteOptionItemDTOList = newArrayList(quoteOptionItemDTO);
        when(quoteOptionItemResource.get()).thenReturn(quoteOptionItemDTOList);
        ProductInstance productInstance = mock(ProductInstance.class);
        when(productInstanceClient.get(new LineItemId("1234"))).thenReturn(productInstance);
        ProductOffering productOffering = mock(ProductOffering.class);
        Pmr.ProductOfferings productOfferings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance(quoteOptionItemDTO.sCode))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(productOffering);
        when(productOffering.isSiteInstallable()).thenReturn(true);
        when(productOffering.isInFrontCatalogue()).thenReturn(true);
        CustomerResource customerResource = mock(CustomerResource.class);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(CUSTOMER_ID)).thenReturn(siteResource);
        when(productInstance.getSiteId()).thenReturn("1234");
        SiteDTO siteDTO = mock(SiteDTO.class);
        when(siteResource.get("1234", PROJECT_ID)).thenReturn(siteDTO);
        SiteId siteId = mock(SiteId.class);
        when(siteDTO.getSiteId()).thenReturn(siteId);
        when(siteId.getValue()).thenReturn(new Long("1234"));
        ProductGroupName productGroupName = mock(ProductGroupName.class);
        when(productOffering.getProductGroupName()).thenReturn(productGroupName);
        when(productGroupName.value()).thenReturn("Test Product");
        when(siteDTO.getSiteName()).thenReturn(siteName);
        when(siteDTO.getCountryName()).thenReturn(countryName);
        when(siteDTO.getCity()).thenReturn(cityName);

        Map<Long, BCMProductPerSite> productPerSiteList = bcmExportOrchestrator.buildBCMProductPerSiteList(bcmInformerFactory.informerFor(CUSTOMER_ID, "", PROJECT_ID, QUOTE_OPTION_ID, ""));
        BCMProductPerSite productPerSite = productPerSiteList.get(new Long("1234"));

        assertThat(productPerSite.getName(), is(siteName));
        assertThat(productPerSite.getCountry(), is(countryName));
        assertThat(productPerSite.getCity(), is(cityName));
        assertThat(productPerSite.getProducts().get(0), is("Test Product".toUpperCase()));

        //When FrontCatalogue is False
        when(productOffering.isInFrontCatalogue()).thenReturn(false);
        productPerSiteList = bcmExportOrchestrator.buildBCMProductPerSiteList(bcmInformerFactory.informerFor(CUSTOMER_ID, "", PROJECT_ID, QUOTE_OPTION_ID, ""));
        assertThat(productPerSiteList.size(), is(0));
    }
}
