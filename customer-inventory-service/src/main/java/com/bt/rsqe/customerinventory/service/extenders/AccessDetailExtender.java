package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.ape.QrefRequestResource;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAccessDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.domain.bom.parameters.QrefRequestUniqueId;
import com.google.common.base.Optional;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.AccessDetail;

public class AccessDetailExtender {
    private final ApeFacade apeFacade;

    public AccessDetailExtender(ApeFacade apeFacade) {
        this.apeFacade = apeFacade;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset) {
        if(AccessDetail.isInList(cifAssetExtensions)){
            final QrefRequestUniqueId uniqueId = QrefRequestUniqueId.newInstance(cifAsset.getAssetKey().getAssetId(),
                                                                                 String.valueOf(cifAsset.getAssetKey().getAssetVersion()));
            final QrefRequestResource qrefRequestResource = apeFacade.qrefRequestResource(uniqueId);
            final Optional<QrefRequestStatus> status = qrefRequestResource.getStatus();

            if(status.isPresent()) {
                cifAsset.loadAccessDetail(new CIFAssetAccessDetail(status.get().getRequestId(), status.get().getStatus().name(), status.get().getErrorMessage()));
            }else{
                cifAsset.loadAccessDetail(CIFAssetAccessDetail.Empty);
            }
        }
    }
}
