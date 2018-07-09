package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.AggregationSet;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.fixtures.BehaviourFixture;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.bom.parameters.BFGOrganisationDetails;
import com.bt.rsqe.domain.order.ItemPrice;
import com.bt.rsqe.domain.product.Behaviour;
import com.bt.rsqe.domain.product.BillingTariffRuleSet;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.DefaultProductInstance;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PricingCaveat;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.pricing.fixture.PricingConfigDTOFixture;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.AccessCaveatDescriptionDTO;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetConstants;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetContractProduct;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetSpecialBidProduct;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture.*;
import static com.bt.rsqe.expedio.fixtures.SiteDTOFixture.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModelFixture.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModelFixture.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.mockito.Mockito.*;

public class PricingSheetTestDataFixture {

    public PricingSheetProductModel aPricingSheetProductModelWithAChild() {
        ProductInstance productInstance = anInstallableRootProductWithAChild();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aPricingSheetProductModelWithAChildAndNonPricableGrandChild() {
        ProductInstance productInstance = anInstallableRootProductWithAChildAndNonPricableGrandChild();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    private PricingSheetProductModel aPricingSheetWithUsageCharges() {
        PriceLine priceLine = PriceLineFixture.aPriceLine().withEupPrice(10.0).withPriceLineName("A Usage Price Line").withPriceType(com.bt.rsqe.domain.product.PriceType.USAGE_BASED).withChargingSchemeName("usageScheme").build();
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("100"), new BigDecimal("50"), "Tier 2", null, null, PriceCategory.FIXED_CHARGE.getLabel()));
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("50"), new BigDecimal("50"), "Tier 2", null, null, PriceCategory.MIN_CHARGE.getLabel()));
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("5"), new BigDecimal("5"), "Tier 2", null, null, PriceCategory.CHARGE_RATE.getLabel()));
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("200"), new BigDecimal("50"), "Tier 1", null, null, PriceCategory.FIXED_CHARGE.getLabel()));
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("150"), new BigDecimal("50"), "Tier 1", null, null, PriceCategory.MIN_CHARGE.getLabel()));
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("5"), new BigDecimal("5"), "Tier 1", null, null, PriceCategory.CHARGE_RATE.getLabel()));

        ProductInstance instance = DefaultProductInstanceFixture.aProductInstance()
                                                             .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                        .withChargingScheme(ProductChargingSchemeFixture.aChargingScheme()
                                                                                                                                                        .withName("usageScheme")
                                                                                                                                                        .withPricingStrategy(PricingStrategy.UsageManagedItem)
                                                                                                                                                        .withPriceVisibility(ProductChargingScheme.PriceVisibility.Customer)
                                                                                                                                                        .build()))
                                                             .withPriceLines(newArrayList(priceLine))
                                                             .build();

        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(instance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);

        return aPricingSheetProductModel()
                    .withProductInstance(instance)
                    .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
                    .withSiteDTO(aSite())
                    .withMergeResult(mergeResult)
                    .withPricingClient(pricingClient)
                    .build();
    }

    public PricingSheetProductModel aPricingSheetICGProductModel() {
        ProductInstance productInstance = aICGProductWithAccessCircuit();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    private void setExpectationsForPricingClientIFC(PricingClient pricingClient) {
        PricingConfig config = PricingConfigDTOFixture.pricingConfig();
        when(pricingClient.getPricingConfig()).thenReturn(config);
    }

    public PricingSheetProductModel aPricingSheetProductModelWithAChildWithARelatedTo() {
        ProductInstance productInstance = anInstallableRootProductWithAChildWithARelatedTo();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aPricingSheetProductModelWithARelatedToAndAChild() {
        ProductInstance productInstance = anInstallableRootProductWithARelatedToAndAChild();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aPricingSheetProductModelWithASpecialBidChild() {
        ProductInstance productInstance = aSpecialBidProduct().withLineItemId("aLineItemId").build();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aPricingSheetProductModelWithAChildForModifyJourney() {
        ProductInstance productInstance = anInstallableRootProductWithAChild();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();

    }

    public PricingSheetProductModel aPricingSheetProductModelWithARelatedToForModifyJourney() {
        ProductInstance productInstance = anInstallableRootProductWithARelatedToAndAChild();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();

    }

    public PricingSheetProductModel aPricingSheetProductModelWithAChildWithARelatedToForModifyJourney() {
        ProductInstance productInstance = anInstallableRootProductWithAChildWithARelatedTo();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();

    }

    public PricingSheetProductModel aPricingSheetProductModelForModifyJourney() {
        ProductInstance productInstance = anInstallableRootProductWithAChild();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    private PricingSheetContractProduct aPricingSheetContractProduct() {
        ProductInstance productInstance = aContractProductInstance();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetContractProduct(aSite(), QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), mergeResult, productInstance, pricingClient, Optional.<ProductInstance>absent());
    }

    private PricingSheetContractProduct aPricingSheetContractProductForModify() {
        ProductInstance productInstance = aContractProductInstance();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetContractProduct(aSite(), QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), mergeResult, productInstance, pricingClient, Optional.<ProductInstance>absent());
    }

    private void setExpectationForModify(ChangeTracker changeTracker) {
        when(changeTracker.changeFor(any(ProductInstance.class))).thenReturn(ChangeType.UPDATE);
        when(changeTracker.changeFor(any(ItemPrice.class))).thenReturn(ChangeType.DELETE);
    }

    private void setExpectationsForAdd(ChangeTracker changeTracker) {
        when(changeTracker.changeFor(any(ProductInstance.class))).thenReturn(ChangeType.ADD);
        when(changeTracker.changeFor(any(ItemPrice.class))).thenReturn(ChangeType.ADD);
    }

    private void setExpectationsForDelete(ChangeTracker changeTracker) {
        when(changeTracker.changeFor(any(ProductInstance.class))).thenReturn(ChangeType.DELETE);
        when(changeTracker.changeFor(any(ItemPrice.class))).thenReturn(ChangeType.DELETE);
    }

    public PricingSheetProductModel aPrincgSheetProductModelWithNoAttributes() {

        ProductInstance productInstance = otherInstallableRootProductWithAChild();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aPricingSheetProductModelWithNoAttributesForModifyJourney() {
        ProductInstance productInstance = otherInstallableRootProductWithAChild();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aPricingSheetProductModelWithTwoSteelHead() {
        ProductInstance productInstance = anInstallableRootProductWithTwoSteelHeads();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(anInstallableRootProductWithTwoSteelHeads())
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aCentralSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();

    }


    public PricingSheetProductModel aSiteAgnosticPricingSheetProductModel() {

        ProductInstance productInstance = aSiteAgnosticProductInstance();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aCentralSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aSiteAgnosticPricingSheetProductModelForModifyJourney() {

        ProductInstance productInstance = aSiteAgnosticProductInstance();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aCentralSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aSiteAgnosticPricingSheetProductModel1() {
        ProductInstance productInstance = aSiteAgnosticProductInstance1();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aCentralSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aSiteAgnosticPricingSheetProductModel1ForModifyJourney() {
        ProductInstance productInstance = aSiteAgnosticProductInstance1();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aCentralSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }

    public PricingSheetProductModel aPricingSheetProductModelWithRelations() {

        ProductInstance productInstance = aRootProductAndChildWithRelationshipName();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return aPricingSheetProductModel()
            .withProductInstance(productInstance)
            .withQuoteOptionItem(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build())
            .withSiteDTO(aCentralSite())
            .withMergeResult(mergeResult)
            .withPricingClient(pricingClient)
            .build();
    }


    public PricingSheetDataModel pricingSheetTestData() {
        PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetProductModelWithAChild();
        PricingSheetProductModel pricingSheetProductModel2 = aSiteAgnosticPricingSheetProductModel();
        PricingSheetProductModel pricingSheetProductModel3 = aPrincgSheetProductModelWithNoAttributes();
        PricingSheetProductModel pricingSheetProductModel4 = aSiteAgnosticPricingSheetProductModel1();
        PricingSheetProductModel pricingSheetProductModel5 = aPricingSheetWithUsageCharges();
        Set<PriceBookDTO> priceBooks = newHashSet();
        priceBooks.add(new PriceBookDTO("id","reqId","CA1","CA1 GL","",""));
        priceBooks.add(new PriceBookDTO("id2","reqId","CI1","CI1 GL","",""));
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withPricingSheetProductModel(pricingSheetProductModel2)
            .withPricingSheetProductModel(pricingSheetProductModel3)
            .withPricingSheetProductModel(pricingSheetProductModel4)
            .withPricingSheetProductModel(pricingSheetProductModel5)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aCentralSite())
            .withPriceBooks(priceBooks)
            .build();
    }

    public PricingSheetDataModel pricingSheetTestDataForNonPricableGrandChild() {
        PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetProductModelWithAChildAndNonPricableGrandChild();
        Set<PriceBookDTO> priceBooks = newHashSet();
        priceBooks.add(new PriceBookDTO("id","reqId","CA1","CA1 GL","",""));
        priceBooks.add(new PriceBookDTO("id2","reqId","CI1","CI1 GL","",""));
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aCentralSite())
            .withPriceBooks(priceBooks)
            .build();
    }

    public PricingSheetDataModel pricingSheetContractTestData() {
        PricingSheetContractProduct pricingSheetContractProduct = aPricingSheetContractProduct();

        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetContractProduct(pricingSheetContractProduct)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aCentralSite())
            .build();
    }

    public PricingSheetDataModel pricingSheetContractTestDataForModify() {
        PricingSheetContractProduct pricingSheetContractProduct = aPricingSheetContractProductForModify();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetContractProduct(pricingSheetContractProduct)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aCentralSite())
            .build();
    }

    public PricingSheetDataModel pricingSheetICrICgTestData() {
        PricingSheetProductModel icgPricingSheetProductModel1 = aPricingSheetProductModelWithAChildWithARelatedTo();
        PricingSheetProductModel icrPricingSheetProductModel2 = aPricingSheetProductModelWithARelatedToAndAChild();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetProductModel(icgPricingSheetProductModel1)
            .withPricingSheetProductModel(icrPricingSheetProductModel2)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aCentralSite())
            .build();
    }

    public PricingSheetDataModel pricingSheetTestDataForModifyJourney() {
        PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetProductModelWithAChildForModifyJourney();
        PricingSheetProductModel pricingSheetProductModel2 = aSiteAgnosticPricingSheetProductModelForModifyJourney();
        PricingSheetProductModel pricingSheetProductModel3 = aPricingSheetProductModelWithNoAttributesForModifyJourney();
        PricingSheetProductModel pricingSheetProductModel4 = aSiteAgnosticPricingSheetProductModel1ForModifyJourney();
        Set<PriceBookDTO> priceBooks = newHashSet();
        priceBooks.add(new PriceBookDTO("id","reqId","CA1","CA1 GL","",""));
        priceBooks.add(new PriceBookDTO("id2","reqId","CI1","CI1 GL","",""));
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withPricingSheetProductModel(pricingSheetProductModel2)
            .withPricingSheetProductModel(pricingSheetProductModel3)
            .withPricingSheetProductModel(pricingSheetProductModel4)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aCentralSite())
            .withPriceBooks(priceBooks)
            .build();
    }

    public PricingSheetDataModel pricingSheetICrICgTestDataForModifyJourney() {
        PricingSheetProductModel icrPricingSheetProductModel1 = aPricingSheetProductModelWithARelatedToForModifyJourney();
        PricingSheetProductModel icgPricingSheetProductModel1 = aPricingSheetProductModelWithAChildWithARelatedToForModifyJourney();
        Set<String> productNames = newHashSet();
        productNames.add("Connect Acceleration");
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetProductModel(icrPricingSheetProductModel1)
            .withPricingSheetProductModel(icgPricingSheetProductModel1)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aCentralSite())
            .withProductNames(productNames)
            .build();
    }

    public PricingSheetDataModel pricingSheetSpecialBidTestData() {
        PricingSheetSpecialBidProduct pricingSheetProductModel1 = aPricingSheetSpecialBidProductModel();
        PricingSheetSpecialBidProduct pricingSheetProductModel2 = aPricingSheetSpecialBidProductModel();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel2)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aSite())
            .build();
    }

    public PricingSheetDataModel pricingSheetSpecialBidTestDataForBCM() {
        PricingSheetSpecialBidProduct pricingSheetProductModel1 = aPricingSheetSpecialBidProductModelForBCM(true);
        PricingSheetSpecialBidProduct pricingSheetProductModel2 = aPricingSheetSpecialBidProductModelForBCM(false);
        PricingSheetSpecialBidProduct pricingSheetProductModel3 = aPricingSheetSpecialBidProductModelCeaseForBCM(true);
        PricingSheetSpecialBidProduct pricingSheetProductModel4 = aPricingSheetSpecialBidProductModelCeaseForBCM(false);
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel2)
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel3)
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel4)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aSite())
            .build();
    }

    public PricingSheetDataModel pricingSheetSpecialBidTestDataForModifyJourney() {
        PricingSheetSpecialBidProduct pricingSheetProductModel1 = aPricingSheetSpecialBidProductModelForModifyJourney();
        PricingSheetSpecialBidProduct pricingSheetProductModel2 = aPricingSheetSpecialBidProductModelForModifyJourney();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel2)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aSite())
            .build();
    }

    private PricingSheetSpecialBidProduct aPricingSheetSpecialBidProductModel() {

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("CONFIGURATION_CATEGORY", "Category");
        attributes.put("CAVEATS", "Description of caveats");
        ProductInstance productInstance = aSpecialBidProduct().withLineItemId("aLineItemId").build();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetSpecialBidProduct(aSite(),
                                                 QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), attributes, mergeResult, productInstance, pricingClient, Optional.<ProductInstance>absent());
    }

    private PricingSheetSpecialBidProduct aPricingSheetSpecialBidProductModelForBCM(Boolean isSiteInstallable) {

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("CONFIGURATION_CATEGORY", "Category");
        attributes.put("CAVEATS", "Description of caveats");
        ProductInstance productInstance = aSpecialBidProductWithSiteInstallable(isSiteInstallable).withLineItemId("aLineItemId").build();
        productInstance.addPriceLine(aPriceLine("Root Product One time price1", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price1", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time Cost", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental Cost", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time price3", "M0302166", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price3", "M0302166", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time price4", "M0302167", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price4", "M0302167", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.PRICE_TO_PARTNER.getLabel(), 333.00, "E", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.PRICE_TO_PARTNER.getLabel(), 333.00, "E", "123"));
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        return new PricingSheetSpecialBidProduct(aSite(),
                                                 QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), attributes, mergeResult, productInstance, pricingClient, Optional.<ProductInstance>absent());
    }

    private PricingSheetSpecialBidProduct aPricingSheetSpecialBidProductModelCeaseForBCM(Boolean isSiteInstallable) {

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("CONFIGURATION_CATEGORY", "Category");
        attributes.put("CAVEATS", "Description of caveats");
        ProductInstance productInstance = aSpecialBidProductWithSiteInstallable(isSiteInstallable).withLineItemId("aLineItemId").build();
        productInstance.addPriceLine(aPriceLine("Root Product One time price1", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price1", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time Cost", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental Cost", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time price3", "M0302166", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price3", "M0302166", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time price4", "M0302167", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price4", "M0302167", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.PRICE_TO_PARTNER.getLabel(), 333.00, "E", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.PRICE_TO_PARTNER.getLabel(), 333.00, "E", "123"));
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForDelete(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetSpecialBidProduct(aSite(),
                                                 QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), attributes, mergeResult, productInstance, pricingClient, Optional.<ProductInstance>absent());
    }

    private PricingSheetSpecialBidProduct aPricingSheetSpecialBidProductModelForModifyJourney() {

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("CONFIGURATION_CATEGORY", "Category");
        attributes.put("CAVEATS", "Description of caveats");
        ProductInstance productInstance = aSpecialBidProduct().withLineItemId("aLineItemId").build();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetSpecialBidProduct(aSite(),
                                                 QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), attributes, mergeResult, productInstance, pricingClient, Optional.<ProductInstance>absent());
    }

    private PricingSheetSpecialBidProduct aPricingSheetSpecialBidProductModelForModifyJourneywithSite() {

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("CONFIGURATION_CATEGORY", "Category");
        attributes.put("CAVEATS", "Description of caveats");
        ProductInstance productInstance = aSpecialBidProductwithSiteSpecific().withLineItemId("aLineItemId").build();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationForModify(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetSpecialBidProduct(aSite(),
                                                 QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), attributes, mergeResult, productInstance, pricingClient, Optional.<ProductInstance>absent());
    }


    public ProductInstance aRootProductAndChildWithRelationshipName() {
        ProductChargingScheme caSiteManagementScheme = new ProductChargingScheme("Connect Acceleration Site Management Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Sales, "Site Management", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme caSitePriceScheme = new ProductChargingScheme("Connect Acceleration Site Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Customer, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadTotalPrice = new ProductChargingScheme("Total Steelhead Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Sales, "Steelhead", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadPrice = new ProductChargingScheme("Steelhead Price", PricingStrategy.PricingEngine, "Steelhead", ProductChargingScheme.PriceVisibility.Hidden, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadVendorMaintenancePrice = new ProductChargingScheme("Steelhead Vendor Maintenance Price", PricingStrategy.StencilManagedItem, "Steelhead", ProductChargingScheme.PriceVisibility.Hidden, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductCodes connectAcceleration = ProductCodes.ConnectAccelerationSite;
        ProductCodes connectAccelerationSteelhead = ProductCodes.ConnectAccelerationSteelhead;
        ProductInstance connectAccelerationSite = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("connectAccelerationSiteId")
                                                                               .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                                                          .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                                          .withAttribute(PricingSheetConstants.NAME_ATTRIBUTE)
                                                                                                                          .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                                          .withProductIdentifier(new ProductIdentifier(connectAcceleration.productCode(), connectAcceleration.productName(), "1.0"))
                                                                                                                          .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                         .withRelationName("primary steelHead")
                                                                                                                                                                         .withProductIdentifier(connectAccelerationSteelhead.productCode(), connectAccelerationSteelhead.productName())
                                                                                                                                                                         .withRelationType(RelationshipType.Child))
                                                                                                                          .withChargingSchemes(newArrayList(caSiteManagementScheme, caSitePriceScheme)))
                                                                               .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                               .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                               .withAttributeValue(PricingSheetConstants.NAME_ATTRIBUTE, "Connect Acceleration Site")
                                                                               .build();
        ProductCodes steelheadVendorMaintenance = ProductCodes.SteelheadVendorMaintenance;
        connectAccelerationSite.addPriceLine(aPriceLine("Connect Acceleration Site Price", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Connect Acceleration Site Price", "1"));
        connectAccelerationSite.addPriceLine(aPriceLine("Connect Acceleration Site Price", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Connect Acceleration Site Price", "2"));
        connectAccelerationSite.addPriceLine(aPriceLine("Connect Acceleration Site Management Price", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Connect Acceleration Site Management Price", "3"));
        connectAccelerationSite.addPriceLine(aPriceLine("Connect Acceleration Site Management Price", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Connect Acceleration Site Management Price", "4"));
        ProductInstance steelHead = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("steelHeadId")
                                                                 .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                                            .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                            .withAttribute(PricingSheetConstants.NAME_ATTRIBUTE)
                                                                                                            .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                            .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                            .withProductIdentifier(new ProductIdentifier(connectAccelerationSteelhead.productCode(), connectAccelerationSteelhead.productName(), "1.0"))
                                                                                                            .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                           .withRelationName("vendor maintenance")
                                                                                                                                                           .withProductIdentifier(steelheadVendorMaintenance.productCode(), steelheadVendorMaintenance.productName())
                                                                                                                                                           .withRelationType(RelationshipType.Child))
                                                                                                            .withChargingSchemes(newArrayList(steelHeadPrice, steelHeadTotalPrice)))
                                                                 .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                 .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                 .withAttributeValue(PricingSheetConstants.NAME_ATTRIBUTE,"Steelhead")
                                                                 .build();
        steelHead.addPriceLine(aPriceLine("Total Steelhead Price", "M0302170", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Total Steelhead Price", "1"));
        steelHead.addPriceLine(aPriceLine("Total Steelhead Price", "M0302170", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Total Steelhead Price", "2"));
        steelHead.addPriceLine(aPriceLine("Steelhead Price", "M0302171", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Steelhead Price", "3"));
        steelHead.addPriceLine(aPriceLine("Steelhead Price", "M0302171", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Steelhead Price", "4"));
        steelHead.setParent(connectAccelerationSite);
        DefaultProductInstance vendorMaintenance = DefaultProductInstanceFixture.aProductInstance()
                                                                                .withProductInstanceId("vendorMaintenanceId")
                                                                                .withProductOffering(ProductOfferingFixture
                                                                                                         .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                                         .withProductIdentifier(new ProductIdentifier(steelheadVendorMaintenance.productCode(), steelheadVendorMaintenance.productName(), "1.0"))
                                                                                                         .withChargingSchemes(newArrayList(steelHeadVendorMaintenancePrice)).withAttribute(ProductOffering.PART_TYPE_IDENTIFIER)
                                                                                )
                                                                                .withAttributeValue(ProductOffering.PART_TYPE_IDENTIFIER, "Vendor Maintenance").build();
        vendorMaintenance.addPriceLine(aPriceLine("Steelhead Vendor Maintenance Price", "M0302172", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Steelhead Price", "3"));
        vendorMaintenance.addPriceLine(aPriceLine("Steelhead Vendor Maintenance Price", "M0302172", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Steelhead Price", "4"));
        vendorMaintenance.addPriceLine(aPriceLine("Steelhead Vendor Maintenance Price", "M0302172", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 200.00, "Steelhead Price", "3"));
        vendorMaintenance.addPriceLine(aPriceLine("Steelhead Vendor Maintenance Price", "M0302172", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 100.00, "Steelhead Price", "4"));
        connectAccelerationSite.addRelationship(new ProductSalesRelationshipInstance(aSalesRelationship()
                                                                                         .withRelationName("primary steelHead").build(), steelHead));
        steelHead.addRelationship(new ProductSalesRelationshipInstance(aSalesRelationship()
                                                                           .withRelationName("vendor maintenance").build(), vendorMaintenance));


        return connectAccelerationSite;
    }

    public ProductInstance aCARootProductAndChildWithRelationshipNameWithoutVendorMaintainance() {
        ProductChargingScheme caSiteManagementScheme = new ProductChargingScheme("Connect Acceleration Site Management Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Sales, "Site Management", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme caSitePriceScheme = new ProductChargingScheme("Connect Acceleration Site Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Customer, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadTotalPrice = new ProductChargingScheme("Total Steelhead Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Sales, "Steelhead", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadPrice = new ProductChargingScheme("Steelhead Price", PricingStrategy.PricingEngine, "Steelhead", ProductChargingScheme.PriceVisibility.Hidden, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductCodes connectAcceleration = ProductCodes.ConnectAccelerationSite;
        ProductCodes connectAccelerationSteelhead = ProductCodes.ConnectAccelerationSteelhead;
        ProductInstance connectAccelerationSite = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("connectAccelerationSiteId")
                                                                               .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                                                          .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                                          .withAttribute(PricingSheetConstants.NAME_ATTRIBUTE)
                                                                                                                          .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                                          .withProductIdentifier(new ProductIdentifier(connectAcceleration.productCode(), connectAcceleration.productName(), "1.0"))
                                                                                                                          .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                         .withRelationName("primary steelHead")
                                                                                                                                                                         .withProductIdentifier(connectAccelerationSteelhead.productCode(), connectAccelerationSteelhead.productName())
                                                                                                                                                                         .withRelationType(RelationshipType.Child))
                                                                                                                          .withChargingSchemes(newArrayList(caSiteManagementScheme, caSitePriceScheme)))
                                                                               .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                               .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                               .withAttributeValue(PricingSheetConstants.NAME_ATTRIBUTE, "Connect Acceleration Site")
                                                                               .build();
        ProductCodes steelheadVendorMaintenance = ProductCodes.SteelheadVendorMaintenance;
        connectAccelerationSite.addPriceLine(aPriceLine("Connect Acceleration Site Price", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Connect Acceleration Site Price", "1"));
        connectAccelerationSite.addPriceLine(aPriceLine("Connect Acceleration Site Price", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Connect Acceleration Site Price", "2"));
        connectAccelerationSite.addPriceLine(aPriceLine("Connect Acceleration Site Management Price", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Connect Acceleration Site Management Price", "3"));
        connectAccelerationSite.addPriceLine(aPriceLine("Connect Acceleration Site Management Price", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Connect Acceleration Site Management Price", "4"));
        ProductInstance steelHead = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("steelHeadId")
                                                                 .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                                            .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                            .withAttribute(PricingSheetConstants.NAME_ATTRIBUTE)
                                                                                                            .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                            .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                            .withProductIdentifier(new ProductIdentifier(connectAccelerationSteelhead.productCode(), connectAccelerationSteelhead.productName(), "1.0"))
                                                                                                            .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                           .withRelationName("vendor maintenance")
                                                                                                                                                           .withProductIdentifier(steelheadVendorMaintenance.productCode(), steelheadVendorMaintenance.productName())
                                                                                                                                                           .withRelationType(RelationshipType.Child))
                                                                                                            .withChargingSchemes(newArrayList(steelHeadPrice, steelHeadTotalPrice)))
                                                                 .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                 .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                 .withAttributeValue(PricingSheetConstants.NAME_ATTRIBUTE,"Steelhead")
                                                                 .build();
        steelHead.setParent(connectAccelerationSite);
        connectAccelerationSite.addRelationship(new ProductSalesRelationshipInstance(aSalesRelationship()
                                                                                         .withRelationName("primary steelHead").build(), steelHead));
        return connectAccelerationSite;
    }

    public ProductInstance aCARootProductAndChildWithRelationshipNameAndWithoutSitemanagementPrticelines() {
        ProductChargingScheme caSiteManagementScheme = new ProductChargingScheme("Connect Acceleration Site Management Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Sales, "Site Management", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme caSitePriceScheme = new ProductChargingScheme("Connect Acceleration Site Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Customer, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadTotalPrice = new ProductChargingScheme("Total Steelhead Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Sales, "Steelhead", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadPrice = new ProductChargingScheme("Steelhead Price", PricingStrategy.PricingEngine, "Steelhead", ProductChargingScheme.PriceVisibility.Hidden, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductCodes connectAcceleration = ProductCodes.ConnectAccelerationSite;
        ProductCodes connectAccelerationSteelhead = ProductCodes.ConnectAccelerationSteelhead;
        ProductInstance connectAccelerationSite = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("connectAccelerationSiteId")
                                                                               .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                                                          .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                                          .withAttribute(PricingSheetConstants.NAME_ATTRIBUTE)
                                                                                                                          .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                                          .withProductIdentifier(new ProductIdentifier(connectAcceleration.productCode(), connectAcceleration.productName(), "1.0"))
                                                                                                                          .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                         .withRelationName("primary steelHead")
                                                                                                                                                                         .withProductIdentifier(connectAccelerationSteelhead.productCode(), connectAccelerationSteelhead.productName())
                                                                                                                                                                         .withRelationType(RelationshipType.Child))
                                                                                                                          .withChargingSchemes(newArrayList(caSiteManagementScheme, caSitePriceScheme)))
                                                                               .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                               .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                               .withAttributeValue(PricingSheetConstants.NAME_ATTRIBUTE, "Connect Acceleration Site")
                                                                               .build();
        ProductCodes steelheadVendorMaintenance = ProductCodes.SteelheadVendorMaintenance;
        ProductInstance steelHead = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("steelHeadId")
                                                                 .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                                            .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                            .withAttribute(PricingSheetConstants.NAME_ATTRIBUTE)
                                                                                                            .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                            .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                            .withProductIdentifier(new ProductIdentifier(connectAccelerationSteelhead.productCode(), connectAccelerationSteelhead.productName(), "1.0"))
                                                                                                            .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                           .withRelationName("vendor maintenance")
                                                                                                                                                           .withProductIdentifier(steelheadVendorMaintenance.productCode(), steelheadVendorMaintenance.productName())
                                                                                                                                                           .withRelationType(RelationshipType.Child))
                                                                                                            .withChargingSchemes(newArrayList(steelHeadPrice, steelHeadTotalPrice)))
                                                                 .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                 .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                 .withAttributeValue(PricingSheetConstants.NAME_ATTRIBUTE,"Steelhead")
                                                                 .build();
        steelHead.addPriceLine(aPriceLine("Total Steelhead Price", "M0302170", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Total Steelhead Price", "1"));
        steelHead.addPriceLine(aPriceLine("Total Steelhead Price", "M0302170", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Total Steelhead Price", "2"));
        steelHead.addPriceLine(aPriceLine("Steelhead Price", "M0302171", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Steelhead Price", "3"));
        steelHead.addPriceLine(aPriceLine("Steelhead Price", "M0302171", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Steelhead Price", "4"));
        steelHead.setParent(connectAccelerationSite);
        return connectAccelerationSite;
    }

    public ProductInstance anInstallableRootProductWithAChild() {

        ProductInstance rootProductInstance = setUpRootInstance();

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child1ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE)
                                                                                              .withProductIdentifier(new ProductIdentifier("S0308469", "child1 product name", "1.0", "Root Display Name"))
                                                                                              .withChargingSchemes(createChildChargingSchemes())
                                                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                             .withRelationName("relationship")
                                                                                                                                             .withProductIdentifier("subChild1Scode", "subChild1")
                                                                                                                                             .withRelationType(RelationshipType.Child))
                                                                     )
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                     .withPricingStatus(PricingStatus.FIRM)
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "G", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "G", "123"));
        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        DefaultProductInstance subChild1 = DefaultProductInstanceFixture.aProductInstance()
                                                                        .withProductInstanceId("subChild1ProductInstanceId")
                                                                        .withProductOffering(ProductOfferingFixture
                                                                                                 .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                                 .withProductIdentifier(new ProductIdentifier("subChild1Scode", "subChild1 product name", "1.0"))
                                                                                                 .withChargingSchemes(createGrandChildChargingSchemes()).withAttribute(ProductOffering.PART_TYPE_IDENTIFIER)
                                                                        ).withAttributeValue(ProductOffering.PART_TYPE_IDENTIFIER, "SW Licence")
                                                                        .build();

        subChild1.addPriceLine(aPriceLine("SubChild1 One time price1", "M0302300", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "H", "123"));
        subChild1.addPriceLine(aPriceLine("SubChild1 Rental price1", "M0302300", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "H", "123"));
        subChild1.addPriceLine(aPriceLine("SubChild1 One time price2", "M0302301", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "I", "123"));
        subChild1.addPriceLine(aPriceLine("SubChild1 Rental price2", "M0302301", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "I", "123"));
        child1.addChildProductInstance(subChild1, RelationshipType.Child);

        return rootProductInstance;
    }

    public ProductInstance anInstallableRootProductWithAChildAndNonPricableGrandChild(){
        ProductInstance rootProductInstance = setUpRootInstance();

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child1ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE)
                                                                                              .withProductIdentifier(new ProductIdentifier("S0308469", "child1 product name", "1.0", "Root Display Name"))
                                                                                              .withChargingSchemes(createChildChargingSchemes())
                                                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                             .withRelationName("relationship")
                                                                                                                                             .withProductIdentifier("subChild1Scode", "subChild1")
                                                                                                                                             .withRelationType(RelationshipType.Child))
                                                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                             .withRelationName("relationship2")
                                                                                                                                             .withProductIdentifier("subChild2Scode", "subChild2")
                                                                                                                                             .withRelationType(RelationshipType.Child))
                                                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                             .withRelationName("relationship3")
                                                                                                                                             .withProductIdentifier("subChild3Scode", "subChild3")
                                                                                                                                             .withRelationType(RelationshipType.Child))
                                                                     )
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                     .withPricingStatus(PricingStatus.FIRM)
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "G", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "G", "123"));
        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        DefaultProductInstance subChild1 = DefaultProductInstanceFixture.aProductInstance()
                                                                        .withProductInstanceId("subChild1ProductInstanceId")
                                                                        .withProductOffering(ProductOfferingFixture
                                                                                                 .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                                 .withProductIdentifier(new ProductIdentifier("subChild1Scode", "subChild1 product name", "1.0"))
                                                                                                 .withChargingSchemes(createGrandChildChargingSchemes()).withAttribute(ProductOffering.PART_TYPE_IDENTIFIER)
                                                                        ).withAttributeValue(ProductOffering.PART_TYPE_IDENTIFIER, "SW Licence")
                                                                        .build();

        DefaultProductInstance subChild2 = DefaultProductInstanceFixture.aProductInstance()
                                                                        .withProductInstanceId("subChild1ProductInstance2Id")
                                                                        .withProductOffering(ProductOfferingFixture
                                                                                                 .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                                 .withProductIdentifier(new ProductIdentifier("subChild2Scode", "subChild2 product name", "1.0"))
                                                                                                 .withChargingSchemes(createGrandChildChargingSchemes()).withAttribute(ProductOffering.PART_TYPE_IDENTIFIER)
                                                                        ).withAttributeValue(ProductOffering.PART_TYPE_IDENTIFIER, "SW Licence")
                                                                        .build();

        DefaultProductInstance subChild3 = DefaultProductInstanceFixture.aProductInstance()
                                                                        .withProductInstanceId("subChild1ProductInstance3Id")
                                                                        .withProductOffering(ProductOfferingFixture
                                                                                                 .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                                 .withProductIdentifier(new ProductIdentifier("subChild3Scode", "subChild3 product name", "1.0"))
                                                                                                 .withChargingSchemes(createGrandChildChargingSchemes()).withAttribute(ProductOffering.PART_TYPE_IDENTIFIER)
                                                                        ).withAttributeValue(ProductOffering.PART_TYPE_IDENTIFIER, "HW Licence")
                                                                        .build();

        child1.addChildProductInstance(subChild1, RelationshipType.Child);
        child1.addChildProductInstance(subChild2, RelationshipType.Child);
        child1.addChildProductInstance(subChild3, RelationshipType.Child);

        return rootProductInstance;
    }

    private ProductInstance setUpRootInstance(){
        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId1")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withSiteSpecific()
                                                                                                                      .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                                      .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE)
                                                                                                                      .withProductIdentifier(new ProductIdentifier("S0308454", "Root Product Name", "1.0", "Root Display Name"))
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withRelationName("relationship")
                                                                                                                                                                     .withProductIdentifier("S0308469", "child1")
                                                                                                                                                                     .withRelationType(RelationshipType.Child)).withChargingSchemes(createParentChargingSchemes()))
                                                                           .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                           .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                           .withPricingStatus(PricingStatus.FIRM)
                                                                           .build();

        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price1", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price1", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time Cost", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental Cost", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price3", "M0302166", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price3", "M0302166", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price4", "M0302167", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price4", "M0302167", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        return rootProductInstance;
    }

    public ProductInstance aICGProductWithAccessCircuit() {

        final List<ProductChargingScheme> parentChargingSchemes = createParentChargingSchemes();
        parentChargingSchemes.add(new ProductChargingScheme("ICg Access Circuit",PricingStrategy.Aggregation, AggregationSet.NIL.getValue(), ProductChargingScheme.PriceVisibility.Sales,"Access", Lists.<BillingTariffRuleSet>newArrayList(), null));
        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId1")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withSiteSpecific()
                                                                                                                      .withProductIdentifier(new ProductIdentifier("S0317991", "Internet Connect Topology", "1.0", "Internet Connect Topology"))
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withRelationName("relationship")
                                                                                                                                                                     .withProductIdentifier("S0316769", "InternetService")
                                                                                                                                                                     .withRelationType(RelationshipType.Child)
                                                                                                                                                                     .withRelationName("PrimaryInternetService")).withChargingSchemes(parentChargingSchemes))
                                                                           .withPricingStatus(PricingStatus.FIRM)
                                                                           .build();

        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price1", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price1", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time Cost", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental Cost", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price3", "M0302166", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price3", "M0302166", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price4", "M0302167", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price4", "M0302167", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        rootProductInstance.addPriceLine(aPriceLine("ICg Access Circuit", "M0305215", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "ICg Access Circuit", "111"));
        rootProductInstance.addPriceLine(aPriceLine("ICg Access Circuit", "M0305215", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "ICg Access Circuit", "111"));

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child1ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkService)
                                                                                              .withAttribute(PricingSheetConstants.SPEED)
                                                                                              .withProductIdentifier(new ProductIdentifier("S0316769", "InternetService", "1.0", "InternetService"))
                                                                                              .withChargingSchemes(createChildChargingSchemes())
                                                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                             .withProductIdentifier("S0316770", "Access Circuit")
                                                                                                                                             .withRelationType(RelationshipType.RelatedTo)
                                                                                                                                             .withRelationName("Access"))
                                                                     )
                                                                     .withAttributeValue(PricingSheetConstants.SPEED, "512Mbps")
                                                                     .withPricingStatus(PricingStatus.FIRM)
                                                                     .build();
        DefaultProductInstance accessProduct = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("accessProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withSimpleProductOfferingType(SimpleProductOfferingType.Bearer)
                                                                                              .withAttribute(PricingSheetConstants.ACCESS_TECHNOLOGY)
                                                                                              .withAttribute(PricingSheetConstants.INTERFACE_TYPE)
                                                                                              .withAttribute(ProductOffering.APE_FLAG)
                                                                                              .withProductIdentifier(new ProductIdentifier("S0316770", "Access Circuit", "1.0", "Access Circuit"))
                                                                                              .withChargingSchemes(createChildChargingSchemes())

                                                                     )
                                                                     .withAttributeValue(PricingSheetConstants.ACCESS_TECHNOLOGY, "Leased Line")
                                                                     .withAttributeValue(PricingSheetConstants.INTERFACE_TYPE, "100Base-T")
                                                                     .withAttributeValue(ProductOffering.APE_FLAG, "Y")
                                                                     .withPricingStatus(PricingStatus.FIRM)
                                                                     .build();
        accessProduct.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "D", "123"));
        accessProduct.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "D", "123"));

        child1.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "G", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "G", "123"));
        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);
        child1.addChildProductInstance(accessProduct, RelationshipType.RelatedTo);

        return rootProductInstance;
    }

    public ProductInstance anInstallableRootProductWithAChildAndSubChild() {

        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId1")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withSiteSpecific()
                                                                                                                      .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                                      .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                                      .withProductIdentifier(new ProductIdentifier("S0308454", "Root Product Name", "1.0", "Root Display Name"))
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withRelationName("relationship")
                                                                                                                                                                     .withProductIdentifier("S0308469", "child1")
                                                                                                                                                                     .withRelationType(RelationshipType.Child)).withChargingSchemes(createParentChargingSchemes()))
                                                                           .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                           .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                           .withPricingStatus(PricingStatus.FIRM)
                                                                           .build();

        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price1", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price1", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time Cost", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental Cost", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price3", "M0302166", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price3", "M0302166", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price4", "M0302167", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price4", "M0302167", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child1ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE)
                                                                                              .withProductIdentifier(new ProductIdentifier("S0308469", "child1 product name", "1.0", "Root Display Name"))
                                                                                              .withChargingSchemes(createChildChargingSchemes())
                                                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                             .withRelationName("relationship")
                                                                                                                                             .withProductIdentifier("subChild1Scode", "subChild1")
                                                                                                                                             .withRelationType(RelationshipType.Child))
                                                                     )
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                     .withPricingStatus(PricingStatus.FIRM)
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "G", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "G", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "J", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "J", "123"));
        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        DefaultProductInstance subChild1 = DefaultProductInstanceFixture.aProductInstance()
                                                                        .withProductInstanceId("subChild1ProductInstanceId")
                                                                        .withProductOffering(ProductOfferingFixture
                                                                                                 .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                                 .withProductIdentifier(new ProductIdentifier("subChild1Scode", "subChild1 product name", "1.0"))
                                                                                                 .withChargingSchemes(createGrandChildChargingSchemes()).withAttribute(ProductOffering.PART_TYPE_IDENTIFIER)
                                                                        ).withAttributeValue(ProductOffering.PART_TYPE_IDENTIFIER, "SW Licence")
                                                                        .build();

        subChild1.addPriceLine(aPriceLine("SubChild1 One time price1", "M0302300", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "H", "123"));
        subChild1.addPriceLine(aPriceLine("SubChild1 Rental price1", "M0302300", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "H", "123"));
        subChild1.addPriceLine(aPriceLine("SubChild1 One time price2", "M0302301", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "I", "123"));
        subChild1.addPriceLine(aPriceLine("SubChild1 Rental price2", "M0302301", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "I", "123"));
        child1.addChildProductInstance(subChild1, RelationshipType.Child);

        return rootProductInstance;
    }

    public ProductInstance anInstallableRootProductWithAChildWithARelatedTo() {

        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId1")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withSiteSpecific()
                                                                                                                      .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                                      .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                                      .withProductIdentifier(new ProductIdentifier("sCode", "Internet Connect Global", "1.0", "Root Display Name"))
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withRelationName("PrimaryInternetService")
                                                                                                                                                                     .withProductIdentifier("S0308469", "child1 product name")
                                                                                                                                                                     .withRelationType(RelationshipType.Child)).withChargingSchemes(createParentChargingSchemes()))
                                                                           .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                           .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                           .withPricingStatus(PricingStatus.FIRM)
                                                                           .build();
        rootProductInstance = addInternetConnectTestDataPricelines(rootProductInstance);

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child1ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE)
                                                                                              .withProductIdentifier(new ProductIdentifier("S0308469", "child1 product name", "1.0", "Root Display Name"))
                                                                                              .withChargingSchemes(createChildChargingSchemes()))
                                                                     .withAttributeValue(PricingSheetConstants.PORT_SPEED, "512Kbps")
                                                                     .withPricingStatus(PricingStatus.FIRM)
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 30.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 30.00, "F", "123"));
        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);


        final SalesRelationshipFixture relatedToSalesRelationshipFixture = SalesRelationshipFixture.aSalesRelationship()
                                                                                                   .withRelationName("PrimaryInternetService")
                                                                                                   .withRelationType(RelationshipType.RelatedTo)
                                                                                                   .withProductIdentifier("Access Circuit");

        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                              .withAttribute(PricingSheetConstants.ACCESS_SPEED)
                                                                              .withAttribute(PricingSheetConstants.ACCESS_TYPE)
                                                                              .withAttribute(PricingSheetConstants.SUPPLIER_SLA)
                                                                              .withAttribute(PricingSheetConstants.INTERFACE)
                                                                              .withAttribute(ProductOffering.APE_FLAG)
            .withSimpleProductOfferingType(SimpleProductOfferingType.Bearer)
                                                                              .withProductIdentifier(new ProductIdentifier("S0308469", "Access Circuit", "1.0", "Root Display Name"))
                                                                              .withChargingSchemes(createChildChargingSchemes())
                                                                              .withSalesRelationship(relatedToSalesRelationshipFixture);

        DefaultProductInstance relatedTo = DefaultProductInstanceFixture.anAccessCircuitProduct()
                                                                        .withProductIdentifier("Access Circuit", "1.0")
                                                                        .withProductInstanceId("relatedToProductInstanceId")
                                                                        .withProductOffering(productOfferingFixture)
                                                                        .withAttributeValue(PricingSheetConstants.ACCESS_SPEED, "HVPN-DSL")
                                                                        .withAttributeValue(PricingSheetConstants.ACCESS_TYPE, "ACCESS TYPE")
                                                                        .withAttributeValue(PricingSheetConstants.SUPPLIER_SLA, "SUPPLIER 1")
                                                                        .withAttributeValue(PricingSheetConstants.INTERFACE, "Interface")
                                                                        .withAttributeValue(ProductOffering.APE_FLAG, "true")
                                                                        .withPricingStatus(PricingStatus.FIRM)
                                                                        .build();

        relatedTo.addPriceLine(aPriceLine("RelatedTo One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 30.00, "F", "123"));
        relatedTo.addPriceLine(aPriceLine("RelatedTo Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 30.00, "F", "123"));
        child1.addRelationship(new ProductSalesRelationshipInstance(relatedToSalesRelationshipFixture.build(), relatedTo));


        return rootProductInstance;
    }

    private ProductInstance addInternetConnectTestDataPricelines(ProductInstance productInstance) {
        productInstance.addPriceLine(aPriceLine("Root Product One time price1", "M0304296", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 40.00, "A", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price1", "M0304296", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 40.00, "A", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time Cost", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 20.00, "B", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental Cost", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 20.00, "B", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product One time price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 20.00, "E", "123"));
        productInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 20.00, "E", "123"));
        return productInstance;
    }

    public ProductInstance anInstallableRootProductWithARelatedToAndAChild() {

        Behaviour visibleInSummaryBehaviour = BehaviourFixture.aBehaviour().withVisibleInSummaryStrategy().build();
        List<Behaviour> behaviours = newArrayList(visibleInSummaryBehaviour);

        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceICrId1")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withSiteSpecific()
                                                                                                                      .withProductIdentifier(new ProductIdentifier("sCode", "Internet Connect Reach", "1.0", "Root Display Name"))
                                                                                                                      .withChargingSchemes(createParentChargingSchemes())
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withRelationName("ProactiveMonitoring")
                                                                                                                                                                     .withProductIdentifier("sCode", "ProactiveMonitoring")
                                                                                                                                                                     .withRelationType(RelationshipType.Child))
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withRelationName("AccessCircuit")
                                                                                                                                                                     .withProductIdentifier("sCode", "AccessCircuit")
                                                                                                                                                                     .withRelationType(RelationshipType.RelatedTo))
                                                                                                                      .withChargingSchemes(createParentChargingSchemes())
                                                                                                                      .withAttribute("Attribute3", behaviours, null)
                                                                                                                      .withAttribute("Attribute4", behaviours, null))
                                                                           .withAttributeValue("Attribute3", "10 Mbps")
                                                                           .withAttributeValue("Attribute4", "Yes")
                                                                           .withPricingStatus(PricingStatus.FIRM)
                                                                           .build();
        rootProductInstance = addInternetConnectTestDataPricelines(rootProductInstance);

        ProductOfferingFixture productOfferingFixture = ProductOfferingFixture.aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                              .withAttribute(PricingSheetConstants.ACCESS_SPEED)
                                                                              .withAttribute(PricingSheetConstants.ACCESS_TYPE)
                                                                              .withAttribute(PricingSheetConstants.SUPPLIER_SLA)
                                                                              .withAttribute(PricingSheetConstants.INTERFACE)
                                                                              .withAttribute(ProductOffering.APE_FLAG)
                                                                              .withSimpleProductOfferingType(SimpleProductOfferingType.Bearer)
                                                                              .withProductIdentifier(new ProductIdentifier("sCode", "Access Circuit", "1.0", "Root Display Name"))
                                                                              .withChargingSchemes(createChildChargingSchemes());

        DefaultProductInstance relatedTo = DefaultProductInstanceFixture.anAccessCircuitProduct()
                                                                        .withProductIdentifier("test", "1.0")
                                                                        .withProductInstanceId("relatedToProductInstanceId")
                                                                        .withProductOffering(productOfferingFixture)
                                                                        .withAttributeValue(PricingSheetConstants.ACCESS_SPEED, "HVPN")
                                                                        .withAttributeValue(PricingSheetConstants.ACCESS_TYPE, "ACCESS TYPE")
                                                                        .withAttributeValue(PricingSheetConstants.SUPPLIER_SLA, "SUPPLIER 1")
                                                                        .withAttributeValue(PricingSheetConstants.INTERFACE, "INTERFACE")
                                                                        .withAttributeValue(ProductOffering.APE_FLAG, "true")
                                                                        .withPricingStatus(PricingStatus.FIRM)
                                                                        .build();

        relatedTo.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 40.00, "F", "123"));
        relatedTo.addPriceLine(aPriceLine("Child1 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 40.00, "F", "123"));
        final SalesRelationshipFixture relatedToSalesRelationshipFixture = SalesRelationshipFixture.aSalesRelationship()
                                                                                                   .withRelationName("AccessCircuit")
                                                                                                   .withRelationType(RelationshipType.RelatedTo)
                                                                                                   .withProductIdentifier("Access Circuit");

        rootProductInstance.addRelationship(new ProductSalesRelationshipInstance(relatedToSalesRelationshipFixture.build(), relatedTo));

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("ProactiveMonitoring")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withAttribute("PROACTIVE MONITORING TYPE")
                                                                                              .withProductIdentifier(new ProductIdentifier("sCode", "Proactive Monitoring", "1.0", "Root Display Name"))
                                                                                              .withChargingSchemes(createChildChargingSchemes())
                                                                     )
                                                                     .withAttributeValue("PROACTIVE MONITORING TYPE", "TEST VALUE")
                                                                     .withPricingStatus(PricingStatus.FIRM)
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 30.00, "F", "123"));
        child1.addPriceLine(aPriceLine("Child1 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 30.00, "F", "123"));
        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        return rootProductInstance;
    }

    public ProductInstance anInstallableRootProductWithTwoSteelHeads() {

        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId1")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withSiteSpecific()
                                                                                                                      .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                                      .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE)
                                                                                                                      .withProductIdentifier(new ProductIdentifier(ProductCodes.ConnectAccelerationSite.productCode(), "Root Product Name", "1.0"))
                                                                                                                      .withChargingSchemes(createParentChargingSchemes())
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withProductIdentifier(ProductCodes.ConnectAccelerationSteelhead.productCode(), "Steelhead child1")
                                                                                                                                                                     .withRelationType(RelationshipType.Child)).withChargingSchemes(createChildChargingSchemes())
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withProductIdentifier(ProductCodes.ConnectAccelerationSteelhead.productCode(), "Steelhead child2")
                                                                                                                                                                     .withRelationType(RelationshipType.Child)).withChargingSchemes(createChildChargingSchemes()))
                                                                           .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                           .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                           .build();

        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price1", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price1", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "A", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time Cost", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental Cost", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 333.00, "B", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Aggregated One time price", "M0302166", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Aggregated Rental price", "M0302166", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "C", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price4", "M0302167", 100.00, PriceType.ONE_TIME, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price4", "M0302167", 100.00, PriceType.RECURRING, PriceCategory.END_USER_PRICE.getLabel(), 333.00, "D", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product One time price5", "M0302168", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));
        rootProductInstance.addPriceLine(aPriceLine("Root Product Rental price5", "M0302168", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "E", "123"));


        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child1ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE)
                                                                                              .withProductIdentifier(new ProductIdentifier(ProductCodes.ConnectAccelerationSteelhead.productCode(), "Steelhead child1", "1.0"))
                                                                                              .withChargingSchemes(createChildChargingSchemes())

                                                                     )
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child1 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "F", "4"));
        child1.addPriceLine(aPriceLine("Child1 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "F", "4"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "G", "4"));
        child1.addPriceLine(aPriceLine("Child1 Rental price2", "M0302270", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "G", "4"));
        child1.addPriceLine(aPriceLine("Total SteelHead Price", "M0302170", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "J", "4"));
        child1.addPriceLine(aPriceLine("Total SteelHead Price", "M0302170", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "J", "4"));

        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        DefaultProductInstance child2 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child2ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                              .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE)
                                                                                              .withProductIdentifier(new ProductIdentifier(ProductCodes.ConnectAccelerationSteelhead.productCode(), "Steelhead child2", "1.0"))
                                                                                              .withChargingSchemes(createChildChargingSchemes())
                                                                     )
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                     .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                     .build();

        child2.addPriceLine(aPriceLine("Child2 One time price1", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "F", "5"));
        child2.addPriceLine(aPriceLine("Child2 Rental price1", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "F", "5"));
        child2.addPriceLine(aPriceLine("Child2 Rental price2", "M0302270", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "G", "5"));
        child2.addPriceLine(aPriceLine("Child2 Rental price2", "M0302270", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "G", "5"));
        child2.addPriceLine(aPriceLine("Total SteelHead Price", "M0302170", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "J", "5"));
        child2.addPriceLine(aPriceLine("Total SteelHead Price", "M0302170", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "J", "5"));

        rootProductInstance.addChildProductInstance(child2, RelationshipType.Child);

        return rootProductInstance;
    }

    public ProductInstance aSiteAgnosticProductInstance() {
        Behaviour visibleInSummaryBehaviour = BehaviourFixture.aBehaviour().withVisibleInSummaryStrategy().build();
        List<Behaviour> behaviours = newArrayList(visibleInSummaryBehaviour);

        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId2")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                               .withFamilyName("Internet Connect Reach")
                                                                                                                      .withProductIdentifier(new ProductIdentifier("rootProductScode2", "Root Product Name2", "1.0"))
                                                                                                                      .withAttribute("Attribute 2", behaviours, null)
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withProductIdentifier("child2Scode", "child2")
                                                                                                                                                                     .withRelationType(RelationshipType.Child))
                                                                                                                      .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer)))
                                                                           .withAttributeValue("Attribute 2", "value")
                                                                           .build();


        PriceLine oneTimePriceLine = aPriceLine("Root Product2 One time price", "M0302168", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "123");
        PriceLine recurringPrice = aPriceLine("Root Product2 Rental price", "M0302168", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "A", "123");
        rootProductInstance.addPriceLine(oneTimePriceLine);
        rootProductInstance.addPriceLine(recurringPrice);

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child2ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withProductIdentifier(new ProductIdentifier("child2Scode", "child2 product name", "1.0"))
                                                                                              .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer))
                                                                     )
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child2 One time price", "M0302169", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "123"));
        child1.addPriceLine(aPriceLine("Child2 Rental price", "M0302169", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "A", "123"));


        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        return rootProductInstance;

    }

    public ProductInstance aCAServiceProductInstance() {
        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId2")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withProductIdentifier(new ProductIdentifier("S0308491", "Connect Acceleration Service", "1.0"))
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withProductIdentifier("child2Scode", "child2")
                                                                                                                                                                     .withRelationType(RelationshipType.Child))
                                                                                                                      .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer)))
                                                                           .build();


        PriceLine oneTimePriceLine = aPriceLine("Root Product2 One time price", "M0302168", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "123");
        PriceLine recurringPrice = aPriceLine("Root Product2 Rental price", "M0302168", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "A", "123");
        rootProductInstance.addPriceLine(oneTimePriceLine);
        rootProductInstance.addPriceLine(recurringPrice);

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child2ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withProductIdentifier(new ProductIdentifier("child2Scode", "child2 product name", "1.0"))
                                                                                              .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer))
                                                                     )
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child2 One time price", "M0302169", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "123"));
        child1.addPriceLine(aPriceLine("Child2 Rental price", "M0302169", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "A", "123"));


        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        return rootProductInstance;

    }

    public ProductInstance aCAServiceProductInstanceWithOutPriceLines() {
        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId2")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withProductIdentifier(new ProductIdentifier("S0308491", "Connect Acceleration Service", "1.0"))
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withProductIdentifier("child2Scode", "child2")
                                                                                                                                                                     .withRelationType(RelationshipType.Child))
                                                                                                                      .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer)))
                                                                           .build();

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child2ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withProductIdentifier(new ProductIdentifier("child2Scode", "child2 product name", "1.0"))
                                                                     )
                                                                     .build();

        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);
        return rootProductInstance;
    }

    public ProductInstance aSiteAgnosticProductInstance1() {
        Behaviour visibleInSummaryBehaviour = BehaviourFixture.aBehaviour().withVisibleInSummaryStrategy().build();
        List<Behaviour> behaviours = newArrayList(visibleInSummaryBehaviour);
        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId2")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withProductIdentifier(new ProductIdentifier("rootProductScode3", "Root Product Name3", "1.0", "Root Product Name3"))
                                                                                                                      .withAttribute("Attribute3", behaviours, null)
                                                                                                                      .withAttribute("Attribute4", behaviours, null)
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withProductIdentifier("child3Scode", "child3")
                                                                                                                                                                     .withRelationType(RelationshipType.Child))
                                                                                                                      .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer)))
                                                                           .withAttributeValue("Attribute3", "10 Mbps")
                                                                           .withAttributeValue("Attribute4", "Yes")
                                                                           .build();


        PriceLine oneTimePriceLine = aPriceLine("Root Product3 One time price", "M0302168", 200.00, PriceType.ONE_TIME, "Recommended Retail Price", 444.00, "A", "123");
        PriceLine recurringPrice = aPriceLine("Root Product3 Rental price", "M0302168", 200.00, PriceType.RECURRING, "Recommended Retail Price", 444.00, "A", "123");
        rootProductInstance.addPriceLine(oneTimePriceLine);
        rootProductInstance.addPriceLine(recurringPrice);

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child3ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withProductIdentifier(new ProductIdentifier("child3Scode", "child3 product name", "1.0"))
                                                                                              .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer))
                                                                     )
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child2 One time price", "M0302169", 200.00, PriceType.ONE_TIME, "Recommended Retail Price", 444.00, "A", "123"));
        child1.addPriceLine(aPriceLine("Child2 Rental price", "M0302169", 200.00, PriceType.RECURRING, "Recommended Retail Price", 444.00, "A", "123"));


        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        return rootProductInstance;

    }

    public ProductInstance aSiteAgnosticProductInstanceWithGrandChild() {
        Behaviour visibleInSummaryBehaviour = BehaviourFixture.aBehaviour().withVisibleInSummaryStrategy().build();
        List<Behaviour> behaviours = newArrayList(visibleInSummaryBehaviour);
        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withProductIdentifier(new ProductIdentifier("rootProductScode1", "Root Product Name1", "1.0", "Root Product Name3"))
                                                                                                                      .withAttribute("Attribute3", behaviours, null)
                                                                                                                      .withAttribute("Attribute4", behaviours, null)
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withProductIdentifier("childScode", "child")
                                                                                                                                                                     .withRelationType(RelationshipType.Child))
                                                                                                                      .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer)))
                                                                           .withAttributeValue("Attribute3", "10 Mbps")
                                                                           .withAttributeValue("Attribute4", "Yes")
                                                                           .build();


        PriceLine oneTimePriceLine = aPriceLine("Root Product3 One time price", "M0302168", 200.00, PriceType.ONE_TIME, "Recommended Retail Price", 444.00, "A", "123");
        PriceLine recurringPrice = aPriceLine("Root Product3 Rental price", "M0302168", 200.00, PriceType.RECURRING, "Recommended Retail Price", 444.00, "A", "123");
        rootProductInstance.addPriceLine(oneTimePriceLine);
        rootProductInstance.addPriceLine(recurringPrice);

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("childProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withProductIdentifier(new ProductIdentifier("childScode", "child product name", "1.0"))
                                                                                              .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                             .withProductIdentifier("grandChildScode", "grandChild")
                                                                                                                                             .withRelationType(RelationshipType.Child))
                                                                                              .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer))
                                                                     )
                                                                     .build();

        child1.addPriceLine(aPriceLine("Child One time price", "M0302169", 200.00, PriceType.ONE_TIME, "Recommended Retail Price", 444.00, "A", "123"));
        child1.addPriceLine(aPriceLine("Child Rental price", "M0302169", 200.00, PriceType.RECURRING, "Recommended Retail Price", 444.00, "A", "123"));


        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        DefaultProductInstance grandChild = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("grandChildProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withProductIdentifier(new ProductIdentifier("grandChildScode", "grandChild", "1.0"))
                                                                                              .withChargingScheme(createChargingScheme("A", PricingStrategy.ManagedItem, ProductChargingScheme.PriceVisibility.Sales))
                                                                     )
                                                                     .build();

        grandChild.addPriceLine(aPriceLine("grand child One time price", "M0302169", 200.00, PriceType.ONE_TIME, "Recommended Retail Price", 444.00, "A", "123"));
        grandChild.addPriceLine(aPriceLine("grand child Rental price", "M0302169", 200.00, PriceType.RECURRING, "Recommended Retail Price", 444.00, "A", "123"));


        child1.addChildProductInstance(grandChild, RelationshipType.Child);

        return rootProductInstance;

    }

    private ProductInstance otherInstallableRootProductWithAChild() {
        ProductInstance rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                           .withProductInstanceId("rootProductInstanceId3")
                                                                           .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                      .withSiteSpecific()
                                                                                                                      .withProductIdentifier(new ProductIdentifier("rootProduct3Scode", "Root Product Name3", "1.0", "Root Product Name3"))
                                                                                                                      .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                     .withProductIdentifier("child3Scode", "child3")
                                                                                                                                                                     .withRelationType(RelationshipType.Child))
                                                                                                                      .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer)))
                                                                           .withPricingStatus(PricingStatus.FIRM)
                                                                           .build();

        PriceLine oneTimePriceLine = aPriceLine("Root Product3 One time price", "M0302170", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "123");
        PriceLine recurringPrice = aPriceLine("Root Product3 Rental price", "M0302170", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "A", "123");
        rootProductInstance.addPriceLine(oneTimePriceLine);
        rootProductInstance.addPriceLine(recurringPrice);

        DefaultProductInstance child1 = DefaultProductInstanceFixture.aProductInstance()
                                                                     .withProductInstanceId("child3ProductInstanceId")
                                                                     .withProductOffering(ProductOfferingFixture
                                                                                              .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                              .withProductIdentifier(new ProductIdentifier("child3Scode", "child3 product name", "1.0"))
                                                                                              .withChargingScheme(createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer))
                                                                     )
                                                                     .withPricingStatus(PricingStatus.FIRM)
                                                                     .build();

        rootProductInstance.addChildProductInstance(child1, RelationshipType.Child);

        return rootProductInstance;
    }

    private ProductInstance aContractProductInstance() {
        ProductInstance contractProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                               .withProductInstanceId("contractProductInstanceId")
                                                                               .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                          .withSimpleProductOfferingType(SimpleProductOfferingType.Contract)
                                                                                                                          .withAttribute(ProductOffering.PLAN_NAME)
                                                                                                                          .withAttribute(ProductOffering.CALL_COMMITMENT)
                                                                                                                          .withProductIdentifier(new ProductIdentifier("aContractSCode", "Contract Product Name", "1.0", "Contract Display Name"))
                                                                                                                          .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                         .withRelationName("relationship")
                                                                                                                                                                         .withProductIdentifier("aContractChildSCode", "contractChild")
                                                                                                                                                                         .withRelationType(RelationshipType.Child)).withChargingSchemes(createParentChargingSchemes()))
                                                                               .withAttributeValue(ProductOffering.PLAN_NAME, "aPlanName")
                                                                               .withAttributeValue(ProductOffering.CALL_COMMITMENT, "aCallCommitment")
                                                                               .withPricingStatus(PricingStatus.FIRM)
                                                                               .build();

        DefaultProductInstance contractProductChild = DefaultProductInstanceFixture.aProductInstance()
                                                                                   .withProductInstanceId("contractChildProductInstanceId")
                                                                                   .withProductOffering(ProductOfferingFixture
                                                                                                            .aProductOffering()
                                                                                                            .withAttribute(ProductOffering.QUANTITY)
                                                                                                            .withProductIdentifier(new ProductIdentifier("aContractChildSCode", "contractChild", "1.0", "contractChild"))
                                                                                                            .withChargingSchemes(createChildChargingSchemes()))
                                                                                   .withAttributeValue(ProductOffering.QUANTITY, "2")
                                                                                   .withPricingStatus(PricingStatus.FIRM)
                                                                                   .build();

        contractProductChild.addPriceLine(aPriceLine("Contract Child Price", "M0302269", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "F", "123"));
        contractProductChild.addPriceLine(aPriceLine("Contract Child Price", "M0302269", 100.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "F", "123"));
        contractProductInstance.addChildProductInstance(contractProductChild, RelationshipType.Child);

        return contractProductInstance;
    }

    public CustomerDTO aCustomerDTO() {
        return new CustomerDTO("id", "name", "sales channel");
    }

    public ProjectDTO aProjectDto() {
        return new ProjectDTO("projectId",
                              "customerId",
                              "customerName",
                              "quoteVersion",
                              "orderType",
                              new BFGOrganisationDetails("salesOrganization"),
                              "quoteStatus",
                              "quoteName",
                              "currency",
                              "salesRepName",
                              "salesRepLoginId",
                              "expRef",
                              "contractId",
                              "contractTerm",
                              "orderId",
                              "bidNumber",
                              "siebelId",
                              "tradeLevel",
                              "someOrderStatus",
                              "612345678",
                              "No",
                              null,
                              "",
                              null,
                              null,
                              "Firm",
                              "subOrderType");
    }

    public AccountManagerDTO anAccountManager() {
        return new AccountManagerDTO("customerId", "firstName", "lastName", "phoneNumber", "faxNumber", "email", "aRole", "123");
    }

    public QuoteOptionDTO aQuoteOptionDto() {
        return QuoteOptionDTO.newInstance("quoteOptionId", "friendlyQuoteId", "name", "currency", "60", "createdBy", null);
    }

    public SiteDTO aCentralSite() {
        return aSiteDTO()
            .withName("central Site Name")
            .withBfgSiteId("aCentralSiteBfgId")
            .withFloor("2")
            .withBuilding("central site building")
            .withCity("central site city")
            .withCountry("central site country")
            .withPostCode("central site postcode")
            .withSubBuilding("a Sub Building")
            .withBuildingNumber("100")
            .withSubStreet("aCentralSubStreet")
            .withStreet("aCentralStreet")
            .withSubLocality("aCentralSubLocality")
            .withLocality("aCentralLocality")
            .withSubStateCountyProvince("aCentralSubCountyProvince")
            .withStateCountyProvince("aCentralCountyProvince")
            .withPostBox("aCentralPostBox")
            .withRoom("someCentralRoom")
            .build();
    }

    public SiteDTO aSite() {
        return aSiteDTO()
            .withName("Site Name")
            .withBfgSiteId("aBfgSiteId")
            .withFloor("3")
            .withBuilding("some building")
            .withCity("city")
            .withBuilding("building")
            .withCountry("country")
            .withPostCode("postcode")
            .withSubBuilding("subBuilding")
            .withBuildingNumber("200")
            .withSubStreet("subStreet")
            .withStreet("aStreet")
            .withSubLocality("aSubLocality")
            .withLocality("aLocality")
            .withSubStateCountyProvince("aSubCountyProvince")
            .withStateCountyProvince("aCountyProvince")
            .withPostBox("aPostBox")
            .withRoom("aRoom")
            .build();
    }

    public PriceLine aPriceLine(String priceLineName, String pmfId, double price, PriceType priceType, String tariff, double chargePrice, String chargingSchemeName, String priceLineId) {
        PriceLineFixture priceLineOTFixture = new PriceLineFixture();
        priceLineOTFixture.withEupPrice(price);
        priceLineOTFixture.withChargePrice(chargePrice);
        priceLineOTFixture.withPriceLineName(priceLineName);
        priceLineOTFixture.withPmfId(pmfId);
        priceLineOTFixture.withPriceType(priceType);
        priceLineOTFixture.withTariff(tariff);
        priceLineOTFixture.withChargingSchemeName(chargingSchemeName);
        priceLineOTFixture.withId(priceLineId);
        priceLineOTFixture.withChargePriceDiscountPct(new BigDecimal(0.0));
        return priceLineOTFixture.build();
    }

    private List<ProductChargingScheme> createParentChargingSchemes() {
        return newArrayList(
            createChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer),
            createChargingScheme("B", PricingStrategy.ManagedItem, ProductChargingScheme.PriceVisibility.Customer),
            createChargingScheme("C", PricingStrategy.Aggregation, ProductChargingScheme.PriceVisibility.Sales),
            createChargingScheme("D", PricingStrategy.PricingEngine, ProductChargingScheme.PriceVisibility.Hidden),
            createChargingScheme("E", PricingStrategy.PricingEngine, ProductChargingScheme.PriceVisibility.Hidden)
        );
    }

    private List<ProductChargingScheme> createChildChargingSchemes() {
        return newArrayList(
            createChargingScheme("F", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer),
            createChargingScheme("G", PricingStrategy.ManagedItem, ProductChargingScheme.PriceVisibility.Sales),
            createChargingScheme("J", PricingStrategy.Aggregation, ProductChargingScheme.PriceVisibility.Sales)
        );
    }

    private List<ProductChargingScheme> createGrandChildChargingSchemes() {
        return newArrayList(
            createChargingScheme("H", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Customer),
            createChargingScheme("I", PricingStrategy.ManagedItem, ProductChargingScheme.PriceVisibility.Hidden)
        );
    }

    private ProductChargingScheme createChargingScheme(String name, PricingStrategy pricingStrategy, ProductChargingScheme.PriceVisibility visibility) {
        return new ProductChargingScheme(name, pricingStrategy, "", visibility, "", new ArrayList<BillingTariffRuleSet>(), null);
    }

    private Behaviour createRFQBehaviour() {
        return BehaviourFixture.aRFQBehaviour().build();
    }

    public DefaultProductInstanceFixture aSpecialBidProduct() {
        PriceLine priceline = aPriceLine("Special Bid Price Line", "M0001230", 120, PriceType.ONE_TIME, "Non-Recurring", 20, "Special Bid Price line", "34123");
        PriceLine priceline2 = aPriceLine("Special Bid Price Line", "M0001230", 120, PriceType.RECURRING, "Recurring", 20, "Special Bid Price line", "34124");

        List<PriceLine> pricelines = newArrayList(priceline, priceline2);
        return DefaultProductInstanceFixture.aProductInstance()
                                            .withProductInstanceId("specialBid")
                                            .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                       .forSpecialBid()
                                                                                       .withProductIdentifier(new ProductIdentifier("test", "test", "test"))
                                                                                       .withChargingScheme(createChargingScheme("Special Bid Price line", PricingStrategy.SpecialBid, ProductChargingScheme.PriceVisibility.Customer)))
                                            .withPricingStatus(PricingStatus.FIRM)
                                            .withPriceLines(pricelines)
                                            .withLineItemId("aLineItemId").withProductInstanceId("productInstanceId")
                                            .withAttributeValue("TPE TEMPLATE NAME", "some name")
                                            .withAttributeValue("SPECIAL BID ID", "some Id")
                                            .withAttributeValue("BILL DESCRIPTION", "Bill Desc value");
    }

    public DefaultProductInstanceFixture aSpecialBidProductWithSiteInstallable(Boolean isSiteInstallable) {
        PriceLine priceline = aPriceLine("Special Bid Price Line", "M0001230", 120, PriceType.ONE_TIME, "Non-Recurring", 20, "Special Bid Price line", "34123");
        PriceLine priceline2 = aPriceLine("Special Bid Price Line", "M0001230", 120, PriceType.RECURRING, "Recurring", 20, "Special Bid Price line", "34124");

        List<PriceLine> pricelines = newArrayList(priceline, priceline2);

        if(isSiteInstallable) {
            return DefaultProductInstanceFixture.aSpecialBidProduct()
                                                .withProductInstanceId("specialBid")
                                                .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                           .forSpecialBid()
                                                                                           .withProductIdentifier(new ProductIdentifier("test", "test", "test"))
                                                                                           .withChargingScheme(createChargingScheme("Special Bid Price line", PricingStrategy.SpecialBid, ProductChargingScheme.PriceVisibility.Customer)))
                                                .withPricingStatus(PricingStatus.FIRM)
                                                .withPriceLines(pricelines)
                                                .withLineItemId("aLineItemId").withProductInstanceId("productInstanceId")
                                                .withAttributeValue("TPE TEMPLATE NAME", "some name")
                                                .withAttributeValue("SPECIAL BID ID", "some Id")
                                                .withAttributeValue("BILL DESCRIPTION", "Bill Desc value");
        }else {
            return DefaultProductInstanceFixture.aSpecialBidProduct()
                                                .withProductInstanceId("specialBid")
                                                .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                           .forSpecialBid()
                                                                                           .withProductIdentifier(new ProductIdentifier("test", "test", "test"))
                                                                                           .withChargingScheme(createChargingScheme("Special Bid Price line", PricingStrategy.SpecialBid, ProductChargingScheme.PriceVisibility.Customer)))
                                                .withPricingStatus(PricingStatus.FIRM)
                                                .withPriceLines(pricelines)
                                                .withLineItemId("aLineItemId").withProductInstanceId("productInstanceId")
                                                .withAttributeValue("TPE TEMPLATE NAME", "some name")
                                                .withAttributeValue("SPECIAL BID ID", "some Id")
                                                .withAttributeValue("BILL DESCRIPTION", "Bill Desc value");
        }



    }

    public PricingSheetDataModel pricingSheetTestDataBothSpecialBidAndStandard() {
        PricingSheetSpecialBidProduct pricingSheetNonStandardProductModel1 = aPricingSheetSpecialBidProductModel();
        PricingSheetSpecialBidProduct pricingSheetNonStandardProductModel2 = aPricingSheetSpecialBidProductModel();
        PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetProductModelWithAChild();
        PricingSheetSpecialBidProduct pricingSheetProductModel2 = aPricingSheetSpecialBidProductModelWithAChild();
        PricingSheetProductModel pricingSheetProductModel3 = aPrincgSheetProductModelWithNoAttributes();
        PricingSheetProductModel pricingSheetProductModel4 = aSiteAgnosticPricingSheetProductModel1();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetSpecialBidProduct(pricingSheetNonStandardProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetNonStandardProductModel2)
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel2)
            .withPricingSheetProductModel(pricingSheetProductModel3)
            .withPricingSheetProductModel(pricingSheetProductModel4)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aSite())
            .build();
    }

    public DefaultProductInstanceFixture aSpecialBidProductwithSiteSpecific() {
        PriceLine priceline = aPriceLine("Special Bid Price Line", "M0001230", 120, PriceType.ONE_TIME, "Non-Recurring", 20, "Special Bid Price line", "34123");
        PriceLine priceline2 = aPriceLine("Special Bid Price Line", "M0001230", 120, PriceType.RECURRING, "Recurring", 20, "Special Bid Price line", "34124");

        List<PriceLine> pricelines = newArrayList(priceline, priceline2);
        return DefaultProductInstanceFixture.aSpecialBidProduct()
                                            .withProductInstanceId("specialBid")
                                            .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                       .forSpecialBid()
                                                                                       .withSiteSpecific()
                                                                                       .withProductIdentifier(new ProductIdentifier("test", "test", "test"))
                                                                                       .withChargingScheme(createChargingScheme("Special Bid Price line", PricingStrategy.Aggregation, ProductChargingScheme.PriceVisibility.Customer)))

                                            .withPricingStatus(PricingStatus.FIRM)
                                            .withPriceLines(pricelines)
                                            .withLineItemId("aLineItemId").withProductInstanceId("productInstanceId")
                                            .withAttributeValue("TPE TEMPLATE NAME", "some name")
                                            .withAttributeValue("SPECIAL BID ID", "some Id")
                                            .withAttributeValue("BILL DESCRIPTION", "Bill Desc value");
    }

    public PricingSheetDataModel pricingSheetTestDataBothSpecialBidAndStandardForModifyJourney() {
        PricingSheetSpecialBidProduct pricingSheetNonStandardProductModel1 = aPricingSheetSpecialBidProductModelForModifyJourney();
        PricingSheetSpecialBidProduct pricingSheetNonStandardProductModel2 = aPricingSheetSpecialBidProductModelForModifyJourney();
        PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetProductModelWithAChildForModifyJourney();
        PricingSheetSpecialBidProduct pricingSheetProductModel2 = aPricingSheetSpecialBidProductModelForModifyJourney();
        PricingSheetProductModel pricingSheetProductModel3 = aPricingSheetProductModelWithNoAttributesForModifyJourney();
        PricingSheetProductModel pricingSheetProductModel4 = aSiteAgnosticPricingSheetProductModelForModifyJourney();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetSpecialBidProduct(pricingSheetNonStandardProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetNonStandardProductModel2)
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel2)
            .withPricingSheetProductModel(pricingSheetProductModel3)
            .withPricingSheetProductModel(pricingSheetProductModel4)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aSite())
            .build();
    }

    public PricingSheetDataModel pricingSheetTestDataBothSpecialBidAndStandardForModifyJourneyWithSite() {
        PricingSheetSpecialBidProduct pricingSheetNonStandardProductModel1 = aPricingSheetSpecialBidProductModelForModifyJourneywithSite();
        PricingSheetSpecialBidProduct pricingSheetNonStandardProductModel2 = aPricingSheetSpecialBidProductModelForModifyJourneywithSite();
        PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetProductModelWithAChildForModifyJourney();
        PricingSheetSpecialBidProduct pricingSheetProductModel2 = aPricingSheetSpecialBidProductModelForModifyJourney();
        PricingSheetProductModel pricingSheetProductModel3 = aPricingSheetProductModelWithNoAttributesForModifyJourney();
        PricingSheetProductModel pricingSheetProductModel4 = aSiteAgnosticPricingSheetProductModelForModifyJourney();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetSpecialBidProduct(pricingSheetNonStandardProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetNonStandardProductModel2)
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withPricingSheetSpecialBidProduct(pricingSheetProductModel2)
            .withPricingSheetProductModel(pricingSheetProductModel3)
            .withPricingSheetProductModel(pricingSheetProductModel4)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aSite())
            .build();
    }

    private PricingSheetSpecialBidProduct aPricingSheetSpecialBidProductModelWithAChild() {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("CONFIGURATION_CATEGORY", "Category");
        attributes.put("CAVEATS", "Description of caveats");
        final ProductInstance productInstance = anInstallableRootProductWithAChild();
        productInstance.setProductInstanceId("specialBidProductInstanceId");
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetSpecialBidProduct(aSite(),
                                                 QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), attributes, mergeResult, productInstance, pricingClient, Optional.<ProductInstance>absent());
    }

    public PricingSheetDataModel pricingSheetAccessCaveatsTestData() {
        PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetAccessCaveatProductModel();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aSite())
            .withAccessCaveatProductModel(pricingSheetProductModel1)
            .build();
    }

    public PricingSheetDataModel pricingSheetPricingCaveatsTestData() {
        PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetPricingCaveatProductModel();
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withAccessCaveatProductModel(pricingSheetProductModel1)
            .withCentralSite(aSite())
            .build();
    }

    private void setExpectationsForAccessCaveats(CaveatResource caveatResource) {
        AccessCaveatDescriptionDTO accessCaveatDescriptionDTO = mock(AccessCaveatDescriptionDTO.class);
        List<AccessCaveatDescriptionDTO> accessCaveatDescriptionDTOList = newArrayList();
        accessCaveatDescriptionDTOList.add(accessCaveatDescriptionDTO);
        when(accessCaveatDescriptionDTO.getDescription()).thenReturn("description");
        when(caveatResource.getCaveatDescriptionFromId("1")).thenReturn(accessCaveatDescriptionDTOList);
        when(caveatResource.getCaveatDescriptionFromId("2")).thenReturn(accessCaveatDescriptionDTOList);
        when(caveatResource.getCaveatDescriptionFromId("3")).thenReturn(accessCaveatDescriptionDTOList);
    }

    private PricingSheetProductModel aPricingSheetAccessCaveatProductModel() {
        ProductInstance productInstance = anAccessCircuitProduct().withLineItemId("accessCaveatLineItemId").build();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        CaveatResource caveatResource = mock(CaveatResource.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        setExpectationsForAccessCaveats(caveatResource);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetProductModel(aSite(), productInstance, QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), mergeResult, caveatResource, pricingClient, Optional.<ProductInstance>absent());
    }

    public PricingSheetProductModel aPricingSheetPricingCaveatProductModel() {
        ProductInstance productInstance = anPricingCaveatsProduct().withLineItemId("accessCaveatLineItemId").build();
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        CaveatResource caveatResource = mock(CaveatResource.class);
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), changeTracker);
        setExpectationsForAdd(changeTracker);
        setExpectationsForAccessCaveats(caveatResource);
        PricingClient pricingClient = mock(PricingClient.class);
        setExpectationsForPricingClientIFC(pricingClient);
        return new PricingSheetProductModel(aSite(), productInstance, QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), mergeResult, caveatResource, pricingClient, Optional.<ProductInstance>absent());
    }

    public DefaultProductInstanceFixture anAccessCircuitProduct() {
        PriceLine priceline = aPriceLine("Access Circuit Price Line", "M0001230", 120, PriceType.ONE_TIME, "Non-Recurring", 100, "Access Circuit Price line", "34123");
        PriceLine priceline2 = aPriceLine("Access Circuit Price Line", "M0001230", 120, PriceType.RECURRING, "Recurring", 100, "Access Circuit Price line", "34124");

        List<PriceLine> pricelines = newArrayList(priceline, priceline2);
        return DefaultProductInstanceFixture.anAccessCircuitProduct()
                                            .withProductInstanceId("accessCircuit")
                                            .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                       .forAccessCircuit()
                                                                                       .withSiteSpecific()
                                                                                       .withProductIdentifier(new ProductIdentifier("test", "test", "test"))
                                                                                       .withChargingScheme(createChargingScheme("Access Circuit Price line", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Sales)))
                                            .withPricingStatus(PricingStatus.FIRM)


                                            .withPriceLines(pricelines)
                                            .withLineItemId("aLineItemId").withProductInstanceId("productInstanceId")
                                            .withAttributeValue("QREF", "qrefId")
                                            .withAttributeValue("CAVEATS", "1,2,3")
                                            .withAttributeValue(PricingSheetConstants.ACCESS_TYPE, "HVPN-DSL")
                                            .withAttributeValue(PricingSheetConstants.INTERFACE, "interface")
                                            .withAttributeValue(PricingSheetConstants.ACCESS_SPEED, "1028Kbps")
                                            .withAttributeValue(ProductOffering.LEGACY_BILLING, "Yes")
                                            .withAttributeValue(PricingSheetConstants.SUPPLIER_SLA, "Expereo");
    }

    public DefaultProductInstanceFixture anPricingCaveatsProduct() {
        PricingCaveat pricingCaveat = new PricingCaveat("assetId", 1l, "siteId", "caveatId", "caveatType", "caveatDescription", null);

        List<PricingCaveat> pricingCaveats = newArrayList(pricingCaveat);
        return DefaultProductInstanceFixture.anAccessCircuitProduct()
                                            .withProductInstanceId("accessCircuit")
                                            .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                       .forAccessCircuit()
                                                                                       .withSiteSpecific()
                                                                                       .withProductIdentifier(new ProductIdentifier("test", "test", "test"))
                                                                                       .withChargingScheme(createChargingScheme("Access Circuit Price line", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Sales)))
                                            .withPricingStatus(PricingStatus.FIRM)


                                            .withPricingCaveats(pricingCaveats)
                                            .withLineItemId("aLineItemId").withProductInstanceId("productInstanceId");

    }

    public PricingSheetDataModel pricingSheetTestDataForICG() {
                PricingSheetProductModel pricingSheetProductModel1 = aPricingSheetICGProductModel();
        Set<PriceBookDTO> priceBooks = newHashSet();
        priceBooks.add(new PriceBookDTO("id","reqId","CA1","CA1 GL","",""));
        priceBooks.add(new PriceBookDTO("id2","reqId","CI1","CI1 GL","",""));
        return aPricingSheetModel()
            .withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withPricingSheetProductModel(pricingSheetProductModel1)
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aCentralSite())
            .withPriceBooks(priceBooks)
            .build();
    }


    public ProductInstance aRootProductAndChildWithRelationshipNameProductInstance(SellableProduct sellableProduct, String cpeProductName) {
        String productName = sellableProduct.getProductName();
        ProductChargingScheme siteManagementScheme = new ProductChargingScheme(productName + " Management Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Sales, "Site Management", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme sitePriceScheme = new ProductChargingScheme(productName + " Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Customer, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadTotalPrice = new ProductChargingScheme("Total "+ cpeProductName + " Price", PricingStrategy.Aggregation, "AAcc", ProductChargingScheme.PriceVisibility.Sales, cpeProductName, new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadPrice = new ProductChargingScheme(cpeProductName+" Price", PricingStrategy.PricingEngine, cpeProductName, ProductChargingScheme.PriceVisibility.Hidden, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductChargingScheme steelHeadVendorMaintenancePrice = new ProductChargingScheme(cpeProductName + " Vendor Maintenance Price", PricingStrategy.StencilManagedItem, cpeProductName, ProductChargingScheme.PriceVisibility.Hidden, "", new ArrayList<BillingTariffRuleSet>(), null);
        ProductCodes cpeProduct = ProductCodes.getByName(cpeProductName);
        ProductInstance siteProductInstance = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId(sellableProduct.getProductId())
                                                                               .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                                                          .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                                          .withAttribute(PricingSheetConstants.NAME_ATTRIBUTE)
                                                                                                                          .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                                          .withProductIdentifier(new ProductIdentifier(sellableProduct.getProductId(), sellableProduct.getProductName(), "1.0"))
                                                                                                                          .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                         .withRelationName(cpeProductName)
                                                                                                                                                                         .withProductIdentifier(cpeProduct.productCode(), cpeProduct.productName())
                                                                                                                                                                         .withRelationType(RelationshipType.Child))
                                                                                                                          .withChargingSchemes(newArrayList(siteManagementScheme, sitePriceScheme)))
                                                                               .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                               .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                               .withAttributeValue(PricingSheetConstants.NAME_ATTRIBUTE, productName)
                                                                               .build();
        ProductCodes steelheadVendorMaintenance = ProductCodes.SteelheadVendorMaintenance;
        siteProductInstance.addPriceLine(aPriceLine(productName + " Price", "M0302164", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, productName + " Price", "1"));
        siteProductInstance.addPriceLine(aPriceLine(productName + " Price", "M0302164", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, productName + " Price", "2"));
        siteProductInstance.addPriceLine(aPriceLine(productName + " Management Price", "M0302165", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, productName + " Management Price", "3"));
        siteProductInstance.addPriceLine(aPriceLine(productName + " Management Price", "M0302165", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, productName + " Management Price", "4"));
        ProductInstance steelHead = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("cpeId")
                                                                 .withProductOffering(ProductOfferingFixture.aProductOffering().withSiteSpecific()
                                                                                                            .withAttribute(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE)
                                                                                                            .withAttribute(PricingSheetConstants.NAME_ATTRIBUTE)
                                                                                                            .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                            .withAttribute(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, newArrayList(createRFQBehaviour()), null)
                                                                                                            .withProductIdentifier(new ProductIdentifier(cpeProduct.productCode(), cpeProduct.productName(), "1.0"))
                                                                                                            .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                           .withRelationName("vendor maintenance")
                                                                                                                                                           .withProductIdentifier(steelheadVendorMaintenance.productCode(), steelheadVendorMaintenance.productName())
                                                                                                                                                           .withRelationType(RelationshipType.Child))
                                                                                                            .withChargingSchemes(newArrayList(steelHeadPrice, steelHeadTotalPrice)))
                                                                 .withAttributeValue(PricingSheetConstants.BUNDLE_NAME_ATTRIBUTE, "BUNDLE NAME VALUE")
                                                                 .withAttributeValue(PricingSheetConstants.BUNDLE_TYPE_ATTRIBUTE, "BUNDLE TYPE VALUE")
                                                                 .withAttributeValue(PricingSheetConstants.NAME_ATTRIBUTE,cpeProductName)
                                                                 .build();
        steelHead.addPriceLine(aPriceLine("Total "+cpeProductName +" Price", "M0302170", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Total "+cpeProductName +" Price", "1"));
        steelHead.addPriceLine(aPriceLine("Total "+cpeProductName +" Price", "M0302170", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, "Total "+cpeProductName +" Price", "2"));
        steelHead.addPriceLine(aPriceLine(cpeProductName +" Price", "M0302171", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, cpeProductName +" Price", "3"));
        steelHead.addPriceLine(aPriceLine(cpeProductName + " Price", "M0302171", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, cpeProductName +" Price", "4"));
        steelHead.setParent(siteProductInstance);
        DefaultProductInstance vendorMaintenance = DefaultProductInstanceFixture.aProductInstance()
                                                                                .withProductInstanceId("vendorMaintenanceId")
                                                                                .withProductOffering(ProductOfferingFixture
                                                                                                         .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                                         .withProductIdentifier(new ProductIdentifier(steelheadVendorMaintenance.productCode(), steelheadVendorMaintenance.productName(), "1.0"))
                                                                                                         .withChargingSchemes(newArrayList(steelHeadVendorMaintenancePrice)).withAttribute(ProductOffering.PART_TYPE_IDENTIFIER)
                                                                                )
                                                                                .withAttributeValue(ProductOffering.PART_TYPE_IDENTIFIER, "Vendor Maintenance").build();
        vendorMaintenance.addPriceLine(aPriceLine(cpeProductName +" Vendor Maintenance Price", "M0302172", 100.00, PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, cpeProductName +" Price", "3"));
        vendorMaintenance.addPriceLine(aPriceLine(cpeProductName +" Vendor Maintenance Price", "M0302172", 100.00, PriceType.RECURRING, PriceCategory.CHARGE_PRICE.getLabel(), 333.00, cpeProductName +" Price", "4"));
        vendorMaintenance.addPriceLine(aPriceLine(cpeProductName +" Vendor Maintenance Price", "M0302172", 100.00, PriceType.ONE_TIME, PriceCategory.COST.getLabel(), 200.00, cpeProductName +" Price", "3"));
        vendorMaintenance.addPriceLine(aPriceLine(cpeProductName +" Vendor Maintenance Price", "M0302172", 100.00, PriceType.RECURRING, PriceCategory.COST.getLabel(), 100.00, cpeProductName +" Price", "4"));
        siteProductInstance.addRelationship(new ProductSalesRelationshipInstance(aSalesRelationship()
                                                                                         .withRelationName(cpeProductName).build(), steelHead));
        steelHead.addRelationship(new ProductSalesRelationshipInstance(aSalesRelationship()
                                                                           .withRelationName("vendor maintenance").build(), vendorMaintenance));


        return siteProductInstance;
    }

    public PricingSheetDataModel pricingSheetBidManagerCaveats() {
        List<BidManagerCommentsDTO> bidComments = newArrayList();
        BidManagerCommentsDTO comment1 = new BidManagerCommentsDTO("This is a sample comment1","Caveat1", DateTime.now(),"bid manager","email");
        BidManagerCommentsDTO comment2 = new BidManagerCommentsDTO("This is a sample comment2","Caveat2", DateTime.now(),"bid manager","email");
        BidManagerCommentsDTO comment3 = new BidManagerCommentsDTO("This is a sample comment3","Caveat3", DateTime.now(),"bid manager","email");
        bidComments.add(comment1);
        bidComments.add(comment2);
        bidComments.add(comment3);
        return aPricingSheetModel().withAccountManagerDTO(anAccountManager())
            .withCustomerDTO(aCustomerDTO())
            .withProjectDTO(aProjectDto())
            .withQuoteOptionDTO(aQuoteOptionDto())
            .withCentralSite(aSite()).withBidManagerCaveats(bidComments).build();
    }
}
