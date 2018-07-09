package com.bt.rsqe.projectengine.web.tpe;

import com.bt.rsqe.LazyValue;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.project.BidStatus;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.tpe.SQETppStatusRequestBuilder;
import com.bt.rsqe.tpe.TpeException;
import com.bt.rsqe.tpe.client.PricingTpeClient;
import com.bt.rsqe.tpe.multisite.SQETppStatusChange;
import com.bt.rsqe.tpe.multisite.TppStatusResponse_Bulk;

public class TpeStatusManager {
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private PricingTpeClient tpeClient;
    private ProductInstanceClient productInstanceClient;
    private ProjectResource projectResource;

    public TpeStatusManager(PricingTpeClient tpeClient, ProductInstanceClient productInstanceClient, ProjectResource projectResource) {
        this.tpeClient = tpeClient;
        this.productInstanceClient = productInstanceClient;
        this.projectResource = projectResource;
    }

    public void checkStatusAndSendTpeStatusChangeIfRequired(LazyValue<String> userEin, OrderDTO orderDTO, PricingStatus action) {
        for (QuoteOptionItemDTO quoteOptionItemDTO : orderDTO.getOrderItems()) {
            AssetDTO asset = productInstanceClient.getAssetDTO(new LineItemId(quoteOptionItemDTO.id));
            recursivelyCheckSpecialBidAndSendTpeStatusChangeIfRequired(asset, userEin, action);
        }
    }

    public boolean cancelSiteForLineItemIfRequired(String lineItemId) {
        for(AssetDTO asset : productInstanceClient.getAssetDTO(new LineItemId(lineItemId)).flattenMeAndMyChildren().values()) {
            if(!cancelSiteForAssetIfRequired(asset)) {
                return false;
            }
        }
        return true;
    }

    private boolean cancelSiteForAssetIfRequired(AssetDTO asset) {
        if(asset.isSpecialBid() && !PricingStatus.TPE_SITE_REMOVAL_NOT_REQUIRED_STATUSES.contains(asset.getPricingStatus())) {
            ProductInstance instance = productInstanceClient.convertAssetToLightweightInstance(asset);
            if(instance.isSpecialBid()) {
                Long productInstanceVersion = null != instance.getAssetSourceVersion() ? instance.getAssetSourceVersion() : instance.getProductInstanceVersion();
                try {
                    return tpeClient.RSQE_TPE_Remove_Site(instance.getProductInstanceId().getValue() + "_" + productInstanceVersion, instance.getSpecialBidId());
                } catch (TpeException e) {
                    LOG.errorCancellingSite(e.getMessage());
                    return false;
                }
            }
        }
        // no action required for asset so return true.
        return true;
    }

    private void recursivelyCheckSpecialBidAndSendTpeStatusChangeIfRequired(AssetDTO asset, LazyValue<String> userEin, PricingStatus action){
        // check DTO is special bid before loading the product instance...
        if(asset.isSpecialBid()) {
            ProductInstance productInstance = productInstanceClient.convertAssetToLightweightInstance(asset);
            // check again for special bid as the ProductInstance check includes the !isCOTC()
            if (productInstance.isSpecialBid()) {
                TppStatusResponse_Bulk ttpStatusResponse = tpeClient.status_Refresh(SQETppStatusRequestBuilder
                                                                                        .newSQETppStatusRequest()
                                                                                        .withProductInstance(productInstance)
                                                                                        .build());
                PricingStatus status = PricingStatus.getByDescription(ttpStatusResponse.getTppOverallResponse().getStatus());
                if(action.equals(PricingStatus.ACTIVATE)) {
                    if(status.equals(PricingStatus.RESPONDED)) {
                        saveBidStatusToTpeRequest(productInstance, BidStatus.COMMITTED.getDescription());
                        changeStatus(userEin.getValue(), productInstance.getSpecialBidId(), productInstance.getSqeUniqueId().getValue(), action);
                    }
                } else if(action.equals(PricingStatus.WON)) {
                    if(status.equals(PricingStatus.COMMITTED)) {
                        saveBidStatusToTpeRequest(productInstance, BidStatus.WON.getDescription());
                        changeStatus(userEin.getValue(), productInstance.getSpecialBidId(), productInstance.getSqeUniqueId().getValue(), action);
                    }
                }
            }
        }
        for(AssetDTO assetDTO : asset.getChildren()){
            recursivelyCheckSpecialBidAndSendTpeStatusChangeIfRequired(assetDTO, userEin, action);
        }
    }

    private void saveBidStatusToTpeRequest(ProductInstance productInstance, String status) {
        TpeRequestDTO tpeRequestDTO = loadTPERequest(productInstance);
        tpeRequestDTO.bidState = status;
        putTpeRequest(productInstance, tpeRequestDTO);
    }


    private void changeStatus(String userEin, String tppId, String sqeUniqueId, PricingStatus action) {
        SQETppStatusChange sqeTppStatusChange = new SQETppStatusChange();
        sqeTppStatusChange.setUser_EIN(userEin);
        sqeTppStatusChange.setAction(action.getSubStatus());
        sqeTppStatusChange.setTPP_Id(tppId);
        sqeTppStatusChange.setSQE_Unique_Id(sqeUniqueId);
        tpeClient.change_Status(sqeTppStatusChange);
    }

    private void putTpeRequest(ProductInstance productInstance, TpeRequestDTO tpeRequest) {
        projectResource.quoteOptionResource(productInstance.getProjectId()).quoteOptionItemResource(productInstance.getQuoteOptionId()).putTpeRequest(tpeRequest);
    }

    private TpeRequestDTO loadTPERequest(ProductInstance productInstance) {
        return projectResource.quoteOptionResource(productInstance.getProjectId())
                              .quoteOptionItemResource(productInstance.getQuoteOptionId())
                              .getTpeRequest(productInstance.getProductInstanceId().getValue(), productInstance.getProductInstanceVersion());
    }

    interface Logger {
        @Log(level = LogLevel.ERROR, format = "Error cancelling site on TPE %s")
        void errorCancellingSite(String error);
    }
}