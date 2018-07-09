package com.bt.rsqe.customerinventory.service.filters;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeResponse;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

public class ValidationImpactChangeResponseFilterTest {
    @Test
    public void shouldFilterValidationImpactResponseIfCancellingResponseIsAlreadyAvailable() {
        //Given
        CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(new AssetKey("assetIdTwo", 1l), "lineItemId", 15,
                                                                                            new AssetKey("cancellingId", 1), "aRelationshipName", "S123", true);
        CIFAssetUpdateResponse cancelRelationshipResponse = new CancelRelationshipResponse(cancelRelationshipRequest, RelationshipType.RelatedTo, Collections.<CIFAssetUpdateRequest>emptyList());

        ValidationImpactChangeRequest impactRequest = new ValidationImpactChangeRequest(new AssetKey("cancellingId", 1));
        ValidationImpactChangeResponse validationImpactChangeResponse = new ValidationImpactChangeResponse(impactRequest);

        //When
        ValidationImpactChangeResponseFilter responseFilter = new ValidationImpactChangeResponseFilter();
        List<CIFAssetUpdateResponse> responses = responseFilter.filter(newArrayList(cancelRelationshipResponse, validationImpactChangeResponse));

        ///Then
        assertThat(responses.size(), is(1));
        assertThat(responses, hasItem(cancelRelationshipResponse));
    }

    @Test
    public void shouldNotFilterValidationImpactRequestIfCancellingRequestIsAlreadyAvailable() {
        //Given
        CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(new AssetKey("assetIdTwo", 1l), "lineItemId", 15,
                                                                                            new AssetKey("cancellingId", 1), "aRelationshipName", "S123", true);
        CIFAssetUpdateResponse cancelRelationshipResponse = new CancelRelationshipResponse(cancelRelationshipRequest, RelationshipType.RelatedTo, Collections.<CIFAssetUpdateRequest>emptyList());

        ValidationImpactChangeRequest impactRequest = new ValidationImpactChangeRequest(new AssetKey("someOtherAssetId", 1));
        ValidationImpactChangeResponse validationImpactChangeResponse = new ValidationImpactChangeResponse(impactRequest);

        //When
        ValidationImpactChangeResponseFilter responseFilter = new ValidationImpactChangeResponseFilter();
        List<CIFAssetUpdateResponse> responses = responseFilter.filter(newArrayList(cancelRelationshipResponse, validationImpactChangeResponse));

        ///Then
        assertThat(responses.isEmpty(), is(false));
        assertThat(responses, hasItem((CIFAssetUpdateResponse) validationImpactChangeResponse));
    }

}