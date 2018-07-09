package com.bt.rsqe.customerrecord;

import com.bt.rsqe.client.PublicAPI;
import com.bt.rsqe.client.SiteClient;
import com.bt.rsqe.domain.bom.parameters.BfgContact;
import com.bt.rsqe.expedio.site.ContactNotFoundException;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;

@PublicAPI
public class SiteResource implements SiteClient {

    private ExpedioSiteCache expedioSiteCache;

    public enum SiteFilterType {
        All,
        Central,
        Branch
    }

    private RestRequestBuilder restRequestBuilder;

    @Deprecated
    /**
     * @deprecated Use the URI and secret-based constructor instead
     *
     */
    public SiteResource(URI baseUri) {
        restRequestBuilder = new ProxyAwareRestRequestBuilder(UriBuilder.fromUri(baseUri).path("sites").build());
        expedioSiteCache = ExpedioSiteCache.get();
    }

    public SiteResource(ExpedioSiteCache expedioSiteCache, URI baseUri, String secret) {
        this(baseUri);
        restRequestBuilder.withSecret(secret);
        this.expedioSiteCache = expedioSiteCache;
    }

    public List<SiteDTO> getBranchSites(String expedioQuoteRef) {
        return get(expedioQuoteRef, SiteFilterType.Branch);
    }

    public List<String> getBranchSiteIds(String expedioQuoteRef) {
        List<SiteDTO> siteDTOs = get(expedioQuoteRef, SiteFilterType.Branch);
        return newArrayList(Iterables.transform(siteDTOs, new Function<SiteDTO, String>() {
            @Override
            public String apply(SiteDTO input) {
                return input.bfgSiteID;
            }
        }));
    }

    public SiteDTO getCentralSite(String projectId) {
        return getFirst(get(projectId, SiteFilterType.Central), null);
    }

    public List<SiteDTO> getBranchSitesByCountry(String countryCode) {
        return get(countryCode);
    }

    @PublicAPI
    public List<SiteDTO> get(final String countryCode){
        return restRequestBuilder.build("countryCode", countryCode, "bEndSites")
                .get().getEntity(new GenericType<List<SiteDTO>>() {
                });
    }

    @PublicAPI
    public List<SiteDTO> get(final String expedioQuoteRef, final SiteFilterType filterType) {
        return restRequestBuilder.build(new HashMap<String, String>() {{
            put("expedio-quote", expedioQuoteRef);
            put("filter", filterType.name());
        }}).get().getEntity(new GenericType<List<SiteDTO>>() {
        });
    }

    @Override
    @PublicAPI
    public SiteDTO get(final String siteId, final String projectId) {

        if(isNullOrEmpty(siteId)) {
             return getCentralSite(projectId);
        }

/*        return expedioSiteCache.get(siteId, new Callable<SiteDTO>() {
            @Override
            public SiteDTO call() throws Exception {
                return restRequestBuilder.build(siteId, singletonMap("expedio-quote", projectId)).get().getEntity(SiteDTO.class);
            }
        });*/

        return restRequestBuilder.build(siteId, singletonMap("expedio-quote", projectId)).get().getEntity(SiteDTO.class);

    }

    @Override
    @PublicAPI
    public SiteDTO refresh(String siteId, String projectId) {
        expedioSiteCache.invalidate(siteId);
        return get(siteId, projectId);
    }


    @PublicAPI
    public BfgContact getPrimaryServiceDeliveryContact(String siteId)throws ContactNotFoundException {
        return restRequestBuilder.build(siteId, "contacts", "primaryServiceDelivery").get().getEntity(BfgContact.class);
    }

    public SiteDTO getSiteDetails(String siteId) {
        return restRequestBuilder.build(siteId, "siteDetails").get().getEntity(SiteDTO.class);
    }

    @PublicAPI
    public BfgContact getPrimaryCustomerContact(String siteId) {
        return restRequestBuilder.build(siteId, "contacts", "primaryCustomerContact").get().getEntity(BfgContact.class);
    }

    @PublicAPI
    public BfgContact getDeliveryContact(String siteId) {
        return restRequestBuilder.build(siteId, "contacts", "deliveryContact").get().getEntity(BfgContact.class);
    }

}
