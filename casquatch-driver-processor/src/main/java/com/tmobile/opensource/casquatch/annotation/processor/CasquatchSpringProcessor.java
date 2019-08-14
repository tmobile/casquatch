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
import com.tmobile.opensource.casquatch.annotation.CasquatchSpring;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Processor for {@link com.tmobile.opensource.casquatch.annotation.CasquatchSpring} to create required sources
 */
@SupportedAnnotationTypes({"com.tmobile.opensource.casquatch.annotation.CasquatchSpring"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
@Slf4j
public class CasquatchSpringProcessor extends CasquatchProcessorStarter {

    /**
     * Process the provided annotation
     */
    @Override
    protected boolean process(Element element) {
        CasquatchSpring casquatchSpring = element.getAnnotation(com.tmobile.opensource.casquatch.annotation.CasquatchSpring.class);
        if(entityList.size()>0) {
            for (String entity : entityList) {
                try {
                    if(casquatchSpring.generateRestDao()) writeSpringRestDao(entity,casquatchSpring);
                    if(casquatchSpring.generateRestDaoTests()) writeSpringRestDaoTests(entity);
                    if(casquatchSpring.generateEntityTests()) writeSpringEntityTests(entity);
                } catch (Exception e) {
                    log.error("Failed to generate source files.", e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Create entity test using spring for class
     * @param className Entity to create test for
     * @throws Exception exception generated while creating source
     */
    private void writeSpringEntityTests(String className) throws Exception {
        Map<String, Object> input = inputStart(className);
        createSource(CasquatchNamingConvention.classToSpringTests(className),"EntitySpringTests.ftl",input);
    }

    /**
     * Create spring rest dao configuration
     * @param className Entity to create rest dao for
     * @param casquatchSpring annotation reference
     * @throws Exception exception generated while creating source
     */
    private void writeSpringRestDao(String className,CasquatchSpring casquatchSpring) throws Exception {
        Map<String, Object> input = inputStart(className);

        if(casquatchSpring.restApi().isEmpty()) {
            input.put("restApi","");
        }
        else {
            input.put("restApi", casquatchSpring.restApi());
        }

        Class casquatch = Class.forName("com.tmobile.opensource.casquatch.CasquatchDao");
        Map<String,Method> restMethods = new HashMap<>();
        for(Method method : casquatch.getDeclaredMethods()) {
            if(method.isAnnotationPresent(com.tmobile.opensource.casquatch.annotation.Rest.class)) {
                restMethods.put(method.getAnnotation(com.tmobile.opensource.casquatch.annotation.Rest.class).value(),method);
            }
        }
        input.put("restMethods",restMethods);

        createSource(CasquatchNamingConvention.classToRestDao(className),"EntityRestDao.ftl",input);
    }

    /**
     * Create entity dao test using spring for class
     * @param className Entity to create test for
     * @throws Exception exception generated while creating source
     */
    private void writeSpringRestDaoTests(String className) throws Exception {
        Map<String, Object> input = inputStart(className);
        createSource(CasquatchNamingConvention.classToRestDaoTests(className),"EntityRestDaoTests.ftl",input);
    }

}
