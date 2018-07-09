package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilVersion;
import com.bt.rsqe.domain.bom.parameters.ProductName;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CIFAssetStencilDetailConverterTest {
    @Test
    public void shouldGetStencilIdFromCIFAssetStencilDetail() {
        final CIFAssetStencilDetail stencilDetail = new CIFAssetStencilDetail("Code", "Version", "Name", null);
        final StencilId stencilId = CIFAssetStencilDetailConverter.toStencilId(stencilDetail);

        assertThat(stencilId.getCCode(), is(StencilCode.newInstance(stencilDetail.getStencilCode())));
        assertThat(stencilId.getVersion(), is(StencilVersion.newInstance(stencilDetail.getStencilVersion())));
        assertThat(stencilId.getProductName(), is(ProductName.newInstance(stencilDetail.getProductName())));
    }
}