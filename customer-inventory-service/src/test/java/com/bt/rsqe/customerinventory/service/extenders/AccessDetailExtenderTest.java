package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.ape.QrefRequestResource;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAccessDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.domain.bom.parameters.QrefRequestUniqueId;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.AccessDetail;
import static com.bt.rsqe.domain.QrefRequestStatus.Status.WAITING;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class AccessDetailExtenderTest {
    private final ApeFacade apeFacade = mock(ApeFacade.class);

    @Test
    public void shouldNotExtendWithAccessDetailWhenNotRequested(){
        CIFAsset baseAsset = mock(CIFAsset.class);

        final AccessDetailExtender accessDetailExtender = new AccessDetailExtender(apeFacade);
        accessDetailExtender.extend(new ArrayList<CIFAssetExtension>(), baseAsset);

        verify(baseAsset, times(0)).loadAccessDetail(any(CIFAssetAccessDetail.class));
    }

    @Test
    public void shouldExtendWithAccessDetailWhenRequested() {
        CIFAsset baseAsset = mock(CIFAsset.class);
        when(baseAsset.getAssetKey()).thenReturn(new AssetKey("1234", 5));

        QrefRequestResource qrefRequestResource = mock(QrefRequestResource.class);
        when(qrefRequestResource.getStatus()).thenReturn(Optional.of(new QrefRequestStatus("1234_5", "requestId", WAITING, "errorMessage")));
        when(apeFacade.qrefRequestResource(new QrefRequestUniqueId("1234_5"))).thenReturn(qrefRequestResource);

        final AccessDetailExtender accessDetailExtender = new AccessDetailExtender(apeFacade);
        accessDetailExtender.extend(newArrayList(AccessDetail), baseAsset);

        verify(baseAsset, times(1)).loadAccessDetail(new CIFAssetAccessDetail("requestId", WAITING.name(), "errorMessage"));
    }

    @Test
    public void shouldExtendWithEmptyAccessDetailIfOptionalStatusIsEmpty() {
        CIFAsset baseAsset = mock(CIFAsset.class);
        when(baseAsset.getAssetKey()).thenReturn(new AssetKey("1234", 5));

        QrefRequestResource qrefRequestResource = mock(QrefRequestResource.class);
        when(qrefRequestResource.getStatus()).thenReturn(Optional.<QrefRequestStatus>absent());
        when(apeFacade.qrefRequestResource(new QrefRequestUniqueId("1234_5"))).thenReturn(qrefRequestResource);

        final AccessDetailExtender accessDetailExtender = new AccessDetailExtender(apeFacade);
        accessDetailExtender.extend(newArrayList(AccessDetail), baseAsset);

        verify(baseAsset, times(1)).loadAccessDetail(CIFAssetAccessDetail.Empty);
    }
}