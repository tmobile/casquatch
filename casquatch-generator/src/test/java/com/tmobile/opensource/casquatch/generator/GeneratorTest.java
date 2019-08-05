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

package com.tmobile.opensource.casquatch.generator;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.tmobile.opensource.casquatch.CasquatchTestDaoBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

@Slf4j
public class GeneratorTest {

    private static CasquatchGeneratorConfiguration casquatchGeneratorConfiguration;
    private static CasquatchTestDaoBuilder casquatchTestDaoBuilder;


    @BeforeClass
    public static void setUp() {
        casquatchTestDaoBuilder = new CasquatchTestDaoBuilder().withEmbedded();
        casquatchGeneratorConfiguration = new CasquatchGeneratorConfiguration();
        Config config = casquatchTestDaoBuilder.getConfig();
        for(Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            log.trace(entry.getKey()+": "+entry.getValue().render());
        }
        casquatchGeneratorConfiguration.setKeyspace(config.getString("basic.session-keyspace"));
        casquatchGeneratorConfiguration.setDatacenter(config.getString("basic.load-balancing-policy.local-datacenter"));
        casquatchGeneratorConfiguration.setContactPoints(config.getStringList("basic.contact-points"));
        log.trace(casquatchGeneratorConfiguration.toString());
    }

    @Test
    public void testGenerator() throws Exception {
        Assert.assertNotNull(casquatchGeneratorConfiguration);

        casquatchTestDaoBuilder.session().execute(SimpleStatement.builder("CREATE TABLE IF NOT EXISTS junitTest.table_name (key_one int,key_two int,col_one text,col_two text,PRIMARY KEY ((key_one), key_two))").setExecutionProfileName("ddl").build());
        CasquatchGenerator casquatchGenerator = new CasquatchGenerator(casquatchGeneratorConfiguration);

        casquatchGenerator.run();

    }
}
