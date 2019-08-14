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

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;

/**
 * Abstract class for Type Factory to transation Type objects and UdtValue
 * @param <T> Type reference
 */
public abstract class AbstractTypeFactory <T extends AbstractCasquatchType> {
    protected final UserDefinedType userDefinedType;

    /**
     * Construct TypeFactory from a UserDefinedType
     * @param userDefinedType type to base factory on
     */
    public AbstractTypeFactory(UserDefinedType userDefinedType) {
        this.userDefinedType=userDefinedType;
    }

    /**
     * Process a UdtValue into an Object
     * @param udtValue UdtValue to process
     * @return Object resulting
     */
    protected abstract T fromUdtValue(UdtValue udtValue);

    /**
     * Process an object into a UdtValue
     * @param obj object to process
     * @return UdtValue
     */
    protected abstract UdtValue toUdtValue(T obj);
}
