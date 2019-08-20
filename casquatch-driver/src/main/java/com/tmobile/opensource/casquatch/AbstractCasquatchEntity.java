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
import com.tmobile.opensource.casquatch.annotation.CasquatchIgnore;
import com.tmobile.opensource.casquatch.annotation.ClusteringColumn;
import com.tmobile.opensource.casquatch.annotation.PartitionKey;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Abstract Entity class to extend Casquatch entities with expected functionality
 */
@Slf4j
public abstract class AbstractCasquatchEntity extends AbstractCasquatchObject {
    /**
     * Return instance of class with only primary key set. A default implementation is provided using Reflection but an explicit procedure is recommended.
     * @return instance containing only primary key
     */
    @JsonIgnore
    @CasquatchIgnore
    public AbstractCasquatchEntity keys() {
        Class<? extends AbstractCasquatchEntity> entityClass = this.getClass();
        AbstractCasquatchEntity entity;
        try {
            entity = entityClass.newInstance();
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(PartitionKey.class) || field.isAnnotationPresent(ClusteringColumn.class)) {
                    entityClass.getMethod(CasquatchNamingConvention.javaVariableToJavaSet(field.getName()),field.getType()).invoke(entity, entityClass.getMethod(CasquatchNamingConvention.javaVariableToJavaGet(field.getName())).invoke(this));
                }
            }
        } catch (Exception e) {
            throw new DriverException(DriverException.CATEGORIES.CASQUATCH_MISSING_GENERATED_CLASS, "Unable to detect primary keys");
        }
        return entity;
    }
}
