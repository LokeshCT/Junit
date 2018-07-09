package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceResponse;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.ChargingScheme;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.PricingStatus;
import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.persistence.NoResultException;
import java.util.Collections;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.updates.AssetKeyMatcher.assetKeyMatcher;
import static com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme.PriceVisibility.Sales;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class InvalidatePriceUpdaterTest {

    private CIFAssetOrchestrator cifAssetOrchestrator;
    private InvalidatePriceUpdater updater;
    private PmrHelper pmrHelper;

    @Before
    public void setup() {
        cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        pmrHelper = mock(PmrHelper.class);
        updater = new InvalidatePriceUpdater(cifAssetOrchestrator, pmrHelper, new PricingStatusHelper(pmrHelper));

    }

    @Test
    public void shouldDoNothingIfPricingStatusIsNotApplicableAndNoPricelinesAndNoChargingSchemes() {
        // Setup
        AssetKey assetKey = new AssetKey("assetKey", 1);
        InvalidatePriceRequest request = new InvalidatePriceRequest("lineItemId", 1, assetKey, InvalidatePriceRequest.ChangeType.PriceAffectingChange);
        CIFAsset aCifAsset = CIFAssetFixture.aCIFAsset().
                withPricingStatus(PricingStatus.NOT_APPLICABLE).
                withRelationships(0).
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, anyListOf(CIFAssetExtension.class)))).thenReturn(aCifAsset);

        ProductOffering productOffering = ProductOffering.Builder.offeringFor(new ProductIdentifier("productOffering", "1")).buildOffering();
        when(pmrHelper.getProductOffering(any(CIFAsset.class))).thenReturn(productOffering);

        // Execute
        InvalidatePriceResponse response = updater.performUpdate(request);

        // Assert
        assertThat(response.getRequest(), is(request));

        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(argumentCaptor.capture());
        CIFAsset cifAsset = argumentCaptor.getValue();
        assertThat(cifAsset.getPricingStatus(), is(PricingStatus.NOT_APPLICABLE));
    }

    @Test
    public void shouldInvalidatePricingStatusOnSingleAsset() {
        // Setup
        AssetKey assetKey = new AssetKey("assetKey", 1);
        InvalidatePriceRequest request = new InvalidatePriceRequest("lineItemId", 1, assetKey, InvalidatePriceRequest.ChangeType.PriceAffectingChange);

        CIFAsset aCifAsset = CIFAssetFixture.aCIFAsset().
                withPriceLines(1).
                withRelationships(0).
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, anyListOf(CIFAssetExtension.class)))).thenReturn(aCifAsset);
        when(cifAssetOrchestrator.getParentAsset(any(CIFAssetKey.class))).thenReturn(null);

        ProductChargingScheme productChargingScheme = ProductChargingSchemeFixture.aChargingScheme().withName("PriceLine").build();
        ProductOffering productOffering = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("productOffering", "1")).
                withChargingSchemes(newArrayList(productChargingScheme)).
                buildOffering();
        when(pmrHelper.getProductOffering(any(CIFAsset.class))).thenReturn(productOffering);

        // Execute
        InvalidatePriceResponse response = updater.performUpdate(request);

        // Assert
        assertThat(response.getRequest().getLineItemId(), is("lineItemId"));
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("assetKey", 1)));

        // Check that the orchestrator save method is called with an asset with priceline invalidated
        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(argumentCaptor.capture());
        CIFAsset cifAsset = argumentCaptor.getValue();
        assertThat(cifAsset.getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAsset.getPriceLines().get(0).getStatus(), is(PriceLineStatus.IN_VALIDATED));
    }

    @Test
    public void shouldInvalidatePricingStatusOnSingleAssetNonPricable() {
        // Setup
        AssetKey assetKey = new AssetKey("assetKey", 1);
        InvalidatePriceRequest request = new InvalidatePriceRequest("lineItemId", 1, assetKey, InvalidatePriceRequest.ChangeType.PriceAffectingChange);

        CIFAsset aCifAsset = CIFAssetFixture.aCIFAsset().
                withPriceLines(1).
                withRelationships(0).
                withPricingStatus(PricingStatus.NOT_PRICED).
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, anyListOf(CIFAssetExtension.class)))).thenReturn(aCifAsset);
        when(cifAssetOrchestrator.getParentAsset(any(CIFAssetKey.class))).thenReturn(null);


        ProductChargingScheme productChargingScheme = ProductChargingSchemeFixture.aChargingScheme().withName("PriceLine").build();
        ProductOffering productOffering = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("productOffering", "1")).
                withChargingSchemes(newArrayList(productChargingScheme)).
                buildOffering();
        when(pmrHelper.getProductOffering(any(CIFAsset.class))).thenReturn(productOffering);

        // Execute
        InvalidatePriceResponse response = updater.performUpdate(request);

        // Assert
        assertThat(response.getRequest().getLineItemId(), is("lineItemId"));
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("assetKey", 1)));

        // Check that the orchestrator save method is called with an asset with priceline invalidated
        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(argumentCaptor.capture());
        CIFAsset cifAsset = argumentCaptor.getValue();
        assertThat(cifAsset.getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAsset.getPriceLines().get(0).getStatus(), is(PriceLineStatus.IN_VALIDATED));
    }

    @Test
    public void shouldInvalidatePricingStatusOnSingleAssetWithPriceLinesAndNotApplicable() {
        // Setup
        AssetKey assetKey = new AssetKey("assetKey", 1);
        InvalidatePriceRequest request = new InvalidatePriceRequest("lineItemId", 1, assetKey, InvalidatePriceRequest.ChangeType.PriceAffectingChange);

        CIFAsset aCifAsset = CIFAssetFixture.aCIFAsset().
                withPriceLines(1).
                withRelationships(0).
                withPricingStatus(PricingStatus.NOT_APPLICABLE).
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, anyListOf(CIFAssetExtension.class)))).thenReturn(aCifAsset);
        when(cifAssetOrchestrator.getParentAsset(any(CIFAssetKey.class))).thenReturn(null);

        ProductChargingScheme productChargingScheme = ProductChargingSchemeFixture.aChargingScheme().withName("PriceLine").build();
        ProductOffering productOffering = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("productOffering", "1")).
                withChargingSchemes(newArrayList(productChargingScheme)).
                buildOffering();
        when(pmrHelper.getProductOffering(any(CIFAsset.class))).thenReturn(productOffering);

        // Execute
        InvalidatePriceResponse response = updater.performUpdate(request);

        // Assert
        assertThat(response.getRequest().getLineItemId(), is("lineItemId"));
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("assetKey", 1)));

        // Check that the orchestrator save method is called with an asset with priceline invalidated
        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(argumentCaptor.capture());
        CIFAsset cifAsset = argumentCaptor.getValue();
        assertThat(cifAsset.getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAsset.getPriceLines().get(0).getStatus(), is(PriceLineStatus.IN_VALIDATED));

    }

    @Test
    public void shouldInvalidatePricingStatusOnAssetAndNotOnParentAsset() {
        // Setup

        final CIFAsset childAsset = CIFAssetFixture.aCIFAsset().
                withID("child").
                withPriceLines(1).
                withRelationships(0).
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        final CIFAsset parentAsset = CIFAssetFixture.aCIFAsset().
                withID("root").
                withPriceLines(1).
                withRelationship(childAsset, "child", RelationshipType.Child).
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();

        when(cifAssetOrchestrator.getAsset(any(CIFAssetKey.class))).thenAnswer(new AnswerAssetKeyWithCIFAsset().
                        put(childAsset.getAssetKey(), childAsset).
                        put(parentAsset.getAssetKey(), parentAsset)
        );

        when(cifAssetOrchestrator.getParentAsset(any(CIFAssetKey.class))).thenAnswer(new AnswerAssetKeyWithCIFAsset().
                        put(childAsset.getAssetKey(), parentAsset)
        );

        ProductChargingScheme productChargingScheme = ProductChargingSchemeFixture.aChargingScheme().withName("PriceLine").build();
        ProductOffering productOffering = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("productOffering", "1")).
                withChargingSchemes(newArrayList(productChargingScheme)).
                buildOffering();
        when(pmrHelper.getProductOffering(any(CIFAsset.class))).thenReturn(productOffering);

        InvalidatePriceRequest request = new InvalidatePriceRequest("lineItemId", 1, childAsset.getAssetKey(), InvalidatePriceRequest.ChangeType.PriceAffectingChange);

        // Execute
        InvalidatePriceResponse response = updater.performUpdate(request);

        // Assert
        assertThat(response.getRequest().getLineItemId(), is("lineItemId"));
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("child", 1)));

        // Check that the orchestrator save method is called with an asset with priceline invalidated
        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(argumentCaptor.capture());
        List<CIFAsset> cifAssets = argumentCaptor.getAllValues();
        assertThat(cifAssets.get(0).getAssetKey(), is(childAsset.getAssetKey()));
        assertThat(cifAssets.get(0).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAssets.get(0).getPriceLines().get(0).getStatus(), is(PriceLineStatus.IN_VALIDATED));

    }

    @Test
    public void shouldInvalidateAssetAndItsRelatedDependantAsset() {
        // Setup

        final CIFAsset childAsset = CIFAssetFixture.aCIFAsset().
                withID("child").
                withPriceLines(1).
                withLineItemId("2").
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        final CIFAsset parentAsset = CIFAssetFixture.aCIFAsset().
                withID("root").
                withPriceLines(1).
                withRelationship(childAsset, "child", RelationshipType.Child).
                withLineItemId("2").
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        final CIFAsset relatedAsset = CIFAssetFixture.aCIFAsset().
                withID("related").
                withPriceLines(2).
                withRelationships(0).
                withLineItemId("1").
                withRelationship(childAsset, "related", RelationshipType.RelatedTo).
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();

        when(cifAssetOrchestrator.getAsset(any(CIFAssetKey.class))).thenAnswer(new AnswerAssetKeyWithCIFAsset().
                        put(childAsset.getAssetKey(), childAsset).
                        put(parentAsset.getAssetKey(), parentAsset).
                        put(relatedAsset.getAssetKey(), relatedAsset)
        );

        when(cifAssetOrchestrator.getParentAsset(argThat(assetKeyMatcher(childAsset)))).thenReturn(parentAsset);
        when(cifAssetOrchestrator.getParentAsset(any(CIFAssetKey.class))).thenThrow(new NoResultException());

        when(cifAssetOrchestrator.getOwnerAssets(any(CIFAssetKey.class))).thenReturn(Collections.<CIFAsset>emptyList());
        when(cifAssetOrchestrator.getOwnerAssets(argThat(assetKeyMatcher(childAsset)))).thenReturn(newArrayList(relatedAsset));


        final ProductChargingScheme productChargingSchemeChild = ProductChargingSchemeFixture.
                aChargingScheme().
                withName("chargingSchemeNameChild").
                withAggregationSet("chargingSchemeName1").
                build();
        final ProductOffering productOfferingChild = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("productOfferingChild", "1")).
                withChargingSchemes(newArrayList(productChargingSchemeChild)).
                buildOffering();
        when(pmrHelper.getProductOffering(childAsset)).thenReturn(productOfferingChild);

        final ProductChargingScheme productChargingSchemeRelated = ProductChargingSchemeFixture.
                aChargingScheme().
                withName("chargingSchemeName1").
                withSetAggregated("chargingSchemeName1").
                build();
        final ProductOffering productOfferingRelated = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("productOfferingRelated", "1")).
                withChargingSchemes(newArrayList(productChargingSchemeRelated)).
                buildOffering();
        when(pmrHelper.getProductOffering(relatedAsset)).thenReturn(productOfferingRelated);

        InvalidatePriceRequest request = new InvalidatePriceRequest("2", 1, childAsset.getAssetKey(), InvalidatePriceRequest.ChangeType.PriceAffectingChange);

        // Execute
        InvalidatePriceResponse response = updater.performUpdate(request);

        // Assert
        assertThat(response.getRequest().getLineItemId(), is("2"));
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("child", 1)));

        assertThat(response.getPriceDeltas().get(0).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(response.getPriceDeltas().get(0).getAssetKey(), is(new AssetKey("child", 1)));
        assertThat(response.getPriceDeltas().get(1).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(response.getPriceDeltas().get(1).getAssetKey(), is(new AssetKey("related", 1)));

        // Check that the orchestrator save method is called with an asset with priceline invalidated
        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(2)).saveAssetAndClearCaches(argumentCaptor.capture());
        List<CIFAsset> cifAssets = argumentCaptor.getAllValues();
        assertThat(cifAssets.get(0).getAssetKey(), is(childAsset.getAssetKey()));
        assertThat(cifAssets.get(0).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAssets.get(0).getPriceLines().get(0).getStatus(), is(PriceLineStatus.IN_VALIDATED));
        assertThat(cifAssets.get(1).getAssetKey(), is(relatedAsset.getAssetKey()));
        assertThat(cifAssets.get(1).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAssets.get(1).getPriceLines().get(0).getStatus(), is(PriceLineStatus.IN_VALIDATED));
        assertThat(cifAssets.get(1).getPriceLines().get(1).getStatus(), is(PriceLineStatus.IN_VALIDATED));

    }

    @Test
    public void shouldInvalidateAssetAndItsRelatedDependantParentAsset() {
        // Setup
        final CIFAsset childAsset = CIFAssetFixture.aCIFAsset().
                withID("child").
                withPriceLines(1).
                withLineItemId("2").
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        final CIFAsset parentAsset = CIFAssetFixture.aCIFAsset().
                withID("root").
                withPriceLines(1).
                withRelationship(childAsset, "child", RelationshipType.Child).
                withLineItemId("2").
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        final CIFAsset relatedAsset = CIFAssetFixture.aCIFAsset().
                withID("related").
                withPriceLines(0).
                withRelationships(0).
                withLineItemId("1").
                withRelationship(childAsset, "related", RelationshipType.RelatedTo).
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        final CIFAsset relatedRootAsset = CIFAssetFixture.aCIFAsset().
                withID("relatedRoot").
                withPriceLines(2).
                withRelationship(relatedAsset, "child", RelationshipType.Child).
                withLineItemId("1").
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();

        when(cifAssetOrchestrator.getAsset(any(CIFAssetKey.class))).
                thenAnswer(new AnswerAssetKeyWithCIFAsset().
                                put(childAsset.getAssetKey(), childAsset).
                                put(parentAsset.getAssetKey(), parentAsset).
                                put(relatedRootAsset.getAssetKey(), relatedRootAsset).
                                put(relatedAsset.getAssetKey(), relatedAsset)
                );

        when(cifAssetOrchestrator.getParentAsset(any(CIFAssetKey.class))).
                thenAnswer(new AnswerAssetKeyWithCIFAsset().
                                put(childAsset.getAssetKey(), parentAsset).
                                put(relatedAsset.getAssetKey(), relatedRootAsset)
                );

        when(cifAssetOrchestrator.getOwnerAssets(any(CIFAssetKey.class))).thenReturn(Collections.<CIFAsset>emptyList());
        when(cifAssetOrchestrator.getOwnerAssets(argThat(assetKeyMatcher(childAsset)))).thenReturn(newArrayList(relatedAsset));


        final ProductChargingScheme productChargingSchemeChild = ProductChargingSchemeFixture.aChargingScheme().withName("chargingSchemeNameChild").withAggregationSet("chargingSchemeName1").build();
        final ProductOffering productOfferingChild = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("productOfferingChild", "1")).
                withChargingSchemes(newArrayList(productChargingSchemeChild)).
                buildOffering();
        when(pmrHelper.getProductOffering(childAsset)).thenReturn(productOfferingChild);

        final ProductOffering productOfferingRelated = ProductOffering.Builder.offeringFor(new ProductIdentifier("productOfferingRelated", "1")).buildOffering();
        when(pmrHelper.getProductOffering(relatedAsset)).thenReturn(productOfferingRelated);

        final ProductChargingScheme productChargingSchemeRelatedRoot = ProductChargingSchemeFixture.aChargingScheme().
                withName("chargingSchemeName1").
                withSetAggregated("chargingSchemeName1").
                build();
        final ProductOffering productOfferingRelatedRoot = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("productOfferingRelatedRoot", "1")).
                withChargingSchemes(newArrayList(productChargingSchemeRelatedRoot)).
                buildOffering();
        when(pmrHelper.getProductOffering(relatedRootAsset)).thenReturn(productOfferingRelatedRoot);

        InvalidatePriceRequest request = new InvalidatePriceRequest("2", 1, childAsset.getAssetKey(), InvalidatePriceRequest.ChangeType.PriceAffectingChange);

        // Execute
        InvalidatePriceResponse response = updater.performUpdate(request);

        // Assert
        assertThat(response.getRequest().getLineItemId(), is("2"));
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("child", 1)));

        assertThat(response.getPriceDeltas().get(0).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(response.getPriceDeltas().get(0).getAssetKey(), is(new AssetKey("child", 1)));
        assertThat(response.getPriceDeltas().get(1).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(response.getPriceDeltas().get(1).getAssetKey(), is(new AssetKey("relatedRoot", 1)));

        // Check that the orchestrator save method is called with an asset with priceline invalidated
        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(2)).saveAssetAndClearCaches(argumentCaptor.capture());
        List<CIFAsset> cifAssets = argumentCaptor.getAllValues();
        assertThat(cifAssets.get(0).getAssetKey(), is(childAsset.getAssetKey()));
        assertThat(cifAssets.get(0).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAssets.get(0).getPriceLines().get(0).getStatus(), is(PriceLineStatus.IN_VALIDATED));
        assertThat(cifAssets.get(1).getAssetKey(), is(relatedRootAsset.getAssetKey()));
        assertThat(cifAssets.get(1).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAssets.get(1).getPriceLines().get(0).getStatus(), is(PriceLineStatus.IN_VALIDATED));
        assertThat(cifAssets.get(1).getPriceLines().get(1).getStatus(), is(PriceLineStatus.IN_VALIDATED));

    }

    @Test
    public void shouldInvalidateAssetAndRemoveItsAggregations() {
        // Setup

        final CIFAsset relatedAsset = CIFAssetFixture.aCIFAsset().
                withID("related").
                withPriceLines(1).
                withRelationships(0).
                withLineItemId("1").
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        final CIFAsset childAsset = CIFAssetFixture.aCIFAsset().
                withID("child").
                withPriceLines(1).
                withRelationship(relatedAsset, "related", RelationshipType.RelatedTo).
                withLineItemId("2").
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();
        final CIFAsset parentAsset = CIFAssetFixture.aCIFAsset().
                withID("root").
                withPriceLines(1).
                withRelationship(childAsset, "child", RelationshipType.Child).
                withLineItemId("2").
                with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                build();

        when(cifAssetOrchestrator.getAsset(any(CIFAssetKey.class))).thenAnswer(new AnswerAssetKeyWithCIFAsset().
                        put(childAsset.getAssetKey(), childAsset).
                        put(parentAsset.getAssetKey(), parentAsset).
                        put(relatedAsset.getAssetKey(), relatedAsset)
        );

        when(cifAssetOrchestrator.getParentAsset(any(CIFAssetKey.class))).thenAnswer(new AnswerAssetKeyWithCIFAsset().
                        put(childAsset.getAssetKey(), parentAsset)
        );


        final ProductChargingScheme productChargingScheme = ProductChargingSchemeFixture.aChargingScheme().
                withName("chargingSchemeName0").
                withSetAggregated("chargingSchemeName0").
                withPricingStrategy(PricingStrategy.Aggregation).
                build();

        ProductOffering productOffering = mock(ProductOffering.class);
        when(productOffering.getProductChargingSchemes()).thenReturn(newArrayList(productChargingScheme));
        when(productOffering.getProductChargingSchemeOf(ChargingScheme.newInstance("chargingSchemeName0"))).thenReturn(Optional.of(productChargingScheme));
        when(productOffering.getProductChargingSchemeOf(ChargingScheme.newInstance("chargingSchemeName1"))).thenReturn(Optional.<ProductChargingScheme>absent());
        when(productOffering.isPriceable()).thenReturn(true);

        when(pmrHelper.getProductOffering(childAsset)).thenReturn(productOffering);
        when(pmrHelper.getProductOffering(relatedAsset)).thenReturn(productOffering);

        InvalidatePriceRequest request = new InvalidatePriceRequest("2", 1, childAsset.getAssetKey(), InvalidatePriceRequest.ChangeType.RelationshipChange);

        // Execute
        InvalidatePriceResponse response = updater.performUpdate(request);

        // Assert
        assertThat(response.getRequest().getLineItemId(), is("2"));
        assertThat(response.getRequest().getAssetKey(), is(new AssetKey("child", 1)));

        assertThat(response.getPriceDeltas().size(), is(1));
        assertThat(response.getPriceDeltas().get(0).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(response.getPriceDeltas().get(0).getAssetKey(), is(new AssetKey("child", 1)));

        // Check that the orchestrator save method is called with an asset with priceline invalidated
        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(argumentCaptor.capture());
        List<CIFAsset> cifAssets = argumentCaptor.getAllValues();
        assertThat(cifAssets.get(0).getAssetKey(), is(childAsset.getAssetKey()));
        assertThat(cifAssets.get(0).getPricingStatus(), is(PricingStatus.NOT_PRICED));
        assertThat(cifAssets.get(0).getPriceLines().size(), is(0));
    }

    @Test
    public void shouldRefreshPricingStatusToNotPricedWhenInvalidPriceRequestIsOfStencilChangeAndStencilChangeCausesAssetIsPriceable() {

        //Given
        final CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().withID("root").withPriceLines(0).withLineItemId("1").withPricingStatus(PricingStatus.NOT_APPLICABLE)
                .with(new CIFAssetQuoteOptionItemDetail(null, 1, false, false, null, null, false, null, null, null, null, false, ProductCategoryCode.NIL, null, false)).
                        build();
        InvalidatePriceRequest request = new InvalidatePriceRequest("2", 1, cifAsset.getAssetKey(), InvalidatePriceRequest.ChangeType.StencilChange);

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(request.getAssetKey(), CIFAssetExtension.noExtensions()))).thenReturn(cifAsset);
        when(pmrHelper.getProductOffering(cifAsset)).thenReturn(new ProductOfferingFixture("aProductId").withChargingScheme(new ProductChargingScheme("A", PricingStrategy.Aggregation, Sales)).build());
        assertThat(cifAsset.getPricingStatus(), is(PricingStatus.NOT_APPLICABLE));

        // Execute
        updater.performUpdate(request);

        // Verify
        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAssetAndClearCaches(argumentCaptor.capture());

        CIFAsset asset = argumentCaptor.getValue();
        assertThat(asset.getPricingStatus(), is(PricingStatus.NOT_PRICED));
    }


}
