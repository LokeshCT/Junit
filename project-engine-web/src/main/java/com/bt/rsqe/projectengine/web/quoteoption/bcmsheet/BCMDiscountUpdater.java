package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.order.OrderItemItemPrice;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PriceLineToProductInstanceMapper;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.AutoPriceAggregator;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;

public class BCMDiscountUpdater {

    private ProductInstanceClient productInstanceClient;
    private AutoPriceAggregator autoPriceAggregator;
    private LineItemFacade lineItemFacade;


    public BCMDiscountUpdater(ProductInstanceClient productInstanceClient, AutoPriceAggregator autoPriceAggregator, LineItemFacade lineItemFacade) {
        this.productInstanceClient = productInstanceClient;
        this.autoPriceAggregator = autoPriceAggregator;
        this.lineItemFacade = lineItemFacade;
    }


    public void updateDiscountsFrom(List<ImportDiscounts> discounts) {
        for (ImportDiscounts discount : discounts) {
            boolean isUpdated = false;
            ProductInstance productInstance = productInstanceClient.getByAssetKey(new LengthConstrainingProductInstanceId(discount.getProductInstanceId()),
                                                                                  new ProductInstanceVersion(discount.getProductInstanceVersion()));

            MergeResult mergeResult = productInstanceClient.getAssetsDiff(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()),
                                                                          new ProductInstanceVersion(productInstance.getProductInstanceVersion()),
                                                                          isNotNull(productInstance.getAssetSourceVersion()) ? new ProductInstanceVersion(productInstance.getAssetSourceVersion()) : null,
                                                                          InstanceTreeScenario.PROVIDE);

            Map<String, Double> priceLineToDiscountMap = discount.getPriceLineToDiscountMap();
            Map<ProductInstanceId, ProductInstance> allProducts = productInstance.flattenMeAndMyRelatedInstances();
            for(ProductInstance relatedProductInstance : allProducts.values()) {
                updatePriceLines(relatedProductInstance, mergeResult, priceLineToDiscountMap);
            }


        }
    }

    private void updatePriceLines(ProductInstance productInstance, MergeResult mergeResult, Map<String, Double> priceLineToDiscountMap) {
        boolean isUpdated = false;
        for (PriceLine priceLine : productInstance.getPriceLines()) {
            if (priceLineToDiscountMap.keySet().contains(priceLine.getId())) {
                ChangeType changeType = mergeResult.changeFor(new OrderItemItemPrice(new PriceLineToProductInstanceMapper(priceLine).map()));
                if(changeType.isChange()){
                    Double discountValue = priceLineToDiscountMap.get(priceLine.getId());
                    BigDecimal discountPercentage = new BigDecimal(discountValue*100);
                    priceLine.getChargePrice().setDiscountPercentage(discountPercentage.setScale(2, RoundingMode.HALF_EVEN));
                    isUpdated = true;
                }
            }
        }
        if (isUpdated) {
            productInstanceClient.put(refreshLockVersion(productInstance));
            QuoteOptionItemDTO quoteOptionItemDTO = lineItemFacade.projectResource.quoteOptionResource(productInstance.getProjectId()).quoteOptionItemResource(productInstance.getQuoteOptionId()).get(productInstance.getLineItemId());
            autoPriceAggregator.aggregatePricesOf(quoteOptionItemDTO.contractDTO.priceBooks.get(0), new LineItemId(productInstance.getLineItemId()));
        }
    }

    private ProductInstance refreshLockVersion(ProductInstance productInstance) {
        int currentLockVersion = productInstanceClient.getCurrentLockVersion(new LineItemId(productInstance.getLineItemId()));
        productInstance.setLineItemLockVersion(currentLockVersion);
        return productInstance;
    }
}

