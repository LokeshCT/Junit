package com.bt.rsqe.customerinventory.service.repository;

import com.bt.rsqe.CustomerInventoryEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.customerinventory.CustomerInventoryDatabaseConfigProvider;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.repository.CustomerInventoryDataTestStateManager;
import com.bt.rsqe.customerinventory.repository.StaleAssetException;
import com.bt.rsqe.customerinventory.repository.jpa.JPARepositoryTest;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetUniqueIdEntity;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetError;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetExternalIdentifier;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.UnloadedExtensionAccessException;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator.MigratedCustomerKey;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.LineItemLockVersion;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.enums.IdentifierType;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATestProvider;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.utils.Uuid;
import com.google.common.base.Optional;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetQuoteOptionItemDetailFixture.*;
import static com.bt.rsqe.enums.AssetVersionStatus.*;
import static com.google.common.collect.Lists.*;
import static java.util.Collections.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CIFAssetJPARepositoryTest extends JPARepositoryTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CIFAssetJPARepositoryTest.class);

    private static CIFAssetJPARepository cifAssetRepository;
    private static UniqueIdJPARepository uniqueIDRepository;

    private static ExternalAssetReader externalAssetReader = mock(ExternalAssetReader.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TestName testName = new TestName();

    String foundCustId ;
    String notFoundCustId ;
    String foundContId ;
    String notFoundContId ;

    @Before
    public void setupIds ()
    {
        foundCustId = testName.getMethodName() + ":foundCustId:" + Uuid.randomUuid() ;
        notFoundCustId = testName.getMethodName() + ":notFoundCustId:" + Uuid.randomUuid() ;
        foundContId = testName.getMethodName() + ":foundContId:" + Uuid.randomUuid() ;
        notFoundContId = testName.getMethodName() + ":notFoundContId:" + Uuid.randomUuid() ;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        provider = JPATestProvider.provider(new CustomerInventoryDatabaseConfigProvider(Environment.env()),
                                            JPATestProvider.CUSTOMERINVENTORY);
        jpa = new JPAPersistenceManager();
        cifAssetRepository = new CIFAssetJPARepository(jpa, externalAssetReader);
        uniqueIDRepository = new UniqueIdJPARepository(jpa);
        new CustomerInventoryDataTestStateManager(ConfigurationProvider.provide(
            CustomerInventoryEnvironmentTestConfig.class, Environment.env())
                                                                       .getCustomerInventoryConfig().getDatabaseConfig("CustomerInventoryDatabase"),
                                                  Environment.env()).beginClean();
    }

    @Test
    public void shouldSaveAndLoadCIFAsset() throws IOException
    {
        CIFAsset cifAsset = aCIFAsset().withCharacteristics(2)
                                       .withErrors(2).build();
        cifAssetRepository.saveAsset(cifAsset);
        CIFAsset loadedAsset = cifAssetRepository.getAsset(cifAsset.getAssetKey(), false);

        // Sort the list of errors since it will have been lost in hibernate
        sort(loadedAsset.getErrors(), cifAssetErrorComparatorById());
        sort(cifAsset.getErrors(), cifAssetErrorComparatorById());

        saveAssetToFile (cifAsset, "cifAsset.txt") ;
        saveAssetToFile (loadedAsset, "loadedAsset.txt") ;

        assertThat(cifAsset, is(loadedAsset));
    }

    @Test
    public void shouldSaveAndLoadCIFAssetWithProductCategoryCode() throws IOException {
        ProductCategoryCode productCategoryCode = new ProductCategoryCode("H123");
        CIFAsset cifAsset = aCIFAsset().withCharacteristics(2).withErrors(2).with(productCategoryCode).build();

        cifAssetRepository.saveAsset(cifAsset);

        CIFAsset loadedAsset = cifAssetRepository.getAsset(cifAsset.getAssetKey(), false);
        assertThat(loadedAsset.getProductCategoryCode(), is(productCategoryCode));
    }

    @Test
    public void shouldSaveAndLoadChildAssetWithCharacteristics() {
        CIFAsset parentAsset = aCIFAsset().withID("Parent").withLineItemId("aLineItemId").withCharacteristic("ParentAttribute", "parentAttributeValue").build();
        cifAssetRepository.saveAsset(parentAsset);

        //Add Child Relationship to Loaded Parent
        parentAsset = cifAssetRepository.getAsset(parentAsset.getAssetKey(), true);
        CIFAsset childAsset = aCIFAsset().withID("Child").withLineItemId("aLineItemId").withCharacteristic("ChildAttribute", null).build();
        final CIFAssetRelationship childRelationship = new CIFAssetRelationship(childAsset,
                                                                                  "aChildRelationName",
                                                                                  RelationshipType.Child,
                                                                                  ProductInstanceState.LIVE);
        parentAsset.getRelationships().add(childRelationship);
        cifAssetRepository.saveAsset(parentAsset);

        parentAsset = cifAssetRepository.getAsset(parentAsset.getAssetKey(), true);
        assertThat(parentAsset.getCharacteristic("ParentAttribute").getValue(), is("parentAttributeValue"));

        childAsset = parentAsset.getChildren().get(0);
        assertThat(childAsset.getCharacteristic("ChildAttribute").getValue(), nullValue());

        //Load Child And Set Value for Child Attribute
        childAsset= cifAssetRepository.getAsset(childAsset.getAssetKey(), true);
        CIFAssetCharacteristic childAttribute = childAsset.getCharacteristic("ChildAttribute");
        childAttribute.setValue("childAttributeValue");
        cifAssetRepository.saveAsset(childAsset);

        childAsset= cifAssetRepository.getAsset(childAsset.getAssetKey(), true);
        assertThat(childAsset.getCharacteristic("ChildAttribute").getValue(), is("childAttributeValue"));


        parentAsset = cifAssetRepository.getAsset(parentAsset.getAssetKey(), true);
        assertThat(parentAsset.getCharacteristic("ParentAttribute").getValue(), is("parentAttributeValue"));

        childAsset = parentAsset.getChildren().get(0);
        assertThat(childAsset.getCharacteristic("ChildAttribute").getValue(), is("childAttributeValue"));

    }

    @Test
    public void shouldSaveAndLoadChildAssetWithCharacteristicsViaParent() {

        CIFAsset parentAsset = aCIFAsset().withID("Parent"+uuid()).withLineItemId("aLineItemId").withCharacteristic("ParentAttribute", "parentAttributeValue").build();
        AssetKey parentAssetKey = parentAsset.getAssetKey();
        cifAssetRepository.saveAsset(parentAsset);

        //Add Child Relationship to Loaded Parent
        parentAsset = cifAssetRepository.getAsset(parentAssetKey, true);

        CIFAsset childAsset = aCIFAsset().withID("Child"+uuid()).withLineItemId("aLineItemId").withCharacteristic("ChildAttribute", "originalChildAttributeValue").build();
        AssetKey childAssetKey = childAsset.getAssetKey() ;
        final CIFAssetRelationship childRelationship = new CIFAssetRelationship(childAsset,
                                                                                  "aChildRelationName",
                                                                                  RelationshipType.Child,
                                                                                  ProductInstanceState.LIVE);
        parentAsset.getRelationships().add(childRelationship);
        cifAssetRepository.saveAsset(parentAsset);

        // Now check what we have just saved

        CIFAsset intermediateParentAsset = cifAssetRepository.getAsset(parentAssetKey, true);
        assertThat(intermediateParentAsset.getCharacteristic("ParentAttribute").getValue(), is("parentAttributeValue"));

        CIFAsset intermediateChildAsset = parentAsset.getChildren().get(0);
        assertThat(intermediateChildAsset.getCharacteristic("ChildAttribute").getValue(), is("originalChildAttributeValue"));

        //Load Parent And Set Value for Child Attribute via the parent child relationship
        CIFAsset updateParentAsset = cifAssetRepository.getAsset(parentAssetKey, true);
        CIFAsset updateChildAsset = updateParentAsset.getChildren().get(0);
        CIFAssetCharacteristic childAttribute = updateChildAsset.getCharacteristic("ChildAttribute");
        childAttribute.setValue("newChildAttributeValue");
        assertThat(updateChildAsset.getCharacteristic("ChildAttribute").getValue(), is("newChildAttributeValue"));

        cifAssetRepository.saveAsset(updateParentAsset);

        //Verify if values are correctly set

        CIFAsset loadedChildAsset = cifAssetRepository.getAsset(childAssetKey, true);
        assertThat(loadedChildAsset.getCharacteristic("ChildAttribute").getValue(), is("newChildAttributeValue"));

        CIFAsset loadedParentAsset = cifAssetRepository.getAsset(parentAssetKey, true);
        assertThat(loadedParentAsset.getCharacteristic("ParentAttribute").getValue(), is("parentAttributeValue"));

        CIFAsset loadChildAssetViaParent = loadedParentAsset.getChildren().get(0);
        assertThat(loadChildAssetViaParent.getCharacteristic("ChildAttribute").getValue(), is("newChildAttributeValue"));

    }

    @Test
    public void shouldRemoveChildAsset () throws Exception
    {
        CIFAsset parentAsset = aCIFAsset().withID("Parent"+uuid()).withLineItemId("aLineItemId").withCharacteristic("ParentAttribute", "parentAttributeValue").build();
        AssetKey parentAssetKey = parentAsset.getAssetKey();
        cifAssetRepository.saveAsset(parentAsset);

        //Add Child Relationship to Loaded Parent
        parentAsset = cifAssetRepository.getAsset(parentAssetKey, true);

        CIFAsset childAsset1 = aCIFAsset().withID("Child" + uuid()).withLineItemId("aLineItemId").withCharacteristic("ChildAttribute", "originalChildAttributeValue1").build();
        AssetKey childAssetKey1 = childAsset1.getAssetKey() ;
        final CIFAssetRelationship childRelationship1 = new CIFAssetRelationship(childAsset1,
                "aChildRelationName",
                RelationshipType.Child,
                ProductInstanceState.LIVE);
        parentAsset.getRelationships().add(childRelationship1);


        CIFAsset childAsset2 = aCIFAsset().withID("Child"+uuid()).withLineItemId("aLineItemId").withCharacteristic("ChildAttribute", "originalChildAttributeValue2").build();
        final CIFAssetRelationship childRelationship2 = new CIFAssetRelationship(childAsset2,
                "aChildRelationName",
                RelationshipType.Child,
                ProductInstanceState.LIVE);
        parentAsset.getRelationships().add(childRelationship2);

        cifAssetRepository.saveAsset(parentAsset);


        parentAsset = cifAssetRepository.getAsset(parentAssetKey, true) ;
        parentAsset.getCharacteristic("ParentAttribute").setValue("newParentAttributeValue");

        cifAssetRepository.saveAsset(parentAsset);


        childAsset1 = cifAssetRepository.getAsset(childAssetKey1, true) ;
        childAsset1.loadQuoteOptionItemDetail(aProvideCIFAssetQuoteOptionItemDetail().build());
        cifAssetRepository.cancelAssetTree(parentAssetKey, "aChildRelationName", childAsset1) ;

        // Verify
        parentAsset = cifAssetRepository.getAsset(parentAssetKey, true) ;
        assertThat(parentAsset.getCharacteristic("ParentAttribute").getValue(), is("newParentAttributeValue")) ;

        assertThat (parentAsset.getChildren().size(), is(1)) ;
        assertThat (parentAsset.getChildren().get(0).getCharacteristic("ChildAttribute").getValue(), is("originalChildAttributeValue2")) ;
    }



    @Test
    // @Ignore("Added to reproduce an issue which happens during an cancelling asset is owned by several owners")
    public void shouldRemoveAnAssetAndItsRelationshipsFromOwners () throws Exception
    {
        CIFAsset parentAsset = aCIFAsset().withID("Parent" + uuid()).withLineItemId("aLineItemId").withCharacteristic("ParentAttribute", "parentAttributeValue").with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        AssetKey parentAssetKey = parentAsset.getAssetKey();
        cifAssetRepository.saveAsset(parentAsset);

        CIFAsset ownerAsset1 = aCIFAsset().withID("Owner"+uuid()).withLineItemId("aOwnerLineItemId1").withCharacteristic("ParentAttribute", "parentAttributeValue").with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        AssetKey ownerAsset1Key = ownerAsset1.getAssetKey();
        cifAssetRepository.saveAsset(ownerAsset1);

        CIFAsset ownerAsset2 = aCIFAsset().withID("Owner"+uuid()).withLineItemId("aOwnerLineItemId2").withCharacteristic("ParentAttribute", "parentAttributeValue").with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        cifAssetRepository.saveAsset(ownerAsset2);

        CIFAsset childAsset = aCIFAsset().withID("Child"+uuid()).withLineItemId("aLineItemId").withCharacteristic("ChildAttribute", "originalChildAttributeValue1").with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        AssetKey childAssetKey = childAsset.getAssetKey();
        cifAssetRepository.saveAsset(childAsset);

        CIFAsset leafAsset = aCIFAsset().withID("Leaf"+uuid()).withLineItemId("aLineItemId").withCharacteristic("LeafAttribute", "originalLeafAttributeValue1").with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        AssetKey leafAssetKey = leafAsset.getAssetKey();
        cifAssetRepository.saveAsset(leafAsset);

        //Add Child Relationship to Loaded Parent
        addRelationship(parentAssetKey, childAssetKey, "parentToChild", RelationshipType.Child) ;

        //Add Child Relationship to Loaded Child
        addRelationship(childAssetKey, leafAssetKey, "childToLeaf", RelationshipType.Child);

        //Add RelatedTo Relationship to Owner1 from child
        // addRelationship(childAssetKey, ownerAsset1Key, "childToOwner1", RelationshipType.RelatedTo) ;

        //Add RelatedTo Relationship to Leaf from Owner1
        // addRelationship(ownerAsset1Key, leafAssetKey, "owner1ToLeaf", RelationshipType.RelatedTo) ;

        //Add RelatedTo Relationship to Loaded Owner1
        addRelationship(ownerAsset1Key, childAssetKey, "owner1ToChild", RelationshipType.RelatedTo) ;

        //Add RelatedTo Relationship to Loaded Owner2
        // addRelationship(ownerAsset2Key, childAssetKey, "owner2ToChild", RelationshipType.RelatedTo) ;

        // Add a RelatedTo relationship between the child and the parent
        // addRelationship(childAssetKey, parentAssetKey, "childToParent", RelationshipType.RelatedTo) ;

        // Add a RelatedTo relationship between the parent and the child
        // addRelationship(parentAssetKey, childAssetKey, "parentToChild", RelationshipType.RelatedTo) ;

        //Cancel the child

        parentAsset = cifAssetRepository.getAsset(parentAssetKey, true);

        LOGGER.debug("parentAsset={}", new CIFAssetToString(parentAsset)) ;

        // Need to repopulate the Quote Option Details that should be here when called normally from CIF Asset Orchestrator
        childAsset = cifAssetRepository.getAsset(childAssetKey, true);
        loadAllQuoteOptionItemDetail(childAsset);


        Set<CancelRelationshipRequest> cancelRequests = cifAssetRepository.cancelAssetTree(parentAssetKey, "parentToChild", childAsset);

        LOGGER.debug ("cancelRequests={}", cancelRequests) ;
    }

    private void loadAllQuoteOptionItemDetail(CIFAsset cifAsset) {
        cifAsset.loadQuoteOptionItemDetail(aProvideCIFAssetQuoteOptionItemDetail().build());
        for (CIFAsset nextChildAsset : cifAsset.getChildren()) {
            loadAllQuoteOptionItemDetail(nextChildAsset) ;
        }
    }

    private void addRelationship (AssetKey fromKey, AssetKey toKey, String name, RelationshipType type)
    {
        CIFAsset from = cifAssetRepository.getAsset(fromKey, true);
        CIFAsset to = cifAssetRepository.getAsset(toKey, true);
        from.getRelationships().add(new CIFAssetRelationship(to, name, type, ProductInstanceState.LIVE));
        cifAssetRepository.saveAsset(from);

    }


    @Test
    public void shouldSaveAndLoadRootCIFAsset() throws IOException
    {
        CIFAsset cifAsset = aCIFAsset().withCharacteristics(2)
                                       .withPricingCaveats(2)
                                       .withPriceLines(2)
                                       .withAuxiliaryAttributes(2)
                                       .withProjectedUsages(newArrayList(TerminationType.MOBILE, TerminationType.ON_NET))
                                       .withErrors(2).build();

        cifAssetRepository.saveAsset(cifAsset);

        CIFAsset loadedAsset = cifAssetRepository.getRootAsset(cifAsset.getLineItemId(), false);

        // Sort the list of errors since it will have been lost in hibernate
        sort(loadedAsset.getErrors(), cifAssetErrorComparatorById());
        sort(cifAsset.getErrors(), cifAssetErrorComparatorById());

        saveAssetToFile(cifAsset, "cifAsset.txt") ;
        saveAssetToFile(loadedAsset, "loadedAsset.txt") ;

        assertThat(cifAsset, is(loadedAsset));
    }

    @Test
    public void shouldSaveAndLoadCIFAssetWithRelationships() {
        CIFAsset cifAsset = aCIFAsset().withRelationships(1).build();

        cifAssetRepository.saveAsset(cifAsset);
        CIFAsset loadedAsset = cifAssetRepository.getAsset(cifAsset.getAssetKey(), true);
        assertThat(cifAsset, is(loadedAsset));
    }

    @Test(expected = UnloadedExtensionAccessException.class)
    public void shouldFailToGetUnloadedRelationships() {
        CIFAsset cifAsset = aCIFAsset().withRelationships(1).build();

        cifAssetRepository.saveAsset(cifAsset);
        CIFAsset loadedAsset = cifAssetRepository.getAsset(cifAsset.getAssetKey(), false);

        try {
            loadedAsset.getRelationships();
        } catch (UnloadedExtensionAccessException exception) {
            String expectedMessage = "Cannot get relationships for this asset. Customer inventory service should be called with the Relationships flag.";
            assertThat(exception.getMessage(), CoreMatchers.is(expectedMessage));
            throw exception;
        }
    }

    @Test
    public void shouldLoadParentAsset() {
        CIFAsset childAsset = aCIFAsset().withRelationships(0).withNullMovesToId().build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child).build();

        cifAssetRepository.saveAsset(childAsset);
        cifAssetRepository.saveAsset(parentAsset);
        CIFAsset loadedAsset = cifAssetRepository.getParentAsset(childAsset.getAssetKey(), true);

        assertThat(parentAsset, is(loadedAsset));
    }

    @Test(expected = NoResultException.class)
    public void shouldThrowNoResultExceptionWhenNoParentAsset() {
        CIFAsset childAsset = aCIFAsset().build();

        cifAssetRepository.saveAsset(childAsset);
        cifAssetRepository.getParentAsset(childAsset.getAssetKey(), true);
    }

    @Test
    public void shouldLoadAllOwnerAssets() {
        CIFAsset childAsset = aCIFAsset().withRelationships(0).build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child).build();
        CIFAsset relatedToAsset = aCIFAsset().withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        cifAssetRepository.saveAsset(childAsset);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(relatedToAsset);
        List<CIFAsset> loadedAsset = cifAssetRepository.getOwnerAssets(childAsset.getAssetKey(), true);

        assertThat(loadedAsset.size(), is(2));
        assertThat(loadedAsset, hasItems(parentAsset, relatedToAsset));
    }

    @Test
    public void shouldLoadOwnerAssetsWithTheCorrectStatus() {
        CIFAsset childAsset = aCIFAsset().withRelationships(0).build();
        CIFAsset parentAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .withRelationship(childAsset, "childRelation", RelationshipType.Child).build();
        CIFAsset relatedToAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.CANCELLED)
                                             .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        cifAssetRepository.saveAsset(childAsset);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(relatedToAsset);
        List<CIFAsset> loadedAsset = cifAssetRepository.getOwnerAssets(childAsset.getAssetKey(), true, newArrayList(AssetVersionStatus.DRAFT));

        assertThat(loadedAsset.size(), is(1));
        assertThat(loadedAsset, hasItems(parentAsset));
    }


    @Test
    public void shouldLoadOwnerAssetsWithTheCorrectStatusOrForQuoteOptionId() {
        CIFAsset childAsset = aCIFAsset().withRelationships(0).withProductIdentifier("child", "A.1").withQuoteOptionId("same").build();

        CIFAsset parentAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.DRAFT).withProductIdentifier("parent", "A.1").withQuoteOptionId("same")
                .withRelationship(childAsset, "childRelation", RelationshipType.Child).build();

        CIFAsset relatedToAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.DRAFT).withQuoteOptionId("same")
                .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        CIFAsset inServiceAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).withQuoteOptionId("different")
                .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        CIFAsset provisioningAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.PROVISIONING).withQuoteOptionId("different")
                .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        CIFAsset acceptedAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.CUSTOMER_ACCEPTED).withQuoteOptionId("different")
                .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        cifAssetRepository.saveAsset(childAsset);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(relatedToAsset);
        cifAssetRepository.saveAsset(inServiceAsset);
        cifAssetRepository.saveAsset(provisioningAsset);
        cifAssetRepository.saveAsset(acceptedAsset);

        List<CIFAsset> loadedAsset = cifAssetRepository.getOwnerAssets(childAsset.getAssetKey(), true, "same", newArrayList(AssetVersionStatus.PROVISIONING, AssetVersionStatus.IN_SERVICE));

        assertThat(loadedAsset.size(), is(4));
        assertThat(loadedAsset, hasItems(parentAsset, relatedToAsset, inServiceAsset, provisioningAsset));
    }

    @Test
    public void shouldLoadOwnerAssetsWithTheCorrectStatusAndProductCodeOrForQuoteOptionId() {
        CIFAsset childAsset = aCIFAsset().withRelationships(0).withProductIdentifier("child", "A.1").withQuoteOptionId("same").build();

        CIFAsset parentAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.DRAFT).withProductIdentifier("parent", "A.1").withQuoteOptionId("same")
                .withRelationship(childAsset, "childRelation", RelationshipType.Child).build();

        CIFAsset relatedToAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.DRAFT).withProductIdentifier("relatedTo", "A.1").withQuoteOptionId("same")
                .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        CIFAsset inServiceAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).withQuoteOptionId("different").withProductIdentifier("inService", "A.1")
                .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        CIFAsset provisioningAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.PROVISIONING).withQuoteOptionId("different").withProductIdentifier("matching", "A.1")
                .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        CIFAsset acceptedAsset = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.CUSTOMER_ACCEPTED).withQuoteOptionId("different").withProductIdentifier("matching", "A.1")
                .withRelationship(childAsset, "relatedTo", RelationshipType.RelatedTo).build();

        cifAssetRepository.saveAsset(childAsset);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(relatedToAsset);
        cifAssetRepository.saveAsset(inServiceAsset);
        cifAssetRepository.saveAsset(provisioningAsset);
        cifAssetRepository.saveAsset(acceptedAsset);

        List<CIFAsset> loadedAsset = cifAssetRepository.getOwnerAssets(childAsset.getAssetKey(), true, "matching", "same", newArrayList(AssetVersionStatus.PROVISIONING, AssetVersionStatus.IN_SERVICE));

        assertThat(loadedAsset.size(), is(1));
        assertThat(loadedAsset, hasItems(provisioningAsset));
    }

    @Test
    public void shouldGetEmptyListWhenNoDependantAssets() {
        CIFAsset childAsset = aCIFAsset().build();

        cifAssetRepository.saveAsset(childAsset);
        List<CIFAsset> cifAssets = cifAssetRepository.getOwnerAssets(childAsset.getAssetKey(), true);

        assertThat(cifAssets.isEmpty(), is(true));
    }

    @Test
    public void shouldLoadRelationshipsOntoAnExistingAsset() {
        CIFAsset baseAsset = aCIFAsset().withID("ID1").withRelationships(2).build();

        cifAssetRepository.saveAsset(baseAsset);

        final CIFAsset assetWithoutRelationships = cifAssetRepository.getAsset(baseAsset.getAssetKey(), false);
        assertFalse(assetWithoutRelationships.hasExtension(CIFAssetExtension.Relationships));

        final CIFAsset assetWithRelationships = cifAssetRepository.getRelationships(assetWithoutRelationships);
        assertThat(assetWithRelationships.getRelationships().size(), is(2));
    }

    @Test
    public void shouldNotLoadAlreadyLoadedRelationships() {
        CIFAsset baseAsset = aCIFAsset().withRelationships(2).build();

        cifAssetRepository.saveAsset(baseAsset);

        CIFAsset assetWithRelationships = cifAssetRepository.getAsset(baseAsset.getAssetKey(), true);
        CIFAsset assetStillWithRelationships = cifAssetRepository.getRelationships(assetWithRelationships);
        assertThat(assetStillWithRelationships.getRelationships().size(), is(2));
    }

    @Test
    public void shouldGetInServiceAsset() {
        CIFAsset baseAsset = aCIFAsset().withID("ASSET_ID").withVersion(2).withAssetVersionStatus(DRAFT).build();
        CIFAsset asIsAsset = aCIFAsset().withID("ASSET_ID").withVersion(1).withAssetVersionStatus(IN_SERVICE).build();

        cifAssetRepository.saveAsset(baseAsset);
        cifAssetRepository.saveAsset(asIsAsset);

        final Optional<CIFAsset> inServiceAsset = cifAssetRepository.getInServiceAsset(baseAsset.getAssetKey(), false);

        assertThat(inServiceAsset.get(), is(asIsAsset));
    }

    @Test
    public void shouldGetAbsentInServiceAssetWhenItDoesNotExist() {
        CIFAsset baseAsset = aCIFAsset().build();
        cifAssetRepository.saveAsset(baseAsset);

        final Optional<CIFAsset> inServiceAsset = cifAssetRepository.getInServiceAsset(baseAsset.getAssetKey(), false);

        assertThat(inServiceAsset, is(Optional.<CIFAsset>absent()));
    }

    @Test
    public void shouldGetSavedLockVersion() throws StaleAssetException {
        String lineItemId = uuid() ;
        cifAssetRepository.saveLineItemLockVersion(lineItemId, 1);

        final int lockVersion = cifAssetRepository.getLockVersion(lineItemId);
        assertThat(lockVersion, is(1));
    }

    @Test
    public void shouldGetImmutableLockVersionIfNotFound() throws StaleAssetException {
        String lineItemId1 = uuid() ;
        String lineItemId2= uuid() ;
        cifAssetRepository.saveLineItemLockVersion(lineItemId1, 1);

        final int lockVersion = cifAssetRepository.getLockVersion(lineItemId2);
        assertThat(lockVersion, is(LineItemLockVersion.IMMUTABLE.getLockVersion()));
    }

    @Test(expected = StaleAssetException.class)
    public void shouldThrowStaleAssetExceptionWhenTryingToSaveEarlierLockVersion() throws StaleAssetException {
        String lineItemId = uuid() ;
        cifAssetRepository.saveLineItemLockVersion(lineItemId, 1);
        cifAssetRepository.saveLineItemLockVersion(lineItemId, 3);
    }

    @Test
    public void shouldLoadRootAsset() throws Exception {
        String lineItemId = uuid();
        String rootId = new LengthConstrainingProductInstanceId().value();

        CIFAsset child1 = aCIFAsset().withLineItemId(lineItemId).build();
        CIFAsset child2 = aCIFAsset().withLineItemId(lineItemId).build();
        CIFAsset rootAsset = aCIFAsset().withLineItemId(lineItemId)
                                        .withID(rootId)
                                        .withRelationship(child1, "childRelation1", RelationshipType.Child)
                                        .withRelationship(child2, "childRelation2", RelationshipType.Child)
                                        .build();

        cifAssetRepository.saveAsset(child1);
        cifAssetRepository.saveAsset(child2);
        cifAssetRepository.saveAsset(rootAsset);

        CIFAsset actualRootAsset = cifAssetRepository.getRootAsset(lineItemId, true);
        assertThat(actualRootAsset.getAssetKey().getAssetId(), is(rootId));
        assertThat(actualRootAsset.getRelationships("childRelation1").get(0).getRelated().getAssetKey().getAssetId(), is(child1.getAssetKey().getAssetId()));
        assertThat(actualRootAsset.getRelationships("childRelation2").get(0).getRelated().getAssetKey().getAssetId(), is(child2.getAssetKey().getAssetId()));
    }

    @Test(expected = NoResultException.class)
    public void shouldThrowNoResultExceptionWhenRootAssetCanNotBeFound() throws Exception {
        cifAssetRepository.getRootAsset("willNotExist", false);
    }

    @Test
    public void shouldSaveAndLoadAssetWithExternalIdentifiers() {
        CIFAsset stubAsset = aCIFAsset().withID("id1").withProductIdentifier("S123", "A.1")
                                        .withLineItemId(null).withSiteId(null)
                                        .withExternalIdentifier(IdentifierType.VPNID, "aVpnId")
                                        .withExternalIdentifier(IdentifierType.INVENTORYID, "anInventoryId")
                                        .build();

        cifAssetRepository.saveAsset(stubAsset);

        CIFAsset loadedAsset = cifAssetRepository.getAsset(stubAsset.getAssetKey(), false);

        Set<CIFAssetExternalIdentifier> externalIdentifiers = loadedAsset.getExternalIdentifiers();

        assertThat(externalIdentifiers, hasItems(new CIFAssetExternalIdentifier(IdentifierType.VPNID, "aVpnId"),
                new CIFAssetExternalIdentifier(IdentifierType.INVENTORYID, "anInventoryId")));

    }


    @Test
    public void shouldLoadExternalAssetForStubbedAsset() {
        CIFAsset stubAsset = aCIFAsset().withID("id2").withProductIdentifier("S123", "A.1")
                                        .withLineItemId(null).withSiteId(null).withAssetType(AssetType.STUB)
                                        .withCustomerId("aCustomerId")
                                        .withExternalIdentifier(IdentifierType.VPNID, "aVpnId")
                                        .build();

        cifAssetRepository.saveAsset(stubAsset);

        CIFAsset externalAsset = aCIFAsset().build();

        when(externalAssetReader.read(new CustomerId("aCustomerId"), new SiteId(null), new ProductCode("S123"),
                                      new LengthConstrainingProductInstanceId("id2"), 1, ProductInstanceState.LIVE, "aVpnId", IdentifierType.VPNID, false))
            .thenReturn(Optional.of(externalAsset));

        CIFAsset loadedAsset = cifAssetRepository.getAsset(stubAsset.getAssetKey(), false);

        assertThat(loadedAsset, is(externalAsset));
        verify(externalAssetReader, times(1)).read(new CustomerId("aCustomerId"), new SiteId(null), new ProductCode("S123"),
                new LengthConstrainingProductInstanceId("id2"), 1, ProductInstanceState.LIVE, "aVpnId", IdentifierType.VPNID, false);
    }

    @Test
    public void shouldThrowExceptionWhenExternalIdentifierNotAvailableForExternalAsset() {

        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage(containsString("External Asset Identifier not found for asset id3"));

        CIFAsset stubAsset = aCIFAsset().withID("id3").withProductIdentifier("S123", "A.1")
                                        .withLineItemId(null).withSiteId(null).withAssetType(AssetType.STUB)
                                        .withCustomerId("aCustomerId")
                                        .build();

        cifAssetRepository.saveAsset(stubAsset);
        cifAssetRepository.getAsset(stubAsset.getAssetKey(), false);
    }

    @Test
    public void shouldThrowExceptionWhenExternalAssetNotAvailableForAnExternalId() {

        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage(containsString("External Asset not found for asset id4"));

        CIFAsset stubAsset = aCIFAsset().withID("id4").withProductIdentifier("S123", "A.1")
                                        .withLineItemId(null).withSiteId(null).withAssetType(AssetType.STUB)
                                        .withCustomerId("aCustomerId")
                                        .withExternalIdentifier(IdentifierType.VPNID, "aVpnId")
                                        .build();

        cifAssetRepository.saveAsset(stubAsset);

        when(externalAssetReader.read(new CustomerId("aCustomerId"), new SiteId(null), new ProductCode("S123"),
                                      new LengthConstrainingProductInstanceId("id4"), 1, ProductInstanceState.LIVE, "aVpnId", IdentifierType.VPNID, false))
            .thenReturn(Optional.<CIFAsset>absent());

        cifAssetRepository.getAsset(stubAsset.getAssetKey(), false);
    }

    @Test
    public void shouldGetNewUniqueIdFromDB() {
        final JPAPersistenceManager jpaPersistenceManager = newPersistenceManager(provider);
        jpaPersistenceManager.save(new FutureAssetUniqueIdEntity("TestType", 0));
        jpaPersistenceManager.done();
        jpaPersistenceManager.unbind();

        final String nextUniqueId = uniqueIDRepository.getNextUniqueId("TestType");
        assertThat(nextUniqueId, not(nullValue()));
        assertThat(nextUniqueId, not(is("")));
    }

    @Test
    public void shouldReturnTrueWhenACustomerHasLegacyBillingSetToYes() {
        CIFAsset rootAsset1 = aCIFAsset().withCustomerId("custId").withContractId("contractId").withProductIdentifier("aProductCode", "version1")
                .withCharacteristic("someAttribute", "someAttributeValue").build();
        CIFAsset rootAsset2 = aCIFAsset().withCustomerId("custId").withContractId("contractId").withProductIdentifier("anotherProductCode", "version1")
                .withCharacteristic("LEGACY BILLING", "Yes").build();

        cifAssetRepository.saveAsset(rootAsset1);
        cifAssetRepository.saveAsset(rootAsset2);

        final boolean migratedCustomer = cifAssetRepository.isMigratedCustomer(new MigratedCustomerKey("custId", "contractId", newArrayList("aProductCode", "anotherProductCode")));

        assertThat(migratedCustomer, is(true));
    }

    @Test
    public void shouldReturnFalseWhenACustomerHasNoLegacyBilling() {
        CIFAsset rootAsset1 = aCIFAsset().withCustomerId("otherCustId").withContractId("contractId").withProductIdentifier("aProductCode", "version1")
                .withCharacteristic("someAttribute", "someAttributeValue").build();
        CIFAsset rootAsset2 = aCIFAsset().withCustomerId("otherCustId").withContractId("contractId").withProductIdentifier("anotherProductCode", "version1")
                .withCharacteristic("LEGACY BILLING", "No").build();

        cifAssetRepository.saveAsset(rootAsset1);
        cifAssetRepository.saveAsset(rootAsset2);

        final boolean migratedCustomer = cifAssetRepository.isMigratedCustomer(new MigratedCustomerKey("otherCustId", "contractId", newArrayList("aProductCode", "anotherProductCode")));

        assertThat(migratedCustomer, is(false));
    }

    @Test
    public void shouldGetAsetsWithCorrectSCodeAndCharacteristicValue() {
        // This customer, and contract with included scode and correct caracteristic - should be included
        CIFAsset rootAsset1 = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId).withProductIdentifier("foundCode1", "version1")
                                         .withCharacteristic("foundChar", "foundVal").build();
        // This customer, and contract with included scode and correct caracteristic - should be included
        CIFAsset rootAsset2 = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId).withProductIdentifier("foundCode2", "version1")
                                         .withCharacteristic("foundChar", "foundVal").build();
        // Wrong customer, correct contract with included scode and correct caracteristic - should not be included
        CIFAsset rootAsset3 = aCIFAsset().withCustomerId(notFoundCustId).withContractId(foundContId).withProductIdentifier("foundCode1", "version1")
                                         .withCharacteristic("foundChar", "foundVal").build();
        // This customer, and wrong contract with included scode and correct caracteristic - should not be included
        CIFAsset rootAsset4 = aCIFAsset().withCustomerId(foundCustId).withContractId(notFoundContId).withProductIdentifier("foundCode1", "version1")
                                         .withCharacteristic("foundChar", "foundVal").build();
        // This customer, and contract with wrong scode and correct caracteristic - should not be included
        CIFAsset rootAsset5 = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId).withProductIdentifier("notFoundCode1", "version1")
                                         .withCharacteristic("foundChar", "foundVal").build();
        // This customer, and contract with included scode and wrong caracteristic - should not be included
        CIFAsset rootAsset6 = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId).withProductIdentifier("foundCode1", "version1")
                                         .withCharacteristic("notFoundChar", "foundVal").build();
        // This customer, and contract with included scode and correct caracteristic with wrong value - should not be included
        CIFAsset rootAsset7 = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId).withProductIdentifier("foundCode1", "version1")
                                         .withCharacteristic("notFoundChar", "nourFoundVal").build();

        cifAssetRepository.saveAsset(rootAsset1);
        cifAssetRepository.saveAsset(rootAsset2);
        cifAssetRepository.saveAsset(rootAsset3);
        cifAssetRepository.saveAsset(rootAsset4);
        cifAssetRepository.saveAsset(rootAsset5);
        cifAssetRepository.saveAsset(rootAsset6);
        cifAssetRepository.saveAsset(rootAsset7);

        final List<CIFAsset> assets = cifAssetRepository.getAssets(foundCustId, foundContId, newArrayList("foundCode1", "foundCode2"),
                                                                   "foundChar", "foundVal", false);

        assertThat(assets.size(), is(2));
        final ArrayList<AssetKey> assetKeys = newArrayList(assets.get(0).getAssetKey(), assets.get(1).getAssetKey());
        assertThat(assetKeys, hasItem(rootAsset1.getAssetKey()));
        assertThat(assetKeys, hasItem(rootAsset2.getAssetKey()));
    }

    @Test
    public void shouldGetAsetsWithCorrectSCode() {
        String foundCode = "foundCode" ;
        String notFoundCode = "notFoundCode" ;
        String version = "version1" ;

        CIFAsset rootAsset1 = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId)
                                         .withProductIdentifier(foundCode, version).build();
        CIFAsset rootAsset2 = aCIFAsset().withCustomerId(notFoundCustId).withContractId(foundContId)
                                         .withProductIdentifier(foundCode, version).build();
        CIFAsset rootAsset3 = aCIFAsset().withCustomerId(foundCustId).withContractId(notFoundContId)
                                         .withProductIdentifier(foundCode, version).build();
        CIFAsset rootAsset4 = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId)
                                         .withProductIdentifier(notFoundCode, version).build();

        cifAssetRepository.saveAsset(rootAsset1);
        cifAssetRepository.saveAsset(rootAsset2);
        cifAssetRepository.saveAsset(rootAsset3);
        cifAssetRepository.saveAsset(rootAsset4);

        final List<CIFAsset> assets = cifAssetRepository.getAssets(foundCustId, foundContId, foundCode, false);

        assertThat(assets.size(), is(1));
        final ArrayList<AssetKey> assetKeys = newArrayList(assets.get(0).getAssetKey());
        assertThat(assetKeys, hasItem(rootAsset1.getAssetKey()));
    }


    @Test
    public void shouldGetEligibleExistingAssets() {  //Existing assets can be an asset with in the same quote option or any other asset which is having Provisioning or inService status.

        CIFAsset sameQuoteOptionAsset_matchingProductCode = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId)
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("sameQuoteOptionId").build();

        CIFAsset sameQuoteOptionAsset_nonMatchingProductCode = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId)
                .withProductIdentifier("differentCode", "version1").withQuoteOptionId("sameQuoteOptionId").build();

        CIFAsset otherQuoteOptionAsset_nonMatchingStatus = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId)
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        CIFAsset otherQuoteOptionAsset_matchingProvisioningStatus = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId)
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.PROVISIONING).build();

        CIFAsset otherQuoteOptionAsset_matchingInServiceStatus = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId)
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build();

        CIFAsset otherQuoteOptionAsset_nonMatchingProductCode = aCIFAsset().withCustomerId(notFoundCustId).withContractId(foundContId)
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build();

        CIFAsset otherQuoteOptionAsset_nonMatchingProvisioningStatus = aCIFAsset().withCustomerId(foundCustId).withContractId(foundContId)
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.PROVISIONING).withAssetType(AssetType.STUB).build();

        cifAssetRepository.saveAsset(sameQuoteOptionAsset_matchingProductCode);
        cifAssetRepository.saveAsset(sameQuoteOptionAsset_nonMatchingProductCode);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_nonMatchingStatus);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_matchingProvisioningStatus);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_matchingInServiceStatus);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_nonMatchingProductCode);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_nonMatchingProvisioningStatus);


        final List<CIFAsset> assets = cifAssetRepository.getEligibleExistingCandidates(foundCustId, foundContId, "foundCode", false, "sameQuoteOptionId");

        assertThat(assets.size(), is(3));
        assertThat(assets, hasItems(sameQuoteOptionAsset_matchingProductCode, otherQuoteOptionAsset_matchingProvisioningStatus, otherQuoteOptionAsset_matchingInServiceStatus));
    }

    @Test
    public void shouldGetEligibleExistingAssetsForAnSite() {

        CIFAsset sameQuoteOptionAsset_matchingProductCode = aCIFAsset().withCustomerId("foundCustId").withContractId("foundContId")
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("sameQuoteOptionId").build();

        CIFAsset sameQuoteOptionAsset_nonMatchingProductCode = aCIFAsset().withCustomerId("foundCustId").withContractId("foundContId")
                .withProductIdentifier("differentCode", "version1").withQuoteOptionId("sameQuoteOptionId").build();

        CIFAsset otherQuoteOptionAsset_nonMatchingStatus = aCIFAsset().withCustomerId("FoundCustId").withContractId("foundContId")
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        CIFAsset otherQuoteOptionAsset_matchingProvisioningStatus = aCIFAsset().withCustomerId("foundCustId").withContractId("foundContId").withSiteId("123")
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.PROVISIONING).build();

        CIFAsset otherQuoteOptionAsset_nonMatchingProvisioningStatus = aCIFAsset().withCustomerId("foundCustId").withContractId("foundContId").withSiteId("123")
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.PROVISIONING).withAssetType(AssetType.STUB).build();


        CIFAsset otherQuoteOptionAsset_nonMatchingInServiceStatus = aCIFAsset().withCustomerId("foundCustId").withContractId("foundContId").withSiteId("234")
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build();

        CIFAsset otherQuoteOptionAsset_nonMatchingProductCode = aCIFAsset().withCustomerId("notFoundCustId").withContractId("foundContId")
                .withProductIdentifier("foundCode", "version1").withQuoteOptionId("otherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build();

        cifAssetRepository.saveAsset(sameQuoteOptionAsset_matchingProductCode);
        cifAssetRepository.saveAsset(sameQuoteOptionAsset_nonMatchingProductCode);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_nonMatchingStatus);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_matchingProvisioningStatus);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_nonMatchingInServiceStatus);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_nonMatchingProductCode);
        cifAssetRepository.saveAsset(otherQuoteOptionAsset_nonMatchingProvisioningStatus);

        final List<CIFAsset> assets = cifAssetRepository.getEligibleExistingCandidates("foundCustId", "foundContId", "foundCode", false, "sameQuoteOptionId", "123");

        assertThat(assets.size(), is(1));
        assertThat(assets, hasItems(otherQuoteOptionAsset_matchingProvisioningStatus));
    }

    @Test
    public void shouldReturnRootAssetFlagCorrectly() {
        //Given
        CIFAsset childAsset = aCIFAsset().withRelationships(0).withNullMovesToId().build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child).build();

        cifAssetRepository.saveAsset(parentAsset);

        CIFAsset loadedParentAsset = cifAssetRepository.getAsset(parentAsset.getAssetKey(), true);
        assertThat(parentAsset, is(loadedParentAsset));

        CIFAsset loadedChildAsset = cifAssetRepository.getAsset(childAsset.getAssetKey(), true);
        assertThat(childAsset, is(loadedChildAsset));

        assertThat(cifAssetRepository.isRootAsset(parentAsset.getAssetKey()), Matchers.is(true));
        assertThat(cifAssetRepository.isRootAsset(childAsset.getAssetKey()), Matchers.is(false));
    }


    @Test
    public void shouldRemoveAnAssetHierarchy() {
        //Given
        CIFAsset grandChildAsset = aCIFAsset().withRelationships(0).withNullMovesToId().with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        CIFAsset childAsset = aCIFAsset().withRelationship(grandChildAsset, "grandChildRelation", RelationshipType.Child).withNullMovesToId().with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child).with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();

        cifAssetRepository.saveAsset(parentAsset);


        // Unload the details for the comparison to work
        parentAsset.unloadQuoteOptionItemDetail();
        childAsset.unloadQuoteOptionItemDetail();
        grandChildAsset.unloadQuoteOptionItemDetail();

        //Check if assets are saved
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey(), true), is(parentAsset));
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey(), true), is(childAsset));
        assertThat(cifAssetRepository.getAsset(grandChildAsset.getAssetKey(), true), is(grandChildAsset));

        // Need to repopulate the Quote Option Details that should be here when called normally from CIF Asset Orchestrator
        loadAllQuoteOptionItemDetail(parentAsset);

        //When
        cifAssetRepository.cancelAssetTree(new AssetKey("anId", 1L), "", parentAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(grandChildAsset.getAssetKey()).isPresent(), is(false));
    }

    @Test
    public void shouldRemoveAChildAsset() throws IOException {
        //Given
        CIFAsset grandChildAsset = aCIFAsset().withRelationships(0).withNullMovesToId().with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        CIFAsset childAsset = aCIFAsset().withRelationship(grandChildAsset, "grandChildRelation", RelationshipType.Child).withNullMovesToId().with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child).with(aProvideCIFAssetQuoteOptionItemDetail().build()).build();

        cifAssetRepository.saveAsset(parentAsset);

        // Need to unload the Quote Option Item Details so that the is comparison will work
        parentAsset.unloadQuoteOptionItemDetail();
        childAsset.unloadQuoteOptionItemDetail();
        grandChildAsset.unloadQuoteOptionItemDetail();

        //Check if assets are saved
        CIFAsset loadedParentAsset = cifAssetRepository.getAsset(parentAsset.getAssetKey(), true);
        assertThat(loadedParentAsset, is(parentAsset));

        CIFAsset loadedChildAsset = cifAssetRepository.getAsset(childAsset.getAssetKey(), true);
        assertThat(loadedChildAsset, is(childAsset));

        CIFAsset loadedGrandChildAsset = cifAssetRepository.getAsset(grandChildAsset.getAssetKey(), true);
        assertThat(loadedGrandChildAsset, is(grandChildAsset));

        // Need to repopulate the Quote Option Details that should be here when called normally from CIF Asset Orchestrator
        loadAllQuoteOptionItemDetail(childAsset) ;

        //When
        cifAssetRepository.cancelAssetTree(parentAsset.getAssetKey(), "childRelation", childAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(true));
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(grandChildAsset.getAssetKey()).isPresent(), is(false));
    }

    @Test
    public void shouldRemoveAllRelationPointsToAssetWhenItsCancelled() throws IOException {
        //Given
        CIFAsset childAsset = aCIFAsset().withRelationships(0)
                .withNullMovesToId()
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        CIFAsset parentAsset = aCIFAsset()
                .withRelationship(childAsset, "childRelation", RelationshipType.Child)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        cifAssetRepository.saveAsset(parentAsset);

        CIFAsset assetFromAnotherHierarchy = aCIFAsset().withRelationship(childAsset, "relatedToRelation", RelationshipType.RelatedTo).build();
        cifAssetRepository.saveAsset(assetFromAnotherHierarchy);

        //Check if assets are saved
        parentAsset.unloadQuoteOptionItemDetail();
        childAsset.unloadQuoteOptionItemDetail();

        final CIFAsset loadedParentAsset = cifAssetRepository.getAsset(parentAsset.getAssetKey(), true);
        saveAssetToFile(parentAsset, "parentAsset");
        saveAssetToFile(loadedParentAsset, "loadedParentAsset");
        assertThat(loadedParentAsset, is(parentAsset));
        assertThat(loadedParentAsset.getChildren().get(0), is(childAsset));

        final CIFAsset loadedOtherAsset = cifAssetRepository.getAsset(assetFromAnotherHierarchy.getAssetKey(), true);
        assertThat(loadedOtherAsset, is(assetFromAnotherHierarchy));
        assertThat(loadedOtherAsset.getRelationships("relatedToRelation").get(0).getRelated(), is(childAsset));

        //When
        loadAllQuoteOptionItemDetail(childAsset) ;
        cifAssetRepository.cancelAssetTree(parentAsset.getAssetKey(), "childRelation", childAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey(), true).getChildren().isEmpty(), is(true));
        assertThat(cifAssetRepository.getAsset(loadedOtherAsset.getAssetKey(), true).getRelationships("relatedToRelation").isEmpty(), is(true));
    }

    @Test
    public void shouldJustRemoveRelationPointsToAInServiceAssetWhenItsCancelled() throws IOException {
        //Given
        CIFAsset inServiceAsset = aCIFAsset().withRelationships(0).withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                .withNullMovesToId()
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        cifAssetRepository.saveAsset(inServiceAsset);

        CIFAsset ownerAsset = aCIFAsset()
                .withRelationship(inServiceAsset, "aRelatedToRelation", RelationshipType.RelatedTo)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        cifAssetRepository.saveAsset(ownerAsset);

        CIFAsset anotherOwnerAsset = aCIFAsset()
                .withRelationship(inServiceAsset, "anotherRelatedToRelation", RelationshipType.RelatedTo)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        cifAssetRepository.saveAsset(anotherOwnerAsset);


        final CIFAsset loadedOwnerAsset = cifAssetRepository.getAsset(ownerAsset.getAssetKey(), true);
        assertThat(loadedOwnerAsset.getAssetKey(), is(ownerAsset.getAssetKey()));
        assertThat(loadedOwnerAsset.getRelationships().get(0).getRelated().getAssetKey(), is(inServiceAsset.getAssetKey()));

        CIFAsset loadedInServiceAsset = cifAssetRepository.getAsset(inServiceAsset.getAssetKey(), true);
        assertThat(loadedInServiceAsset.getAssetKey(), is(inServiceAsset.getAssetKey()));

        //When
        cifAssetRepository.cancelAssetTree(ownerAsset.getAssetKey(), "aRelatedToRelation", inServiceAsset);

        //Then
        loadedInServiceAsset = cifAssetRepository.getAsset(inServiceAsset.getAssetKey(), true);
        assertThat(cifAssetRepository.getAsset(inServiceAsset.getAssetKey()).isPresent(), is(true));
        assertThat(loadedInServiceAsset.getAssetVersionStatus(), is(AssetVersionStatus.IN_SERVICE));

        assertThat(cifAssetRepository.getAsset(ownerAsset.getAssetKey(), true).getRelationships().isEmpty(), is(true));

        assertThat(cifAssetRepository.getAsset(anotherOwnerAsset.getAssetKey(), true).getRelationships().isEmpty(), is(false));
    }


    @Test
    public void shouldCreateCancellationRequestWhenRelatedToAssetFromSameQuoteOptionConsumedOnlyByCancelingAsset() {
        //Given
        CIFAsset assetFromAnotherHierarchy = aCIFAsset().withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                        .build();

        CIFAsset childAsset = aCIFAsset().withRelationships(0)
                                         .withNullMovesToId()
                                         .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                         .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                         .build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                          .withRelationship(assetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                          .build();
        cifAssetRepository.saveAsset(assetFromAnotherHierarchy);
        cifAssetRepository.saveAsset(parentAsset);

        //When
        final Set<CancelRelationshipRequest> cancelRelationshipRequests = cifAssetRepository.cancelAssetTree(new AssetKey("anId", 1L), "", parentAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(assetFromAnotherHierarchy.getAssetKey()).isPresent(), is(true));

        assertThat(cancelRelationshipRequests.size(), is(1));
        final CancelRelationshipRequest cancelRelationshipRequest = cancelRelationshipRequests.iterator().next();
        assertThat(cancelRelationshipRequest.getCancellingAssetId(), is(assetFromAnotherHierarchy.getAssetKey()));
        assertThat(cancelRelationshipRequest.getRelationshipName(), is("relatedToRelation"));
        assertThat(cancelRelationshipRequest.getProductCode(), is(assetFromAnotherHierarchy.getProductCode()));
    }

    @Test
    public void shouldCreateCancellationRequestEvenWhenAnotherParentDraftAssetFromAnotherQuoteOptionConsumesRelatedToAsset() {
        //Given
        CIFAsset relatedAssetFromAnotherHierarchy = aCIFAsset().withQuoteOptionId("anotherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        CIFAsset childAsset = aCIFAsset().withRelationships(0)
                .withNullMovesToId()
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                          .withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                          .withQuoteOptionId("aQuoteOptionId")
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                          .build();

        CIFAsset anotherParentFromSameQuoteOption = aCIFAsset().withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                                               .withQuoteOptionId("anotherQuoteOptionId")
                                                               .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                               .build();


        cifAssetRepository.saveAsset(relatedAssetFromAnotherHierarchy);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(anotherParentFromSameQuoteOption);

        //When
        final Set<CancelRelationshipRequest> cancelRelationshipRequests = cifAssetRepository.cancelAssetTree(new AssetKey("anId", 1L), "", parentAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(relatedAssetFromAnotherHierarchy.getAssetKey()).isPresent(), is(true));

        assertThat(cancelRelationshipRequests.size(), is(1));
        final CancelRelationshipRequest cancelRelationshipRequest = cancelRelationshipRequests.iterator().next();
        assertThat(cancelRelationshipRequest.getCancellingAssetId(), is(relatedAssetFromAnotherHierarchy.getAssetKey()));
        assertThat(cancelRelationshipRequest.getRelationshipName(), is("relatedToRelation"));
    }

    @Test
    public void shouldNotCreateCancellationRequestEvenWhenAnotherParentAssetFromConsumesRelatedToAsset() {
        //Given
        CIFAsset relatedAssetFromAnotherHierarchy = aCIFAsset().withQuoteOptionId("aQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        CIFAsset childAsset = aCIFAsset().withRelationships(0)
                                         .withNullMovesToId()
                                         .withQuoteOptionId("aQuoteOptionId")
                                         .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                         .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                         .build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                          .withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                          .withQuoteOptionId("aQuoteOptionId")
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                          .build();

        CIFAsset anotherParentFromSameQuoteOption = aCIFAsset().withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                                               .withQuoteOptionId("aQuoteOptionId")
                                                               .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                               .build();


        cifAssetRepository.saveAsset(relatedAssetFromAnotherHierarchy);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(anotherParentFromSameQuoteOption);

        //When
        final Set<CancelRelationshipRequest> cancelRelationshipRequests = cifAssetRepository.cancelAssetTree(new AssetKey("anId", 1L), "", parentAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(relatedAssetFromAnotherHierarchy.getAssetKey()).isPresent(), is(true));

        assertThat(cancelRelationshipRequests.isEmpty(), is(true));
    }

    @Test
    public void shouldCreateCancellationRequestEvenWhenOtherObsoleteAssetFromSameQuoteOptionConsumesRelatedToAsset() {
        //Given
        CIFAsset relatedAssetFromAnotherHierarchy = aCIFAsset().withQuoteOptionId("anotherQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        CIFAsset childAsset = aCIFAsset().withRelationships(0)
                .withNullMovesToId()
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                          .withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                          .withQuoteOptionId("aQuoteOptionId")
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                          .build();

        CIFAsset anotherParentFromSameQuoteOption = aCIFAsset().withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                                               .withQuoteOptionId("aQuoteOptionId")
                                                               .withAssetVersionStatus(AssetVersionStatus.OBSOLETE)
                                                               .build();


        cifAssetRepository.saveAsset(relatedAssetFromAnotherHierarchy);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(anotherParentFromSameQuoteOption);

        //When
        final Set<CancelRelationshipRequest> cancelRelationshipRequests = cifAssetRepository.cancelAssetTree(new AssetKey("anId", 1L), "", parentAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(relatedAssetFromAnotherHierarchy.getAssetKey()).isPresent(), is(true));

        assertThat(cancelRelationshipRequests.size(), is(1));
        final CancelRelationshipRequest cancelRelationshipRequest = cancelRelationshipRequests.iterator().next();
        assertThat(cancelRelationshipRequest.getCancellingAssetId(), is(relatedAssetFromAnotherHierarchy.getAssetKey()));
        assertThat(cancelRelationshipRequest.getRelationshipName(), is("relatedToRelation"));
    }


    @Test
    public void shouldCreateCancellationRequestEvenWhenOtherObsoleteAssetFromAnotherQuoteOptionConsumesRelatedToAsset() {
        //Given
        CIFAsset relatedAssetFromAnotherHierarchy = aCIFAsset().withQuoteOptionId("aQuoteOptionId").withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        CIFAsset childAsset = aCIFAsset().withRelationships(0)
                .withNullMovesToId()
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                          .withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                          .withQuoteOptionId("aQuoteOptionId")
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                          .build();

        CIFAsset anotherParentFromSameQuoteOption = aCIFAsset().withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                                               .withQuoteOptionId("anotherQuoteOptionId")
                                                               .withAssetVersionStatus(AssetVersionStatus.OBSOLETE)
                                                               .build();


        cifAssetRepository.saveAsset(relatedAssetFromAnotherHierarchy);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(anotherParentFromSameQuoteOption);

        //When
        final Set<CancelRelationshipRequest> cancelRelationshipRequests = cifAssetRepository.cancelAssetTree(new AssetKey("anId", 1L), "", parentAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(relatedAssetFromAnotherHierarchy.getAssetKey()).isPresent(), is(true));

        assertThat(cancelRelationshipRequests.size(), is(1));
        final CancelRelationshipRequest cancelRelationshipRequest = cancelRelationshipRequests.iterator().next();
        assertThat(cancelRelationshipRequest.getCancellingAssetId(), is(relatedAssetFromAnotherHierarchy.getAssetKey()));
        assertThat(cancelRelationshipRequest.getRelationshipName(), is("relatedToRelation"));
    }


    @Test
    public void shouldNotCreateCancellationRequestEvenWhenNonDraftAssetFromDifferentQuoteOptionConsumesRelatedToAsset() {
        //Given
        CIFAsset relatedAssetFromAnotherHierarchy = aCIFAsset()
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .build();

        CIFAsset childAsset = aCIFAsset().withRelationships(0)
                .withNullMovesToId()
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                          .withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                          .withQuoteOptionId("aQuoteOptionId")
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                          .build();

        CIFAsset anotherParentFromSameQuoteOption = aCIFAsset().withRelationship(relatedAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                                               .withQuoteOptionId("anotherQuoteOptionId")
                                                               .withAssetVersionStatus(AssetVersionStatus.CUSTOMER_ACCEPTED)
                                                               .build();


        cifAssetRepository.saveAsset(relatedAssetFromAnotherHierarchy);
        cifAssetRepository.saveAsset(parentAsset);
        cifAssetRepository.saveAsset(anotherParentFromSameQuoteOption);

        //When
        final Set<CancelRelationshipRequest> cancelRelationshipRequests = cifAssetRepository.cancelAssetTree(new AssetKey("anId", 1L), "", parentAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(relatedAssetFromAnotherHierarchy.getAssetKey()).isPresent(), is(true));

        assertThat(cancelRelationshipRequests.isEmpty(), is(true));
    }

    @Test
    public void shouldNotCreateCancellationRequestEvenWhenRelatedToAssetHasInServiceAsset() {
        //Given
        CIFAsset relatedInServiceAssetFromAnotherHierarchy = aCIFAsset().withQuoteOptionId("inServiceQuoteOptionId").withID("related").withVersion(1L).withAssetVersionStatus(AssetVersionStatus.IN_SERVICE).build();
        CIFAsset relatedDraftAssetFromAnotherHierarchy = aCIFAsset().withQuoteOptionId("anotherQuoteOptionId").withID("related").withVersion(2L).withAssetVersionStatus(AssetVersionStatus.DRAFT).build();

        CIFAsset childAsset = aCIFAsset().withRelationships(0)
                .withNullMovesToId()
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();
        CIFAsset parentAsset = aCIFAsset().withRelationship(childAsset, "childRelation", RelationshipType.Child)
                                          .withRelationship(relatedDraftAssetFromAnotherHierarchy, "relatedToRelation", RelationshipType.RelatedTo)
                                          .withQuoteOptionId("aQuoteOptionId")
                                          .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                          .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                                          .build();

        cifAssetRepository.saveAsset(relatedInServiceAssetFromAnotherHierarchy);
        cifAssetRepository.saveAsset(relatedDraftAssetFromAnotherHierarchy);
        cifAssetRepository.saveAsset(parentAsset);

        //When
        final Set<CancelRelationshipRequest> cancelRelationshipRequests = cifAssetRepository.cancelAssetTree(new AssetKey("anId", 1L), "", parentAsset);

        //Then
        assertThat(cifAssetRepository.getAsset(childAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(parentAsset.getAssetKey()).isPresent(), is(false));
        assertThat(cifAssetRepository.getAsset(relatedDraftAssetFromAnotherHierarchy.getAssetKey()).isPresent(), is(true));
        assertThat(cifAssetRepository.getAsset(relatedInServiceAssetFromAnotherHierarchy.getAssetKey()).isPresent(), is(true));

        assertThat(cancelRelationshipRequests.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnTrueIfThereIsAInServiceAsset() {
        String assetId = uuid();
        CIFAsset inServiceAsset = aCIFAsset().withID(assetId).withVersion(1L)
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();

        CIFAsset draftAsset = aCIFAsset().withID(assetId).withVersion(2L)
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();

        CIFAsset someOtherAsset = aCIFAsset().withID(uuid()).withVersion(1L)
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();

        cifAssetRepository.saveAsset(inServiceAsset);
        cifAssetRepository.saveAsset(draftAsset);
        cifAssetRepository.saveAsset(someOtherAsset);

        assertThat(cifAssetRepository.hasProvisiongOrInServiceAsset(draftAsset.getAssetKey()), is(true));
        assertThat(cifAssetRepository.hasProvisiongOrInServiceAsset(someOtherAsset.getAssetKey()), is(false));
    }

    @Test
    public void shouldReturnTrueIfThereIsAProvisioningAsset() {
        String assetId = uuid();
        CIFAsset inServiceAsset = aCIFAsset().withID(assetId).withVersion(1L)
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.PROVISIONING)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();

        CIFAsset draftAsset = aCIFAsset().withID(assetId).withVersion(2L)
                .withQuoteOptionId("aQuoteOptionId")
                .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                .with(aProvideCIFAssetQuoteOptionItemDetail().build())
                .build();


        cifAssetRepository.saveAsset(inServiceAsset);

        assertThat(cifAssetRepository.hasProvisiongOrInServiceAsset(draftAsset.getAssetKey()), is(true));
    }


    private JPAPersistenceManager newPersistenceManager(JPAEntityManagerProvider provider) {
        JPAPersistenceManager jpa = new JPAPersistenceManager();
        jpa.bind(provider.entityManager());
        jpa.start();
        return jpa;
    }

    private String uuid ()
    {
        return UUID.randomUUID().toString() ;
    }

    Comparator<CIFAssetError> cifAssetErrorComparatorById ()
    {
        return new Comparator<CIFAssetError>()
        {
            @Override
            public int compare (CIFAssetError o1, CIFAssetError o2)
            {
                return o1.getId().compareTo(o2.getId());
            }
        } ;
    }

    File getFile (String... parts)
    {
        File file = null ;
        for (String part : parts)
        {
            if (file == null)
            {
                file = new File (part) ;
            }
            else
            {
                file = new File (file, part) ;
            }
        }
        return file ;
    }

    private RecursiveToStringStyle toStringStyle ()
    {
        return new RecursiveToStringStyle()
        {{
                setContentStart("[");
                setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
                setFieldSeparatorAtStart(true);
                setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
                setUseIdentityHashCode(false);
        }} ;
    }

    class CIFAssetToString
    {
        private final CIFAsset cifAsset;

        public CIFAssetToString (CIFAsset cifAsset)
        {
            this.cifAsset = cifAsset;
        }

        @Override
        public String toString ()
        {
            RecursiveToStringStyle style = toStringStyle () ;
            return ToStringBuilder.reflectionToString(cifAsset, style) ;
        }
    }

    private void saveAssetToFile (CIFAsset cifAsset, String filename) throws IOException
    {
        RecursiveToStringStyle style = toStringStyle () ;

        File output = getFile("build", "output", testName.getMethodName(), filename) ;
        if(! output.getParentFile().exists())
        {
            assertTrue(output.getCanonicalPath(), output.getParentFile().mkdirs());
        }
        FileWriter writer = new FileWriter(output);
        IOUtils.write(ToStringBuilder.reflectionToString(cifAsset, style), writer) ;
        writer.flush();
        writer.close();
    }

}
