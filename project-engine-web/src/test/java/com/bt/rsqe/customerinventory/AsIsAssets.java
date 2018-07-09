package com.bt.rsqe.customerinventory;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FromProductInstance;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AsIsAssets{

    private Map<LengthConstrainingProductInstanceId, AssetDTO> assets;

    public AsIsAssets() {
        assets = Collections.synchronizedMap(new HashMap<LengthConstrainingProductInstanceId, AssetDTO>());
    }

    public void add(ProductInstance productInstance){
        try {
            AssetDTO dto = new FromProductInstance().toFutureAssetDTO(productInstance);
            if(dto.getId() == null) {
                throw new RuntimeException("ProductInstanceId is required for future assets");
            }
            assets.put(new LengthConstrainingProductInstanceId(dto.getId()), dto);
        } catch (InstanceCharacteristicNotFound e) {
            throw new RuntimeException(e);
        }
    }

    public int size() {
        return assets.size();
    }

    public boolean isEmpty() {
        return assets.isEmpty();
    }

    public boolean containsKey(LengthConstrainingProductInstanceId key) {
        return assets.containsKey(key);
    }

    public boolean containsValue(AssetDTO value) {
        return assets.containsValue(value);
    }

    public AssetDTO get(LengthConstrainingProductInstanceId key) {
        return assets.get(key);
    }

    public AssetDTO put(LengthConstrainingProductInstanceId key, AssetDTO value) {
        return assets.put(key, value);
    }

    public AssetDTO remove(LengthConstrainingProductInstanceId key) {
        return assets.remove(key);
    }

    public void putAll(Map<? extends LengthConstrainingProductInstanceId, ? extends AssetDTO> m) {
        assets.putAll(m);
    }

    public void clear() {
        assets.clear();
    }

    public Set<LengthConstrainingProductInstanceId> keySet() {
        return assets.keySet();
    }

    public Collection<AssetDTO> values() {
        return assets.values();
    }

    public Set<Map.Entry<LengthConstrainingProductInstanceId, AssetDTO>> entrySet() {
        return assets.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return assets.equals(o);
    }

    @Override
    public int hashCode() {
        return assets.hashCode();
    }
}
