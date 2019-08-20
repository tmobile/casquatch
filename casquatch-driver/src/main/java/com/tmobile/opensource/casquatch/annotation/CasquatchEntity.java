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

package com.tmobile.opensource.casquatch.annotation;

import java.lang.annotation.*;

/**
 * Annotation to flag an object as a CasquatchEntity to trigger CasquatchEntityProcessor for code generation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface CasquatchEntity {
    /**
     * Triggers the generation of the Statement Factory to implement {@link com.tmobile.opensource.casquatch.AbstractStatementFactory}
     * @return boolean indicator
     */
    boolean generateFactory() default true;

    /**
     * Triggers the generation of Test suite
     * @return boolean indicator
     */
    boolean generateTests() default false;

    /**
     * Override table name. Infered from {@link com.tmobile.opensource.casquatch.CasquatchNamingConvention#javaClassToCql(String)} if left blank
     * @return name of table
     */
    String table() default "";
}
