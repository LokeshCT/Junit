package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadResponse;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.tpe.client.TemplateTpeClient;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Request;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Response;

import java.util.List;

import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.google.common.collect.Lists.*;

public class SpecialBidCharacteristicsReloadUpdater implements CIFAssetUpdater<SpecialBidAttributesReloadRequest, SpecialBidAttributesReloadResponse> {

    private CIFAssetOrchestrator cifAssetOrchestrator;
    private TemplateTpeClient tpeClient;
    private ExternalAttributesHelper externalAttributesHelper;
    private PmrHelper pmrHelper;
    private DependentUpdateBuilderFactory dependentUpdateBuilderFactory;

    public SpecialBidCharacteristicsReloadUpdater(CIFAssetOrchestrator cifAssetOrchestrator, TemplateTpeClient tpeClient, ExternalAttributesHelper externalAttributesHelper, PmrHelper pmrHelper, DependentUpdateBuilderFactory dependentUpdateBuilderFactory) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.tpeClient = tpeClient;
        this.externalAttributesHelper = externalAttributesHelper;
        this.pmrHelper = pmrHelper;
        this.dependentUpdateBuilderFactory = dependentUpdateBuilderFactory;
    }

    @Override
    public SpecialBidAttributesReloadResponse performUpdate(SpecialBidAttributesReloadRequest request) {
        List<CharacteristicChange> characteristicChanges = newArrayList();
        List<String> impactedCharacteristics = newArrayList();
        DependantUpdatesBuilder dependantUpdatesBuilder = dependentUpdateBuilderFactory.getDependentUpdateBuilderFactory();


        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(request.getAssetKey(), newArrayList(CIFAssetExtension.Relationships)));
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset.getProductCode());

        String productGroupName = productOffering.getProductGroupName().value();
        String configType = (String) productOffering.getAttribute(new AttributeName(CONFIGURATION_TYPE_RESERVED_NAME)).getDefaultValue().getValue();
        String tpeTemplateName = request.getValue();

        TPE_TemplateDetails_Request detailsRequest = new TPE_TemplateDetails_Request(productGroupName, configType, tpeTemplateName);
        TPE_TemplateDetails_Response detailsResponse = tpeClient.SQE_TPE_TemplateDetails(detailsRequest);

        //Reload Config Category
        CIFAssetCharacteristic configCategory = cifAsset.getCharacteristic(CONFIGURATION_CATEGORY_RESERVED_NAME);
        if (isNotNull(configCategory)) {
            characteristicChanges.add(new CharacteristicChange(CONFIGURATION_CATEGORY_RESERVED_NAME, detailsResponse.getConfiguration_Category(), configCategory.getValue()));
            configCategory.setValue(detailsResponse.getConfiguration_Category());
            impactedCharacteristics.add(CONFIGURATION_CATEGORY_RESERVED_NAME);
        }

        //Reload Bill Description
        CIFAssetCharacteristic billDescription = cifAsset.getCharacteristic(SPECIAL_BID_BILL_DESCRIPTION);
        if (isNotNull(billDescription)) {
            characteristicChanges.add(new CharacteristicChange(SPECIAL_BID_BILL_DESCRIPTION, detailsResponse.getBill_Description(), billDescription.getValue()));
            billDescription.setValue(detailsResponse.getBill_Description());
            impactedCharacteristics.add(SPECIAL_BID_BILL_DESCRIPTION);
        }

        if (isNotNull(configCategory) || isNotNull(billDescription)) {
            cifAssetOrchestrator.saveAssetAndClearCaches(cifAsset);
            dependantUpdatesBuilder.withOptional(dependentUpdateBuilderFactory.getInvalidatePriceRequestBuilder().invalidatePriceForCharacteristicChanges(cifAsset, impactedCharacteristics));
        }

        //Reload External characteristics template details
        TpeRequestDTO tpeRequestDTO = externalAttributesHelper.getAttributes(cifAsset);
        tpeRequestDTO.tpeTemplateUniqueId = detailsResponse.getTemplate_Unique_id();
        tpeRequestDTO.templateWiki = detailsResponse.getTemplate_WIKI();
        externalAttributesHelper.saveAttributes(cifAsset, tpeRequestDTO);

        return new SpecialBidAttributesReloadResponse(request, characteristicChanges, dependantUpdatesBuilder.dependantRequests());
    }
}
