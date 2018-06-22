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
package com.tmobile.opensource.casquatch.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface for Cachable Cassandra Tables to require get/set of CacheKey for generic usage
 *
 * @version 1.0
 * @since   2018-02-26
 */
public abstract class AbstractCachable extends AbstractCassandraTable{
    /**
     * Set the cache key of the object
     * @param key key to define object in cache
     */
    @JsonIgnore
    public abstract void setCacheKey(String key);

    /**
     * Get the cache key of the object
     * @return value of the key for an object
     */
    @JsonIgnore
    public abstract String getCacheKey();
}
