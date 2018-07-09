package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCategoryDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class ProductCategoryExtenderTest{
    private final QuoteMigrationDetailsProvider migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
    private final ProductCategoryExtender productCategoryExtender = new ProductCategoryExtender(migrationDetailsProvider);
    private final CIFAsset cifAsset = mock(CIFAsset.class);

    @Test
    public void shouldNotExtendAssetWhenCategoryExtensionNotPassed() {
        productCategoryExtender.extend(new ArrayList<CIFAssetExtension>(), cifAsset);
        verify(cifAsset, times(0)).loadCategoryDetail(any(CIFAssetCategoryDetail.class));
    }

    @Test
    public void shouldReturnDefaultCategoryDetailsWhenNoMigrationDetails(){
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(cifAsset.getProductCode())).thenReturn(
            Optional.<ProductCategoryMigration>absent());

        productCategoryExtender.extend(newArrayList(CIFAssetExtension.CategoryDetail), cifAsset);
        verify(cifAsset, times(1)).loadCategoryDetail(CIFAssetCategoryDetail.DefaultValues);
    }

    @Test
    public void shouldReturnCategoryDetailBasedOnProvidedValues() {
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(cifAsset.getProductCode())).thenReturn(
            Optional.of(new ProductCategoryMigration(true, true, true)));

        productCategoryExtender.extend(newArrayList(CIFAssetExtension.CategoryDetail), cifAsset);
        verify(cifAsset, times(1)).loadCategoryDetail(new CIFAssetCategoryDetail(true));
    }
}