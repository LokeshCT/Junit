package com.bt.rsqe.inlife.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.dto.PriceUpdateDTO;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.keys.PriceUpdateKey;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class PriceUpdater{
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    private ProductInstanceClient instanceClient;

    PriceUpdater(ProductInstanceClient instanceClient){
        this.instanceClient = instanceClient;
    }

    public void updatePrices(String pmfId, ProductInstance productInstance, Map<PriceUpdateKey, BigDecimal> convertedPrices) {
        boolean priceUpdated = false;
        final String productInstanceId = productInstance.getProductInstanceId().getValue();
        final Long productInstanceVersion = productInstance.getProductInstanceVersion();
        List<PriceUpdateDTO> priceUpdates = newArrayList();
        final boolean bfgUpliftRequired = AssetVersionStatus.IN_SERVICE.equals(productInstance.getAssetVersionStatus());
        for(PriceLine priceline:productInstance.getPriceLines()){
            if(priceline.getPmfId().equalsIgnoreCase(pmfId)){
               final BigDecimal price = convertedPrices.get(new PriceUpdateKey(priceline.getPriceType(), priceline.getPpsrId(), pmfId));
                LOG.convertedPrice(productInstanceId,price);
               if(isNotNull(price)) {
                   priceline.getChargePrice().setPrice(price);
                   priceUpdated = true;
                   if(bfgUpliftRequired){
                    priceUpdates.add(new PriceUpdateDTO(productInstanceId,
                                                        productInstanceVersion,
                                                        priceline.getId(),
                                                        priceline.getPriceType(),
                                                        priceline.getPpsrId(),
                                                        price));
                   }
               }
           }
        }
        if(priceUpdated){
            instanceClient.put(productInstance);
            LOG.cifDataUpdated();

            if (bfgUpliftRequired) {
                LOG.bfgDataStarted();
                for (PriceUpdateDTO updateDTO : priceUpdates) {
                    instanceClient.updatePricesInBFG(updateDTO);

                }

            }
        }

    }

    private interface Logger {

        @Log(level = LogLevel.INFO, format = "Price uplift started for asset id : %s  and converted Price: %s ")
        void convertedPrice(String productInstanceId, BigDecimal price);

        @Log(level = LogLevel.INFO, format = "Price uplift completed for  cif")
        void cifDataUpdated();

        @Log(level = LogLevel.INFO, format = "Price uplift require and started for BFG update")
        void bfgDataStarted();


    }
}
