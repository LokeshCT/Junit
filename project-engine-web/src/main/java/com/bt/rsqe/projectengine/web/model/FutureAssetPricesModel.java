package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.dto.PriceDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.dto.ProjectedUsageDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.PricingStrategyDecider;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.model.modelfactory.ProjectedUsageModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.ManualPrice;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDeltas;
import com.bt.rsqe.projectengine.web.view.filtering.Filters;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import com.bt.rsqe.domain.product.chargingscheme.Discount;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.bt.rsqe.utils.Strings.replaceEncodedSpcialChar;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.newHashSet;

public class FutureAssetPricesModel {
    private String projectId;
    private String customerId;
    private String quoteOptionId;
    private final FutureAssetPricesDTO futureAssetPricesDTO;
    private SiteFacade siteFacade;
    private ProductIdentifierFacade productIdentifierFacade;
    private ProjectedUsageModelFactory projectedUsageModelFactory;
    private PriceSuppressStrategy priceSuppressStrategy;
    private List<PriceLineModel> priceLineModels;
    private List<PriceLineModel> deepFlattenedPriceLineModels;
    private DiscountUpdater discountUpdater;
    private PricingClient pricingClient;
    private List<ProductChargingScheme> totalSchemesForAsset = new ArrayList<ProductChargingScheme>();
    private static final Map<String, PriceType> priceType = new HashMap<String, PriceType>() {{
        put("oneTime", PriceType.ONE_TIME);
        put("recurring", PriceType.RECURRING);
    }};
    private Optional<PricingStrategyDecider> pricingStrategyDecider;
    private ProjectResource projects;
    private ProductInstanceClient productInstanceClient;

    public FutureAssetPricesModel(String customerId, String projectId, String quoteOptionId, FutureAssetPricesDTO futureAssetPricesDTO, SiteFacade siteFacade,
                                  ProductIdentifierFacade productIdentifierFacade, ProjectedUsageModelFactory projectedUsageModelFactory,
                                  PriceSuppressStrategy priceSuppressStrategy, DiscountUpdater discountUpdater, Optional<PricingStrategyDecider> pricingStrategyDecider,
                                  PricingClient pricingClient,  ProjectResource projects, ProductInstanceClient productInstanceClient) {
        this.customerId = customerId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.futureAssetPricesDTO = futureAssetPricesDTO;
        this.siteFacade = siteFacade;
        this.productIdentifierFacade = productIdentifierFacade;
        this.projectedUsageModelFactory = projectedUsageModelFactory;
        this.priceSuppressStrategy = priceSuppressStrategy;
        this.discountUpdater = discountUpdater;
        this.pricingStrategyDecider = pricingStrategyDecider;
        this.pricingClient = pricingClient;
        this.projects = projects;
        this.productInstanceClient = productInstanceClient;
    }

    public String getLineItemId() {
        return futureAssetPricesDTO.getLineItemId();
    }

    public List<FutureAssetPricesModel> getChildren() {
        List<FutureAssetPricesModel> children = new ArrayList<FutureAssetPricesModel>();

        for (FutureAssetPricesDTO childDTO : futureAssetPricesDTO.getChildren()) {
            children.add(new FutureAssetPricesModel(customerId, projectId, quoteOptionId, childDTO, siteFacade, productIdentifierFacade, projectedUsageModelFactory, priceSuppressStrategy, discountUpdater, pricingStrategyDecider, pricingClient, projects, productInstanceClient));
        }
        return children;
    }

    protected List<PriceLineModel> getPriceLines() {
        if (priceLineModels == null || priceLineModels.isEmpty()) {
            priceLineModels = fetchPriceLines();
        }
        return priceLineModels;
    }

    public List<PriceLineModel> getDeepFlattenedPriceLines() {
        if (deepFlattenedPriceLineModels == null) {
            deepFlattenedPriceLineModels = fetchDeepFlattenedPriceLines();
        }
        return deepFlattenedPriceLineModels;
    }

    public SiteDTO getSite() {
        if (StringUtils.isBlank(futureAssetPricesDTO.getSiteId())) {
            return siteFacade.getCentralSite(customerId, projectId);
        }
        return siteFacade.get(customerId, projectId, futureAssetPricesDTO.getSiteId());
    }

    public String getSiteName() {
        final SiteDTO site = getSite();
        if (site != null) {
            return site.name;
        } else {
            return "";
        }
    }

    public boolean isForLineItem(String lineItemId) {
        return lineItemId.equals(futureAssetPricesDTO.getLineItemId());
    }

    public String getProductName() {
        return productIdentifierFacade.getProductName(futureAssetPricesDTO.getProductCode());
    }

    public String getDisplayName() {
        return productIdentifierFacade.getDisplayName(futureAssetPricesDTO.getProductCode());
    }

    private List<PriceLineModel> fetchPriceLines() {
        Map<Long, PriceLineDTO> ppsrLookup = new HashMap<Long, PriceLineDTO>();
        List<PriceLineModel> models = new ArrayList<PriceLineModel>();
        for (PriceLineDTO priceLineDTO : getPriceLinesAfterSuppression(futureAssetPricesDTO.getPriceLines())) {
            if (ppsrLookup.containsKey(priceLineDTO.getPpsrId())) {
                final PriceLineDTO storedDto = ppsrLookup.get(priceLineDTO.getPpsrId());

                addToModelList(models, priceLineDTO, storedDto);

                ppsrLookup.remove(priceLineDTO.getPpsrId());

            } else {
                ppsrLookup.put(priceLineDTO.getPpsrId(), priceLineDTO);
            }
        }
        for (PriceLineDTO priceLineDTO : ppsrLookup.values()) {
            addToModelList(models, priceLineDTO);
        }
        return models;
    }

    private List<PriceLineDTO> getPriceLinesAfterSuppression(List<PriceLineDTO> priceLines) {
        List<ProductChargingScheme> chargingSchemes = newArrayList(getChargingSchemes(futureAssetPricesDTO.getProductCode(), futureAssetPricesDTO.getStencilId()));
        if(pricingStrategyDecider.isPresent()) {
            // in certain scenarios the pricing strategy defined in the product model can be overridden
            pricingStrategyDecider.get().resetStrategyForScenario(chargingSchemes,
                                                                  new LengthConstrainingProductInstanceId(futureAssetPricesDTO.getId()),
                                                                  new ProductInstanceVersion(futureAssetPricesDTO.getAssetDetailDTO().getVersion()));
        }
        return priceSuppressStrategy.suppressPriceCostLineDTOs(Optional.of(pricingClient.getPricingConfig()), chargingSchemes, priceLines);
    }

    private List<ProductChargingScheme> getChargingSchemes(String sCode, String stencilId) {
        return productIdentifierFacade.getChargingSchemes(sCode, stencilId);
    }

    private ProductChargingScheme getChargingScheme(final String schemeName) {
        if(totalSchemesForAsset.isEmpty()){
            totalSchemesForAsset.addAll(getChargingSchemes(futureAssetPricesDTO.getProductCode(), null));
        }
        Optional<ProductChargingScheme> optionalScheme = tryFind(totalSchemesForAsset, new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(@Nullable ProductChargingScheme scheme) {
                return scheme != null && scheme.getName().equals(schemeName);
            }
        });
        return optionalScheme.isPresent() ? optionalScheme.get() : null;
    }

    private List<PriceLineModel> fetchDeepFlattenedPriceLines() {
        List<PriceLineModel> models = new ArrayList<PriceLineModel>();
        recurseAndFlattenProductPriceLines(futureAssetPricesDTO, models);
        return models;
    }

    private void recurseAndFlattenProductPriceLines(FutureAssetPricesDTO parent, List<PriceLineModel> models) {
        Map<Long, PriceLineDTO> ppsrLookup = new HashMap<Long, PriceLineDTO>();
        totalSchemesForAsset.addAll(productIdentifierFacade.getChargingSchemes(parent.getProductCode(), parent.getStencilId()));
        for (PriceLineDTO priceLineDTO : parent.getPriceLines()) {
            if (ppsrLookup.containsKey(priceLineDTO.getPpsrId())) {
                final PriceLineDTO storedDto = ppsrLookup.get(priceLineDTO.getPpsrId());

                addToModelList(models, priceLineDTO, storedDto);

                ppsrLookup.remove(priceLineDTO.getPpsrId());

            } else {
                ppsrLookup.put(priceLineDTO.getPpsrId(), priceLineDTO);
            }
        }
        for (PriceLineDTO priceLineDTO : ppsrLookup.values()) {
            addToModelList(models, priceLineDTO);
        }
        for (FutureAssetPricesDTO child : parent.getChildren()) {
            recurseAndFlattenProductPriceLines(child, models);
        }
    }

    private void addToModelList(List<PriceLineModel> models, PriceLineDTO priceLineDTO) {
        addToModelList(models, priceLineDTO, null);
    }

    private void addToModelList(List<PriceLineModel> models, PriceLineDTO firstPriceLineDto, PriceLineDTO secondPriceLineDto) {
        Boolean isProvideAsset = ChangeType.ADD.equals(getAssetChangeType());
        if (firstPriceLineDto.getPriceType() == PriceType.ONE_TIME) {
            models.add(new PriceLineModel(firstPriceLineDto, secondPriceLineDto, getChargingScheme(firstPriceLineDto.getChargingSchemeName()), pricingClient, isProvideAsset));
        } else {
            models.add(new PriceLineModel(secondPriceLineDto, firstPriceLineDto, getChargingScheme(firstPriceLineDto.getChargingSchemeName()), pricingClient, isProvideAsset));
        }
    }

    private ChangeType getAssetChangeType() {
        ProductInstance instance = productInstanceClient.getByAssetKey(new LengthConstrainingProductInstanceId(getId()),
                                                                       new ProductInstanceVersion(futureAssetPricesDTO.getVersion()));
        return productInstanceClient.getAction(instance);
    }

    public void applyDiscountDeltas(Map<String, DiscountDelta> discounts) {
        discountUpdater.applyDiscount(discounts, futureAssetPricesDTO, getDeepFlattenedPriceLines());
    }

    /**
     * @deprecated use applyDiscountDeltas
     */
    public void applyDiscount(Map<String, BigDecimal> discounts) {
        applyDiscountDeltas(Maps.transformValues(discounts, new Function<BigDecimal, DiscountDelta>() {
            @Override
            public DiscountDelta apply(BigDecimal input) {
                return new DiscountDelta(input);
            }
        }));
    }

    public void applyUsageDiscounts(List<QuoteOptionPricingDeltas.QuoteOptionPricingDelta> pricingDeltas) {
        for(QuoteOptionPricingDeltas.QuoteOptionPricingDelta pricingDelta : pricingDeltas) {
            PriceLineDTO priceLine = PriceLineDTO.fetchPriceLineById(getPricesDTO().getPriceLines(), pricingDelta.getPriceLineId());
            List<PriceDTO> usagePrices = priceLine.getPricesByClassifier(pricingDelta.getClassifier());

            for(PriceDTO price : usagePrices) {
                BigDecimal discount = pricingDelta.getChargeDiscount(price.getCategory());
                if(null != discount) {
                    price.discountPercentage = discount;
                }
            }
        }
    }

    public void applyUsageDiscounts(List<QuoteOptionPricingDeltas.QuoteOptionPricingDelta> pricingDeltas, FutureAssetPricesModel lineItem,
                                       FutureAssetPricesFacade futureAssetPricesFacade) {

        for(QuoteOptionPricingDeltas.QuoteOptionPricingDelta pricingDelta : pricingDeltas) {
            PriceLineDTO priceLine = PriceLineDTO.fetchPriceLineById(getPricesDTO().getPriceLines(), pricingDelta.getPriceLineId());
            List<PriceDTO> usagePrices = priceLine.getPricesByClassifier(pricingDelta.getClassifier());

            for(PriceDTO price : usagePrices) {
                BigDecimal discount = pricingDelta.getChargeDiscount(price.getCategory());
                if(null != discount) {
                    price.discountPercentage = discount;
                }
            }
            //Call the method
            if(isNotNull(lineItem) && isNotNull(priceLine) && isNotNull(priceLine.getChargingSchemeName())){
                copyDiscounts(priceLine,lineItem,futureAssetPricesFacade,pricingDelta);
            }
        }
    }

    public void copyDiscounts(PriceLineDTO priceLine,FutureAssetPricesModel lineItem,FutureAssetPricesFacade futureAssetPricesFacade,
                              QuoteOptionPricingDeltas.QuoteOptionPricingDelta pricingDelta){

        List<FutureAssetPricesModel> ownerLineItemPriceModels = getOwnerAssets(priceLine.getChargingSchemeName(), lineItem, futureAssetPricesFacade);

        for(FutureAssetPricesModel ownerLineItemPriceModel : ownerLineItemPriceModels) {

            if(isNotNull(ownerLineItemPriceModel) && isNotNull(ownerLineItemPriceModel.getPricesDTO()) &&
               isNotNull(ownerLineItemPriceModel.getPricesDTO().getProductCode()) && isNotNull(ownerLineItemPriceModel.getPricesDTO().getPriceLines())){

                List<ProductChargingScheme> chargingSchemeList = getChargingSchemes(ownerLineItemPriceModel.getPricesDTO().getProductCode(),
                                                                                    ownerLineItemPriceModel.getPricesDTO().getStencilId());
                if(isNotNull(chargingSchemeList)){
                    List<PriceLineDTO> priceLineOwnerList = ownerLineItemPriceModel.getPricesDTO().getPriceLines();
                    for(PriceLineDTO priceLineDto : priceLineOwnerList){
                        for (ProductChargingScheme chargingScheme : getMatchingDiscountChargingSchemes(chargingSchemeList, priceLineDto)) {
                            if (isNotNull(priceLineDto) && isNotNull(chargingScheme) && isNotNull(chargingScheme.getDiscount()) && isNotNull(chargingScheme.getDiscount().getDiscountChargingSchema())
                                && priceLine.getChargingSchemeName().equals(chargingScheme.getDiscount().getDiscountChargingSchema())) {

                                List<PriceDTO> parentUsagePrice = priceLineDto.getPricesByClassifier(pricingDelta.getClassifier());
                                for (PriceDTO price : parentUsagePrice) {
                                    if(isNotNull(price)){
                                        BigDecimal discount = pricingDelta.getChargeDiscount(price.getCategory());
                                        if (isNotNull(discount)) {
                                            price.discountPercentage = discount;
                                        }
                                    }
                                }
                                futureAssetPricesFacade.save(ownerLineItemPriceModel);
                            }
                        }
                    }
                }
            }
        }
    }

    private  List<ProductChargingScheme>  getMatchingDiscountChargingSchemes(List<ProductChargingScheme> chargingSchemeList,final PriceLineDTO priceLineDto) {

        List<ProductChargingScheme> productChargingSchemes = newArrayList(Iterables.filter(chargingSchemeList, new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(ProductChargingScheme input) {
                return isNotNull(input) && isNotNull(input.getName()) && isNotNull(priceLineDto) && priceLineDto.getChargingSchemeName().equals(input.getName());
            }
        }));

        return productChargingSchemes;
    }

    public List<FutureAssetPricesModel> getOwnerAssets(String chargingSchemeName, FutureAssetPricesModel lineItem, FutureAssetPricesFacade futureAssetPricesFacade){

        Set<Discount> relationShipNames = productIdentifierFacade.getChargingSchemesForDiscount(chargingSchemeName);
        String relationShipName = StringUtils.EMPTY;
        if(isNotNull(relationShipNames)){
            Iterator<Discount> iterator = relationShipNames.iterator();
            while (iterator.hasNext()) {
                Discount disc = iterator.next();
                relationShipName = disc.getProductPath();
                break;
            }
        }
        List<FutureAssetPricesModel> futureAssetPricesModelsForOwner = new ArrayList<FutureAssetPricesModel>();
        if(!isNullOrEmpty(relationShipName)){
            if(isNotNull(lineItem.getPricesDTO()) && isNotNull(lineItem.getPricesDTO().getAssetDetailDTO())
               && isNotNull(lineItem.getPricesDTO().getAssetDetailDTO().getId()) && isNotNull(lineItem.getPricesDTO().getAssetDetailDTO().getVersion())){

                List<String> ownerLineItemIdList = futureAssetPricesFacade.getOwnerAssetForLineItem(lineItem.getPricesDTO().getAssetDetailDTO().getId(),
                                                                                          lineItem.getPricesDTO().getAssetDetailDTO().getVersion(),relationShipName);
                Set<LineItemId> parentLineItems = newHashSet();
                for(String lineItemId : ownerLineItemIdList){
                    if(!isNullOrEmpty(lineItemId)){
                        parentLineItems.add(new LineItemId(lineItemId));
                    }
                }
                futureAssetPricesModelsForOwner = futureAssetPricesFacade.getForLineItems(customerId,projectId,quoteOptionId,newArrayList(parentLineItems));
            }

        }
        return futureAssetPricesModelsForOwner;
    }

    public void applyGrossPriceUpdate(Set<ManualPrice> manualPrices) {
        updatePrices(manualPrices, futureAssetPricesDTO);
    }

    private void updatePrices(Set<ManualPrice> manualPrices, FutureAssetPricesDTO futureAssetPricesDTO) {

       for (PriceLineDTO priceLineDTO : futureAssetPricesDTO.getPriceLines()) {
            Optional<ManualPrice> manualPrice = getManualPrice(manualPrices, priceLineDTO);
            if (manualPrice.isPresent()) {
                priceLineDTO.getPrice(PriceCategory.CHARGE_PRICE).setPrice(manualPrice.get().getGross());
                priceLineDTO.setStatus(PriceLineStatus.FIRM);
            }
        }

        for (FutureAssetPricesDTO assetPricesDTO : futureAssetPricesDTO.getChildren()) {
            updatePrices(manualPrices, assetPricesDTO);
        }
    }

    private Optional<ManualPrice> getManualPrice(final Set<ManualPrice> manualPrices, final PriceLineDTO priceLineDTO) {
        return Iterables.tryFind(manualPrices, new Predicate<ManualPrice>() {
            @Override
            public boolean apply(ManualPrice input) {
                return priceLineDTO.getId().equals(input.getId())
                       && replaceEncodedSpcialChar(input.getProductDescription()).equals(priceLineDTO.getDescription())
                       && priceLineDTO.getPriceType().equals(priceType.get(input.getType()));
            }
        });
    }

    public String getId() {
        return futureAssetPricesDTO.getId();
    }

    //TODO:  make private
    public FutureAssetPricesDTO getPricesDTO() {
        return futureAssetPricesDTO;
    }

    public List<PriceLineModel> filterPriceLines(Filters.Filter<PriceLineModel>... filtersToApply) {
        Filters<PriceLineModel> filters = new Filters<PriceLineModel>();
        for (Filters.Filter<PriceLineModel> filter : filtersToApply) {
            filters.add(filter);
        }
        return filters.apply(getDeepFlattenedPriceLines());
    }

    public void accept(LineItemVisitor priceVisitor) {
        accept(priceVisitor, 0);
    }

    /* ********************************************************************************************
    TODO: getProjectedUsages() will be made private once visitor pattern implemented for BCM generation
     ******************************************************************************************** */
    public List<ProjectedUsageModel> getProjectedUsages() {
        final List<ProjectedUsageModel> projectedUsages = newArrayList();
        for (ProjectedUsageDTO projectedUsageDTO : futureAssetPricesDTO.getAssetDetailDTO().getProjectedUsages()) {
            projectedUsages.add(projectedUsageModelFactory.create(projectedUsageDTO, quoteOptionId, this.getSite().country));
        }
        return projectedUsages;
    }

    private void accept(LineItemVisitor visitor, int groupingLevel) {
        visitor.visit(this, groupingLevel);
        for (PriceLineModel priceLineModel : getPriceLines()) {
            priceLineModel.accept(visitor);
        }
        for (ProjectedUsageModel projectedUsage : getProjectedUsages()) {
            projectedUsage.accept(visitor);
        }

        groupingLevel += 1;
        for (FutureAssetPricesModel child : getChildren()) {
            child.accept(visitor, groupingLevel);
        }
    }

    public boolean hasNoPriceLines() {
        return getDeepFlattenedPriceLines().isEmpty() && hasNoProjectedUsages();
    }

    private boolean hasNoProjectedUsages() {
        return getPricesDTO().getAssetDetailDTO().getProjectedUsages().isEmpty();
    }

    public String getSiteId() {
        return futureAssetPricesDTO.getSiteId();
    }

    public String getProjectId() {
        return projectId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }
}
