package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModel;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

public enum BCMProductSheetProperty {
    SiteInstallable("Site", 4) {
        @Override
        public List<PricingSheetProductModel> filterProductModel(List<PricingSheetProductModel> productModels) {
            return Lists.newArrayList(Iterables.filter(productModels, new Predicate<PricingSheetProductModel>() {
                @Override
                public boolean apply(PricingSheetProductModel productModel) {
                    return ProductCodes.ConnectAccelerationSite.productCode().equalsIgnoreCase(productModel.getSCode());

                }
            }));
        }

        @Override
        public String getSheetNameFor(String productFamilyName) {
            return productFamilyName + " " + this.sheetName;
        }
    },
    SiteAgnostic("Customer Level Services", 6) {
        @Override
        public List<PricingSheetProductModel> filterProductModel(List<PricingSheetProductModel> productModels) {
            return Lists.newArrayList(Iterables.filter(productModels, new Predicate<PricingSheetProductModel>() {
                @Override
                public boolean apply(PricingSheetProductModel productModel) {
                    return (!productModel.getProductInstance().getProductOffering().isSiteInstallable());
                }
            }));
        }

        @Override
        public String getSheetNameFor(String productFamilyName) {
            return productFamilyName + " " + this.sheetName;
        }

    },
    SpecialBid("CA Special Bid", 2) {
        @Override
        public List<PricingSheetProductModel> filterProductModel(List<PricingSheetProductModel> productModels) {
            return Lists.newArrayList(Iterables.filter(productModels, new Predicate<PricingSheetProductModel>() {
                @Override
                public boolean apply(PricingSheetProductModel productModel) {
                    return (productModel.getProductInstance().isSpecialBid());
                }
            }));
        }

        @Override
        public String getSheetNameFor(String productFamilyName) {
            return productFamilyName + " " + this.sheetName;
        }
    },
    ProductInfo("Product Level Info",3){
        @Override
        public List<PricingSheetProductModel> filterProductModel(List<PricingSheetProductModel> productModels) {
            return null;
        }

        @Override
        public String getSheetNameFor(String productFamilyName) {
            return null;
        }
    },
    Contract("Contract Definition Products", 7) {
        @Override
        public List<PricingSheetProductModel> filterProductModel(List<PricingSheetProductModel> productModels) {
            return Lists.newArrayList(Iterables.filter(productModels, new Predicate<PricingSheetProductModel>() {
                @Override
                public boolean apply(PricingSheetProductModel productModel) {
                    return productModel.getProductInstance().getProductOffering().isSimpleTypeOf(SimpleProductOfferingType.Contract);
                }
            }));
        }

        @Override
        public String getSheetNameFor(String productFamilyName) {
            return this.sheetName;
        }
    };

    public String sheetName;
    public int costSetCount;

    private BCMProductSheetProperty(String sheetName, int costSetCount) {
        this.sheetName = sheetName;
        this.costSetCount = costSetCount;
    }
    public abstract List<PricingSheetProductModel> filterProductModel(List<PricingSheetProductModel> productModels);

    public static int getCostSetCountFor(String sheetName){
        for(BCMProductSheetProperty property : BCMProductSheetProperty.values()){
            if(property.sheetName.equalsIgnoreCase(sheetName)){
                return property.costSetCount;
            }
        }
        return 0;
    }

    public abstract String getSheetNameFor(String productFamilyName);
}
