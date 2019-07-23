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

import com.tmobile.opensource.casquatch.CasquatchSpringBeans;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Triggers Spring code generation. This should be placed on the main Application class
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(CasquatchSpringBeans.class)
@Documented
public @interface CasquatchSpring {
    /**
     * Triggers generation of RestController containing APIs from {@link com.tmobile.opensource.casquatch.CasquatchDao}
     * @return boolean indicator
     */
    boolean generateRestDao() default false;
    /**
     * This is the prefix for the base of the rest api. I.E. /app/v1
     * @return string to prefix API
     */
    String restApi() default "";

    /**
     * Triggers creation of test suite for Rest Controller
     * @return boolean indicator
     */
    boolean generateRestDaoTests() default false;

    /**
     * Triggers creation of test suite for entity using Spring configuration
     * @return boolean indicator
     */
    boolean generateEntityTests() default false;
}
