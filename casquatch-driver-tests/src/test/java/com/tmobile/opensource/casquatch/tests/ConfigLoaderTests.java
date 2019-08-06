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

package com.tmobile.opensource.casquatch.tests;

import com.tmobile.opensource.casquatch.ConfigLoader;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class ConfigLoaderTests {

    @Test
    public void testConfigPrefix() {
        String contactPoints="127.0.0.2:9045";
        String prefix="thisisatest";
        System.setProperty(prefix+".basic.contact-points.0",contactPoints);
        ConfigLoader.clear();
        Config prefixedConfig = ConfigLoader.casquatch(prefix);
        assertNotNull(prefixedConfig);
        assertEquals(prefixedConfig.getStringList("basic.contact-points").get(0),contactPoints);
    }

    @Test
    public void testConfigGenerator() {
        assertNotNull(ConfigLoader.generator());
    }
}
