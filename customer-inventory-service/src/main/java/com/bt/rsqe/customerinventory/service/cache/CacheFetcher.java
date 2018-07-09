package com.bt.rsqe.customerinventory.service.cache;


import com.bt.rsqe.cache.ResourceCache;

public abstract class CacheFetcher<K, V> {
    ThreadLocalResourceCache<K, V> cache;
    boolean cacheHit = false;

    public CacheFetcher(ThreadLocalResourceCache<K, V> cache) {
        this.cache = cache;
    }

    public V get(K key) {
        ResourceCache<K, V> resourceCache = cache.get();
        V value = resourceCache.get(key);
        cacheHit = (value != null);
        if (!cacheHit) {
            value = fetch(key);
            resourceCache.put(key, value);
        }

        return value;
    }

    public boolean isCacheHit() {
        return cacheHit;
    }

    public abstract V fetch(K key);
}