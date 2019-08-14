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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

/**
 * Helper class for typesafe config loading
 */
@Slf4j
public class ConfigLoader {

    private static Config root;

    /**
     * Get configuration for name. Returns empty if missing and logs values to trace
     * @param configName name of configuration
     * @return Config object
     */
    private static Config getConfig(String configName) {
        if(root().hasPath(configName)) {
            log.debug("Loading {}",configName);
            Config newConfig = root().getConfig(configName);
            for (Map.Entry<String, ConfigValue> entry : newConfig.entrySet()) {
                log.debug("{}: {} -> {}",configName,entry.getKey(),entry.getValue().render());
            }
            return newConfig;
        }
        else {
            log.trace("No values found for {}",configName);
            return ConfigFactory.empty();
        }
    }

    /**
     * Load root config
     * @return typesafe config object
     */
    private static Config root() {
        if(root==null ) {
            ConfigFactory.invalidateCaches();
            if (log.isTraceEnabled()) {
                try {
                    Enumeration<URL> classList = ConfigLoader.class.getClassLoader().getResources("reference.conf");
                    while (classList.hasMoreElements()) {
                        log.trace("Loading reference.conf @ {}", classList.nextElement().getFile());
                    }
                } catch (Exception e) {
                    log.trace("No reference.conf can be found");
                }
            }
            root = ConfigFactory.load();
        }
        return root;
    }

    /**
     * Load casquatch configuration
     * @return Config object for Casquatch
     */
    public static Config casquatch() {
        Config config = ConfigFactory.empty();
        config=getConfig("datastax-java-driver").withFallback(config);
        config=getConfig("casquatch-defaults").withFallback(config);
        config=getConfig("casquatch").withFallback(config);
        return config;
    }

    /**
     * Load casquatch configuration with prefixed config on top
     * @param prefix prefix to search for
     * @return Config object for Casquatch
     */
    public static Config casquatch(String prefix) {
        Config baseConfig=casquatch();
        return getConfig(prefix).withFallback(baseConfig);
    }

    /**
     * Clears the root cache
     */
    public static void clear() {
        root=null;
    }

    /**
     * Load configuration for generator
     * @return Config object for casquatch generator
     */
    public static Config generator() {
        return getConfig("casquatch.generator");
    }

}
