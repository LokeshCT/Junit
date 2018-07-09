package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.ContractTermHelper;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.parameters.OrderType;
import com.bt.rsqe.domain.order.OrderItemItemPrice;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.Price;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PriceLineToProductInstanceMapper;
import com.bt.rsqe.domain.project.PricingCaveat;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.StencilIdInstanceCharacteristicNotFoundException;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.factory.PricePredicates;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PriceType;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PriceType.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetPriceModel.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Collections2.*;
import static com.google.common.collect.Lists.*;

public class AbstractPricingSheetProductModel {
    private static final String ONE_TIME = "one time";
    private static final String RECURRING = "recurring";
    public ProductInstance productInstance;
    public QuoteOptionItemDTO quoteOptionItem;
    public SiteDTO site;
    public MergeResult mergeResult;
    public PricingClient pricingClient;
    private Optional<ProductInstance> asIs;
    public CaveatResource caveatResource;
    List<String> leg1Aggregations = newArrayList("Access","Aacc","ICGAccessCancellation","CustomAccess");
    List<String> leg2Aggregations =newArrayList("SecondaryAccess","SecAaCC","ICGSecAccessCancellation", "CustomAccess");
    String leg1PenaltyAggregation ="Aacc1";
    String leg2PenaltyAggregation ="SecAacc1";
    List<String> cpeAggregations =newArrayList("CPE","CPECease","TotalCPECancellation");
    List<String> serviceList = newArrayList("M0304296","M0304298","M0315765","M0315766","M0316053","M0316054");
    List<String> customAccessCircuitPrices = newArrayList("M0318789", "M0318954");
    List<String> customAccessCircuitPenaltyPrices = newArrayList("M0318957", "M0318958");

    public AbstractPricingSheetProductModel(SiteDTO site, ProductInstance productInstance, QuoteOptionItemDTO quoteOptionItem, MergeResult mergeResult, PricingClient pricingClient, Optional<ProductInstance> asIs) {
        this.site = site;
        this.productInstance = productInstance;
        this.quoteOptionItem = quoteOptionItem;
        this.mergeResult = mergeResult;
        this.pricingClient = pricingClient;
        this.asIs = asIs;
    }

    public String getSiteId() {
        return this.site.bfgSiteID;

    }

    public String getSCode() {
        return this.productInstance.getProductIdentifier().getProductId();
    }

    public String getProductName() {
        return this.productInstance.getProductName();
    }

    public boolean isCpe() {
        return this.productInstance.isCpe();
    }

    public String getRelationName(String relatedInstanceId) {
        return this.productInstance.getRelationshipNameWith(relatedInstanceId);
    }

    public String getProductVersion() {
        return this.productInstance.getProductIdentifier().getVersionNumber();
    }

    public String getProductInstanceID() {
        return this.productInstance.getProductInstanceId().getValue();
    }

    public String getProductCategoryName() {
        return this.getProductInstance().getProductGroupName().value();
    }

    public Optional<ProductInstance> getAsIs() {
        return asIs;
    }

    public boolean hasAccessProducts() {
        final Optional<ProductSalesRelationshipInstance> optionalAccess = Iterables.tryFind(productInstance.getRelationships(), new Predicate<ProductSalesRelationshipInstance>() {
            @Override
            public boolean apply(ProductSalesRelationshipInstance input) {
                final ProductOffering productOffering = input.getRelatedProductInstance().getProductOffering();
                return productOffering.hasApeFlag() && SimpleProductOfferingType.Bearer.equals(productOffering.getSimpleProductOfferingType());
            }
        });
        return optionalAccess.isPresent();
    }

    public boolean hasCPE() {
        return Iterables.tryFind(productInstance.getRelationships(), new Predicate<ProductSalesRelationshipInstance>() {
            @Override
            public boolean apply(ProductSalesRelationshipInstance input) {
                final ProductOffering productOffering = input.getRelatedProductInstance().getProductOffering();
                return input.getSalesRelationship().isRelatedToRelationship() && productOffering.isCpe();
            }
        }).isPresent();
    }

    public boolean isCustomAccess() {
        return  "CustomAccess".equals(productInstance.getInstanceCharacteristicValue(ProductOffering.REQUESTTYPE));
    }

    public boolean hasNoOtherPricableSWLicences(String priceType){
        for(PricingSheetProductModel model: getRelatedTo()){
           if("SW Licence".equalsIgnoreCase(model.getInstanceCharacteristic("PART TYPE")) && (arePricelinesPresent(priceType, model))){
               return false;
           }
        }
        return true;
    }

    private boolean arePricelinesPresent(String priceType, PricingSheetProductModel model) {
        return !filter(model.getDetailedPriceLines(priceType), notDummyPriceModelPredicate()).isEmpty();
    }

    public String getAction() {
        ChangeType changeType = mergeResult.changeFor(productInstance);
        switch (changeType) {
            case ADD:
                return OrderType.PROVIDE.name();
            case UPDATE:
                return OrderType.MODIFY.name();
            case DELETE_ROOT:
            case DELETE:
                return OrderType.CEASE.name();
            case NONE:
                return OrderType.MODIFY.name();
            default:
                return ChangeType.NONE.name();
        }
    }

    public String getSummary(){
        String description = "";
        for (Attribute attribute: this.productInstance.getProductOffering().getAttributes()) {
            if (attribute.isVisibleInSummary()) {
                try {
                    if (this.productInstance.getInstanceCharacteristic(attribute.getName()).hasValue()) {
                        description += this.productInstance.getInstanceCharacteristic(attribute.getName()).getValue() + ", ";
                    }
                } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                    // Instance Characteristic may not exist
                }
            }
        }
        return description.contains(",") ? description.substring(0, description.lastIndexOf(",")) : description;
    }

    public String getPriceType(PriceLine priceLine) {
        ProductInstance itemPrice = new PriceLineToProductInstanceMapper(priceLine).map();
        ChangeType changeType = mergeResult.changeFor(new OrderItemItemPrice(itemPrice));
        return changeType.getValue();
    }

    public List<String> getPriceTypes() {
        final String action = getAction();
        List<String> priceTypes = newArrayList();
        if (action.equalsIgnoreCase(OrderType.PROVIDE.name()) || !asIs.isPresent()) {
            priceTypes.add(NEW.name());
        } else {
            priceTypes.addAll(newArrayList(NEW.name(), EXISTING.name()));
        }
        if(quoteOptionItem.isIfc){
            priceTypes.add(NEW_IFC.name());
        }
        return priceTypes;
    }

    public String getLineItemOrderStatus() {
        return this.getQuoteOptionItem().orderStatus.name();
    }

    public String getContractTerm() {
        String contractTerm = this.getProductInstance().getContractTerm();
        return !isNullOrEmpty(contractTerm) ? contractTerm : this.getQuoteOptionItem().contractTerm;
    }

    public String getRemainingContractTerm() {
        if(this.getAsIs().isPresent()){
            ProductInstance asIsInstance = this.asIs.get();
            return String.valueOf(ContractTermHelper.getMaxRemainingMonths(asIsInstance.getEarlyBillingStartDate(),asIsInstance.getContractTerm()));
        }else{
            return getContractTerm();
        }
    }

    public String getInstanceCharacteristic(String attributeName) {
        String accessSpeed = "";
        try {
            if (attributeName.equalsIgnoreCase(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE) && productInstance.hasInstanceCharacteristic(new AttributeName(ProductOffering.ACCESS_UPSTREAM_SPEED_DISPLAY_VALUE))){

                accessSpeed = productInstance.getInstanceCharacteristic(ProductOffering.ACCESS_UPSTREAM_SPEED_DISPLAY_VALUE).getStringValue() + "/"
                        + productInstance.getInstanceCharacteristic(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE).getStringValue();
            } else{
                return productInstance.getInstanceCharacteristic(attributeName).getStringValue();
            }

        } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
            return attributeName.equalsIgnoreCase(ProductOffering.QUANTITY)?"1":StringUtils.EMPTY;
        }
        return accessSpeed;
    }
    public String getQuantityInstanceCharacteristic(String attributeName, String priceType) {
        String result = null;
        try {
            switch (PriceType.valueOf(priceType)) {
                case NEW:
                    result = getInstanceCharacteristic(attributeName);
                    break;
                case EXISTING:
                    result = (String) mergeResult.getChangeTracker().previousAttributeValueFor(productInstance, attributeName);
                    break;
                case NEW_IFC:
                    result = (String) mergeResult.getChangeTracker().previousAttributeValueFor(productInstance, attributeName);
                    break;
            }

        } catch (Exception e) {
            result = null;
        }
        if (null == result) {
            result = getInstanceCharacteristic(attributeName);
        }
        return result;
    }

    public String getPricingStatusForPricingSheet() {
        PricingStatus priceStatus = productInstance.getPricingStatus();
        switch (priceStatus) {
            case ICB_BUDGETARY:
            case BUDGETARY:
                return PricingStatus.BUDGETARY.getDescription();
            default:
                return priceStatus.getDescription();
        }
    }

    public List<PricingSheetProductModel> getAllChildren() {
        List<PricingSheetProductModel> childProductModelList = newArrayList();
        Set<ProductInstance> children = productInstance.getChildren();
        for (ProductInstance child : children) {
            PricingSheetProductModel childProductModel = new PricingSheetProductModel(site, child, quoteOptionItem, mergeResult, caveatResource, pricingClient, Optional.<ProductInstance>absent());
            childProductModelList.add(childProductModel);
            childProductModelList.addAll(childProductModel.getAllChildren());
        }
        return childProductModelList;
    }

       public List<PricingSheetProductModel> getChildren() {
        List<PricingSheetProductModel> childs = newArrayList();
        Set<ProductInstance> children = productInstance.getChildren();
        for (ProductInstance child : children) {
            if (child.getPricingStatus().isPriced()) {
                childs.add(new PricingSheetProductModel(site, child, quoteOptionItem, mergeResult, caveatResource, pricingClient, null));
            }
        }
        return childs;
    }

   public List<PricingSheetProductModel> getCPEs(){
        List<PricingSheetProductModel> CPEs = newArrayList();
       for(ProductSalesRelationshipInstance relation : productInstance.getRelationships()){
           final ProductInstance relatedProductInstance = relation.getRelatedProductInstance();
           if(relatedProductInstance.isCpe()){
              CPEs.add(new PricingSheetProductModel(site, relatedProductInstance, quoteOptionItem, mergeResult, caveatResource, pricingClient, null));
          }
          CPEs.addAll(getCPEsFor(relatedProductInstance));
       }

        return CPEs;
    }

    private List<PricingSheetProductModel> getCPEsFor(ProductInstance instance) {
        List<PricingSheetProductModel> CPEs = newArrayList();
        for(ProductSalesRelationshipInstance relation : instance.getRelationships()){
            final ProductInstance relatedProductInstance = relation.getRelatedProductInstance();
            if(relatedProductInstance.isCpe()){
             CPEs.add(new PricingSheetProductModel(site, relatedProductInstance, quoteOptionItem, mergeResult, caveatResource, pricingClient, null));
           }
            CPEs.addAll(getCPEsFor(relatedProductInstance));
        }
       return CPEs;
    }


    public PricingSheetProductModel getChild(String sCode) {
        for (PricingSheetProductModel child : getChildren()) {
            if (sCode.equals(child.getSCode())) {
                return child;
            }
        }
        return null;
    }

    public List<PricingSheetProductModel> getAccessProducts() {
        List<PricingSheetProductModel> accessProducts = newArrayList();
        for(ProductSalesRelationshipInstance salesRelationshipInstance : productInstance.getRelationships()){
            ProductInstance instance = salesRelationshipInstance.getRelatedProductInstance();
            if(instance.getProductOffering().hasApeFlag() &&
               instance.getProductOffering().isBearer()){
                accessProducts.add(new PricingSheetProductModel(site, instance, quoteOptionItem, mergeResult, caveatResource, pricingClient, null));
            }
		}
        return accessProducts;
    }

    public List<PricingSheetProductModel> getRelatedTo() {
        List<PricingSheetProductModel> relatedTo = newArrayList();
        for (ProductInstance related : productInstance.relatedProductInstances()) {
            if (related.getPricingStatus().isPriced()) {
                relatedTo.add(new PricingSheetProductModel(site, related, quoteOptionItem, mergeResult, caveatResource, pricingClient, null));
            }
        }
        return relatedTo;
    }

    public List<PricingSheetPriceModel> transformToPricingSheetPriceModel(Collection<PriceLine> priceLines) {
        ArrayList<PricingSheetPriceModel> priceLineModelList = newArrayList();
        Multimap<String, PriceLine> priceLineMultimap = convertToPmfIdMap(priceLines);

        for (String key : priceLineMultimap.keySet()) {
            final PriceLine oneTimePrice = filterPriceline(priceLineMultimap.get(key), PricePredicates.OneTimePriceType);
            final PriceLine rentalPrice = filterPriceline(priceLineMultimap.get(key), PricePredicates.RecurringPriceType);
            final PriceLine usagePrice = filterPriceline(priceLineMultimap.get(key), PricePredicates.UsagePriceType);
            final String pmfId = newArrayList(priceLineMultimap.get(key)).get(0).getPmfId();
            priceLineModelList.add(new PricingSheetPriceModel(pmfId,
                                                              oneTimePrice,
                                                              rentalPrice,
                                                              usagePrice,
                                                              getContractTerm(), getPriceType(isNotNull(oneTimePrice)?oneTimePrice:
                                                                                                         isNotNull(rentalPrice)?rentalPrice:usagePrice),
                                                              productInstance, asIs));
        }

        if (priceLineModelList.size() == 0) {
            priceLineModelList.add(PricingSheetPriceModel.dummyPriceModel());
        }
        return priceLineModelList;
    }

    private PriceLine filterPriceline(Collection<PriceLine> priceLines, Predicate<PriceLine> predicates) {
        List<PriceLine> filter = newArrayList(filter(priceLines, predicates));
        // there can be only one price for a price type(recurring)
        return filter.size() > 0 ? filter.get(0) : null;
    }

    private Multimap<String, PriceLine> convertToPmfIdMap(Collection<PriceLine> priceLines) {
        Multimap<String, PriceLine> priceLineMap = HashMultimap.create();
        for (PriceLine priceLine : priceLines) {
            priceLineMap.put(priceLine.getPmfId()+getPriceType(priceLine), priceLine);
        }
        return priceLineMap;
    }

    public List<PricingSheetPriceModel> getPriceLines(PriceSuppressStrategy priceSuppressStrategy) {
        Collection<PriceLine> priceLines = priceSuppressStrategy.suppressPriceCostLines(productInstance.getChargingSchemes(), productInstance.getPriceLines());
        return transformToPricingSheetPriceModel(priceLines);
    }

    public List<PricingSheetPriceModel> getAllPriceLines(PriceSuppressStrategy priceSuppressStrategy) {
        ArrayList<PricingSheetPriceModel> priceLineModelList = newArrayList();
        priceLineModelList.addAll(filter(this.getPriceLines(priceSuppressStrategy), notDummyPriceModelPredicate()));

        for (PricingSheetProductModel childProductModel : getAllChildren()) {
            priceLineModelList.addAll(filter(childProductModel.getPriceLines(priceSuppressStrategy), notDummyPriceModelPredicate()));
        }

        if (priceLineModelList.size() == 0) {
            //Dummy price line is to keep excel row for a Product even if the product doesn't have price lines. TODO: Need to remove this by modifying the jx:forEach in xls template
            priceLineModelList.add(PricingSheetPriceModel.dummyPriceModel());
        }
        return priceLineModelList;
    }

    public List<PricingSheetPriceModel> getAllChildPriceLines(PriceSuppressStrategy priceSuppressStrategy) {
        ArrayList<PricingSheetPriceModel> priceLineModelList = newArrayList();

        for (PricingSheetProductModel childProductModel : getAllChildren()) {
            priceLineModelList.addAll(filter(childProductModel.getPriceLines(priceSuppressStrategy), notDummyPriceModelPredicate()));
        }

        if (priceLineModelList.size() == 0) {
            priceLineModelList.add(PricingSheetPriceModel.dummyPriceModel());
        }
        return priceLineModelList;
    }

    public double getDetailedSheetTotalEupPriceForProductInstanceId(String productInstanceId, String priceFrequency, String priceType) {
        return getTotalEupPriceForProductInstanceId(productInstanceId, PriceSuppressStrategy.DetailedSheet, priceFrequency, priceType);
    }

    public double getDetailedSheetSiteLevelManagementEupPriceForProductInstanceId(String productInstanceId, String priceFrequency, String priceType) {
        return getSiteLevelManagementEupPriceForProductInstanceId(productInstanceId, PriceSuppressStrategy.DetailedSheet, priceFrequency,priceType);
    }

    public double getDetailedSheetSpecialBidEupPriceForProductInstanceId(String productInstanceId, String priceFrequency, String priceType) {
        return getSpecialBidEupPriceForProductInstanceId(productInstanceId, PriceSuppressStrategy.DetailedSheet, priceFrequency, priceType);
    }

    public double getIndirectUserDetailedSheetTotalPtpPriceForProductInstanceId(String productInstanceId, String priceFrequency, String priceType) {
        return getIndirectUserTotalEupPriceForProductInstanceId(productInstanceId, PriceSuppressStrategy.DetailedSheet, priceFrequency, priceType);
    }

    public double getIndirectUserDetailedSheetSiteLevelManagementPtpPriceForProductInstanceId(String productInstanceId, String priceFrequency, String priceType) {
        return getIndirectUserSiteLevelManagementEupPriceForProductInstanceId(productInstanceId, PriceSuppressStrategy.DetailedSheet, priceFrequency, priceType);
    }

    public double getIndirectUserDetailedSheetSpecialBidPtpPriceForProductInstanceId(String productInstanceId, String priceFrequency) {
        return getIndirectUserSpecialBidEupPriceForProductInstanceId(productInstanceId, PriceSuppressStrategy.DetailedSheet, priceFrequency);
    }

    public double getTotalEupPriceForProductInstanceId(String productInstanceId, PriceSuppressStrategy suppressStrategy, String priceFrequency, String priceType) {
        List<ProductChargingScheme> productChargingSchemeList = productInstance.getChargingSchemes();
        List<PricingSheetPriceModel> priceLines = newArrayList(filter(getPriceLines(suppressStrategy), notDummyPriceModelPredicate()));
        List<String> customerSalesType = new ArrayList();
        for (ProductChargingScheme scheme : productChargingSchemeList) {
            if (scheme.getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Customer)) {
                customerSalesType.add(scheme.getName());
            }
        }
        for (PricingSheetPriceModel priceLine : priceLines) {
            if (isPriceApplicableFor(priceLine, priceType, pricingClient) && productInstanceId.equals(productInstance.getProductInstanceId().getValue())) {
                for (String schemeName : customerSalesType) {
                    if (schemeName.equals(priceLine.getOneTimePrice().getChargingSchemeName())) {
                        if (priceFrequency.equalsIgnoreCase(ONE_TIME)) {
                            return priceLine.getNonRecurringEupPrice().doubleValue();
                        } else if (priceFrequency.equalsIgnoreCase(RECURRING)) {
                            return priceLine.getRecurringEupPrice().doubleValue();
                        }
                    }
                }
            }
        }
        return 0.0;
    }

    public double getSiteLevelManagementEupPriceForProductInstanceId(String productInstanceId, PriceSuppressStrategy suppressStrategy, String priceFrequency, String priceType) {
        if(hasAccessProducts() || childrenHasAccessProducts()) {
            return 0.00;
        }
        List<ProductChargingScheme> productChargingSchemeList = getAllChargingSchemes();
        List<PricingSheetPriceModel> priceLines = newArrayList(filter(getPriceLines(suppressStrategy), notDummyPriceModelPredicate()));
        List<String> customerSalesType = new ArrayList();
        for (ProductChargingScheme scheme : productChargingSchemeList) {
            if (scheme.getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Sales)) {
                customerSalesType.add(scheme.getName());
            }
        }
        for (PricingSheetPriceModel priceLine : priceLines) {
            if (isPriceApplicableFor(priceLine, priceType, pricingClient) && productInstanceId.equals(productInstance.getProductInstanceId().getValue())) {
                for (String schemeName : customerSalesType) {
                    if (isNotNull(priceLine.getOneTimePrice()) && schemeName.equals(priceLine.getOneTimePrice().getChargingSchemeName())) {
                        if (priceFrequency.equals(ONE_TIME)) {
                            return priceLine.getNonRecurringEupPrice().doubleValue();
                        } else if (priceFrequency.equals(RECURRING)) {
                            return priceLine.getRecurringEupPrice().doubleValue();
                        }
                    }
                }
            }
        }
        return 0.00;
    }

    private boolean childrenHasAccessProducts() {
        for(PricingSheetProductModel child:getChildren()){
            if(child.hasAccessProducts()){
                return true;
            }

        }
        return false;
    }

    public double getSpecialBidEupPriceForProductInstanceId(String productInstanceId, PriceSuppressStrategy suppressStrategy, String priceFrequency, String priceType) {
        List<ProductChargingScheme> productChargingSchemeList = productInstance.getChargingSchemes();
        List<PricingSheetPriceModel> priceLines = newArrayList(filter(getPriceLines(suppressStrategy), notDummyPriceModelPredicate()));
        List<String> customerSalesType = new ArrayList();
        for (ProductChargingScheme scheme : productChargingSchemeList) {
            if (scheme.getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Customer)
                && scheme.getPricingStrategy().equals(PricingStrategy.SpecialBid)) {
                customerSalesType.add(scheme.getName());
            }
        }
        for (PricingSheetPriceModel priceLine : priceLines) {
            if (isPriceApplicableFor(priceLine, priceType, pricingClient)&& productInstanceId.equals(productInstance.getProductInstanceId().getValue())) {
                for (String schemeName : customerSalesType) {
                    if (schemeName.equals(priceLine.getOneTimePrice().getChargingSchemeName())) {
                        if (priceFrequency.equals(ONE_TIME)) {
                            return priceLine.getNonRecurringEupPrice().doubleValue();
                        } else if (priceFrequency.equals(RECURRING)) {
                            return priceLine.getRecurringEupPrice().doubleValue();
                        }
                    }
                }
            }
        }
        return 0.0;
    }

    public double getIndirectUserTotalEupPriceForProductInstanceId(String productInstanceId, PriceSuppressStrategy suppressStrategy, String priceFrequency, String priceType) {
        List<ProductChargingScheme> productChargingSchemeList = productInstance.getChargingSchemes();
        List<PricingSheetPriceModel> priceLines = newArrayList(filter(getPriceLines(suppressStrategy), notDummyPriceModelPredicate()));
        List<String> customerSalesType = new ArrayList();
        for (ProductChargingScheme scheme : productChargingSchemeList) {
            if (scheme.getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Customer)) {
                customerSalesType.add(scheme.getName());
            }
        }
        for (PricingSheetPriceModel priceLine : priceLines) {
            if (isPriceApplicableFor(priceLine, priceType, pricingClient) && productInstanceId.equals(productInstance.getProductInstanceId().getValue())) {
                for (String schemeName : customerSalesType) {
                    if (schemeName.equals(priceLine.getOneTimePrice().getChargingSchemeName())) {
                        if (priceFrequency.equals(ONE_TIME)) {
                            return priceLine.getNonRecurringPtpPrice().doubleValue();
                        } else if (priceFrequency.equals(RECURRING)) {
                            return priceLine.getRecurringPtpPrice().doubleValue();
                        }
                    }
                }
            }
        }
        return 0.0;
    }

    public double getIndirectUserSiteLevelManagementEupPriceForProductInstanceId(String productInstanceId, PriceSuppressStrategy suppressStrategy, String priceFrequency, String priceType) {
        if(hasAccessProducts() || childrenHasAccessProducts()) {
            return 0.00;
        }
        List<ProductChargingScheme> productChargingSchemeList = productInstance.getChargingSchemes();
        List<PricingSheetPriceModel> priceLines = newArrayList(filter(getPriceLines(suppressStrategy), notDummyPriceModelPredicate()));
        List<String> customerSalesType = new ArrayList();
        for (ProductChargingScheme scheme : productChargingSchemeList) {
            if (scheme.getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Sales)) {
                customerSalesType.add(scheme.getName());
            }
        }
        for (PricingSheetPriceModel priceLine : priceLines) {
            if (isPriceApplicableFor(priceLine, priceType, pricingClient) && productInstanceId.equals(productInstance.getProductInstanceId().getValue())) {
                for (String schemeName : customerSalesType) {
                    if (schemeName.equals(priceLine.getOneTimePrice().getChargingSchemeName())) {
                        if (priceFrequency.equals(ONE_TIME)) {
                            return priceLine.getNonRecurringPtpPrice().doubleValue();
                        } else if (priceFrequency.equals(RECURRING)) {
                            return priceLine.getRecurringPtpPrice().doubleValue();
                        }
                    }
                }
            }
        }
        return 0.0;
    }

    public double getIndirectUserSpecialBidEupPriceForProductInstanceId(String productInstanceId, PriceSuppressStrategy suppressStrategy, String priceFrequency) {
        List<ProductChargingScheme> productChargingSchemeList = productInstance.getChargingSchemes();
        List<PricingSheetPriceModel> priceLines = newArrayList(filter(getPriceLines(suppressStrategy), notDummyPriceModelPredicate()));
        List<String> customerSalesType = new ArrayList();
        for (ProductChargingScheme scheme : productChargingSchemeList) {
            if (scheme.getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Customer)
                && scheme.getPricingStrategy().equals(PricingStrategy.SpecialBid)) {
                customerSalesType.add(scheme.getName());
            }
        }
        for (PricingSheetPriceModel priceLine : priceLines) {
            if (productInstanceId.equals(productInstance.getProductInstanceId().getValue())) {
                for (String schemeName : customerSalesType) {
                    if (schemeName.equals(priceLine.getOneTimePrice().getChargingSchemeName())) {
                        if (priceFrequency.equals(ONE_TIME)) {
                            return priceLine.getNonRecurringPtpPrice().doubleValue();
                        } else if (priceFrequency.equals(RECURRING)) {
                            return priceLine.getRecurringPtpPrice().doubleValue();
                        }
                    }
                }
            }
        }
        return 0.0;
    }

    public double getRecurringEupPriceForPmfId(String pmfId, PriceSuppressStrategy suppressStrategy) {
        List<PricingSheetPriceModel> priceLines = newArrayList(filter(getPriceLines(suppressStrategy), notDummyPriceModelPredicate()));
        for (PricingSheetPriceModel priceLine : priceLines) {
            if (pmfId.equals(priceLine.getPmfId())) {
                return priceLine.getRecurringEupPrice().doubleValue();
            }
        }
        return 0.0;
    }


    public List<PricingSheetPriceModel> getAllSummarySheetPriceLines(String priceType) {
        final List<PricingSheetPriceModel> allPriceLines = getAllPriceLines(PriceSuppressStrategy.SummarySheet);
        return filterPriceLineForAction(allPriceLines, priceType);
    }

    protected List<PricingSheetPriceModel> filterPriceLineForAction(List<PricingSheetPriceModel> allPriceLines, final String priceType) {
        return newArrayList(Iterables.filter(allPriceLines, new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(PricingSheetPriceModel priceModel) {
                return isPriceApplicableFor(priceModel, priceType, pricingClient);
            }
        }));
    }

    public static boolean isPriceApplicableFor(PricingSheetPriceModel priceModel, String priceType, PricingClient pricingClient) {
        final String priceModelPriceType = priceModel.getPriceType();
        if(PricingSheetPriceModel.DUMMY_PMF_ID.equalsIgnoreCase(priceModel.getPmfId())){
            return true;
        }
        if (priceType.equalsIgnoreCase(NEW_IFC.name())) {
            return isIFCChargingScheme(priceModel, pricingClient) && priceModelPriceType.equalsIgnoreCase(ChangeType.ADD.getValue());
        } else {
            if (priceType.equalsIgnoreCase(NEW.name()) &&
                (priceModelPriceType.equalsIgnoreCase(ChangeType.ADD.getValue())||priceModelPriceType.equalsIgnoreCase(ChangeType.NONE.getValue()))) {
                return !isIFCChargingScheme(priceModel, pricingClient);
            } else if (priceType.equalsIgnoreCase(EXISTING.name()) && (priceModelPriceType.equalsIgnoreCase(ChangeType.DELETE.getValue()))) {
                return true;
            }
        }
         return false;
    }

    private static boolean isIFCChargingScheme(PricingSheetPriceModel priceModel, PricingClient pricingClient) {
        return !pricingClient.getPricingConfig().chargingSchemes().forName(priceModel.getChargingSchemeName()).forIfc("Y").search().isEmpty();
    }

    public List<PricingSheetPriceModel> getAllDetailSheetPriceLines(final String priceType) {
        final List<PricingSheetPriceModel> allDetailSheetPriceLines = getAllDetailSheetPriceLines();

        final List<PricingSheetPriceModel> priceModels = newArrayList(Iterables.filter(allDetailSheetPriceLines, new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(PricingSheetPriceModel price) {
                ProductInstance itemPrice;
                if (isNotNull(price.getOneTimePrice())) {
                    itemPrice = new PriceLineToProductInstanceMapper(price.getOneTimePrice()).map();
                } else if (isNotNull(price.getRentalPrice())) {
                    itemPrice = new PriceLineToProductInstanceMapper(price.getRentalPrice()).map();
                } else if (price.isUsageChargePriceLine()) {
                    itemPrice = new PriceLineToProductInstanceMapper(price.getUsagePrice()).map();
                } else {
                    return false;
                }
                mergeResult.changeFor(new OrderItemItemPrice(itemPrice));
                return isPriceApplicableFor(price, priceType, pricingClient);
            }
        }));

        // expand usage charges based on Tier
        expandWithUsageChargePriceLines(priceModels);

        return priceModels;
    }

    public List<PricingSheetPriceModel> getAllDetailSheetPriceLines() {
        ArrayList<PricingSheetPriceModel> priceLines = newArrayList();
        if (isNotNull(productInstance.getChargingSchemes()) && !productInstance.getChargingSchemes().isEmpty()) {
            priceLines = Lists.newArrayList(Iterables.filter(getAllPriceLines(PriceSuppressStrategy.DetailedSheet), new Predicate<PricingSheetPriceModel>() {
                @Override
                public boolean apply(final PricingSheetPriceModel priceLine) {
                    if (!PriceCategory.COST.getLabel().equals(priceLine.getTariffType())) {
                        final List<ProductChargingScheme> chargingSchemes = getAllChargingSchemes();
                        Optional<ProductChargingScheme> chargingSchemeOptional =
                            Iterables.tryFind(chargingSchemes, new Predicate<ProductChargingScheme>() {
                                @Override
                                public boolean apply(ProductChargingScheme productChargingScheme) {
                                    return productChargingScheme.getName().equalsIgnoreCase(priceLine.getChargingSchemeName());
                                }
                            });
                        if (chargingSchemeOptional.isPresent() && !(chargingSchemeOptional.get().getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Hidden)) &&
                            !(chargingSchemeOptional.get().getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Customer) && chargingSchemeOptional.get().getPricingStrategy().equals(PricingStrategy.Aggregation))) {
                            return true;
                        }
                    }
                    return false;
                }
            }));
            ;

        }
        return priceLines;
    }
   //This method is invoked from PricingSheet template
    public PricingSheetPriceModel getAccessCircuitPriceline(final String priceType, final String leg){
        final List<PricingSheetPriceModel> allPriceLines = new ArrayList<>();
        allPriceLines.addAll(getAllDetailSheetPriceLines(priceType));
        allPriceLines.addAll(getAllSummarySheetPriceLines(priceType));
        final List<String> setAggregations = getSetAggregationFor(leg);
        final List<ProductChargingScheme> accessChargingSchemes = newArrayList(Iterables.filter(productInstance.getChargingSchemes(), new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(ProductChargingScheme scheme) {
                return setAggregations.contains(scheme.getSetAggregated());
            }
        }));
        final Optional<PricingSheetPriceModel> pricingSheetPriceModelOptional = Iterables.tryFind(allPriceLines, new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(final PricingSheetPriceModel priceModel) {
                return Iterables.tryFind(accessChargingSchemes, new Predicate<ProductChargingScheme>() {
                    @Override
                    public boolean apply(ProductChargingScheme chargingScheme) {
                        return priceModel.getChargingSchemeName().equalsIgnoreCase(chargingScheme.getName());
                    }
                }).isPresent();
            }
        });
        if (pricingSheetPriceModelOptional.isPresent()) {
            return pricingSheetPriceModelOptional.get();
        }
        return PricingSheetPriceModel.dummyPriceModel();
    }

    public PricingSheetPriceModel getCustomAccessCircuitPrice(final String priceType, final String leg){
        List<PricingSheetPriceModel> pricingSheetPriceModels = getPriceLines(PriceSuppressStrategy.DetailedSheet);
        final List<PriceLine> accessChargingSchemes = newArrayList(Iterables.filter(productInstance.getPriceLines(), new Predicate<PriceLine>() {
            @Override
            public boolean apply(PriceLine priceLine) {
                return customAccessCircuitPrices.contains(priceLine.getPmfId());
            }
        }));
        final Optional<PricingSheetPriceModel> pricingSheetPriceModelOptional = Iterables.tryFind(pricingSheetPriceModels, new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(final PricingSheetPriceModel priceModel) {
                return Iterables.tryFind(accessChargingSchemes, new Predicate<PriceLine>() {
                    @Override
                    public boolean apply(PriceLine priceLine) {
                        return priceModel.getPmfId().equalsIgnoreCase(priceLine.getPmfId());
                    }
                }).isPresent();
            }
        });
        if (pricingSheetPriceModelOptional.isPresent()) {
            return pricingSheetPriceModelOptional.get();
        }
        return PricingSheetPriceModel.dummyPriceModel();
    }

    public PricingSheetPriceModel getCustomAccessCircuitPenaltyPrice(final String priceType, final String leg){
        List<PricingSheetPriceModel> pricingSheetPriceModels = getPriceLines(PriceSuppressStrategy.DetailedSheet);
        final List<PriceLine> accessChargingSchemes = newArrayList(Iterables.filter(productInstance.getPriceLines(), new Predicate<PriceLine>() {
            @Override
            public boolean apply(PriceLine priceLine) {
                return customAccessCircuitPenaltyPrices.contains(priceLine.getPmfId());
            }
        }));
        final Optional<PricingSheetPriceModel> pricingSheetPriceModelOptional = Iterables.tryFind(pricingSheetPriceModels, new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(final PricingSheetPriceModel priceModel) {
                return Iterables.tryFind(accessChargingSchemes, new Predicate<PriceLine>() {
                    @Override
                    public boolean apply(PriceLine priceLine) {
                        return priceModel.getPmfId().equalsIgnoreCase(priceLine.getPmfId());
                    }
                }).isPresent();
            }
        });
        if (pricingSheetPriceModelOptional.isPresent()) {
            return pricingSheetPriceModelOptional.get();
        }
        return PricingSheetPriceModel.dummyPriceModel();
    }

    public PricingSheetPriceModel getPenaltyChargeForAccessByLeg(final String priceType, final String leg) {
        final List<PricingSheetPriceModel> allDetailSheetPriceLines = getAllSummarySheetPriceLines(priceType);
        final String penaltySetAggregationName = getAccessPenaltySetAggregationByLeg(leg);

        final Optional<ProductChargingScheme> penaltyChargingScheme = Iterables.tryFind(productInstance.getChargingSchemes(), new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(ProductChargingScheme scheme) {
                return penaltySetAggregationName.equals(scheme.getSetAggregated());
            }
        });

        if (penaltyChargingScheme.isPresent()) {
            Optional<PricingSheetPriceModel> penaltyChargeOptional = Iterables.tryFind(allDetailSheetPriceLines, new Predicate<PricingSheetPriceModel>() {
                @Override
                public boolean apply(PricingSheetPriceModel input) {
                    return input.getChargingSchemeName().equals(penaltyChargingScheme.get().getName());
                }
            });

            if (penaltyChargeOptional.isPresent()) {
                return penaltyChargeOptional.get();
            }

            return PricingSheetPriceModel.dummyPriceModel();
        }

        return PricingSheetPriceModel.dummyPriceModel();
    }

    //This method is invoked from PricingSheet template
    public PricingSheetPriceModel getCPEPriceline(final String priceType,final String sheetName){
        final List<PricingSheetPriceModel> allDetailSheetPriceLines = "Detailed".equalsIgnoreCase(sheetName) ? getAllDetailSheetPriceLines(priceType) :getAllSummarySheetPriceLines(priceType);
        final List<ProductChargingScheme> cpeChargingSchemes = newArrayList(Iterables.filter(productInstance.getChargingSchemes(), new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(ProductChargingScheme scheme) {
                return cpeAggregations.contains(scheme.getSetAggregated());
            }
        }));
        final Optional<PricingSheetPriceModel> pricingSheetPriceModelOptional = Iterables.tryFind(allDetailSheetPriceLines, new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(final PricingSheetPriceModel priceModel) {
                return Iterables.tryFind(cpeChargingSchemes, new Predicate<ProductChargingScheme>() {
                    @Override
                    public boolean apply(ProductChargingScheme chargingScheme) {
                        return priceModel.getChargingSchemeName().equalsIgnoreCase(chargingScheme.getName());
                    }
                }).isPresent();
            }
        });
        if (pricingSheetPriceModelOptional.isPresent()) {
            return pricingSheetPriceModelOptional.get();
        }
        return PricingSheetPriceModel.dummyPriceModel();
    }

    private List<String> getSetAggregationFor(String leg) {
        if ("Leg1".equalsIgnoreCase(leg)) {
            return leg1Aggregations;
        } else {
            return leg2Aggregations;
        }
    }

    private String getAccessPenaltySetAggregationByLeg(String leg) {
        if ("Leg1".equals(leg)) {
            return leg1PenaltyAggregation;
        } else {
            return leg2PenaltyAggregation;
        }
    }

    public List<InstanceCharacteristic> getInstanceCharacteristics() {
        return productInstance.getInstanceCharacteristics();
    }

    public boolean isSiteInstallable() {
        return productInstance.getProductOffering().isSiteInstallable();
    }

    public String getEupPriceBook() {
        List<PriceBookDTO> priceBooks = quoteOptionItem.contractDTO.priceBooks;
        return priceBooks.get(0).eupPriceBook;
    }

    public String getPtpPriceBook() {
        List<PriceBookDTO> priceBooks = quoteOptionItem.contractDTO.priceBooks;
        return priceBooks.get(0).ptpPriceBook;
    }

    public String getResilienceType() {
        if (!SimpleProductOfferingType.Bearer.equals(productInstance.getSimpleProductOfferingType()) &&
            !productInstance.isCpe()) {
            try {
                return productInstance.getResilienceType();
            } catch (StencilIdInstanceCharacteristicNotFoundException ex) {
                return StringUtils.EMPTY;
            }
        }
        return StringUtils.EMPTY;
    }

    public ProductInstance getProductInstance() {
        return productInstance;
    }

    public QuoteOptionItemDTO getQuoteOptionItem() {
        return quoteOptionItem;
    }

    public SiteDTO getSite() {
        return site;
    }

    public boolean isSpecialBid() {
        return productInstance.isSpecialBid();
    }

    public boolean isAccess() {
        return productInstance.getProductOffering().isBearer();
    }

    public String getLineItemId() {
        return productInstance.getLineItemId();
    }

    public String getProjectId() {
        return productInstance.getProjectId();
    }

    public Date getProductEffectiveStartDate() {
        if (productInstance.getProductOffering().getEffectiveStartDate().after(new GregorianCalendar().getTime())) {
            return productInstance.getProductOffering().getEffectiveStartDate();
        }
        return null;
    }

    public List<ProductChargingScheme> getAllChargingSchemes() {
        List<ProductChargingScheme> schemes = newArrayList();
        schemes.addAll(productInstance.getChargingSchemes());
        for (PricingSheetProductModel child : getAllChildren()) {
            schemes.addAll(child.productInstance.getChargingSchemes());
        }
        return schemes;
    }

    public List<PricingCaveat> getPricingCaveats() {
        return productInstance.getPricingCaveats();
    }

    public BigDecimal getRecurringOrNonRecurringSummaryTotal(String priceType, boolean withContract, String recurringOrNonRecurringEupPrice) {
        BigDecimal total = BigDecimal.ZERO;
        final Collection<PricingSheetPriceModel> summarySheetPriceLines = getPricingSheetContractPriceModels(priceType);

        if (withContract) {
            for (PricingSheetPriceModel priceModel : summarySheetPriceLines) {
                if (isNotNull(priceModel.getRentalPrice())) {
                    final PriceLine priceLine = priceModel.getRentalPrice().multiplyPricesBy(priceModel.getRemainingContractTerm());
                    priceModel.setRentalPrice(priceLine);
                }
            }
        }

        if ("nonRecurringEupPrice".equalsIgnoreCase(recurringOrNonRecurringEupPrice)) {
            total = sumNonRecurringPrice(summarySheetPriceLines);
        } else if ("recurringEupPrice".equalsIgnoreCase(recurringOrNonRecurringEupPrice)) {
            total = sumRecurringPrice(summarySheetPriceLines);
        }
        return total;
    }

    public BigDecimal getNewNonRecurringSummaryTotal() {
        final Collection<PricingSheetPriceModel> summarySheetPriceLines = getPricingSheetPriceModels("NEW");
        return sumNonRecurringPrice(summarySheetPriceLines);
    }

    public BigDecimal getNewRecurringSummaryTotal() {
        final Collection<PricingSheetPriceModel> summarySheetPriceLines = getPricingSheetPriceModels("NEW");
        return sumRecurringPrice(summarySheetPriceLines);
    }

    public BigDecimal getExistingNonRecurringSummaryTotal() {
        final Collection<PricingSheetPriceModel> summarySheetPriceLines = getPricingSheetPriceModels("EXISTING");
        return sumNonRecurringPrice(summarySheetPriceLines);
    }

    public BigDecimal getExistingRecurringSummaryTotal() {
        final Collection<PricingSheetPriceModel> summarySheetPriceLines = getPricingSheetPriceModels("EXISTING");
        return sumRecurringPrice(summarySheetPriceLines);
    }

    //This method is invoked from PricingSheet template
    public Number getPriceBasedOnSetAggregatedName(final String priceType, final String chargeType, final String... setAggregatedName){
        final List<PricingSheetPriceModel> allDetailSheetPriceLines = getAllSummarySheetPriceLines(priceType);
        final List<String> setAggregations = Arrays.asList(setAggregatedName);
        final List<ProductChargingScheme> accessChargingSchemes = newArrayList(Iterables.filter(productInstance.getChargingSchemes(), new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(ProductChargingScheme scheme) {
                return setAggregations.contains(scheme.getSetAggregated());
            }
        }));
        final Optional<PricingSheetPriceModel> pricingSheetPriceModelOptional = Iterables.tryFind(allDetailSheetPriceLines, new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(final PricingSheetPriceModel priceModel) {
                return Iterables.tryFind(accessChargingSchemes, new Predicate<ProductChargingScheme>() {
                    @Override
                    public boolean apply(ProductChargingScheme chargingScheme) {
                        return priceModel.getChargingSchemeName().equalsIgnoreCase(chargingScheme.getName());
                    }
                }).isPresent();
            }
        });
        if (pricingSheetPriceModelOptional.isPresent()) {
            if(chargeType.equals(com.bt.rsqe.domain.product.PriceType.ONE_TIME.getValue())){
                return pricingSheetPriceModelOptional.get().getNonRecurringEupPrice();
            }else if(chargeType.equals(com.bt.rsqe.domain.product.PriceType.RECURRING.getValue())){
                return pricingSheetPriceModelOptional.get().getRecurringEupPrice();
            }
            return 0.0;
        }
        return 0.0;
    }

    private Collection<PricingSheetPriceModel> getPricingSheetPriceModels(final String priceType) {
        return filter(getAllSummarySheetPriceLines(priceType), new Predicate<PricingSheetPriceModel>() {
            @Override
            public boolean apply(PricingSheetPriceModel input) {
                return isPriceApplicableFor(input, priceType, pricingClient);
            }
        });
    }

    private Collection<PricingSheetPriceModel> getPricingSheetContractPriceModels(final String priceType) {
        return filterPriceLineForAction(getAllChildPriceLines(PriceSuppressStrategy.DetailedSheet), priceType);
    }

    private BigDecimal sumRecurringPrice(Collection<PricingSheetPriceModel> summarySheetPriceLines) {
        BigDecimal total = BigDecimal.ZERO;
        for(PricingSheetPriceModel priceLine : summarySheetPriceLines){
            final String quantityCharacteristic = priceLine.getInstanceCharacteristic("QUANTITY");
            final BigDecimal quantity = (quantityCharacteristic.equals("")) ? BigDecimal.ONE : new BigDecimal(quantityCharacteristic);
            final BigDecimal nonRecurringEupPrice = new BigDecimal(priceLine.getRecurringEupPrice().toString());
            total = total.add(nonRecurringEupPrice.multiply(quantity));
        }
        return total;
    }

    private BigDecimal sumNonRecurringPrice(Collection<PricingSheetPriceModel> summarySheetPriceLines) {
        BigDecimal total = BigDecimal.ZERO;
        for(PricingSheetPriceModel priceLine : summarySheetPriceLines){
            final String quantityCharacteristic = priceLine.getInstanceCharacteristic("QUANTITY");
            final BigDecimal quantity = (quantityCharacteristic.equals("")) ? BigDecimal.ONE : new BigDecimal(quantityCharacteristic);
            final BigDecimal nonRecurringEupPrice = new BigDecimal(priceLine.getNonRecurringEupPrice().toString());
            total = total.add(nonRecurringEupPrice.multiply(quantity));
        }
        return total;
    }

    private void expandWithUsageChargePriceLines(List<PricingSheetPriceModel> allPriceLines) {
        List<UsageChargeTierPricingSheetModel> usageCharges = newArrayList();
        Iterator<PricingSheetPriceModel> priceModelsIterator = allPriceLines.iterator();

        while(priceModelsIterator.hasNext()) {
            PricingSheetPriceModel currentModel = priceModelsIterator.next();

            if(currentModel.isUsageChargePriceLine()) {
                ListMultimap<String, Price> pricesGroupedByClassifier = Price.groupPricesByClassifier(currentModel.getUsagePrice().getUsageCharges());
                createAndAppendUsageCharges(currentModel, pricesGroupedByClassifier.asMap(), usageCharges);
                priceModelsIterator.remove(); // remove usage charge as it will be replaced with tiered PriceLines.
            }
        }

        Collections.sort(usageCharges);  // sort usage charges by Tier.
        allPriceLines.addAll(usageCharges);
    }

    private void createAndAppendUsageCharges(PricingSheetPriceModel priceModel,
                                             Map<String, Collection<Price>> classifierToPrices,
                                             List<UsageChargeTierPricingSheetModel> usageCharges) {
        for(Map.Entry<String, Collection<Price>> entry : classifierToPrices.entrySet()) {
            String classifier = entry.getKey();
            Collection<Price> prices = entry.getValue();
            usageCharges.add(new UsageChargeTierPricingSheetModel(priceModel, classifier, prices));
        }
    }
    public boolean hasCustomerVisibleChargingSchemes() {
        ArrayList<ProductChargingScheme> customerChargingSchemes = newArrayList(Iterables.filter(productInstance.getChargingSchemes(), new Predicate<ProductChargingScheme>() {
            @Override

            public boolean apply(ProductChargingScheme chargingScheme) {
                return ProductChargingScheme.PriceVisibility.Customer.equals(chargingScheme.getPriceVisibility());
            }
        }));
        for(final ProductChargingScheme scheme: customerChargingSchemes){
            boolean present = Iterables.tryFind(productInstance.getPriceLines(), new Predicate<PriceLine>() {
                @Override
                public boolean apply(PriceLine priceLine) {
                    return isPriceline(priceLine)
                           && priceLine.getChargingSchemeName().equalsIgnoreCase(scheme.getName());
                }
            }).isPresent();
            if(present) {
                return true;
            }
        }
        return false;
    }
    private boolean isPriceline(PriceLine priceLine) {
        return isEmpty(priceLine.getLosc()) || "No".equalsIgnoreCase(priceLine.getLosc());
    }
    // This will give service prices
    public List<PricingSheetPriceModel> getServicePrices(){
        List<PricingSheetPriceModel> allPriceLines = getAllPriceLines(PriceSuppressStrategy.DetailedSheet);
        List<PricingSheetPriceModel> servicePriceLines = newArrayList();
        for(PricingSheetPriceModel priceLine:allPriceLines){
            if(serviceList.contains(priceLine.getPmfId().substring(0,8).trim())){
                servicePriceLines.add(priceLine);
            }
        }
        if (servicePriceLines.size() == 0) {
            servicePriceLines.add(PricingSheetPriceModel.dummyPriceModel());
        }
        return servicePriceLines;
    }
}
