package com.bt.rsqe.customerinventory.service.evaluators;


import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class EvaluatorPathExpressionTest {
    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private Pmr pmrClient = mock(Pmr.class);
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = new CIFAssetCharacteristicEvaluatorFactory(pmrClient, null, null, null);
    private final ArrayList<AssetVersionStatus> allowedStatus = newArrayList(AssetVersionStatus.PROVISIONING, AssetVersionStatus.IN_SERVICE);


    @Test
    public void shouldGetOwnerAssetWhenInSameQuote() {
        CIFAsset baseCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        CIFAsset parentCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        List<CIFAssetExtension> pathExtensions = new ArrayList<CIFAssetExtension>();
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), pathExtensions), allowedStatus, Optional.<String>absent(), Optional.of(baseCifAsset.getQuoteOptionId()))).thenReturn(newArrayList(parentCifAsset));

        EvaluatorPathExpression evaluatorPathExpression = new EvaluatorPathExpression("Owner", baseCifAsset, cifAssetOrchestrator, evaluatorFactory);
        List<CIFAsset> matchingAssets = evaluatorPathExpression.getMatchingAssets(baseCifAsset, baseCifAsset.getQuoteOptionId(), pathExtensions);

        assertThat(matchingAssets.size(), is(1));
        assertThat(matchingAssets.get(0), is(parentCifAsset));
    }

    @Test
    public void shouldGetOwnerAssetUsingProduct() {
        CIFAsset baseCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        CIFAsset parentCifAsset = aCIFAsset().withQuoteOptionId("QO1").withProductIdentifier("S123", "A.1").build();
        List<CIFAssetExtension> pathExtensions = new ArrayList<CIFAssetExtension>();

        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), pathExtensions), allowedStatus, Optional.of("S123"), Optional.of(baseCifAsset.getQuoteOptionId()))).thenReturn(newArrayList(parentCifAsset));

        EvaluatorPathExpression evaluatorPathExpression = new EvaluatorPathExpression("Owner[ProductCode='S123']", baseCifAsset, cifAssetOrchestrator, evaluatorFactory);
        List<CIFAsset> matchingAssets = evaluatorPathExpression.getMatchingAssets(baseCifAsset, baseCifAsset.getQuoteOptionId(), pathExtensions);

        assertThat(matchingAssets.size(), is(1));
        assertThat(matchingAssets.get(0), is(parentCifAsset));
    }

    @Test
    public void shouldNotGetConsumerAssetUsingProductWhenProductCodeFilterNotGiven() {
        CIFAsset baseCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        CIFAsset parentCifAsset = aCIFAsset().withQuoteOptionId("QO1").withProductIdentifier("S123", "A.1").withCharacteristic("Attribute", "A").build();
        List<CIFAssetExtension> pathExtensions = new ArrayList<CIFAssetExtension>();
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), pathExtensions), allowedStatus, Optional.<String>absent(), Optional.of(baseCifAsset.getQuoteOptionId()))).thenReturn(newArrayList(parentCifAsset));

        EvaluatorPathExpression evaluatorPathExpression = new EvaluatorPathExpression("Owner[Attribute='A']", baseCifAsset, cifAssetOrchestrator, evaluatorFactory);
        List<CIFAsset> matchingAssets = evaluatorPathExpression.getMatchingAssets(baseCifAsset, baseCifAsset.getQuoteOptionId(), pathExtensions);

        assertThat(matchingAssets.size(), is(1));
        assertThat(matchingAssets.get(0), is(parentCifAsset));
    }


    @Test
    public void shouldGetAssetOwnerRelationsAssetWhenInSameQuote() {
        CIFAsset baseCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        CIFAsset parentCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        List<CIFAssetExtension> pathExtensions = new ArrayList<CIFAssetExtension>();
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), pathExtensions), allowedStatus, Optional.<String>absent(), Optional.of(baseCifAsset.getQuoteOptionId()))).thenReturn(newArrayList(parentCifAsset));

        EvaluatorPathExpression evaluatorPathExpression = new EvaluatorPathExpression("AssetOwnerRelations", baseCifAsset, cifAssetOrchestrator, evaluatorFactory);
        List<CIFAsset> matchingAssets = evaluatorPathExpression.getMatchingAssets(baseCifAsset, baseCifAsset.getQuoteOptionId(), pathExtensions);

        assertThat(matchingAssets.size(), is(1));
        assertThat(matchingAssets.get(0), is(parentCifAsset));
    }

    @Test
    public void shouldGetOwnerAssetsWhenStatusInInServiceOrProvisioning() {
        CIFAsset baseCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        CIFAsset relatedToCifAsset1 = aCIFAsset().withQuoteOptionId("QO2").withID("assetId1").withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build();
        CIFAsset relatedToCifAsset2 = aCIFAsset().withQuoteOptionId("QO3").withID("assetId2").withAssetVersionStatus(AssetVersionStatus.PROVISIONING).build();
        List<CIFAssetExtension> pathExtensions = new ArrayList<CIFAssetExtension>();
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), pathExtensions), allowedStatus, Optional.<String>absent(), Optional.of(baseCifAsset.getQuoteOptionId()))).thenReturn(newArrayList(relatedToCifAsset1, relatedToCifAsset2));

        EvaluatorPathExpression evaluatorPathExpression = new EvaluatorPathExpression("Owner", baseCifAsset, cifAssetOrchestrator, evaluatorFactory);
        List<CIFAsset> matchingAssets = evaluatorPathExpression.getMatchingAssets(baseCifAsset, baseCifAsset.getQuoteOptionId(), pathExtensions);

        assertThat(matchingAssets.size(), is(2));
        assertThat(matchingAssets, hasItem(relatedToCifAsset1));
        assertThat(matchingAssets, hasItem(relatedToCifAsset2));
    }

    @Test
    public void shouldNotGetAnyOwnerAssetsWhenNotInSameQuoteAlsoNotInStatusOfInServiceOrProvisioning() {
        CIFAsset baseCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        CIFAsset parentCifAsset = aCIFAsset().withQuoteOptionId("QO2").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();
        List<CIFAssetExtension> pathExtensions = new ArrayList<CIFAssetExtension>();
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), pathExtensions), allowedStatus, Optional.<String>absent(), Optional.of(baseCifAsset.getQuoteOptionId()))).thenReturn(newArrayList(parentCifAsset));

        EvaluatorPathExpression evaluatorPathExpression = new EvaluatorPathExpression("Owner", baseCifAsset, cifAssetOrchestrator, evaluatorFactory);
        List<CIFAsset> matchingAssets = evaluatorPathExpression.getMatchingAssets(baseCifAsset, baseCifAsset.getQuoteOptionId(), pathExtensions);

        assertThat(matchingAssets.size(), is(0));
    }

    @Test
    public void shouldTakeTheProperOwnerAssetAfterSorting() {
        CIFAsset baseCifAsset = aCIFAsset().withQuoteOptionId("QO1").build();
        CIFAsset relatedToCifAsset1 = aCIFAsset().withQuoteOptionId("QO1").withID("assetId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();
        CIFAsset relatedToCifAsset2 = aCIFAsset().withQuoteOptionId("QO2").withID("assetId").withAssetVersionStatus(AssetVersionStatus.PROVISIONING).build();
        CIFAsset relatedToCifAsset3 = aCIFAsset().withQuoteOptionId("QO3").withID("assetId").withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build();
        List<CIFAssetExtension> pathExtensions = new ArrayList<CIFAssetExtension>();
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(baseCifAsset.getAssetKey(), pathExtensions), allowedStatus, Optional.<String>absent(), Optional.of(baseCifAsset.getQuoteOptionId()))).thenReturn(newArrayList(relatedToCifAsset3, relatedToCifAsset2, relatedToCifAsset1));
        EvaluatorPathExpression evaluatorPathExpression = new EvaluatorPathExpression("Owner", baseCifAsset, cifAssetOrchestrator, evaluatorFactory);
        List<CIFAsset> matchingAssets = evaluatorPathExpression.getMatchingAssets(baseCifAsset, baseCifAsset.getQuoteOptionId(), pathExtensions);

        assertThat(matchingAssets.size(), is(1));
        assertThat(matchingAssets.get(0), is(relatedToCifAsset1));
    }
}
