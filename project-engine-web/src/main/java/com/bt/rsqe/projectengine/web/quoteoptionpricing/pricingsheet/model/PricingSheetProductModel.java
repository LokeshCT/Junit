package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.AccessCaveatDescriptionDTO;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class PricingSheetProductModel extends AbstractPricingSheetProductModel {


    private CaveatResource caveatResource;

    public PricingSheetProductModel(SiteDTO site, ProductInstance productInstance, QuoteOptionItemDTO quoteOptionItem, MergeResult mergeResult, CaveatResource caveatResource, PricingClient pricingClient, Optional<ProductInstance> asIs) {
        super(site, productInstance, quoteOptionItem, mergeResult, pricingClient, asIs);
        this.caveatResource = caveatResource;
    }

    public List<PricingSheetPriceModel> getSummaryPriceLines() {
        return getPriceLines(PriceSuppressStrategy.SummarySheet);
    }

    public List<PricingSheetPriceModel> getDetailedPriceLines() {
        return getPriceLines(PriceSuppressStrategy.DetailedSheet);
    }

    public List<PricingSheetPriceModel> getDetailedPriceLines(String priceType) {
        return filterPriceLineForAction(getPriceLines(PriceSuppressStrategy.DetailedSheet),priceType);
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PricingSheetProductModel that = (PricingSheetProductModel) o;
        return !(productInstance.getProductIdentifier() == null ? that.productInstance.getProductIdentifier() != null : !productInstance.getProductIdentifier().equals(that.productInstance.getProductIdentifier())) &&
        productInstance.getProductInstanceId().getValue().equals(that.productInstance.getProductInstanceId().getValue());
    }

    @Override
    public int hashCode() {
        return this.getProductInstance().getProductIdentifier().hashCode();
    }
     public List<String> getCaveatsList() {
        List<String> caveatIdList = newArrayList();
        try{
        final InstanceCharacteristic instanceCharacteristic = productInstance.getInstanceCharacteristic(ProductOffering.CAVEATS);
        for (String caveatId : Arrays.asList(instanceCharacteristic.getValue().toString().split(","))) {
            caveatIdList.add(caveatId);
        } }catch (Exception ex){
            //
        }
        return caveatIdList;
    }

    public List<String> getCaveatDescriptionList(String caveatId){
        List<String> caveatDescriptionList = new ArrayList();

        for (AccessCaveatDescriptionDTO accessCaveatDescrDTO : caveatResource.getCaveatDescriptionFromId(caveatId)) {
            caveatDescriptionList.add(accessCaveatDescrDTO.getDescription());
        }

        return caveatDescriptionList;
    }
}

