package com.bt.rsqe.customerinventory.service.cache;


import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class CacheAwareTransaction {

    private static ThreadLocal<Boolean> cacheAwareThreads = new ThreadLocal<Boolean>();

    public static boolean isCacheAware() {
        return isNotNull(cacheAwareThreads.get()) && cacheAwareThreads.get();
    }

    public static void set(boolean cacheAwareTransaction) {
        cacheAwareThreads.set(cacheAwareTransaction);
    }

    public static void remove() {
        cacheAwareThreads.remove();
    }

}
