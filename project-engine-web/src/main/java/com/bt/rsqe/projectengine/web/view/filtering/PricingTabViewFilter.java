package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;

import java.util.List;

// TODO: Remove once new pricing filter is complete.
public class PricingTabViewFilter {

    private static final String PRODUCT_COLUMN = "product";
    private static final String COUNTRY_COLUMN = "country";
    private static final String VENDOR_DISCOUNT_COLUMN = "vendorDiscount";
    private Filters<LineItemModel> filters = new Filters<LineItemModel>();

    public PricingTabViewFilter(FilterValues filterValues) {
        if (!filterValues.getValue(PRODUCT_COLUMN).isEmpty()) {
            filters.add(new ProductFilter(filterValues.getValue(PRODUCT_COLUMN)));
        }
        if (!filterValues.getValue(COUNTRY_COLUMN).isEmpty()) {
            filters.add(new CountryFilter(filterValues.getValue(COUNTRY_COLUMN)));
        }
        if(!filterValues.getValue(VENDOR_DISCOUNT_COLUMN).isEmpty()) {
            filters.add(new VendorDiscountFilter(filterValues.getValue(VENDOR_DISCOUNT_COLUMN)));
        }
    }

    public List<LineItemModel> filter(List<LineItemModel> futureAssetPricesModels) {
        return filters.apply(futureAssetPricesModels);
    }

    private static class ProductFilter implements Filters.Filter<LineItemModel> {
        private final String productName;

        public ProductFilter(String productName) {
            this.productName = productName;
        }

        public boolean apply(LineItemModel model) {
            return productName.equalsIgnoreCase(model.getProductName()) || productName.equalsIgnoreCase(model.getDisplayName());
        }
    }

    private static class CountryFilter implements Filters.Filter<LineItemModel> {
        private final String country;

        public CountryFilter(String country) {
            this.country = country;
        }

        public boolean apply(LineItemModel model) {
            return country.equals(model.getSite().country);
        }
    }

    private static class VendorDiscountFilter implements Filters.Filter<LineItemModel> {
        private final String vendorDiscount;

        public VendorDiscountFilter(String vendorDiscount) {
            this.vendorDiscount = vendorDiscount;
        }

        public boolean apply(LineItemModel model) {
            for(PriceLineModel priceLineModel : model.getFutureAssetPricesModel().getDeepFlattenedPriceLines()) {
                if(vendorDiscount.equals(priceLineModel.getPriceLineDTO().getVendorDiscountRef())) {
                    return true;
                }
            }
            return false;
        }
    }
}
