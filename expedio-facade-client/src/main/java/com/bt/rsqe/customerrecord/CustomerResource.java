package com.bt.rsqe.customerrecord;

import com.bt.rsqe.client.CustomerClient;
import com.bt.rsqe.client.PublicAPI;
import com.bt.rsqe.domain.bom.parameters.BfgContact;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;

import javax.ws.rs.core.GenericType;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Collections.*;

@PublicAPI
public class CustomerResource implements CustomerClient {
    private RestRequestBuilder restRequestBuilder;
    private URI uri;
    private String secret;


    private final ExpedioSiteCache siteCache = ExpedioSiteCache.get();
    private static List<ExpedioSalesChannelDto> expedioSalesChannelCache;

    public CustomerResource(URI baseUri, String secret) {
        this(new ProxyAwareRestRequestBuilder(cusomerUri(baseUri)).withSecret(secret), cusomerUri(baseUri), secret);
    }

    private static URI cusomerUri(URI baseUri) {
        return UriBuilder.fromUri(baseUri).path("rsqe").path("expedio").path("customers").build();
    }

    public CustomerResource(RestRequestBuilder restRequestBuilder, URI uri, String secret) {
        this.uri = uri;
        this.secret = secret;
        this.restRequestBuilder = restRequestBuilder;
    }

    @Override
    public SiteResource siteResource(String customerId) {
        return new SiteResource(siteCache, UriBuilder.fromUri(uri).path(customerId).build(), secret);
    }

    public AccountManagerResource accountManagerResource(String customerId, String quoteId) {
        return new AccountManagerResource(UriBuilder.fromUri(uri).path(customerId).path("expedio-quote").path(quoteId).build(), secret);
    }

    public PriceBookResource priceBookResource(String customerId) {
        return new PriceBookResource(UriBuilder.fromUri(uri).path(customerId).build(), secret);
    }

    @Override
    public CustomerDTO get(String customerId, String contractId) {
        return restRequestBuilder.build(customerId, "contracts", contractId).get().getEntity(CustomerDTO.class);
    }

    public BfgContact getBfgContact(String siteId, String customerId) {
        return restRequestBuilder.build(customerId, "sites", siteId, "contacts", "primaryServiceDelivery").get().getEntity(BfgContact.class);
    }

    @Override
    public CustomerDTO getByToken(String customerId, String token) {
        return restRequestBuilder.build(customerId, "token", token).get().getEntity(CustomerDTO.class);
    }

    public List<BillingAccountDTO> billingAccounts(String customerId, String currency) {
        return restRequestBuilder.build(new String[]{customerId, "billing-accounts"}, singletonMap("currency", currency)).get().getEntity(new GenericType<List<BillingAccountDTO>>() {
        });
    }

    public List<BillingAccountDTO> billingAccounts(String customerId) {
        return billingAccounts(customerId, "");
    }

    public SiteDTO findSiteById(final String siteId, final String expedioQuoteRefId, final String customerId) {
        Long.valueOf(siteId);//first stage of refactoring string to long. fail fast for now.

        return siteCache.get(siteId, new Callable<SiteDTO>() {
            @Override
            public SiteDTO call() throws Exception {
                final RestResource resource = restRequestBuilder.build(new String[]{customerId, "sites", siteId},
                                                                       singletonMap("expedio-quote", expedioQuoteRefId));
                final RestResponse restResponse = resource.get();
                return restResponse.getEntity(new GenericType<SiteDTO>() {
                });

            }
        });
    }

    public String getGfrCode(String salesChannelName) {
        String gfrCode = null;
        try {
            if (salesChannelName != null) {
                Map<String, String> qParam = new HashMap<String, String>();
                qParam.put("salesChannel", salesChannelName);

                gfrCode = restRequestBuilder.build("gfrCode", qParam).get().getEntity(String.class);
            }
        } catch (Exception e) {
        }

        return gfrCode;
    }

    public List<ExpedioSalesChannelDto> getAllSalesChannelsWithGfr() {
        if (expedioSalesChannelCache == null) {

            expedioSalesChannelCache = restRequestBuilder.build("salesChannelsWithGfr").get().getEntity(new GenericType<List<ExpedioSalesChannelDto>>() {
            });
        }

        return expedioSalesChannelCache;
    }

    public String getSalesChannelFromGfrCode(String gfrCode) {
        Map<String, String> qParam = new HashMap<>();
        String salesChannel = null;
        try {
            if (gfrCode != null) {
                qParam.put("gfrCode", gfrCode);
                salesChannel = restRequestBuilder.build("salesChannel", qParam).get().getEntity(String.class);

            }
        } catch (Exception e) {
        }

        return salesChannel;
    }

    public SiteDTO getSiteDetail(String customerId, String siteId,String siteType) {
        return restRequestBuilder.build(customerId, "site", siteId,"siteType",siteType).get().getEntity(SiteDTO.class);
    }

}
