package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerinventory.parameter.RandomSiteId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModel;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class BCMProductsSheetTestDataFixture {

    private PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
    private final String siteId = new RandomSiteId().value();
    private final String action = "PROVIDE";

    public HeaderRowModel createHeaderModel() {
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        headerCells.add(new HeaderCell(0, "Test0", true, "site", "", 0, 1));
        headerCells.add(new HeaderCell(1, "Test1", true, "site", "", 0, 1));
        headerCells.add(new HeaderCell(2, "Test2", true, "site", "", 0, 1));
        headerCells.add(new HeaderCell(3, "Test3", true, "site", "", 0, 1));
        headerCells.add(new HeaderCell(4, "Test4", true, "site", "", 0, 1));
        return new HeaderRowModel(headerCells);
    }

    public HeaderRowModel aHeaderModelWithOneSiteAndAggregatedPrice() {
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        headerCells.add(new HeaderCell(0, "SiteID", true, "site", "Site.bfgSiteID", 0, 1));
        headerCells.add(new HeaderCell(1, "Site Name", true, "site", "Site.name", 0, 1));
        headerCells.add(new HeaderCell(2, "Price description", true, "price", "PriceLine.PriceDescription", 0, 1));
        headerCells.add(new HeaderCell(3, "One time EUP price", true, "price", "PriceLine.OnetimeEUPPrice", 0, 1));
        headerCells.add(new HeaderCell(4, "Monthly Recurring EUP price", true, "price", "PriceLine.RecurringEUPPrice", 0, 1));
        return new HeaderRowModel(headerCells);
    }

    public HeaderRowModel aHeaderModelWithOneSiteAndCostPrice() {
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        headerCells.add(new HeaderCell(0, "SiteID", true, "site", "Site.bfgSiteID", 0, 1));
        headerCells.add(new HeaderCell(1, "Site Name", true, "site", "Site.name", 0, 1));
        headerCells.add(new HeaderCell(2, "Price description", true, "price", "PriceLine.PriceDescription", 0, 1));
        headerCells.add(new HeaderCell(3, "One time EUP price", true, "price", "PriceLine.OnetimeEUPPrice", 0, 1));
        headerCells.add(new HeaderCell(4, "One time PTP price", true, "price", "PriceLine.OnetimePTPPrice", 0, 1));
        headerCells.add(new HeaderCell(5, "One time Discount", true, "price", "PriceLine.OneTimeDiscount", 0, 0));
        headerCells.add(new HeaderCell(6, "Monthly Recurring EUP price", true, "price", "PriceLine.RecurringEUPPrice", 0, 1));
        headerCells.add(new HeaderCell(7, "Monthly Recurring PTP price", true, "price", "PriceLine.RecurringPTPPrice", 0, 1));
        headerCells.add(new HeaderCell(8, "Monthly Discount", true, "price", "PriceLine.MonthlyDiscount", 0, 0));
        headerCells.add(new HeaderCell(9, "Cost Description", true, "cost", "PriceDescription", 0, 1));
        headerCells.add(new HeaderCell(10, "One Time Cost", true, "cost", "RecurringPrice", 0, 1));
        headerCells.add(new HeaderCell(11, "Recurring Cost", true, "cost", "NonRecurringPrice", 0, 1));
        return new HeaderRowModel(headerCells);
    }

    public BCMDataRowModel aDataModelWithOneAggregatedPrice() {
        PriceLine oneTimePriceLine = pricingSheetTestDataFixture.aPriceLine("Root Product One time price", "M0302165", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "1");
        PriceLine recurringPrice = pricingSheetTestDataFixture.aPriceLine("Root Product Rental price", "M0302165", 50.00, PriceType.RECURRING, "Recommended Retail Price", 331.00, "A", "2");
        BCMPriceModel priceModel = new BCMPriceModel(oneTimePriceLine, recurringPrice, "Price", null, null, action);

        return new BCMDataRowModel(null, null, new SiteDTO(siteId, "sitename"), null, priceModel, null);
    }

    public BCMDataRowModel aDataModelWithOneCostPrice() {
        PriceLine oneTimePriceLine = pricingSheetTestDataFixture.aPriceLine("Root Product One time price", "M0302165", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 332.00, "A", "3");
        PriceLine recurringPrice = pricingSheetTestDataFixture.aPriceLine("Root Product Rental price", "M0302165", 50.00, PriceType.RECURRING, "Recommended Retail Price", 334.00, "A", "4");
        BCMPriceModel priceModel = new BCMPriceModel(oneTimePriceLine, recurringPrice, "price", null, null, action);

        PriceLine oneTimeCostPriceLine = pricingSheetTestDataFixture.aPriceLine("Root Product One time cost", "M0302166", 100.00, PriceType.ONE_TIME, "Recommended Retail cost", 332.00, "A", "5");
        PriceLine recurringCostPrice = pricingSheetTestDataFixture.aPriceLine("Root Product Rental cost", "M0302166", 50.00, PriceType.RECURRING, "Recommended Retail cost", 334.00, "A", "6");
        BCMPriceModel costPriceModel = new BCMPriceModel(oneTimeCostPriceLine, recurringCostPrice, "cost", null, null, action);

        return new BCMDataRowModel(null, null, new SiteDTO(siteId, "sitename"), null, priceModel, newArrayList(costPriceModel));
    }

    public BCMPriceModel aPriceModelWithOnePrice() {
        PriceLine oneTimePriceLine = pricingSheetTestDataFixture.aPriceLine("Root Product One time price", "M0302165", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "7");
        PriceLine recurringPrice = pricingSheetTestDataFixture.aPriceLine("Root Product Rental price", "M0302165", 50.00, PriceType.RECURRING, "Recommended Retail Price", 331.00, "A", "8");
        return new BCMPriceModel(oneTimePriceLine, recurringPrice, "price", null, null, action);

    }

    public List<BCMPriceModel> aPriceModelWithTwoCost() {
        PriceLine oneTime = pricingSheetTestDataFixture.aPriceLine("Root Product One time cost", "M0302166", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "9");
        PriceLine recurringPrice = pricingSheetTestDataFixture.aPriceLine("Root Product Rental cost", "M0302166", 50.00, PriceType.RECURRING, "Recommended Retail Price", 331.00, "A", "10");

        PriceLine oneTime1 = pricingSheetTestDataFixture.aPriceLine("Root Product One time cost1", "M0302166", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "11");
        PriceLine recurringPrice1 = pricingSheetTestDataFixture.aPriceLine("Root Product Rental cost1", "M0302166", 50.00, PriceType.RECURRING, "Recommended Retail Price", 331.00, "A", "12");
        return newArrayList(new BCMPriceModel(oneTime, recurringPrice, "cost", null, null, action), new BCMPriceModel(oneTime1, recurringPrice1, "cost", null, null, action));
    }

    public List<BCMPriceModel> aPriceModelWithOneCost() {
        PriceLine oneTime = pricingSheetTestDataFixture.aPriceLine("Root Product One time cost", "M0302166", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "13");
        PriceLine recurringPrice = pricingSheetTestDataFixture.aPriceLine("Root Product Rental cost", "M0302166", 50.00, PriceType.RECURRING, "Recommended Retail Price", 331.00, "A", "14");

        return newArrayList(new BCMPriceModel(oneTime, recurringPrice, "cost", null, null, action));
    }


    public BCMDataRowModel aDataModelWithOneProduct() {
        PricingSheetProductModel model = pricingSheetTestDataFixture.aPricingSheetProductModelWithAChild();
        return BCMDataRowModelFixture.aBCMRowModelFixture().withRootProductInstance(model.getProductInstance())
                                     .withAggregatedPriceLine(aPriceModelWithOnePrice())
                                     .withChildProducts(newArrayList(model.getProductInstance().getChildren()))
                                     .withCostLines(aPriceModelWithTwoCost())
                                     .withSite(model.getSite())
                                     .withQuoteOptionItem(model.getQuoteOptionItem())
                                     .build();
    }

    public BCMDataRowModel aDataModelWithAnotherProduct() {
        PricingSheetProductModel model = pricingSheetTestDataFixture.aSiteAgnosticPricingSheetProductModel();
        return BCMDataRowModelFixture.aBCMRowModelFixture().withRootProductInstance(model.getProductInstance())
                                     .withAggregatedPriceLine(aPriceModelWithOnePrice())
                                     .withChildProducts(newArrayList(model.getProductInstance().getChildren()))
                                     .withCostLines(aPriceModelWithOneCost())
                                     .withSite(model.getSite())
                                     .withQuoteOptionItem(model.getQuoteOptionItem())
                                     .build();
    }


    public HeaderRowModel aHeaderModelWithOneProduct() {
        List<HeaderCell> headerCells = new ArrayList<HeaderCell>();
        int startIndex = 22;
        PricingSheetProductModel rootModel = pricingSheetTestDataFixture.aPricingSheetProductModelWithAChild();
        PricingSheetProductModel rootModel1 = pricingSheetTestDataFixture.aSiteAgnosticPricingSheetProductModel();
        List<PricingSheetProductModel> allProducts = new ArrayList<PricingSheetProductModel>();
        allProducts.add(rootModel);
        allProducts.addAll(rootModel.getAllChildren());
        allProducts.add(rootModel1);
        allProducts.addAll(rootModel1.getAllChildren());
        for (ProductSheetStaticColumn staticColumn : ProductSheetStaticColumn.values()) {
            headerCells.add(new HeaderCell(staticColumn.columnIndex, staticColumn.columnName, staticColumn.visible, staticColumn.type, staticColumn.retrieveValueFrom, 0, 1));
        }
        for (int count = 0; count < 4; count++) {
            headerCells.add(new HeaderCell(startIndex++, CostColumn.COST_DESCRIPTION.columnName, CostColumn.COST_DESCRIPTION.visible, CostColumn.COST_DESCRIPTION.type, CostColumn.COST_DESCRIPTION.retrieveValueFrom, 0, 1));
            headerCells.add(new HeaderCell(startIndex++, CostColumn.ONE_TIME_COST.columnName, CostColumn.ONE_TIME_COST.visible, CostColumn.ONE_TIME_COST.type, CostColumn.ONE_TIME_COST.retrieveValueFrom, 0, 1));
            headerCells.add(new HeaderCell(startIndex++, CostColumn.RECURRING_COST.columnName, CostColumn.RECURRING_COST.visible, CostColumn.RECURRING_COST.type, CostColumn.RECURRING_COST.retrieveValueFrom, 0, 1));
        }
        for (PricingSheetProductModel model : allProducts) {
            headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " " + ProductDetailsColumn.PRIMARY_IDENTIFIER.columnName + " " + model.getSCode(), true, "product", "", 0, 1));
            headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " " + ProductDetailsColumn.VERSION_NUMBER.columnName, true, "product", "", 0, 1));
            headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " " + ProductDetailsColumn.PRODUCT_INSTANCE_ID.columnName, true, "product", "", 0, 1));

            for (InstanceCharacteristic instanceCharacteristic : model.getInstanceCharacteristics()) {
                headerCells.add(new HeaderCell(startIndex++, model.getProductName() + " " + instanceCharacteristic.getName(), true, "product", "", 0, 1));
            }
        }
        return new HeaderRowModel(headerCells);
    }

    public String siteId() {
        return siteId;
    }
}
