package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.CreatableRelationshipInstanceFilter;
import com.bt.rsqe.domain.project.ProductInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class InVisibleCreatableLineItemRetrieverTest {

    @Mock
    private ProductInstanceClient productInstanceClient;

    @Mock
    private CreatableRelationshipInstanceFilter creatableRelationshipInstanceFilter;

    private InVisibleCreatableLineItemRetriever inVisibleCreatableLineItemRetriever;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        inVisibleCreatableLineItemRetriever = new InVisibleCreatableLineItemRetriever(productInstanceClient, creatableRelationshipInstanceFilter);
    }

    @Test
    public void shouldReturnInVisibleCreatableLineItemsWhenAnAssetCreatedIt() throws Exception {
       //Given
        ProductInstance inVisibleCreatableAsset = new DefaultProductInstanceFixture(new ProductOfferingFixture().withVisibleInOnlineSummary(false).build())
            .withLineItemId("inVisibleLineItemId").withQuoteOptionId("sameQuoteOptionId").build();
        ProductInstance inVisibleCreatableAssetFromOtherQuoteOption = new DefaultProductInstanceFixture(new ProductOfferingFixture().withVisibleInOnlineSummary(false).build())
                .withLineItemId("inVisibleLineItemId_2").withQuoteOptionId("differentQuoteOptionId").build();
        ProductInstance nonFrontCatalogueCreatableAsset = new DefaultProductInstanceFixture(new ProductOfferingFixture().withIsInFrontCatalogue(false).build())
            .withLineItemId("nonFrontCatalogueLineItemId").withQuoteOptionId("sameQuoteOptionId").build();
        ProductInstance visibleCreatableAsset = new DefaultProductInstanceFixture(new ProductOfferingFixture().withVisibleInOnlineSummary(true).withIsInFrontCatalogue(true).build())
            .withLineItemId("visibleLineItemId").withQuoteOptionId("sameQuoteOptionId").build();
        ProductInstance productInstance = new DefaultProductInstanceFixture("aProductId").withLineItemId("aLineItemId").withQuoteOptionId("sameQuoteOptionId")
                                                                                         .withRelatedToProductInstance(inVisibleCreatableAsset, visibleCreatableAsset, nonFrontCatalogueCreatableAsset, inVisibleCreatableAssetFromOtherQuoteOption)
                                                                                         .build();

        AssetDTO asset = AssetDTOFixture.anAsset().withRelation(AssetDTOFixture.anAsset().build(), RelationshipName.newInstance("invisibleRelationship"), RelationshipType.RelatedTo, ProductInstanceState.LIVE).build();
        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId"))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(productInstance);

        when(creatableRelationshipInstanceFilter.filter(productInstanceClient, productInstance)).thenReturn(productInstance.getRelationships());

        //When
        Set<String> inVisibleCreatableLineItems = inVisibleCreatableLineItemRetriever.whatInVisibleLineItemsIHaveCreated("aLineItemId");

        //Then
        assertThat(inVisibleCreatableLineItems.size(), is(2));
        assertThat(inVisibleCreatableLineItems, hasItem("inVisibleLineItemId"));
        assertThat(inVisibleCreatableLineItems, hasItem("nonFrontCatalogueLineItemId"));
    }

    @Test
    public void shouldNotReturnInVisibleCreatableLineItemsWhenAnAssetDoesNotHaveIt() throws Exception {
        //Given
        ProductInstance visibleCreatableAsset = new DefaultProductInstanceFixture(new ProductOfferingFixture().withVisibleInOnlineSummary(true).withIsInFrontCatalogue(true).build())
            .withLineItemId("visibleLineItemId").build();
        ProductInstance productInstance = new DefaultProductInstanceFixture("aProductId").withLineItemId("aLineItemId")
                                                                                         .withRelatedToProductInstance(visibleCreatableAsset)
                                                                                         .build();
        AssetDTO asset = AssetDTOFixture.anAsset().withRelation(AssetDTOFixture.anAsset().build(), RelationshipName.newInstance("visibleRelationship"), RelationshipType.RelatedTo, ProductInstanceState.LIVE).build();
        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId"))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(productInstance);
        when(creatableRelationshipInstanceFilter.filter(productInstanceClient, productInstance)).thenReturn(productInstance.getRelationships());

        //When
        Set<String> inVisibleCreatableLineItems = inVisibleCreatableLineItemRetriever.whatInVisibleLineItemsIHaveCreated("aLineItemId");

        //Then
        assertThat(inVisibleCreatableLineItems.size(), is(0));
    }

    @Test
    public void shouldReturnEmptyListWhenNoRelatedRelationshipsCanBeFoundOnAsset() throws Exception {
        //Given
        AssetDTO asset = AssetDTOFixture.anAsset().build();
        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId"))).thenReturn(asset);

        //When
        Set<String> inVisibleCreatableLineItems = inVisibleCreatableLineItemRetriever.whatInVisibleLineItemsIHaveCreated("aLineItemId");

        //Then
        assertThat(inVisibleCreatableLineItems.size(), is(0));

        verify(productInstanceClient, never()).convertAssetToLightweightInstance(Matchers.<AssetDTO>any());
        verify(creatableRelationshipInstanceFilter, never()).filter(Matchers.<ProductInstanceClient>any(), Matchers.<ProductInstance>any());
    }

    @Test
    public void shouldDeemAssetEligibleForCheckingIfAnyOfTheAssetTreeHasARelatedToRelationship() throws Exception {
        //Given
        ProductInstance visibleCreatableAsset = new DefaultProductInstanceFixture(new ProductOfferingFixture().withVisibleInOnlineSummary(true).build())
            .withLineItemId("visibleLineItemId").build();
        ProductInstance productInstance = new DefaultProductInstanceFixture("aProductId").withLineItemId("aLineItemId")
                                                                                         .withRelatedToProductInstance(visibleCreatableAsset)
                                                                                         .build();
        AssetDTO greatGrandChild = AssetDTOFixture.anAsset().build();
        AssetDTO grandChild = AssetDTOFixture.anAsset().withRelation(greatGrandChild, RelationshipName.newInstance("greatGrandChildRelationship"), RelationshipType.RelatedTo, ProductInstanceState.LIVE).build();
        AssetDTO child = AssetDTOFixture.anAsset().withRelation(grandChild, RelationshipName.newInstance("grandChildRelationship"), RelationshipType.Child, ProductInstanceState.LIVE).build();
        AssetDTO asset = AssetDTOFixture.anAsset()
                                        .withRelation(AssetDTOFixture.anAsset().build(), RelationshipName.newInstance("childRelationship1"), RelationshipType.Child, ProductInstanceState.LIVE)
                                        .withRelation(child, RelationshipName.newInstance("childRelationship2"), RelationshipType.Child, ProductInstanceState.LIVE)
                                        .build();
        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId"))).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(productInstance);
        when(creatableRelationshipInstanceFilter.filter(productInstanceClient, productInstance)).thenReturn(productInstance.getRelationships());

        //When
        inVisibleCreatableLineItemRetriever.whatInVisibleLineItemsIHaveCreated("aLineItemId");

        //Then
        verify(productInstanceClient).convertAssetToLightweightInstance(asset);
    }
}
