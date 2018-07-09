package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

/**
 * Created with IntelliJ IDEA.
 * User: 608045304
 * Date: 28/03/14
 * Time: 11:36
 * To change this template use File | Settings | File Templates.
 */
public class PriceLineIndex {

    private Integer priceLineIndex;

    private Integer discountIndex;

    public PriceLineIndex(Integer priceLineIndex, Integer discountIndex) {
        this.priceLineIndex = priceLineIndex;
        this.discountIndex = discountIndex;
    }

    public Integer getPriceLineIndex() {
        return priceLineIndex;
    }

    public void setPriceLineIndex(Integer priceLineIndex) {
        this.priceLineIndex = priceLineIndex;
    }

    public Integer getDiscountIndex() {
        return discountIndex;
    }

    public void setDiscountIndex(Integer discountIndex) {
        this.discountIndex = discountIndex;
    }
}
