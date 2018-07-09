package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCategoryDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.google.common.base.Optional;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.CategoryDetail;

public class ProductCategoryExtender {
    private final QuoteMigrationDetailsProvider migrationDetailsProvider;

    public ProductCategoryExtender(QuoteMigrationDetailsProvider migrationDetailsProvider) {
        this.migrationDetailsProvider = migrationDetailsProvider;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset) {
        if(CategoryDetail.isInList(cifAssetExtensions)) {
            Optional<ProductCategoryMigration> migrationDetails =
                migrationDetailsProvider.getMigrationDetailsForProductCode(cifAsset.getProductCode());

            if(migrationDetails.isPresent()){
                cifAsset.loadCategoryDetail(new CIFAssetCategoryDetail(migrationDetails.get().isMigrationLegacyBilling()));
            }else{
                cifAsset.loadCategoryDetail(CIFAssetCategoryDetail.DefaultValues);
            }
        }
    }
}
