/* Copyright 2018 T-Mobile US, Inc.
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tmobile.opensource.casquatch.models.AbstractCachable;

/**
 * Interface for generic database cache
 *
 * @version 1.0
 * @since   2018-02-26
 */
public class DatabaseCache<T extends AbstractCachable> {

    CassandraDriver db;

    final Class<T> classType;

    private final static Logger logger = LoggerFactory.getLogger(DatabaseCache.class);

    private Long expirationTime;

    Map<String, T> cacheMap;
    Map<String, Long> cachExpirationMap;

    /**
     * Initializes the Cache
     * @param classType Type of class to cache
     * @param db Database connection to use
     * @param expirationTime Time in milliseconds to expire cache
     */
    public DatabaseCache (Class<T> classType, CassandraDriver db, Long expirationTime) {
        this.db = db;
        this.classType = classType;
        this.expirationTime = expirationTime;
        this.cacheMap = new HashMap<String,T>();
        this.cachExpirationMap = new HashMap<String,Long>();
    }

    /**
     * Initializes the Cache with a default of 15 minutes expiration
     * @param classType Type of class to cache
     * @param db Database connection to use
     */
    public DatabaseCache (Class<T> classType, CassandraDriver db) {
        this(classType,db,Long.valueOf(15*60*1000));
    }

    /**
     * Get the cached object for a key
     * @param key Key for cache
     * @return Object containing the cached object
     */
    public T get(String key) {
        if(checkCache(key)) {
            logger.debug("DatabaseCache <"+this.classType+"+> Returned "+key+" from cache");
            return getCache(key);
        }
        else {
            T obj;
            try {
                obj = classType.newInstance();
                obj.setCacheKey(key);
                logger.trace(obj.toString());
                obj = db.getById(classType, obj);
                logger.debug("DatabaseCache <"+this.classType+"+> Returned "+key+" from DB");
            }
            catch (Exception e) {
                logger.debug("DatabaseCache <"+this.classType+"+> Returned null for "+key+" from DB");
                return null;
            }
            this.setCache(key, obj);
            return obj;
        }
    }

    /**
     * Set the cached object of a key
     * @param key Name of key
     * @param obj Object to cache
     */
    public void set(String key, T obj) {
        db.save(classType,obj);
        this.setCache(key, obj);
    }

    /**
     * Private function to save an item to the cache
     * @param key Name of key
     * @param obj Object to cache
     */
    private void setCache(String key, T obj) {
    	if(obj != null) {
    		logger.trace("Setting :"+obj.toString());
    	}
        cacheMap.put(key, obj);
        cachExpirationMap.put(key, System.currentTimeMillis() + expirationTime);
        logger.debug("DatabaseCache <"+this.classType+"+> Added "+key+" with Expiration "+System.currentTimeMillis() + expirationTime);
    }

    /**
     * Private function to get an item from the cache
     * @param key name of key
     * @return Object from cache
     */
    private T getCache(String key) {
        if (  cacheMap.containsKey(key) && cachExpirationMap.containsKey(key) && cachExpirationMap.get(key) < System.currentTimeMillis() + expirationTime) {
    		if(cacheMap.get(key) != null) {
    			logger.trace("Setting :"+cacheMap.get(key).toString());
    		}
    		else {
    			logger.trace("Setting : NULL");
    		}
            return cacheMap.get(key);
        } else {
            return null;
        }
    }

    /**
     * Private function to check if item is in cache
     * @param key name of key
     * @return Object from cache
     */
    private boolean checkCache(String key) {
        if (  cacheMap.containsKey(key) && cachExpirationMap.containsKey(key)) {
            if (cachExpirationMap.get(key) >  System.currentTimeMillis()) {
                logger.debug("DatabaseCache <" + this.classType + "+> Hit: " + key);
                return true;
            }
            else {
                logger.debug("DatabaseCache <" + this.classType + "+> Miss (Expired): " + key);
                return true;
            }
        } else {
            logger.debug("DatabaseCache <"+this.classType+"+> Miss: "+key);
            return false;
        }
    }


    /**
     * Clear the cache
     */
    public void clearCache() {
        this.cacheMap.clear();
        this.cachExpirationMap.clear();
    }
}
