package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CancelRelationshipRequestBuilderTest {
    private final String matchingName = "matchingName";
    private final String nonMatchingName = "nonMatchingName";

    private final RelationshipType matchingType = RelationshipType.Child;
    private final RelationshipType nonMatchingType = RelationshipType.MovesTo;

    private final String matchingProductId = "matchingProductId";
    private final String nonMatchingProductId = "nonMatchingProductId";

    private final String matchingStencilId = "matchingStencilId";
    private final String nonMatchingStencilId = "nonMatchingStencilId";

    private final CIFAsset stencilableRelatedAsset = aCIFAsset().withProductIdentifier(matchingProductId, "")
                                                     .with(new CIFAssetStencilDetail(matchingStencilId, "", "", null)).build();

    private final CIFAsset nonStencilableRelatedAsset = aCIFAsset().withProductIdentifier(matchingProductId, "").build();

    private final CancelRelationshipRequestBuilder cancelRelationshipRequestBuilder = new CancelRelationshipRequestBuilder();

    @Test
    public void shouldNotCreateCancelRelationshipRequestForMatchingRelationships() {
        final CIFAsset cifAsset = createCIFAsset(matchingName, matchingType, matchingProductId, matchingStencilId, stencilableRelatedAsset, 0, 1, new ArrayList<String>());

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertThat(cancelRelationshipRequests.size(), is(0));
    }

    @Test
    public void shouldNotCreateCancelRelationshipRequestWhenRelationshipMatchesByLinkedIdentifiers() {
        final CIFAsset cifAsset = createCIFAsset(matchingName, matchingType, nonMatchingProductId, matchingStencilId, stencilableRelatedAsset, 0, 1, newArrayList(matchingProductId));

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertThat(cancelRelationshipRequests.size(), is(0));
    }

    @Test
    public void shouldCreateCancelRelationshipRequestWhenNamedRelationshipsDoesNotExist() {
        final CIFAsset cifAsset = createCIFAsset(nonMatchingName, matchingType, matchingProductId, matchingStencilId, stencilableRelatedAsset, 0, 1, new ArrayList<String>());

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertCancelRequestExists(cifAsset, cancelRelationshipRequests);
    }

    @Test
    public void shouldCreateCancelRelationshipRequestWhenTypeOfRelationshipDoesNotExist() {
        final CIFAsset cifAsset = createCIFAsset(matchingName, nonMatchingType, matchingProductId, matchingStencilId, stencilableRelatedAsset, 0, 1, new ArrayList<String>());

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertCancelRequestExists(cifAsset, cancelRelationshipRequests);
    }

    @Test
    public void shouldCreateCancelRelationshipRequestWhenProductIdOfRelationshipDoesNotExist() {
        final CIFAsset cifAsset = createCIFAsset(matchingName, matchingType, nonMatchingProductId, matchingStencilId, stencilableRelatedAsset, 0, 1, new ArrayList<String>());

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertCancelRequestExists(cifAsset, cancelRelationshipRequests);
    }

    @Test
    public void shouldCreateCancelRelationshipRequestWhenStencilIdOfRelationshipDoesNotExist() {
        final CIFAsset cifAsset = createCIFAsset(matchingName, matchingType, matchingProductId, nonMatchingStencilId, stencilableRelatedAsset, 0, 1, new ArrayList<String>());

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertCancelRequestExists(cifAsset, cancelRelationshipRequests);
    }

    @Test
    public void shouldCreateCancelRelationshipRequestWhenStencilledRelationshipDoesNotExist() {
        final CIFAsset cifAsset = createCIFAsset(nonMatchingName, nonMatchingType, nonMatchingProductId, nonMatchingStencilId, stencilableRelatedAsset, 0, 1, new ArrayList<String>());

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertCancelRequestExists(cifAsset, cancelRelationshipRequests);
    }

    @Test
    public void shouldCreateCancelRelationshipRequestWhenRelationshipDoesNotExist() {
        final CIFAsset cifAsset = createCIFAsset(nonMatchingName, nonMatchingType, nonMatchingProductId, null, nonStencilableRelatedAsset, 0, 1, new ArrayList<String>());

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertThat(cancelRelationshipRequests.size(), is(1));
        assertThat(cancelRelationshipRequests.get(0), is(new CancelRelationshipRequest(cifAsset.getAssetKey(),
                                                                                       cifAsset.getLineItemId(),
                                                                                       cifAsset.getQuoteOptionItemDetail()
                                                                                               .getLockVersion(),
                                                                                       nonStencilableRelatedAsset.getAssetKey(),
                                                                                       matchingName,
                                                                                       nonStencilableRelatedAsset.getProductCode(),
                                                                                       true)));
    }

    @Test
    public void shouldCreateCancelRelationshipRequestWhenRelationshipExistButMaxCardinalitySetToZero() {
        final CIFAsset cifAsset = createCIFAsset(matchingName, matchingType, matchingProductId, matchingStencilId, stencilableRelatedAsset, 0, 0, new ArrayList<String>());

        final List<CancelRelationshipRequest> cancelRelationshipRequests = cancelRelationshipRequestBuilder.removeInvalidRelationships(cifAsset);

        assertCancelRequestExists(cifAsset, cancelRelationshipRequests);
    }

    private void assertCancelRequestExists(CIFAsset cifAsset, List<CancelRelationshipRequest> cancelRelationshipRequests) {
        assertThat(cancelRelationshipRequests.size(), is(1));
        assertThat(cancelRelationshipRequests.get(0), is(new CancelRelationshipRequest(cifAsset.getAssetKey(),
                                                                                       cifAsset.getLineItemId(),
                                                                                       cifAsset.getQuoteOptionItemDetail()
                                                                                                              .getLockVersion(),
                                                                                       stencilableRelatedAsset.getAssetKey(),
                                                                                       matchingName,
                                                                                       stencilableRelatedAsset.getProductCode(),
                                                                                       true)));
    }

    private CIFAsset createCIFAsset(String relationshipName, RelationshipType relationshipType, String productId, String stencilId, CIFAsset relatedAsset, int min, int max, ArrayList<String> linkedIdentifiers) {
        String groupName = "groupName";
        return aCIFAsset().with(new CIFAssetQuoteOptionItemDetail(null, 10, false, false, "", "", false, null, null, "", "name", true, ProductCategoryCode.NIL, null, false))
                          .withRelationshipDefinition(relationshipName, relationshipType, productId, groupName, linkedIdentifiers,
                                                      new CIFAssetRelationshipCardinality(min),
                                                      new CIFAssetRelationshipCardinality(max),
                                                      new CIFAssetRelationshipCardinality(0),
                                                      stencilId)
                          .withRelationship(relatedAsset, matchingName, matchingType).build();
    }
}