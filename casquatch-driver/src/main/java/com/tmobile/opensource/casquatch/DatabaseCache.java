/*
 * Copyright 2018 T-Mobile US, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tmobile.opensource.casquatch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


/**
 * Interface for generic database cache
 */
@Slf4j
public class DatabaseCache<E extends AbstractCasquatchEntity> {

    /**
     * Internal class to represent a cache item
     */
    @AllArgsConstructor
    @Getter
    private class CacheItem {
        E item;
        Long expiration;
    }

    private final CasquatchDao dao;
    private final Class<E> classType;
    private final Long expirationTime;
    private final Map<String, CacheItem> cacheMap;

    /**
     * Initializes the Cache
     * @param classType Type of class to cache
     * @param dao Database connection to use
     * @param expirationTime Time in milliseconds to expire cache
     */
    public DatabaseCache (Class<E> classType, CasquatchDao dao, Long expirationTime) {
        this.dao = dao;
        this.classType = classType;
        this.expirationTime = expirationTime;
        this.cacheMap = new HashMap<>();
    }

    /**
     * Initializes the Cache with a default of 15 minutes expiration
     * @param classType Type of class to cache
     * @param dao Database connection to use
     */
    public DatabaseCache (Class<E> classType, CasquatchDao dao) {
        this(classType,dao, (long) (15 * 60 * 1000));
    }

    /**
     * Get the cached object for a key
     * @param key Key for cache
     * @return Object containing the cached object
     */
    public E get(E key) {
        //noinspection unchecked
        key = (E) key.keys();
        if(checkCache(key)) {
            log.debug("DatabaseCache <{}> Returned {} from cache",this.classType,key);
            return getCache(key);
        }
        else {
            E obj = dao.getById(this.classType,key);
            if(obj == null) {
                log.debug("DatabaseCache <{}> Returned null from DB", this.classType);
                return null;
            }
            else {
                this.setCache(key, obj);
                log.debug("DatabaseCache <{}> Returned {} from DB", this.classType, key);
                return obj;
            }
        }
    }

    /**
     * Set the cached object of a key
     * @param key Name of key
     * @param obj Object to cache
     */
    public void set(E key, E obj) {
        //noinspection unchecked
        key = (E) key.keys();
        dao.save(this.classType,obj);
        this.setCache(key, obj);
    }

    /**
     * Private function to save an item to the cache
     * @param key Name of key
     * @param obj Object to cache
     */
    private void setCache(E key, E obj) {
        if(obj != null) {
            log.trace("Setting :"+obj.toString());
        }
        Long expiration = System.currentTimeMillis() + expirationTime;
        cacheMap.put(key.toString(), new CacheItem(obj,expiration));
        log.debug("DatabaseCache <{}> Added {} with key {} and  Expiration {}",this.classType,obj,key,expiration);
    }

    /**
     * Private function to get an item from the cache
     * @param key name of key
     * @return Object from cache
     */
    private E getCache(E key) {
        if ( checkCache(key)) {
            if(cacheMap.get(key.toString()) != null) {
                log.trace("Setting : {}",cacheMap.get(key.toString()).toString());
            }
            else {
                log.trace("Setting : NULL");
            }
            return cacheMap.get(key.toString()).getItem();
        } else {
            return null;
        }
    }

    /**
     * Private function to check if item is in cache
     * @param key name of key
     * @return Object from cache
     */
    private boolean checkCache(E key) {
        if ( cacheMap.containsKey(key.toString())) {
            if (cacheMap.get(key.toString()).getExpiration() >  System.currentTimeMillis()) {
                log.debug("DatabaseCache <{}> Hit: {}",this.classType,key);
                return true;
            }
            else {
                log.debug("DatabaseCache <{}> Miss(Expired): {}",this.classType,key);
                return false;
            }
        } else {
            log.debug("DatabaseCache <{}> Miss: {}",this.classType,key);
            if(log.isTraceEnabled()) {
                log.trace("The following {} keys are in the cache",cacheMap.size());
                for(Map.Entry<String,CacheItem> entry : cacheMap.entrySet()) {
                    log.trace(entry.toString());
                }
            }
            return false;
        }
    }


    /**
     * Clear the cache
     */
    public void clearCache() {
        this.cacheMap.clear();
    }
}

