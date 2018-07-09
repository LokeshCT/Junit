package com.bt.rsqe.customerinventory.service.entities;

import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetProjectedUsageEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.details.ProjectedUsageDetail;
import com.bt.rsqe.customerinventory.repository.jpa.keys.FutureAssetProjectedUsageKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetProjectedUsage;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.utils.countries.Countries;

public class CIFAssetProjectedUsageTransformer {
    public static CIFAssetProjectedUsage fromProjectedUsageEntity(FutureAssetProjectedUsageEntity futureAssetProjectedUsageEntity) {
        return new CIFAssetProjectedUsage(Countries.byIsoStatic(futureAssetProjectedUsageEntity.getDestination()),
                                          TerminationType.valueOf(futureAssetProjectedUsageEntity.getTerminationType()),
                                          futureAssetProjectedUsageEntity.getDetails().getIncomingUnits(),
                                          futureAssetProjectedUsageEntity.getDetails().getOutgoingUnits(),
                                          futureAssetProjectedUsageEntity.getDetails().getBasePrice(),
                                          futureAssetProjectedUsageEntity.getDetails().getBaseRRP());
    }

    public static FutureAssetProjectedUsageEntity toProjectedUsageEntity(CIFAsset cifAsset, CIFAssetProjectedUsage cifAssetProjectedUsage) {
        FutureAssetProjectedUsageKey key = new FutureAssetProjectedUsageKey(cifAsset.getAssetKey().getAssetId(),
                                                                            cifAsset.getLineItemId(),
                                                                            cifAssetProjectedUsage.getDestination(),
                                                                            cifAssetProjectedUsage.getTerminationType(),
                                                                            cifAsset.getAssetKey().getAssetVersion());
        ProjectedUsageDetail details = new ProjectedUsageDetail(cifAssetProjectedUsage.getIncomingUnits(),
                                                                cifAssetProjectedUsage.getOutgoingUnits(),
                                                                cifAssetProjectedUsage.getBasePrice(),
                                                                cifAssetProjectedUsage.getBaseRRP());
        return new FutureAssetProjectedUsageEntity(key, details);
    }
}
