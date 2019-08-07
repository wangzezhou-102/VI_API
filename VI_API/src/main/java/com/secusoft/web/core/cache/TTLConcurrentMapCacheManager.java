package com.secusoft.web.core.cache;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理
 * Created by ChenYongHeng on 2018/10/19.
 */
public class TTLConcurrentMapCacheManager extends AbstractCacheManager implements Runnable, DisposableBean {
    private List<TTLConcurrentMapCache> caches;
    private ScheduledExecutorService executor;

    public TTLConcurrentMapCacheManager() {
        this.caches = new ArrayList<>();
        (this.executor = Executors.newSingleThreadScheduledExecutor()).scheduleWithFixedDelay(this, 1L, 1L, TimeUnit.HOURS);
    }

    public void destroy() {
        this.executor.shutdown();
    }

    protected Collection<? extends Cache> loadCaches() {
        return this.caches;
    }

    public List<TTLConcurrentMapCache> getCaches() {
        return this.caches;
    }

    public void setCaches(List<TTLConcurrentMapCache> caches) {
        this.caches = caches;
    }

    public void run() {
        this.caches.forEach(TTLConcurrentMapCache::tryClean);
    }
}
