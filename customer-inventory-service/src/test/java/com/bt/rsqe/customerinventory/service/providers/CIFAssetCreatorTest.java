package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.AttributeSorter;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.repository.UniqueIdJPARepository;
import com.bt.rsqe.customerinventory.service.updates.UpdateException;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.AttributeOwner;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.util.TestWithRules;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;

public class CIFAssetCreatorTest extends TestWithRules {

    private final PmrHelper pmrHelper = mock(PmrHelper.class);
    private final AttributeSorter attributeSorter = new AttributeSorter();
    private final UniqueIdJPARepository uniqueIDJPArepository = mock(UniqueIdJPARepository.class);
    private final CIFAssetCreator cifAssetCreator = new CIFAssetCreator(pmrHelper, uniqueIDJPArepository, attributeSorter);

    @Test
    public void shouldAddAChildRelationshipToTheAsset() {
        //Setup
        CIFAsset fromAsset = aCIFAsset().withRelationships(0).withRelationshipDefinition("relationshipName", RelationshipType.Child, "ProdID",
                                                                                         "GroupName", null, "stencilId").build();
        CIFAsset toAsset = aCIFAsset().build();
        //Execute
        final CIFAssetRelationship assetRelationship = cifAssetCreator.relateAssets(fromAsset, toAsset, "relationshipName");

        final CIFAssetRelationship expectedAssetRelationship = new CIFAssetRelationship(toAsset, "relationshipName",
                                                                                        RelationshipType.Child,
                                                                                        ProductInstanceState.LIVE);
        //Verify
        assertThat(assetRelationship, is(expectedAssetRelationship));
        assertThat(fromAsset.getRelationships().size(), is(1));
    }

    @Test
    public void shouldThrowAnExceptionWhenRelationshipDefinitionDoesNotExist() {
        expectException(UpdateException.class);

        //Setup
        CIFAsset fromAsset = aCIFAsset().withRelationships(0).withRelationshipDefinition("unknownRelationshipName", RelationshipType.Child, "ProdID",
                                                                                         "GroupName", null, "stencilId").build();
        CIFAsset toAsset = aCIFAsset().build();
        //Execute
        cifAssetCreator.relateAssets(fromAsset, toAsset, "relationshipName");
    }

    @Test
    public void shouldCreateAsset() {
        //Setup
        ProductOffering productOffering = mock(ProductOffering.class);
        when(pmrHelper.getProductOffering("productCode", "STENCIL_CODE")).thenReturn(productOffering);
        when(productOffering.getProductIdentifier()).thenReturn(new ProductIdentifier("ProductID", "VersionId"));
        Attribute attribute = mock(Attribute.class);
        when(attribute.getName()).thenReturn(new AttributeName("AttributeName"));
        when(productOffering.getAttributes()).thenReturn(newArrayList(attribute));
        when(attribute.getName()).thenReturn(new AttributeName("OfferingAttribute"));
        when(productOffering.getProductChargingSchemes()).thenReturn(new ArrayList<ProductChargingScheme>()) ;

        //Execute
        final CIFAsset asset = cifAssetCreator.createAsset("productCode", "STENCIL_CODE", "lineItemId", "siteId", "contractTerm",
                                                            "customerId", "contractId", "projectId", "quoteOptionId", "alternateCity", new ProductCategoryCode("H123"), null, null, null, null);

        //Verify
        assertThat(asset.getProductCode(), is("productCode"));
        assertThat(asset.getLineItemId(), is("lineItemId"));
        assertThat(asset.getSiteId(), is("siteId"));
        assertThat(asset.getContractTerm(), is("contractTerm"));
        assertThat(asset.getCustomerId(), is("customerId"));
        assertThat(asset.getContractId(), is("contractId"));
        assertThat(asset.getProjectId(), is("projectId"));
        assertThat(asset.getQuoteOptionId(), is("quoteOptionId"));
        assertThat(asset.getAlternateCity(), is("alternateCity"));
        assertThat(asset.getCharacteristics(), hasItem(new CIFAssetCharacteristic("OfferingAttribute", null, true)));
        assertThat(asset.getPricingStatus(), is(PricingStatus.NOT_APPLICABLE)) ;
        assertThat(asset.getProductCategoryCode(), is(new ProductCategoryCode("H123"))) ;
    }

    @Test
    public void shouldCreateAssetAsNotPriced() {
        //Setup
        ProductOffering productOffering = mock(ProductOffering.class);
        when(pmrHelper.getProductOffering("productCode", "STENCIL_CODE")).thenReturn(productOffering);
        when(productOffering.getProductIdentifier()).thenReturn(new ProductIdentifier("ProductID", "VersionId"));
        Attribute attribute = mock(Attribute.class);
        when(attribute.getName()).thenReturn(new AttributeName("AttributeName"));
        when(productOffering.getAttributes()).thenReturn(newArrayList(attribute));
        when(attribute.getName()).thenReturn(new AttributeName("OfferingAttribute"));
        when(productOffering.getProductChargingSchemes()).thenReturn(newArrayList(new ProductChargingScheme("name", PricingStrategy.PricingEngine, ProductChargingScheme.PriceVisibility.Customer))) ;

        //Execute
        final CIFAsset asset = cifAssetCreator.createAsset("productCode", "STENCIL_CODE", "lineItemId", "siteId", "contractTerm",
                "customerId", "contractId", "projectId", "quoteOptionId", "alternateCity", ProductCategoryCode.NIL, null, null, null, null);

        //Verify
        assertThat(asset.getProductCode(), is("productCode"));
        assertThat(asset.getLineItemId(), is("lineItemId"));
        assertThat(asset.getSiteId(), is("siteId"));
        assertThat(asset.getContractTerm(), is("contractTerm"));
        assertThat(asset.getCustomerId(), is("customerId"));
        assertThat(asset.getContractId(), is("contractId"));
        assertThat(asset.getProjectId(), is("projectId"));
        assertThat(asset.getQuoteOptionId(), is("quoteOptionId"));
        assertThat(asset.getAlternateCity(), is("alternateCity"));
        assertThat(asset.getCharacteristics(), hasItem(new CIFAssetCharacteristic("OfferingAttribute", null, true)));
        assertThat(asset.getPricingStatus(), is(PricingStatus.NOT_PRICED)) ;
        assertThat(asset.getProductCategoryCode(), is(ProductCategoryCode.NIL)) ;
    }

    @Test
    public void shouldCreateAssetWithProductOfferingAttributes() {
        //Setup
        ProductOffering productOffering = mock(ProductOffering.class);
        when(pmrHelper.getProductOffering("productCode", "STENCIL_CODE")).thenReturn(productOffering);
        when(productOffering.getProductIdentifier()).thenReturn(new ProductIdentifier("ProductID", "VersionId"));
        Attribute productOfferingAttribute = mock(Attribute.class);
        when(productOfferingAttribute.getName()).thenReturn(new AttributeName("OfferingAttribute"));
        when(productOfferingAttribute.getAttributeOwner()).thenReturn(AttributeOwner.Offering);

        Attribute qrefAttribute = mock(Attribute.class);
        when(qrefAttribute.getName()).thenReturn(new AttributeName("QrefAttribute"));
        when(qrefAttribute.getAttributeOwner()).thenReturn(AttributeOwner.Qref);

        when(productOffering.getAttributes()).thenReturn(newArrayList(productOfferingAttribute, qrefAttribute));

        //Execute
        final CIFAsset asset = cifAssetCreator.createAsset("productCode", "STENCIL_CODE", "lineItemId", "siteId", "contractTerm",
                                                           "customerId", "contractId", "projectId", "quoteOptionId", "alternateCity", ProductCategoryCode.NIL, null, null, null, null);

        //Verify
        assertThat(asset.getProductCode(), is("productCode"));
        assertThat(asset.getLineItemId(), is("lineItemId"));
        assertThat(asset.getSiteId(), is("siteId"));
        assertThat(asset.getCharacteristics(), hasItem(new CIFAssetCharacteristic("OfferingAttribute", null, true)));
    }

}