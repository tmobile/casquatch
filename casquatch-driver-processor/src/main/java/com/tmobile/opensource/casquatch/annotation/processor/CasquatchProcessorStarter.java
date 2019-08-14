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

import com.tmobile.opensource.casquatch.CasquatchNamingConvention;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.*;

/**
 * Starter class for annotation processing
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public abstract class CasquatchProcessorStarter extends AbstractProcessor {

    protected Configuration fmConfig;
    protected RoundEnvironment roundEnv;
    protected final List<String> knownClasses = new ArrayList<>();
    protected final List<String> entityList = new ArrayList<>();

    /**
     * Abstract procedure to annotation
     * @param element list of entities
     * @return boolean for success/failure
     */
    protected abstract boolean process(Element element);

    /**
     * Create the source file based on input and template
     * @param filename name of file
     * @param template freemarker template
     * @param input input map
     * @throws Exception any exception while writing file. Ignores if file already exists.
     */
    protected void createSource(String filename, String template, Map<String, Object> input) throws Exception {
        //Allow a generated class to manually be overridden
        if(knownClasses.contains(filename)) {
            return;
        }

        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(filename);
            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                fmConfig.getTemplate(template).process(input, out);
            }
            catch (Exception e) {
                log.error("Error writing "+filename,e);
                throw e;
            }
        }
        catch(javax.annotation.processing.FilerException e) {
            log.warn("Attempted to recreate {}. Skipped",filename);
        }
        catch (Exception e) {
            log.error("Error writing "+filename,e);
            throw e;
        }
    }

    /**
     * Get a class name from an element
     * @param element element to parse
     * @return class name for element
     */
    protected String getClassName(Element element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString()+"."+element.getSimpleName().toString();
    }

    /**
     * Start the template input map for shared values
     * @param className name of class
     * @return input map
     */
    protected Map<String,Object> inputStart(String className) {
        Map<String, Object> input = new HashMap<>();
        input.put("package", CasquatchNamingConvention.classToPackageName(className));
        input.put("class", className);
        input.put("naming", new CasquatchNamingConvention());
        return input;
    }

    /**
     * Required process procedure. Parses out entity list and calls abstract process
     * @param annotations set of annotations
     * @param roundEnv environment for this processing round
     * @return boolean representing success/fail
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log.trace("Processor called");
        this.roundEnv = roundEnv;
        this.fmConfig = new Configuration(Configuration.VERSION_2_3_28);
        fmConfig.setClassForTemplateLoading(CasquatchProcessorStarter.class, "/templates/");
        fmConfig.setDefaultEncoding("UTF-8");
        fmConfig.setLocale(Locale.US);
        fmConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        fmConfig.setLogTemplateExceptions(false);
        fmConfig.setWrapUncheckedExceptions(true);

        //Generate list of known classes
        for (Element element : roundEnv.getRootElements()) {
            if(!knownClasses.contains(processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString()+"."+element.getSimpleName().toString())) {
                knownClasses.add(processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString()+"."+element.getSimpleName().toString());
                if(log.isTraceEnabled()) log.trace("Detected class: {}",processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString()+"."+element.getSimpleName().toString());
            }
        }

        //Generate list of known entities
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(com.tmobile.opensource.casquatch.annotation.CasquatchEntity.class);
        for (Element element : annotatedElements) {
            if(!entityList.contains(getClassName(element))) {
                entityList.add(getClassName(element));
                log.trace("Found Entity : {}", getClassName(element));
            }
        }
        log.trace("Found {} entities.",entityList.size());

        //Find list of entities
        boolean result = true;
        for (TypeElement annotation : annotations) {
            for(Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                result=result && process(element);
            }
        }
        return result;
    }

}
