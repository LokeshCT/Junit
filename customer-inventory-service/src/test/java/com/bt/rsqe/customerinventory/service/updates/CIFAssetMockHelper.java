package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;

import static org.mockito.Mockito.when;

/**
 * Created by 802998369 on 31/12/2015.
 */
public class CIFAssetMockHelper
{
    public static void mockCharacteristicAsNull(CIFAsset asset, String characteristicName)
    {
        when (asset.getCharacteristic(characteristicName)).thenReturn(null) ;
    }

    public static void mockCharacteristic(CIFAsset asset, CIFAssetCharacteristic characteristic)
    {
        when (asset.getCharacteristic(characteristic.getName())).thenReturn(characteristic) ;
    }

    public static void mockCharacteristics(CIFAsset asset, CIFAssetCharacteristic... characteristics)
    {
        for (CIFAssetCharacteristic characteristic : characteristics)
        {
            mockCharacteristic(asset, characteristic);
        }

    }


}
