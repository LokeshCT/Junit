package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.ToBeAssets;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientManager;
import com.bt.rsqe.customerinventory.client.CustomerInventoryStubClientManagerFactory;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FromProductInstance;
import com.bt.rsqe.customerinventory.fixtures.LineItemIdFixture;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.customerinventory.parameter.RandomSiteId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.extensions.CardinalityExpression;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.ProductInstanceFactory;
import com.bt.rsqe.domain.project.StubCountryResolver;
import com.bt.rsqe.domain.project.ToProductInstance;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.pmr.api.ProductNotFoundException;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.utils.countries.Countries;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.fixtures.LineItemIdFixture.*;
import static com.bt.rsqe.matchers.EquivalenceMatcher.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class FutureProductInstanceFacadeInteractionTest {
    private static final ProductVersion VERSION = new ProductVersion("A.1");

    private PmrClient pmr;
    private CustomerInventoryClientManager cifClientManager;
    private ProductInstanceFactory productInstanceFactory;
    private FutureProductInstanceFacade facade;
    private CustomerId customerId;
    private QuoteOptionId quoteOptionId;
    private ProjectId projectId;
    private String contractTerm = "24";

    @Before
    public void setup() throws Exception {
        pmr = PmrMocker.getMockedInstance();
        cifClientManager = CustomerInventoryStubClientManagerFactory.getClientManager(pmr);
        productInstanceFactory = ProductInstanceFactory.getProductInstanceFactory(pmr, cifClientManager, StubCountryResolver.resolveTo(Countries.byIsoStatic("GB")));
        facade = new FutureProductInstanceFacade(cifClientManager.getProductInstanceClient(), productInstanceFactory);
        customerId = new CustomerId("customerId");
        quoteOptionId = new QuoteOptionId("quoteOptionId");
        projectId = new ProjectId("projectId");

        PmrMocker.returnForProduct(pmr, ProductOfferingFixture.aProductOffering()
                                                              .withProductIdentifier(new ProductIdentifier("S0205086",
                                                                                                           VERSION.value()))
                                                              .build());

        PmrMocker.returnForProduct(pmr, ProductOfferingFixture.aProductOffering()
                                                              .withProductIdentifier(new ProductIdentifier(ProductCodes.Onevoice.productCode(),
                                                                                                           VERSION.value()))
                                                              .build());
    }

    @Test(expected = ProductNotFoundException.class)
    @Ignore("Test repeated failing on rsqeci-1 but not on resqeci-2")
    public void shouldCreateProductInstanceBasedOnSuppliedProductVersionSadPathTest() throws Exception {
        //Given I ask to create an instance based on an offering version that doesn't exist, a ProductNotFoundException should be thrown.
        //This is to test that the client/CIF does not default to the latest offering version.
        final ProductVersion nonExistentproductVersion = new ProductVersion("ABCDEFG.1");
        productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                        ProductInstanceVersion.DEFAULT_VALUE,
                                                        aUniqueLineItemId().build(),
                                                        new ProductCode(ProductCodes.Onevoice.productCode()),
                                                        nonExistentproductVersion,
                                                        new RandomSiteId(),
                                                        StencilId.NIL,
                                                        customerId,
                                                        null, quoteOptionId,
                                                        AssetType.REAL,
                                                        projectId,
                                                        contractTerm, null, null, null,null,null, null,null,null);
    }

    @Test
    public void shouldCreateProductInstanceBasedOnLatestVersionOfOffering() throws Exception {
        PmrMocker.returnForProduct(pmr, ProductOfferingFixture.aProductOffering()
                                                              .withProductIdentifier(new ProductIdentifier(ProductCodes.Onevoice.productCode(),
                                                                                                           "latest"))
                                                              .build());

        final ProductInstance instance = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                         ProductInstanceVersion.DEFAULT_VALUE,
                                                                                         aUniqueLineItemId().build(),
                                                                                         new ProductCode(ProductCodes.Onevoice.productCode()),
                                                                                         new ProductVersion("latest"),
                                                                                         new RandomSiteId(),
                                                                                         StencilId.NIL,
                                                                                         customerId,
                                                                                         null,
                                                                                         quoteOptionId,
                                                                                         AssetType.REAL,
                                                                                         projectId,
                                                                                         contractTerm, null, null, null,null,null, null,null,null);
        assertThat(instance.getProductOffering().getProductIdentifier().getVersionNumber(), is("latest"));
    }

    @Test
    public void shouldCreateRFOProductWhereNotExistentInTree() throws Exception {
        PmrMocker.returnForProduct(pmr, ProductOfferingFixture.aProductOffering()
                                                              .withProductIdentifier(new ProductIdentifier(ProductCodes.Onevoice.productCode(),
                                                                                                           VERSION.value()))
                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                             .withRelationName("childRelation")
                                                                                                             .withProductIdentifier(new ProductIdentifier("childProduct", VERSION.value()))
                                                                                                             .withRelationType(RelationshipType.Child)
                                                                                                             .withCardinality(1, 1, 1)
                                                                                                             .withCardinalityExpression(CardinalityExpression.NIL))
                                                              .build());

        PmrMocker.returnForProduct(pmr, ProductOfferingFixture.aProductOffering()
                                                              .withProductIdentifier(new ProductIdentifier("childProduct",
                                                                                                           VERSION.value()))
                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                             .withRelationName("grandChildRelation")
                                                                                                             .withProductIdentifier(new ProductIdentifier("grandChildProduct", VERSION.value()))
                                                                                                             .withRelationType(RelationshipType.Child)
                                                                                                             .withCardinality(1, 1, 1)
                                                                                                             .withCardinalityExpression(CardinalityExpression.NIL))
                                                              .withAttribute("childAttribute")
                                                              .build());

        PmrMocker.returnForProduct(pmr, ProductOfferingFixture.aProductOffering()
                                                              .withProductIdentifier(new ProductIdentifier("grandChildProduct",
                                                                                                           VERSION.value()))
                                                              .withAttribute("grandChildAttribute")
                                                              .build());

        LineItemId lineItemId = LineItemIdFixture.aUniqueLineItemId().build();
        SiteId siteId = new RandomSiteId();

        // Create and persist root product only
        ProductInstance onevoice = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                   ProductInstanceVersion.DEFAULT_VALUE,
                                                                                   lineItemId,
                                                                                   new ProductCode(ProductCodes.Onevoice.productCode()),
                                                                                   VERSION,
                                                                                   siteId,
                                                                                   StencilId.NIL,
                                                                                   customerId,
                                                                                   null, quoteOptionId,
                                                                                   AssetType.REAL,
                                                                                   projectId,
                                                                                   contractTerm, null, null, null,null,null, null,null,null);
        assertThat(countNodesInTree(onevoice), is(1));
        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(onevoice);
        // Get flattened structure via the client
        FlattenedProductStructure flattened = facade.buildFullFlattenedRelationshipStructure(lineItemId);

        // Flattened structure has six potential product instances in the tree
        assertThat(flattened.size(), is(3));
        // The underlying product instance structure has only the root instance
        assertThat(flattened.getRootProductInstance().getSourceInstance().getProductInstanceId().getValue(),
                   is(onevoice.getProductInstanceId().getValue()));
        assertThat(flattened.getRootProductInstance().getSourceInstance().getChildren().size(), is(0));

        // Set one RFO attribute for descendant product S0205089 and save
        flattened.setAttributeValueFor(new FlattenedProductStructure.Attribute("childAttribute", "yes", "childProduct"));
        flattened.setAttributeValueFor(new FlattenedProductStructure.Attribute("grandChildAttribute", "yes", "grandChildProduct"));
        facade.saveProductInstance(flattened);

        AssetDTO stored = CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.get(new ToBeAssets.Key(onevoice));
        assertThat(stored, isEquivalentTo(new FromProductInstance().toFutureAssetDTO(flattened.getRootProductInstance().getSourceInstance())));

        ProductInstance actual = new ToProductInstance(productInstanceFactory).fromFutureAsset(stored);
        assertThat(countNodesInTree(actual), is(3));
        // We have the right product
        assertThat(actual.getProductInstanceId().getValue(), is(onevoice.getProductInstanceId().getValue()));
        // The product has a new child S0205087 which is the branch parent of our target attribute
        assertThat(actual.getChildren().size(), is(1));
        ProductInstance child = new ArrayList<ProductInstance>(actual.getChildren()).get(0);
        assertThat(child.getProductIdentifier().getProductId(), is("childProduct"));
        assertThat(child.getChildren().size(), is(1));
        // The product has a new child S0205089 which contains our target attribute
        ProductInstance grandChild = new ArrayList<ProductInstance>(child.getChildren()).get(0);
        assertThat(grandChild.getProductIdentifier().getProductId(), is("grandChildProduct"));
        assertThat(grandChild.getChildren().size(), is(0));
    }

    @Test
    public void shouldNotCreateNewProductsIfNotRequired() throws Exception {
        LineItemId lineItemId = LineItemIdFixture.aUniqueLineItemId().build();

        // Create and persist a full OneVoice product structure
        ProductInstance onevoice = DefaultProductInstanceFixture.aProductInstance()
                                                                .withProductIdentifier(new ProductIdentifier("S0205086", VERSION.value()))
                                                                .withLineItemId(lineItemId.value())
                                                                .build();
        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(onevoice);

        // Get flattened structure via the client
        FlattenedProductStructure flattened = facade.buildFullFlattenedRelationshipStructure(lineItemId);
        // Flattened structure has seven product instances in the tree (including price line)
        assertThat(flattened.size(), is(countNodesInTree(onevoice)));

        // Set one RFO attribute for descendant product S0205089 and save
        flattened.setAttributeValueFor(new FlattenedProductStructure.Attribute("VOICE TRAFFIC REPORTING REQUIRED", "yes", "S0205089"));
        facade.saveProductInstance(flattened);
        ProductInstance actual = new ToProductInstance(productInstanceFactory).fromFutureAsset(
            CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.get(new ToBeAssets.Key(onevoice)));
        assertThat(countNodesInTree(actual), is(countNodesInTree(onevoice)));
    }

    @Test
    public void shouldNotCreateAnyNewProductInstancesWhereProductCodeDoesNotMatch() throws Exception {
        LineItemId lineItemId = LineItemIdFixture.aUniqueLineItemId().build();
        // Create and persist root product only
        ProductInstance onevoice = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                   ProductInstanceVersion.DEFAULT_VALUE, lineItemId,
                                                                                   new ProductCode("S0205086"),
                                                                                   VERSION, null, StencilId.NIL, customerId, null, quoteOptionId, AssetType.REAL, projectId, contractTerm, null, null, null,null,null, null,null,null);
        assertThat(countNodesInTree(onevoice), is(1));
        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(onevoice);

        // Get flattened structure via the client
        FlattenedProductStructure flattened = facade.buildFullFlattenedRelationshipStructure(lineItemId);
        flattened.setAttributeValueFor("non-existent",
                                       "VOICE TRAFFIC REPORTING REQUIRED",
                                       "a value");
        facade.saveProductInstance(flattened);
        ProductInstance actual = new ToProductInstance(productInstanceFactory).fromFutureAsset(
            CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.get(new ToBeAssets.Key(onevoice)));
        assertThat(countNodesInTree(actual), is(countNodesInTree(onevoice)));
    }

    @Test
    public void shouldNotCreateAnyNewProductInstancesWhereAttributeDoesNotMatch() throws Exception {
        LineItemId lineItemId = LineItemIdFixture.aUniqueLineItemId().build();
        // Create and persist root product only
        ProductInstance onevoice = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                   ProductInstanceVersion.DEFAULT_VALUE, lineItemId,
                                                                                   new ProductCode("S0205086"),
                                                                                   VERSION, null, StencilId.NIL, customerId, null, quoteOptionId, AssetType.REAL, projectId, contractTerm, null, null, null,null,null, null,null,null);
        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(onevoice);

        // Get flattened structure via the client
        FlattenedProductStructure flattened = facade.buildFullFlattenedRelationshipStructure(lineItemId);
        flattened.setAttributeValueFor("S0205089",
                                       "non-existent",
                                       "a value");
        facade.saveProductInstance(flattened);
        ProductInstance actual = new ToProductInstance(productInstanceFactory).fromFutureAsset(
            CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.get(new ToBeAssets.Key(onevoice)));
        assertThat(countNodesInTree(actual), is(countNodesInTree(onevoice)));
    }

    private int countNodesInTree(ProductInstance instance) {
        return countNodesInTree(instance, 0);
    }

    private int countNodesInTree(ProductInstance instance, int count) {
        if (instance != null) {
            count++;
            for (ProductInstance child : instance.getChildren()) {
                count = countNodesInTree(child, count);
            }
        }
        return count;
    }
}
