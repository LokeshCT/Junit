package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.repository.StaleAssetException;
import com.bt.rsqe.customerinventory.service.cache.AssetCacheManager;
import com.bt.rsqe.customerinventory.service.cache.CacheAwareTransaction;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.externals.QuoteEngineHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.util.TestWithRules;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.updates.CIFAssetMockHelper.mockCharacteristic;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CreateRelationshipUpdaterTest extends TestWithRules {
    private static final AssetKey ASSET_KEY = new AssetKey("assetId", 2);
    public static final String CLIENT_ID = "clientId";
    public static final String CHILD_RELATIONSHIP_NAME = "childRelationshipName";
    public static final String RELATED_TO_RELATIONSHIP_NAME = "relatedToRelationshipName";
    public static final String PRODUCT_CODE = "productCode";
    public static final String STENCIL_CODE = "stencilCode";
    private CIFAssetOrchestrator assetOrchestrator;
    public static final String SITE_ID = "10";
    public static final String ALTERNATE_CITY = "alternateCity";
    public static final String LINE_ITEM_ID = "lineItemId";
    public static final int LOCK_VERSION = 7;
    private CIFAsset ownerAsset;
    private final QuoteEngineHelper quoteEngineHelper = mock(QuoteEngineHelper.class);
    private static final String OWNER_SITE_ID = "20";
    private final CharacteristicChangeRequestBuilder characteristicChangeRequestBuilder = mock(CharacteristicChangeRequestBuilder.class);
    private final ContributesToChangeRequestBuilder contributesToChangeRequestBuilder = mock(ContributesToChangeRequestBuilder.class);
    private final ExecutionRequestBuilder executionRequestBuilder = mock(ExecutionRequestBuilder.class);
    private final InvalidatePriceRequestBuilder invalidatePriceRequestBuilder = mock(InvalidatePriceRequestBuilder.class) ;
    private final CharacteristicChangeRequest characteristicChangeRequest = new CharacteristicChangeRequest();
    private final CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest();
    private final AutoDefaultRelationshipsRequest autoDefaultRelationshipsRequest = new AutoDefaultRelationshipsRequest(new AssetKey("assetId", 1l), "aLineItemId", 0, PRODUCT_CODE);
    private final SpecialBidAttributesCreationRequest specialBidAttributesCreationRequest = new SpecialBidAttributesCreationRequest(new AssetKey("assetId", 1l));
    private List<CIFAssetUpdateRequest> dependantUpdates = newArrayList((CIFAssetUpdateRequest) characteristicChangeRequest, autoDefaultRelationshipsRequest, characteristicReloadRequest, specialBidAttributesCreationRequest);
    private CIFAssetQuoteOptionItemDetail quoteOptionItemDetail;
    private PmrHelper pmrHelper;
    private DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(characteristicChangeRequestBuilder)
                                                                                                                    .with(executionRequestBuilder)
                                                                                                                    .with(invalidatePriceRequestBuilder)
                                                                                                                    .with(contributesToChangeRequestBuilder)
                                                                                                                    .build();

    @Before
    public void setUp() throws Exception {
        PriceBookDTO priceBook = new PriceBookDTO("id", "requestId", "eup", "ptp", "monthlyRevenue", "triggerMonths");
        quoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(QuoteOptionItemStatus.DRAFT, 0, false, true, "GBP",
                                                                                                "contractTerm", true, JaxbDateTime.NIL,
                                                                                                newArrayList(priceBook), LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false);
        quoteOptionItemDetail.setProductCategoryCode(new ProductCategoryCode("H123"));
        assetOrchestrator = mock(CIFAssetOrchestrator.class);
        pmrHelper = mock(PmrHelper.class);
        CIFAsset relatedAsset = mock(CIFAsset.class);
        when(relatedAsset.getAssetKey()).thenReturn(new AssetKey("assetId", 1l));
        mockCharacteristic(relatedAsset, new CIFAssetCharacteristic(ProductOffering.STENCIL_RESERVED_NAME, "", false));
        when(relatedAsset.getLineItemId()).thenReturn("aLineItemId");
        when(relatedAsset.getProductCode()).thenReturn(PRODUCT_CODE);
        when(relatedAsset.getQuoteOptionItemDetail()).thenReturn(quoteOptionItemDetail);

        ownerAsset = CIFAssetFixture.aCIFAsset()
                                    .withSiteId(OWNER_SITE_ID)
                                    .withRelationshipDefinition(CHILD_RELATIONSHIP_NAME, RelationshipType.Child, "prodId", "groupId", new ArrayList<String>(), "stencilId")
                                    .withRelationshipDefinition(RELATED_TO_RELATIONSHIP_NAME, RelationshipType.RelatedTo, "prodId", "groupId", new ArrayList<String>(), "stencilId")
                                    .with(quoteOptionItemDetail).with(new CIFAssetOfferingDetail("productName", "displayName", "group", "", false, false, "", false, true, SimpleProductOfferingType.CentralService)).build();
        CIFAssetRelationship expectedChildRelationship = new CIFAssetRelationship(relatedAsset, CHILD_RELATIONSHIP_NAME, RelationshipType.Child, ProductInstanceState.LIVE);
        CIFAssetRelationship expectedRelatedToRelationship = new CIFAssetRelationship(relatedAsset, RELATED_TO_RELATIONSHIP_NAME, RelationshipType.RelatedTo, ProductInstanceState.LIVE);

        when(assetOrchestrator.getAsset(new CIFAssetKey(ASSET_KEY, newArrayList(CIFAssetExtension.ProductOfferingRelationshipDetail,
                                                                                CIFAssetExtension.QuoteOptionItemDetail))))
            .thenReturn(ownerAsset);

        when(characteristicChangeRequestBuilder.defaultForAllCharacteristics(any(CIFAsset.class), anyString(), anyString())).thenReturn(characteristicChangeRequest);

        when(contributesToChangeRequestBuilder.buildRequests(any(AssetKey.class), anyString(), any(RelationshipName.class), anyInt())).thenReturn(Sets.<CIFAssetUpdateRequest>newHashSet(characteristicReloadRequest));

        when(assetOrchestrator.createAndRelateAsset(ownerAsset, CHILD_RELATIONSHIP_NAME,
                                                    PRODUCT_CODE, STENCIL_CODE,
                                                    ownerAsset.getLineItemId(), ownerAsset.getSiteId(),
                                                    ownerAsset.getContractTerm(), ownerAsset.getCustomerId(),
                                                    ownerAsset.getContractId(), ownerAsset.getProjectId(),
                                                    ownerAsset.getQuoteOptionId(),
                                                    ALTERNATE_CITY, ownerAsset.getProductCategoryCode())).thenReturn(expectedChildRelationship);
        when(assetOrchestrator.createAndRelateAsset(eq(ownerAsset), eq(RELATED_TO_RELATIONSHIP_NAME),
                                                    eq(PRODUCT_CODE), eq(STENCIL_CODE),
                                                    anyString(), eq(ownerAsset.getSiteId()),
                                                    eq(ownerAsset.getContractTerm()), eq(ownerAsset.getCustomerId()),
                                                    eq(ownerAsset.getContractId()), eq(ownerAsset.getProjectId()),
                                                    eq(ownerAsset.getQuoteOptionId()),
                                                    eq(ALTERNATE_CITY), any(ProductCategoryCode.class))).thenReturn(expectedRelatedToRelationship);

    }

    @Test
    public void shouldReturnCreateRelationshipResponseFromAssetOrchestratorResponses() {
        CreateRelationshipRequest request = new CreateRelationshipRequest(CLIENT_ID, ASSET_KEY, CHILD_RELATIONSHIP_NAME, PRODUCT_CODE,
                                                                          STENCIL_CODE, SITE_ID, ALTERNATE_CITY, LINE_ITEM_ID, LOCK_VERSION);
        CreateRelationshipResponse expectedResponse = new CreateRelationshipResponse(request, CHILD_RELATIONSHIP_NAME, RelationshipType.Child,
                                                                                     ProductInstanceState.LIVE, new AssetKey("assetId", 1l),
                                                                                     dependantUpdates, "");

        final CreateRelationshipUpdater createRelationshipUpdater = new CreateRelationshipUpdater(assetOrchestrator, quoteEngineHelper,
                dependentUpdateBuilderFactory, pmrHelper);
        final CreateRelationshipResponse createRelationshipResponse = createRelationshipUpdater.performUpdate(request);

        assertThat(createRelationshipResponse, is(expectedResponse));
        verify(assetOrchestrator, times(1)).saveAssetAndClearCaches(ownerAsset);
        verify(assetOrchestrator, times(1)).getAsset(any(CIFAssetKey.class));
        verify(assetOrchestrator, times(1)).createAndRelateAsset(any(CIFAsset.class), anyString(), anyString(), anyString(), anyString(),
                                                                 anyString(), anyString(), anyString(), anyString(), anyString(),
                                                                 anyString(), anyString(), any(ProductCategoryCode.class));
        verify(assetOrchestrator, times(1)).extendAsset(any(CIFAsset.class), eq(newArrayList(CIFAssetExtension.ProductRules)));
        verifyNoMoreInteractions(assetOrchestrator);
    }

    @Test
    public void shouldCallAssetOrchestratorWithOverriddenSiteIdWhenSetInRequestForRelatedToRelationship() throws StaleAssetException {
        CIFAsset relatedAsset = CIFAssetFixture.aCIFAsset().withID("assetId").withVersion(1L).withLineItemId("aLineItemId").withProductIdentifier(PRODUCT_CODE, "A.1").withCharacteristic(ProductOffering.STENCIL_RESERVED_NAME, "").with(quoteOptionItemDetail).build();
        final CIFAssetRelationship cifAssetRelationship = new CIFAssetRelationship(relatedAsset, RELATED_TO_RELATIONSHIP_NAME, RelationshipType.RelatedTo, ProductInstanceState.LIVE);
        when(assetOrchestrator.createAndRelateAsset(eq(ownerAsset), eq(RELATED_TO_RELATIONSHIP_NAME),
                                                    eq(PRODUCT_CODE), eq(STENCIL_CODE),
                                                    anyString(), eq(SITE_ID),
                                                    eq(ownerAsset.getContractTerm()), eq(ownerAsset.getCustomerId()),
                                                    eq(ownerAsset.getContractId()), eq(ownerAsset.getProjectId()),
                                                    eq(ownerAsset.getQuoteOptionId()),
                                                    eq(ALTERNATE_CITY), any(ProductCategoryCode.class))).thenReturn(cifAssetRelationship);
        CreateRelationshipRequest request = new CreateRelationshipRequest(CLIENT_ID, ASSET_KEY, RELATED_TO_RELATIONSHIP_NAME, PRODUCT_CODE,
                                                                          STENCIL_CODE, SITE_ID, ALTERNATE_CITY, LINE_ITEM_ID, LOCK_VERSION);
        CreateRelationshipResponse expectedResponse = new CreateRelationshipResponse(request, RELATED_TO_RELATIONSHIP_NAME, RelationshipType.RelatedTo,
                                                                                     ProductInstanceState.LIVE, relatedAsset.getAssetKey(),
                                                                                     dependantUpdates, "");

        final CreateRelationshipUpdater createRelationshipUpdater = new CreateRelationshipUpdater(assetOrchestrator, quoteEngineHelper,
                dependentUpdateBuilderFactory, pmrHelper);
        final CreateRelationshipResponse createRelationshipResponse = createRelationshipUpdater.performUpdate(request);

        assertThat(createRelationshipResponse, is(expectedResponse));
        verify(assetOrchestrator, times(1)).saveAssetAndClearCaches(ownerAsset);
        verify(assetOrchestrator, times(1)).saveAssetAndClearCaches(relatedAsset);
        verify(assetOrchestrator, times(1)).createLineItemLockVersion(anyString());
    }

    @Test
    public void shouldCallAssetOrchestratorWithOwnerSiteIdWhenSiteIdNotSetInRequestForRelatedToRelationship() throws StaleAssetException {
        CIFAsset relatedAsset = CIFAssetFixture.aCIFAsset().withID("assetId").withVersion(1L).withLineItemId("aLineItemId").withProductIdentifier(PRODUCT_CODE, "A.1").withCharacteristic(ProductOffering.STENCIL_RESERVED_NAME, "").with(quoteOptionItemDetail).build();
        final CIFAssetRelationship cifAssetRelationship = new CIFAssetRelationship(relatedAsset, RELATED_TO_RELATIONSHIP_NAME, RelationshipType.RelatedTo, ProductInstanceState.LIVE);
        when(assetOrchestrator.createAndRelateAsset(eq(ownerAsset), eq(RELATED_TO_RELATIONSHIP_NAME),
                                                    eq(PRODUCT_CODE), eq(STENCIL_CODE),
                                                    anyString(), eq(OWNER_SITE_ID),
                                                    eq(ownerAsset.getContractTerm()), eq(ownerAsset.getCustomerId()),
                                                    eq(ownerAsset.getContractId()), eq(ownerAsset.getProjectId()),
                                                    eq(ownerAsset.getQuoteOptionId()),
                                                    eq(ALTERNATE_CITY), eq(ownerAsset.getProductCategoryCode()))).thenReturn(cifAssetRelationship);
        CreateRelationshipRequest request = new CreateRelationshipRequest(CLIENT_ID, ASSET_KEY, RELATED_TO_RELATIONSHIP_NAME, PRODUCT_CODE,
                                                                          STENCIL_CODE, "", ALTERNATE_CITY, LINE_ITEM_ID, LOCK_VERSION);
        CreateRelationshipResponse expectedResponse = new CreateRelationshipResponse(request, RELATED_TO_RELATIONSHIP_NAME, RelationshipType.RelatedTo,
                                                                                     ProductInstanceState.LIVE, relatedAsset.getAssetKey(),
                                                                                     dependantUpdates, "");

        final CreateRelationshipUpdater createRelationshipUpdater = new CreateRelationshipUpdater(assetOrchestrator, quoteEngineHelper,
                dependentUpdateBuilderFactory, pmrHelper);
        final CreateRelationshipResponse createRelationshipResponse = createRelationshipUpdater.performUpdate(request);

        assertThat(createRelationshipResponse, is(expectedResponse));
        verify(assetOrchestrator, times(1)).saveAssetAndClearCaches(ownerAsset);
        verify(assetOrchestrator, times(1)).createLineItemLockVersion(anyString());
    }

    @Test
    public void shouldCallQuoteOptionHelperToCreateQuoteOptionItemWhenRelatedToRelationship() throws StaleAssetException {
        CacheAwareTransaction.set(true);
        CreateRelationshipRequest request = new CreateRelationshipRequest(CLIENT_ID, ASSET_KEY, RELATED_TO_RELATIONSHIP_NAME, PRODUCT_CODE,
                                                                          STENCIL_CODE, "", ALTERNATE_CITY, LINE_ITEM_ID, LOCK_VERSION);

        final CreateRelationshipUpdater createRelationshipUpdater = new CreateRelationshipUpdater(assetOrchestrator, quoteEngineHelper,
                dependentUpdateBuilderFactory, pmrHelper);
        createRelationshipUpdater.performUpdate(request);

        ContractDTO contractDTO = new ContractDTO(ownerAsset.getContractId(),
                                                  ownerAsset.getContractTerm(),
                                                  ownerAsset.getQuoteOptionItemDetail().getPriceBooks());
        verify(quoteEngineHelper, times(1)).createQuoteOptionItem(eq(ownerAsset.getProjectId()),
                eq(ownerAsset.getQuoteOptionId()),
                anyString(),
                eq(request.getProductCode()),
                eq(ownerAsset.getQuoteOptionItemDetail().getLineItemAction()),
                eq(ownerAsset.getContractTerm()),
                eq(contractDTO),
                eq(ownerAsset.getQuoteOptionItemDetail().isIfc()),
                eq(ownerAsset.getQuoteOptionItemDetail().isImportable()),
                eq(ownerAsset.getQuoteOptionItemDetail().getCustomerRequiredDate()),
                eq(new ProductCategoryCode("H123")),
                eq(quoteOptionItemDetail.getBundleItemId()),
                eq(quoteOptionItemDetail.isBundleProduct()));

        verify(assetOrchestrator, times(1)).createLineItemLockVersion(anyString());
        verify(characteristicChangeRequestBuilder, times(1)).defaultForAllCharacteristics(any(CIFAsset.class),
                eq(STENCIL_CODE),
                eq(RELATED_TO_RELATIONSHIP_NAME));
        verify(executionRequestBuilder, times(1)).buildFor(any(CIFAsset.class));

        Set<QuoteOptionItemDTO> createdQuoteOptionItems = AssetCacheManager.getCreatedQuoteOptionItems();
        assertThat(createdQuoteOptionItems.size(), is(1));
    }
}
