package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.comparisons.ActionCalculator;
import com.bt.rsqe.productinstancemerge.ChangeType;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.Action;
import static com.bt.rsqe.productinstancemerge.ChangeType.UPDATE;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ActionExtenderTest {
    private ActionCalculator actionCalculator = mock(ActionCalculator.class);

    @Test
    public void shouldNotExtendActionWhenNotRequested() {
        CIFAsset baseAsset = mock(CIFAsset.class);

        final ActionExtender actionExtender = new ActionExtender(actionCalculator);
        actionExtender.extend(new ArrayList<CIFAssetExtension>(), baseAsset);

        verify(baseAsset, times(0)).loadAction(any(ChangeType.class));
    }

    @Test
    public void shouldLoadActionFromActionCalculator() {
        CIFAsset asIsAsset = mock(CIFAsset.class);
        CIFAsset baseAsset = mock(CIFAsset.class);
        when(baseAsset.getAsIsAsset()).thenReturn(asIsAsset);

        when(actionCalculator.getAction(baseAsset, baseAsset.getAsIsAsset())).thenReturn(UPDATE);
        final ActionExtender actionExtender = new ActionExtender(actionCalculator);
        actionExtender.extend(newArrayList(Action), baseAsset);

        verify(baseAsset, times(1)).loadAction(UPDATE);
    }
}