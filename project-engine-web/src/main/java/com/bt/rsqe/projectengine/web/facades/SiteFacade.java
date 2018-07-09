package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SiteFacade {
    private CustomerResource customers;

    private Map<String, SiteDTO> cache = new ConcurrentHashMap<String, SiteDTO>();
    private Cache<String, List<SiteDTO>> sitesCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();

    public SiteFacade(CustomerResource customers) {
        this.customers = customers;
    }

    public SiteDTO get(String customerId, String projectId, String siteId) {
        if (!cache.containsKey(siteId)) {
            SiteDTO siteDTO =  customers.siteResource(customerId).get(siteId, projectId);
            cache.put(siteId, siteDTO);
        }
        return cache.get(siteId);
    }

    public SiteDTO refresh(String customerId, String projectId, String siteId) {
        cache.put(siteId, customers.siteResource(customerId).refresh(siteId, projectId));
        return cache.get(siteId);
    }

    public List<SiteDTO> getAllBranchSites(final String customerId, final String projectId) {
        /**
         * Toy: Put a local cache in for 1 hour, just in case new sites have been added to Expedio.
         * The proper solution would be to paginate the result from database, but the site name has to be
         * in natural order in order to "order by" in oracle which the solution would be
         *  order by to_number(regexp_substr(SIT_NAME,'^[0-9]+')),
         *  to_number(regexp_substr(SIT_NAME,'[0-9]+$')), SIT_NAME ASC
         * which is nasty, and filter by country would have to be passed in this method.
         * So, the best solution is to just cache at the client side.
         *
         */
        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        try {
            siteDTOs = sitesCache.get(projectId, new Callable<List<SiteDTO>>() {
                @Override
                public List<SiteDTO> call() throws Exception {
                    return customers.siteResource(customerId).getBranchSites(projectId);
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return siteDTOs;
    }

    public List<String> getAllProductQuoteSites(final String customerId, final String contractId, final String projectId, final String quoteOptionId, final String productCode, LineItemFacade lineItemFacade) {
        final List<LineItemModel> models = lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, productCode, true, PriceSuppressStrategy.None);
        List<String> siteDTOList = new ArrayList<String>();

        for (LineItemModel lineItem : models) {
            siteDTOList.add(lineItem.getSite().bfgSiteID);
        }
        return siteDTOList;
    }

    public List<String> getCountries(String customerId, String projectId) {

        List<SiteDTO> siteDTOs = getAllBranchSites(customerId, projectId);
        Set<String> distinctCountries = new HashSet<String>();
        for (SiteDTO siteDTO : siteDTOs) {
            distinctCountries.add(siteDTO.country);
        }

        Comparator<String> comparator = new Comparator<String>() {

            @Override
            public int compare(String country1, String country2) {
                return country1.compareTo(country2);
            }
        };

        ArrayList<String> countries = new ArrayList<String>(distinctCountries);
        Collections.sort(countries, comparator);
        return countries;
    }

    public SiteDTO getCentralSite(String customerId, String projectId) {
        return customers.siteResource(customerId).getCentralSite(projectId);
    }
}
