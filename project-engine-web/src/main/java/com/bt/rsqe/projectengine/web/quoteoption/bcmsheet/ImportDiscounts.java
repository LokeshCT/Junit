package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import java.util.HashMap;
import java.util.Map;

public class ImportDiscounts {

    private String productInstanceId;

    private long productInstanceVersion;

    private Map<String, Double> priceLineToDiscountMap = new HashMap<String, Double>();

    public ImportDiscounts() {
    }

    public ImportDiscounts(String productInstanceId, long productInstanceVersion, Map<String, Double> priceLineToDiscountMap) {
        this.productInstanceId = productInstanceId;
        this.productInstanceVersion = productInstanceVersion;
        this.priceLineToDiscountMap = priceLineToDiscountMap;
    }

    public Map<String, Double> getPriceLineToDiscountMap() {
        return priceLineToDiscountMap;
    }

    public void setPriceLineToDiscountMap(Map<String, Double> priceLineToDiscountMap) {
        this.priceLineToDiscountMap = priceLineToDiscountMap;
    }

    public void putPriceLineAndDiscount(String priceLineId, Double discount){
        priceLineToDiscountMap.put(priceLineId, discount);
    }

    public void setProductInstanceVersion(long productInstanceVersion) {
        this.productInstanceVersion = productInstanceVersion;
    }

    public String getProductInstanceId() {
        return productInstanceId;
    }

    public void setProductInstanceId(String productInstanceId) {
        this.productInstanceId = productInstanceId;
    }

    public long getProductInstanceVersion() {
        return productInstanceVersion;
    }
}
