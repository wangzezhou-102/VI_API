package com.secusoft.web.core.cache;

import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 自定义支持过期时间MapCache
 * Created by ChenYongHeng on 2018/10/19.
 */
public class TTLConcurrentMapCache extends ConcurrentMapCache {
    private int expiresTime;


    public TTLConcurrentMapCache(String name) {
        super(name, true);
        this.expiresTime = 0;
    }


    protected Object lookup(Object key) {
        Element e = (Element) super.lookup(key);
        if (e == null) {
            return null;
        }
        if (this.expiresTime == 0) {
            return e.getValue();
        }
        if (System.currentTimeMillis() - e.getCreatedAt() >= TimeUnit.SECONDS.toMillis(this.expiresTime)) {
            this.evict(key);
            return null;
        }
        return e.getValue();
    }

    protected Object fromStoreValue(Object storeValue) {
        return storeValue;
    }

    protected Object toStoreValue(Object userValue) {
        return new Element(userValue);
    }

    public void tryClean() {
        ConcurrentMap<Object, Object> map = this.getNativeCache();
        map.keySet().forEach(key -> this.lookup(key));
    }

    public int getExpiresTime() {
        return expiresTime;
    }

    public void setExpiresTime(int expiresTime) {
        this.expiresTime = expiresTime;
    }
}