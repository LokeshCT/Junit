package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.cache.ResourceCache;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.*;

public class BCMInformer {
    private final QuoteOptionFacade quoteOptionFacade;
    private final ExpedioProjectResource expedioProjectsResource;
    private final CustomerFacade customerFacade;
    private ExpedioClientResources expedioClientResources;
    private ProjectResource projectResource;
    private ProductInstanceClient futureProductInstanceClient;
    private final String customerId;
    private final String contractId;
    private final String projectId;
    private final String quoteOptionId;
    private String offerName;

    public BCMInformer(final QuoteOptionFacade quoteOptionFacade,
                       final ExpedioProjectResource expedioProjectsResource,
                       final CustomerFacade customerFacade,
                       final ExpedioClientResources expedioClientResources,
                       final ProjectResource projectResource,
                       final ProductInstanceClient futureProductInstanceClient,
                       final String customerId,
                       final String contractId,
                       final String projectId,
                       final String quoteOptionId, String offerName) {
        this.quoteOptionFacade = quoteOptionFacade;
        this.expedioProjectsResource = expedioProjectsResource;
        this.customerFacade = customerFacade;
        this.expedioClientResources = expedioClientResources;
        this.projectResource = projectResource;
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.offerName = offerName;
    }

    private ProjectDTO project;
    public ProjectDTO getProject() {
        if(null == project) {
            project = expedioProjectsResource.getProject(projectId);
        }
        return project;
    }

    private CustomerDTO customer;
    public CustomerDTO getCustomer() {
        if(null == customer) {
            customer = customerFacade.get(customerId, contractId);
        }
        return customer;
    }

    private QuoteOptionDTO quoteOption;
    public QuoteOptionDTO getQuoteOption() {
        if(null == quoteOption) {
            quoteOption = quoteOptionFacade.get(projectId, quoteOptionId);
        }
        return quoteOption;
    }

    private List<QuoteOptionItemDTO> quoteOptionItems;
    public List<QuoteOptionItemDTO> getQuoteOptionItems() {
        if(null == quoteOptionItems) {
            quoteOptionItems = projectResource.quoteOptionResource(projectId)
                                              .quoteOptionItemResource(quoteOptionId)
                                              .get();
        }
        return filterByExportType(quoteOptionItems);
    }

    private List<QuoteOptionItemDTO> filterByExportType(List<QuoteOptionItemDTO> quoteOptionItems) {
        return newArrayList(Iterables.filter(quoteOptionItems,new Predicate<QuoteOptionItemDTO>() {
            @Override
            public boolean apply(QuoteOptionItemDTO item) {
                return isEmpty(offerName) || offerName.equalsIgnoreCase("All") ||
                       (isNotEmpty(item.offerName) && item.offerName.equalsIgnoreCase(offerName));
            }
        })
        );
    }

    private ResourceCache<String, ProductInstance> productInstanceCache = new ResourceCache<String, ProductInstance>();
    public ProductInstance getProductInstance(String lineItemId) {
        ProductInstance productInstance = productInstanceCache.get(lineItemId);

        if(null == productInstance) {
            productInstance = futureProductInstanceClient.get(new LineItemId(lineItemId));
            productInstanceCache.put(lineItemId, productInstance);
        }

        return productInstance;
    }

    private ResourceCache<String, SiteDTO> siteCache = new ResourceCache<String, SiteDTO>();
    public SiteDTO getSite(String siteId) {
        SiteDTO site = siteCache.get(siteId);

        if(null == site) {
            site = expedioClientResources.getCustomerResource().siteResource(customerId).get(siteId, projectId);
            siteCache.put(siteId, site);
        }

        return site;
    }

    private SiteDTO centralSite;
    public SiteDTO getCentralSite() {
        if(centralSite == null) {
            centralSite = expedioClientResources.getCustomerResource().siteResource(customerId).getCentralSite(projectId);
        }
        return centralSite;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getOfferName(){
        return this.offerName;
    }

    public String getSalesChannelType(){
        CustomerDTO customerDTO = expedioClientResources.getCustomerResource().getByToken(customerId, UserContextManager.getCurrent().getRsqeToken());
        return customerDTO.getSalesChannelType();
    }
}
