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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.opensource.casquatch.annotation.CasquatchIgnore;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * Abstract Entity class to extend Casquatch entities with expected functionality
 */
@Slf4j
abstract class AbstractCasquatchObject {

    /**
     * Compare two objects for equality
     * @param obj object to compare to
     * @return boolean indicating equality
     */
    public boolean equals(Object obj) {
        if(obj==null) {
            return false;
        }
        else if(!this.getClass().equals(obj.getClass())) {
            log.trace("Class does not match "+this.getClass()+" != "+obj.getClass());
            return false;
        }
        else {
            Class c = this.getClass();
            for (Method method : c.getDeclaredMethods()) {
                if (method.getName().startsWith("get") &&
                        !method.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIgnore.class) &&
                        !method.isAnnotationPresent(com.tmobile.opensource.casquatch.annotation.CasquatchIgnore.class)) {
                    try {
                        if (!(method.invoke(this) == null && method.invoke(obj)==null) && !method.invoke(obj).equals(method.invoke(this))) {
                            log.trace("No match on {}",method.getName());
                            return false;
                        }

                    } catch (Exception e) {
                        throw new DriverException(e);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Return ddl statement to create the table
     * @return ddl to create table
     */
    @JsonIgnore
    @CasquatchIgnore
    public static String getDDL() {
        throw new DriverException(DriverException.CATEGORIES.APPLICATION_DDL_QUERY,"DDL is not defined for this object");
    }

    /**
     * Return object as json string
     * @return json representation of object
     */
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }
}
