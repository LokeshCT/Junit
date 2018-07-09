package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.AbstractPricingSheetProductModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetSpecialBidProduct;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetProperty.*;
import static com.google.common.collect.Lists.*;

public class ProductsBCMSheetFactory {
    private static String[] STANDARD_GROUPS = new String[]{Groups.site.name(), Groups.common.name(), Groups.price.name()};
    private BCMProductSheetGenerator bcmProductsSheetGenerator;
    private HeaderRowModelFactory headerRowModelFactory;
    private BCMDataRowModelFactory bcmDataRowModelFactory;
    private PricingSheetDataModelFactory pricingSheetDataModelFactory;
    private PmrClient pmrClient;
    private PricingClient pricingClient;

    public ProductsBCMSheetFactory(BCMProductSheetGenerator bcmProductsSheetGenerator,
                                   HeaderRowModelFactory headerRowModelFactory,
                                   BCMDataRowModelFactory bcmDataRowModelFactory,
                                   PricingSheetDataModelFactory pricingSheetDataModelFactory,
                                   PmrClient pmrClient, PricingClient pricingClient) {
        this.bcmProductsSheetGenerator = bcmProductsSheetGenerator;
        this.headerRowModelFactory = headerRowModelFactory;
        this.bcmDataRowModelFactory = bcmDataRowModelFactory;
        this.pricingSheetDataModelFactory = pricingSheetDataModelFactory;
        this.pmrClient = pmrClient;
        this.pricingClient = pricingClient;
    }

    public void createProductSheets(HSSFWorkbook workbook, String customerId, String projectId, String quoteOptionId) {
        final PricingSheetDataModel dataModel = createDataModel(customerId, projectId, quoteOptionId);
        List<? extends AbstractPricingSheetProductModel> productModels = dataModel.getProducts();

        Map<String, List<ProductIdentifier>> productsForSheet = getProductsForSheet();
        HashMultimap<String, AbstractPricingSheetProductModel> pricingSheetProductModelMapForSiteInstallable = groupPricingSheetProductModelByScode(productModels, productsForSheet.get(SiteInstallable.sheetName));
        HashMultimap<String, AbstractPricingSheetProductModel> pricingSheetProductModelMapForSiteAgnostic = groupPricingSheetProductModelByScode(productModels, productsForSheet.get(SiteAgnostic.sheetName));
        HashMultimap<String, AbstractPricingSheetProductModel> pricingSheetProductModelMapForContract = groupPricingSheetProductModelByScode(dataModel.getContractProducts(), productsForSheet.get(Contract.sheetName));

        if(!pricingSheetProductModelMapForSiteInstallable.isEmpty()){
            createBcmSheets(productsForSheet.get(SiteInstallable.sheetName),
                            pricingSheetProductModelMapForSiteInstallable,
                            workbook,
                            SiteInstallable,
                            STANDARD_GROUPS);
        }

        if(!pricingSheetProductModelMapForSiteAgnostic.isEmpty()){
            CreateSiteAgnosticBcmSheets(productsForSheet.get(SiteAgnostic.sheetName), pricingSheetProductModelMapForSiteAgnostic, workbook);
        }

        if(!pricingSheetProductModelMapForContract.isEmpty()) {
            createBcmSheets(productsForSheet.get(Contract.sheetName),
                            pricingSheetProductModelMapForContract,
                            workbook,
                            Contract,
                            Groups.common.name(),
                            Groups.price.name());
        }

        final List<AbstractPricingSheetProductModel> specialBidProducts = transformSpecialBidToProductModel(dataModel.getSpecialBidProducts());

        if(specialBidProducts.size() > 0){
            createSheet(workbook, specialBidProducts, SpecialBid.sheetName, SpecialBid.costSetCount, (productsForSheet.get(SpecialBid.sheetName)), STANDARD_GROUPS);
        }
    }

    private HashMultimap<String, AbstractPricingSheetProductModel> groupPricingSheetProductModelByScode(List<? extends AbstractPricingSheetProductModel> productModels, List<ProductIdentifier> productIdentifiers) {
        HashMultimap<String, AbstractPricingSheetProductModel> pricingSheetProductModelMap = HashMultimap.create();
        for (AbstractPricingSheetProductModel pricingSheetProductModel : productModels){
            for(ProductIdentifier productIdentifier : productIdentifiers) {
                if(pricingSheetProductModel.getSCode().equals(productIdentifier.getProductId())) {
                    pricingSheetProductModelMap.put(pricingSheetProductModel.getSCode(), pricingSheetProductModel);
                }
            }
        }
        return pricingSheetProductModelMap;
    }

    private void createBcmSheets(List<ProductIdentifier> productIdentifiers,
                                 HashMultimap<String, AbstractPricingSheetProductModel> productModelMap,
                                 HSSFWorkbook workbook,
                                 BCMProductSheetProperty sheetProperty,
                                 String... groups) {
        for(String productModelScode : productModelMap.keySet()){
            ProductIdentifier productFamily = pmrClient.getProductHCode(productModelScode).get();
            createSheet(workbook,
                        Lists.newArrayList(productModelMap.get(productModelScode)),
                        sheetProperty.getSheetNameFor(!Strings.isNullOrEmpty(productFamily.getDisplayName()) ? productFamily.getDisplayName() : productFamily.getProductName()),
                        sheetProperty.costSetCount,
                        filterProductsByScode(productIdentifiers, productModelScode),
                        groups);
        }
    }

    private void CreateSiteAgnosticBcmSheets(List<ProductIdentifier> productIdentifiers, HashMultimap<String, AbstractPricingSheetProductModel> productModelMap, HSSFWorkbook workbook) {
        int sheetcount = 0;
        for(String productModelScode : productModelMap.keySet()){
            sheetcount++;
            String productFamilyName = pmrClient.getProductHCode(productModelScode).get().getDisplayName();
            if(sheetcount>1) {
                createSheet(workbook, Lists.newArrayList(productModelMap.get(productModelScode)), SiteAgnostic.getSheetNameFor(productFamilyName) + " " + (sheetcount - 1), SiteAgnostic.costSetCount, filterProductsByScode(productIdentifiers, productModelScode), STANDARD_GROUPS);
            } else {
                createSheet(workbook, Lists.newArrayList(productModelMap.get(productModelScode)), SiteAgnostic.getSheetNameFor(productFamilyName), SiteAgnostic.costSetCount, filterProductsByScode(productIdentifiers, productModelScode), STANDARD_GROUPS);
            }
        }
    }

    public List<ProductIdentifier> filterProductsByScode(List<ProductIdentifier> productIdentifiers, final String productScode) {
        return Lists.newArrayList(Iterables.filter(productIdentifiers, new Predicate<ProductIdentifier>() {
            @Override
            public boolean apply(ProductIdentifier identifier) {
                return productScode.equalsIgnoreCase(identifier.getProductId());
            }
        }));
    }

    public List<AbstractPricingSheetProductModel> transformSpecialBidToProductModel(List<PricingSheetSpecialBidProduct> specialBidProducts) {
        return newArrayList(Iterables.transform(specialBidProducts, new Function<PricingSheetSpecialBidProduct, AbstractPricingSheetProductModel>() {
            @Override
            public AbstractPricingSheetProductModel apply(@Nullable PricingSheetSpecialBidProduct specialBidProduct) {
                return new PricingSheetProductModel(specialBidProduct.getSite(),
                                                    specialBidProduct.getProductInstance(),
                                                    specialBidProduct.getQuoteOptionItem(),
                                                    specialBidProduct.mergeResult,
                                                    null, pricingClient, null);
            }
        }));
    }

    public void createSheet(HSSFWorkbook workbook, List<AbstractPricingSheetProductModel> productModels, String sheetName, int costSetCount, List<ProductIdentifier> products, String[] groups) {
        HeaderRowModel headerRowModel = headerRowModelFactory.createHeader(sheetName, costSetCount, products);
        List<BCMDataRowModel> dataRowModel = bcmDataRowModelFactory.createRowModel(productModels);
        bcmProductsSheetGenerator.createSheet(workbook, headerRowModel, dataRowModel, sheetName, costSetCount, groups);
    }

    public PricingSheetDataModel createDataModel(String customerId, String projectId, String quoteOptionId) {
        PricingSheetDataModel pricingSheetDataModel = pricingSheetDataModelFactory.create(customerId, projectId, quoteOptionId, Optional.<String>absent());
        return pricingSheetDataModel;
    }

    public Map<String, List<ProductIdentifier>> getProductsForSheet() {
        List<ProductIdentifier> allSellableProductIdentifiers = pmrClient.getSalesCatalogue().getAllSellableProductIdentifiers();
        List<ProductIdentifier> siteAgnosticProducts = new ArrayList<ProductIdentifier>();
        List<ProductIdentifier> siteInstallableProducts = new ArrayList<ProductIdentifier>();
        List<ProductIdentifier> specialBidProducts = new ArrayList<ProductIdentifier>();
        List<ProductIdentifier> contractProducts = new ArrayList<ProductIdentifier>();
        Map<String, List<ProductIdentifier>> productMap = new HashMap<String, List<ProductIdentifier>>();
        for (ProductIdentifier productIdentifier : allSellableProductIdentifiers) {
            ProductOffering productOffering = pmrClient.productOffering(ProductSCode.newInstance(productIdentifier.getProductId())).get();

            if(productOffering.isSimpleTypeOf(SimpleProductOfferingType.Contract)) {
                contractProducts.add(productIdentifier);
            } else if (productOffering.isSpecialBid()) {
                specialBidProducts.add(productIdentifier);
            } else if (productOffering.isSiteInstallable()) {
                siteInstallableProducts.add(productIdentifier);
            } else if (!productOffering.isSiteInstallable()) {
                siteAgnosticProducts.add(productIdentifier);
            }
        }
        productMap.put(SiteAgnostic.sheetName, siteAgnosticProducts);
        productMap.put(SiteInstallable.sheetName, siteInstallableProducts);
        productMap.put(SpecialBid.sheetName, specialBidProducts);
        productMap.put(Contract.sheetName, contractProducts);
        return productMap;
    }
}
