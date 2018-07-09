package com.bt.rsqe.customerinventory.service.repository;

import com.bt.rsqe.ConnectionRole;
import com.bt.rsqe.asset.ivpn.dto.IVPNConfigurationDTO;
import com.bt.rsqe.asset.ivpn.dto.IVPNLegConfigurationDTO;
import com.bt.rsqe.customerinventory.parameter.AssetSourceVersion;
import com.bt.rsqe.customerinventory.parameter.BfgAssetId;
import com.bt.rsqe.customerinventory.parameter.City;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.ContractTerm;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAuxiliaryAttribute;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetError;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetExternalIdentifier;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPriceLine;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetProjectedUsage;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.domain.AssetUniqueId;
import com.bt.rsqe.domain.product.AssetProcessType;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PricingCaveat;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.sqefacade.InProgressAssetResource;
import com.bt.rsqe.sqefacade.domain.IvpnAssetId;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.bt.rsqe.IVPNAttributeName.*;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;

public class LegacySqeFacade {

    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private InProgressAssetResource inProgressAssetResource;

    public LegacySqeFacade(InProgressAssetResource inProgressAssetResource) {
        this.inProgressAssetResource = inProgressAssetResource;
    }

    public Optional<CIFAsset> getAsset(SiteId siteId, String externalId, LengthConstrainingProductInstanceId assetId, long assetVersion, ProductInstanceState assetState, boolean loadRelationships) {
        com.bt.rsqe.domain.project.SiteId id = com.bt.rsqe.domain.project.SiteId.newInstance(Long.valueOf(siteId.value()));
        IVPNConfigurationDTO iVPNConfig = null;
        try {
            iVPNConfig = inProgressAssetResource.get(id, IvpnAssetId.newInstance(externalId));
            return transform(iVPNConfig,
                             getOnlyElement(iVPNConfig.getIVPNLegConfigurations()),
                             assetId,
                             assetVersion,
                             assetState,
                             loadRelationships);
        } catch (Exception e) {
            LOG.error(iVPNConfig);
            return Optional.absent();
        }
    }

    private Optional<CIFAsset> transform(IVPNConfigurationDTO configurationDTO, IVPNLegConfigurationDTO legConfigurationDTO,
                                         LengthConstrainingProductInstanceId assetId, long assetVersion,
                                         ProductInstanceState assetState,
                                         boolean loadRelationships) {

        return Optional.of(new CIFAsset(assetId,
                                        new ProductInstanceVersion(assetVersion),
                                        new LineItemId(null),
                                        new ProductCode(ProductCodes.IpConnectGlobalLeg.productCode()),
                                        new ProductVersion(ProductIdentifier.UNDEFINED_PRODUCT_VERSION),
                                        assetState,
                                        PricingStatus.NOT_APPLICABLE,
                                        new com.bt.rsqe.customerinventory.parameter.SiteId(configurationDTO.getSiteId()),
                                        new ContractTerm(null),
                                        new CustomerId(configurationDTO.getCustomerId()),
                                        new ContractId(configurationDTO.getContractId()),
                                        new QuoteOptionId(null),
                                        AssetType.STUB,
                                        new ProjectId(null),
                                        new BfgAssetId(null),
                                        new AssetSourceVersion(null),
                                        AssetVersionStatus.IN_SERVICE,
                                        new City(null),
                                        AssetProcessType.NOT_APPLICABLE,
                                        AssetProcessType.NOT_APPLICABLE,
                                        new LengthConstrainingProductInstanceId(null),
                                        new AssetUniqueId(null),
                                        assetCharacteristics(configurationDTO, legConfigurationDTO),
                                        loadRelationships ? Optional.<List<CIFAssetRelationship>>of(new ArrayList<CIFAssetRelationship>()) : Optional.<List<CIFAssetRelationship>>absent(),
                                        new ArrayList<CIFAssetError>(),
                                        new ArrayList<PricingCaveat>(),
                                        new HashSet<CIFAssetExternalIdentifier>(),
                                        new ArrayList<CIFAssetPriceLine>(),
                                        new ArrayList<CIFAssetProjectedUsage>(),
                                        new ArrayList<CIFAssetAuxiliaryAttribute>(), ProductCategoryCode.NIL, null, null, null, null, null));

    }

    private static List<CIFAssetCharacteristic> assetCharacteristics(IVPNConfigurationDTO iVPNIVPNConfigurationDTO, IVPNLegConfigurationDTO IVPNLegConfig) {
        ConnectionRole connectionRole = IVPNLegConfig.isPrimaryLeg() ? ConnectionRole.PRIMARY : ConnectionRole.SECONDARY;

        List<CIFAssetCharacteristic> assetCharacteristics = newArrayList();
        assetCharacteristics.add(characteristicFor(PORT_SPEED.getDpv2Name(), IVPNLegConfig.getPortSpeed()));
        assetCharacteristics.add(characteristicFor(ACCESS_TECHNOLOGY.getDpv2Name(), IVPNLegConfig.getAccessTechnology()));
        assetCharacteristics.add(characteristicFor(CONNECTION_ROLE.getDpv2Name(), connectionRole.toString()));
        assetCharacteristics.add(characteristicFor(REACH_OUT_INTERCONNECT.getDpv2Name(), iVPNIVPNConfigurationDTO.isReachOutInterconnect() ? "Yes" : "No"));

        return assetCharacteristics;
    }

    private static CIFAssetCharacteristic characteristicFor(String name, String value) {
        return new CIFAssetCharacteristic(name, value, false);
    }

    private interface Logger {
        @Log(level = LogLevel.ERROR, loggerName = "IVPNQuoteMarshallerLogger", format = "Error during In-progress IVPN asset response conversions for json - '%s'")
        void error(IVPNConfigurationDTO config);
    }
}
