package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.bom.parameters.OrderType;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.ProductInstanceStatus;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class BCMProductInstanceInfoFactory {
    private ProductInstanceClient futureProductInstanceClient;
    private BCMSiteDetailsFactory bcmSiteDetailsFactory;
    private BCMPriceLineInfoFactory bcmPriceLineInfoFactory;

    public BCMProductInstanceInfoFactory(ProductInstanceClient futureProductInstanceClient,
                                         BCMPriceLineInfoFactory bcmPriceLineInfoFactory,
                                         BCMSiteDetailsFactory bcmSiteDetailsFactory) {
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.bcmPriceLineInfoFactory = bcmPriceLineInfoFactory;
        this.bcmSiteDetailsFactory = bcmSiteDetailsFactory;
    }

    public List<BCMProductInstanceInfo> create(BCMInformer informer) {
        List<BCMProductInstanceInfo> bcmProductInstanceInfoList = newLinkedList();

        final List<QuoteOptionItemDTO> quoteOptionItems = informer.getQuoteOptionItems();
        for (QuoteOptionItemDTO quoteOptionItemDTO : quoteOptionItems) {
            ProductInstance toBeProductInstance = informer.getProductInstance(quoteOptionItemDTO.id);
            ProductInstance asIsInstance = null;
            if(ProductInstanceStatus.CEASED.equals(toBeProductInstance.getStatus())){
                Optional<ProductInstance> asIsProductInstanceOptional = futureProductInstanceClient.getSourceAsset(toBeProductInstance.getProductInstanceId().getValue());
                asIsInstance = asIsProductInstanceOptional.isPresent()? asIsProductInstanceOptional.get():asIsInstance;
            }
            MergeResult mergeResult = futureProductInstanceClient.getMergeResult(toBeProductInstance, asIsInstance, null);

            bcmProductInstanceInfoList.add(create(toBeProductInstance,
                                                  mergeResult,
                                                  informer,
                                                  quoteOptionItemDTO.getId()));
        }

        return bcmProductInstanceInfoList;
    }

    public BCMProductInstanceInfo create(ProductInstance productInstance,
                                         MergeResult mergeResult,
                                         BCMInformer informer,
                                         String quoteOptionItemId) {
        ProductIdentifier productIdentifier = productInstance.getProductIdentifier();
        ProductOffering productOffering = productInstance.getProductOffering();

        final QuoteOptionDTO quoteOptionDTO = informer.getQuoteOption();

        BCMProductInstanceInfo bcmProductInstanceInfo = new BCMProductInstanceInfo(bcmSiteDetailsFactory.create(informer, productInstance),
                bcmPriceLineInfoFactory.create(productInstance),
                productIdentifier.getProductId(),
                productInstance.getDisplayName(),
                productIdentifier.getVersionNumber(),
                productInstance.getProductInstanceId().getValue(),
                productInstance.getProductInstanceVersion(),
                findProductAction(mergeResult, productInstance),
                productInstance.getBcmPricingStatus(),
                "Indirect".equalsIgnoreCase(informer.getSalesChannelType()),
                buildBCMInstanceCharacteristicsMap(productInstance.getInstanceCharacteristics()),
                productOffering.isVisibleInOnlineSummary() && productOffering.isSpecialBid() && productOffering.isInFrontCatalogue(),
                quoteOptionDTO.getCurrency(),
                quoteOptionItemId,
                productInstance.getPriceLines(), quoteOptionDTO, productInstance.getDescription());

        for(ProductSalesRelationshipInstance relationshipInstance: productInstance.getRelationships()) {
            if(null == bcmProductInstanceInfo.getRelatedInstancesMap().get(relationshipInstance.getRelationshipName().value().toUpperCase())) {
                List<BCMProductInstanceInfo> bcmProductInstanceInfoList = newLinkedList();
                bcmProductInstanceInfo.getRelatedInstancesMap().put(relationshipInstance.getRelationshipName().value().toUpperCase(), bcmProductInstanceInfoList);
            }
            bcmProductInstanceInfo.getRelatedInstancesMap().get(relationshipInstance.getRelationshipName().value().toUpperCase()).add(this.create(relationshipInstance.getRelatedProductInstance(),
                                                                                                                                                  mergeResult,
                                                                                                                                                  informer,
                                                                                                                                                  quoteOptionItemId));
        }
        return bcmProductInstanceInfo;
    }

    private String findProductAction(MergeResult mergeResult, ProductInstance productInstance) {
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

    private Map<String, String> buildBCMInstanceCharacteristicsMap(List<InstanceCharacteristic> instanceCharacteristicList) {
        Map<String, String> bcmInstanceCharacteristicsMap = newLinkedHashMap();
        for(InstanceCharacteristic instanceCharacteristic:instanceCharacteristicList) {
            bcmInstanceCharacteristicsMap.put(instanceCharacteristic.getAttributeName().getValue().toUpperCase(), instanceCharacteristic.getStringValue());
        }
        return bcmInstanceCharacteristicsMap;
    }
}
