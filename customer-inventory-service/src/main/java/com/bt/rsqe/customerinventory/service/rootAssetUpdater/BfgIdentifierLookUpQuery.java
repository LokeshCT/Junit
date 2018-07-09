package com.bt.rsqe.customerinventory.service.rootAssetUpdater;

import com.bt.rsqe.bfgfacade.exception.BfgWriteRuntimeException;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;

import static java.lang.String.*;

public enum BfgIdentifierLookUpQuery {
    Software(SimpleProductOfferingType.Software, format("select software.SW_NAME,software.sw_swp_id" + " from " +
                                                        "$owning_user$.cif_bfg_sw_mirror_v software " +
                                                        "where software.sw_id = :%s",RootAssetConstants.PRODUCT_INSTANCE_ID)),

    FOI(SimpleProductOfferingType.FOI, format("select foi.FOI_IDENTIFIER,foi.foi_feo_id" + " from " +
                                              "$owning_user$.cif_bfg_foi_mirror_v foi " +
                                              "where foi.foi_id = :%s", RootAssetConstants.PRODUCT_INSTANCE_ID)),

    VAS(SimpleProductOfferingType.VAS, format("select foi.FOI_IDENTIFIER,foi.foi_feo_id" + " from " +
                                              "$owning_user$.cif_bfg_foi_mirror_v foi " +
                                              "where foi.foi_id = :%s", RootAssetConstants.PRODUCT_INSTANCE_ID)),

    Bearer(SimpleProductOfferingType.Bearer, format("select bin.BIN_BEARER_IDENTIFIER,bin.bin_friendly_name" + " from " +
                                                    "$owning_user$.cif_bfg_bin_mirror_v bin " +
                                                    "where bin.bin_id = :%s", RootAssetConstants.PRODUCT_INSTANCE_ID)),

    VPN(SimpleProductOfferingType.VPN, format("select vnw.VNW_SERVICE_ID,vnw.vnw_pi_id" + " from " +
                                              "$owning_user$.CIF_BFG_VNW_MIRROR_V vnw " +
                                              "where vnw.vnw_id = :%s", RootAssetConstants.PRODUCT_INSTANCE_ID)),

    NetworkNode(SimpleProductOfferingType.NetworkNode, format("select ntn.NTN_MAN_HOST_NAME,ntn.ndp_name" + " from " +
                                                              "$owning_user$.cif_bfg_ntn_mirror_v ntn " +
                                                              "where ntn.ntn_id = :%s", RootAssetConstants.PRODUCT_INSTANCE_ID)),

    NetworkService(SimpleProductOfferingType.NetworkService, format("select nws.NWS_SERVICE_IDENTIFIER,nws.nwp_pmf_key" + " from " +
                                                                    "$owning_user$.cif_bfg_nws_mirror_v nws " +
                                                                    "where nws.nws_id = :%s", RootAssetConstants.PRODUCT_INSTANCE_ID)),

    Package(SimpleProductOfferingType.Package, format("select pi.PI_IDENTIFIER,pi.pi_pac_id" + " from " +
                                                      "$owning_user$.CIF_BFG_PI_MIRROR_V pi " +
                                                      "where pi.pi_id = :%s", RootAssetConstants.PRODUCT_INSTANCE_ID));

    private final SimpleProductOfferingType productOfferingType;
    private final String inventoryQuery;

    BfgIdentifierLookUpQuery(SimpleProductOfferingType productOfferingType, String inventoryQuery) {
        this.productOfferingType = productOfferingType;
        this.inventoryQuery = inventoryQuery;
    }

    public static String getInventoryIdentifierQuery(final SimpleProductOfferingType simpleProductOfferingType) {
        for (BfgIdentifierLookUpQuery bfgDummyPackageInventoryLookUpQuery : BfgIdentifierLookUpQuery.values()) {
            if (bfgDummyPackageInventoryLookUpQuery.getProductOfferingType().equals(simpleProductOfferingType)) {
                return bfgDummyPackageInventoryLookUpQuery.getInventoryQuery();
            }
        }
        throw new BfgWriteRuntimeException(String.format("Bfg Dummy package Inventory data lookup is not required for type %s", simpleProductOfferingType.name()));
    }

    public SimpleProductOfferingType getProductOfferingType() {
        return productOfferingType;
    }

    public String getInventoryQuery() {
        return inventoryQuery;
    }
}
