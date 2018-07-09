package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.domain.AssetKey;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.persistence.NoResultException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

class AnswerAssetKeyWithCIFAsset implements Answer<CIFAsset>
{
    private final Map<AssetKey, CIFAsset> map = newHashMap () ;

    public AnswerAssetKeyWithCIFAsset ()
    {
    }

    public AnswerAssetKeyWithCIFAsset put (AssetKey key, CIFAsset asset)
    {
        map.put(key, asset) ;
        return this ;
    }

    @Override
    public CIFAsset answer (InvocationOnMock invocation) throws Throwable
    {
        CIFAssetKey key = (CIFAssetKey) invocation.getArguments()[0];
        CIFAsset asset = map.get(key.getAssetKey()) ;
        if (asset == null)
        {
            throw new NoResultException() ;
        }

        return asset;
    }

}

