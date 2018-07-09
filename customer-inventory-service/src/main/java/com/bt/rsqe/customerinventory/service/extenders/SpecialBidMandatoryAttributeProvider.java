package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFPermission;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.domain.DateFormats;
import com.bt.rsqe.domain.SpecialBidMandatoryAttribute;
import com.bt.rsqe.domain.SpecialBidWellKnownAttribute;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import org.apache.commons.lang.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.domain.product.AttributeOwner.*;
import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

public class SpecialBidMandatoryAttributeProvider {

    public String getValue(SpecialBidMandatoryAttribute attribute, CIFAsset cifAsset) {

        String value = null;

        // All of these are dependent on cif asset site detail, check it up front
        if (cifAsset != null && cifAsset.getSiteDetail() != null) {
            switch (attribute) {
                case CustomerSiteCity:
                    value = cifAsset.getSiteDetail().getCityName();
                    break;
                case CustomerSitePostCode:
                    value = cifAsset.getSiteDetail().getPostCode();
                    break;
                case CustomerSiteAddress:
                    value = cifAsset.getSiteDetail().getAddress();
                    break;
                case CustomerSiteBuildingNameOrNumber:
                    value = String.format("%s %s",
                            cifAsset.getSiteDetail().getBuilding(),
                            cifAsset.getSiteDetail().getBuildingNumber());
                    break;
                case CustomerPhoneNumber:
                    value = cifAsset.getSiteDetail().getTelephoneNumber();
                    break;
            }
        }

        if(value == null) {
            value = EMPTY;
        }

        return value;
    }

}
