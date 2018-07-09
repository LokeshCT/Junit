package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.customerinventory.service.client.domain.UnloadedExtensionAccessException;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilVersion;
import com.bt.rsqe.domain.bom.parameters.ProductName;
import com.bt.rsqe.domain.product.ProductOffering;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class CIFAssetStencilDetailExtenderTest {
    public static final StencilCode STENCIL_CODE_1 = StencilCode.newInstance("StencilCode1");
    public static final StencilVersion STENCIL_VERSION_1 = StencilVersion.newInstance("StencilVersion1");
    public static final ProductName STENCIL_NAME_1 = ProductName.newInstance("StencilName1");
    private final StencilReservedAttributesHelper stencilReservedAttributesHelper = mock(StencilReservedAttributesHelper.class);
    private final StencilDetailExtender stencilDetailExtender = new StencilDetailExtender(stencilReservedAttributesHelper);
    private final ProductOffering productOffering = mock(ProductOffering.class);

    @Test
    public void shouldLoadStencilDetailsIntoProperty() {
        CIFAsset cifAsset = aCIFAsset().build();
        CIFAssetStencilDetail stencilDetail = new CIFAssetStencilDetail(STENCIL_CODE_1.getValue(),
                                                                        STENCIL_VERSION_1.getValue(),
                                                                        STENCIL_NAME_1.getValue(), new ArrayList<CIFAssetStencilDetail>());
        when(stencilReservedAttributesHelper.getStencilDetail(cifAsset)).thenReturn(stencilDetail);
        when(productOffering.isStencilUpdatable()).thenReturn(true);

        stencilDetailExtender.extend(newArrayList(StencilDetails), cifAsset);

        assertThat(cifAsset.getStencilDetail(), is(new CIFAssetStencilDetail(STENCIL_CODE_1.getValue(),
                                                                             STENCIL_VERSION_1.getValue(),
                                                                             STENCIL_NAME_1.getValue(),
                                                                             new ArrayList<CIFAssetStencilDetail>())));
    }

    @Test(expected = UnloadedExtensionAccessException.class)
    public void shouldNotLoadStencilDetailWhenNotRequested(){
        CIFAsset cifAsset = aCIFAsset().with((CIFAssetStencilDetail)null).build();
        stencilDetailExtender.extend(new ArrayList<CIFAssetExtension>(), cifAsset);

        cifAsset.getStencilDetail();
    }

    @Test
    public void shouldLoadNullExtensionDetailWhenAbsentFromOffering(){
        CIFAsset cifAsset = aCIFAsset().build();
        when(stencilReservedAttributesHelper.getStencilDetail(cifAsset)).thenReturn(null);
        when(productOffering.isStencilUpdatable()).thenReturn(false);

        stencilDetailExtender.extend(newArrayList(StencilDetails), cifAsset);

        assertThat(cifAsset.getStencilDetail(), is(nullValue()));
    }
}