package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class BCMProductPerSiteFactory {
    private PmrClient pmrClient;

    public BCMProductPerSiteFactory(PmrClient pmrClient) {
        this.pmrClient = pmrClient;
    }

    public Map<Long, BCMProductPerSite> create(BCMInformer informer) {
        Map<Long, BCMProductPerSite> productListPerSite = newLinkedHashMap();

        final List<QuoteOptionItemDTO> quoteOptionItems = informer.getQuoteOptionItems();
        for (QuoteOptionItemDTO quoteOptionItemDTO : quoteOptionItems) {
            ProductInstance toBeProductInstance = informer.getProductInstance(quoteOptionItemDTO.id);
            final ProductOffering productOffering = pmrClient.productOffering(ProductSCode.newInstance(quoteOptionItemDTO.sCode)).get();

            if (productOffering.isSiteInstallable() && productOffering.isInFrontCatalogue()) {
                SiteDTO siteDTO = informer.getSite(toBeProductInstance.getSiteId());

                final Long siteKey = siteDTO.getSiteId().getValue();
                if(productListPerSite.containsKey(siteKey)) {
                    productListPerSite.get(siteKey).getProducts().add(productOffering.getProductGroupName().value().toUpperCase());
                } else {
                    List<String> products = newArrayList(productOffering.getProductGroupName().value().toUpperCase());
                    productListPerSite.put(siteKey, new BCMProductPerSite(siteKey,
                                                                          siteDTO.getSiteName(),
                                                                          siteDTO.getCountryName(),
                                                                          siteDTO.getCity(),
                                                                          products));
                }
            }
        }

        return productListPerSite;
    }
}
