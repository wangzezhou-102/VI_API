package com.secusoft.web.core.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存配置
 * Created by ChenYongHeng on 2018/10/19.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {

        TTLConcurrentMapCache cameraCache = new TTLConcurrentMapCache("camera_list");
        cameraCache.setExpiresTime(300);

        List<TTLConcurrentMapCache> caches = new ArrayList<>();
        caches.add(cameraCache);

        TTLConcurrentMapCacheManager ttlConcurrentMapCacheManager = new TTLConcurrentMapCacheManager();
        ttlConcurrentMapCacheManager.setCaches(caches);

        return ttlConcurrentMapCacheManager;
    }


}
