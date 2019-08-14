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

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Object for holding query options.
 */
@Slf4j
public class QueryOptions {
    @Getter private Boolean ignoreNonPrimaryKeys=false;
    private String consistency;
    @Getter private Integer limit;
    @Getter private Boolean persistNulls=false;
    @Getter private String profile;
    @Getter private Integer ttl;

    /**
     * No Args Constructor
     */
    public QueryOptions() {

    }

    /**
     * Create QueryOptions from Config object. Used on CasquatchDao initialization
     * @param config populated config object
     */
    QueryOptions(Config config) {
        if(log.isTraceEnabled()) {
            log.trace("Creating Query Options From Config");
            for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
                log.debug("{}: {} -> {}","Query Options",entry.getKey(),entry.getValue().render());
            }
        }
        if(config.hasPath("ignore-non-primary-keys")) this.ignoreNonPrimaryKeys=config.getBoolean("ignore-non-primary-keys");
        if(config.hasPath("consistency")) this.consistency=config.getString("consistency");
        if(config.hasPath("limit")) this.limit=config.getInt("limit");
        if(config.hasPath("persist-nulls")) this.persistNulls=config.getBoolean("persist-nulls");
        if(config.hasPath("profile")) this.profile=config.getString("profile");
        if(config.hasPath("ttl")) this.ttl=config.getInt("ttl");

    }

    /**
     * Create QueryOptions from other QueryOptions.
     * @param queryOptions populated QueryOptions object
     */
    private QueryOptions(QueryOptions queryOptions) {
        this.ignoreNonPrimaryKeys=queryOptions.ignoreNonPrimaryKeys;
        this.consistency=queryOptions.consistency;
        this.limit=queryOptions.limit;
        this.persistNulls=queryOptions.persistNulls;
        this.profile=queryOptions.profile;
        this.ttl=queryOptions.ttl;
    }

    /**
     * Convert to JSON string
     * @return json string
     */
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }

    /**
     * Query Consistency Level as Object
     * @return object of consistencyLevel
     */
    public ConsistencyLevel getConsistencyLevel() {
        if(this.consistency!=null) {
            return DefaultConsistencyLevel.valueOf(this.consistency);
        }
        return null;
    }

    /**
     * Set to use all columns
     * @return query options with value set
     */
    public QueryOptions withAllColumns() {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.ignoreNonPrimaryKeys=false;
        return queryOptions;
    }

    /**
     * Set consistency level
     * @param consistencyLevel consistency level to use for query
     * @return query options with value set
     */
    public QueryOptions withConsistencyLevel(String consistencyLevel) {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.consistency=consistencyLevel;
        return queryOptions;
    }

    /**
     * Set query limit
     * @param limit limit for query
     * @return query options with value set
     */
    public QueryOptions withLimit(Integer limit) {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.limit=limit;
        return queryOptions;
    }

    /**
     * Set to only use primary keys
     * @return query options with value set
     */
    public QueryOptions withPrimaryKeysOnly() {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.ignoreNonPrimaryKeys=true;
        return queryOptions;
    }

    /**
     * Set execution profile
     * @param profile execution profile for query
     * @return query options with value set
     */
    public QueryOptions withProfile(String profile) {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.profile=profile;
        return queryOptions;
    }

    /**
     * Set to persist nulls
     * @return query options with value set
     */
    public QueryOptions withPersistNulls() {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.persistNulls=true;
        return queryOptions;
    }

    /**
     * Set TTL
     * @param ttl TTL for query
     * @return query options with value set
     */
    public QueryOptions withTTL(Integer ttl) {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.ttl=ttl;
        return queryOptions;
    }

    /**
     * Clear query limit
     * @return query options with value set
     */
    public QueryOptions withoutLimit() {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.limit=null;
        return queryOptions;
    }

    /**
     * Set to not persist nulls
     * @return query options with value set
     */
    public QueryOptions withoutPersistNulls() {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.persistNulls=false;
        return queryOptions;
    }

    /**
     * Clear execution profile
     * @return query options with value set
     */
    public QueryOptions withoutProfile() {
        QueryOptions queryOptions = new QueryOptions(this);
        queryOptions.profile=null;
        return queryOptions;
    }

    /**
     * Clear TTL
     * @return query options with value set
     */
    public QueryOptions withoutTTL() {
        this.ttl=null;
        return this;
    }

}
