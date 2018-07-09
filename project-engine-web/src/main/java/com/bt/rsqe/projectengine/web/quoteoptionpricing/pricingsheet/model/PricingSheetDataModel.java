package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.AggregationSet;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PricingCaveat;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetSiteAddressStrategy;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetPriceModel.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

public class PricingSheetDataModel{

    private final ProjectDTO project;
    private final CustomerDTO customer;
    private final AccountManagerDTO accountManager;
    private final QuoteOptionDTO quoteOption;
    private List<PricingSheetContractProduct> contractProducts;
    private Set<PriceBookDTO> priceBooks;
    private Set<String> productFamilies;
    private List<PricingSheetProductModel> accessProducts = newArrayList();
    private final PricingSheetSiteAddressStrategy centralSite;
    private List<PricingSheetSpecialBidProduct> specialBidProducts = newArrayList();
    private final List<PricingSheetProductModel> products = newArrayList();
    private final List<BidManagerCommentsDTO> caveatsDetailList;
    static final String BUDGETARY = "Budgetary";
    static final String FIRM = "Firm";
    static final String NOT_PRICED = "Not Priced";
    static final String ONE_TIME = "ONE TIME";
    static final String EUP = "eup";
    static final String RRP = "rrp";
    static final String PTP = "ptp";

    PricingSheetDataModel(ProjectDTO project,
                          CustomerDTO customer,
                          AccountManagerDTO accountManager,
                          QuoteOptionDTO quoteOption,
                          SiteDTO centralSite,
                          List<PricingSheetProductModel> products,
                          List<PricingSheetSpecialBidProduct> specialBidProducts,
                          List<PricingSheetContractProduct> contractProducts, Set<PriceBookDTO> priceBooks, Set<String> productFamilies, List<PricingSheetProductModel> accessProducts, List<BidManagerCommentsDTO> bidManagerCommentsDTOs) {
        this.project = project;
        this.customer = customer;
        this.accountManager = accountManager;
        this.quoteOption = quoteOption;
        this.contractProducts = contractProducts;
        this.priceBooks = priceBooks;
        this.productFamilies = productFamilies;
        this.accessProducts = accessProducts;
        this.centralSite = new PricingSheetSiteAddressStrategy(centralSite);
        this.specialBidProducts = specialBidProducts;
        this.products.addAll( sort(products) );
        this.caveatsDetailList = bidManagerCommentsDTOs;
    }

    public Map<String, Object> map() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            Field[] fields = PricingSheetDataModel.class.getDeclaredFields();
            for (Field field : fields) {
                resultMap.put(field.getName(), field.get(this));
            }
        } catch (IllegalAccessException e) { /*Keep calm*/ }
        resultMap.put("model", this);
        return resultMap;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public CustomerDTO getCustomer() {
        return customer;
    }

    public AccountManagerDTO getAccountManager() {
        return accountManager;
    }

    public QuoteOptionDTO getQuoteOption() {
        return quoteOption;
    }

    public PricingSheetSiteAddressStrategy getCentralSite() {
        return centralSite;
    }

    public List<PricingSheetProductModel> getProducts() {
        return products;
    }

    public PricingSheetDataModel getModel() {
        return this;
    }

    public String getCurrentDate(){
        return new SimpleDateFormat(PricingSheetConstants.DATE_FORMAT).format(new Date());
    }

    public List<PricingSheetSpecialBidProduct> getSpecialBidProducts() {
        return specialBidProducts;
    }

    public List<PricingSheetContractProduct> getContractProducts() {
        return contractProducts;
    }

    public List<PricingSheetProductModel> getAccessProducts() {
        return newArrayList(Iterables.filter(accessProducts,new Predicate<PricingSheetProductModel>() {
            @Override
            public boolean apply(@Nullable PricingSheetProductModel model) {
                return model.isAccess();
            }
        }));
    }

    public List<PricingSheetPriceModel> getAllSummaryPrices(){
        ArrayList<PricingSheetPriceModel> priceModelList = newArrayList();
        for (PricingSheetSpecialBidProduct product : specialBidProducts) {
           priceModelList.addAll(filter(product.getAllPriceLines(PriceSuppressStrategy.SummarySheet), notDummyPriceModelPredicate()));
        }
        for (PricingSheetProductModel product : products) {
           priceModelList.addAll(filter(product.getAllPriceLines(PriceSuppressStrategy.SummarySheet), notDummyPriceModelPredicate()));
        }
/*        for (PricingSheetContractProduct product : contractProducts) {
            priceModelList.addAll(filter(product.getAllDetailedPricingSheetChildPriceModels(priceType), notDummyPriceModelPredicate()));
        }*/
        if( priceModelList.size() == 0) {
            priceModelList.add(PricingSheetPriceModel.dummyPriceModel());
        }
        return priceModelList;
    }

    public List<PricingSheetPriceModel> getAllDetailedPrices(){
        ArrayList<PricingSheetPriceModel> priceModelList = newArrayList();
        for (PricingSheetProductModel product : products) {
            priceModelList.addAll(filter(product.getAllPriceLines(PriceSuppressStrategy.DetailedSheet), notDummyPriceModelPredicate()));
        }

        if( priceModelList.size() == 0) {
            priceModelList.add(PricingSheetPriceModel.dummyPriceModel());
        }
        return priceModelList;
    }

    private double calculateAggregatedPrices(double price, AbstractPricingSheetProductModel productModel, String priceFrequency, String priceType, boolean siteInstallable, String chargeType){
        for (PricingSheetPriceModel priceModel : getAggregatedPrices(productModel, PriceSuppressStrategy.DetailedSheet)) {
                if (!(productModel.isSiteInstallable() ^ siteInstallable)) {
                    if (priceFrequency.equalsIgnoreCase(ONE_TIME)) {
                        if (AbstractPricingSheetProductModel.isPriceApplicableFor(priceModel, priceType, productModel.pricingClient)) {
                            price += chargeType.equalsIgnoreCase(PTP) ? priceModel.getNonRecurringPtpPrice().doubleValue() : priceModel.getNonRecurringEupPrice().doubleValue();
                        }
                    } else {
                        if (AbstractPricingSheetProductModel.isPriceApplicableFor(priceModel, priceType, productModel.pricingClient)) {
                            price += chargeType.equalsIgnoreCase(PTP) ? priceModel.getRecurringPtpPrice().doubleValue() : priceModel.getRecurringEupPrice().doubleValue();
                        }
                    }
                }
            }
        return price;
    }

    public double getAggregatedPrice(String priceFrequency, boolean siteInstallable, String priceType, boolean isSpecialBid, String chargeType){
        double price = 0.0;
        // If the products are special bid loop the special bid product, else loop the standard product list then the access products
        for (AbstractPricingSheetProductModel productModel : isSpecialBid? getSpecialBidProducts(): getProducts()) {
            price = calculateAggregatedPrices(price, productModel, priceFrequency, priceType, siteInstallable, chargeType);
        }
        return price;
    }

    public double getAggregatedSiteLevelTotalManagementCharges(String priceFrequency, boolean isSpecialBid, String priceType, String chargeType){
        double price = 0.0;
        for (AbstractPricingSheetProductModel productModel : isSpecialBid? getSpecialBidProducts(): getProducts()) {
             price = calculateAggregatedSiteLevelManagementCharge(price, productModel, priceFrequency, priceType, chargeType);
        }
        return price;
    }

    private double calculateAggregatedSiteLevelManagementCharge(double price, AbstractPricingSheetProductModel productModel, String priceFrequency, String priceType, String chargeType){
        if(productModel.isSiteInstallable()){
            if(chargeType.equalsIgnoreCase(EUP)){
                price += productModel.getDetailedSheetSiteLevelManagementEupPriceForProductInstanceId(productModel.productInstance.getProductInstanceId().getValue(), priceFrequency, priceType);
             }else{
                 price += productModel.getIndirectUserDetailedSheetSiteLevelManagementPtpPriceForProductInstanceId(productModel.productInstance.getProductInstanceId().getValue(), priceFrequency, priceType);
             }
         }
        return price;
    }

    public double getAggregatedSiteLevelManagementCharges(String priceFrequency, boolean isSpecialBid, boolean isAccess, String priceType, String chargeType){
        double price = 0.0;
            for (AbstractPricingSheetProductModel productModel : isSpecialBid? getSpecialBidProducts(): getProducts()) {
                 price = calculateAggregatedSiteLevelManagementCharge(price, productModel, priceFrequency, priceType, chargeType);
            }
        return price;
    }

     private List<PricingSheetPriceModel> getAggregatedPrices(AbstractPricingSheetProductModel productModel, PriceSuppressStrategy suppressStrategy) {
        final List<ProductChargingScheme> aggregatedSchemes = getAggregatedSchemes(productModel);
        return newArrayList(Iterables.filter(filter(productModel.getAllPriceLines(suppressStrategy), notDummyPriceModelPredicate()), new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(@Nullable final PricingSheetPriceModel priceModel) {
                return Iterables.tryFind(aggregatedSchemes, new Predicate<ProductChargingScheme>() {
                    @Override
                    public boolean apply(@Nullable ProductChargingScheme scheme) {
                        return scheme.getName().equalsIgnoreCase(priceModel.getChargingSchemeName());
                    }
                }).isPresent();
            }
        }));
    }

    private List<ProductChargingScheme> getAggregatedSchemes(AbstractPricingSheetProductModel productModel) {
       return newArrayList(Iterables.filter(productModel.getAllChargingSchemes(), new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(@Nullable ProductChargingScheme scheme) {
                return scheme.getAggregationSet().equalsIgnoreCase(AggregationSet.NIL.toString());
            }
        }));
    }

    public BigDecimal getSumOfAllSummaryPrices(String priceType, boolean withContract, String recurringOrNonRecurringEupPrice) {

        BigDecimal total = BigDecimal.ZERO;
        List<PricingSheetPriceModel> allSummaryPrices = getAllSummaryPrices(priceType, withContract);
        for (PricingSheetPriceModel priceLine : allSummaryPrices) {
            if ("nonRecurringEupPrice".equalsIgnoreCase(recurringOrNonRecurringEupPrice)) {
                final BigDecimal nonRecurringEupPrice = new BigDecimal(priceLine.getNonRecurringEupPrice().toString());
                total = total.add(nonRecurringEupPrice);
            } else if ("recurringEupPrice".equalsIgnoreCase(recurringOrNonRecurringEupPrice)) {
                final BigDecimal recurringEupPrice = new BigDecimal(priceLine.getRecurringEupPrice().toString());
                total = total.add(recurringEupPrice);
            }
        }
        return total;
    }

    public List<PricingSheetPriceModel> getAllSummaryPrices(String priceType,boolean withContract) {
        List<PricingSheetPriceModel> allSummaryPrices = getAllSummaryPrices();
        if(withContract){
            enhancePriceModelsWithContractTerm(allSummaryPrices);
        }
        ArrayList<PricingSheetPriceModel> priceModels = newArrayList();
        if (allSummaryPrices.size() == 0) {
            priceModels.add(PricingSheetPriceModel.dummyPriceModel());
        } else {
            for (PricingSheetPriceModel pricingSheetPriceModel : allSummaryPrices) {
                if (AbstractPricingSheetProductModel.isPriceApplicableFor(pricingSheetPriceModel, priceType, getPricingClient())) {
                    priceModels.add(pricingSheetPriceModel);
                }
            }
        }
       return priceModels;
    }

    private void enhancePriceModelsWithContractTerm(List<PricingSheetPriceModel> allSummaryPrices) {
        for(PricingSheetPriceModel priceModel: allSummaryPrices){
            if(isNotNull(priceModel.getRentalPrice())) {
                final PriceLine priceLine = priceModel.getRentalPrice().multiplyPricesBy(priceModel.getRemainingContractTerm());
                priceModel.setRentalPrice(priceLine);
            }
        }
    }

    private PricingClient getPricingClient() {
        return !products.isEmpty() ? products.get(0).pricingClient :
            !specialBidProducts.isEmpty() ? specialBidProducts.get(0).pricingClient :
                !contractProducts.isEmpty() ? contractProducts.get(0).pricingClient : null;
    }

    public String fetchPricingStatus(){
        List<AbstractPricingSheetProductModel> allProducts = newArrayList();
        List<String> pricingStatusList = newArrayList();
        allProducts.addAll(specialBidProducts);
        allProducts.addAll(products);
        allProducts.addAll(accessProducts);
       for(AbstractPricingSheetProductModel model: allProducts){
           if(!pricingStatusList.contains((model.getPricingStatusForPricingSheet()))){
               pricingStatusList.add(model.getPricingStatusForPricingSheet());
           }
       }

        if(pricingStatusList.size()== 1 && !pricingStatusList.get(0).equalsIgnoreCase(BUDGETARY) && !pricingStatusList.get(0).equalsIgnoreCase(NOT_PRICED)){
            return FIRM;
        }
        if(pricingStatusList.size()== 0)
        {
            return FIRM;
        }

        return BUDGETARY;
    }

    public List<PricingCaveat> getPricingCaveats(){
        List<PricingCaveat> pricingCaveats = newArrayList();
       for(PricingSheetProductModel productModel: products){
           pricingCaveats.addAll(productModel.getPricingCaveats());
           for(PricingSheetProductModel child: productModel.getChildren()){
               pricingCaveats.addAll(child.getPricingCaveats());
           }
       }
       return pricingCaveats;
    }

    public String getPriceBooks(String version){
        Set<String> priceBookName = newHashSet();
       for(PriceBookDTO priceBookDTO: priceBooks){
         if(RRP.equalsIgnoreCase(version)){
             priceBookName.add(priceBookDTO.eupPriceBook);
         } else{
             priceBookName.add(priceBookDTO.ptpPriceBook);
         }
       }
       return StringUtils.join(priceBookName.toArray(),',');
    }

    public String getProductNames(){
        return StringUtils.join(productFamilies.toArray(),',');
    }


    public BigDecimal getContractSummaryTotal(String priceType, boolean withContract, String recurringOrNonRecurringEupPrice) {
        BigDecimal total = BigDecimal.ZERO;
        for (PricingSheetContractProduct contractProduct : contractProducts) {
            total = total.add(contractProduct.getRecurringOrNonRecurringSummaryTotal(priceType,withContract,recurringOrNonRecurringEupPrice));
        }
        return total;
    }

    public BigDecimal getContractNewNonRecurringSummaryTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (PricingSheetContractProduct contractProduct : contractProducts) {
            total = total.add(contractProduct.getNewNonRecurringSummaryTotal());
        }
        return total;
    }

    public BigDecimal getContractNewRecurringSummaryTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (PricingSheetContractProduct contractProduct : contractProducts) {
            total = total.add(contractProduct.getNewRecurringSummaryTotal());
        }
        return total;
    }

    public BigDecimal getContractExistingNonRecurringSummaryTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (PricingSheetContractProduct contractProduct : contractProducts) {
            total = total.add(contractProduct.getExistingNonRecurringSummaryTotal());
        }
        return total;
    }

    public BigDecimal getContractExistingRecurringSummaryTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (PricingSheetContractProduct contractProduct : contractProducts) {
            total = total.add(contractProduct.getExistingRecurringSummaryTotal());
        }
        return total;
    }

    private List<PricingSheetProductModel> sort(List<PricingSheetProductModel> pricingSheetProductModels) {

        List<PricingSheetProductModel> orderedProductModels = newArrayList();
        for (final PricingSheetProductModel sellableProduct : pricingSheetProductModels) {

            if( !orderedProductModels.contains(sellableProduct)) {

                final Set<ProductInstanceId> assetGroup = sellableProduct.getProductInstance().flattenMeAndMyRelatedInstances().keySet();
                Collection<PricingSheetProductModel> relatedProductModels = filter(pricingSheetProductModels, new Predicate<PricingSheetProductModel>() {
                    @Override
                    public boolean apply(PricingSheetProductModel input) {
                        return assetGroup.contains(new ProductInstanceId(input.getProductInstanceID())) && !input.equals(sellableProduct);
                    }
                });

                orderedProductModels.removeAll(relatedProductModels);  //remove if related product already added before its owner.
                orderedProductModels.add(sellableProduct);
                orderedProductModels.addAll(relatedProductModels);
            }
        }
        return orderedProductModels;
    }

    public List<PricingSheetProductModel> getSummarySheetProducts(){
        return newArrayList(Iterables.filter(products, new Predicate<PricingSheetProductModel>() {
            @Override
            public boolean apply(PricingSheetProductModel product) {
                if(product.hasCustomerVisibleChargingSchemes())
                return true;
                else{
                    for(PricingSheetProductModel child:product.getAllChildren()){
                        if(child.hasCustomerVisibleChargingSchemes())
                            return true;
                    }
                }
                return false;
            }
        }));
    }

    public List<PricingSheetSpecialBidProduct> getSummarySheetSpecialBidProducts(){
        return newArrayList(Iterables.filter(specialBidProducts, new Predicate<PricingSheetSpecialBidProduct>() {
            @Override
            public boolean apply(PricingSheetSpecialBidProduct product) {
                return product.hasCustomerVisibleChargingSchemes();
            }
        }));
    }

    public BidManagerCommentsDTO getBidDetails() {
         if(isNull(caveatsDetailList) || caveatsDetailList.isEmpty()){
            return new BidManagerCommentsDTO();
         }
        return Iterables.getLast(caveatsDetailList);
    }

    public String getContractResignValue(){

        Optional<PricingSheetProductModel> pricingSheetProductModelOptional = Iterables.tryFind(products, new Predicate<PricingSheetProductModel>() {
            @Override
            public boolean apply(PricingSheetProductModel input) {
                 if(input.getProductInstance().isContractResigned()){
                    return true;
                }else{
                     for(PricingSheetProductModel child:input.getAllChildren()){
                         if(child.getProductInstance().isContractResigned())
                             return true;
                     }
                     for(PricingSheetProductModel related:input.getRelatedTo()){
                         if(related.getProductInstance().isContractResigned())
                             return true;
                     }
                 }
                return false;
            }
        });

        return pricingSheetProductModelOptional.isPresent() ? Constants.YES :Constants.NO;
    }

}
