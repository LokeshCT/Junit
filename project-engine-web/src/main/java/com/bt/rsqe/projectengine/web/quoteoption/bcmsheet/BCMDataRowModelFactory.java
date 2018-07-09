package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.config.dto.AssociatedCostConfig;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.ChargingSchemeConfig;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.pricing.factory.PricePredicates;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.AbstractPricingSheetProductModel;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Collections2.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.apache.commons.lang.StringUtils.*;

public class BCMDataRowModelFactory {

    private ProductInstanceClient productInstanceClient;

    public BCMDataRowModelFactory(ProductInstanceClient productInstanceClient) {
        this.productInstanceClient = productInstanceClient;
    }

    public List<ProductDataRowModel> createProductRowModel(List<AbstractPricingSheetProductModel> productModels){
        List<ProductDataRowModel> productDataRows = newArrayList();
        for (AbstractPricingSheetProductModel productModel : productModels){
            productDataRows.addAll(fetchSiteProductRowModels(productModel));
        }
        return productDataRows;
    }

    public List<ProductDataRowModel> createServiceRowModel(List<AbstractPricingSheetProductModel> productModels, PricingConfig pricingConfig){
        List<ProductDataRowModel> productDataRows = newArrayList();
        for (AbstractPricingSheetProductModel productModel : productModels){
            productDataRows.addAll(fetchServiceRowModels(productModel, pricingConfig));
        }
        return productDataRows;
    }

    private List<ProductDataRowModel> fetchServiceRowModels(AbstractPricingSheetProductModel productModel, PricingConfig pricingConfig) {
        List<ProductDataRowModel> productDataRows = newArrayList();
        QuoteOptionItemDTO itemDto = productModel.getQuoteOptionItem();
        SiteDTO siteDTO = productModel.getSite();
        ProductInstance latestProductInstance = getLatestProductInstance(productModel.getProductInstance());
        List<ProductDataInfo> serviceProdInfoList = newArrayList();
        getAllChildProductDataInfo(pricingConfig, itemDto, latestProductInstance,serviceProdInfoList);
        for (ProductDataInfo productDataInfo : serviceProdInfoList){
            productDataRows.add(new ProductDataRowModel(itemDto, siteDTO, productDataInfo));
        }
        return productDataRows;
    }

    private void getAllChildProductDataInfo(PricingConfig pricingConfig, QuoteOptionItemDTO itemDto, ProductInstance latestProductInstance,List<ProductDataInfo> serviceProdInfoList) {
        serviceProdInfoList.addAll(getServiceProductDataInfo(latestProductInstance, itemDto.action, pricingConfig));
        Set<ProductInstance> serviceChildren = latestProductInstance.getChildren();
        for (ProductInstance serviceChild : serviceChildren) {
            getAllChildProductDataInfo(pricingConfig,itemDto,serviceChild,serviceProdInfoList);
        }
    }

    private List<ProductDataInfo> getServiceProductDataInfo(ProductInstance productInstance, String action, PricingConfig pricingConfig) {
        List<ProductDataInfo> productDataInfoList = newArrayList();
        ProductInstance asIsProductInstance = getAsIsProductInstance(productInstance);
        List<BCMPriceModel> priceModels = getBCMPriceModelForServices(productInstance, action, asIsProductInstance);
        for (BCMPriceModel priceModel : priceModels) {
            List<BCMPriceModel> costModel = getCostModelForPriceModel(productInstance, action, priceModel, pricingConfig);
            productDataInfoList.add(new ProductDataInfo(productInstance , priceModel, costModel));
        }
        return productDataInfoList;
    }

    private List<BCMPriceModel> getCostModelForPriceModel(ProductInstance productInstance, String action, final BCMPriceModel priceModel, PricingConfig pricingConfig) {
        List<ChargingSchemeConfig> priceChargingSchemes = newArrayList(Iterables.filter(pricingConfig.getChargingSchemes(), new Predicate<ChargingSchemeConfig>() {
            @Override
            public boolean apply(ChargingSchemeConfig input) {
                return (isNotNull(priceModel) && priceModel.getScheme().getName().equalsIgnoreCase(input.getName()));
            }
        }));
        List<BCMPriceModel> costModel;
        if(!(priceChargingSchemes.isEmpty())){
            BillingTariffRulesetConfig billingTariffRulesetConfig = getMachingBillingtariffRuleForPriceModel(priceChargingSchemes.get(0), priceModel);
            List<BCMPriceModel> associatedCostLines = new LinkedList<BCMPriceModel>();
            if(isNotNull(billingTariffRulesetConfig)){
                associatedCostLines = getAssociatedCostLinesForPriceLine(productInstance, action, billingTariffRulesetConfig.getAssociatedCostSets());
            }

            costModel = aggregateCostModels(associatedCostLines);
        }else{
           costModel = getBcmCostModel(productInstance, action, "");
        }
        return costModel;
    }

    private BillingTariffRulesetConfig getMachingBillingtariffRuleForPriceModel(ChargingSchemeConfig chargingSchemeConfig, BCMPriceModel priceModel) {
        for (BillingTariffRulesetConfig billingTariffRulesetConfig : chargingSchemeConfig.getBillingTariffRuleSets()){
            if(isNotNull(priceModel.getOneTimePriceLine()) && billingTariffRulesetConfig.getId().equals(priceModel.getOneTimePriceLine().getPmfId())){
                return billingTariffRulesetConfig;
            }else if(isNotNull(priceModel.getMonthlyPriceLine()) && billingTariffRulesetConfig.getId().equals(priceModel.getMonthlyPriceLine().getPmfId())){
                return billingTariffRulesetConfig;
            }
        }
        return null;
    }


    private List<BCMPriceModel> getAssociatedCostLinesForPriceLine(ProductInstance productInstance, String action, List<AssociatedCostConfig> associatedCostSets) {
        List<BCMPriceModel> allCostLines = getCostLines(productInstance, action, "");
        List<BCMPriceModel> associatedCostLines = newArrayList();
        if(!associatedCostSets.isEmpty()){
            for (ProductInstance childProductInstance : productInstance.getChildren()) {
                getCostLinesFromChildren(childProductInstance, allCostLines, action);
            }
            associatedCostLines = filterAssociatedCostLines(allCostLines, associatedCostSets);
        }else{
            associatedCostLines.addAll(allCostLines);
        }
        return associatedCostLines;
    }

    private List<BCMPriceModel> filterAssociatedCostLines(List<BCMPriceModel> allCostLines, List<AssociatedCostConfig> associatedCostSets) {
        List<BCMPriceModel> associatedCosts = newArrayList();
        for (BCMPriceModel costLine : allCostLines){
            matchCostLinesBasedOnMCode(costLine, associatedCosts, associatedCostSets);
        }
        return associatedCosts;
    }

    private void matchCostLinesBasedOnMCode(BCMPriceModel costLine, List<BCMPriceModel> associatedCosts, List<AssociatedCostConfig> associatedCostSets) {
        for (AssociatedCostConfig costConfig : associatedCostSets){
            if(isNotNull(costLine.getOneTimePriceLine()) && costConfig.getId().equals(costLine.getOneTimePriceLine().getPmfId())){
                associatedCosts.add(costLine);
            }else if(isNotNull(costLine.getMonthlyPriceLine()) && costConfig.getId().equals(costLine.getMonthlyPriceLine().getPmfId())){
                associatedCosts.add(costLine);
            }
        }
    }


    public List<ProductDataRowModel> createSiteManagementRowModel(List<AbstractPricingSheetProductModel> productModels, PricingConfig pricingConfig){
        List<ProductDataRowModel> productDataRows = newArrayList();
        for (AbstractPricingSheetProductModel productModel : productModels){
            productDataRows.addAll(fetchSiteManagementRowModels(productModel,pricingConfig));
        }
        return productDataRows;
    }

    private List<ProductDataRowModel> fetchSiteManagementRowModels(AbstractPricingSheetProductModel productModel, PricingConfig pricingConfig) {
        List<ProductDataRowModel> productDataRows = newArrayList();
        QuoteOptionItemDTO itemDto = productModel.getQuoteOptionItem();
        SiteDTO siteDTO = productModel.getSite();
        ProductInstance latestProductInstance = getLatestProductInstance(productModel.getProductInstance());
        ProductDataInfo rootProductInfo = getSiteMgmtProductDataInfo(latestProductInstance, itemDto.action, pricingConfig);
        if(isNotNull(rootProductInfo)){
            ProductDataRowModel prodDataRow = new ProductDataRowModel(itemDto, siteDTO, rootProductInfo);
            productDataRows.add(prodDataRow);
        }
        return productDataRows;
    }


    private List<ProductDataRowModel> fetchSiteProductRowModels(AbstractPricingSheetProductModel productModel) {
        List<ProductDataRowModel> productDataRows = newArrayList();
        QuoteOptionItemDTO itemDto = productModel.getQuoteOptionItem();
        SiteDTO siteDTO = productModel.getSite();
        ProductInstance latestProductInstance = getLatestProductInstance(productModel.getProductInstance());
        ProductDataInfo rootProductInfo = getProductDataInfo(latestProductInstance, itemDto.action, "");
        if(latestProductInstance.isSiteInstallable()){
            List<ProductInstance> cpeInstances = getCpeInstances(latestProductInstance);
            for (ProductInstance cpeInstance : cpeInstances){
                ProductDataInfo cpeProdInfo = getProductDataInfo(cpeInstance, itemDto.action, "Site Management");
                if(isNotNull(cpeProdInfo)){
                    ProductDataRowModel productRow = new ProductDataRowModel(itemDto , siteDTO, rootProductInfo, cpeProdInfo,
                                                                             getVendorMaintenanceProdInfo(cpeInstance, itemDto.action),
                                                                             getLicences(cpeInstance, itemDto.action));
                    productDataRows.add(productRow);
                }
            }
        }else{
            ProductDataRowModel productRow = new ProductDataRowModel(itemDto,siteDTO,rootProductInfo);
            productDataRows.add(productRow);
            Set<ProductInstance> serviceChildren = latestProductInstance.getChildren();
            for (ProductInstance serviceChild : serviceChildren) {
                ProductDataInfo productDataInfo = getProductDataInfo(serviceChild, itemDto.action, "");
                if(isNotNull(productDataInfo)){
                    ProductDataRowModel serviceRow = new ProductDataRowModel(itemDto,siteDTO,productDataInfo);
                    productDataRows.add(serviceRow);
                }
            }
        }
        return productDataRows;
    }

    private  ProductDataInfo getProductDataInfo(ProductInstance productInstance, String action, String excludedCostlines){
        BCMPriceModel priceModel = getBcmPriceModel(productInstance, action);
        List<BCMPriceModel> costModel =  getBcmCostModel(productInstance, action, excludedCostlines);
        if(isNotNull(priceModel) || isNotNull(costModel)){
            return new ProductDataInfo(productInstance , priceModel, costModel);
        }
        return null;
    }

    private ProductInstance getAsIsProductInstance(ProductInstance productInstance) {
        Optional<ProductInstance> asIsProductInstanceOptional = productInstanceClient.getSourceAsset(productInstance.getProductInstanceId().getValue());
        return asIsProductInstanceOptional.isPresent()? asIsProductInstanceOptional.get():null;
    }

    private ProductInstance getLatestProductInstance(ProductInstance productInstance){
        return productInstanceClient.getLatestProduct(productInstance.getProductInstanceId(), productInstance.getQuoteOptionId());
    }

    private  ProductDataInfo getSiteMgmtProductDataInfo(ProductInstance productInstance, String action,PricingConfig pricingConfig){
        BCMPriceModel priceModel = getBcmPriceModel(productInstance, action);
        List<BCMPriceModel> costModel =  getCostModelForPriceModel(productInstance, action, priceModel, pricingConfig);
        if(isNotNull(priceModel) || isNotNull(costModel)){
            return new ProductDataInfo(productInstance , priceModel, costModel);
        }
        return null;
    }

    private ProductDataInfo getVendorMaintenanceProdInfo(ProductInstance cpeInstance, String action){
        List<ProductSalesRelationshipInstance> vendorMaintenanceProdRelations = newArrayList(Iterables.filter(cpeInstance.getRelationships(),
                                                                                                              new Predicate<ProductSalesRelationshipInstance>() {
            @Override
            public boolean apply(@Nullable ProductSalesRelationshipInstance input) {
                try {
                    return input.getRelatedProductInstance().getInstanceCharacteristic(ProductOffering.PART_TYPE_IDENTIFIER)
                                .getStringValue().equalsIgnoreCase("Vendor Maintenance") ? true : false;
                } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                    instanceCharacteristicNotFound.printStackTrace();
                }
                return false;
            }
        }));
        ProductDataInfo vendorMaintenanceInfo = null;
        if(vendorMaintenanceProdRelations != null && !(vendorMaintenanceProdRelations.isEmpty())){
            ProductSalesRelationshipInstance vendorMaintenanceInstance = vendorMaintenanceProdRelations.get(0);
            vendorMaintenanceInfo = new ProductDataInfo(vendorMaintenanceInstance.getRelatedProductInstance(),
                                                        getBcmPriceModel(vendorMaintenanceInstance.getRelatedProductInstance(), action),
                                                        getBcmCostModel(vendorMaintenanceInstance.getRelatedProductInstance(), action, ""));
        }
        return vendorMaintenanceInfo;
    }

    private List<ProductDataInfo> getLicences(ProductInstance cpeInstance, String action){
        List<ProductSalesRelationshipInstance> licenceRelations = newArrayList(Iterables.filter(cpeInstance.getRelationships(), new Predicate<ProductSalesRelationshipInstance>() {
            @Override
            public boolean apply(@Nullable ProductSalesRelationshipInstance input) {
                try {
                    return input.getRelatedProductInstance().getInstanceCharacteristic(ProductOffering.PART_TYPE_IDENTIFIER).
                        getStringValue().equalsIgnoreCase("SW Licence") ? true : false;
                } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                    instanceCharacteristicNotFound.printStackTrace();
                }
                return false;
            }
        }));
        List<ProductDataInfo> licencesList = newArrayList();
        for (ProductSalesRelationshipInstance relationshipInstance : licenceRelations){
            ProductDataInfo licenceInfo = new ProductDataInfo(relationshipInstance.getRelatedProductInstance(),
                                                              getBcmPriceModel(relationshipInstance.getRelatedProductInstance(), action),
                                                              getBcmCostModel(relationshipInstance.getRelatedProductInstance(), action, ""));
            licencesList.add(licenceInfo);
        }
        return licencesList;
    }

    public List<BCMPriceModel> getBCMPriceModelForServices(ProductInstance productInstance, String action, ProductInstance asIs){
        List<PriceLine> priceLines = PriceSuppressStrategy.BCMSheet.suppressPriceCostLines(productInstance.getChargingSchemes(), productInstance.getPriceLines());
        List<PriceLine> asIsPriceLines = null != asIs ? PriceSuppressStrategy.BCMSheet.suppressPriceCostLines(asIs.getChargingSchemes(), asIs.getPriceLines()) : new LinkedList<PriceLine>();
        List<BCMPriceModel> priceModels = transformPriceLinesToPriceModel(productInstance, priceLines, action, asIsPriceLines);
        priceModels = newArrayList(Iterables.filter(priceModels, new Predicate<BCMPriceModel>() {
            @Override
            public boolean apply(@Nullable BCMPriceModel input) {
                return (input.getScheme().getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Hidden) ||
                        (input.getScheme().getPricingStrategy().equals(PricingStrategy.Aggregation) &&
                         input.getScheme().getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Customer))) ? false : true;
            }
        }));
       return priceModels;
    }

    private BCMPriceModel getBcmPriceModel(ProductInstance productInstance, String action){
        ProductInstance asIs = getAsIsProductInstance(productInstance);
        List<PriceLine> priceLines = PriceSuppressStrategy.BCMSheet.suppressPriceCostLines(productInstance.getChargingSchemes(), productInstance.getPriceLines());
        List<PriceLine> asIsPriceLines = null != asIs ? PriceSuppressStrategy.BCMSheet.suppressPriceCostLines(asIs.getChargingSchemes(), asIs.getPriceLines()) : new LinkedList<PriceLine>();
        List<BCMPriceModel> priceModels = transformPriceLinesToPriceModel(productInstance, priceLines, action, asIsPriceLines);
        BCMPriceModel bcmPriceModel = null;
        if(priceModels.size() > 1){
            priceModels = newArrayList(Iterables.filter(priceModels, new Predicate<BCMPriceModel>() {
                @Override
                public boolean apply(@Nullable BCMPriceModel input) {
                    return (input.getScheme().getPricingStrategy().equals(PricingStrategy.Aggregation)
                            && input.getScheme().getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Sales)) ? true : false;
                }
            }));
        }
        if(!priceModels.isEmpty()){
            bcmPriceModel = priceModels.get(0);
        }
        return bcmPriceModel;
    }

    private List<BCMPriceModel> getBcmCostModel(ProductInstance productInstance, String action, String excludedCostlines) {
        List<BCMPriceModel> costLines = getCostLines(productInstance, action, excludedCostlines);
        return costLines;
    }

    private List<BCMPriceModel> aggregateCostModels(List<BCMPriceModel> costLines){
        List<BCMPriceModel> bcmCostModel = newArrayList();
        if(!costLines.isEmpty()){
            Double oneTimeEUP = 0.0;
            Double monthlyEUP = 0.0;
            for (BCMPriceModel costModel : costLines){
                if(costModel.getOneTimePriceLine() != null){
                    oneTimeEUP += Double.parseDouble(costModel.getOnetimeEUPPrice());
                }
                if(costModel.getMonthlyPriceLine() != null){
                    monthlyEUP += Double.parseDouble(costModel.getRecurringEUPPrice());
                }
            }
            DecimalFormat doubleFormatter = new DecimalFormat("#.##");
            bcmCostModel.add(new BCMPriceModel(doubleFormatter.format(oneTimeEUP), doubleFormatter.format(monthlyEUP)));
        }
        return bcmCostModel;
    }

    private List<ProductInstance> getCpeInstances(ProductInstance productInstance) {
        return newArrayList(Iterables.filter(productInstance.getChildren(), new Predicate<ProductInstance>() {
            @Override
            public boolean apply(@Nullable ProductInstance input) {
                return input.isCpe();
            }
        }));
    }

    public List<BCMDataRowModel> createRowModel(List<AbstractPricingSheetProductModel> productModels) {
        List<BCMDataRowModel> bcmRows = newArrayList();
        for (AbstractPricingSheetProductModel product : productModels) {
            bcmRows.addAll(fetchBcmRowModel(product));
        }
        return bcmRows;
    }

    public List<BCMDataRowModel> fetchBcmRowModel(AbstractPricingSheetProductModel product) {
        List<BCMDataRowModel> bcmRows = newArrayList();
        List<BCMPriceModel> priceModels = newArrayList();
        createBCMPriceModel(product.getProductInstance(), priceModels, product.getAction());
        for (BCMPriceModel priceModel : priceModels) {
            List<ProductInstance> productInstances = newArrayList();
            List<BCMPriceModel> costLines = newArrayList();
            ProductChargingScheme chargingScheme = priceModel.getScheme();
            ProductInstance rootProductInstance = priceModel.getProductInstance();
            if (ProductChargingScheme.PriceVisibility.Sales.equals(chargingScheme.getPriceVisibility()) && PricingStrategy.Aggregation.equals(chargingScheme.getPricingStrategy())) {
                fetchAggregatedSetProductInstances(rootProductInstance, chargingScheme.getSetAggregated(), productInstances);
                for (ProductInstance productInstance : productInstances) {
                    getCostLinesFromChildren(productInstance, costLines, product.getAction());
                }
            } else {
                productInstances.add(rootProductInstance);
                costLines.addAll(getCostLines(rootProductInstance, product.getAction(), ""));
            }
            BCMDataRowModel rowModel = new BCMDataRowModel(product.getProductInstance(), product.getQuoteOptionItem(), product.getSite(), productInstances, priceModel, costLines);
            bcmRows.add(rowModel);
        }
        return bcmRows;
    }

    public List<ProductInstance> fetchAggregatedSetProductInstances(ProductInstance productInstance, String aggregationSetName, List<ProductInstance> productInstanceList) {
        List<ProductChargingScheme> chargingSchemeForAggregationSet = productInstance.getChargingSchemeForAggregationSet(aggregationSetName);
        if (!chargingSchemeForAggregationSet.isEmpty()) {
            productInstanceList.add(productInstance);
        }
        for (ProductInstance child : productInstance.getChildren()) {
            fetchAggregatedSetProductInstances(child, aggregationSetName, productInstanceList);
        }
        return productInstanceList;
    }

    public List<BCMPriceModel> getCostLines(ProductInstance productInstance, String actionType, String excludedCostLines) {
        List<PriceLine> costLines = newArrayList();
        for (PriceLine priceLine : productInstance.getPriceLines()) {
            if (isNotEmpty(priceLine.getTariffType()) && priceLine.getTariffType().equalsIgnoreCase("Cost") && ("".equals(excludedCostLines) || !priceLine.getChargingSchemeName().contains(excludedCostLines))) {
                costLines.add(priceLine);
            }
        }
        return newArrayList(transformPriceLinesToPriceModel(productInstance, costLines, actionType, new LinkedList<PriceLine>()));
    }

    public List<BCMPriceModel> createBCMPriceModel(ProductInstance productInstance, List<BCMPriceModel> priceModels, String actionType) {
        List<PriceLine> priceLines = PriceSuppressStrategy.BCMSheet.suppressPriceCostLines(productInstance.getChargingSchemes(), productInstance.getPriceLines());
        priceModels.addAll(transformPriceLinesToPriceModel(productInstance, priceLines, actionType, new LinkedList<PriceLine>()));
        for (ProductInstance childProduct : productInstance.getChildren()) {
            createBCMPriceModel(childProduct, priceModels, actionType);
        }
        return priceModels;
    }

    public List<BCMPriceModel> transformPriceLinesToPriceModel(ProductInstance productInstance, List<PriceLine> priceLines, String action, List<PriceLine> asIsPriceLines) {
        List<BCMPriceModel> priceLineModelList = newArrayList();
        List<PriceLine> filteredPriceLines = newArrayList();
        List<String> asIsPriceLineIds = getPriceLineIds(asIsPriceLines);
        for(PriceLine priceLine: priceLines) {
            if(!asIsPriceLineIds.contains(priceLine.getId())) {
                filteredPriceLines.add(priceLine);
            }
        }
        Map<String, PriceLine> nonRecurringPriceModelMap = convertToMap(filter(filteredPriceLines, PricePredicates.OneTimeType));
        Map<String, PriceLine> recurringPriceModelMap = convertToMap(filter(filteredPriceLines, PricePredicates.RecurringType));
        List<String> foundInRecurringMap = newArrayList();
        for (String id : nonRecurringPriceModelMap.keySet()) {
            PriceLine oneTimePriceLine = nonRecurringPriceModelMap.get(id);
            ProductChargingScheme scheme = getChargingSchemeFor(oneTimePriceLine, productInstance.getChargingSchemes());
            priceLineModelList.add(new BCMPriceModel(oneTimePriceLine,
                                                     recurringPriceModelMap.get(id),
                                                     oneTimePriceLine.getTariffType(),
                                                     scheme,
                                                     productInstance, action));
            foundInRecurringMap.add(id);
        }

        for (String pmfId : recurringPriceModelMap.keySet()) {
            if(!foundInRecurringMap.contains(pmfId)){
                PriceLine recurringPriceLine = recurringPriceModelMap.get(pmfId);
                priceLineModelList.add(new BCMPriceModel(null,recurringPriceLine,
                                                         recurringPriceLine.getTariffType(),
                                                         getChargingSchemeFor(recurringPriceLine, productInstance.getChargingSchemes()),
                                                         productInstance,action));
            }
        }
        return priceLineModelList;
    }

    private List<String> getPriceLineIds(List<PriceLine> priceLines) {
        List<String> priceLineIds = new LinkedList<String>();
        for(PriceLine priceLine:priceLines) {
            priceLineIds.add(priceLine.getId());
        }
        return priceLineIds;
    }

    private ProductChargingScheme getChargingSchemeFor(PriceLine oneTimePriceLine, List<ProductChargingScheme> chargingSchemes) {
        for (ProductChargingScheme scheme : chargingSchemes) {
            if (scheme.getName().equalsIgnoreCase(oneTimePriceLine.getChargingSchemeName())) {
                return scheme;
            }
        }
        return null;
    }

    private Map<String, PriceLine> convertToMap(Collection<PriceLine> priceLines) {
        HashMap<String, PriceLine> priceMap = newHashMap();
        for (PriceLine priceLine : priceLines) {
            priceMap.put(priceLine.getPmfId(), priceLine);
        }
        return priceMap;
    }

    public List<BCMPriceModel> getCostLinesFromChildren(ProductInstance productInstance, List<BCMPriceModel> costLineModel, String actionType) {
        costLineModel.addAll(getCostLines(productInstance, actionType, ""));
        return costLineModel;
    }


}
