package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes;

import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.enums.ProductCodes;
import com.google.common.base.Optional;

public class BFGContactsStrategyFactory {
    public Optional<? extends BFGContactsStrategy> getStrategyFor(SimpleProductOfferingType simpleProductOfferingType) {
        switch (simpleProductOfferingType) {
            case CentralService:
                return Optional.of(new ServiceProductsBFGContactsStrategy());
        }

        return Optional.absent();
    }
}
