package com.concur.mobile.platform.common;

import java.util.Collection;

/**
 * A generic interface to store & manage a cache
 * 
 * @author OlivierB
 */
public interface Cache<K, V> {

    /**
     * @return cached values
     */
    V getValue(K key);

    /**
     * @return cached values
     */
    Collection<V> getValues();

    /**
     * add a value in cache
     * 
     * @param value value
     */
    void addValue(K key, V value);

    /**
     * remove values for a specific key
     * 
     * @param key key
     */
    void removeValue(K key);

    /**
     * @return true if this cache contains any value
     */
    boolean hasCachedValues();

    /**
     * clear all existing cache
     */
    void clear();
}
