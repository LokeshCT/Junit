package com.bt.rsqe.customerinventory.service.updates;


import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadRequest;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ProductOffering;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class SpecialBidAttributesReloadRequestBuilder {

    public List<SpecialBidAttributesReloadRequest> buildRequests(final AssetKey assetKey, List<CharacteristicChange> characteristicChanges) {
        return newArrayList(FluentIterable.from(characteristicChanges).filter(new Predicate<CharacteristicChange>() {
            @Override
            public boolean apply(CharacteristicChange input) {
                return ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME.equals(input.getName()) && isNotEmpty(input.getNewValue());
            }
        }).transform(new Function<CharacteristicChange, SpecialBidAttributesReloadRequest>() {
            @Override
            public SpecialBidAttributesReloadRequest apply(CharacteristicChange input) {
                return new SpecialBidAttributesReloadRequest(assetKey, input.getName(), input.getNewValue());
            }
        }));

    }


}
