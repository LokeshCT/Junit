package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.domain.bom.fixtures.BehaviourFixture;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetProperty.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class HeaderRowModelFactoryTest {

    PricingSheetTestDataFixture pricingSheetTestDataFixture;
    HeaderRowModelFactory headerRowModelFactory;
    @Mock
    PmrClient mockPmrClient;
    @Mock
    Pmr.ProductOfferings caSiteProductOfferings;
    @Mock
    Pmr.ProductOfferings steelProductOfferings;
    @Mock
    Pmr.ProductOfferings secondarySteelProductOfferings;

    ArrayList<ProductIdentifier> caProducts = newArrayList(new ProductIdentifier("S0308454", "Connection Acceleration Site"));

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        headerRowModelFactory = new HeaderRowModelFactory(mockPmrClient);
        when(mockPmrClient.productOffering(ProductSCode.newInstance(ProductCodes.ConnectAccelerationSite.productCode()))).thenReturn(caSiteProductOfferings);
        when(caSiteProductOfferings.get()).thenReturn(getCASiteProductOffering());
        when(mockPmrClient.productOffering(ProductSCode.newInstance(ProductCodes.ConnectAccelerationSteelhead.productCode()))).thenReturn(steelProductOfferings);
        when(steelProductOfferings.get()).thenReturn(getStencilProductOffering());
        when(mockPmrClient.productOffering(ProductSCode.newInstance("S0308468"))).thenReturn(secondarySteelProductOfferings);
        when(secondarySteelProductOfferings.get()).thenReturn(getSecondarySteelheadProductOffering());
    }

    @Test
    public void shouldCreateHeader(){
        HeaderRowModel model = headerRowModelFactory.createHeader("CASite", 4, caProducts);
        assertThat(model.getHeaderRow().size(), is(44));
    }

    @Test
    public void shouldReturnCorrectCostLineCount() throws Exception {
        int caService = SiteAgnostic.costSetCount;
        int caSite = SiteInstallable.costSetCount;
        int caSpecialBid = SpecialBid.costSetCount;
        assertThat(caService, is(6));
        assertThat(caSite, is(4));
        assertThat(caSpecialBid, is(2));
    }

    @Test
    public void shouldGetProductModelAndAllItsChildrenInSequentialOrder(){
        Set<BCMHeaderProductModel> allBCMHeaderProductModel = headerRowModelFactory.getChildrenBasedOnProductOffering(caProducts);
        assertThat(allBCMHeaderProductModel.size(), is(2));
        Iterator<BCMHeaderProductModel> iterator = allBCMHeaderProductModel.iterator();
        assertThat(iterator.next().getSCode(), is("S0308454"));
        assertThat(iterator.next().getSCode(), is("S0308469"));
    }

    @Test
    public void shouldGetProductModelAndAllItsChildrenInSequentialOrderWithOutDuplicates(){
        Set<BCMHeaderProductModel> allBCMHeaderProductModel = headerRowModelFactory.getChildrenBasedOnProductOffering(caProducts);
        Assert.assertThat(allBCMHeaderProductModel.size(), is(2));
        Iterator<BCMHeaderProductModel> iterator = allBCMHeaderProductModel.iterator();
        Assert.assertThat(iterator.next().getSCode(), is("S0308454"));
        Assert.assertThat(iterator.next().getSCode(), is("S0308469"));
    }

    @Test
    public void shouldCreateFourSetsOfHeaderCellsForCostIfSheetIsCASite() {
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        headerRowModelFactory.createCostHeader(headerCells, "CASite", 4);
        assertThat(headerCells.size(), is(12));
        assertThat(headerCells.get(0).columnName, is("Cost Description"));
        assertThat(headerCells.get(1).columnName, is("One Time Cost"));
        assertThat(headerCells.get(2).columnName, is("Recurring Cost"));
        assertThat(headerCells.get(3).columnName, is("Cost Description"));
    }

    @Test
    public void shouldCreateSixSetsOfHeaderCellsForCostIfSheetIsCACustomerLevel() {
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        headerRowModelFactory.createCostHeader(headerCells, "CA Customer Level Services", 6);
        assertThat(headerCells.size(), is(18));
        assertThat(headerCells.get(0).columnName, is("Cost Description"));
        assertThat(headerCells.get(1).columnName, is("One Time Cost"));
        assertThat(headerCells.get(2).columnName, is("Recurring Cost"));
        assertThat(headerCells.get(3).columnName, is("Cost Description"));
    }

    @Test
    public void shouldCreateTwoSetsOfHeaderCellsForCostIfSheetIsSpecialBid() {
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        headerRowModelFactory.createCostHeader(headerCells, "CA Special Bid", 2);
        assertThat(headerCells.size(), is(6));
        assertThat(headerCells.get(0).columnName, is("Cost Description"));
        assertThat(headerCells.get(1).columnName, is("One Time Cost"));
        assertThat(headerCells.get(2).columnName, is("Recurring Cost"));
        assertThat(headerCells.get(3).columnName, is("Cost Description"));
    }

    @Test
    public void shouldCreateStaticColumns(){
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        headerRowModelFactory.createStaticHeader(headerCells, ProductSheetStaticColumn.values());
        assertThat(headerCells.size(), is(22));
    }

    @Test
    public void checkProductHeadersCreatesHeaderCellInCorrectSheets(){
        List<HeaderCell> headerCellsForOneSheet = new ArrayList<HeaderCell>();
        List<HeaderCell> headerCellsForAdditionalSheet = new ArrayList<HeaderCell>();
        List<HeaderCell> headerCellsForTwoSheet = new ArrayList<HeaderCell>();

        int endIndexForFirstSheet = headerRowModelFactory.createProductsHeader(headerCellsForOneSheet, headerProductModels() ,10, new ArrayList<ProductIdentifier>());
        int endIndexForAdditionalSheet = headerRowModelFactory.createProductsHeader(headerCellsForAdditionalSheet, headerProductModels(), 251, new ArrayList<ProductIdentifier>());
        int endIndexForTwoSheet = headerRowModelFactory.createProductsHeader(headerCellsForTwoSheet, headerProductModels(), 249, new ArrayList<ProductIdentifier>());

        assertThat(endIndexForFirstSheet, is(18));
        assertThat(headerCellsForOneSheet.size(), is(8));
        assertThat(headerCellsForOneSheet.get(0).sheetIndex, is(0));
        assertThat(headerCellsForOneSheet.get(0).columnIndex, is(10));

        assertThat(endIndexForAdditionalSheet, is(8));
        assertThat(headerCellsForAdditionalSheet.size(), is(8));
        assertThat(headerCellsForAdditionalSheet.get(0).sheetIndex, is(1));
        assertThat(headerCellsForAdditionalSheet.get(0).columnIndex, is(0));

        assertThat(endIndexForTwoSheet, is(4));
        assertThat(headerCellsForTwoSheet.size(), is(8));
        assertThat(headerCellsForTwoSheet.get(0).sheetIndex, is(0));
        assertThat(headerCellsForTwoSheet.get(0).columnIndex, is(249));
        assertThat(headerCellsForTwoSheet.get(7).sheetIndex, is(1));
        assertThat(headerCellsForTwoSheet.get(7).columnIndex, is(3));

    }

    private Set<BCMHeaderProductModel> headerProductModels(){
        Set<BCMHeaderProductModel> headerProductModels = new HashSet<BCMHeaderProductModel>();
        BCMHeaderProductModel bcmHeaderProductModel = new BCMHeaderProductModel(new ProductIdentifier("S0308454", "Connection Acceleration Site"), new ArrayList<Attribute>());
        BCMHeaderProductModel bcmHeaderProductModel1 = new BCMHeaderProductModel(new ProductIdentifier("S0308469", "Steelhead"), new ArrayList<Attribute>());
        headerProductModels.add(bcmHeaderProductModel);
        headerProductModels.add(bcmHeaderProductModel1);
        return headerProductModels;
    }

    @Test
    public void shouldGetProductAndItsChildInSequentialOrder(){
        Set<BCMHeaderProductModel> productsAndItsChildrenInSequentialOrder = headerRowModelFactory.getProductsAndItsChildrenInSequentialOrder(ProductCodes.ConnectAccelerationSite.productCode(), new LinkedHashSet<BCMHeaderProductModel>());
        assertThat(productsAndItsChildrenInSequentialOrder.size(), is(2));
        assertThat(productsAndItsChildrenInSequentialOrder.iterator().next().attributes.size(), is(1));
        assertThat(productsAndItsChildrenInSequentialOrder.iterator().next().attributes.size(), is(1));
        assertThat(productsAndItsChildrenInSequentialOrder.iterator().next().attributes.size(), is(1));
    }

    public ProductOffering getCASiteProductOffering() {
        return ProductOfferingFixture.aProductOffering()
                                     .withProductIdentifier(new ProductIdentifier("S0308454", "Connection Acceleration Site"))
                                     .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(BehaviourFixture.aRFQBehaviour().build()), null)
                                     .withSiteSpecific()
                                     .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                    .withProductIdentifier("S0308469", "Steelhead")
                                                                                    .withRelationType(RelationshipType.Child)
                                                                                    .withRelationName("Steelhead")).build();

    }

    public ProductOffering getStencilProductOffering() {
        return ProductOfferingFixture.aProductOffering()
                                     .withProductIdentifier(new ProductIdentifier("S0308469", "Steelhead"))
                                     .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(BehaviourFixture.aRFQBehaviour().build()), null)
                                     .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                    .withProductIdentifier("S0308469", "SecondarySteelhead")
                                                                                    .withRelationType(RelationshipType.Child).withRelationName("SecondarySteelhead")).build();

    }

    public ProductOffering getSecondarySteelheadProductOffering() {
        return ProductOfferingFixture.aProductOffering()
                                     .withProductIdentifier(new ProductIdentifier("S0308469", "SecondarySteelhead"))
                                     .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(BehaviourFixture.aRFQBehaviour().build()), null)
                                     .build();

    }

    public ProductOffering getCAServiceProductOffering() {
        return ProductOfferingFixture.aProductOffering()
                                     .withProductIdentifier(new ProductIdentifier("caServiceScode", "Connection Acceleration Service"))
                                     .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(BehaviourFixture.aRFQBehaviour().build()), null)
                                     .build();

    }

    public ProductOffering getSpecialBidProductOffering() {
        return ProductOfferingFixture.aProductOffering()
                                     .withProductIdentifier(new ProductIdentifier("specialScode", "Special Bid"))
                                     .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(BehaviourFixture.aRFQBehaviour().build()), null)
                                     .forSpecialBid()
                                     .build();

    }

    public ProductOffering getContractProductOffering() {
        return ProductOfferingFixture.aProductOffering()
                                     .withProductIdentifier(new ProductIdentifier("aContractSCode", "Contract"))
                                     .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(BehaviourFixture.aRFQBehaviour().build()), null)
                                     .withSimpleProductOfferingType(SimpleProductOfferingType.Contract)
                                     .build();
    }

    public ProductOffering getOneVoiceProductOffering() {
        return ProductOfferingFixture.aProductOffering()
                                     .withProductIdentifier(new ProductIdentifier("S0205086", "Onevoice"))
                                     .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(BehaviourFixture.aRFQBehaviour().build()), null)
                                     .build();
    }
}
