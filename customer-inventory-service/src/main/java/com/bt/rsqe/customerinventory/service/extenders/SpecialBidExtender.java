package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetBidManagerDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.customerrecord.UserRole;
import com.bt.rsqe.customerrecord.UsersDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.security.UserDTO;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.product.AssetProcessType.*;
import static com.bt.rsqe.domain.project.PricingStatus.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class SpecialBidExtender {
    private final SpecialBidReservedAttributesHelper attributeHelper;
    private final ProjectResource projectResource;
    private final SpecialBidWellKnownAttributeProvider wellKnownAttributeProvider;
    private final SpecialBidTemplateAttributeProvider templateAttributeProvider;
    private final UserResource userResource;

    public SpecialBidExtender(SpecialBidReservedAttributesHelper attributeHelper,
                              ProjectResource projectResource,
                              SpecialBidWellKnownAttributeProvider wellKnownAttributeProvider,
                              SpecialBidTemplateAttributeProvider templateAttributeProvider,
                              UserResource userResource) {
        this.attributeHelper = attributeHelper;
        this.projectResource = projectResource;
        this.wellKnownAttributeProvider = wellKnownAttributeProvider;
        this.templateAttributeProvider = templateAttributeProvider;
        this.userResource = userResource;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset, String userToken, String loginName) {
        if(SpecialBidDetail.isInList(cifAssetExtensions)) {
            if (isSpecialBid(cifAsset)) {
                TpeRequestDTO tpeRequest = getTpeRequest(cifAsset);
                cifAsset.loadSpecialBidDetails(getCifAssetSpecialBidCharacteristics(cifAsset, tpeRequest), getBidManagers(cifAsset, userToken, loginName));
            } else {
                cifAsset.loadSpecialBidDetails(new ArrayList<CIFAssetCharacteristic>(), getBidManagers(cifAsset, userToken, loginName));
            }
        }
    }

    private TpeRequestDTO getTpeRequest(CIFAsset cifAsset) {
        return projectResource.quoteOptionResource(cifAsset.getProjectId())
                              .quoteOptionItemResource(cifAsset.getQuoteOptionId())
                              .getTpeRequest(cifAsset.getAssetKey().getAssetId(), cifAsset.getAssetKey().getAssetVersion());
    }

    private List<CIFAssetCharacteristic> getCifAssetSpecialBidCharacteristics(CIFAsset cifAsset, TpeRequestDTO tpeRequest) {
        final List<CIFAssetCharacteristic> specialBidCharacteristics = wellKnownAttributeProvider.getSpecialBidCharacteristics(tpeRequest, cifAsset);
        specialBidCharacteristics.addAll(templateAttributeProvider.getSpecialBidCharacteristics(cifAsset, tpeRequest));

        for (CIFAssetCharacteristic characteristic : specialBidCharacteristics) {
            if(overrideHidden(cifAsset)) {
                characteristic.setVisible(false);
            }
            if(overrideReadOnly(cifAsset)) {
                characteristic.getAttributeDetail().setReadOnly(true);
            }
            if(overrideMandatory(cifAsset)) {
                characteristic.getAttributeDetail().setMandatory(false);
            }
        }

        return specialBidCharacteristics;
    }

    private boolean overrideMandatory(CIFAsset cifAsset) {
        return MOVE.equals(cifAsset.getAssetProcessType()) || overrideReadOnly(cifAsset);
    }

    private boolean overrideReadOnly(CIFAsset cifAsset) {
        return MOVE.equals(cifAsset.getAssetProcessType()) ||
               PROGRESSING.equals(cifAsset.getPricingStatus()) ||
               FIRM.equals(cifAsset.getPricingStatus());
    }

    private boolean overrideHidden(CIFAsset cifAsset) {
        return cifAsset.getQuoteOptionItemDetail().isMigrationQuoteOption() &&
                         cifAsset.getCategoryDetail().isMigrationLegacyBilling();
    }

    private boolean isSpecialBid(CIFAsset cifAsset) {
        CIFAssetCharacteristic specialBidCharacteristic = attributeHelper.getSpecialBidCharacteristic(cifAsset);
        return specialBidCharacteristic != null &&
               ("Y".equals(specialBidCharacteristic.getValue()) || "Yes".equals(specialBidCharacteristic.getValue()));
    }

    private boolean isIcbCpe(CIFAsset cifAsset) {
        return cifAsset.getOfferingDetail().isCPE() &&
               (cifAsset.getPricingStatus().isIcb());
    }

    private List<CIFAssetBidManagerDetail> getBidManagers(CIFAsset cifAsset, String userToken, String loginName) {
        ArrayList<CIFAssetBidManagerDetail> bidManagerList = newArrayList();
        if(isIcbCpe(cifAsset) && !isEmpty(userToken) && !isEmpty(loginName)) {
            UsersDTO bidManagers = userResource.find(cifAsset.getCustomerId(), UserRole.BID_MANAGER.value(), loginName, userToken);
            for (UserDTO user : bidManagers.users) {
                bidManagerList.add(new CIFAssetBidManagerDetail(String.format("%s %s (%s)", user.forename, user.surname, user.email), user.loginName));
            }
        }
        return bidManagerList;
    }
}
