package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;

import java.util.List;


public class ProductDataRowModel {

    private QuoteOptionItemDTO quoteOptionItem;

    private SiteDTO site;

    private ProductDataInfo rootProductInstance;

    private ProductDataInfo cpeProductInstance;
    private ProductDataInfo vendorMaintenanceInstance;
    private List<ProductDataInfo> licences;

    public ProductDataRowModel(QuoteOptionItemDTO quoteOptionItem, SiteDTO site, ProductDataInfo  rootProductInstance, ProductDataInfo cpeProductInstance, ProductDataInfo vendorMaintenanceInstance, List<ProductDataInfo> licences) {
        this.quoteOptionItem = quoteOptionItem;
        this.site = site;
        this.rootProductInstance = rootProductInstance;
        this.cpeProductInstance = cpeProductInstance;
        this.vendorMaintenanceInstance = vendorMaintenanceInstance;
        this.licences = licences;
    }

    public ProductDataRowModel(QuoteOptionItemDTO quoteOptionItem, SiteDTO site, ProductDataInfo rootProductInstance) {
        this.quoteOptionItem = quoteOptionItem;
        this.site = site;
        this.rootProductInstance = rootProductInstance;
    }

    public ProductDataInfo getRootProductInstance() {
        return rootProductInstance;
    }

    public QuoteOptionItemDTO getQuoteOptionItem() {
        return quoteOptionItem;
    }

    public SiteDTO getSite() {
        return site;
    }

    public ProductDataInfo getCpeProductInstance() {
        return cpeProductInstance;
    }

    public ProductDataInfo getVendorMaintenanceInstance() {
        return vendorMaintenanceInstance;
    }

    public ProductDataInfo getHardwareSupplierMaintenanceInstance() {
        return null;
    }

    public List<ProductDataInfo> getLicences() {
        return licences;
    }
}
