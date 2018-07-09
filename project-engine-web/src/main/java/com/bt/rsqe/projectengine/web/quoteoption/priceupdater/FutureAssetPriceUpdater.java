package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.projectengine.web.model.LineItemModel;

public interface FutureAssetPriceUpdater {
    void update(LineItemModel lineItem);
}
