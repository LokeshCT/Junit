package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.QuoteOptionContext;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicValueEvaluator;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.providers.AssociatedAssetKeyProvider;
import com.bt.rsqe.customerinventory.service.updates.ContributesToChangeRequestBuilder.AssociatedAssetKey;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.DetailedAssetKey;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.DirectAssociation;
import com.bt.rsqe.domain.product.LocalAssociation;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.product.Association.AssociationType.*;
import static com.bt.rsqe.domain.product.extensions.ExpressionExpectedResultType.Boolean;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ContributesToChangeRequestBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private PmrHelper pmrHelper = mock(PmrHelper.class);
    private AssociatedAssetKeyProvider associatedAssetKeyProvider = mock(AssociatedAssetKeyProvider.class);
    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);
    private ContributesToChangeRequestBuilder requestBuilder = new ContributesToChangeRequestBuilder(pmrHelper, associatedAssetKeyProvider, cifAssetOrchestrator, evaluatorFactory);

    @Test
    public void shouldThrowExceptionWhenContributesToReachedMaximumLevel() {
        //Expect
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage(containsString("Stopping the ContributesTo RuleAttributeSource execution as recursion reaches max configured limit 5. There might be infinite loop."));

        //When
        requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", "anAttributeName", 6);
    }


    @Test
    public void shouldReturnEmptyRequestListWhenNoAssociationsAvailableForGivenAttribute() {
        //Given
        ProductOffering productOffering = mock(ProductOffering.class);
        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations("anAttributeName")).thenReturn(Collections.<Association>emptySet());

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", "anAttributeName", 1);

        //Then
        assertThat(updateRequests.isEmpty(), is(true));
    }

    @Test
    public void shouldBuildContributesToRequestListWhenAssociationsAvailableForGivenAttribute() {
        //Given
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation = new DirectAssociation("anAssociatedAttribute",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Child", "Grandchild"),
                new Expression("1.0", Boolean));

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations("anAttributeName")).thenReturn(newHashSet(directAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId", 1L, "aQuoteOptionId")));
        CIFAsset associatedAsset = CIFAssetFixture.aCIFAsset().withID("anAssociatedAssetId").withVersion(1L).withProductIdentifier("anAssociatedProductCode", "A.1").withQuoteOptionId("aQuoteOptionId").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anAssociatedAssetId", 1L), newArrayList(Relationships, CharacteristicAllowedValues)))).thenReturn(associatedAsset);

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", "anAttributeName", 1);

        //Then
        assertThat(updateRequests.size(), is(1));
        CIFAssetUpdateRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("anAssociatedAssetId", 1L), "anAssociatedAttribute", 1);
        assertThat(updateRequests, hasItem(characteristicReloadRequest));
    }

    @Test
    public void shouldBuildContributesToRequestListForAllAttributesDirectAssociationsUponCancellation() {
        //Given
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation_1 = new DirectAssociation("anAssociatedAttribute_1",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode_1", "A.1"),
                newArrayList("Child", "Grandchild"),
                new Expression("1.0", Boolean));

        Association directAssociation_2 = new DirectAssociation("anAssociatedAttribute_2",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode_2", "A.1"),
                newArrayList("Child", "Grandchild"),
                new Expression("1.0", Boolean));

        Association localAssociation = new LocalAssociation("anAssociatedAttribute_3", Association.AssociationType.ATTRIBUTE_SOURCE);

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getDirectAssociations()).thenReturn(newHashSet(directAssociation_1, directAssociation_2, localAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation_1))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId_1", 1L, "aQuoteOptionId")));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation_2))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId_2", 1L, "aQuoteOptionId")));

        CIFAsset associatedAsset_1 = CIFAssetFixture.aCIFAsset().withID("anAssociatedAssetId_1").withVersion(1L).withProductIdentifier("anAssociatedProductCode_1", "A.1").withQuoteOptionId("aQuoteOptionId").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anAssociatedAssetId_1", 1L), newArrayList(Relationships, CharacteristicAllowedValues)))).thenReturn(associatedAsset_1);

        CIFAsset associatedAsset_2 = CIFAssetFixture.aCIFAsset().withID("anAssociatedAssetId_2").withVersion(1L).withProductIdentifier("anAssociatedProductCode_1", "A.1").withQuoteOptionId("aQuoteOptionId").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anAssociatedAssetId_2", 1L), newArrayList(Relationships, CharacteristicAllowedValues)))).thenReturn(associatedAsset_2);

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequestsOnCancellation(new AssetKey("anAssetId", 1L), "aProductCode", 1);

        //Then
        assertThat(updateRequests.size(), is(2));
        CIFAssetUpdateRequest characteristicReloadRequest_1 = new CharacteristicReloadRequest(new AssetKey("anAssociatedAssetId_1", 1L), "anAssociatedAttribute_1", 1);
        CIFAssetUpdateRequest characteristicReloadRequest_2 = new CharacteristicReloadRequest(new AssetKey("anAssociatedAssetId_2", 1L), "anAssociatedAttribute_2", 1);
        assertThat(updateRequests, hasItems(characteristicReloadRequest_1, characteristicReloadRequest_2));
    }

    @Test
    public void shouldFilterOutContributesToRequestListWhenSelfAssociationsAvailableForGivenAttribute() {
        //Given
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation = new DirectAssociation("anAssociatedAttribute",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Child", "Grandchild"),
                null);
        Association otherAttributeLocalAssociation = new LocalAssociation("locallyAssociatedOtherAttribute", ATTRIBUTE_SOURCE);
        Association sameAttributeLocalAssociation = new LocalAssociation("anAttributeName", ATTRIBUTE_SOURCE);

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations("anAttributeName")).thenReturn(newHashSet(directAssociation, otherAttributeLocalAssociation, sameAttributeLocalAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId", 1L, "aQuoteOptionId")));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), otherAttributeLocalAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssetId", 1L, "aQuoteOptionId")));
        CIFAsset associatedAsset = CIFAssetFixture.aCIFAsset().withID("anAssociatedAssetId").withVersion(1L).withProductIdentifier("anAssociatedProductCode", "A.1").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anAssociatedAssetId", 1L), CIFAssetExtension.allExtensions()))).thenReturn(associatedAsset);

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", "anAttributeName", 1);

        //Then
        assertThat(updateRequests.size(), Matchers.is(2));
        CIFAssetUpdateRequest localAttributeReloadRequest = new CharacteristicReloadRequest(new AssetKey("anAssetId", 1L), "locallyAssociatedOtherAttribute", 1);
        CIFAssetUpdateRequest directAttrAssociationReloadRequest = new CharacteristicReloadRequest(new AssetKey("anAssociatedAssetId", 1L), "anAssociatedAttribute", 1);
        assertThat(updateRequests, hasItems(localAttributeReloadRequest, directAttrAssociationReloadRequest));
    }

    @Test
    public void shouldBuildContributesToRequestListWhenAttributeAssociationDoesNotHaveFilterExpression() {
        //Given
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation = new DirectAssociation("anAssociatedAttribute",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Child"), null);

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations("anAttributeName")).thenReturn(newHashSet(directAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId", 1L, "aQuoteOptionId")));
        CIFAsset associatedAsset = CIFAssetFixture.aCIFAsset().withID("anAssociatedAssetId").withVersion(1L).withProductIdentifier("anAssociatedProductCode", "A.1").withQuoteOptionId("aQuoteOptionId").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anAssociatedAssetId", 1L), newArrayList(Relationships, CharacteristicAllowedValues)))).thenReturn(associatedAsset);

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", "anAttributeName", 1);

        //Then
        assertThat(updateRequests.size(), is(1));
        CIFAssetUpdateRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("anAssociatedAssetId", 1L), "anAssociatedAttribute", 1);
        assertThat(updateRequests, hasItem(characteristicReloadRequest));
    }

    @Test
    public void shouldNotBuildContributesToRequestWhenAttributeAssociationFilterIsNotSatisfied() {
        //Given
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation = new DirectAssociation("anAssociatedAttribute",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Child"), new Expression("A = '100'", Boolean));

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations("anAttributeName")).thenReturn(newHashSet(directAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId", 1L, "")));
        CIFAsset associatedAsset = CIFAssetFixture.aCIFAsset().withID("anAssociatedAssetId").withVersion(1L).withProductIdentifier("anAssociatedProductCode", "A.1")
                .withCharacteristic("A", "10").withQuoteOptionId("aQuoteOptionId").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anAssociatedAssetId", 1L), newArrayList(Relationships, CharacteristicAllowedValues)))).thenReturn(associatedAsset);
        when(evaluatorFactory.getCharacteristicEvaluator("A")).thenReturn(new CIFAssetCharacteristicValueEvaluator("A"));

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", "anAttributeName", 1);

        //Then
        assertThat(updateRequests.isEmpty(), is(true));
    }

    @Test
    public void shouldNotBuildContributesToRequestWhenAttributeAssociationFilterExpressionHasSyntaxError() {
        //Given
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation = new DirectAssociation("anAssociatedAttribute",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Child"), new Expression("A =! '100'", Boolean));

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations("anAttributeName")).thenReturn(newHashSet(directAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId", 1L, "")));
        CIFAsset associatedAsset = CIFAssetFixture.aCIFAsset().withID("anAssociatedAssetId").withVersion(1L).withProductIdentifier("anAssociatedProductCode", "A.1")
                .withCharacteristic("A", "10").withQuoteOptionId("aQuoteOptionId").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anAssociatedAssetId", 1L), newArrayList(Relationships, CharacteristicAllowedValues)))).thenReturn(associatedAsset);
        when(evaluatorFactory.getCharacteristicEvaluator("A")).thenReturn(new CIFAssetCharacteristicValueEvaluator("A"));

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", "anAttributeName", 1);

        //Then
        assertThat(updateRequests.isEmpty(), is(true));
    }

    @Test
    public void shouldBuildContributesToRequestListWhenAnAssetContributesToOtherAssetViaRelationshipName() {
        //Given
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation = new DirectAssociation("anAssociatedAttribute",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Child"), null);

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations(RelationshipName.newInstance("aRelationshipName"))).thenReturn(newHashSet(directAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId", 1L, "anAssociatedQuoteOptionId")));
        CIFAsset associatedAsset = CIFAssetFixture.aCIFAsset().withID("anAssociatedAssetId").withVersion(1L).withProductIdentifier("anAssociatedProductCode", "A.1").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(new AssetKey("anAssociatedAssetId", 1L), newArrayList(Relationships, CharacteristicAllowedValues)))).thenReturn(associatedAsset);

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", RelationshipName.newInstance("aRelationshipName"), 1);

        //Then
        assertThat(updateRequests.size(), is(1));
        CIFAssetUpdateRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("anAssociatedAssetId", 1L), "anAssociatedAttribute", 1);
        assertThat(updateRequests, hasItem(characteristicReloadRequest));
    }

    @Test
    public void shouldBuildContributesToRequestListWhenAnAssetContributesToOtherAssetWithInCurrentQuoteOptionViaRelationshipName() {
        //Given
        QuoteOptionContext.set("aQuoteOptionId");
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation = new DirectAssociation("anAssociatedAttribute",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("relatedTo"), null);

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations(RelationshipName.newInstance("aRelationshipName"))).thenReturn(newHashSet(directAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId", 1L, "aQuoteOptionId")));

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", RelationshipName.newInstance("aRelationshipName"), 1);

        //Then
        assertThat(updateRequests.size(), is(1));
    }


    @Test
    public void shouldNotBuildContributesToRequestListWhenAssociationsAvailableForExternalQuoteOption() {
        //Given
        QuoteOptionContext.set("aQuoteOptionId");
        ProductOffering productOffering = mock(ProductOffering.class);
        Association directAssociation = new DirectAssociation("anAssociatedAttribute",
                ATTRIBUTE_SOURCE,
                new ProductIdentifier("anAssociatedProductCode", "A.1"),
                newArrayList("Related"),
                new Expression("1.0", Boolean));

        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
        when(productOffering.getAttributeAssociations("anAttributeName")).thenReturn(newHashSet(directAssociation));
        when(associatedAssetKeyProvider.getKeys(new AssociatedAssetKey(new AssetKey("anAssetId", 1L), directAssociation))).thenReturn(newHashSet(new DetailedAssetKey("anAssociatedAssetId", 1L, "externalQuoteOptionId")));

        //When
        Set<CIFAssetUpdateRequest> updateRequests = requestBuilder.buildRequests(new AssetKey("anAssetId", 1L), "aProductCode", "anAttributeName", 1);

        //Then
        assertThat(updateRequests.size(), is(0));
    }
}