package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadRequest;
import com.bt.rsqe.domain.AssetKey;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SpecialBidAttributesRebuildRequestBuilderTest {
    @Test
    public void shouldBuildSpecialBidAttributesReloadRequest() {
        List<SpecialBidAttributesReloadRequest> attributesRefreshRequests = new SpecialBidAttributesReloadRequestBuilder().buildRequests(new AssetKey("anAssetId", 1L), newArrayList(new CharacteristicChange(SPECIAL_BID_TEMPLATE_RESERVED_NAME, "newTemplateName"),
                new CharacteristicChange("OtherAttribute", "123")));

        assertThat(attributesRefreshRequests.size(), is(1));
        assertThat(attributesRefreshRequests, hasItem(new SpecialBidAttributesReloadRequest(new AssetKey("anAssetId", 1L), SPECIAL_BID_TEMPLATE_RESERVED_NAME, "newTemplateName")));
    }

    @Test
    public void shouldNotBuildSpecialBidAttributesReloadRequestWhenTpeTemplateNameIsNull() {
        List<SpecialBidAttributesReloadRequest> attributesRefreshRequests = new SpecialBidAttributesReloadRequestBuilder().buildRequests(new AssetKey("anAssetId", 1L), newArrayList(new CharacteristicChange(SPECIAL_BID_TEMPLATE_RESERVED_NAME, null),
                new CharacteristicChange("OtherAttribute", "123")));

        assertThat(attributesRefreshRequests.size(), is(0));
    }


}