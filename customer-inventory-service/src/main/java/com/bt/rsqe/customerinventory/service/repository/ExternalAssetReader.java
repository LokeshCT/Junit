package com.bt.rsqe.customerinventory.service.repository;

import com.bt.rsqe.bfgfacade.domain.ServiceInstance;
import com.bt.rsqe.bfgfacade.exception.BfgReadException;
import com.bt.rsqe.bfgfacade.readers.CIFAssetVpnReader;
import com.bt.rsqe.bfgfacade.repository.BfgRepositoryJPA;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.enums.IdentifierType;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.google.common.base.Optional;

import static com.bt.rsqe.enums.IdentifierType.*;
import static com.bt.rsqe.utils.AssertObject.*;

public class ExternalAssetReader {
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private LegacySqeFacade legacySqeFacade;
    private BfgRepositoryJPA bfgReadRepository;
    private CIFAssetVpnReader cifAssetVpnReader;

    public ExternalAssetReader(LegacySqeFacade legacySqeFacade, BfgRepositoryJPA bfgReadRepository, CIFAssetVpnReader cifAssetVpnReader) {
        this.legacySqeFacade = legacySqeFacade;
        this.bfgReadRepository = bfgReadRepository;
        this.cifAssetVpnReader = cifAssetVpnReader;
    }

    public Optional<CIFAsset> read(CustomerId customerId, SiteId siteId, ProductCode productCode, LengthConstrainingProductInstanceId assetId, long assetVersion, ProductInstanceState assetState, String externalId, IdentifierType identifierType, boolean loadRelationships) {

        Optional<CIFAsset> cifAssetOptional = Optional.absent();

        if (INVENTORYID == identifierType) {
            cifAssetOptional = legacySqeFacade.getAsset(siteId, externalId, assetId, assetVersion, assetState, loadRelationships);
        } else if (NETWORKNODEID == identifierType) {
            cifAssetOptional = bfgReadRepository.getCpe(externalId, assetId, assetVersion, assetState, loadRelationships);
        } else {
            ServiceInstance serviceInstance = bfgReadRepository.getAsset(externalId);
            if (isNotNull(serviceInstance)) {
                cifAssetOptional = Optional.of(serviceInstance.toCIFAsset(assetId, assetVersion, assetState, loadRelationships));
            }
        }

        if (!cifAssetOptional.isPresent()) {
            if (IdentifierType.VPNID.equals(identifierType)) {
                try {
                    return cifAssetVpnReader.readAsset(customerId, productCode, assetId, assetVersion, assetState, Long.valueOf(externalId), siteId, loadRelationships);
                } catch (BfgReadException e) {
                    logger.readExternalAssetError(e);
                }
            }
        }

        return cifAssetOptional;
    }

    interface Logger {
        @Log(level = LogLevel.ERROR, format = "External Asset Read Error %s")
        void readExternalAssetError(Exception e);
    }
}
