package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.domain.product.AssetProcessType;
import static com.bt.rsqe.domain.product.AssetProcessType.*;

public class MoveType {
    public static AssetProcessType fromAssetSubProcessType(AssetProcessType assetSubProcessType) {
        if (SAME_SITE.equals(assetSubProcessType)) {
            return MOVE_IN_CAMPUS;
        } else if (DIFFERENT_SITE.equals(assetSubProcessType)) {
            return MOVE_IN_COUNTRY;
        }
        return NOT_APPLICABLE;
    }
}
