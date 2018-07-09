package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.driver.SiteDriver;
import com.bt.rsqe.customerinventory.dto.AssetCharacteristicDTO;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.ServiceDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.dto.BFGAssetIdentifier;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.web.EndOfLifeValidator;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.ProductServiceDTO;
import com.bt.rsqe.projectengine.web.view.ProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.perf4j.StopWatch;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.logging.LogLevel.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.apache.commons.lang.StringUtils.*;


public class ModifyProductOrchestrator extends ProductOrchestrator<ProductSitesDTO> {
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private SiteDriver siteDriver;
    private EndOfLifeValidator endOfLifeValidator;


    public ModifyProductOrchestrator(SiteFacade siteFacade, ProductIdentifierFacade productIdentifierFacade, UriFactory productConfiguratorUriFactory,
                                     QuoteOptionFacade quoteOptionFacade, ExpedioProjectResource projectResource, Pmr pmr, SiteDriver siteDriver,
                                     ExpedioClientResources expedioClientResources, ProductInstanceClient productInstanceClient) {
        super(siteFacade, productIdentifierFacade, productConfiguratorUriFactory, quoteOptionFacade, projectResource, pmr, expedioClientResources,
              productInstanceClient);
        this.siteDriver = siteDriver;
        this.endOfLifeValidator = new EndOfLifeValidator(productInstanceClient,pmr);
    }

    @Override
    public ProductSitesDTO buildSitesView(String customerId, String projectId, PaginatedFilter paginatedFilter, String forProduct, String newSiteId,
                                          List<String> existingSiteIds, Optional<String> productVersion) {
        Map<AssetDTO, SiteDTO> assetSiteMap = newHashMap();
        List<SiteDTO> siteDTOs = siteFacade.getAllBranchSites(customerId, projectId);

        if (isNotNull(forProduct)) {
            List<BFGAssetIdentifier> activeSites = siteDriver.get(customerId, forProduct);
            activeSites = filterToInterestedSites(activeSites); //This has been added for live workaround, will be removed later.
            siteDTOs = filterSites(siteDTOs, activeSites);
            assetSiteMap = fetchMapOfInstancesToSites(activeSites, siteDTOs, productVersion);
        }

        final PaginatedFilterResult<SiteDTO> filterResult = paginatedFilter.applyTo(Lists.newArrayList(assetSiteMap.values()));
        boolean isSpecialBidProduct = productIdentifierFacade.isProductSpecialBid(forProduct);

        List<String> supportedStandardCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getSupportedCountries(forProduct);
        List<String> supportedSpecialCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getCountriesWithSpecialBidPricingType(forProduct);

        return new ProductSitesDTO(filterResult, supportedStandardCountries, supportedSpecialCountries, isSpecialBidProduct, null, false, Optional.of(assetSiteMap));
    }

    private List<BFGAssetIdentifier> filterToInterestedSites(List<BFGAssetIdentifier> activeSites) {
        if(activeSites != null && !activeSites.isEmpty() && singleSiteEnabled && isNotEmpty(siteId)) {
            Optional<BFGAssetIdentifier> assetIdentifiers = Iterables.tryFind(activeSites, new Predicate<BFGAssetIdentifier>() {
                @Override
                public boolean apply(BFGAssetIdentifier input) {
                    return input.getSiteId().equals(siteId);
                }
            });

            if(assetIdentifiers.isPresent()) {
                return newArrayList(assetIdentifiers.get());
            }
        }
        return activeSites;
    }

    private Map<AssetDTO, SiteDTO> fetchMapOfInstancesToSites(List<BFGAssetIdentifier> bfgAssetIdentifiers,
                                                              List<SiteDTO> siteDTOs,
                                                              Optional<String> productVersion) {
        Map<AssetDTO, SiteDTO> instancesToSites = newHashMap();
        if (productVersion.isPresent()) {
            for (BFGAssetIdentifier bfgAssetIdentifier : bfgAssetIdentifiers) {
                try {
                    final Optional<SiteDTO> siteDTO = fetchSite(bfgAssetIdentifier.getSiteId(), siteDTOs);
                    if (siteDTO.isPresent()) {
                        StopWatch stopwatch = new StopWatch("readAsset");
                        Optional<AssetDTO> assetDTOOptional = productInstanceClient.getSourceAssetDTO(bfgAssetIdentifier.getProductInstanceId());
                        if (assetDTOOptional.isPresent()) {
                            AssetDTO assetDTO = assetDTOOptional.get();
                            assetDTO.setDescription(getDescription(assetDTO));
                            instancesToSites.put(assetDTO, siteDTO.get());
                        }
                        logger.timeTakenToFetchAsset(bfgAssetIdentifier.getSiteId(), bfgAssetIdentifier.getProductInstanceId(), stopwatch);
                    }
                } catch (Exception e) {
                    //DO Nothing, because if any of the inService asset is not available/ having issue, all other in-service sites needs to be returned.
                    logger.readAssetFailedError(bfgAssetIdentifier.getSiteId(), bfgAssetIdentifier.getProductInstanceId());
                    logger.readError(e);
                }
            }
        }
        return instancesToSites;
    }

    private ProductOffering getOffering(AssetDTO assetDTO) {
        StencilId stencilId = assetDTO.getStencilId();
        Pmr.ProductOfferings productOfferings = pmr.productOffering(ProductSCode.newInstance(assetDTO.getProductCode()));
        if(StencilId.NIL != stencilId) {
            productOfferings.withStencil(stencilId);
        }
        return productOfferings.get();
    }

    private Optional<SiteDTO> fetchSite(final String siteId, List<SiteDTO> siteDTOs) {
        return Iterables.tryFind(siteDTOs, new Predicate<SiteDTO>() {
            @Override
            public boolean apply(@Nullable SiteDTO input) {
                return input.bfgSiteID.equals(siteId);
            }
        });
    }

    @Override
    public ProductServiceDTO buildServicesView(String customerId, String sCode, String contractId, String productVersion,
                                               Pagination pagination) {
        List<ProductInstance> productInstances = newArrayList();
        try {
            productInstances = productInstanceClient.getInServiceAssets(new CustomerId(customerId), new ContractId(contractId), new ProductCode(sCode), new ProductVersion(productVersion));
        } catch (com.bt.rsqe.web.rest.exception.ResourceNotFoundException ex) {
            // Do nothing not an error
        }
        List<ServiceDTO> serviceDTOs = toServiceDTOs(productInstances);
        final PaginatedFilterResult<ServiceDTO> paginatedFilter = new PaginatedFilterResult<ServiceDTO>(serviceDTOs.size(),
                                                         pagination.paginate(serviceDTOs),
                                                         productInstances.size(),
                                                         pagination.getPageNumber());

        return new ProductServiceDTO(paginatedFilter);
    }

    private List<ServiceDTO> toServiceDTOs(List<ProductInstance> productInstances) {
        List<ServiceDTO> serviceDTOs = newArrayList();
        for(ProductInstance productInstance : productInstances) {
            serviceDTOs.add(new ServiceDTO(productInstance.getLineItemId(), productInstance.getProductName(), getVisibleInSummaryAttributes(productInstance.getInstanceCharacteristics()),productInstance.getQuoteOptionId()));
        }
        return serviceDTOs;
    }

    private Map<String, String> getVisibleInSummaryAttributes(List<InstanceCharacteristic> instanceCharacteristics) {
        Map<String, String> attributes = new HashMap<String, String>();
        for(InstanceCharacteristic instanceCharacteristic : instanceCharacteristics) {
            if (instanceCharacteristic.isVisibleInSummary()) {
                attributes.put(instanceCharacteristic.getDisplayName().toLowerCase(), instanceCharacteristic.getStringValue());
            }
        }
        return attributes;
    }

    public String getDescription(AssetDTO assetDTO) {
        try {
            List<String> descriptions = newArrayList();
            ProductOffering productOffering = getOffering(assetDTO);

            final List<String> visibleInSummaryAttributeNames = productOffering.getVisibleInSummaryAttributeNames();

            List<AssetCharacteristicDTO> eligibleCharacteristics= newArrayList(Iterables.filter(assetDTO.getCharacteristics(), new Predicate<AssetCharacteristicDTO>() {
                @Override
                public boolean apply(AssetCharacteristicDTO input) {
                    return visibleInSummaryAttributeNames.contains(input.getName())
                            && input.getValue() != null
                            && !input.getValue().equalsIgnoreCase("0.0");
                }
            }));

            for (AssetCharacteristicDTO characteristicDTO : eligibleCharacteristics) {
                if (characteristicDTO.getName().equalsIgnoreCase(ProductOffering.STENCIL_RESERVED_NAME)) {
                    String stencilName = "";
                    if (productOffering.getStencilId().isPresent()) {
                        stencilName = productOffering.getStencilId().get().getProductName().getValue();
                    }
                    descriptions.add(isNotEmpty(stencilName) ? stencilName : characteristicDTO.getValue());
                } else {
                    descriptions.add(characteristicDTO.getValue());
                }
            }
            return StringUtils.join(descriptions, ",");
        } catch (Exception e) {
            logger.summaryError(e);
        }

        return "";
    }


    public Notification endOfLifeCheck(String siteId, String productCode, String productVersion, Date systemDate, String contractResignStatus, String lineItemId) {
        if(Constants.YES.equals(contractResignStatus) && isNotNull(siteId)){
            return endOfLifeValidator.endOfLifeCheck(siteId, productCode, productVersion, systemDate, lineItemId);
        }

        return new Notification();
    }

    interface Logger {
        @Log(level = LogLevel.ERROR, format = "Modify Asset read failed. %s")
        void readError(Exception e);

        @Log(level = LogLevel.ERROR, format = "Summary build failed. %s")
        void summaryError(Exception e);

        @Log(level = LogLevel.ERROR, format = "Site Id %s, ProductInstanceId %s")
        void readAssetFailedError(String siteId, String productInstanceId);

        @Log(level = INFO, format = "Modify Asset Fetch for site - %s,  Asset Id - %s, Stopwatch - %s")
        void timeTakenToFetchAsset(String siteId, String productInstanceId, StopWatch stopwatch);
    }
}
