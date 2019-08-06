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

import com.tmobile.opensource.casquatch.CasquatchTestDaoBuilder;
import com.tmobile.opensource.casquatch.ConfigLoader;
import com.typesafe.config.ConfigBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertTrue;

@Slf4j
public class GeneratorExternalTests {

    @Test
    public void testGenerator() throws Exception {

        CasquatchGeneratorConfiguration casquatchGeneratorConfiguration = ConfigBeanFactory.create(ConfigLoader.generator(),CasquatchGeneratorConfiguration.class);

        new CasquatchTestDaoBuilder().withDDL("CREATE TABLE IF NOT EXISTS table_name (key_one int,key_two int,col_one text,col_two text,PRIMARY KEY ((key_one), key_two))");

        new CasquatchGenerator(casquatchGeneratorConfiguration).run();

        assertTrue(new File(casquatchGeneratorConfiguration.getOutputFolder()+"/src/main/java/"+casquatchGeneratorConfiguration.getPackageName().replace(".","/")+"/TableName.java").exists());

    }
}
