package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;


public class BCMPriceLineInfoFactory {

    public Map<BCMPriceLineInfoKey, List<BCMPriceLineInfo>> create(ProductInstance productInstance) {
        Map<BCMPriceLineInfoKey, List<BCMPriceLineInfo>> bcmPriceLineInfoMap = newLinkedHashMap();
        for(PriceLine priceLine:productInstance.getAllPriceLines()) {
            BCMPriceLineInfoKey newKey = new BCMPriceLineInfoKey(
                    null != priceLine.getPmfId()? priceLine.getPmfId():"",
                                                                 null != priceLine.getPriceType() ? priceLine.getPriceType().getValue(): "");

            BCMPriceLineInfo bcmPriceLineInfo = new BCMPriceLineInfo(priceLine.getPriceLineName(),
                                                                      priceLine.getId(),
                                                                      priceLine.getPriceType(),
                                                                      priceLine.getChargePrice(),
                                                                      priceLine.getEupPrice(),
                                                                      priceLine.getStatus(),
                                                                      priceLine.getPmfId(),
                                                                      priceLine.getTariffType(),
                                                                      priceLine.getChargePrice().getDiscountPercentage());

            if(null == bcmPriceLineInfoMap.get(newKey)) {
                List<BCMPriceLineInfo> bcmPriceLineInfoList = newLinkedList();
                bcmPriceLineInfoList.add(bcmPriceLineInfo);
                bcmPriceLineInfoMap.put(newKey, bcmPriceLineInfoList);
            }
            else {
                bcmPriceLineInfoMap.get(newKey).add(bcmPriceLineInfo);
            }
        }
        return  bcmPriceLineInfoMap;
    }
}
