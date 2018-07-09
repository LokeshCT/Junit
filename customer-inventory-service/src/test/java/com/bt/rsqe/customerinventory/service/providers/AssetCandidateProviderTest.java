package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.handlers.AssetCandidateHandler;
import com.bt.rsqe.customerinventory.service.orchestrators.AssetModelOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.matchers.LazyValueMatcher.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;

public class AssetCandidateProviderTest {
    private static final AssetKey ASSET_KEY = new AssetKey("anAssetId", 5);
    private static final RelationshipName RELATIONSHIP_NAME = RelationshipName.newInstance("aRelationshipName");
    private static final String QUOTE_OPTION_NAME = "aQuoteOptionName";
    private static final CustomerId CUSTOMER_ID = new CustomerId("aCustomerId");
    private static final ContractId CONTRACT_ID = new ContractId("aContractId");
    private static final String QUOTE_OPTION_ID = "aQuoteOptionId";

    private AssetModelOrchestrator assetModelOrchestrator;
    private AssetCandidateHandler assetCandidateHandler;
    private AssetCandidateProvider assetCandidateProvider;



    @Before
    public void setup() {
        assetModelOrchestrator = mock(AssetModelOrchestrator.class);
        AssetCandidateProviderFactory candidateProviderFactory = new AssetCandidateProviderFactory(assetModelOrchestrator);
        assetCandidateProvider = candidateProviderFactory.choosableProvider();
        assetCandidateHandler = new AssetCandidateHandler(candidateProviderFactory);
    }

    private AssetDTOFixture anAsset(String quoteOptionId) {
        return anAsset(quoteOptionId, UUID.randomUUID().toString());
    }

    private AssetDTOFixture anAsset(String quoteOptionId, String productCode) {
        return AssetDTOFixture.anAsset()
                              .withQuoteOptionId(quoteOptionId)
                              .withProductCode(new ProductCode(productCode))
                              .withCustomerId(CUSTOMER_ID)
                              .withContractId(CONTRACT_ID)
                              .withProjectId(new ProjectId("aProjectId"));
    }

    private void seedAsset(AssetDTO asset) {
        when(assetModelOrchestrator.fetchAsset(ASSET_KEY)).thenReturn(asset);
    }

    private void seedOffering(AssetDTO asset, ProductOffering offering) {
        when(assetModelOrchestrator.fetchOffering(asset)).thenReturn(offering);
    }

    private void seedAvailableOfferings(List<AssetDTO> assets, boolean productAvailable) {
        for(AssetDTO asset : assets) {
            seedAvailableOffering(asset, productAvailable);
        }
    }

    private void seedAvailableOffering(AssetDTO asset, boolean productAvailable) {
        final ProductOffering offering = spy(ProductOfferingFixture.aProductOffering().build());
        doReturn(productAvailable).when(offering).isAvailable();
        when(assetModelOrchestrator.fetchBaseOffering(asset)).thenReturn(offering);
    }

    private void seedQuoteOption(AssetDTO asset, QuoteOptionDTO quoteOption) {
        when(assetModelOrchestrator.fetchQuoteOption(asset)).thenReturn(quoteOption);
    }

    private void seedQuoteOption(AssetDTO asset) {
        seedQuoteOption(asset, QuoteOptionDTOFixture.aQuoteOptionDTO()
                .withName(QUOTE_OPTION_NAME)
                .build());
    }

    private void seedQuoteOptions(List<AssetDTO> assets) {
        for(AssetDTO asset : assets) {
            seedQuoteOption(asset);
        }
    }

    private void seedAssets(String productIdentifier, List<AssetDTO> assets) {
        when(assetModelOrchestrator.fetchAssets(CUSTOMER_ID, CONTRACT_ID, new ProductIdentifier(productIdentifier, "1.0"))).thenReturn(assets);
    }

    private void seedExternalAssets(AssetDTO asset, SalesRelationship salesRelationship, boolean siteMatters, List<AssetDTO> assets) {
        when(assetModelOrchestrator.fetchExternalAssets(eq(asset), eq(salesRelationship.getProductIdentifier()), argThat(isALazyValue(QUOTE_OPTION_NAME)), eq(siteMatters), eq(salesRelationship.getRelationshipName()), eq(false))).thenReturn(assets);
    }

    private void verifyFetchAssets(String productIdentifier) {
        verify(assetModelOrchestrator).fetchAssets(CUSTOMER_ID, CONTRACT_ID, new ProductIdentifier(productIdentifier, "1.0"));
    }

    private ProductOffering siteSpecificOffering(ProductOffering offering, RelationshipName relationshipName) {
        ProductOffering spyOnOffering = spy(offering);
        doReturn(true).when(spyOnOffering).isRelationshipSiteSpecific(relationshipName);
        return spyOnOffering;
    }

    private SalesRelationshipFixture relationshipFor(RelationshipName name, String productCode, RelationshipType type) {
        return SalesRelationshipFixture.aSalesRelationship()
                                       .withProductIdentifier(productCode)
                                       .withRelationName(name.value())
                                       .withRelationType(type);
    }

    private List<AssetDTO> getChoosableCandidates(AssetKey assetKey, RelationshipName relationshipName) {
        final Response response = assetCandidateHandler.getChoosableCandidates(relationshipName.value(), assetKey);
        assertThat(response.getStatus(), is(200));
        return (List<AssetDTO>)response.getEntity();
    }

    @Test
    public void shouldOnlyUseRelatedToDefaultRelationshipsWithTheGivenRelationshipNameWhenFetchingChoosableCandidates() throws Exception {
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        seedOffering(ownerAsset, ProductOfferingFixture.aProductOffering()
                                                       .withSalesRelationship(relationshipFor(RELATIONSHIP_NAME, "P1", RelationshipType.Child))
                                                       .withSalesRelationship(relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo))
                                                       .withSalesRelationship(relationshipFor(RelationshipName.newInstance("someOtherRelationship"), "P3", RelationshipType.RelatedTo))
                                                       .build());

        final List<AssetDTO> eligibleCandidates = newArrayList(anAsset(QUOTE_OPTION_ID).withId("eligibleCandidate1").build(),
                                                               anAsset(QUOTE_OPTION_ID).withId("eligibleCandidate2").build());

        seedAssets("P2", eligibleCandidates);
        seedQuoteOptions(eligibleCandidates);
        seedAvailableOfferings(eligibleCandidates, true);

        List<AssetDTO> candidates = getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);
        verifyFetchAssets("P2");

        assertThat(candidates, hasItems(eligibleCandidates.get(0),
                eligibleCandidates.get(1)));
    }

    @Test
    public void shouldFetchChoosableCandidatesOnSameQuote() throws Exception {
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        seedOffering(ownerAsset, ProductOfferingFixture.aProductOffering()
                                                       .withSalesRelationship(relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo))
                                                       .build());

        final List<AssetDTO> eligibleCandidates = newArrayList(anAsset(QUOTE_OPTION_ID).withId("eligibleCandidate1").build(),
                                                               anAsset("aDifferentQuoteOption").withId("eligibleCandidate2").build());

        seedAssets("P2", eligibleCandidates);
        seedQuoteOptions(eligibleCandidates);
        seedAvailableOfferings(eligibleCandidates, true);

        List<AssetDTO> candidates = getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);

        assertThat(candidates.size(), is(1));
        assertThat(candidates, hasItem(eligibleCandidates.get(0)));
    }

    @Test
    public void shouldFetchOnlyTheLatestVersionOfACandidate() throws Exception {
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        seedOffering(ownerAsset, ProductOfferingFixture.aProductOffering()
                                                       .withSalesRelationship(relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo))
                                                       .build());

        final List<AssetDTO> eligibleCandidates = newArrayList(anAsset(QUOTE_OPTION_ID).withId("sameAssetId").withAssetVersion(1L).build(),
                                                               anAsset(QUOTE_OPTION_ID).withId("sameAssetId").withAssetVersion(5L).build());

        seedAssets("P2", eligibleCandidates);
        seedQuoteOptions(eligibleCandidates);
        seedAvailableOfferings(eligibleCandidates, true);

        List<AssetDTO> candidates = getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);

        assertThat(candidates.size(), is(1));
        assertThat(candidates.get(0).getId(), is("sameAssetId"));
        assertThat(candidates.get(0).getVersion(), is(5L));
    }

    @Test
    public void shouldFetchChoosableCandidatesThatAreCustomerAcceptedOrProvisioningOrInService() throws Exception {
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        seedOffering(ownerAsset, ProductOfferingFixture.aProductOffering()
                                                       .withSalesRelationship(relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo))
                                                       .build());

        final AssetDTO provisioningAsset = anAsset("Q4", "P2").withId("A4").withAssetVersionStatus(AssetVersionStatus.PROVISIONING).build();
        final AssetDTO inserviceAsset = anAsset("Q5", "P2").withId("A5").withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build();
        final AssetDTO customeracceptedAsset = anAsset("Q6", "P2").withId("A6").withAssetVersionStatus(AssetVersionStatus.CUSTOMER_ACCEPTED).build();
        final List<AssetDTO> eligibleCandidates = newArrayList(anAsset("Q1", "P2").withId("A1").withAssetVersionStatus(AssetVersionStatus.DRAFT).build(),
                                                               anAsset("Q2", "P2").withId("A2").withAssetVersionStatus(AssetVersionStatus.OBSOLETE).build(),
                                                               anAsset("Q3", "P2").withId("A3").withAssetVersionStatus(AssetVersionStatus.REJECTED).build(),
                                                               provisioningAsset,
                                                               inserviceAsset,
                                                               customeracceptedAsset);

        seedAssets("P2", eligibleCandidates);
        seedQuoteOptions(eligibleCandidates);
        seedAvailableOfferings(eligibleCandidates, true);

        List<AssetDTO> candidates = getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);

        assertThat(candidates.size(), is(3));
        assertThat(candidates, hasItems(provisioningAsset, inserviceAsset, customeracceptedAsset));
    }

    @Test
    public void shouldFetchChoosableCandidatesThatHaveAnActiveProductOffering() throws Exception {
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        seedOffering(ownerAsset, ProductOfferingFixture.aProductOffering()
                                                       .withSalesRelationship(relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo))
                                                       .build());

        final AssetDTO activeCandidate = anAsset(QUOTE_OPTION_ID).withId("activeCandidate").build();
        final AssetDTO inactiveCandidate = anAsset(QUOTE_OPTION_ID).withId("inactiveCandidate").build();
        final List<AssetDTO> eligibleCandidates = newArrayList(activeCandidate,
                                                               inactiveCandidate);

        seedAssets("P2", eligibleCandidates);
        seedQuoteOptions(eligibleCandidates);
        seedAvailableOffering(activeCandidate, true);
        seedAvailableOffering(inactiveCandidate, false);

        List<AssetDTO> candidates = getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);

        assertThat(candidates.size(), is(1));
        assertThat(candidates, hasItem(activeCandidate));
    }

    @Test
    public void shouldFetchChoosableCandidatesThatAreNotOnAMigrationQuote() throws Exception {
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        seedOffering(ownerAsset, ProductOfferingFixture.aProductOffering()
                                                       .withSalesRelationship(relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo))
                                                       .build());

        final AssetDTO activeCandidate = anAsset(QUOTE_OPTION_ID).withId("activeCandidate").build();
        final AssetDTO inactiveCandidate = anAsset(QUOTE_OPTION_ID).withId("inactiveCandidateButOnMigration").withProjectId(new ProjectId("someOtherProjectId")).build();
        final List<AssetDTO> eligibleCandidates = newArrayList(activeCandidate,
                                                               inactiveCandidate);

        seedAssets("P2", eligibleCandidates);
        seedQuoteOptions(eligibleCandidates);
        seedQuoteOption(inactiveCandidate, QuoteOptionDTOFixture.aQuoteOptionDTO().withMigrationQuote(true).build());
        seedAvailableOffering(activeCandidate, true);
        seedAvailableOffering(inactiveCandidate, false);

        List<AssetDTO> candidates = getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);

        assertThat(candidates.size(), is(2));
        assertThat(candidates, hasItems(activeCandidate, inactiveCandidate));
    }

    @Test
    public void shouldFetchChoosableSiteSpecificExternalCandidates() throws Exception {
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        final SalesRelationshipFixture relationship = relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo);
        seedOffering(ownerAsset, siteSpecificOffering(ProductOfferingFixture.aProductOffering()
                        .withSalesRelationship(relationship)
                        .build(),
                RELATIONSHIP_NAME));

        final List<AssetDTO> eligibleCandidates = newArrayList(anAsset(QUOTE_OPTION_ID).withId("eligibleCandidate1").build());

        seedExternalAssets(ownerAsset, relationship.build(), true, eligibleCandidates);

        List<AssetDTO> candidates = getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);

        assertThat(candidates.size(), is(1));
        assertThat(candidates, hasItem(eligibleCandidates.get(0)));
    }

    @Test
    public void shouldFetchChoosableNonSiteSpecificExternalCandidates() throws Exception {
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        final SalesRelationshipFixture relationship = relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo);
        seedOffering(ownerAsset, ProductOfferingFixture.aProductOffering()
                                                       .withSalesRelationship(relationship)
                                                       .build());

        final List<AssetDTO> eligibleCandidates = newArrayList(anAsset(QUOTE_OPTION_ID).withId("eligibleCandidate1").build());

        seedExternalAssets(ownerAsset, relationship.build(), false, eligibleCandidates);

        List<AssetDTO> candidates = getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);

        assertThat(candidates.size(), is(1));
        assertThat(candidates, hasItem(eligibleCandidates.get(0)));
    }

    @Test
    public void shouldFilterRelationshipsThroughRules() throws Exception {
        // TODO once rule engine works off AssetDTO's
    }

    @Test
    public void shouldFilterCandidatesThroughRules() throws Exception {
        // TODO once rule engine works off AssetDTO's
    }

    @Test
    public void shouldFilterCandidatesByConsumerCardinality() throws Exception {
        // TODO
    }

    @Test
    public void shouldSaveAnAsset() throws Exception {
        AssetDTO assetDTO = AssetDTOFixture.anAsset().build();
        new AssetCandidateProvider(assetModelOrchestrator).putAsset(assetDTO);
        verify(assetModelOrchestrator, times(1)).put(assetDTO);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionUponAnyExceptionDuringAssetSave() throws Exception {
        AssetDTO assetDTO = AssetDTOFixture.anAsset().build();
        doThrow(RuntimeException.class).when(assetModelOrchestrator).put(assetDTO);
        new AssetCandidateProvider(assetModelOrchestrator).putAsset(assetDTO);
    }

    @Test
    public void shouldFetchChoosableCandidatesFromCache() throws Exception {
        CacheAwareTransaction.set(true);
        final AssetDTO ownerAsset = anAsset(QUOTE_OPTION_ID).build();
        seedAsset(ownerAsset);
        seedQuoteOption(ownerAsset);
        seedOffering(ownerAsset, ProductOfferingFixture.aProductOffering()
                .withSalesRelationship(relationshipFor(RELATIONSHIP_NAME, "P2", RelationshipType.RelatedTo))
                .build());

        final List<AssetDTO> eligibleCandidates = newArrayList(anAsset(QUOTE_OPTION_ID).withId("eligibleCandidate1").build(),
                anAsset("aDifferentQuoteOption").withId("eligibleCandidate2").build());

        seedAssets("P2", eligibleCandidates);
        seedQuoteOptions(eligibleCandidates);
        seedAvailableOfferings(eligibleCandidates, true);

        List<AssetDTO> candidates = assetCandidateProvider.getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);
        assertThat(candidates.size(), is(1));
        assertThat(candidates, hasItem(eligibleCandidates.get(0)));

        candidates = assetCandidateProvider.getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME);
        assertThat(candidates.size(), is(1));
        assertThat(candidates, hasItem(eligibleCandidates.get(0)));

        verify(assetModelOrchestrator, times(1)).fetchAsset(ASSET_KEY);
        verify(assetModelOrchestrator, times(1)).fetchOffering(any(AssetDTO.class));
        verify(assetModelOrchestrator, times(1)).fetchAssets(CUSTOMER_ID, CONTRACT_ID, new ProductIdentifier("P2", "1.0"));

    }

    @After
    public void clearCache() {
        AssetCacheManager.clearAllCaches();
    }

}
