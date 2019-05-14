/* Copyright 2018 T-Mobile US, Inc.
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
package com.tmobile.opensource.casquatch.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.opensource.casquatch.exceptions.DriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Interface for Cassandra Tables to require getID for generic usage
 *
 * @version 1.0
 * @since   2018-02-26
 */
public abstract class AbstractCassandraTable {
    protected final static Logger logger = LoggerFactory.getLogger(AbstractCassandraTable.class);


    /**
     * Return a list of Object keys
     * @return List of key objects to pass to mapper
     */
    @JsonIgnore
    public abstract Object[] getID();

    /**
     * Return object as json string
     */
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON";
        }
    }

    /**
     * Compare two objects for equality
     * @param obj object to compare to
     */
    public boolean equals(Object obj) {
        if(obj==null) {
            return false;
        }
        else if(!this.getClass().equals(obj.getClass())) {
            logger.trace("Class does not match "+this.getClass()+" != "+obj.getClass());
            return false;
        }
        else {
            Class c = this.getClass();
            for (Method method : c.getDeclaredMethods()) {
                if (method.getName().startsWith("get") &&
                        !method.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIgnore.class) &&
                        !method.isAnnotationPresent(com.datastax.driver.mapping.annotations.Transient.class)) {
                    try {
                        if (!method.invoke(obj).equals(method.invoke(this))) {
                            logger.trace("No match on " + method.getName());
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

}
