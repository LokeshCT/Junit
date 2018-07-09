package com.bt.rsqe.customerrecord;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ExpedioSiteCache {

    private static final CacheLog LOG = LogFactory.createDefaultLogger(CacheLog.class);

    /*
            Concurrency level has been set to be equivalent to the number of threads that *could* be concurrently accessing it.
            In our applications, this is around 10 currently.
         */
    private final Cache<String, SiteDTO> siteCache = CacheBuilder.newBuilder()
                                                                               .expireAfterWrite(30, TimeUnit.MINUTES)
                                                                               .concurrencyLevel(10)
                                                                               .build();

    private static ExpedioSiteCache cache = new ExpedioSiteCache();

    private ExpedioSiteCache() {
    }

    public static ExpedioSiteCache get() {
        return cache;
    }

    public SiteDTO get(String siteId, Callable<SiteDTO> callable) {
        try {
            final SiteDTO siteDTO = siteCache.get(siteId, callable);
            LOG.stats(siteId, siteCache.stats());
            return siteDTO;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void invalidate(String key) {
        siteCache.invalidate(key);
    }

    private interface CacheLog {

        @Log(level= LogLevel.DEBUG, format = "Expedio site cache - Key:%s, Stats: %s" )
        void stats(String siteId, CacheStats stats);
    }

}
