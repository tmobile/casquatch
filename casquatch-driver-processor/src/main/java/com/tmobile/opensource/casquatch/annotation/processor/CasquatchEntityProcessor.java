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
import com.tmobile.opensource.casquatch.CasquatchNamingConvention;
import com.tmobile.opensource.casquatch.annotation.CasquatchEntity;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.HashMap;
import java.util.Map;

/**
 * Processor for {@link com.tmobile.opensource.casquatch.annotation.CasquatchEntity} to create required sources
 */
@SupportedAnnotationTypes("com.tmobile.opensource.casquatch.annotation.CasquatchEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
@Slf4j
public class CasquatchEntityProcessor  extends CasquatchProcessorStarter {

    /**
     * Process the provided entity list.
     */
    @Override
    protected boolean process(Element element) {
        CasquatchEntity casquatchEntity = element.getAnnotation(com.tmobile.opensource.casquatch.annotation.CasquatchEntity.class);
        try {
            if(casquatchEntity.generateFactory()) writeStatementFactory(getClassName(element));
            if(casquatchEntity.generateTests()) writeTests(getClassName(element));
        } catch (Exception e) {
            log.error("Failed to generate source files.", e);
            return false;
        }
        return true;
    }

    /**
     * Create entity test for class
     * @param className Entity to create test for
     * @throws Exception exception generated while creating source
     */
    private void writeTests(String className) throws Exception {
        Map<String, Object> input = inputStart(className);
        createSource(CasquatchNamingConvention.classToEmbeddedTests(className),"EmbeddedTests.ftl",input);
        createSource(CasquatchNamingConvention.classToExternalTests(className),"ExternalTests.ftl",input);
    }

    /**
     * Creates a Statement Factory class
     * @param className object holding an entity class
     * @throws Exception exception generated while creating source
     */
    private void writeStatementFactory(String className) throws Exception {
        Map<String, Object> input = inputStart(className);
        Map<String,String> keyFields = new HashMap<>();
        Map<String,String> nonKeyFields = new HashMap<>();
        Map<String,String> udtFields = new HashMap<>();

        for (Element element : roundEnv.getRootElements()) {
            if (element.getSimpleName().toString().equals(CasquatchNamingConvention.classToSimpleClass(className))) {
                for (Element enclosedElement : element.getEnclosedElements()) {
                    if (enclosedElement.getKind().equals(ElementKind.FIELD)) {
                        if(
                                enclosedElement.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class)==null &&
                                enclosedElement.getAnnotation(com.tmobile.opensource.casquatch.annotation.CasquatchIgnore.class)==null
                        ) {
                            if(enclosedElement.getAnnotation(com.tmobile.opensource.casquatch.annotation.PartitionKey.class)!=null) {
                                keyFields.put(enclosedElement.getSimpleName().toString(),enclosedElement.asType().toString());
                            }
                            else if(enclosedElement.getAnnotation(com.tmobile.opensource.casquatch.annotation.ClusteringColumn.class)!=null) {
                                keyFields.put(enclosedElement.getSimpleName().toString(),enclosedElement.asType().toString());
                            }
                            else if(enclosedElement.getAnnotation(com.tmobile.opensource.casquatch.annotation.UDT.class)!=null) {
                                udtFields.put(enclosedElement.getSimpleName().toString(),enclosedElement.asType().toString());
                            }
                            else {
                                nonKeyFields.put(enclosedElement.getSimpleName().toString(),enclosedElement.asType().toString());
                            }
                        }
                    }
                }
            }
        }

        input.put("keyFields", keyFields);
        input.put("udtFields", udtFields);
        input.put("nonKeyFields", nonKeyFields);
        createSource(CasquatchNamingConvention.classToStatementFactory(className),"StatementFactory.ftl",input);
    }

}
