package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static org.apache.commons.collections.CollectionUtils.*;

public class BCMDataRowModel {

    private final QuoteOptionItemDTO quoteOptionItem;
    private final SiteDTO site;
    private final List<ProductInstance> childProducts;
    private final BCMPriceModel priceLine;
    private final List<BCMPriceModel> costLines;
    private final ProductInstance rootProductInstance;

    public BCMDataRowModel(ProductInstance rootProductInstance, QuoteOptionItemDTO quoteOptionItem,
                           SiteDTO site, List<ProductInstance> childProducts,
                           BCMPriceModel priceLine,
                           List<BCMPriceModel> costLines) {
        this.rootProductInstance = rootProductInstance;
        this.quoteOptionItem = quoteOptionItem;
        this.site = site;
        this.childProducts = childProducts;
        this.priceLine = priceLine;
        this.costLines = costLines;
    }

    public ProductInstance getRootProductInstance() {
        return rootProductInstance;
    }

    public QuoteOptionItemDTO getQuoteOptionItem() {
        return quoteOptionItem;
    }

    public SiteDTO getSite() {
        return site;
    }

    public List<ProductInstance> getChildProducts() {
        return isNotEmpty(childProducts) ? childProducts : new ArrayList<ProductInstance>();
    }

    public BCMPriceModel getPriceLine() {
        return priceLine;
    }

    public List<BCMPriceModel> getCostLines() {
        return isNotEmpty(costLines) ? costLines : new ArrayList<BCMPriceModel>();
    }

    public List<ProductInstance> getProductInstanceAndItsChildProducts() {
        List<ProductInstance> productInstances = new ArrayList<ProductInstance>();
        if (isNotNull(rootProductInstance)) {
            productInstances.add(rootProductInstance);
        }
        if (isNotEmpty(childProducts)) {
            productInstances.addAll(childProducts);
        }
        return productInstances;
    }

    public String getProductCategoryName() {
        return rootProductInstance.getProductOffering().getProductGroupName().value();
    }

    public String getProductId() {
        return rootProductInstance.getProductIdentifier().getProductId();
    }

    public String getProductName() {
        return rootProductInstance.getProductName();
    }

    public String getLineItemAction() {
        return priceLine.getLineItemAction();
    }

    public String getLineItemOrderStatus() {
        return quoteOptionItem.orderStatus.name();
    }

    public String getContractTerm() {
        return quoteOptionItem.contractTerm;
    }

    public String getProductVersion() {
        return rootProductInstance.getProductOffering().getProductIdentifier().getVersionNumber();
    }

    public String getProductInstanceId() {
        return rootProductInstance.getProductInstanceId().getValue();
    }

}

