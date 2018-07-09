package com.bt.rsqe.customerinventory.service.cache;


import com.bt.rsqe.cache.ResourceCache;

public class ThreadLocalResourceCache<K, V> extends ThreadLocal<ResourceCache<K, V>> {
    private static final int FIVE_MINUTES = 1000 * 5;

    @Override
    protected ResourceCache<K, V> initialValue() {
        return new ResourceCache<K, V>(FIVE_MINUTES);
    }

}