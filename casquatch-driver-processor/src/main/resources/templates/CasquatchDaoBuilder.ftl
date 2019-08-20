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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


/**
 * Builder for {@link CasquatchDao}
 */
@SuppressWarnings({"WeakerAccess", "SpellCheckingInspection"})
@Slf4j
public class ${naming.classToSimpleClass(class)} {

    protected final Map<String,Object> configMap = new HashMap<>();
    protected String prefix=null;
    protected String path=null;
    protected Config config;

    /**
     * Clear cached configuration
     */
    private void clearConfigCache() {
        this.config=null;
    }

    /**
     * Create CasquatchDao from configuration
     * @return configured CasquatchDao
     */
    public CasquatchDao build() {
        return new CasquatchDao(this);
    }

    /**
     * End the current profile
     * @return builder profile ended
     */
    public CasquatchDaoBuilder endProfile() {
        this.path=null;
        return this;
    }

    /**
     * Generate configuration from files as well as runtime settings
     * @return typesafe config object
     */
    public Config getConfig() {
        if(this.config == null) {
            ConfigLoader.clear();
            if (this.prefix == null) {
                this.config = ConfigLoader.casquatch();
            } else {
                this.config = ConfigLoader.casquatch(this.prefix);
            }
            if (!this.configMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : this.configMap.entrySet()) {
                    if (entry.getValue() != null && !(entry.getValue() instanceof String && ((String) entry.getValue()).isEmpty())) {
                        if (log.isTraceEnabled())
                            log.trace("Runtime Property: {} -> {}", entry.getKey(), entry.getValue());
                        this.config = this.config.withValue(entry.getKey(), ConfigValueFactory.fromAnyRef(entry.getValue()));
                    }
                }
            }
        }
        return this.config;
    }

    /**
     * Get prefix of properties
     * @return prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Providers access to a raw session based on Casquatch config
     * @return CqlSession object
     */
    public CqlSession session() {
        return this.sessionBuilder().build();
    }

    /**
     * Provides access to a raw session based on Casquatch config
     * @param keyspace override the keyspace for this session
     * @return CqlSession object
     */
    public CqlSession session(String keyspace) {
        return this.sessionBuilder().withKeyspace(keyspace).build();
    }

    /**
     * Provides access to the underlying session builder based on Casquatch config
     * @return CqlSessionBuilder object
     */
    public CqlSessionBuilder sessionBuilder() {
        return CqlSession.builder().withConfigLoader(new DefaultDriverConfigLoader(this::getConfig));
    }

    /**
     * Set the prefix for properties
     * @param prefix property prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Start a profile
     * @param profile name of profile
     * @return builder with profile started
     */
    public CasquatchDaoBuilder startProfile(String profile) {
        this.path=String.format("profiles.%s.",profile);
        return this;
    }

    /**
     * Prints out the config in JSON format
     * #return config in json format
     */
    public String toString() {
        Map<String,String> configString = new HashMap<>();
        for (Map.Entry<String, ConfigValue> entry : this.getConfig().entrySet()) {
            configString.put(entry.getKey(),entry.getValue().render());
        }
        try {
            return new ObjectMapper().writeValueAsString(configString);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }

    /**
     * Add a single value to the config
     * @param key key for value
     * @param value value (object)
     * @return builder with value set
     */
    public CasquatchDaoBuilder with(String key, Object value) {
        this.clearConfigCache();
        if(this.path !=null) {
            key = this.path + key;
        }
        this.configMap.put(key,value);
        return this;
    }

    /**
     * Add a list to the config
     * @param key key for value
     * @param valueList list of values
     * @return builder with value set
     */
    public CasquatchDaoBuilder with(String key, List<String> valueList) {
        if(this.path !=null) {
            key = this.path + key;
        }

        List<String> list;
        if(this.configMap.containsKey(key)) {
            if(this.configMap.get(key) instanceof List) {
                //noinspection unchecked
                list = (List<String>) this.configMap.get(key);
                list.addAll(valueList);
            }
            else if(this.configMap.get(key) instanceof String) {
                list = new ArrayList<>();
                list.add((String) this.configMap.get(key));
            }
            else {
                throw new DriverException(DriverException.CATEGORIES.CASQUATCH_INVALID_CONFIGURATION, "Attempted to set %s to a list but it already contained another class");
            }
        }
        else {
            list = new ArrayList<>(valueList);
        }
        this.configMap.put(key,list);
        return this;
    }


<#list properties as key,type>
    /**
     * Add value to property list mapped to ${key}
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value value for property
     * @return builder with property set
     */
    <#switch type>
        <#case "LIST">
    public CasquatchDaoBuilder ${naming.configToFunction(key)}(String value) {
        if(value.contains(",")) {
            return this.with("${key}", Collections.singletonList(value.split(",")));
        }
        else {
            return this.with("${key}", Collections.singletonList(value));
        }
    }
    /**
     * Add value to property list mapped to ${key}
     *
     * NOTE: Generated from reference.conf files on release
     *
     * @param value list of values for property
     * @return builder with property set
     */
    public CasquatchDaoBuilder ${naming.configToFunction(key)}(List<String> value) {
        return this.with("${key}",value);
    }
            <#break>
        <#case "NUMBER">
    public CasquatchDaoBuilder ${naming.configToFunction(key)}(Integer value) {
        return this.with("${key}",value);
    }
            <#break>
        <#case "BOOLEAN">
    public CasquatchDaoBuilder ${naming.configToFunction(key)}(Boolean value) {
        return this.with("${key}",value);
    }
            <#break>
        <#default>
    public CasquatchDaoBuilder ${naming.configToFunction(key)}(String value) {
        return this.with("${key}",value);
    }
    </#switch>

</#list>

}