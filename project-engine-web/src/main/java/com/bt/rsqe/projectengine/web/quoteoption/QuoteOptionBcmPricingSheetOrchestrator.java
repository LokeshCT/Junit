package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.client.SpecialPriceBookClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.BcmSpreadSheet;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetProperty;
import com.bt.rsqe.projectengine.web.quoteoption.priceupdater.FutureAssetPriceUpdaterFactory;
import com.bt.rsqe.projectengine.web.quoteoption.priceupdater.SpecialPriceBookUpdater;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.enums.ProductCodes.*;
import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetProperty.*;
import static com.google.common.collect.Lists.*;

public class QuoteOptionBcmPricingSheetOrchestrator {

    private FutureAssetPricesFacade productInstancePricesFacade;
    private LineItemFacade lineItemFacade;
    private SpecialPriceBookClient specialPriceBookClient;

    private FutureAssetPriceUpdaterFactory updaterFactory;
    private PmrClient pmrClient;

    public QuoteOptionBcmPricingSheetOrchestrator(FutureAssetPricesFacade productInstancePricesFacade, LineItemFacade lineItemFacade, SpecialPriceBookClient specialPriceBookClient, FutureAssetPriceUpdaterFactory priceUpdaterFactory, PmrClient pmrClient) {
        this.productInstancePricesFacade = productInstancePricesFacade;
        this.lineItemFacade = lineItemFacade;
        this.specialPriceBookClient = specialPriceBookClient;
        this.updaterFactory = priceUpdaterFactory;
        this.pmrClient = pmrClient;
    }

    //todo: move this to  the REST layer
    public void importBCMData(String customerId, String contractId, String projectId, String quoteOptionId, BcmSpreadSheet bcmSpreadSheet) {
        final List<LineItemModel> allLineItems = lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, PriceSuppressStrategy.None);
        final List<LineItemModel> lineItems = lineItemsToBeApproved(allLineItems);
        Map<String, List<ProductIdentifier>> productsForSheet = getProductsForSheet();

        for (LineItemModel lineItem : lineItems) {
            String productSCode =  lineItem.getProductSCode();
            ProductIdentifier productFamily = pmrClient.getProductHCode(productSCode).get();
            String productFamilyName = !Strings.isNullOrEmpty(productFamily.getDisplayName()) ? productFamily.getDisplayName() : productFamily.getProductName();
            // TODO logic should not be based on checking product codes
            if (Onevoice.productCode().equals(lineItem.getProductSCode())) {
                updaterFactory.updaterFor(bcmSpreadSheet.getOneVoiceOptionsSheet()).update(lineItem);
                updaterFactory.updaterFor(bcmSpreadSheet.getOneVoiceChannelInformationSheet()).update(lineItem);
            } else if(OneCloudCiscoContract.productCode().equals(lineItem.getProductSCode())) {
                updaterFactory.updaterFor(bcmSpreadSheet.getSheet(productFamilyName, BCMProductSheetProperty.Contract)).update(lineItem);
            } else {
                if(shouldUpdateSheetData(lineItem.getProductSCode(), productsForSheet.get(SiteInstallable.sheetName))) {
                    updaterFactory.updaterFor(bcmSpreadSheet.getCASiteSheet(productFamilyName)).update(lineItem);
                }
                if(shouldUpdateSheetData(lineItem.getProductSCode(), productsForSheet.get(SiteAgnostic.sheetName))) {
                    updaterFactory.updaterFor(bcmSpreadSheet.getCAServiceSheet(productFamilyName)).update(lineItem);
                }
                if(shouldUpdateSheetData(lineItem.getProductSCode(), productsForSheet.get(SpecialBid.sheetName))) {
                    updaterFactory.updaterFor(bcmSpreadSheet.getSpecialBidServiceSheet()).update(lineItem);
                }
                if (UserContextManager.getPermissions().indirectUser) {
                    updaterFactory.updaterFor(bcmSpreadSheet.getProductInfoSheet(), lineItemFacade).update(lineItem);
                }
            }
            productInstancePricesFacade.save(lineItem.getFutureAssetPricesModel());
        }

        lineItemFacade.approveDiscounts(projectId, quoteOptionId, lineItemIds(lineItems));
        importSpecialPriceBookData(quoteOptionId, bcmSpreadSheet);
    }

    private boolean shouldUpdateSheetData(final String productSCode, List<ProductIdentifier> productIdentifiers) {
        Optional<ProductIdentifier> ProductIdentifierOptional = Iterables.tryFind(productIdentifiers, new Predicate<ProductIdentifier>() {
            @Override
            public boolean apply(ProductIdentifier input) {
                return productSCode.equals(input.getProductId());
            }
        });

        return ProductIdentifierOptional.isPresent();
    }

    private Map<String, List<ProductIdentifier>> getProductsForSheet() {
        List<ProductIdentifier> allSellableProductIdentifiers = pmrClient.getSalesCatalogue().getAllSellableProductIdentifiers();
        List<ProductIdentifier> siteAgnosticProducts = new ArrayList<ProductIdentifier>();
        List<ProductIdentifier> siteInstallableProducts = new ArrayList<ProductIdentifier>();
        List<ProductIdentifier> specialBidProducts = new ArrayList<ProductIdentifier>();
        Map<String, List<ProductIdentifier>> productMap = new HashMap<String, List<ProductIdentifier>>();
        for (ProductIdentifier productIdentifier : allSellableProductIdentifiers) {
            if (pmrClient.productOffering(ProductSCode.newInstance(productIdentifier.getProductId())).get().isSpecialBid()) {
                specialBidProducts.add(productIdentifier);
            } else if (pmrClient.productOffering(ProductSCode.newInstance(productIdentifier.getProductId())).get().isSiteInstallable()) {
                siteInstallableProducts.add(productIdentifier);
            } else if (!pmrClient.productOffering(ProductSCode.newInstance(productIdentifier.getProductId())).get().isSiteInstallable()) {
                siteAgnosticProducts.add(productIdentifier);
            }
        }
        productMap.put(SiteAgnostic.sheetName, siteAgnosticProducts);
        productMap.put(SiteInstallable.sheetName, siteInstallableProducts);
        productMap.put(SpecialBid.sheetName, specialBidProducts);
        return productMap;

    }

    private List<LineItemModel> lineItemsToBeApproved(List<LineItemModel> allLineItems) {
        List<LineItemModel> lineItems = newArrayList();
        for (LineItemModel lineItem : allLineItems) {
            if (lineItem.isDiscountApprovalRequested()) {
                lineItems.add(lineItem);
            }
        }
        return lineItems;
    }

    private List<LineItemId> lineItemIds(List<LineItemModel> lineItems) {
        return Lists.transform(lineItems, new Function<LineItemModel, LineItemId>() {
            @Override
            public LineItemId apply(LineItemModel input) {
                return new LineItemId(input.getId());
            }
        });
    }

    private void importSpecialPriceBookData(String quoteOptionId, BcmSpreadSheet oneVoiceBcmSpreadSheet) {
        List<SpecialPriceBook> specialPriceBooks = specialPriceBookClient.get(new QuoteOptionId(quoteOptionId));
        SpecialPriceBookUpdater specialPriceBookUpdater = updaterFactory.updaterFor(oneVoiceBcmSpreadSheet.getSpecialPriceBookSheet());
        for (SpecialPriceBook specialPriceBook : specialPriceBooks) {
            specialPriceBookUpdater.update(specialPriceBook);
            specialPriceBookClient.put(specialPriceBook);
        }
    }

    public void rejectDiscounts(String projectId, String quoteOptionId) {
        lineItemFacade.rejectDiscounts(projectId,
                                       quoteOptionId,
                                       lineItemFacade.fetchLineItemIds(projectId, quoteOptionId));
    }
}
