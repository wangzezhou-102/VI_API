package com.secusoft.web.core.cache;

import java.io.Serializable;

/**
 * 缓存内容
 * Created by ChenYongHeng on 2018/10/19.
 */
public class Element implements Serializable
{

    private static final long serialVersionUID = -140974076580824974L;
    private long createdAt;
    private Object value;

    public Element(Object value) {
        this.createdAt = System.currentTimeMillis();
        this.value = value;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public Object getValue() {
        return this.value;
    }
}