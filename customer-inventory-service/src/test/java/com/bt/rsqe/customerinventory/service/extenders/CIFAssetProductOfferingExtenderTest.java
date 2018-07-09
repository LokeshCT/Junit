package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.product.ProductGroupName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class CIFAssetProductOfferingExtenderTest {
    private final PmrHelper pmrHelper = mock(PmrHelper.class);
    private final CIFAsset baseAsset = mock(CIFAsset.class);
    private final ProductOffering baseProductOffering = mock(ProductOffering.class);

    @Before
    public void setUp() throws Exception {
        when(pmrHelper.getProductOffering(baseAsset)).thenReturn(baseProductOffering);
    }

    @Test
    public void shouldExtendWithProductOfferingDetailWhenRequested() {
        ProductIdentifier baseProductIdentifier = new ProductIdentifier("BaseProductId", "BaseProductName", "V1", "DisplayName");
        ProductIdentifier baseLegacyIdentifier = new ProductIdentifier("BaseLegacyId", "BaseLegacyName", "BaseLegacyVersion");
        when(baseProductOffering.getProductIdentifier()).thenReturn(baseProductIdentifier);
        when(baseProductOffering.getLegacyIdentifier()).thenReturn(baseLegacyIdentifier);
        when(baseProductOffering.getProposition()).thenReturn("Proposition");
        when(baseProductOffering.isCpe()).thenReturn(true);
        when(baseProductOffering.isBearer()).thenReturn(true);
        when(baseProductOffering.hasApeFlag()).thenReturn(true);
        when(baseProductOffering.getProductGroupName()).thenReturn(new ProductGroupName("GroupName"));
        when(baseProductOffering.isAvailable()).thenReturn(true);

        ProductOfferingExtender extender = new ProductOfferingExtender();
        extender.extend(newArrayList(ProductOfferingDetail), baseAsset, baseProductOffering);

        CIFAssetOfferingDetail expectedBaseOfferingDetail = new CIFAssetOfferingDetail("BaseProductName", "DisplayName", "GroupName", "BaseLegacyId", true, true, "Proposition", true, true, null);
        verify(baseAsset, times(1)).loadOfferingDetail(expectedBaseOfferingDetail);
        verify(baseAsset, times(0)).loadProductRules(anyListOf(StructuredRule.class));
    }

    @Test
    public void shouldExtendWithProductOfferingDetailWithNullDisplaynameWhenRequested() {
        ProductIdentifier baseProductIdentifier = new ProductIdentifier("BaseProductId", "BaseProductName", "V1", null);
        ProductIdentifier baseLegacyIdentifier = new ProductIdentifier("BaseLegacyId", "BaseLegacyName", "BaseLegacyVersion");
        when(baseProductOffering.getProductIdentifier()).thenReturn(baseProductIdentifier);
        when(baseProductOffering.getLegacyIdentifier()).thenReturn(baseLegacyIdentifier);
        when(baseProductOffering.getProposition()).thenReturn("Proposition");
        when(baseProductOffering.isCpe()).thenReturn(true);
        when(baseProductOffering.isBearer()).thenReturn(true);
        when(baseProductOffering.hasApeFlag()).thenReturn(true);
        when(baseProductOffering.getProductGroupName()).thenReturn(new ProductGroupName("GroupName"));
        when(baseProductOffering.isAvailable()).thenReturn(true);
        when(baseProductOffering.getSimpleProductOfferingType()).thenReturn(SimpleProductOfferingType.Package);

        ProductOfferingExtender extender = new ProductOfferingExtender();
        extender.extend(newArrayList(ProductOfferingDetail), baseAsset, baseProductOffering);

        CIFAssetOfferingDetail expectedBaseOfferingDetail = new CIFAssetOfferingDetail("BaseProductName", "BaseProductName", "GroupName", "BaseLegacyId", true, true, "Proposition", true, true, SimpleProductOfferingType.Package);
        verify(baseAsset, times(1)).loadOfferingDetail(expectedBaseOfferingDetail);
        verify(baseAsset, times(0)).loadProductRules(anyListOf(StructuredRule.class));
    }

    @Test
    public void shouldExtendWithEmptyLegacyIdWhenProductOfferingDetailHasNull() {
        ProductIdentifier baseProductIdentifier = new ProductIdentifier("BaseProductId", "BaseProductName", "V1", "DisplayName");
        when(baseProductOffering.getProductIdentifier()).thenReturn(baseProductIdentifier);
        when(baseProductOffering.getLegacyIdentifier()).thenReturn(null);
        when(baseProductOffering.getProposition()).thenReturn("Proposition");
        when(baseProductOffering.isCpe()).thenReturn(true);
        when(baseProductOffering.isBearer()).thenReturn(true);
        when(baseProductOffering.hasApeFlag()).thenReturn(true);
        when(baseProductOffering.getProductGroupName()).thenReturn(new ProductGroupName("GroupName"));
        when(baseProductOffering.isAvailable()).thenReturn(true);

        ProductOfferingExtender extender = new ProductOfferingExtender();
        extender.extend(newArrayList(ProductOfferingDetail), baseAsset, baseProductOffering);

        CIFAssetOfferingDetail expectedBaseOfferingDetail = new CIFAssetOfferingDetail("BaseProductName", "DisplayName", "GroupName", "", true, true, "Proposition", true, true, null);
        verify(baseAsset, times(1)).loadOfferingDetail(expectedBaseOfferingDetail);
        verify(baseAsset, times(0)).loadProductRules(anyListOf(StructuredRule.class));
    }

    @Test
    public void shouldExtendWithProductOfferingRulesWhenRequested() {
        ProductIdentifier baseProductIdentifier = new ProductIdentifier("BaseProductId", "BaseProductName", "V1");
        when(baseProductOffering.getProductIdentifier()).thenReturn(baseProductIdentifier);
        when(baseProductOffering.getLegacyIdentifier()).thenReturn(null);
        when(baseProductOffering.getProposition()).thenReturn("Proposition");
        when(baseProductOffering.getProductGroupName()).thenReturn(new ProductGroupName("GroupName"));

        StructuredRule expectedBaseRule1 = mock(StructuredRule.class);
        StructuredRule expectedBaseRule2 = mock(StructuredRule.class);
        List<StructuredRule> expectedBaseRules = newArrayList(expectedBaseRule1, expectedBaseRule2);

        when(baseProductOffering.getRules()).thenReturn(expectedBaseRules);

        ProductOfferingExtender extender = new ProductOfferingExtender();
        extender.extend(newArrayList(ProductRules), baseAsset, baseProductOffering);

        verify(baseAsset, times(1)).loadProductRules(expectedBaseRules);
        verify(baseAsset, times(1)).loadOfferingDetail(any(CIFAssetOfferingDetail.class));
    }

    @Test
    public void shouldNotExtendWithAnyDetailsWhenNotRequested() {
        ProductOfferingExtender extender = new ProductOfferingExtender();
        extender.extend(new ArrayList<CIFAssetExtension>(), baseAsset, baseProductOffering);

        verify(baseAsset, times(0)).loadProductRules(anyListOf(StructuredRule.class));
        verify(baseAsset, times(0)).loadOfferingDetail(any(CIFAssetOfferingDetail.class));
    }
}