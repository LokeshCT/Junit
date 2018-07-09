package com.bt.rsqe.projectengine.web.view;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class QuoteOptionPricingView {

    private final List<ItemRow> items = newArrayList();

    public List<ItemRow> getItems() {
        return items;
    }

    public void addItem(ItemRow itemRow) {
        items.add(itemRow);
    }

    public static class ItemRow {
        private final String product;
        private final String priceLineName;
        private final String priceType;
        private final String value;
        private final String status;
        private final Double discount;
        private final Double netTotal;
        private final boolean forIfc;

        public ItemRow(String product, String priceLineName, String priceType, String value, String status, Double discount, Double netTotal, boolean forIfc) {
            this.product = product;
            this.priceLineName = priceLineName;
            this.priceType = priceType;
            this.value = value;
            this.status = status;
            this.discount = discount;
            this.netTotal = netTotal;
            this.forIfc = forIfc;
        }

        public String getProduct() {
            return product;
        }

        public String getPriceLineName() {
            return priceLineName;
        }

        public String getPriceType() {
            return priceType;
        }

        public String getValue() {
            return value;
        }

        public String getStatus() {
            return status;
        }

        public Double getDiscount() {
            return discount;
        }

        public Double getNetTotal() {
            return netTotal;
        }

        public boolean isForIfc() {
            return forIfc;
        }
    }

}
