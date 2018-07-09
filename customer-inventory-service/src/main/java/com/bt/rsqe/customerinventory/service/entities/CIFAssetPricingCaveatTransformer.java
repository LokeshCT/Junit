package com.bt.rsqe.customerinventory.service.entities;

import com.bt.rsqe.customerinventory.repository.jpa.entities.AbstractAssetEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetCaveatDocEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetCaveatEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.details.AssetCaveatDetails;
import com.bt.rsqe.customerinventory.repository.jpa.keys.FutureAssetCaveatDocKey;
import com.bt.rsqe.customerinventory.repository.jpa.keys.FutureAssetCaveatKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.domain.project.PricingCaveat;
import com.bt.rsqe.domain.project.PricingCaveatDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CIFAssetPricingCaveatTransformer {
    public static FutureAssetCaveatEntity toPricingCaveatEntity(CIFAsset cifAsset, PricingCaveat pricingCaveat) {
        FutureAssetCaveatKey key = new FutureAssetCaveatKey(pricingCaveat.getCaveatId(),
                                                            cifAsset.getAssetKey().getAssetId(),
                                                            cifAsset.getAssetKey().getAssetVersion());
        AssetCaveatDetails detail = new AssetCaveatDetails(pricingCaveat.getCaveatType(),
                                                           pricingCaveat.getCaveatDescription());
        SortedSet<FutureAssetCaveatDocEntity> docs = new TreeSet<FutureAssetCaveatDocEntity>();
        for (PricingCaveatDoc pricingCaveatDoc : pricingCaveat.getPricingCaveatDocs()) {
            docs.add(new FutureAssetCaveatDocEntity(new FutureAssetCaveatDocKey(cifAsset.getAssetKey().getAssetId(),
                                                                                cifAsset.getAssetKey().getAssetVersion(),
                                                                                pricingCaveat.getCaveatId(),
                                                                                pricingCaveatDoc.getDocName()),
                                                    pricingCaveatDoc.getDocLink()));
        }
        return new FutureAssetCaveatEntity(key, detail, docs);
    }

    public static PricingCaveat fromPricingCaveatEntity(AbstractAssetEntity assetEntity, FutureAssetCaveatEntity futureAssetCaveatEntity) {
        List<PricingCaveatDoc> caveatDocs = new ArrayList<PricingCaveatDoc>();
        for (FutureAssetCaveatDocEntity futureAssetCaveatDocEntity : futureAssetCaveatEntity.getDocs()) {
            caveatDocs.add(new PricingCaveatDoc(futureAssetCaveatDocEntity.getDocName(), futureAssetCaveatDocEntity.getDetails().getDocLink()));
        }
        return new PricingCaveat(assetEntity.getKey().getAssetId(),
                                             assetEntity.getKey().getAssetVersion(),
                                             assetEntity.getDetails().getSiteId(),
                                             futureAssetCaveatEntity.getCaveatId(),
                                             futureAssetCaveatEntity.getDetails().getCaveatType(),
                                             futureAssetCaveatEntity.getDetails().getCaveatDescription(),
                                             caveatDocs);
    }
}
