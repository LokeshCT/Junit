package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.configuration.SqeAppUrlConfig;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.resource.ProductAgreementResourceClient;
import com.bt.rsqe.customerinventory.dto.AssetCharacteristicDTO;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.dto.ProductInstancePricesDTO;
import com.bt.rsqe.customerinventory.parameter.CharacteristicName;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.ContractTermHelper;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.AssetProcessType;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.IfcAction;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.LineItemValidationDescriptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.bt.rsqe.projectengine.web.quoteoptiondetails.IfcActionMapper.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

public class LineItemModel {
    private static final LineItemModelLogger LOG = LogFactory.createDefaultLogger(LineItemModelLogger.class);
    private String projectId;
    private String quoteOptionId;
    private String customerId;
    private String contractId;
    private QuoteOptionItemDTO quoteOptionItemDTO;
    private ExpedioProjectResource projects;
    private FutureAssetPricesFacade productInstancePricesFacade;
    private ProductIdentifierFacade productIdentifierFacade;
    private UriFactory productConfiguratorUriBuilder;
    private FutureAssetPricesModel futureAssetPricesModel;
    private LineItemModel parent;
    private PmrClient pmrClient;
    private PriceSuppressStrategy priceSuppressStrategy;
    private ProductInstanceClient productInstanceClient;
    private AssetDTO rootInstance;
    private ProductOffering productOffering, baseProductOffering;
    private String summary;
    private String subLocationName;
    private String room;
    private String floor;
    private QuoteOptionDTO quoteOptionDTO;
    private boolean hasLineItemNotes;
    private ProductAgreementResourceClient productAgreementResourceClient;
    private static final String YES = "Y";
    private SqeAppUrlConfig sqeAppUrlConfig;
    private String userToken;
    private boolean isProxyProduct;

    public static final Map<Enum, String> ORDER_STATUSES = new HashMap<Enum, String>() {{
        put(LineItemOrderStatus.NOT_APPLICABLE, "N/A");
        put(LineItemOrderStatus.CANCELLED, "Cancelled");
        put(LineItemOrderStatus.SUSPENDED, "On Hold");
        put(LineItemOrderStatus.ORDERED, "Ordered");
        put(LineItemOrderStatus.REJECTED, "Rejected");
        put(LineItemOrderStatus.OPEN, "Open");
        put(LineItemOrderStatus.ACCEPTED, "Accepted");
        put(LineItemOrderStatus.COMPLETE, "Complete");
    }};

    public LineItemModel(String projectId,
                         String quoteOptionId,
                         String customerId,
                         String contractId, QuoteOptionItemDTO quoteOptionItemDTO,
                         ExpedioProjectResource projects,
                         FutureAssetPricesFacade productInstancePricesFacade,
                         ProductIdentifierFacade productIdentifierFacade,
                         UriFactory productConfiguratorUriBuilder,
                         LineItemModel parent,
                         PmrClient pmrClient,
                         PriceSuppressStrategy priceSuppressStrategy,
                         ProductInstanceClient productInstanceClient, QuoteOptionDTO quoteOptionDTO,
                         ProductAgreementResourceClient productAgreementResourceClient, SqeAppUrlConfig sqeAppUrlConfig) {

        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.customerId = customerId;
        this.contractId = contractId;
        this.quoteOptionItemDTO = quoteOptionItemDTO;
        this.projects = projects;
        this.productInstancePricesFacade = productInstancePricesFacade;
        this.productIdentifierFacade = productIdentifierFacade;
        this.productConfiguratorUriBuilder = productConfiguratorUriBuilder;
        this.parent = parent;
        this.pmrClient = pmrClient;
        this.priceSuppressStrategy = priceSuppressStrategy;
        this.productInstanceClient = productInstanceClient;
        this.quoteOptionDTO = quoteOptionDTO;
        this.productAgreementResourceClient= productAgreementResourceClient;
        this.sqeAppUrlConfig = sqeAppUrlConfig;
    }

    public SiteDTO getSite() {
        final SiteDTO site = getSiteImpl();
        return site == null ? SiteDTO.CUSTOMER_OWNED : site;
    }

    public String getSiteId() {
        return fetchFutureAsset().getSiteId();
    }


    public String getOfferName() {
        return quoteOptionItemDTO.offerName == null ? "" : quoteOptionItemDTO.offerName;
    }


    public boolean hasSite() {
        return getSiteImpl() != null;
    }

    private SiteDTO getSiteImpl() {
        return fetchFutureAsset().getSite();
    }


    public String getProductName() {
        return productIdentifierFacade.getProductName(getBaseOffering());
    }

    public String getDisplayName() {
        return productIdentifierFacade.getDisplayName(getBaseOffering());
    }

    public String getProductCategoryName() {
        Optional<ProductIdentifier> productIdentifierOptional = pmrClient.getProductHCode(getProductSCode());
        return productIdentifierOptional.isPresent() ? productIdentifierOptional.get().getProductName() : getProductName();
    }

    public String getProductSCode() {
        return quoteOptionItemDTO.sCode;
    }

    public boolean getHasLineItemNotes(){return quoteOptionItemDTO.hasLineItemNotes;}

    public ProductCategoryCode getProductCategoryCode() {
        return quoteOptionItemDTO.getProductCategoryCode();
    }

    public String  getSubLocationName(){
        AssetDTO assetDTO = getRootInstance();
        subLocationName = assetDTO.detail().getSubLocationName();
        return subLocationName;
    }
    public String getRoom(){
        AssetDTO assetDTO = getRootInstance();
        room = assetDTO.detail().getRoom();
        return room;
    }
    public String getFloor(){
        AssetDTO assetDTO = getRootInstance();
        floor = assetDTO.detail().getFloor();
        return floor;
    }
    public String getSummary() {
        if (summary == null) {
            summary = StringUtils.EMPTY;
            AssetDTO asset = getRootInstance();

            for (Attribute attribute : getProductOffering().getAttributes()) {
                if (attribute.isVisibleInSummary()) {
                    if (asset.hasCharacteristic(new CharacteristicName(attribute.getName().toString()))) {
                        if (attribute.getName().toString().equalsIgnoreCase(ProductOffering.STENCIL_RESERVED_NAME)) {
                            if (getProductOffering().getStencilId().isPresent()) {
                                summary += getProductOffering().getStencilId().get().getProductName().getValue() + ", ";
                            }
                        } else {
                            AssetCharacteristicDTO visibleInSummaryCharacteristic = asset.getCharacteristic(attribute.getName().toString());
                            if (!isNullOrEmpty(visibleInSummaryCharacteristic.getValue()) && !visibleInSummaryCharacteristic.getValue().equalsIgnoreCase("0.0")) {
                                    summary += asset.getCharacteristic(attribute.getName().toString()).getValue() + ", ";
                            }
                        }
                    }
                }
            }

            if (asset.hasCharacteristic(new CharacteristicName(ProductOffering.MOVE_TYPE))) {
                final AssetCharacteristicDTO moveType = asset.getCharacteristic(ProductOffering.MOVE_TYPE);
                if (!isEmpty(moveType.getValue()) && !AssetProcessType.NOT_APPLICABLE.value().equals(moveType.getValue())
                    && !getProductOffering().getAttribute(new AttributeName(moveType.getName().toString())).isVisibleInSummary()) {
                    summary += moveType.getValue() + ", ";
                }
            } else if (getProductOffering().hasSpecialBidIndicator()) {
                // TODO this should be using isVisibleInSummary so that we can use other attribute values here
                if (asset.isSpecialBid() && asset.hasCharacteristic(new CharacteristicName(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME))) {
                    final AssetCharacteristicDTO tpeTemplateName = asset.getCharacteristic(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME);
                    if (!isNullOrEmpty(tpeTemplateName.getValue()) && !getProductOffering().getAttribute(new AttributeName(tpeTemplateName.getName().toString())).isVisibleInSummary()) {
                        summary += tpeTemplateName.getValue() + ", ";
                    }
                }
            }
        }
        return summary.isEmpty() ? summary : summary.substring(0, summary.toString().trim().length() - ",".length());
    }

    public boolean isProductVisibleOnlineSummary() {
        return getBaseOffering().isVisibleInOnlineSummary();
    }

    public boolean isInFrontCatlogueProduct() {
        return getBaseOffering().isInFrontCatalogue();
    }

    public boolean isMAGApplicable(){
        return getBaseOffering().isMAGApplicable();
    }

    public String getId() {
        return quoteOptionItemDTO.id;
    }

    public String getContractTerm() {
        return quoteOptionItemDTO.contractTerm;
    }

    public String getPriceBook() {
        List<PriceBookDTO> priceBooks = getPriceBooks();
        return priceBooks.get(0).eupPriceBook;
    }

    private List<PriceBookDTO> getPriceBooks() {
        return quoteOptionItemDTO.contractDTO.priceBooks;
    }

    public String getStencil(String sCode) {
        Pmr.ProductOfferings offerings = pmrClient.productOffering(ProductSCode.newInstance(sCode));

        return offerings.get().getProductIdentifier().getProductName();

    }



    public void setHasLineItemNotes(boolean hasLineItemNotes)
    {
        LOG.logLIMsetHasLineItemNotes(hasLineItemNotes);
        this.hasLineItemNotes = hasLineItemNotes;
    }

    public List<String> getRelatedProductOfferingSCodes(String sCode) {
        //Pmr.ProductOfferings offerings = ;

        List<String> listOfSCodes = new ArrayList<String>();

        for (SalesRelationship salesRelationship : pmrClient.productOffering(ProductSCode.newInstance(sCode)).get().getSalesRelationships()) {
            listOfSCodes.add(salesRelationship.getProductIdentifier().getProductId());
        }

        return listOfSCodes;

    }

    public Pmr.ProductOfferings getRootCOTCProduct(String sCode, String stencilId) {
        Pmr.ProductOfferings productOfferings = pmrClient.productOffering(ProductSCode.newInstance(sCode));
        com.bt.rsqe.domain.product.ProductOffering relatedProductOffering = productOfferings.withStencil(StencilId.latestVersionFor(StencilCode.newInstance(stencilId))).get();

        StencilId stencilForCOTCRootProduct = relatedProductOffering.getSalesRelationships().get(0).getRelatedProductIdentifier().getStencilId();
        String rootCOTCProductSCode = productOfferings.get().getSalesRelationships().get(0).getRelatedProductIdentifier().getProductId();

        //TODO apply the stencil to root COTC product
        Pmr.ProductOfferings rootProductOffering = pmrClient.productOffering(ProductSCode.newInstance(rootCOTCProductSCode)).withStencil(stencilForCOTCRootProduct);
        com.bt.rsqe.domain.product.ProductOffering rootCOTCProduct = rootProductOffering.get();


        return rootProductOffering;
    }

    public String getAction() {
        return quoteOptionItemDTO.action;
    }


    public String getStatus() {
        return quoteOptionItemDTO.status.getDescription();
    }


    public String getDiscountStatus() {
        return quoteOptionItemDTO.discountStatus.getDescription();
    }


    public boolean isDiscountApprovalRequested() {
        return quoteOptionItemDTO.discountStatus == LineItemDiscountStatus.APPROVAL_REQUESTED;
    }

    public String getIfcAction() {
        return viewNameFor(quoteOptionItemDTO.ifcAction);
    }

    public String getBillingId() {
        return quoteOptionItemDTO.billingId;
    }


    public boolean isForIfc() {
        return quoteOptionItemDTO.ifcAction != IfcAction.NOT_APPLICABLE;
    }


    public String getConfigureUrl(ProjectDTO project) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("status", quoteOptionItemDTO.status.getDescription());
        parameters.put("readOnly", quoteOptionItemDTO.readOnly.toString());
        parameters.put("revenueOwner", project.organisation.name);
        return productConfiguratorUriBuilder.getConfigurationUri(getProductSCode(), customerId, contractId, projectId, quoteOptionId, getId(), parameters);
    }


    public String getOfferDetailsUrl() {
        final String offerId = quoteOptionItemDTO.offerId;
        if (offerId != null && !offerId.isEmpty()) {
            return UriFactoryImpl.offerDetails(customerId, contractId, projectId, quoteOptionId, offerId).toString();
        }
        return "";
    }

    public boolean isProxyProduct() {
        List<String> proxyAssetProducts = transform(pmrClient.getProxyAssetTypeProducts(),toProductCode());
        isProxyProduct = proxyAssetProducts.contains(quoteOptionItemDTO.sCode);
        return isProxyProduct;
    }

    private static Function<ProductIdentifier, String> toProductCode() {
        return new Function<ProductIdentifier, String>() {
            @Override
            public String apply(ProductIdentifier input) {
                return input.getProductId();
            }
        };
    }

    public String getProductDetailsUrl() {

          final String productName = productIdentifierFacade.getProductName(getBaseOffering());
          final String orderType = quoteOptionItemDTO.action;
          final String guId = this.getUserToken();
          final String quoteHeaderId = projectId;
          String SQE_BASE_URI = sqeAppUrlConfig.getUrl();
          if ((productName != null && !productName.isEmpty()) && (orderType != null && !orderType.isEmpty())) {
            return UriFactoryImpl.productDetails(SQE_BASE_URI, orderType, productName, guId);
          }
        return "";

    }


    public String getErrorMessage() {
        List<String> descriptions = newArrayList();
        if(!quoteOptionItemDTO.validity.descriptions.isEmpty()) {
            for (LineItemValidationDescriptionDTO lineItemValidationDescriptionDTO : quoteOptionItemDTO.validity.descriptions) {
                descriptions.add(lineItemValidationDescriptionDTO.getMessage());
            }
        }
        return descriptions.isEmpty() ? "" : StringUtils.join(descriptions, System.getProperty("line.separator"));
    }


    public String getValidity() {
        return quoteOptionItemDTO.validity.status.name();
    }


    public boolean isReadOnly() {
        return quoteOptionItemDTO.readOnly;
    }


    public String getOrderStatus() {
        return ORDER_STATUSES.get(quoteOptionItemDTO.orderStatus);
    }

    public String getOrderId() {
        return quoteOptionItemDTO.orderId;
    }

    public void accept(LineItemVisitor visitor) {
        visitor.visit(this);
        fetchFutureAsset().accept(visitor);
        visitor.visitAfterChildren(this);
    }

    public FutureAssetPricesModel getFutureAssetPricesModel() {
        return fetchFutureAsset();
    }

    public LineItemId getLineItemId() {
        return new LineItemId(getId());
    }

    public PricingStatus getPricingStatusOfTree() {
        int rank = getPricingRankOfRootAndItsChildren();
        for (AssetDTO lineItemAsset : getRootInstance().flattenMeAndMyChildren().values()) {

            if (lineItemAsset.getRelatedToAssets().isEmpty()) {
                if (!pmrClient.productOffering(ProductSCode.newInstance(lineItemAsset.getProductCode())).get().isInFrontCatalogue) {
                    rank = Math.min(rank, lineItemAsset.getPricingStatus().rank());
                }
            } else {
                for (AssetDTO related : lineItemAsset.getRelatedToAssets()) {
                    if (!isNullOrEmpty(lineItemAsset.getQuoteOptionId()) && lineItemAsset.getQuoteOptionId().equals(related.getQuoteOptionId())
                            && !pmrClient.productOffering(ProductSCode.newInstance(related.getProductCode())).get().isInFrontCatalogue) {
                        for (AssetDTO relatedChild : related.flattenMeAndMyChildren().values()) {
                            rank = Math.min(rank, relatedChild.getPricingStatus().rank());
                        }
                        rank = Math.min(rank, related.getPricingStatus().rank());
                    }
                }
            }
        }
        return PricingStatus.rankOf(rank);
    }

    public boolean anyAssetsAreFirm() {
        for(AssetDTO asset : getRootInstance().flattenMeAndMyChildren().values()) {
            if(asset.anyPriceLinesAreFirm()) {
                return true;
            }
            for(AssetDTO related : asset.getRelatedToAssets()) {
                if (!pmrClient.productOffering(ProductSCode.newInstance(related.getProductCode())).get().isInFrontCatalogue) {
                    if(related.anyPriceLinesAreFirm()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean anyAssetsAreContractResigned() {
        for(AssetDTO asset : getRootInstance().flattenMeAndMyChildren().values()) {
            if(Constants.YES.equals(asset.getContractResignStatus())) {
                return true;
            }

            for(AssetDTO related : asset.getRelatedToAssets()) {
                if (!pmrClient.productOffering(ProductSCode.newInstance(related.getProductCode())).get().isInFrontCatalogue) {
                    if(Constants.YES.equals(related.getContractResignStatus())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isPricingStatusOfTreeApplicableForOnPricingTab() {
        ProductInstancePricesDTO productInstancePricesDTO = new FutureAssetPricesDTO(getRootInstance());
        int rank = getPricingRankOfRootAndItsChildren();
        for (AssetDTO related : getRootInstance().getRelatedToAssets()) {
            // If the relatedTo product isn't an inFrontCatalogue product (and its not FIRM with a NOT_APPLICABLE root product) then consider the related PricingStatus (hidden from Quote Details in R37)
            if (pmrClient.productOffering(ProductSCode.newInstance(rootInstance.getProductCode())).get().isVisibleInOnlineSummary() && !pmrClient.productOffering(ProductSCode.newInstance(related.getProductCode())).get().isInFrontCatalogue &&
                !((related.getPricingStatus().equals(PricingStatus.FIRM) || related.getPricingStatus().equals(PricingStatus.RESPONDED)) && productInstancePricesDTO.getPricingStatus().equals(PricingStatus.NOT_APPLICABLE))) {
                rank = Math.min(rank, related.getPricingStatus().rank());
            }
        }
        return !PricingStatus.rankOf(rank).equals(PricingStatus.NOT_APPLICABLE);
    }

    private int getPricingRankOfRootAndItsChildren() {
        ProductInstancePricesDTO productInstancePricesDTO = new FutureAssetPricesDTO(getRootInstance());
        int rank = productInstancePricesDTO.getPricingStatus().rank();
        for (PriceLineDTO priceLine : (List<PriceLineDTO>) productInstancePricesDTO.getPriceLines()) {
            rank = Math.min(rank, PriceLineStatus.toPricingStatus(priceLine.getStatus()).rank());
        }
        for (AssetDTO child : getRootInstance().getChildren()) {
            rank = Math.min(rank, child.getPricingStatus().rank());
        }
        return rank;
    }

    public QuoteOptionItemStatus getLineItemStatus() {
        return quoteOptionItemDTO.status;
    }

    public boolean isSlaConfigured(){
        AssetDTO rootInstance = getRootInstance();
        return !Strings.isNullOrEmpty(rootInstance.getSlaId());
    }
    public boolean isMAGConfigured(){
        AssetDTO rootInstance = getRootInstance();
        return !Strings.isNullOrEmpty(rootInstance.getMagId());
    }
    public String getMonthlyRevenueCommitment(){
        final List<PriceBookDTO> priceBooks = getPriceBooks();
        return !priceBooks.isEmpty()? priceBooks.get(0).monthlyRevenue:"";
    }

    public void visitParent(LineItemVisitor lineItemVisitor) {
        if (parent != null && !this.isSuperseded()) {
            parent.accept(lineItemVisitor);
        }
    }

    public boolean priceLinesCanBeUnLocked() {
        boolean hasApplicableLineItemStatus = (this.getStatus().equalsIgnoreCase(QuoteOptionItemStatus.DRAFT.getDescription())) ||
                                              this.getStatus().equalsIgnoreCase(QuoteOptionItemStatus.OFFERED.getDescription())||
                                              this.getStatus().equalsIgnoreCase(QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_APPROVED.getDescription()) ||
                                              this.getStatus().equalsIgnoreCase(QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_REJECTED.getDescription());
        boolean hasApplicableDiscountStatus = (this.getDiscountStatus().equalsIgnoreCase(LineItemDiscountStatus.APPROVED.getDescription()));
        boolean hasApplicablePricingStatus = (!PricingStatus.RESTRICT_UNLOCK_LINE_ITEM_STATUSES.contains(getPricingStatusOfTree()));

        return (hasApplicableDiscountStatus && hasApplicableLineItemStatus && hasApplicablePricingStatus);
    }

    public boolean isSuperseded() {
        return quoteOptionItemDTO.superseded;
    }

    private FutureAssetPricesModel fetchFutureAsset() {
        if (futureAssetPricesModel == null) {
            futureAssetPricesModel = productInstancePricesFacade.get(customerId, projectId, quoteOptionId, getRootInstance(), priceSuppressStrategy);
        }

        return futureAssetPricesModel;
    }

    public ProjectDTO getProjectDTO() {
        return projects.getProject(projectId());
    }

    public String projectId() {
        return projectId;
    }

    public String quoteOptionId() {
        return quoteOptionId;
    }

    public String customerId() {
        return customerId;
    }

    public String getContractId() {
        return contractId;
    }

    public boolean isInitialised() {
        return !INITIALIZING.equals(quoteOptionItemDTO.status);
    }

    public String getTriggerMonths() {
        final List<PriceBookDTO> priceBooks = getPriceBooks();
        return !priceBooks.isEmpty()? priceBooks.get(0).triggerMonths:"";
    }

    public boolean getIsImportable() {
        return this.quoteOptionItemDTO.isImportable;
    }

	public JaxbDateTime getCustomerRequiredDate() {
        return this.quoteOptionItemDTO.getCustomerRequiredDate();
    }

    public Date getInitialBillingStartDate() {
        return getRootInstance().detail().getInitialBillingStartDate();
    }

    private ProductOffering getBaseOffering() {
        if(null == baseProductOffering) {
            final Pmr.ProductOfferings productOfferings = pmrClient.productOffering(ProductSCode.newInstance(getProductSCode()));
            baseProductOffering = productOfferings.get();
        }
        return baseProductOffering;
    }

    public ProductOffering getProductOffering() {
        if(null == productOffering) {
            final Pmr.ProductOfferings productOfferings = pmrClient.productOffering(ProductSCode.newInstance(getProductSCode()));
            AssetDTO rootInstance = getRootInstance();
            if(null != rootInstance && !StencilId.NIL.equals(getRootInstance().getStencilId())) {
                productOfferings.withStencil(getRootInstance().getStencilId());
            }
            productOffering = productOfferings.get();
        }
        return productOffering;
    }

    public AssetDTO getRootInstance() {
        if(null == rootInstance) {
            rootInstance = productInstanceClient.getAssetDTO(getLineItemId());
        }
        return rootInstance;
    }

    public boolean isQuoteOnlyLeadToCashPhase(){
       return getProductOffering().isQuoteOnlyLeadToCashPhase();
    }

    public QuoteOptionDTO getQuoteOptionDTO() {
        return quoteOptionDTO;
    }

    public boolean isNewPriceLineAddedInTobe(){
        String result = productInstanceClient.isNewPriceLineAddedInTobe(getId());
        if(result != null){
            return new Boolean(result).booleanValue();
        }
        return false;
    }

    public boolean isComplexContractCustomer() {
        return YES.equalsIgnoreCase(productAgreementResourceClient.getContractManagedSolutionFlag(this.contractId.toString()));

    }

    public int getRemainingMonths() {
        AssetDTO rootAsset = getRootInstance();
        final Optional<AssetDTO> asIsAssetOptional = productInstanceClient.getSourceAssetDTO(rootAsset.getId());
        int remainingMonths = 0;
        Date initialBillingStartDate;
        if(asIsAssetOptional.isPresent()) {
            AssetDTO asIsAsset = asIsAssetOptional.get();
            initialBillingStartDate = asIsAsset.getInitialBillingStartDate();
            initialBillingStartDate = isNotNull(initialBillingStartDate) ? initialBillingStartDate : fetchEarlyBillingStartDateFromPriceLines(asIsAsset.getPriceLines());
            remainingMonths = ContractTermHelper.getMaxRemainingMonths(initialBillingStartDate, asIsAsset.getContractTerm());
        }

        return remainingMonths;
    }

    private Date fetchEarlyBillingStartDateFromPriceLines(List<PriceLineDTO> priceLines) {
        if(priceLines.size()==0){
            return null;
        }

        SortedSet<Date> billingStartDates = newTreeSet(Iterables.transform(validPriceLines(priceLines),
                                                                           new Function<PriceLineDTO, Date>() {
            public Date apply(PriceLineDTO input) {
                return input.getBillingStartDate();
            }
        }));

        return billingStartDates.isEmpty() ? null : billingStartDates.first();
    }

    private List<PriceLineDTO> validPriceLines(List<PriceLineDTO> priceLines) {
        return newArrayList(Iterables.filter(priceLines, new Predicate<PriceLineDTO>() {
            public boolean apply(PriceLineDTO input) {
                return isNotNull(input.getBillingStartDate());
            }
        }));
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

   public boolean isLineItemHasPriceLines(){
       return productInstanceClient.get(getLineItemId()).getPriceLines().size()>0?true:false;

   }
    interface  LineItemModelLogger {
        @Log(level = LogLevel.DEBUG, loggerName = "LineItemModelLogger", format = "LineItemMOdel setHasLineItemNotes with:[%s]")
        void logLIMsetHasLineItemNotes(boolean hasLineItemNotes);
    }

}
