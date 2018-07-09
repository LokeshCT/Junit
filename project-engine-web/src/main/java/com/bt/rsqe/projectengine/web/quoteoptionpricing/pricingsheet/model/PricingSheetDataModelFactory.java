package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.error.RsqeApplicationException;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.BidManagerCommentsResource;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.quoteengine.domain.TpeResponseAttributeNames;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme.PriceVisibility.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

public class PricingSheetDataModelFactory {


    private ExpedioClientResources expedioClientResources;
    private static ProjectResource projectResource;
    private ProductInstanceClient futureProductInstanceClient;
    private CaveatResource caveatResource;
    private PricingClient pricingClient;

    public boolean isSpecialBidAvailable;
    public boolean isAccessAvailable;

    public PricingSheetDataModelFactory(ExpedioClientResources expedioClientResources,
                                        ProjectResource projectResource,
                                        ProductInstanceClient futureProductInstanceClient,
                                        CaveatResource caveatResource, PricingClient pricingClient) {

        this.expedioClientResources = expedioClientResources;
        this.projectResource = projectResource;
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.caveatResource = caveatResource;
        this.pricingClient = pricingClient;
    }

    public static PricingSheetDataModel create(ProjectDTO project,
                                               CustomerDTO customer,
                                               AccountManagerDTO accountManager,
                                               QuoteOptionDTO quoteOption,
                                               SiteDTO centralSite,
                                               List<PricingSheetProductModel> products,
                                               List<PricingSheetSpecialBidProduct> specialBidProductList,
                                               List<PricingSheetContractProduct> contractProducts, Set<PriceBookDTO> priceBooks, Set<String> productFamilies, List<PricingSheetProductModel> accessProduct, List<BidManagerCommentsDTO> bidManagerCommentsDTOs) {
        return new PricingSheetDataModel(project,
                                         customer,
                                         accountManager,
                                         quoteOption,
                                         centralSite,
                                         products,
                                         specialBidProductList,
                                         contractProducts,
                                         priceBooks,
                                         productFamilies, accessProduct, bidManagerCommentsDTOs);
    }

    public PricingSheetDataModel create(String customerId, String projectId, final String quoteOptionId, Optional<String> offerId) {
        boolean isSpecialbid = false;
        boolean isAccess = false;
        List<PricingSheetSpecialBidProduct> specialBidProductList = newArrayList();
        List<PricingSheetContractProduct> contractProductList = newArrayList();
        List<PricingSheetProductModel> pricingSheetProductModelList = newArrayList();
        List<PricingSheetProductModel> accessCaveatProductList = newArrayList();
        final BidManagerCommentsResource bidManagerCommentsResource = projectResource.quoteOptionResource(projectId).bidManagerCommentsResource(quoteOptionId);
        final List<BidManagerCommentsDTO> bidManagerCommentsDTOs = bidManagerCommentsResource.getAll();
        CustomerDTO customerDTO = expedioClientResources.getCustomerResource().getByToken(customerId, UserContextManager.getCurrent().getRsqeToken());
        AccountManagerDTO accountManagerDTO;
        try {
            accountManagerDTO = expedioClientResources.getCustomerResource().accountManagerResource(customerId, projectId).get();
        } catch (ResourceNotFoundException exception) {
            accountManagerDTO = new AccountManagerDTO(customerId, "", "", "", "", "", "", "");
        }
        ProjectDTO projectDTO = expedioClientResources.projectResource().getProject(projectId);
        SiteDTO centralSiteDTO;
        final List<QuoteOptionDTO> quoteOptionDTOs = projectResource.quoteOptionResource(projectId).get();
        QuoteOptionDTO quoteOptionDTO = Iterables.find(quoteOptionDTOs, new Predicate<QuoteOptionDTO>() {
            @Override
            public boolean apply(QuoteOptionDTO quoteOptionDTO1) {
                return quoteOptionDTO1.getId().equals(quoteOptionId);
            }
        });
        try {
            centralSiteDTO = expedioClientResources.getCustomerResource().siteResource(customerId).getCentralSite(projectId);
        } catch (ResourceNotFoundException e) {
            throw new RsqeApplicationException(e, "Unable to find central site for customer " + customerId);
        }
        List<QuoteOptionItemDTO> quoteOptionItemDTOs = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).get();

        if (offerId.isPresent()) {
            List<QuoteOptionItemDTO> quoteOptionItemDTOsInThisOffer = newArrayList();
            for (QuoteOptionItemDTO itemDTO : quoteOptionItemDTOs) {
                if (itemDTO.offerId.toString().equalsIgnoreCase(offerId.get())) {
                    quoteOptionItemDTOsInThisOffer.add(itemDTO);
                }
            }
            quoteOptionItemDTOs = quoteOptionItemDTOsInThisOffer;
        }

        Set<PriceBookDTO> priceBooks = newHashSet();
        Set<String> productFamily = newHashSet();

        for (QuoteOptionItemDTO quoteOptionItemDTO : quoteOptionItemDTOs) {
            ProductInstance toBeProductInstance = futureProductInstanceClient.get(new LineItemId(quoteOptionItemDTO.id));
            String productInstanceId = toBeProductInstance.getProductInstanceId().getValue();

            Optional<ProductInstance> asIsProductInstance = Optional.absent();
            if(null != toBeProductInstance.getAssetSourceVersion()) {
                asIsProductInstance = futureProductInstanceClient.getSourceAsset(new LengthConstrainingProductInstanceId(productInstanceId));
            }

            priceBooks.addAll(quoteOptionItemDTO.contractDTO.priceBooks);
            MergeResult mergeResult;
            String parentChangeType = null;
            if(quoteOptionItemDTO.isIfc){
                ProductInstance parentAsset = futureProductInstanceClient.getByAssetKey(new AssetKey(productInstanceId, toBeProductInstance.getAssetSourceVersion()));
                mergeResult = futureProductInstanceClient.getAssetsDiff(productInstanceId, toBeProductInstance.getProductInstanceVersion(),
                                                                        parentAsset.getProductInstanceVersion(), null);
                parentChangeType = mergeResult.changeFor(parentAsset).getValue();
            }


            if (asIsProductInstance.isPresent()) {
                mergeResult = futureProductInstanceClient.getAssetsDiff(productInstanceId, toBeProductInstance.getProductInstanceVersion(),
                                                                        asIsProductInstance.get().getProductInstanceVersion(), null);
            } else {
                mergeResult = futureProductInstanceClient.getAssetsDiff(productInstanceId, toBeProductInstance.getProductInstanceVersion(),
                                                                        null, null);
            }

            ProductInstance mergedProductInstance = mergeResult.getMergedProductInstances().get(0);
            SiteDTO siteDTO;
            final ProductOffering productOffering = toBeProductInstance.getProductOffering();
            // Bearer and networkNode are not separate Orderable.
            if(!SimpleProductOfferingType.Bearer.equals(productOffering.getSimpleProductOfferingType()) && !SimpleProductOfferingType.NetworkNode.equals(productOffering.getSimpleProductOfferingType())) {
                productFamily.add(toBeProductInstance.getProductGroupName().value());
            }
            if (productOffering.isSiteInstallable()) {
                siteDTO = expedioClientResources.getCustomerResource().siteResource(customerId).get(toBeProductInstance.getSiteId(), projectId);
            } else {
                siteDTO = centralSiteDTO;
            }
            if (isPriceableAssetHierarchy(mergedProductInstance) &&
                productOffering.isVisibleInOnlineSummary()) {
                final boolean applicableForJourney = isApplicableForJourney(quoteOptionItemDTO, mergedProductInstance, mergeResult, parentChangeType);
                if (productOffering.isSpecialBid() && !productOffering.isCpe() && applicableForJourney) {
                    isSpecialbid = true;
                    Map<String, String> attributes = createAttributesMap(projectId, toBeProductInstance);
                    specialBidProductList.add(new PricingSheetSpecialBidProduct(siteDTO, quoteOptionItemDTO, attributes, mergeResult, mergedProductInstance, pricingClient, asIsProductInstance));
                } else if (productOffering.isSimpleTypeOf(SimpleProductOfferingType.Contract) && applicableForJourney) {
                    contractProductList.add(new PricingSheetContractProduct(siteDTO, quoteOptionItemDTO, mergeResult, mergedProductInstance, pricingClient, asIsProductInstance));
                } else if (productOffering.isBearer()) {
                    isAccess = true;
                    final PricingSheetProductModel accessProductModel = PricingSheetProductModelFactory.create(siteDTO, mergedProductInstance, quoteOptionItemDTO, mergeResult, caveatResource, pricingClient, asIsProductInstance);
                    accessCaveatProductList.add(accessProductModel);
                }else if (!productOffering.isCpe() && applicableForJourney) {
                    pricingSheetProductModelList.add(PricingSheetProductModelFactory.create(siteDTO, mergedProductInstance, quoteOptionItemDTO, mergeResult, caveatResource, pricingClient, asIsProductInstance));
                } else if (productOffering.isInFrontCatalogue() && applicableForJourney) {
                    pricingSheetProductModelList.add(PricingSheetProductModelFactory.create(siteDTO, mergedProductInstance, quoteOptionItemDTO, mergeResult, caveatResource, pricingClient, asIsProductInstance));
                }

            }
        }
        this.isSpecialBidAvailable = isSpecialbid;
        this.isAccessAvailable = isAccess;
        return create(projectDTO,
                      customerDTO,
                      accountManagerDTO,
                      quoteOptionDTO,
                      centralSiteDTO,
                      pricingSheetProductModelList,
                      specialBidProductList,
                      contractProductList,
                      priceBooks, productFamily, accessCaveatProductList, bidManagerCommentsDTOs);
    }

    private boolean isApplicableForJourney(QuoteOptionItemDTO quoteOptionItemDTO, ProductInstance toBeProductInstance, MergeResult mergeResult, String parentChangeType) {
        ChangeType changeType = mergeResult.changeFor(toBeProductInstance);
        final List<ProductChargingScheme> productChargingSchemes = pricingClient.filterChargingSchemes(toBeProductInstance, changeType.getValue(), toBeProductInstance.getProductIdentifier().getProductId(), parentChangeType, quoteOptionItemDTO.isIfc);
        boolean applicable =  (!productChargingSchemes.isEmpty() && shouldSchemesBeDisplayedInSheet(productChargingSchemes) || (isPricelinePresentForJourney(productChargingSchemes,toBeProductInstance)));
        if(!applicable && toBeProductInstance.hasChildren()){
            for (ProductInstance childInstance : toBeProductInstance.getChildren()) {
                applicable = isApplicableForJourney(quoteOptionItemDTO, childInstance, mergeResult, changeType.getValue());
                if (applicable) {
                    break;
                }
            }
        }

        return applicable;

    }
    private boolean isPricelinePresentForJourney(List<ProductChargingScheme> productChargingSchemes, ProductInstance toBeProductInstance) {
        List<PriceLine> priceLines = toBeProductInstance.getPriceLines();
        for(ProductChargingScheme modifyScheme: productChargingSchemes){
            for(PriceLine price: priceLines){
                if(!StringUtils.isEmpty(price.getChargingSchemeName()) &&
                   modifyScheme.getName().equalsIgnoreCase(price.getChargingSchemeName())
                   && !modifyScheme.getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Hidden)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldSchemesBeDisplayedInSheet(List<ProductChargingScheme> productChargingSchemes) {
        for (ProductChargingScheme scheme : productChargingSchemes) {
            if (!Hidden.equals(scheme.getPriceVisibility())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPriceableAssetHierarchy(ProductInstance toBeProductInstance) {
        for (ProductInstance productInstance : toBeProductInstance.flattenMeAndMyChildren().values()) {
            if(!productInstance.getPriceLines().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> createAttributesMap(String projectId, ProductInstance productInstance) {
        HashMap<String, String> attributes = new HashMap<String, String>();
        try {
            attributes.put(TpeResponseAttributeNames.CONFIGURATION_CATEGORY.name(), productInstance.getInstanceCharacteristic(ProductOffering.CONFIGURATION_CATEGORY_RESERVED_NAME).getStringValue());
        } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
            attributes.put(TpeResponseAttributeNames.CONFIGURATION_CATEGORY.name(), StringUtils.EMPTY);
        }

        attributes.put(TpeResponseAttributeNames.CAVEATS.name(), getDetailedResponse(projectId, productInstance.getQuoteOptionId(), productInstance.getProductInstanceId().getValue(), productInstance.getProductInstanceVersion()));

        return attributes;
    }

    public String getDetailedResponse(String projectId, String quoteOptionId, String productInstanceId, Long productInstanceVersion) {
        final QuoteOptionItemResource itemResource = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
        final TpeRequestDTO tpeRequestDTO = itemResource.getTpeRequest(productInstanceId, productInstanceVersion);

        return tpeRequestDTO.detailedResponse;
    }
}
