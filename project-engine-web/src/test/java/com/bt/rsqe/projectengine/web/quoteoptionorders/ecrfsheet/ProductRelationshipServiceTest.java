package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilVersion;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.bom.parameters.ProductName;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.AssetKeyContainer;
import com.bt.rsqe.projectengine.web.ImportResults;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class ProductRelationshipServiceTest {

    private static final String RELATED_MAPPING = "Related";
    private static final String RELATED_PRODUCT_MAPPING_SHEET = "RelatedTo Mapping";
    private static final String OWNER_PRODUCT_ROW_ID = "owner";
    private static final String RELATED_PRODUCT_ROW_ID = "related";
    private ProductRelationshipService productRelationshipService;
    private ProductInstanceClient productInstanceClient;
    private PmrClient pmrClient;
    private Pmr.ProductOfferings productOfferings;

    @Before
    public void setUp() {
        productInstanceClient = mock(ProductInstanceClient.class);
        pmrClient = mock(PmrClient.class);
        productRelationshipService = new ProductRelationshipService(productInstanceClient, pmrClient);
        productOfferings = mock(Pmr.ProductOfferings.class);
    }

    @Test
    public void shouldCreateRelationShipWhenRelatedAndOwnerInstanceCreated() {
        String relationShipName = "related1";
        ECRFSheet ecrfSheetModelForRelatedMapping = ECRFModelFixture.aECRFModel()
                                                                    .withScode(RELATED_MAPPING)
                                                                    .withSheetName(RELATED_PRODUCT_MAPPING_SHEET)
                                                                    .withSheetIndex(3)
                                                                    .withSheetTypeStrategy(SheetTypeStrategy.Related)
                                                                    .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                     .withOwnerProductId(OWNER_PRODUCT_ROW_ID)
                                                                                                     .withRelatedToId(RELATED_PRODUCT_ROW_ID)
                                                                                                     .withRelationShipName(relationShipName)
                                                                                                     .build())
                                                                    .build();

        AssetKeyContainer assetKeyContainer = new AssetKeyContainer();
        assetKeyContainer.addKey(OWNER_PRODUCT_ROW_ID, new AssetKey("ownerAssetId", Long.valueOf(1)));
        assetKeyContainer.addKey(RELATED_PRODUCT_ROW_ID, new AssetKey("relatedAssetId", Long.valueOf(1)));
        String ownerProductCode = "ownerProductCode";
        AssetDTO ownerAssetDTO = new AssetDTOFixture().withState(ProductInstanceState.LIVE).withProductCode(new ProductCode(ownerProductCode)).build();
        AssetDTO relatedAssetDTO = new AssetDTOFixture().withState(ProductInstanceState.LIVE).withProductCode(new ProductCode("relatedProductCode")).build();
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("ownerAssetId"), new ProductInstanceVersion(Long.valueOf(1)))).thenReturn(ownerAssetDTO);
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("relatedAssetId"), new ProductInstanceVersion(Long.valueOf(1)))).thenReturn(relatedAssetDTO);
        StencilId stencilId = StencilId.versioned(StencilCode.newInstance("relatedProductCode"), StencilVersion.newInstance("1"), ProductName.newInstance("stencilName1"));
        ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withSalesRelationship(new SalesRelationshipFixture().withRelationName(relationShipName).withRelationType(RelationshipType.RelatedTo).withRelatedProductIdentifier("relatedProductCode", relationShipName, stencilId)).build();
        when(productOfferings.get()).thenReturn(productOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ownerProductCode))).thenReturn(productOfferings);

        productRelationshipService.createRelations(ecrfSheetModelForRelatedMapping, assetKeyContainer, new ImportResults());
        verify(productInstanceClient, atLeastOnce()).putAsset(ownerAssetDTO);
        assertTrue(ownerAssetDTO.getRelationships().size() > 0);
        assertTrue(ownerAssetDTO.getRelationships().get(0).getRelationshipName().value().equals(relationShipName));
        assertTrue(ownerAssetDTO.getRelationships().get(0).getRelationshipType().value().equals(RelationshipType.RelatedTo.value()));
    }


    @Test
    public void shouldNotCreateRelationShipWhenOwnerIsntanceNotCreated() {
        String relationShipName = "related1";
        ECRFSheet ecrfSheetModelForRelatedMapping = ECRFModelFixture.aECRFModel()
                                                                    .withScode(RELATED_MAPPING)
                                                                    .withSheetName(RELATED_PRODUCT_MAPPING_SHEET)
                                                                    .withSheetIndex(3)
                                                                    .withSheetTypeStrategy(SheetTypeStrategy.Related)
                                                                    .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                     .withOwnerProductId(OWNER_PRODUCT_ROW_ID)
                                                                                                     .withRelatedToId(RELATED_PRODUCT_ROW_ID)
                                                                                                     .withRelationShipName(relationShipName)
                                                                                                     .build())
                                                                    .build();

        AssetKeyContainer assetKeyContainer = new AssetKeyContainer();
        assetKeyContainer.addKey(RELATED_PRODUCT_ROW_ID, new AssetKey("relatedAssetId", Long.valueOf(1)));
        String ownerProductCode = "ownerProductCode";
        AssetDTO ownerAssetDTO = new AssetDTOFixture().withState(ProductInstanceState.LIVE).withProductCode(new ProductCode(ownerProductCode)).build();
        AssetDTO relatedAssetDTO = new AssetDTOFixture().withState(ProductInstanceState.LIVE).withProductCode(new ProductCode("relatedProductCode")).build();
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("ownerAssetId"), new ProductInstanceVersion(Long.valueOf(1)))).thenReturn(ownerAssetDTO);
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("relatedAssetId"), new ProductInstanceVersion(Long.valueOf(1)))).thenReturn(relatedAssetDTO);
        StencilId stencilId = StencilId.versioned(StencilCode.newInstance("relatedProductCode"), StencilVersion.newInstance("1"), ProductName.newInstance("stencilName1"));
        ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withSalesRelationship(new SalesRelationshipFixture().withRelationName(relationShipName).withRelationType(RelationshipType.RelatedTo).withRelatedProductIdentifier("relatedProductCode", relationShipName, stencilId)).build();
        when(productOfferings.get()).thenReturn(productOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ownerProductCode))).thenReturn(productOfferings);

        productRelationshipService.createRelations(ecrfSheetModelForRelatedMapping, assetKeyContainer, new ImportResults());
        verify(productInstanceClient, never()).putAsset(ownerAssetDTO);
    }

    @Test
    public void shouldNotCreateRelationShipWhenRelatedInstanceNotCreated() {
        String relationShipName = "related1";
        ECRFSheet ecrfSheetModelForRelatedMapping = ECRFModelFixture.aECRFModel()
                                                                    .withScode(RELATED_MAPPING)
                                                                    .withSheetName(RELATED_PRODUCT_MAPPING_SHEET)
                                                                    .withSheetIndex(3)
                                                                    .withSheetTypeStrategy(SheetTypeStrategy.Related)
                                                                    .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                     .withOwnerProductId(OWNER_PRODUCT_ROW_ID)
                                                                                                     .withRelatedToId(RELATED_PRODUCT_ROW_ID)
                                                                                                     .withRelationShipName(relationShipName)
                                                                                                     .build())
                                                                    .build();

        AssetKeyContainer assetKeyContainer = new AssetKeyContainer();
        assetKeyContainer.addKey(OWNER_PRODUCT_ROW_ID, new AssetKey("ownerAssetId", Long.valueOf(1)));
        String ownerProductCode = "ownerProductCode";
        AssetDTO ownerAssetDTO = new AssetDTOFixture().withState(ProductInstanceState.LIVE).withProductCode(new ProductCode(ownerProductCode)).build();
        AssetDTO relatedAssetDTO = new AssetDTOFixture().withState(ProductInstanceState.LIVE).withProductCode(new ProductCode("relatedProductCode")).build();
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("ownerAssetId"), new ProductInstanceVersion(Long.valueOf(1)))).thenReturn(ownerAssetDTO);
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("relatedAssetId"), new ProductInstanceVersion(Long.valueOf(1)))).thenReturn(relatedAssetDTO);
        StencilId stencilId = StencilId.versioned(StencilCode.newInstance("relatedProductCode"), StencilVersion.newInstance("1"), ProductName.newInstance("stencilName1"));
        ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withSalesRelationship(new SalesRelationshipFixture().withRelationName(relationShipName).withRelationType(RelationshipType.RelatedTo).withRelatedProductIdentifier("relatedProductCode", relationShipName, stencilId)).build();
        when(productOfferings.get()).thenReturn(productOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ownerProductCode))).thenReturn(productOfferings);

        productRelationshipService.createRelations(ecrfSheetModelForRelatedMapping, assetKeyContainer, new ImportResults());
        verify(productInstanceClient, never()).putAsset(ownerAssetDTO);
    }

    @Test
    public void shouldNotCreateRelationShipWhenANonExistingRelationShipNameSpecified() {
        String relationShipName = "related1";
        ECRFSheet ecrfSheetModelForRelatedMapping = ECRFModelFixture.aECRFModel()
                                                                    .withScode(RELATED_MAPPING)
                                                                    .withSheetName(RELATED_PRODUCT_MAPPING_SHEET)
                                                                    .withSheetIndex(3)
                                                                    .withSheetTypeStrategy(SheetTypeStrategy.Related)
                                                                    .withRow(ECRFSheetModelRowFixture.aECRFSheetModelRow()
                                                                                                     .withOwnerProductId(OWNER_PRODUCT_ROW_ID)
                                                                                                     .withRelatedToId(RELATED_PRODUCT_ROW_ID)
                                                                                                     .withRelationShipName("nonExistingRelationShip")
                                                                                                     .build())
                                                                    .build();

        AssetKeyContainer assetKeyContainer = new AssetKeyContainer();
        assetKeyContainer.addKey(RELATED_PRODUCT_ROW_ID, new AssetKey("relatedAssetId", Long.valueOf(1)));
        String ownerProductCode = "ownerProductCode";
        AssetDTO ownerAssetDTO = new AssetDTOFixture().withState(ProductInstanceState.LIVE).withProductCode(new ProductCode(ownerProductCode)).build();
        AssetDTO relatedAssetDTO = new AssetDTOFixture().withState(ProductInstanceState.LIVE).withProductCode(new ProductCode("relatedProductCode")).build();
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("ownerAssetId"), new ProductInstanceVersion(Long.valueOf(1)))).thenReturn(ownerAssetDTO);
        when(productInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId("relatedAssetId"), new ProductInstanceVersion(Long.valueOf(1)))).thenReturn(relatedAssetDTO);
        StencilId stencilId = StencilId.versioned(StencilCode.newInstance("relatedProductCode"), StencilVersion.newInstance("1"), ProductName.newInstance("stencilName1"));
        ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withSalesRelationship(new SalesRelationshipFixture().withRelationName(relationShipName).withRelationType(RelationshipType.RelatedTo).withRelatedProductIdentifier("relatedProductCode", relationShipName, stencilId)).build();
        when(productOfferings.get()).thenReturn(productOffering);
        when(pmrClient.productOffering(ProductSCode.newInstance(ownerProductCode))).thenReturn(productOfferings);

        ImportResults importResults = new ImportResults();
        productRelationshipService.createRelations(ecrfSheetModelForRelatedMapping, assetKeyContainer, importResults);
        verify(productInstanceClient, never()).putAsset(ownerAssetDTO);
    }
}
