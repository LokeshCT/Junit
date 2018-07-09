package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.PriceBooks;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;

import java.util.List;
import java.util.UUID;

public class PriceBookFacade {
    private CustomerResource customerResource;
    private Pmr pmr;

    public PriceBookFacade(CustomerResource customerResource, Pmr pmr) {
        this.customerResource = customerResource;
        this.pmr = pmr;
    }

    public List<PriceBookDTO> inDirectPriceBooks(String customerId, String productScode, ProductCategoryCode productCategoryCode) {
        String productHCode = ProductCategoryCode.catCodeSet(productCategoryCode) ? productCategoryCode.value() : pmr.getProductHCode(productScode).get().getProductId();
        return customerResource.priceBookResource(customerId).getPriceBooks(productHCode);
    }

    public PriceBookDTO getLatestPriceBookForIndirectUser(String customerId, String productCode, ProductCategoryCode productCategoryCode) {
        String productHCode = ProductCategoryCode.catCodeSet(productCategoryCode) ? productCategoryCode.value() : pmr.getProductHCode(productCode).get().getProductId();
        return customerResource.priceBookResource(customerId).defaultPriceBook(productHCode);
    }

    public PriceBookDTO getLatestPriceBookForDirectUser(String productCode, ProductCategoryCode productCategoryCode) {
        PriceBooks priceBooks = new PriceBooks(pmr.getPriceBooks(productCode, productCategoryCode.value()));
        return new PriceBookDTO(UUID.randomUUID().toString(), null, priceBooks.latestPriceBookForDirectUsers().version, null, null, null);
    }
}
