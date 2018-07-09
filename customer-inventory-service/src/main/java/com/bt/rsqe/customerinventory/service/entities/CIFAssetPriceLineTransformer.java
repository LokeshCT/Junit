package com.bt.rsqe.customerinventory.service.entities;

import com.bt.rsqe.customerinventory.parameter.PriceFrequency;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetPriceEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetPriceLineEntity;
import com.bt.rsqe.customerinventory.repository.jpa.entities.details.AssetPriceDetails;
import com.bt.rsqe.customerinventory.repository.jpa.entities.details.AssetPriceLineDetails;
import com.bt.rsqe.customerinventory.repository.jpa.keys.FutureAssetPriceKey;
import com.bt.rsqe.customerinventory.repository.jpa.keys.FutureAssetPriceLineKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPrice;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPriceLine;
import com.bt.rsqe.enums.Currency;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CIFAssetPriceLineTransformer {
    public static FutureAssetPriceLineEntity toPriceLineEntity(CIFAsset cifAsset, CIFAssetPriceLine priceLine) {
        FutureAssetPriceLineKey priceLineKey = new FutureAssetPriceLineKey(priceLine.getPriceLineId(),
                                                                           cifAsset.getLineItemId());

        AssetPriceLineDetails priceLineDetails = new AssetPriceLineDetails(priceLine.getPriceCode(), priceLine.getPpsrId(),
                                                                           priceLine.getPriceType(), priceLine.getPriceFrequency(),
                                                                           priceLine.getDscription(), priceLine.getPmfId(),
                                                                           priceLine.getLosb(), priceLine.getInventoryType(),
                                                                           priceLine.getStatus(), priceLine.getBasisOfCharge(),
                                                                           priceLine.getRegion(), priceLine.getPriceLineName(),
                                                                           priceLine.getRevenueOwner(), priceLine.getTariffType(),
                                                                           priceLine.getLocs(), priceLine.getChargingSchemeName(),
                                                                           priceLine.getRated(),
                                                                           getBillingsStartDateForSqlOrNull(priceLine.getBillingsStartDate()),
                                                                           getBillingsStartDateForSqlOrNull(priceLine.getBillingEndDate()),
                                                                           priceLine.getClientPOReference(),
                                                                           getBillingsStartDateForSqlOrNull(priceLine.getPriceLineExpiryDate()),
                                                                           priceLine.getTariffName(),
                                                                           priceLine.getVendorDiscountRef());

        SortedSet<FutureAssetPriceEntity> prices = new TreeSet<FutureAssetPriceEntity>();
        for (CIFAssetPrice cifAssetPrice : priceLine.getPrices()) {
            FutureAssetPriceKey key = new FutureAssetPriceKey(priceLine.getPriceLineId(), cifAsset.getLineItemId(),
                                                              cifAssetPrice.getCategory(),
                                                              cifAssetPrice.getCurrency().name(),
                                                              cifAssetPrice.getClassifier());
            AssetPriceDetails details = new AssetPriceDetails(cifAssetPrice.getPriceBookVersion(),
                                                              cifAssetPrice.getPrice(),
                                                              cifAssetPrice.getDiscountPercent(),
                                                              cifAssetPrice.getPriceIdentifier(),
                                                              cifAssetPrice.getPriceDescription());
            prices.add(new FutureAssetPriceEntity(key, details));
        }

        return new FutureAssetPriceLineEntity(priceLineKey, cifAsset.getAssetKey().getAssetId(),
                                              cifAsset.getAssetKey().getAssetVersion(), priceLineDetails, prices);
    }

    private static Date getBillingsStartDateForSqlOrNull(java.util.Date date) {
        return date == null ? null : new Date(date.getTime());
    }

    public static CIFAssetPriceLine fromPriceLineEntity(FutureAssetPriceLineEntity priceLineEntity) {
        List<CIFAssetPrice> prices = new ArrayList<CIFAssetPrice>();

        for (FutureAssetPriceEntity priceEntity : priceLineEntity.getPrices()) {
            prices.add(new CIFAssetPrice(PriceCategory.valueOf(priceEntity.getCategory()),
                                         Currency.valueOf(priceEntity.getCurrency()),
                                         priceEntity.getClassifier(), priceEntity.getDetails().getPriceBookVersion(),
                                         priceEntity.getDetails().getPrice(), priceEntity.getDetails().getDiscountPercent(),
                                         priceEntity.getDetails().getPriceIdentifier(), priceEntity.getDetails().getPriceDescription()));
        }

        return new CIFAssetPriceLine(priceLineEntity.getPriceLineId(), priceLineEntity.getDetails().getPriceCode(),
                                                            priceLineEntity.getDetails().getPpsrId(),
                                                            PriceType.valueOf(priceLineEntity.getDetails().getPriceType()),
                                                            PriceFrequency.valueOf(priceLineEntity.getDetails().getPriceFrequency()),
                                                            priceLineEntity.getDetails().getDescription(),
                                                            priceLineEntity.getDetails().getPmfId(), priceLineEntity.getDetails().getLosb(),
                                                            priceLineEntity.getDetails().getInventoryType(),
                                                            PriceLineStatus.valueOf(priceLineEntity.getDetails().getStatus()),
                                                            priceLineEntity.getDetails().getBasisOfCharge(), priceLineEntity.getDetails().getRegion(),
                                                            priceLineEntity.getDetails().getPriceLineName(), priceLineEntity.getDetails().getRevenueOwner(),
                                                            priceLineEntity.getDetails().getTariffType(), priceLineEntity.getDetails().getLocs(),
                                                            priceLineEntity.getDetails().getChargingSchemeName(), priceLineEntity.getDetails().getRated(),
                                                            priceLineEntity.getDetails().getBillingsStartDate(), priceLineEntity.getDetails().getBillingEndDate(),
                                                            priceLineEntity.getDetails().getClientPOReference(), priceLineEntity.getDetails().getPriceLineExpiryDate(),
                                                            priceLineEntity.getDetails().getTariffName(), priceLineEntity.getDetails().getVendorDiscountRef(),
                                                            prices);
    }
}
