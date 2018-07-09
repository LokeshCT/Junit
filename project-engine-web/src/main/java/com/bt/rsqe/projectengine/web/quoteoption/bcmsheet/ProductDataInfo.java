package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.project.ProductInstance;

import java.util.List;

public class ProductDataInfo {
    private ProductInstance productInstance;

    private BCMPriceModel bcmPriceModel;

    private List<BCMPriceModel> bcmCostModel;

    public ProductDataInfo(ProductInstance productInstance, BCMPriceModel bcmPriceModel, List<BCMPriceModel> bcmCostModel) {
        this.productInstance = productInstance;
        this.bcmPriceModel = bcmPriceModel;
        this.bcmCostModel = bcmCostModel;
    }

    public ProductInstance getProductInstance() {
        return productInstance;
    }

    public BCMPriceModel getBcmPriceModel() {
        return bcmPriceModel;
    }

    public List<BCMPriceModel> getBcmCostModel() {
        return bcmCostModel;
    }
}
