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

package com.tmobile.opensource.casquatch.annotation.processor;

import com.google.auto.service.AutoService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Map;

/**
 * Processor for {@link com.tmobile.opensource.casquatch.annotation.CasquatchBuilder} to create required sources
 */
@SupportedAnnotationTypes("com.tmobile.opensource.casquatch.annotation.CasquatchBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
@Slf4j
public class CasquatchBuilderProcessor extends CasquatchProcessorStarter {

    /**
     * Process the provided entity list.
     */
    @Override
    protected boolean process(Element element) {
        try {
            writeDaoBuilder();
        } catch (Exception e) {
            log.error("Failed to generate source files.", e);
            return false;
        }
        return true;
    }

    /**
     * Creates the Property Builder based on reference.conf files
     * @throws Exception exception generated while creating source
     */
    private void writeDaoBuilder() throws Exception {
        String className = "com.tmobile.opensource.casquatch.CasquatchDaoBuilder";
        Map<String, Object> input = inputStart(className);

        ConfigFactory.invalidateCaches();
        Config root = ConfigFactory.load(this.getClass().getClassLoader(),"reference.conf");
        Config config = root.getConfig("casquatch-template").withFallback(root.getConfig("casquatch-defaults").withFallback(root.getConfig("datastax-java-driver")));
        Map<String,String> properties = new HashMap<>();
        for(Map.Entry entry : config.entrySet()) {
            properties.put(entry.getKey().toString(),((ConfigValue)entry.getValue()).valueType().name());
        }
        input.put("properties", properties);
        createSource(className,"CasquatchDaoBuilder.ftl",input);
    }

}
