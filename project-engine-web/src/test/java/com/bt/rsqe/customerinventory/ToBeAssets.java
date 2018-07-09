package com.bt.rsqe.customerinventory;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FromProductInstance;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ToBeAssets {
    private Map<ToBeAssets.Key, AssetDTO> assets;

    public ToBeAssets() {
        assets = Collections.synchronizedMap(new HashMap<ToBeAssets.Key, AssetDTO>());
    }

    public void add(ProductInstance productInstance){
        try {
            AssetDTO dto = new FromProductInstance().toFutureAssetDTO(productInstance);
            if(dto.getLineItemId() == null) {
                throw new RuntimeException("LineItemId is required for future assets");
            }
            if(dto.getId() == null) {
                throw new RuntimeException("ProductInstanceId is required for future assets");
            }
            put(new Key(dto), dto);
        } catch (InstanceCharacteristicNotFound e) {
            throw new RuntimeException(e);
        }
    }

    public AssetDTO put(Key key, AssetDTO value) {
        return assets.put(key, value);
    }

    public AssetDTO remove(Key key) {
        return assets.remove(key);
    }

    public void putAll(Map<? extends Key, ? extends AssetDTO> m) {
        for(Map.Entry<? extends Key, ? extends AssetDTO> entry : entrySet()){
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        assets.clear();
    }

    public Set<Key> keySet() {
        return assets.keySet();
    }

    public Collection<AssetDTO> values() {
        return assets.values();
    }

    public Set<Map.Entry<Key, AssetDTO>> entrySet() {
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

    public int size() {
        return assets.size();
    }

    public boolean isEmpty() {
        return assets.isEmpty();
    }

    public boolean containsKey(Key key) {
        return assets.containsKey(key);
    }

    public boolean containsValue(AssetDTO value) {
        return assets.containsValue(value);
    }

    public AssetDTO get(Key key) {
        return assets.get(key);
    }

    public static class Key{
        private LengthConstrainingProductInstanceId id;
        private LineItemId lineItemId;
        private static final LengthConstrainingProductInstanceId WILDCARD = new LengthConstrainingProductInstanceId("WILDCARD");

        public Key(LengthConstrainingProductInstanceId id, LineItemId lineItemId) {
            this.id = id;
            this.lineItemId = lineItemId;
        }

        public Key(AssetDTO dto){
            this.id = new LengthConstrainingProductInstanceId(dto.getId());
            this.lineItemId = new LineItemId(dto.getLineItemId());
        }

        public Key(ProductInstance instance){
            this.id = new LengthConstrainingProductInstanceId(instance.getProductInstanceId().getValue());
            this.lineItemId = new LineItemId(instance.getLineItemId());
        }

        public Key(String lineItemId) {
            this.id = WILDCARD;
            this.lineItemId = new LineItemId(lineItemId);
        }

        @Override
        public boolean equals(Object o) {
            // Overridden to support a wildcard match on ProductInstanceId
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key key = (Key) o;

            if (id != null ? !id.equals(key.id) : key.id != null) {
                if (id != WILDCARD) {
                    return false;
                }
            }
            if (lineItemId != null ? !lineItemId.equals(key.lineItemId) : key.lineItemId != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + (lineItemId != null ? lineItemId.hashCode() : 0);
            return result;
        }

    }
}
