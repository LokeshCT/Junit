package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator.MigratedCustomerKey;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.utils.countries.Countries;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.customerinventory.service.updates.CharacteristicValueCalculator.*;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class CharacteristicChangeRequestBuilder {
    private final PmrHelper pmrHelper;
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private Countries countries;

    public CharacteristicChangeRequestBuilder(PmrHelper pmrHelper, CIFAssetOrchestrator cifAssetOrchestrator) {
        this.pmrHelper = pmrHelper;
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.countries = new Countries();
    }

    public CharacteristicChangeRequest defaultForAllCharacteristics(CIFAsset cifAsset, String stencilCode, String relationshipName) {
        final List<CharacteristicChange> changes = new ArrayList<CharacteristicChange>();
        for (CIFAssetCharacteristic cifAssetCharacteristic : cifAsset.getCharacteristics()) {
            cifAssetCharacteristic.unloadExtension(CharacteristicAllowedValues);
        }
        cifAssetOrchestrator.forceExtendAsset(cifAsset, newArrayList(SiteDetail, CharacteristicAllowedValues, CharacteristicValue, AttributeDetails));

        for (CIFAssetCharacteristic characteristic : cifAsset.getCharacteristics()) {
            String characteristicName = characteristic.getName();
            if (STENCIL_RESERVED_NAME.equals(characteristicName) || PRODUCT_IDENTIFIER_RESERVED_NAME.equals(characteristicName)) {
                changes.add(new CharacteristicChange(characteristicName, stencilCode));
            } else if (STENCIL_VERSION_RESERVED_NAME.equals(characteristicName)) {
                ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset.getProductCode(), stencilCode);
                String versionNumber = productOffering.getProductIdentifier().getVersionNumber();
                changes.add(new CharacteristicChange(characteristicName, versionNumber));
            } else if (RELATION_NAME_ATTRIBUTE.equals(characteristicName) || CHILD_RELATION_ATTRIBUTE.equals(characteristicName)) {
                if (relationshipName != null) {
                    changes.add(new CharacteristicChange(characteristicName, relationshipName));
                }
            } else if (CONTRACT_TERM_ATTRIBUTE_NAME.equals(characteristicName)) {
                String contractTerm = characteristic.getValue();
                if(isEmpty(characteristic.getValue()))   {
                    cifAsset = cifAssetOrchestrator.forceExtendAsset(cifAsset, newArrayList(QuoteOptionItemDetail));
                    contractTerm = cifAsset.getQuoteOptionItemDetail().getContractTerm();
                }
                changes.add(new CharacteristicChange(characteristicName,contractTerm));
            } else if (REGION_ATTRIBUTE.equals(characteristicName)) {
                changes.add(new CharacteristicChange(characteristicName, countries.byIso(cifAsset.getSiteDetail().getCountryISOCode()).getExpedioName()));
            }  else if (MOVE_TYPE.equals(characteristicName)) {
                changes.add(new CharacteristicChange(characteristicName, null));   // Defaulting value to null till Move/modify implementation is  done.
            } else if (MIGRATING_ASSET.equals(characteristicName)) {
                changes.add(new CharacteristicChange(characteristicName, cifAsset.getQuoteOptionItemDetail().isMigrationQuoteOption() ? "Yes" : "No"));
            } else if (LEGACY_BILLING.equals(characteristicName)) {
                if (cifAsset.getQuoteOptionItemDetail().isMigrationQuoteOption() && cifAsset.getCategoryDetail().isMigrationLegacyBilling()) {
                    changes.add(new CharacteristicChange(characteristicName, "Yes"));
                } else {
                    final List<String> codesForCategories = pmrHelper.getPackageAndContractProductCodesForCategory(cifAsset);
                    if (!codesForCategories.isEmpty() && cifAssetOrchestrator.isMigratedCustomer(new MigratedCustomerKey(cifAsset.getCustomerId(),
                                                                                                                         cifAsset.getContractId(),
                                                                                                                         codesForCategories))) {
                        changes.add(new CharacteristicChange(characteristicName, "Yes"));
                    } else {
                        changes.add(new CharacteristicChange(characteristicName, "No"));
                    }
                }
            } else {
                changes.add(new CharacteristicChange(characteristicName, calculate(characteristic)));
            }
        }

        return new CharacteristicChangeRequest(cifAsset.getAssetKey(), new ArrayList<CIFAssetExtension>(), changes,
                                               cifAsset.getLineItemId(), cifAsset.getQuoteOptionItemDetail().getLockVersion());
    }

}
