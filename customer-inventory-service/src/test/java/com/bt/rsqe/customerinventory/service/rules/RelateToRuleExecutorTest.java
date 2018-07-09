package com.bt.rsqe.customerinventory.service.rules;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.DirectAssociation;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.extensions.RuleRelateTo;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.domain.product.Association.AssociationType.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class RelateToRuleExecutorTest {
    private static final String RULE_NAME = "ruleName";
    private static final String RELATIONSHIP_NAME = "relationshipName";
    private static final boolean DONT_REPROVIDE = false;
    private static final boolean DO_REPROVIDE = true;
    private static final AssetKey ASSET_KEY = new AssetKey("assetId", 1);
    private static final AssetKey NEW_OWNER_ASSET_KEY = new AssetKey("assetId2", 1);
    private static final String PATH_TO_NEW_OWNER = "relationshipName";
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);
    private final PmrHelper pmrHelper = mock(PmrHelper.class);
    private final CIFAsset newOwnerAsset = aCIFAsset().withID(NEW_OWNER_ASSET_KEY.getAssetId())
                                                      .withVersion(NEW_OWNER_ASSET_KEY.getAssetVersion())
                                                      .build();
    private final CIFAsset initialAsset = aCIFAsset().withID(ASSET_KEY.getAssetId())
                                                     .withVersion(ASSET_KEY.getAssetVersion())
                                                     .withRelationship(newOwnerAsset, RELATIONSHIP_NAME, RelationshipType.Child)
                                                     .build();
    private final RelateToRuleExecutor relateToRuleExecutor = new RelateToRuleExecutor(cifAssetOrchestrator, evaluatorFactory, pmrHelper);

    @Before
    public void setUp() throws Exception {
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(ASSET_KEY, newArrayList(CIFAssetExtension.Relationships)))).thenReturn(initialAsset);
    }

    @Test
    public void shouldReturnNoRequetsWhenNoAssetFoundBasedOnPath() {
        String pathToNoOwner = "pathToNoOwner";
        final RuleRelateTo ruleRelateTo = new RuleRelateTo(RULE_NAME, Expression.alwaysPassesExpression(), RELATIONSHIP_NAME,
                                                           pathToNoOwner, DONT_REPROVIDE);

        final List<CIFAssetUpdateRequest> requestsGenerated = relateToRuleExecutor.execute(ruleRelateTo, initialAsset);

        assertThat(requestsGenerated.size(), is(0));
    }

    @Test
    public void shouldReturnChooseRelationshipRequestsForEachMatchedAsset() {
        final RuleRelateTo ruleRelateTo = new RuleRelateTo(RULE_NAME, Expression.alwaysPassesExpression(), RELATIONSHIP_NAME,
                                                           PATH_TO_NEW_OWNER, DONT_REPROVIDE);

        when(pmrHelper.getProductOffering(initialAsset.getProductCode())).thenReturn(ProductOfferingFixture.aProductOffering().build());

        final List<CIFAssetUpdateRequest> requestsGenerated = relateToRuleExecutor.execute(ruleRelateTo, initialAsset);

        CIFAssetUpdateRequest expectedRequest = new ChooseRelationshipRequest(NEW_OWNER_ASSET_KEY, ASSET_KEY, RELATIONSHIP_NAME, "", 0, UpdateRequestSource.RelateTo);
        assertThat(requestsGenerated.get(0), is(expectedRequest));
    }

    @Test
    public void shouldReturnChooseRelationshipRequestForMatchedAssetAndReloadCharacteristicRequests() {
        final RuleRelateTo ruleRelateTo = new RuleRelateTo(RULE_NAME, Expression.alwaysPassesExpression(), RELATIONSHIP_NAME,
                                                           PATH_TO_NEW_OWNER, DONT_REPROVIDE);

        Association directAttributeAssociation = new DirectAssociation("anAssociatedAttribute",
                                                                       ATTRIBUTE_SOURCE,
                                                                       new ProductIdentifier(newOwnerAsset.getProductCode(), "A.1"),
                                                                       newArrayList("Child", "Grandchild"),
                                                                       null);
        Association directRuleFilterAssociation = new DirectAssociation("anotherAssociatedAttribute",
                                                                        Association.AssociationType.RULE_FILTER,
                                                                        new ProductIdentifier(newOwnerAsset.getProductCode(), "A.1"),
                                                                        newArrayList("Child", "Grandchild"),
                                                                        null);

        ProductOffering productOffering = mock(ProductOffering.class);
        when(pmrHelper.getProductOffering(initialAsset.getProductCode())).thenReturn(productOffering);
        when(productOffering.getDirectAssociations()).thenReturn(newHashSet(directAttributeAssociation, directRuleFilterAssociation));

        final List<CIFAssetUpdateRequest> requestsGenerated = relateToRuleExecutor.execute(ruleRelateTo, initialAsset);

        CIFAssetUpdateRequest expectedChooseRelationshipRequest = new ChooseRelationshipRequest(NEW_OWNER_ASSET_KEY, ASSET_KEY, RELATIONSHIP_NAME, "", 0, UpdateRequestSource.RelateTo);
        CIFAssetUpdateRequest expectedReloadRequest = new CharacteristicReloadRequest(NEW_OWNER_ASSET_KEY, "anAssociatedAttribute", 1);
        assertThat(requestsGenerated.size(), is(2));
        assertThat(requestsGenerated, hasItems(expectedChooseRelationshipRequest, expectedReloadRequest));
    }

    @Test
    public void shouldNotReturnAnyRequestsWhenFilterExpressionFails() {
        final RuleRelateTo ruleRelateTo = new RuleRelateTo(RULE_NAME, Expression.alwaysFailsExpression(), RELATIONSHIP_NAME,
                                                           PATH_TO_NEW_OWNER, DONT_REPROVIDE);

        final List<CIFAssetUpdateRequest> requestsGenerated = relateToRuleExecutor.execute(ruleRelateTo, initialAsset);

        assertThat(requestsGenerated.size(), is(0));
    }

    @Test
    public void shouldCreateChooseRequestAndReprovideRequestForReprovide() {
        final RuleRelateTo ruleRelateTo = new RuleRelateTo(RULE_NAME, Expression.alwaysPassesExpression(), RELATIONSHIP_NAME,
                                                           PATH_TO_NEW_OWNER, DO_REPROVIDE);

        when(pmrHelper.getProductOffering(initialAsset.getProductCode())).thenReturn(ProductOfferingFixture.aProductOffering().build());

        final List<CIFAssetUpdateRequest> requestsGenerated = relateToRuleExecutor.execute(ruleRelateTo, initialAsset);

        CIFAssetUpdateRequest expectedChooseRequest = new ChooseRelationshipRequest(NEW_OWNER_ASSET_KEY, ASSET_KEY, RELATIONSHIP_NAME, "", 0, UpdateRequestSource.RelateTo);
        assertThat(requestsGenerated.get(0), is(expectedChooseRequest));
        CIFAssetUpdateRequest expectedReprovideRequest = new ReprovideAssetRequest(ASSET_KEY, NEW_OWNER_ASSET_KEY, "", 0);
        assertThat(requestsGenerated.get(1), is(expectedReprovideRequest));
    }
}